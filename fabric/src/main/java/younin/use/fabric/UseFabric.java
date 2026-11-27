package younin.use.fabric;

import net.fabricmc.api.ModInitializer;
import younin.use.Use;

public class UseFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Use.init();
    }
}
