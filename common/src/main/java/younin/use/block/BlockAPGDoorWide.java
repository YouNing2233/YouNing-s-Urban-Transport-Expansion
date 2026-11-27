package younin.use.block;

import mtr.mappings.BlockEntityMapper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import younin.use.UseBlockEntityTypes;
import younin.use.UseItems;
import younin.use.block.flag.BlockFlagApg;

/**
 * 三扇宽屏蔽门（半高 / APG）。
 *
 * <p>与全高版共用全部逻辑，只是门体模型与贴图不同。</p>
 */
public class BlockAPGDoorWide extends BlockPSDDoorWide implements BlockFlagApg {

    public BlockAPGDoorWide() {
    }

    @Override
    protected boolean isAPG() {
        return true;
    }

    @Override
    public Item asItem() {
        return UseItems.APG_DOOR_WIDE_ITEM.get();
    }

    @Override
    public BlockEntityMapper createBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityAPGDoorWide(pos, state);
    }

    public static class TileEntityAPGDoorWide extends TileEntityPSDDoorWide {

        public TileEntityAPGDoorWide(BlockPos pos, BlockState state) {
            this(UseBlockEntityTypes.APG_DOOR_WIDE.get(), pos, state);
        }

        protected TileEntityAPGDoorWide(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }
    }
}
