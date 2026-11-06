package zone.moddev.mc.basesteam.graphics;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import zone.moddev.mc.basesteam.blocks.DrillBitTileEntity;

/** Client-only renderer registration. */
public final class ClientRegistration {
    private ClientRegistration() {}

    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(DrillBitTileEntity.class, new DrillBitRenderer());
    }
}
