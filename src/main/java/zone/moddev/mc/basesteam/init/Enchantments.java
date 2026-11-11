package zone.moddev.mc.basesteam.init;

import zone.moddev.mc.basesteam.SteamAdvantage;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.resources.ResourceLocation;

/** Stable save identities; holders belong to the current world's data-driven registry. */
public final class Enchantments {
    public static final ResourceKey<Enchantment> rapid_reload = key("rapid_reload");
    public static final ResourceKey<Enchantment> powderless = key("powderless");
    public static final ResourceKey<Enchantment> high_explosive = key("high_explosive");
    public static final ResourceKey<Enchantment> recoil = key("recoil");

    private Enchantments() {}

    public static int level(ResourceKey<Enchantment> enchantment, ItemStack stack, RegistryAccess registries) {
        return EnchantmentHelper.getItemEnchantmentLevel(
                registries.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(enchantment), stack);
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath(SteamAdvantage.MODID, path));
    }
}
