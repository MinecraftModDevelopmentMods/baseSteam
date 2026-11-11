package zone.moddev.mc.basesteam.init;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zone.moddev.mc.basesteam.SteamAdvantage;

/** Legacy smith careers map to their native 1.14 professions; prices and levels are unchanged. */
@Mod.EventBusSubscriber(modid = SteamAdvantage.MODID)
public final class Villages {
    private Villages() {}
    public static void init() {}

    @SubscribeEvent
    public static void addTrades(VillagerTradesEvent event) {
        net.minecraft.resources.ResourceKey<VillagerProfession> profession = event.getType();
        List<VillagerTrades.ItemListing> novice = event.getTrades().get(1);
        if (profession == VillagerProfession.ARMORER || profession == VillagerProfession.TOOLSMITH
                || profession == VillagerProfession.WEAPONSMITH || profession == VillagerProfession.LIBRARIAN) {
            novice.add(sell(Items.steam_governor, -4, -1));
        }
        if (profession == VillagerProfession.LIBRARIAN) novice.add(sell(Blocks.steam_pipe.asItem(), -8, -4));
        if (profession == VillagerProfession.WEAPONSMITH || profession == VillagerProfession.TOOLSMITH) {
            novice.add(sell(Items.blackpowder_cartridge, -7, -5));
            novice.add(sell(Items.blackpowder_musket, 10, 15));
        }
        if (profession == VillagerProfession.CLERIC) {
            event.getTrades().get(3).add((level, trader, random) -> {
                int enchantmentLevel = 5 + random.nextInt(15);
                ItemStack musket = EnchantmentHelper.enchantItem(random,
                        new ItemStack(Items.blackpowder_musket), enchantmentLevel,
                        trader.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                                .getOrThrow(EnchantmentTags.NON_TREASURE).stream());
                int price = Math.min(randomPrice(random, 15, 25) + enchantmentLevel, 64);
                return offer(price, musket);
            });
        }
    }

    private static VillagerTrades.ItemListing sell(Item item, int min, int max) {
        return (level, trader, random) -> {
            int price = randomPrice(random, min, max);
            return offer(price < 0 ? 1 : price, new ItemStack(item, price < 0 ? -price : 1));
        };
    }

    private static int randomPrice(RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static MerchantOffer offer(int emeralds, ItemStack output) {
        return new MerchantOffer(new ItemCost(net.minecraft.world.item.Items.EMERALD, emeralds), output, 7, 1, 0.0F);
    }
}
