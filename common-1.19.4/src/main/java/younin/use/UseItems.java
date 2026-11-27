package younin.use;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import younin.use.item.DispatcherBaton;
import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public final class UseItems {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Use.MOD_ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Use.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Block> USE_EMBLEM = BLOCKS.register("use_emblem",
            () -> new Block(BlockBehaviour.Properties.of(Material.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL)));

    public static final RegistrySupplier<Item> USE_EMBLEM_ITEM = ITEMS.register("use_emblem",
            () -> new BlockItem(USE_EMBLEM.get(), new Item.Properties()));

    public static final RegistrySupplier<Item> DISPATCHER_BATON = ITEMS.register("dispatcher_baton",
            () -> new DispatcherBaton(new Item.Properties()));

    private UseItems() {
    }

    public static void register() {
        BLOCKS.register();
        ITEMS.register();

        final CreativeTabRegistry.TabSupplier tab = CreativeTabRegistry.create(new ResourceLocation(Use.MOD_ID, "main"),
                () -> new ItemStack(USE_EMBLEM_ITEM.get()));
        CreativeTabRegistry.append(tab, DISPATCHER_BATON.get(), USE_EMBLEM_ITEM.get());
    }
}
