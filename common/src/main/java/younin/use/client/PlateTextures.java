package younin.use.client;

import com.mojang.blaze3d.platform.NativeImage;
import mtr.client.ClientData;
import mtr.data.IGui;
import mtr.data.Route;
import mtr.data.Station;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import younin.use.Use;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mtr.client.ClientCache.LINE_HEIGHT_MULTIPLIER;
import static mtr.data.IGui.ARGB_BLACK;
import static mtr.data.IGui.ARGB_WHITE;

/**
 * 三格吊板的动态贴图：站名牌 / 下一站牌 / 线路色带。
 *
 * <p>每种参数组合只生成一次并缓存（60 秒后回收），因此每帧只是取一个 {@link ResourceLocation}，
 * 不会重复绘制。生成逻辑移植自天津地铁 MTR3 适配版（ZiYueCommentary，MIT），文字像素交给
 * MTR 自己的 {@code getTextPixels}，字体跟随玩家设置。</p>
 */
public final class PlateTextures {

    /** 贴图高度（像素）；宽度按传入的宽高比换算。 */
    public static final int PLATE_HEIGHT = 64;

    private static final long TTL_MILLIS = 60_000L;
    private static final ResourceLocation ARROW_RESOURCE = new ResourceLocation("mtr", "textures/block/sign/arrow.png");
    private static final Map<String, Entry> CACHE = new HashMap<>();

    private PlateTextures() {
    }

    private record Entry(ResourceLocation resourceLocation, long timestamp) {
    }

    /** 站名牌：整块底板 + 居中的站名。 */
    public static ResourceLocation stationName(long platformId, float paddingScale, float aspectRatio, int backgroundColor, int textColor, int transparentColor) {
        if (aspectRatio <= 0 || platformId == 0) {
            return null;
        }

        return get("station_" + platformId + "_" + paddingScale + "_" + aspectRatio + "_" + backgroundColor + "_" + textColor + "_" + transparentColor, () -> {
            final int height = PLATE_HEIGHT;
            final int width = Math.round(height * aspectRatio);
            final int padding = Math.round(height * paddingScale);
            final int tileSize = height - padding * 2;
            if (width <= 0 || tileSize <= 0) {
                return null;
            }

            final NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
            PlateText.fill(image, backgroundColor);

            final PlateText.Text name = PlateText.create(getStationName(platformId), width - padding * 2, (int) (tileSize * LINE_HEIGHT_MULTIPLIER), tileSize * 3, tileSize * 3 / 2, padding, IGui.HorizontalAlignment.CENTER);
            PlateText.drawString(image, name, width / 2, height / 2, IGui.HorizontalAlignment.CENTER, IGui.VerticalAlignment.CENTER, backgroundColor, textColor, false);

            if (transparentColor != 0) {
                PlateText.clearColor(image, PlateText.invertColor(transparentColor));
            }
            return image;
        });
    }

    /** 下一站牌：箭头 + 开往 XX + 下一站 YY。 */
    public static ResourceLocation nextStation(long platformId, int arrowDirection, float paddingScale, float aspectRatio, int backgroundColor, int textColor, int transparentColor) {
        if (aspectRatio <= 0 || platformId == 0) {
            return null;
        }

        return get("next_" + platformId + "_" + arrowDirection + "_" + paddingScale + "_" + aspectRatio + "_" + backgroundColor + "_" + textColor + "_" + transparentColor, () -> {
            final int height = PLATE_HEIGHT;
            final int width = Math.round(height * aspectRatio);
            final int padding = Math.round(height * paddingScale);
            final int tileSize = height - padding * 2;
            if (width <= 0 || tileSize <= 0) {
                return null;
            }

            final String destination = getDestination(platformId);
            final String nextStation = getNextStationName(platformId);

            final NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
            PlateText.fill(image, backgroundColor);

            final boolean flipArrow = arrowDirection == 2;
            final int arrowSize = tileSize;

            if (destination.isEmpty() && nextStation.isEmpty()) {
                final PlateText.Text terminus = PlateText.create("终点站", width - padding * 2, (int) (tileSize * LINE_HEIGHT_MULTIPLIER), tileSize * 3, tileSize * 3 / 2, padding, IGui.HorizontalAlignment.CENTER);
                PlateText.drawString(image, terminus, width / 2, height / 2, IGui.HorizontalAlignment.CENTER, IGui.VerticalAlignment.CENTER, backgroundColor, textColor, false);
                return image;
            }

            final int arrowX = flipArrow ? width - padding - arrowSize : padding;
            try {
                PlateText.drawResource(image, ARROW_RESOURCE, arrowX, padding, arrowSize, arrowSize, flipArrow, 0, 1, textColor);
            } catch (Exception e) {
                Use.LOGGER.warn("Unable to draw the plate arrow", e);
            }

            final int textX = flipArrow ? width - padding - arrowSize - padding : padding + arrowSize + padding;
            final PlateText.Text destinationText = PlateText.create(destination, width - padding * 2 - arrowSize - padding, (int) (tileSize * LINE_HEIGHT_MULTIPLIER), tileSize * 3, tileSize * 3 / 2, 0, flipArrow ? IGui.HorizontalAlignment.RIGHT : IGui.HorizontalAlignment.LEFT);
            PlateText.drawString(image, destinationText, textX, height / 2, flipArrow ? IGui.HorizontalAlignment.RIGHT : IGui.HorizontalAlignment.LEFT, IGui.VerticalAlignment.CENTER, 0, textColor, false);

            if (!nextStation.isEmpty()) {
                final PlateText.Text nextText = PlateText.create("下一站 " + nextStation, width - padding * 2, tileSize, tileSize, tileSize / 2, 0, flipArrow ? IGui.HorizontalAlignment.LEFT : IGui.HorizontalAlignment.RIGHT);
                PlateText.drawString(image, nextText, flipArrow ? padding : width - padding, height, flipArrow ? IGui.HorizontalAlignment.LEFT : IGui.HorizontalAlignment.RIGHT, IGui.VerticalAlignment.BOTTOM, 0, textColor, false);
            }

            if (transparentColor != 0) {
                PlateText.clearColor(image, PlateText.invertColor(transparentColor));
            }
            return image;
        });
    }

