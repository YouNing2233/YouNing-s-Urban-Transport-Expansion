package younin.use;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import younin.use.item.DispatcherBaton;
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

    public static final RegistrySupplier<Block> USE_EMBLEM = BLOCKS.register("use_emblem",
            () -> new Block(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL)));

    public static final RegistrySupplier<Item> USE_EMBLEM_ITEM = ITEMS.register("use_emblem",
            () -> new BlockItem(USE_EMBLEM.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> DISPATCHER_BATON = ITEMS.register("dispatcher_baton",
            () -> new DispatcherBaton(new Item.Properties()));

    private UseItems() {
    }

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
        TABS.register();
        CreativeTabRegistry.append(MAIN_TAB, DISPATCHER_BATON.get(), USE_EMBLEM_ITEM.get());
    }
}
