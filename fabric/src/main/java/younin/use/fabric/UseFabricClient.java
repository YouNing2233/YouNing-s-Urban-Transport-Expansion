package younin.use.fabric;

import net.fabricmc.api.ClientModInitializer;
import younin.use.Use;

public class UseFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Use.initClient();
    }
}
