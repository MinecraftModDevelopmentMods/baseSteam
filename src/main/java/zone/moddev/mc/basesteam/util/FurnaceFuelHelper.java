package zone.moddev.mc.basesteam.util;

import net.minecraft.world.item.ItemStack;

/** Uses NeoForge's fuel data maps and item/event burn-time overrides. */
public final class FurnaceFuelHelper {
    private FurnaceFuelHelper() {}

    public static int getBurnTime(ItemStack stack) {
        return stack.getBurnTime(null);
    }
}
