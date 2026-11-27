package younin.use.block;

import mtr.Items;
import mtr.block.BlockPSDTop;
import mtr.block.IBlock;
import mtr.mappings.BlockEntityMapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import younin.use.Use;
import younin.use.UseBlockEntityTypes;
import younin.use.UseItems;

import java.util.function.UnaryOperator;

import static mtr.block.IBlock.SIDE_EXTENDED;
import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

/**
 * 三格吊板（屏蔽门上方的站名牌）。
 *
 * <p>三块吊板由同一个渲染器一次画出整幅贴图的三段，所以连着放三块时是一整块牌子，
 * 而不是三块各画各的。调度棒切换「站名 / 下一站」，刷子切换箭头方向，两者都作用于整组。
 * 移植自天津地铁 MTR3 适配版（ZiYueCommentary，MIT）。</p>
 */
public class BlockPSDTopWide extends BlockPSDTop {

    public static final EnumProperty<Style> STYLE = EnumProperty.create("style", Style.class);
    private static final int MAX_GROUP_SIZE = 8;

    public BlockPSDTopWide() {
        super();
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return IBlock.checkHoldingItem(world, player, item -> {
            if (item == UseItems.DISPATCHER_BATON.get()) {
                // 调度棒：整组在「站名 / 下一站」之间切换
                final Style next = IBlock.getStatePropertySafe(state, STYLE) == Style.NEXT_STATION ? Style.STATION_NAME : Style.NEXT_STATION;
                cycleGroup(world, pos, groupState -> groupState.setValue(STYLE, next));
            } else if (item == Items.BRUSH.get()) {
                // 刷子：整组一起切换箭头方向（与 MTR 原版一致）
                cycleGroup(world, pos, groupState -> groupState.cycle(ARROW_DIRECTION));
            }
        }, null, UseItems.DISPATCHER_BATON.get(), Items.BRUSH.get());
    }

    /** 对当前吊板以及左右相邻的同一组吊板一起执行 {@code operator}。 */
    private static void cycleGroup(Level world, BlockPos pos, UnaryOperator<BlockState> operator) {
        applyAt(world, pos, operator);
        final BlockState state = world.getBlockState(pos);
        final Direction facing = IBlock.getStatePropertySafe(state, FACING);
        for (Direction direction : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
            for (int i = 1; i <= MAX_GROUP_SIZE; i++) {
                final BlockPos offsetPos = pos.relative(direction, i);
                if (world.getBlockState(offsetPos).getBlock() instanceof BlockPSDTopWide) {
                    applyAt(world, offsetPos, operator);
                } else {
                    break;
                }
            }
        }
    }

    private static void applyAt(Level world, BlockPos pos, UnaryOperator<BlockState> operator) {
        final BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockPSDTopWide) {
            final BlockState updated = operator.apply(state);
            if (updated != state) {
                world.setBlockAndUpdate(pos, updated);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && IBlock.getStatePropertySafe(state, PERSISTENT) == EnumPersistent.NONE && !(neighborState.getBlock() instanceof BlockPSDDoorWide) && !(neighborState.getBlock() instanceof BlockAPGDoorWide)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return getActualState(world, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STYLE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockEntityMapper createBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityPSDTopWide(pos, state);
    }

    /** 依据下方门方块计算朝向与左右位置。 */
    public static BlockState getActualState(LevelAccessor world, BlockPos pos) {
        final BlockState stateBelow = world.getBlockState(pos.below());
        final Block blockBelow = stateBelow.getBlock();

        Direction facing = null;
        IBlock.EnumSide side = null;

        if (blockBelow instanceof BlockPSDDoorWide || blockBelow instanceof BlockAPGDoorWide) {
            facing = IBlock.getStatePropertySafe(stateBelow, FACING);
            if (blockBelow instanceof BlockPSDDoorWide) {
                final BlockPSDDoorWide.WideSide wideSide = IBlock.getStatePropertySafe(stateBelow, BlockPSDDoorWide.WIDE_SIDE);
                side = wideSide == BlockPSDDoorWide.WideSide.LEFT ? IBlock.EnumSide.LEFT : wideSide == BlockPSDDoorWide.WideSide.MIDDLE ? IBlock.EnumSide.MIDDLE : IBlock.EnumSide.RIGHT;
            }
        }

        BlockState state = UseItems.PSD_TOP_WIDE.get().defaultBlockState().setValue(AIR_LEFT, false).setValue(AIR_RIGHT, false);
        if (facing != null) {
            state = state.setValue(FACING, facing);
        }
        if (side != null) {
            state = state.setValue(SIDE_EXTENDED, side);
        }
        return state;
    }

    public static class TileEntityPSDTopWide extends BlockPSDTop.TileEntityRouteBase {

        public TileEntityPSDTopWide(BlockPos pos, BlockState state) {
            super(UseBlockEntityTypes.PSD_TOP_WIDE.get(), pos, state);
        }
    }

    /** 吊板显示内容：站名或下一站。 */
    public enum Style implements StringRepresentable {
        STATION_NAME("station_name"),
        NEXT_STATION("next_station");

        private final String name;

        Style(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
