package younin.use.client;

import com.mojang.blaze3d.platform.NativeImage;
import mtr.client.ClientData;
import mtr.data.IGui;
import mtr.mappings.Utilities;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.io.IOException;

import static mtr.data.IGui.ARGB_BLACK;
import static mtr.data.IGui.RGB_WHITE;

/**
 * 吊板贴图绘制用的文字与像素工具。
 *
 * <p>文字像素由 MTR 自己的 {@code getTextPixels} 生成（跟随游戏语言设置，双语用 {@code |} 分隔），
 * 这里只负责把像素画进 {@link NativeImage}，与天津地铁 MTR3 适配版的 {@code RouteMapGenerator}
 * 使用同一套绘制算法（MIT）。</p>
 */
public final class PlateText implements IGui {

    /** 一张文字贴图：像素、宽高，以及实际占用宽度（用于右对齐）。 */
    public record Text(byte[] pixels, int width, int height, int renderWidth) {

        public boolean isEmpty() {
            return pixels == null || pixels.length == 0 || width <= 0 || height <= 0;
        }
    }

    public static final Text EMPTY = new Text(new byte[0], 0, 0, 0);

    private PlateText() {
    }

    /**
     * 生成一张文字贴图。
     *
     * @param maxWidth 允许的最大宽度
     * @param maxHeight 允许的最大高度
     * @param fontSizeCjk CJK 字号
     * @param fontSize 拉丁字号
     * @param padding 内边距
     */
    public static Text create(String text, int maxWidth, int maxHeight, int fontSizeCjk, int fontSize, int padding, IGui.HorizontalAlignment horizontalAlignment) {
        if (text == null || text.isEmpty() || maxWidth <= 0 || maxHeight <= 0) {
            return EMPTY;
        }

        try {
            final int[] dimensions = new int[2];
            final byte[] pixels = ClientData.DATA_CACHE.getTextPixels(text, dimensions, maxWidth, maxHeight, fontSizeCjk, fontSize, padding, horizontalAlignment);
            if (pixels == null || dimensions[0] <= 0 || dimensions[1] <= 0) {
                return EMPTY;
            }
            return new Text(pixels, dimensions[0], dimensions[1], dimensions[0]);
        } catch (Exception e) {
            return EMPTY;
        }
    }

    /** 把文字贴图按对齐方式画到目标图片上；{@code backgroundColor} 不透明时会先铺底。 */
    public static void drawString(NativeImage nativeImage, Text text, int x, int y, IGui.HorizontalAlignment horizontalAlignment, IGui.VerticalAlignment verticalAlignment, int backgroundColor, int textColor, boolean rotate90) {
        if (text == null || text.isEmpty()) {
            return;
        }

        if (((backgroundColor >> 24) & 0xFF) > 0) {
            for (int drawX = 0; drawX < (rotate90 ? text.height() : text.renderWidth()); drawX++) {
                for (int drawY = 0; drawY < (rotate90 ? text.renderWidth() : text.height()); drawY++) {
                    drawPixelSafe(nativeImage, (int) horizontalAlignment.getOffset(drawX + x, (rotate90 ? text.height() : text.renderWidth())), (int) verticalAlignment.getOffset(drawY + y, (rotate90 ? text.renderWidth() : text.height())), backgroundColor);
                }
            }
        }

        int drawX = 0;
        int drawY = rotate90 ? text.renderWidth() - 1 : 0;
        for (int i = 0; i < text.renderWidth() * text.height(); i++) {
            blendPixel(nativeImage, (int) horizontalAlignment.getOffset(x + drawX, (rotate90 ? text.height() : text.renderWidth())), (int) verticalAlignment.getOffset(y + drawY, (rotate90 ? text.renderWidth() : text.height())), ((text.pixels()[i] & 0xFF) << 24) + (textColor & RGB_WHITE));
            if (rotate90) {
                drawY--;
                if (drawY < 0) {
                    drawY = text.renderWidth() - 1;
                    drawX++;
                }
            } else {
                drawX++;
                if (drawX == text.renderWidth()) {
                    drawX = 0;
                    drawY++;
                }
            }
        }
    }

