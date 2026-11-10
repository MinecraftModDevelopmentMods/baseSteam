package zone.moddev.mc.basesteam.init;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import cyano.poweradvantage.api.ConduitType;
import zone.moddev.mc.basesteam.SteamAdvantage;

public abstract class Power {

	public static final ConduitType steam_power = new ConduitType("steam");

	public static float ENERGY_LOST_PER_TICK = 0.0625f;
	

	private static final ResourceKey<DamageType> MACHINE_DAMAGE = damageType("steam_machine");
	private static final ResourceKey<DamageType> MUSKET_DAMAGE = damageType("musket");

	public static DamageSource machineDamage(Level level) {
		return damageSource(level, MACHINE_DAMAGE);
	}

	public static DamageSource musketDamage(Level level) {
		return damageSource(level, MUSKET_DAMAGE);
	}

	private static ResourceKey<DamageType> damageType(String id) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(SteamAdvantage.MODID, id));
	}

	private static DamageSource damageSource(Level level, ResourceKey<DamageType> type) {
		return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type));
	}
}
