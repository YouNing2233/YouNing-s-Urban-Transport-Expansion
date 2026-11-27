package younin.use;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import younin.use.block.BlockAPGDoorWide;
import younin.use.block.BlockPSDDoorWide;
import younin.use.block.UseEmblem;
import younin.use.item.DispatcherBaton;
import younin.use.item.ItemWideApgDoor;
import younin.use.item.ItemWidePsdDoor;
import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class UseItems {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Use.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Use.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Use.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MAIN_TAB = TABS.register("main",
            () -> CreativeTabRegistry.create(Component.translatable("itemGroup.use.main"), () -> new ItemStack(UseItems.USE_EMBLEM_ITEM.get())));
    public static final RegistrySupplier<CreativeModeTab> BEIJING_TAB = TABS.register("beijing",
            () -> CreativeTabRegistry.create(Component.translatable("itemGroup.use.beijing"), () -> new ItemStack(UseItems.USE_EMBLEM_ITEM.get())));

    public static final RegistrySupplier<Block> USE_EMBLEM = BLOCKS.register("use_emblem",
            () -> new UseEmblem(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL).noOcclusion()));

    public static final RegistrySupplier<Item> USE_EMBLEM_ITEM = ITEMS.register("use_emblem",
            () -> new BlockItem(USE_EMBLEM.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> DISPATCHER_BATON = ITEMS.register("dispatcher_baton",
            () -> new DispatcherBaton(new Item.Properties()));

    // ── 三扇宽屏蔽门（北京地铁）────────────────────────────────────
    public static final RegistrySupplier<Block> PSD_DOOR_WIDE = BLOCKS.register("psd_door_wide",
            BlockPSDDoorWide::new);

    public static final RegistrySupplier<Block> APG_DOOR_WIDE = BLOCKS.register("apg_door_wide",
            BlockAPGDoorWide::new);

    public static final RegistrySupplier<Item> PSD_DOOR_WIDE_ITEM = ITEMS.register("psd_door_wide",
            () -> new ItemWidePsdDoor(PSD_DOOR_WIDE, new Item.Properties().stacksTo(1)));

    public static final RegistrySupplier<Item> APG_DOOR_WIDE_ITEM = ITEMS.register("apg_door_wide",
            () -> new ItemWideApgDoor(APG_DOOR_WIDE, new Item.Properties().stacksTo(1)));

    private UseItems() {
    }

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
        TABS.register();
        UseBlockEntityTypes.register();
        CreativeTabRegistry.append(MAIN_TAB, DISPATCHER_BATON.get(), USE_EMBLEM_ITEM.get());
        CreativeTabRegistry.append(BEIJING_TAB, PSD_DOOR_WIDE_ITEM.get(), APG_DOOR_WIDE_ITEM.get());
    }
}
