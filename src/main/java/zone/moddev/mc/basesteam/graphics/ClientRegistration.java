package zone.moddev.mc.basesteam.graphics;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import zone.moddev.mc.basesteam.init.Entities;

/** Client-only renderer registration. */
public final class ClientRegistration {
    private ClientRegistration() {}

    public static void register(BusGroup modBus) {
        EntityRenderersEvent.RegisterRenderers.BUS.addListener(ClientRegistration::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Entities.DRILLBIT, DrillBitRenderer::new);
    }

}
