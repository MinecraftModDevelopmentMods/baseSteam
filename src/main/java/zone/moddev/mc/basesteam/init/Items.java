package zone.moddev.mc.basesteam.init;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
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
        steam_governor = add("steam_governor", new Item(properties("steam_governor")));
        steam_drill_bit = add("steam_drill_bit", new Item(properties("steam_drill_bit")));
        blackpowder_cartridge = add("blackpowder_cartridge", new Item(properties("blackpowder_cartridge")));
        blackpowder_musket = add("musket", new MusketItem(properties("musket").stacksTo(1)));
        initialized = true;
    }

    private static Item add(String id, Item item) {
        allItems.put(id, item);
        return item;
    }

    private static Item.Properties properties(String path) {
        return new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                Identifier.fromNamespaceAndPath(SteamAdvantage.MODID, path)));
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        if (!ForgeRegistries.Keys.ITEMS.equals(event.getRegistryKey())) return;
        init();
        event.register(ForgeRegistries.Keys.ITEMS, registry -> allItems.forEach((id, item) -> {
            registry.register(Identifier.fromNamespaceAndPath(SteamAdvantage.MODID, id), item);
            ItemGroups.show(item);
        }));
    }

    public static Item getItemByName(String id) { return allItems.get(id); }
    public static Set<Map.Entry<String, Item>> getAllRegisteredItems() { return allItems.entrySet(); }
}
