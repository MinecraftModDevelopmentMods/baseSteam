package zone.moddev.mc.basesteam.init;

import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.enchantments.HighExplosiveEnchantment;
import zone.moddev.mc.basesteam.enchantments.PowderlessEnchantment;
import zone.moddev.mc.basesteam.enchantments.RapidReloadEnchantment;
import zone.moddev.mc.basesteam.enchantments.RecoilEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;

import java.util.HashSet;
import java.util.Set;

public class Enchantments {

	public static Enchantment rapid_reload;
	public static Enchantment powderless;
	public static Enchantment high_explosive;
	public static Enchantment recoil;
	
	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;

		high_explosive = new HighExplosiveEnchantment();
		powderless = new PowderlessEnchantment();
		rapid_reload = new RapidReloadEnchantment();
		recoil = new RecoilEnchantment();

		// Match PersistentRegistryManager snapshots and numeric NBT; keep keys and allocation order.
		Enchantment.REGISTRY.register(getNextEnchantmentID(),
				new ResourceLocation(SteamAdvantage.LEGACY_MODID+":"+"high_explosive"),
				high_explosive);
		Enchantment.REGISTRY.register(getNextEnchantmentID(),
				new ResourceLocation(SteamAdvantage.LEGACY_MODID+":"+"powderless"),
				powderless);
		Enchantment.REGISTRY.register(getNextEnchantmentID(),
				new ResourceLocation(SteamAdvantage.LEGACY_MODID+":"+"rapid_reload"),
				rapid_reload);
		Enchantment.REGISTRY.register(getNextEnchantmentID(),
				new ResourceLocation(SteamAdvantage.LEGACY_MODID+":"+"recoil"),
				recoil);

		
		initDone = true;
	}

	
	private static final Set<Integer> reservedIDs = new HashSet<>();
	private static int getNextEnchantmentID(){
		for(int i = 0; i < 255; i++){
			if(Enchantment.REGISTRY.getObjectById(i) == null && reservedIDs.contains(i) == false){
				reservedIDs.add(i);
				return i;
			}
		}
		FMLLog.severe("Failed to find free enchantment ID!");
		return 255;
	}
}
