package younin.use.item;

import dev.architectury.registry.registries.RegistrySupplier;
import mtr.block.BlockPSDAPGBase;
import mtr.block.BlockPSDAPGDoorBase;
import mtr.block.IBlock;
import mtr.item.ItemPSDAPGBase;
import mtr.mappings.Text;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import younin.use.block.BlockPSDDoorWide;
import younin.use.block.flag.BlockFlagApg;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * 三扇宽屏蔽门的放置物品：一次铺 3 格 × 2 高，并写上每扇的位置。
 *
 * <p>移植自天津地铁 MTR3 适配版（ZiYueCommentary，MIT）。</p>
 */
public class ItemWidePsdDoorBase extends Item {

    private final RegistrySupplier<Block> block;

    public ItemWidePsdDoorBase(RegistrySupplier<Block> block, Properties properties) {
        super(properties);
        this.block = block;
    }

    public Block getBlock() {
        return block.get();
    }

    protected boolean isAPG() {
        return getBlock() instanceof BlockFlagApg;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Block block = getBlock();
        final int horizontalBlocks = 3;

        if (ItemPSDAPGBase.blocksNotReplaceable(context, horizontalBlocks, isAPG() ? 2 : 3, block)) {
            return InteractionResult.FAIL;
        }

        final Level world = context.getLevel();
        final Direction playerFacing = context.getHorizontalDirection();
        final BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

        for (int x = 0; x < horizontalBlocks; x++) {
            final BlockPos newPos = pos.relative(playerFacing.getClockWise(), x);

            for (int y = 0; y < 2; y++) {
                final BlockState state = block.defaultBlockState()
                        .setValue(BlockPSDAPGDoorBase.FACING, playerFacing)
                        .setValue(IBlock.HALF, y == 1 ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER)
                        .setValue(IBlock.SIDE, x == 0 ? IBlock.EnumSide.LEFT : IBlock.EnumSide.RIGHT)
                        .setValue(BlockPSDDoorWide.WIDE_SIDE, x == 0
                                ? BlockPSDDoorWide.WideSide.LEFT
                                : x == 1 ? BlockPSDDoorWide.WideSide.MIDDLE : BlockPSDDoorWide.WideSide.RIGHT);

                world.setBlockAndUpdate(newPos.above(y), state);
            }
        }

        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Text.translatable("tooltip.mtr.psd_apg_door").withStyle(ChatFormatting.GRAY));
        tooltip.add(Text.translatable("tooltip.use.wide_door").withStyle(ChatFormatting.GRAY));
    }
}
