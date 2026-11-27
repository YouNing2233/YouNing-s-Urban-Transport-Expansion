package younin.use.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mtr.MTRClient;
import mtr.block.BlockPSDAPGDoorBase;
import mtr.block.IBlock;
import mtr.mappings.BlockEntityRendererMapper;
import mtr.mappings.UtilitiesClient;
import mtr.render.RenderTrains;
import mtr.render.StoredMatrixTransformations;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import younin.use.block.BlockPSDDoorWide;

/**
 * Renderer for the three-leaf wide Tianjin PSD door.
 *
 * <p>Two leaves slide into one glass pocket while the third one stays closed;
 * which side opens is stored in {@link BlockPSDDoorWide#OPEN_LEFT}.</p>
 *
 * @since 1.1.7
 */
public class RenderWidePsdDoor<T extends BlockPSDAPGDoorBase.TileEntityPSDAPGDoorBase> extends BlockEntityRendererMapper<T>
{
    private static final ModelSingleCube MODEL_PSD = new ModelSingleCube(36, 18, 0, 0, 0, 16, 16, 2);
    private static final ModelSingleCube MODEL_PSD_LIGHT_LEFT = new ModelSingleCube(16, 16, 0, -1, 5, 1, 1, 1);
    private static final ModelSingleCube MODEL_PSD_LIGHT_CENTER = new ModelSingleCube(16, 16, 7.0F, -1, 5, 1, 1, 1);
    private static final ModelSingleCube MODEL_PSD_LIGHT_RIGHT = new ModelSingleCube(16, 16, 15, -1, 5, 1, 1, 1);
    private static final ModelSingleCube MODEL_PSD_DOOR_LOCKED = new ModelSingleCube(6, 6, 5, 6, 1, 6, 6, 0);

    /**
     * 叠放时前后错开的距离，避免两扇门贴图重叠闪烁。
     */
    private static final float STACK_OFFSET = 1F / 16F;

    public RenderWidePsdDoor(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(T entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        final Level world = entity.getLevel();
        if (world == null) {
            return;
        }

        final BlockPos pos = entity.getBlockPos();
        final Direction facing = IBlock.getStatePropertySafe(world, pos, BlockPSDAPGDoorBase.FACING);
        // 第几扇（0=左 1=中 2=右）：按相邻门块位置推算，不依赖方块状态（MTR 更新时会重置自定义属性）
        final int leafIndex = getLeafIndex(world, pos, facing);
        final BlockPSDDoorWide.WideSide side = leafIndex == 0
                ? BlockPSDDoorWide.WideSide.LEFT
                : leafIndex == 1 ? BlockPSDDoorWide.WideSide.MIDDLE : BlockPSDDoorWide.WideSide.RIGHT;
        final boolean half = IBlock.getStatePropertySafe(world, pos, BlockPSDAPGDoorBase.HALF) == DoubleBlockHalf.UPPER;
        final boolean unlocked = IBlock.getStatePropertySafe(world, pos, BlockPSDAPGDoorBase.UNLOCKED);
        final int openMode = BlockPSDDoorWide.getOpenMode(world, pos);
        // MTR 的列车脚本只会驱动它认定的那一扇门，这里取整组的最大开度，保证三扇同步
        final float open = Math.min(entity.getOpen(MTRClient.getLastFrameDuration()), 1F);

        // 三扇全开：两扇一起去一侧（并排两格），单独那扇去另一侧（一格）
        // 本渲染坐标里 +X 指向"左"
        //   openLeft  : 第 0、1 扇往左滑 1 格；第 2 扇往右滑 1 格
        //   openRight : 第 1、2 扇往右滑 1 格；第 0 扇往左滑 1 格
        final float slideBlocks;
        if (openMode == BlockPSDDoorWide.OPEN_MODE_ALL) {
            // 三扇全开：前两扇一起叠进左侧玻璃袋，第三扇进右侧玻璃袋
            slideBlocks = leafIndex == 0 ? 1F : leafIndex == 1 ? 2F : -1F;
        } else if (openMode == BlockPSDDoorWide.OPEN_MODE_LEFT) {
            slideBlocks = leafIndex <= 1 ? 1F : -1F;
        } else {
            slideBlocks = leafIndex >= 1 ? -1F : 1F;
        }
        final float offset = open * slideBlocks;
        final float stackOffset = 0F;

        final boolean sideIsRight = side != BlockPSDDoorWide.WideSide.LEFT;
        final String sideName = sideIsRight ? "right" : "left";

        final StoredMatrixTransformations storedMatrixTransformations = new StoredMatrixTransformations();
        storedMatrixTransformations.add(matricesNew -> {
            matricesNew.translate(0.5 + pos.getX(), pos.getY(), 0.5 + pos.getZ());
            UtilitiesClient.rotateYDegrees(matricesNew, -facing.toYRot());
            UtilitiesClient.rotateXDegrees(matricesNew, 180);
        });
        final StoredMatrixTransformations storedMatrixTransformationsLight = storedMatrixTransformations.copy();

        if (half && side == BlockPSDDoorWide.WideSide.MIDDLE) {
            final ResourceLocation lightTexture = new ResourceLocation(String.format("use:textures/block/light_%s.png", open > 0 ? "on" : "off"));
            RenderTrains.scheduleRender(lightTexture, false, open > 0 ? RenderTrains.QueuedRenderLayer.LIGHT : RenderTrains.QueuedRenderLayer.EXTERIOR, (matricesNew, vertexConsumer) -> {
                storedMatrixTransformationsLight.transform(matricesNew);
                MODEL_PSD_LIGHT_CENTER.renderToBuffer(matricesNew, vertexConsumer, light, overlay, 1, 1, 1, 1);
                matricesNew.popPose();
            });
        }


        storedMatrixTransformations.add(matricesNew -> matricesNew.translate(offset, 0, stackOffset));

        final ResourceLocation doorTexture = new ResourceLocation(String.format("use:textures/block/psd_door_%s_%s.png", half ? "top" : "bottom", sideName));
        RenderTrains.scheduleRender(doorTexture, false, RenderTrains.QueuedRenderLayer.EXTERIOR, (matricesNew, vertexConsumer) -> {
            storedMatrixTransformations.transform(matricesNew);
            MODEL_PSD.renderToBuffer(matricesNew, vertexConsumer, light, overlay, 1, 1, 1, 1);
            matricesNew.popPose();
        });

        if (half && !unlocked) {
            RenderTrains.scheduleRender(new ResourceLocation("mtr:textures/block/sign/door_not_in_use.png"), false, RenderTrains.QueuedRenderLayer.EXTERIOR, (matricesNew, vertexConsumer) -> {
                storedMatrixTransformations.transform(matricesNew);
                MODEL_PSD_DOOR_LOCKED.renderToBuffer(matricesNew, vertexConsumer, light, overlay, 1, 1, 1, 1);
                matricesNew.popPose();
            });
        }
    }

    /** 沿门墙往回数，得到本扇是第几格（0/1/2） */
    private static int getLeafIndex(Level world, BlockPos pos, Direction facing) {
        int index = 0;
        BlockPos checkPos = pos.relative(facing.getCounterClockWise());
        while (index < 2 && world.getBlockState(checkPos).getBlock() instanceof BlockPSDDoorWide) {
            index++;
            checkPos = checkPos.relative(facing.getCounterClockWise());
        }
        return index;
    }

    /** 沿门墙扫描 ±2 格，取同组门扇的最大开度 */
    private static long lastDebug;

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }
}
