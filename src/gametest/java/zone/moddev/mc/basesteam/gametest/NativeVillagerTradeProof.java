package zone.moddev.mc.basesteam.gametest;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.init.Items;

final class NativeVillagerTradeProof {
    private NativeVillagerTradeProof() {}

    static void verify(GameTestHelper helper) {
        var level = helper.getLevel();
        var registries = level.registryAccess();
        var registry = registries.lookupOrThrow(Registries.VILLAGER_TRADE);
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        Map<String, List<String>> placements = Map.of(
                "armorer/level_1", List.of("steam_governor"),
                "toolsmith/level_1", List.of("steam_governor", "blackpowder_cartridge", "musket"),
                "weaponsmith/level_1", List.of("steam_governor", "blackpowder_cartridge", "musket"),
                "librarian/level_1", List.of("steam_governor", "steam_pipe"),
                "cleric/level_3", List.of("enchanted_musket"));
        int placementCount = 0;
        for (var placement : placements.entrySet()) {
            var tag = registry.getOrThrow(TagKey.create(Registries.VILLAGER_TRADE,
                    Identifier.withDefaultNamespace(placement.getKey())));
            Set<String> own = tag.stream().map(holder -> holder.unwrapKey().orElseThrow().identifier())
                    .filter(id -> id.getNamespace().equals(SteamAdvantage.MODID))
                    .map(Identifier::getPath).collect(Collectors.toSet());
            helper.assertTrue(own.equals(Set.copyOf(placement.getValue())), "Wrong native trade tag " + placement.getKey());
            placementCount += own.size();
        }
        helper.assertTrue(placementCount == 10, "Missing a legacy trade placement");
        var trader = helper.spawnWithNoFreeWill(EntityType.VILLAGER, new BlockPos(1, 1, 1));
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, trader.position())
                .withParameter(LootContextParams.THIS_ENTITY, trader)
                .withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, net.minecraft.util.Unit.INSTANCE)
                .create(LootContextParamSets.VILLAGER_TRADE);
        String[] names = {"steam_governor", "steam_pipe", "blackpowder_cartridge", "musket", "enchanted_musket"};
        int[][] bounds = {{1, 4, 1, 1}, {4, 8, 1, 1}, {5, 7, 1, 1}, {1, 1, 10, 15}, {1, 1, 20, 44}};
        JsonObject enchantedData = null;
        for (int index = 0; index < names.length; index++) {
            String name = names[index];
            var nativeTrade = registry.getOrThrow(ResourceKey.create(Registries.VILLAGER_TRADE, id(name))).value();
            var encoded = VillagerTrade.CODEC.encodeStart(ops, nativeTrade).getOrThrow();
            var parsed = VillagerTrade.CODEC.parse(ops, encoded);
            helper.assertTrue(parsed.error().isEmpty() && parsed.result().isPresent(), "Native codec rejected " + name);
            if (name.equals("enchanted_musket")) enchantedData = encoded.getAsJsonObject();
            Set<Integer> counts = new HashSet<>();
            for (int sample = 1; sample <= 256; sample++) {
                long seed = 0x9E3779B97F4A7C15L * sample;
                var context = new LootContext.Builder(params).withOptionalRandomSeed(seed).create(Optional.empty());
                var offer = parsed.getOrThrow().getOffer(context);
                helper.assertTrue(offer != null, "Native trade produced no offer: " + name);
                ItemStack output = offer.getResult();
                int price = offer.getBaseCostA().getCount();
                int[] expected = bounds[index];
                helper.assertTrue(output.getCount() >= expected[0] && output.getCount() <= expected[1], "Wrong count for " + name);
                helper.assertTrue(price >= expected[2] && price <= expected[3], "Wrong price for " + name);
                helper.assertTrue(offer.getBaseCostA().is(net.minecraft.world.item.Items.EMERALD), "Wrong currency for " + name);
                helper.assertTrue(offer.getMaxUses() == 7 && offer.getXp() == 1 && offer.getPriceMultiplier() == 0.0F,
                        "Changed legacy uses, XP or price multiplier: " + name);
                helper.assertTrue(output.get(DataComponents.ADDITIONAL_TRADE_COST) == null, "Temporary trade cost leaked to output");
                if (name.equals("enchanted_musket")) {
                    RandomSource random = RandomSource.create(seed);
                    int enchantmentLevel = 5 + random.nextInt(15);
                    ItemStack legacy = EnchantmentHelper.enchantItem(random, new ItemStack(Items.blackpowder_musket),
                            enchantmentLevel, registries, Optional.of(registries.lookupOrThrow(Registries.ENCHANTMENT)
                                    .getOrThrow(EnchantmentTags.NON_TREASURE)));
                    int legacyPrice = Math.min(15 + random.nextInt(11) + enchantmentLevel, 64);
                    helper.assertTrue(price == legacyPrice && ItemStack.isSameItemSameComponents(legacy, output),
                            "Native enchantment/correlated surcharge differs from the legacy algorithm at seed " + seed);
                } else {
                    String itemId = index < 3 ? name : "musket";
                    helper.assertTrue(output.is(registries.lookupOrThrow(Registries.ITEM)
                            .getOrThrow(ResourceKey.create(Registries.ITEM, id(itemId))).value()), "Wrong trade item: " + name);
                }
                counts.add(output.getCount());
            }
            Set<Integer> expectedCounts = IntStream.rangeClosed(bounds[index][0], bounds[index][1]).boxed().collect(Collectors.toSet());
            helper.assertTrue(counts.equals(expectedCounts), "Native sampling did not cover every inclusive count for " + name);
        }
        helper.assertTrue(enchantedData != null, "Missing actual native enchanted-trade codec data");
        JsonObject fallbackData = enchantedData.deepCopy();
        fallbackData.getAsJsonObject("wants").getAsJsonObject("count")
                .addProperty("type", "missing_mod:unregistered_number_provider");
        var fallback = VillagerTrade.CODEC.parse(ops, fallbackData);
        helper.assertTrue(fallback.error().isEmpty() && fallback.result().isPresent()
                        && VillagerTrade.CODEC.encodeStart(ops, fallback.getOrThrow()).getOrThrow().equals(enchantedData),
                "Native uniform-number fallback changed the exact trade definition");
        for (int control = 0; control < 5; control++) {
            JsonObject invalid = enchantedData.deepCopy();
            switch (control) {
                case 0 -> invalid.getAsJsonObject("wants").addProperty("id", "missing_mod:unregistered_item");
                case 1 -> invalid.getAsJsonObject("gives").addProperty("count", 0);
                case 2 -> invalid.getAsJsonArray("given_item_modifiers").get(0).getAsJsonObject()
                        .addProperty("function", "missing_mod:unregistered_function");
                case 3 -> invalid.getAsJsonArray("given_item_modifiers").get(0).getAsJsonObject()
                        .addProperty("options", "#missing_mod:unbound_enchantment_tag");
                case 4 -> {
                    JsonObject count = invalid.getAsJsonObject("wants").getAsJsonObject("count");
                    count.addProperty("type", "missing_mod:unregistered_number_provider");
                    count.remove("min");
                    count.remove("max");
                }
                default -> throw new AssertionError("Unexpected negative control");
            }
            var rejected = VillagerTrade.CODEC.parse(ops, invalid);
            helper.assertTrue(rejected.error().isPresent() && rejected.result().isEmpty(),
                    "Native codec accepted invalid trade control " + control);
        }
        com.mojang.logging.LogUtils.getLogger().info(
                "BASESTEAM_NATIVE_TRADES_PASS definitions=5 placements=10 samples=1280 codecNegatives=5 fallbackWitnesses=1");
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(SteamAdvantage.MODID, path);
    }
}
