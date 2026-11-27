package younin.use;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import younin.use.block.BlockAPGDoorWide;
import younin.use.block.BlockPSDDoorWide;

public final class UseBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Use.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<BlockPSDDoorWide.TileEntityPSDDoorWide>> PSD_DOOR_WIDE =
            BLOCK_ENTITY_TYPES.register("psd_door_wide", () -> BlockEntityType.Builder
                    .of(BlockPSDDoorWide.TileEntityPSDDoorWide::new, UseItems.PSD_DOOR_WIDE.get())
                    .build(null));

    public static final RegistrySupplier<BlockEntityType<BlockAPGDoorWide.TileEntityAPGDoorWide>> APG_DOOR_WIDE =
            BLOCK_ENTITY_TYPES.register("apg_door_wide", () -> BlockEntityType.Builder
                    .of(BlockAPGDoorWide.TileEntityAPGDoorWide::new, UseItems.APG_DOOR_WIDE.get())
                    .build(null));

    private UseBlockEntityTypes() {
    }

    public static void register() {
        BLOCK_ENTITY_TYPES.register();
    }
}