    /** 线路色带：一条按经停线路颜色等分的竖条。 */
    public static ResourceLocation colorStrip(long platformId) {
        if (platformId == 0) {
            return null;
        }

        return get("strip_" + platformId, () -> {
            final List<Integer> colors = getRouteColors(platformId);
            if (colors.isEmpty()) {
                return null;
            }

            final int height = PLATE_HEIGHT;
            final int width = Math.max(4, height / 4);
            final int heightPerColor = Math.max(1, height / colors.size());
            final NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);

            for (int i = 0; i < colors.size(); i++) {
                for (int drawX = 0; drawX < width; drawX++) {
                    for (int drawY = 0; drawY < heightPerColor; drawY++) {
                        PlateText.drawPixelSafe(image, drawX, heightPerColor * i + drawY, ARGB_BLACK | colors.get(i));
                    }
                }
            }
            return image;
        });
    }

    // ── 数据读取 ────────────────────────────────────────────────

    private static String getStationName(long platformId) {
        final Station station = ClientData.DATA_CACHE.platformIdToStation.get(platformId);
        return station == null || station.name == null ? "" : station.name;
    }

    /** 经停本站台的线路里，第一条的终点站名字。 */
    private static String getDestination(long platformId) {
        for (Route route : getRoutes(platformId)) {
            final int index = route.getPlatformIdIndex(platformId);
            if (index < 0) {
                continue;
            }
            String destination = route.getDestination(index);
            if (destination == null || destination.isEmpty()) {
                final Station terminus = ClientData.DATA_CACHE.platformIdToStation.get(route.platformIds.get(route.platformIds.size() - 1).platformId);
                destination = terminus == null ? "" : terminus.name;
            }
            if (!destination.isEmpty()) {
                return destination;
            }
        }
        return "";
    }

    /** 经停本站台的线路里，下一站的站名。 */
    private static String getNextStationName(long platformId) {
        for (Route route : getRoutes(platformId)) {
            final int index = route.getPlatformIdIndex(platformId);
            if (index < 0 || index + 1 >= route.platformIds.size()) {
                continue;
            }
            final Station next = ClientData.DATA_CACHE.platformIdToStation.get(route.platformIds.get(index + 1).platformId);
            if (next != null && next.name != null && !next.name.isEmpty()) {
                return next.name;
            }
        }
        return "";
    }

    private static List<Integer> getRouteColors(long platformId) {
        final List<Integer> colors = new ArrayList<>();
        for (Route route : getRoutes(platformId)) {
            colors.add(route.color);
        }
        return colors;
    }

    private static List<Route> getRoutes(long platformId) {
        final List<Route> routes = new ArrayList<>();
        for (Route route : ClientData.ROUTES) {
            if (route.containsPlatformId(platformId)) {
                routes.add(route);
            }
        }
        return routes;
    }

    // ── 缓存与注册 ──────────────────────────────────────────────

    private static ResourceLocation get(String key, java.util.function.Supplier<NativeImage> supplier) {
        final long now = System.currentTimeMillis();
        final Entry cached = CACHE.get(key);
        if (cached != null && now - cached.timestamp() < TTL_MILLIS) {
            return cached.resourceLocation();
        }

        final NativeImage image;
        try {
            image = supplier.get();
        } catch (Exception e) {
            Use.LOGGER.warn("Unable to generate the plate texture {}", key, e);
            return null;
        }

        if (image == null) {
            return null;
        }

        final ResourceLocation resourceLocation = new ResourceLocation(Use.MOD_ID, "dynamic/plate_" + Integer.toHexString(key.hashCode()));
        final DynamicTexture texture = new DynamicTexture(image);
        texture.setFilter(false, false);
        Minecraft.getInstance().getTextureManager().register(resourceLocation, texture);
        CACHE.put(key, new Entry(resourceLocation, now));
        return resourceLocation;
    }

    /** 清空缓存（资源重载时调用）。 */
    public static void clear() {
        for (Entry entry : CACHE.values()) {
            Minecraft.getInstance().getTextureManager().release(entry.resourceLocation());
        }
        CACHE.clear();
    }
}
