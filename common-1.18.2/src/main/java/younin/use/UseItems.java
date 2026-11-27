package younin.use;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.Item;
import younin.use.item.DispatcherBaton;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;

public final class UseItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Use.MOD_ID, Registry.ITEM_REGISTRY);

    public static final RegistrySupplier<Item> DISPATCHER_BATON = ITEMS.register("dispatcher_baton", () -> new DispatcherBaton(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)));

    private UseItems() {
    }

    public static void register() {
        ITEMS.register();
    }
}
