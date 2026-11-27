package younin.use;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;
import younin.use.item.DispatcherBaton;
import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;

public final class UseItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Use.MOD_ID, net.minecraft.core.registries.Registries.ITEM);

    public static final RegistrySupplier<Item> DISPATCHER_BATON = ITEMS.register("dispatcher_baton", () -> new DispatcherBaton(new Item.Properties()));

    private UseItems() {
    }

    public static void register() {
        ITEMS.register();
        CreativeTabRegistry.appendBuiltin(BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.TOOLS_AND_UTILITIES), DISPATCHER_BATON.get());
    }
}
