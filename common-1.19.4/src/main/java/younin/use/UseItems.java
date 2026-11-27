package younin.use;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import younin.use.item.DispatcherBaton;

public final class UseItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Use.MOD_ID, net.minecraft.core.registries.Registries.ITEM);

    public static final RegistrySupplier<Item> DISPATCHER_BATON = ITEMS.register("dispatcher_baton", () -> new DispatcherBaton(new Item.Properties()));

    private UseItems() {
    }

    public static void register() {
        ITEMS.register();

        // 1.19.4 的创造模式标签页不是注册表项，Architectury 直接帮我们建一个并用自己当图标
        final CreativeTabRegistry.TabSupplier tab = CreativeTabRegistry.create(new ResourceLocation(Use.MOD_ID, "main"), () -> new ItemStack(DISPATCHER_BATON.get()));
        CreativeTabRegistry.append(tab, DISPATCHER_BATON.get());
    }
}
