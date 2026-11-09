package zone.moddev.mc.basesteam.init;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
        ItemGroups.init();
        steam_governor = add("steam_governor", new Item(properties()));
        steam_drill_bit = add("steam_drill_bit", new Item(properties()));
        blackpowder_cartridge = add("blackpowder_cartridge", new Item(properties()));
        blackpowder_musket = add("musket", new MusketItem(properties().stacksTo(1)));
        initialized = true;
    }

    private static Item.Properties properties() {
        return new Item.Properties().tab(ItemGroups.tab_powerAdvantage);
    }

    private static Item add(String id, Item item) {
        item.setRegistryName(SteamAdvantage.MODID, id);
        allItems.put(id, item);
        return item;
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Item> event) {
        init();
        event.getRegistry().registerAll(allItems.values().toArray(new Item[0]));
    }

    public static Item getItemByName(String id) { return allItems.get(id); }
    public static Set<Map.Entry<String, Item>> getAllRegisteredItems() { return allItems.entrySet(); }
}