    /** 把一张贴图（例如 MTR 的箭头）按目标尺寸缩放后画上去。 */
    public static void drawResource(NativeImage nativeImage, ResourceLocation resource, int x, int y, int width, int height, boolean flipX, float v1, float v2, int color) throws IOException {
        final NativeImage source = NativeImage.read(NativeImage.Format.RGBA, Utilities.getInputStream(Minecraft.getInstance().getResourceManager().getResource(resource)));
        final int resourceWidth = source.getWidth();
        final int resourceHeight = source.getHeight();

        for (int drawX = 0; drawX < width; drawX++) {
            for (int drawY = Math.round(v1 * height); drawY < Math.round(v2 * height); drawY++) {
                final float pixelX = (float) drawX / width * resourceWidth;
                final float pixelY = (float) drawY / height * resourceHeight;
                final int floorX = (int) pixelX;
                final int floorY = (int) pixelY;
                final int ceilX = floorX + 1;
                final int ceilY = floorY + 1;
                final float percentX1 = ceilX - pixelX;
                final float percentY1 = ceilY - pixelY;
                final float percentX2 = pixelX - floorX;
                final float percentY2 = pixelY - floorY;
                final int pixel1 = source.getPixelRGBA(Mth.clamp(floorX, 0, resourceWidth - 1), Mth.clamp(floorY, 0, resourceHeight - 1));
                final int pixel2 = source.getPixelRGBA(Mth.clamp(ceilX, 0, resourceWidth - 1), Mth.clamp(floorY, 0, resourceHeight - 1));
                final int pixel3 = source.getPixelRGBA(Mth.clamp(floorX, 0, resourceWidth - 1), Mth.clamp(ceilY, 0, resourceHeight - 1));
                final int pixel4 = source.getPixelRGBA(Mth.clamp(ceilX, 0, resourceWidth - 1), Mth.clamp(ceilY, 0, resourceHeight - 1));
                final float luminance1 = ((pixel1 >> 24) & 0xFF) * percentX1 * percentY1;
                final float luminance2 = ((pixel2 >> 24) & 0xFF) * percentX2 * percentY1;
                final float luminance3 = ((pixel3 >> 24) & 0xFF) * percentX1 * percentY2;
                final float luminance4 = ((pixel4 >> 24) & 0xFF) * percentX2 * percentY2;
                blendPixel(nativeImage, (flipX ? width - drawX - 1 : drawX) + x, drawY + y, (color & RGB_WHITE) + ((int) (luminance1 + luminance2 + luminance3 + luminance4) << 24));
            }
        }
    }

    public static void blendPixel(NativeImage nativeImage, int x, int y, int color) {
        if (isInside(nativeImage, x, y)) {
            final float percent = (float) ((color >> 24) & 0xFF) / 0xFF;
            if (percent > 0) {
                final int existingPixel = nativeImage.getPixelRGBA(x, y);
                final boolean existingTransparent = ((existingPixel >> 24) & 0xFF) == 0;
                final int r1 = existingTransparent ? 0xFF : (existingPixel & 0xFF);
                final int g1 = existingTransparent ? 0xFF : ((existingPixel >> 8) & 0xFF);
                final int b1 = existingTransparent ? 0xFF : ((existingPixel >> 16) & 0xFF);
                final int r2 = (color >> 16) & 0xFF;
                final int g2 = (color >> 8) & 0xFF;
                final int b2 = color & 0xFF;
                final float inversePercent = 1 - percent;
                final int finalColor = ARGB_BLACK | (((int) (r1 * inversePercent + r2 * percent) << 16) + ((int) (g1 * inversePercent + g2 * percent) << 8) + (int) (b1 * inversePercent + b2 * percent));
                drawPixelSafe(nativeImage, x, y, finalColor);
            }
        }
    }

    public static void drawPixelSafe(NativeImage nativeImage, int x, int y, int color) {
        if (isInside(nativeImage, x, y)) {
            nativeImage.setPixelRGBA(x, y, invertColor(color));
        }
    }

    public static int invertColor(int color) {
        return ((color & ARGB_BLACK) != 0 ? ARGB_BLACK : 0) + ((color & 0xFF) << 16) + (color & 0xFF00) + ((color & 0xFF0000) >> 16);
    }

    public static void clearColor(NativeImage nativeImage, int color) {
        for (int x = 0; x < nativeImage.getWidth(); x++) {
            for (int y = 0; y < nativeImage.getHeight(); y++) {
                if (nativeImage.getPixelRGBA(x, y) == color) {
                    nativeImage.setPixelRGBA(x, y, 0);
                }
            }
        }
    }

    public static void fill(NativeImage nativeImage, int color) {
        nativeImage.fillRect(0, 0, nativeImage.getWidth(), nativeImage.getHeight(), invertColor(color));
    }

    private static boolean isInside(NativeImage nativeImage, int x, int y) {
        return x >= 0 && y >= 0 && x < nativeImage.getWidth() && y < nativeImage.getHeight();
    }
}
