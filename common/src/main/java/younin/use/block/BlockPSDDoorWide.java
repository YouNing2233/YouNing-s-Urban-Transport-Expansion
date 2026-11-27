package younin.use.block;

import mtr.block.BlockPSDAPGDoorBase;
import mtr.block.IBlock;
import mtr.mappings.BlockEntityMapper;
import mtr.mappings.Text;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import younin.use.UseBlockEntityTypes;
import younin.use.UseItems;
import younin.use.block.flag.BlockFlagDoorWide;

/**
 * 三扇宽屏蔽门（全高）。
 *
 * <p>三片门叶组成一个门洞，用调度棒切换开法：左侧两扇开、右侧两扇开、三扇全开（左二右一）。
 * 开源方向存在方块实体里，方块状态只用来同步客户端。</p>
 *
 * <p>移植自天津地铁 MTR3 适配版（ZiYueCommentary，MIT）。</p>
 */
public class BlockPSDDoorWide extends BlockPSDAPGDoorBase implements BlockFlagDoorWide {

    /** 兼容字段：true = 左两扇开 */
    public static final BooleanProperty OPEN_LEFT = BooleanProperty.create("open_left");

    /** 0 = 左两扇开；1 = 右两扇开；2 = 三扇全开（左二右一） */
    public static final int OPEN_MODE_LEFT = 0;
    public static final int OPEN_MODE_RIGHT = 1;
    public static final int OPEN_MODE_ALL = 2;

    /** 三扇中的位置（MTR 的 SIDE 只有 LEFT/RIGHT，这里单独用一个属性表达中间扇） */
    public static final EnumProperty<WideSide> WIDE_SIDE = EnumProperty.create("wide_side", WideSide.class);

    public enum WideSide implements StringRepresentable {
        LEFT("left"),
        MIDDLE("middle"),
        RIGHT("right");

        private final String name;

        WideSide(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public BlockPSDDoorWide() {
        registerDefaultState(defaultBlockState().setValue(OPEN_LEFT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN_LEFT, WIDE_SIDE);
    }

    @Override
    public Item asItem() {
        return UseItems.PSD_DOOR_WIDE_ITEM.get();
    }

    @Override
    public BlockEntityMapper createBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityPSDDoorWide(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        final BlockState state = super.getStateForPlacement(ctx);
        return state != null && state.getBlock() == this ? state.setValue(OPEN_LEFT, false) : state;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (IBlock.getStatePropertySafe(state, IBlock.SIDE) == IBlock.EnumSide.SINGLE) {
            final Direction sideDirection = IBlock.getSideDirection(state);

            final BlockState leftState = state.setValue(IBlock.SIDE, IBlock.EnumSide.LEFT).setValue(WIDE_SIDE, WideSide.LEFT);
            world.setBlock(pos, leftState, 3);
            super.setPlacedBy(world, pos, leftState, placer, stack);

            placeLeaf(world, pos.relative(sideDirection), state, WideSide.MIDDLE);
            placeLeaf(world, pos.relative(sideDirection, 2), state, WideSide.RIGHT);
        } else {
            super.setPlacedBy(world, pos, state, placer, stack);
        }
    }

    private static void placeLeaf(Level world, BlockPos pos, BlockState base, WideSide side) {
        final BlockState bottomState = base
                .setValue(IBlock.SIDE, side == WideSide.LEFT ? IBlock.EnumSide.LEFT : IBlock.EnumSide.RIGHT)
                .setValue(WIDE_SIDE, side)
                .setValue(IBlock.HALF, DoubleBlockHalf.LOWER);
        world.setBlock(pos, bottomState, 3);
        world.setBlock(pos.above(), bottomState.setValue(IBlock.HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        // 调度棒：整组（上下两半 × 三扇）一起切换开法，并给操作者一行提示
        if (player.getItemInHand(interactionHand).is(UseItems.DISPATCHER_BATON.get())) {
            if (!world.isClientSide) {
                final Direction sideDirection = IBlock.getSideDirection(blockState);

                BlockPos currentPos = IBlock.getStatePropertySafe(blockState, IBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;

                for (int i = 0; i < 2; i++) {
                    final BlockPos neighbourPos = currentPos.relative(sideDirection.getOpposite());
                    if (world.getBlockState(neighbourPos).getBlock() == this) {
                        currentPos = neighbourPos;
                    } else {
                        break;
                    }
                }

                final int openMode = (getOpenMode(world, currentPos) + 1) % 3;
                for (int i = 0; i < 3; i++) {
                    setOpenMode(world, currentPos, openMode);
                    setOpenMode(world, currentPos.above(), openMode);
                    currentPos = currentPos.relative(sideDirection);
                }

                player.displayClientMessage(Text.translatable(openMode == OPEN_MODE_LEFT
                        ? "message.use.wide_door.open_left"
                        : openMode == OPEN_MODE_RIGHT
                        ? "message.use.wide_door.open_right"
                        : "message.use.wide_door.open_all"), true);
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(blockState, world, pos, player, interactionHand, blockHitResult);
    }

    /** 从方块实体读开门模式（方块状态会被 MTR 的状态刷新重置，不能作为唯一来源） */
    public static int getOpenMode(Level world, BlockPos pos) {
        final BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockPSDDoorWide
                && world.getBlockEntity(pos) instanceof TileEntityPSDDoorWide te) {
            return te.openMode;
        }
        return OPEN_MODE_LEFT;
    }

    /** 兼容旧调用：true 表示“左两扇开”这一档 */
    public static boolean isOpenLeft(Level world, BlockPos pos) {
        return getOpenMode(world, pos) == OPEN_MODE_LEFT;
    }

    private static void setOpenMode(Level world, BlockPos pos, int openMode) {
        if (world.getBlockState(pos).getBlock() instanceof BlockPSDDoorWide
                && world.getBlockEntity(pos) instanceof TileEntityPSDDoorWide te) {
            te.openMode = openMode;
            world.setBlock(pos, world.getBlockState(pos).setValue(OPEN_LEFT, openMode == OPEN_MODE_LEFT), 2);
            te.setChanged();
            te.syncData();
        }
    }

    public static class TileEntityPSDDoorWide extends BlockPSDAPGDoorBase.TileEntityPSDAPGDoorBase {

        /** 0 = 左两扇开；1 = 右两扇开；2 = 三扇全开 */
        public int openMode = OPEN_MODE_LEFT;

        public TileEntityPSDDoorWide(BlockPos pos, BlockState state) {
            this(UseBlockEntityTypes.PSD_DOOR_WIDE.get(), pos, state);
        }

        protected TileEntityPSDDoorWide(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            this.openMode = IBlock.getStatePropertySafe(state, OPEN_LEFT) ? OPEN_MODE_LEFT : OPEN_MODE_RIGHT;
        }

        @Override
        public void readCompoundTag(CompoundTag compoundTag) {
            openMode = compoundTag.contains("open_mode")
                    ? compoundTag.getInt("open_mode")
                    : (compoundTag.getBoolean("open_left") ? OPEN_MODE_LEFT : OPEN_MODE_RIGHT);
            super.readCompoundTag(compoundTag);
        }

        @Override
        public void writeCompoundTag(CompoundTag compoundTag) {
            compoundTag.putInt("open_mode", openMode);
            super.writeCompoundTag(compoundTag);
        }
    }
}
