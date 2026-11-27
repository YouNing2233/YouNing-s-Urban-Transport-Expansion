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
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import younin.use.block.BlockAPGDoorWide;
import younin.use.block.BlockPSDDoorWide;

/**
 * Renderer for the three-leaf wide Tianjin APG door (half-height).
 *
 * <p>Geometry and textures follow {@link RenderTianjinAPGDoor}, but the sliding distance is
 * computed per leaf like {@link RenderWidePsdDoor}.</p>
 *
 * @since 1.1.8
 */
public class RenderWideApgDoor<T extends BlockPSDAPGDoorBase.TileEntityPSDAPGDoorBase> extends BlockEntityRendererMapper<T>
{
    private static final ModelSingleCube MODEL_APG_TOP = new ModelSingleCube(34, 9, 0, 8, 1, 16, 8, 1);
    private static final ModelAPGDoorBottom MODEL_APG_BOTTOM = new ModelAPGDoorBottom();
    private static final ModelSingleCube MODEL_APG_DOOR_LOCKED = new ModelSingleCube(6, 6, 5, 10, 1, 6, 6, 0);

    public RenderWideApgDoor(BlockEntityRendererProvider.Context context) {
        super(context.getBlockEntityRenderDispatcher());
    }

    @Override
    public void render(T entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        final Level world = entity.getLevel();
        if (world == null) {
            return;
        }

        final BlockPos pos = entity.getBlockPos();
        final Direction facing = IBlock.getStatePropertySafe(world, pos, BlockPSDAPGDoorBase.FACING);
        final boolean half = IBlock.getStatePropertySafe(world, pos, BlockPSDAPGDoorBase.HALF) == DoubleBlockHalf.UPPER;
        final boolean unlocked = IBlock.getStatePropertySafe(world, pos, BlockPSDAPGDoorBase.UNLOCKED);
        final int leafIndex = getLeafIndex(world, pos, facing);
        final int openMode = BlockPSDDoorWide.getOpenMode(world, pos);
        final float open = Math.min(entity.getOpen(MTRClient.getLastFrameDuration()), 1F);

        // 本渲染坐标里 +X 指向"左"；三扇全开：两扇去一侧、单独那扇去另一侧
        final float slideBlocks;
        if (openMode == BlockPSDDoorWide.OPEN_MODE_ALL) {
            // 三扇全开：前两扇一起叠进左侧玻璃袋，第三扇进右侧玻璃袋
            slideBlocks = leafIndex == 0 ? 1F : leafIndex == 1 ? 2F : -1F;
        } else if (openMode == BlockPSDDoorWide.OPEN_MODE_LEFT) {
            slideBlocks = leafIndex <= 1 ? 1F : -1F;
        } else {
            slideBlocks = leafIndex >= 1 ? -1F : 1F;
        }

        final boolean sideIsRight = leafIndex != 0;
        final String sideName = sideIsRight ? "right" : "left";

        final StoredMatrixTransformations storedMatrixTransformations = new StoredMatrixTransformations();
        storedMatrixTransformations.add(matricesNew -> {
            matricesNew.translate(0.5 + pos.getX(), pos.getY(), 0.5 + pos.getZ());
            UtilitiesClient.rotateYDegrees(matricesNew, -facing.toYRot());
            UtilitiesClient.rotateXDegrees(matricesNew, 180);
        });

        storedMatrixTransformations.add(matricesNew -> matricesNew.translate(open * slideBlocks, 0, 0));

        final ResourceLocation doorTexture = new ResourceLocation(String.format("use:textures/block/apg_door_%s_%s.png", half ? "top" : "bottom", sideName));
        RenderTrains.scheduleRender(doorTexture, false, RenderTrains.QueuedRenderLayer.EXTERIOR, (matricesNew, vertexConsumer) -> {
            storedMatrixTransformations.transform(matricesNew);
            (half ? MODEL_APG_TOP : MODEL_APG_BOTTOM).renderToBuffer(matricesNew, vertexConsumer, light, overlay, 1, 1, 1, 1);
            matricesNew.popPose();
        });

        if (half && !unlocked) {
            RenderTrains.scheduleRender(new ResourceLocation("mtr:textures/block/sign/door_not_in_use.png"), false, RenderTrains.QueuedRenderLayer.EXTERIOR, (matricesNew, vertexConsumer) -> {
                storedMatrixTransformations.transform(matricesNew);
                MODEL_APG_DOOR_LOCKED.renderToBuffer(matricesNew, vertexConsumer, light, overlay, 1, 1, 1, 1);
                matricesNew.popPose();
            });
        }
    }

    /** 沿门墙往回数，得到本扇是第几格（0/1/2） */
    private static int getLeafIndex(Level world, BlockPos pos, Direction facing) {
        int index = 0;
        BlockPos checkPos = pos.relative(facing.getCounterClockWise());
        while (index < 2 && world.getBlockState(checkPos).getBlock() instanceof BlockAPGDoorWide) {
            index++;
            checkPos = checkPos.relative(facing.getCounterClockWise());
        }
        return index;
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }
}
