package zone.moddev.mc.basesteam.gametest;

import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.gametest.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestDontPrefix;
import net.minecraftforge.gametest.GameTestNamespace;
import zone.moddev.mc.basepower.migration.LegacyWorldDataHook;
import zone.moddev.mc.basepower.util.ItemData;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.blocks.DrillBitTileEntity;
import zone.moddev.mc.basesteam.init.Blocks;
import zone.moddev.mc.basesteam.init.Entities;
import zone.moddev.mc.basesteam.init.Items;
import zone.moddev.mc.basesteam.init.Enchantments;
import zone.moddev.mc.basesteam.items.MusketItem;

@GameTestNamespace(SteamAdvantage.MODID)
@GameTestDontPrefix
public final class SteamNativeGameTests {
    private SteamNativeGameTests() {}

    @GameTest(structure = "basesteam:basesteam_empty")
    public static void drillBitPacketPreservesTypePositionAndDirection(GameTestHelper helper) {
        BlockPos local = new BlockPos(1, 1, 1);
        helper.setBlock(local, Blocks.drillbit);
        DrillBitTileEntity source = helper.getBlockEntity(local, DrillBitTileEntity.class);
        var registries = helper.getLevel().registryAccess();
        for (Direction direction : Direction.values()) {
            source.setDirection(direction);
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
            try {
                ClientboundBlockEntityDataPacket.STREAM_CODEC.encode(buffer, source.getUpdatePacket());
                var received = ClientboundBlockEntityDataPacket.STREAM_CODEC.decode(buffer);
                helper.assertTrue(received.getType() == Entities.DRILLBIT, "Wrong drill-bit packet type");
                helper.assertTrue(received.getPos().equals(helper.absolutePos(local)), "Wrong drill-bit position");
                DrillBitTileEntity destination = new DrillBitTileEntity(received.getPos(), source.getBlockState());
                destination.onDataPacket(null, net.minecraft.world.level.storage.TagValueInput.create(
                        net.minecraft.util.ProblemReporter.DISCARDING, registries, received.getTag()), registries);
                helper.assertTrue(destination.getUpdateTag(registries).getByteOr("dir", (byte)0) == direction.get3DDataValue(),
                        "Lost drill-bit direction " + direction);
            } finally {
                buffer.release();
            }
        }
        helper.succeed();
    }

