package zone.moddev.mc.basesteam.graphics;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import zone.moddev.mc.basesteam.init.Items;
import zone.moddev.mc.basesteam.items.MusketItem;
import zone.moddev.mc.basesteam.init.Entities;

/** Client-only renderer registration. */
@OnlyIn(Dist.CLIENT)
public final class ClientRegistration {
    private ClientRegistration() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(ClientRegistration::onClientSetup);
        modBus.addListener(ClientRegistration::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Entities.DRILLBIT, DrillBitRenderer::new);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(Items.blackpowder_musket, ResourceLocation.withDefaultNamespace("loaded"),
                    (item, world, entity, seed) -> MusketItem.isLoaded(item) ? 1.0F : 0.0F);
            ItemProperties.register(Items.blackpowder_musket, ResourceLocation.withDefaultNamespace("loading"),
                    (item, world, entity, seed) -> entity != null && entity.isUsingItem()
                            && entity.getUseItem() == item ? 1.0F : 0.0F);
        });
    }
}
