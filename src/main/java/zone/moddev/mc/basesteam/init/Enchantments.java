package zone.moddev.mc.basesteam.init;

import zone.moddev.mc.basesteam.SteamAdvantage;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SteamAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Enchantments {
	private static final TagKey<Item> MUSKETS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(SteamAdvantage.MODID, "muskets"));

	public static Enchantment rapid_reload;
	public static Enchantment powderless;
	public static Enchantment high_explosive;
	public static Enchantment recoil;

	public static void init(){
		createEnchantments();
	}

	@SubscribeEvent
	public static void registerEnchantments(RegisterEvent event) {
		if (!ForgeRegistries.Keys.ENCHANTMENTS.equals(event.getRegistryKey())) return;
		init();
		event.register(ForgeRegistries.Keys.ENCHANTMENTS, registry -> {
			registry.register(ResourceLocation.fromNamespaceAndPath(SteamAdvantage.MODID, "high_explosive"), high_explosive);
			registry.register(ResourceLocation.fromNamespaceAndPath(SteamAdvantage.MODID, "powderless"), powderless);
			registry.register(ResourceLocation.fromNamespaceAndPath(SteamAdvantage.MODID, "rapid_reload"), rapid_reload);
			registry.register(ResourceLocation.fromNamespaceAndPath(SteamAdvantage.MODID, "recoil"), recoil);
		});
	}

	private static void createEnchantments() {
		if(high_explosive != null) return;
		high_explosive = musket(5, 5, Enchantment.dynamicCost(7, 7), Enchantment.dynamicCost(17, 7), 2);
		powderless = musket(1, 1, Enchantment.constantCost(20), Enchantment.constantCost(50), 8);
		rapid_reload = musket(2, 1, Enchantment.constantCost(15), Enchantment.constantCost(50), 4);
		recoil = musket(10, 5, Enchantment.dynamicCost(2, 5), Enchantment.dynamicCost(12, 5), 1);

	}

	private static Enchantment musket(int weight, int maxLevel, Enchantment.Cost min, Enchantment.Cost max,
			int anvilCost) {
		return new Enchantment(Enchantment.definition(MUSKETS, weight, maxLevel, min, max,
				anvilCost, EquipmentSlot.MAINHAND));
	}
}
