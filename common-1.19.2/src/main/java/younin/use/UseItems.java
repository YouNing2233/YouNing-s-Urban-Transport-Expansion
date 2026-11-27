package younin.use;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import younin.use.item.DispatcherBaton;
import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public final class UseItems {
    public static final CreativeModeTab MAIN_TAB = CreativeTabRegistry.create(new ResourceLocation(Use.MOD_ID, "main"),
            () -> new ItemStack(UseItems.USE_EMBLEM_ITEM.get()));

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Use.MOD_ID, Registry.BLOCK_REGISTRY);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Use.MOD_ID, Registry.ITEM_REGISTRY);

    public static final RegistrySupplier<Block> USE_EMBLEM = BLOCKS.register("use_emblem",
            () -> new Block(BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)));

    public static final RegistrySupplier<Item> USE_EMBLEM_ITEM = ITEMS.register("use_emblem",
            () -> new BlockItem(USE_EMBLEM.get(), new Item.Properties().tab(MAIN_TAB)));

    public static final RegistrySupplier<Item> DISPATCHER_BATON = ITEMS.register("dispatcher_baton",
            () -> new DispatcherBaton(new Item.Properties().tab(MAIN_TAB)));

    private UseItems() {
    }

    public static void register() {
        BLOCKS.register();
        ITEMS.register();
    }
}
