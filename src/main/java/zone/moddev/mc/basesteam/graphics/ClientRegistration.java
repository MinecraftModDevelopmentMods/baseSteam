package zone.moddev.mc.basesteam.graphics;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import zone.moddev.mc.basesteam.init.Items;
import zone.moddev.mc.basesteam.items.MusketItem;
import zone.moddev.mc.basesteam.init.Entities;

/** Client-only renderer registration. */
public final class ClientRegistration {
    private ClientRegistration() {}

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Entities.DRILLBIT, DrillBitRenderer::new);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(Items.blackpowder_musket, new ResourceLocation("loaded"),
                    (item, world, entity, seed) -> MusketItem.isLoaded(item) ? 1.0F : 0.0F);
            ItemProperties.register(Items.blackpowder_musket, new ResourceLocation("loading"),
                    (item, world, entity, seed) -> entity != null && entity.isUsingItem()
                            && entity.getUseItem() == item ? 1.0F : 0.0F);
        });
    }
}
