package zone.moddev.mc.basesteam.init;

import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.enchantments.HighExplosiveEnchantment;
import zone.moddev.mc.basesteam.enchantments.PowderlessEnchantment;
import zone.moddev.mc.basesteam.enchantments.RapidReloadEnchantment;
import zone.moddev.mc.basesteam.enchantments.RecoilEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SteamAdvantage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Enchantments {

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
			registry.register(new ResourceLocation(SteamAdvantage.MODID, "high_explosive"), high_explosive);
			registry.register(new ResourceLocation(SteamAdvantage.MODID, "powderless"), powderless);
			registry.register(new ResourceLocation(SteamAdvantage.MODID, "rapid_reload"), rapid_reload);
			registry.register(new ResourceLocation(SteamAdvantage.MODID, "recoil"), recoil);
		});
	}

	private static void createEnchantments() {
		if(high_explosive != null) return;
		high_explosive = new HighExplosiveEnchantment();
		powderless = new PowderlessEnchantment();
		rapid_reload = new RapidReloadEnchantment();
		recoil = new RecoilEnchantment();

	}
}
