package zone.moddev.mc.basesteam.init;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import zone.moddev.mc.basepower.init.ItemGroups;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.items.MusketItem;

@Mod.EventBusSubscriber(modid = SteamAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Items {
    public static final Map<String, Item> allItems = new LinkedHashMap<>();
    public static Item steam_governor;
    public static Item steam_drill_bit;
    public static Item blackpowder_cartridge;
    public static Item blackpowder_musket;
    private static boolean initialized;

    private Items() {}

    /** Constructs items during the Forge item registry event. */
    public static synchronized void init() {
        if (initialized) return;
        steam_governor = add("steam_governor", new Item(new Item.Properties()));
        steam_drill_bit = add("steam_drill_bit", new Item(new Item.Properties()));
        blackpowder_cartridge = add("blackpowder_cartridge", new Item(new Item.Properties()));
        blackpowder_musket = add("musket", new MusketItem(new Item.Properties().stacksTo(1)));
        initialized = true;
    }

    private static Item add(String id, Item item) {
        allItems.put(id, item);
        return item;
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        if (!ForgeRegistries.Keys.ITEMS.equals(event.getRegistryKey())) return;
        init();
        event.register(ForgeRegistries.Keys.ITEMS, registry -> allItems.forEach((id, item) -> {
            registry.register(new ResourceLocation(SteamAdvantage.MODID, id), item);
            ItemGroups.show(item);
        }));
    }

    public static Item getItemByName(String id) { return allItems.get(id); }
    public static Set<Map.Entry<String, Item>> getAllRegisteredItems() { return allItems.entrySet(); }
}
