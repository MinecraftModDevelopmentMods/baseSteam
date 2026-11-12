package zone.moddev.mc.basesteam.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;

/** Forge-aware access to the vanilla furnace fuel calculation. */
public final class FurnaceFuelHelper {
    private FurnaceFuelHelper() {}

    public static int getBurnTime(ItemStack stack) {
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        FuelValues fuels = server == null ? FuelValues.vanillaBurnTimes(
                net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(
                        net.minecraft.core.registries.BuiltInRegistries.REGISTRY),
                net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS) : server.fuelValues();
        return fuels.burnDuration(stack, null);
    }

    public static int getBurnTime(ItemStack stack, Level level) {
        return level == null ? getBurnTime(stack) : level.fuelValues().burnDuration(stack, null);
    }
}
