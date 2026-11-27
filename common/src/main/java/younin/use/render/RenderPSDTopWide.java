package younin.use.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mtr.block.BlockPSDTop;
import mtr.block.IBlock;
import mtr.client.ClientData;
import mtr.client.IDrawing;
import mtr.data.IGui;
import mtr.mappings.BlockEntityRendererMapper;
import mtr.mappings.UtilitiesClient;
import mtr.render.RenderTrains;
import mtr.render.StoredMatrixTransformations;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import younin.use.block.BlockPSDTopWide;
import younin.use.client.PlateTextures;

import static mtr.render.RenderRouteBase.getShadingColor;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

/**
 * 三格吊板的渲染器。
 *
 * <p>每一块只画整幅贴图里属于自己的一段（按 UV 切分），因此连着放三块时是一整块牌子：
 * 中间是站名、两端是箭头，下方是线路色带。移植自天津地铁 MTR3 适配版
 * （ZiYueCommentary，MIT），贴图改由 {@link PlateTextures} 生成并缓存。</p>
 */
public class RenderPSDTopWide extends BlockEntityRendererMapper<BlockPSDTopWide.TileEntityPSDTopWide> implements IGui {

    protected static final float Z = 1.9995F / 16F;
    protected static final float FRONT_OFFSET = -0.004F;
    protected static final float TOP_PADDING = 7.5F / 16F;
    protected static final float BOTTOM_PADDING = 1.5F / 16F;
    protected static final float COLOR_STRIP_START = 14.5F / 16F;
    protected static final float COLOR_STRIP_END = 15F / 16F;
    protected static final int MAX_GROUP_SIZE = 8;
    /** 吊板底板颜色（贴图区域之外的部分）。 */
    protected static final int COLOR_BOARD = 0xFFF2F4F7;
    /** 纯白贴图，配合颜色参数画出实色底板。 */
    protected static final ResourceLocation BOARD_TEXTURE = new ResourceLocation("mtr", "textures/block/white.png");

    public RenderPSDTopWide(BlockEntityRendererProvider.Context context) {
        super(context.getBlockEntityRenderDispatcher());
    }

    @Override
    public void render(BlockPSDTopWide.TileEntityPSDTopWide entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        final Level world = entity.getLevel();
        if (world == null) {
            return;
        }

        final BlockPos pos = entity.getBlockPos();
        final BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockPSDTopWide)) {
            return;
        }

        final Direction facing = IBlock.getStatePropertySafe(state, FACING);
        final int index = countPlates(world, pos, facing.getCounterClockWise());
        final int after = countPlates(world, pos, facing.getClockWise());
        final int total = index + after + 1;

        if (RenderTrains.shouldNotRender(pos, RenderTrains.maxTrainRenderDistance, null)) {
            return;
        }

        final long platformId = entity.getPlatformId(ClientData.PLATFORMS, ClientData.DATA_CACHE);
        if (platformId == 0) {
            return;
        }

        final StoredMatrixTransformations storedMatrixTransformations = new StoredMatrixTransformations();
        storedMatrixTransformations.add(matricesNew -> {
            matricesNew.translate(0.5 + pos.getX(), pos.getY(), 0.5 + pos.getZ());
            UtilitiesClient.rotateYDegrees(matricesNew, -facing.toYRot());
        });
        storedMatrixTransformations.add(matricesNew -> {
            matricesNew.translate(0, 1, 0);
            UtilitiesClient.rotateZDegrees(matricesNew, 180);
            matricesNew.translate(-0.5, -getAdditionalOffset(state), Z);
        });

        final float height = 1 - TOP_PADDING - BOTTOM_PADDING;
        final float width = total * 0.95F;
        final float aspectRatio = width / height;
        final int color = getShadingColor(facing, ARGB_WHITE);

        final BlockPSDTopWide.Style style = IBlock.getStatePropertySafe(state, BlockPSDTopWide.STYLE);
        final int arrowDirection = IBlock.getStatePropertySafe(state, BlockPSDTop.ARROW_DIRECTION);

        final ResourceLocation texture = style == BlockPSDTopWide.Style.NEXT_STATION
                ? PlateTextures.nextStation(platformId, arrowDirection, 0.25F, aspectRatio, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE)
                : PlateTextures.stationName(platformId, 0.25F, aspectRatio, ARGB_WHITE, ARGB_BLACK, ARGB_WHITE);

        final float u0 = index / (float) total;
        final float u1 = (index + 1F) / total;

        // 整块底板：贴图以外（上下留白）的部分也要是白色的牌子，不能透空
        RenderTrains.scheduleRender(BOARD_TEXTURE, false, RenderTrains.QueuedRenderLayer.EXTERIOR, (matricesNew, vertexConsumer) -> {
            storedMatrixTransformations.transform(matricesNew);
            IDrawing.drawTexture(matricesNew, vertexConsumer, 0, 0, FRONT_OFFSET, 1, 1, FRONT_OFFSET, 0, 0, 1, 1, facing.getOpposite(), COLOR_BOARD, light);
            matricesNew.popPose();
        });

        if (texture != null) {
            RenderTrains.scheduleRender(texture, false, RenderTrains.QueuedRenderLayer.EXTERIOR, (matricesNew, vertexConsumer) -> {
                storedMatrixTransformations.transform(matricesNew);
                IDrawing.drawTexture(matricesNew, vertexConsumer, 0, TOP_PADDING, FRONT_OFFSET, 1, 1 - BOTTOM_PADDING, FRONT_OFFSET, u0, 0, u1, 1, facing.getOpposite(), color, light);
                matricesNew.popPose();
            });
        }

        final ResourceLocation colorStrip = PlateTextures.colorStrip(platformId);
        if (colorStrip != null) {
            RenderTrains.scheduleRender(colorStrip, false, RenderTrains.QueuedRenderLayer.EXTERIOR, (matricesNew, vertexConsumer) -> {
                storedMatrixTransformations.transform(matricesNew);
                IDrawing.drawTexture(matricesNew, vertexConsumer, 0, COLOR_STRIP_START, FRONT_OFFSET, 1, COLOR_STRIP_END, FRONT_OFFSET, u0, 0, u1, 1, facing.getOpposite(), color, light);
                matricesNew.popPose();
            });
        }
    }

    /** 从 {@code pos} 沿 {@code direction} 数连续的宽吊板数量。 */
    private static int countPlates(Level world, BlockPos pos, Direction direction) {
        for (int i = 1; i <= MAX_GROUP_SIZE; i++) {
            if (!(world.getBlockState(pos.relative(direction, i)).getBlock() instanceof BlockPSDTopWide)) {
                return i - 1;
            }
        }
        return MAX_GROUP_SIZE;
    }

    private static float getAdditionalOffset(BlockState state) {
        return IBlock.getStatePropertySafe(state, BlockPSDTop.PERSISTENT) == BlockPSDTop.EnumPersistent.NONE ? 0 : BlockPSDTop.PERSISTENT_OFFSET_SMALL;
    }
}
