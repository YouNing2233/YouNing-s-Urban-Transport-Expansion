package younin.use.client;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import net.minecraft.client.renderer.RenderType;
import younin.use.UseBlockEntityTypes;
import younin.use.UseItems;
import younin.use.render.RenderWideApgDoor;
import younin.use.render.RenderWidePsdDoor;

/** 客户端注册：方块实体渲染器与渲染层。Fabric / Forge 都由 Use.initClient() 调进来。 */
public final class UseClient {

    private UseClient() {
    }

    public static void register() {
        BlockEntityRendererRegistry.register(UseBlockEntityTypes.PSD_DOOR_WIDE.get(), RenderWidePsdDoor::new);
        BlockEntityRendererRegistry.register(UseBlockEntityTypes.APG_DOOR_WIDE.get(), RenderWideApgDoor::new);
        RenderTypeRegistry.register(RenderType.cutout(),
                UseItems.PSD_DOOR_WIDE.get(),
                UseItems.APG_DOOR_WIDE.get());
    }
}