    @GameTest(structure = "basesteam:basesteam_empty")
    public static void legacyMusketWearAndLoadedByteSurviveNativeComponentization(GameTestHelper helper) {
        CompoundTag legacy = new CompoundTag();
        legacy.putString("id", "basesteam:musket");
        legacy.putByte("Count", (byte) 1);
        CompoundTag tag = new CompoundTag();
        tag.putInt("Damage", 249);
        tag.putByte("loaded", (byte) 1);
        tag.putString("owner", "legacy fixture");
        tag.put("Enchantments", legacyEnchantments("basesteam"));
        legacy.put("tag", tag);
        var registries = helper.getLevel().registryAccess();
        var fixed = DataFixers.getDataFixer().update(References.ITEM_STACK,
                new Dynamic<>(NbtOps.INSTANCE, legacy), 3465, SharedConstants.getCurrentVersion().dataVersion().version());
        ItemStack stack = ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), fixed.getValue()).getOrThrow();
        helper.assertTrue(stack.is(Items.blackpowder_musket), "Converted the wrong item");
        helper.assertTrue(stack.getDamageValue() == 249 && stack.getMaxDamage() == 250, "Lost legacy wear");
        helper.assertTrue(MusketItem.isLoaded(stack), "Lost the legacy loaded byte");
        assertMusketEnchantments(helper, stack);
        MusketItem.unload(stack);
        ItemStack reloaded = ItemStack.CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
                ItemStack.CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack).getOrThrow()).getOrThrow();
        helper.assertTrue(MusketItem.isNotLoaded(reloaded) && reloaded.getDamageValue() == 249,
                "Changing loaded state must not reset wear");
        helper.assertTrue("legacy fixture".equals(reloaded.get(DataComponents.CUSTOM_DATA).copyTag().getStringOr("owner", "")),
                "Changing loaded state must not discard unrelated legacy data");
        assertMusketEnchantments(helper, reloaded);
        helper.succeed();
    }

    @GameTest(structure = "basesteam:basesteam_empty")
    public static void olderMusketAliasAndTopLevelWearSurvivePowerMigration(GameTestHelper helper) {
        CompoundTag legacy = new CompoundTag();
        legacy.putString("id", "steamadvantage:musket");
        legacy.putByte("Count", (byte) 1);
        legacy.putShort("Damage", (short) 131);
        CompoundTag tag = new CompoundTag();
        tag.putByte("loaded", (byte) 2);
        tag.put("Enchantments", legacyEnchantments("steamadvantage"));
        legacy.put("tag", tag);
        LegacyWorldDataHook.prepareLegacyChunk(legacy);
        ItemStack migrated = ItemData.readStack(helper.getLevel().registryAccess(), legacy);
        helper.assertTrue(migrated.is(Items.blackpowder_musket), "The old musket ID was not remapped");
        helper.assertTrue(migrated.getDamageValue() == 131, "The legacy Damage short was lost");
        helper.assertTrue(MusketItem.isAlmostLoaded(migrated), "The partial reload was lost");
        assertMusketEnchantments(helper, migrated);
        helper.succeed();
    }

    @GameTest(structure = "basesteam:basesteam_empty")
    public static void nativeResourcesLoadWithExpectedIds(GameTestHelper helper) {
        var jvmArguments = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String name : java.util.List.of("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS")) {
            helper.assertTrue(System.getenv(name) == null, "Native JVM inherited " + name);
        }
        helper.assertTrue(jvmArguments.contains("-XX:ActiveProcessorCount=4")
                        && jvmArguments.contains("-Xmx1536m") && Runtime.getRuntime().availableProcessors() == 4
                        && Runtime.getRuntime().maxMemory() <= 1536L * 1024 * 1024,
                "Native JVM did not retain explicit CPU/heap caps: " + jvmArguments);
        com.mojang.logging.LogUtils.getLogger().info(
                "BASESTEAM_NATIVE_JVM_PASS inheritedOptions=absent processorCap=4 heapCapMiB=1536 args={}", jvmArguments);
        var level = helper.getLevel();
        var registries = level.registryAccess();
        var nativeEnchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        for (var key : java.util.List.of(Enchantments.high_explosive, Enchantments.powderless,
                Enchantments.rapid_reload, Enchantments.recoil)) {
            var holder = nativeEnchantments.getOrThrow(key);
            helper.assertTrue(holder.is(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE),
                    "Missing table discovery for " + key.identifier());
            helper.assertTrue(holder.is(net.minecraft.tags.EnchantmentTags.TRADEABLE),
                    "Missing enchanted-book trade discovery for " + key.identifier());
            helper.assertTrue(holder.is(net.minecraft.tags.EnchantmentTags.ON_RANDOM_LOOT),
                    "Missing random loot discovery for " + key.identifier());
            helper.assertTrue(holder.value().isPrimaryItem(new ItemStack(Items.blackpowder_musket)),
                    "Native musket support tag was not bound for " + key.identifier());
        }
        var fragile = TagKey.create(Registries.BLOCK, id("musket_fragile"));
        helper.assertTrue(net.minecraft.world.level.block.Blocks.SHORT_GRASS.defaultBlockState().is(fragile),
                "The 1.21.1 short grass ID must bind the fragile block tag");
        helper.assertTrue(!net.minecraft.world.level.block.Blocks.STONE.defaultBlockState().is(fragile),
                "The fragile block tag must not include solid stone");
        var manager = level.getServer().getAdvancements();
        helper.assertTrue(manager.get(id("recipes/steam_pipe")) != null, "Missing Steam pipe recipe advancement");
        var unlock = manager.get(id("recipes/musket_right_normal")).value();
        var ingredient = (InventoryChangeTrigger.TriggerInstance)
                unlock.criteria().get("has_ingredient_1").triggerInstance();
        var predicate = ingredient.items().getFirst();
        helper.assertTrue(predicate.test(new ItemStack(net.minecraft.world.item.Items.FLINT_AND_STEEL)),
                "Musket discovery lost the matching ingredient");
        helper.assertTrue(!predicate.test(new ItemStack(net.minecraft.world.item.Items.STONE)),
                "An unrelated item unlocked the musket recipe");
        var tagIngredient = (InventoryChangeTrigger.TriggerInstance)
                unlock.criteria().get("has_ingredient_3").triggerInstance();
        helper.assertTrue(tagIngredient.items().getFirst().test(new ItemStack(net.minecraft.world.item.Items.OAK_PLANKS)),
                "Musket discovery lost the plank tag predicate");
        var loot = level.getServer().reloadableRegistries();
        var chest = ResourceKey.create(Registries.LOOT_TABLE, id("chests/advantage_parts"));
        LootTable chestTable = loot.getLootTable(chest);
        helper.assertTrue(chestTable != LootTable.EMPTY && chestTable.getParamSet() == LootContextParamSets.CHEST,
                "Missing Steam chest loot table");
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                .create(LootContextParamSets.CHEST);
        boolean foundSteamItem = false;
        for (int seed = 1; seed <= 32; seed++) {
            if (!chestTable.getRandomItems(params, seed).isEmpty()) foundSteamItem = true;
        }
        helper.assertTrue(foundSteamItem, "Steam chest rolls returned no items");
        helper.assertTrue(loot.getLootTable(Blocks.steam_pipe.getLootTable().orElseThrow()) != LootTable.EMPTY,
                "Missing Steam pipe block loot");
        var pipe = level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, id("steam_pipe")));
        var brass = net.minecraft.tags.ItemTags.create(Identifier.fromNamespaceAndPath("forge", "ingots/brass"));
        if (registries.lookupOrThrow(Registries.ITEM).get(brass).filter(tag -> tag.size() > 0).isPresent()) {
            var recipe = (net.minecraft.world.item.crafting.ShapedRecipe)pipe.orElseThrow().value();
            var result = recipe.assemble(net.minecraft.world.item.crafting.CraftingInput.EMPTY);
            helper.assertTrue(result.is(Blocks.steam_pipe.asItem()) && result.getCount() == 6,
                    "Wrong Steam pipe recipe output");
        } else {
            helper.assertTrue(pipe.isEmpty(), "Recipe must not load with an empty brass tag");
        }
        NativeVillagerTradeProof.verify(helper);
        assertSalvageRecipes(helper);
        helper.succeed();
    }

    private static void assertSalvageRecipes(GameTestHelper helper) {
        var level = helper.getLevel();
        var registries = level.registryAccess();
        var ops = registries.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        var mode = zone.moddev.mc.basepower.PowerAdvantage.recipeMode;
        boolean enabled = mode == zone.moddev.mc.basepower.RecipeMode.APOCALYPTIC;
        String[] inputs = {"steam_boiler_coal", "steam_boiler_electric", "steam_boiler_geothermal",
                "steam_crusher", "steam_drill", "steam_elevator", "steam_furnace", "steam_governor", "steam_tank"};
        for (String input : inputs) {
            var loaded = level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, id("crushing_" + input)));
            if (!enabled) {
                helper.assertTrue(loaded.isEmpty(), "Salvage recipe loaded outside APOCALYPTIC mode: " + input);
                continue;
            }
            var actual = loaded.orElseThrow().value();
            helper.assertTrue(actual instanceof zone.moddev.mc.basemetals.recipe.CrushingRecipe,
                    "Missing native crushing serializer: " + input);
            var encoded = net.minecraft.world.item.crafting.Recipe.DIRECT_CODEC.encodeStart(ops, actual).getOrThrow();
            var parsed = net.minecraft.world.item.crafting.Recipe.DIRECT_CODEC.parse(ops, encoded).getOrThrow();
            helper.assertTrue(parsed instanceof zone.moddev.mc.basemetals.recipe.CrushingRecipe,
                    "Native salvage recipe codec did not round-trip: " + input);
            var inputItem = registries.lookupOrThrow(Registries.ITEM)
                    .getOrThrow(ResourceKey.create(Registries.ITEM, id(input))).value();
            var inventory = new net.minecraft.world.item.crafting.SingleRecipeInput(new ItemStack(inputItem));
            Identifier expectedId = input.equals("steam_governor")
                    ? Identifier.fromNamespaceAndPath("basepower", "sprocket") : id("steam_governor");
            var expectedItem = registries.lookupOrThrow(Registries.ITEM)
                    .getOrThrow(ResourceKey.create(Registries.ITEM, expectedId)).value();
            int count = input.equals("steam_tank") ? 1 : input.equals("steam_elevator") ? 3 : 2;
            for (var recipe : java.util.List.of((zone.moddev.mc.basemetals.recipe.CrushingRecipe)actual,
                    (zone.moddev.mc.basemetals.recipe.CrushingRecipe)parsed)) {
                helper.assertTrue(recipe.matches(inventory, level), "Native salvage input did not match: " + input);
                helper.assertTrue(!recipe.matches(new net.minecraft.world.item.crafting.SingleRecipeInput(
                        new ItemStack(net.minecraft.world.item.Items.DIAMOND)), level), "Unrelated salvage input matched: " + input);
                ItemStack result = recipe.assemble(inventory);
                helper.assertTrue(result.is(expectedItem) && result.getCount() == count,
                        "Salvage output identity or exact quantity changed: " + input);
            }
            helper.assertTrue(inventory.item().is(inputItem) && inventory.item().getCount() == 1,
                    "Salvage matching/assembly mutated the supplied input: " + input);
        }
        com.mojang.logging.LogUtils.getLogger().info(
                "BASESTEAM_NATIVE_SALVAGE_PASS mode={} present={} absent={} nativeCodecs={} inputNegatives={}",
                mode, enabled ? 9 : 0, enabled ? 0 : 9, enabled ? 9 : 0, enabled ? 9 : 0);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(SteamAdvantage.MODID, path);
    }

    private static ListTag legacyEnchantments(String namespace) {
        ListTag result = new ListTag();
        for (String path : java.util.List.of("high_explosive", "powderless", "rapid_reload", "recoil")) {
            CompoundTag enchantment = new CompoundTag();
            enchantment.putString("id", namespace + ":" + path);
            enchantment.putShort("lvl", (short) (path.equals("high_explosive") || path.equals("recoil") ? 5 : 1));
            result.add(enchantment);
        }
        return result;
    }

    private static void assertMusketEnchantments(GameTestHelper helper, ItemStack stack) {
        var registries = helper.getLevel().registryAccess();
        for (var key : java.util.List.of(Enchantments.high_explosive, Enchantments.powderless,
                Enchantments.rapid_reload, Enchantments.recoil)) {
            int expected = key.equals(Enchantments.high_explosive) || key.equals(Enchantments.recoil) ? 5 : 1;
            helper.assertTrue(Enchantments.level(key, stack, registries) == expected,
                    "Lost legacy enchantment holder or level: " + key.identifier());
        }
    }
}
