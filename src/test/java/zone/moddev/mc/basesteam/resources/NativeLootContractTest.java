package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeLootContractTest {
    private static final Path ROOT = Paths.get("src/main/resources");
    private static final Path TABLES = ROOT.resolve("data/basesteam/loot_table/blocks");

    @Test public void ordinaryBlocksKeepOneSelfDropWithExplosionSurvival() throws Exception {
        for (String id : new String[] { "steam_pipe", "steam_track", "steam_boiler_coal",
                "steam_boiler_electric", "steam_boiler_geothermal", "steam_tank", "steam_furnace",
                "steam_crusher", "steam_drill", "steam_elevator", "steam_switch", "steam_still",
                "steam_pump", "steam_boiler_oil" }) {
            JsonObject table = read(TABLES.resolve(id + ".json"));
            assertEquals("minecraft:block", table.get("type").getAsString());
            assertEquals(1, table.getAsJsonArray("pools").size());
            JsonObject pool = table.getAsJsonArray("pools").get(0).getAsJsonObject();
            assertEquals(1, pool.get("rolls").getAsInt());
            assertEquals(1, pool.getAsJsonArray("entries").size());
            JsonObject entry = pool.getAsJsonArray("entries").get(0).getAsJsonObject();
            assertEquals("minecraft:item", entry.get("type").getAsString());
            assertEquals("basesteam:" + id, entry.get("name").getAsString());
            assertEquals("minecraft:survives_explosion", pool.getAsJsonObject("condition").get("type").getAsString());
            assertFalse(pool.has("conditions"));
        }
    }

    @Test public void helpersHaveNoSelfDropTables() {
        for (String id : new String[] { "drillbit", "platform", "pump_pipe_steam" }) {
            assertFalse(id, Files.exists(TABLES.resolve(id + ".json")));
        }
    }

    @Test public void nativeExplosionPredicateCodecRejectsLegacyAndUnknownDiscriminators() throws Exception {
        zone.moddev.mc.basesteam.testing.NativeTestBootstrap.start();
        var codec = net.minecraft.world.level.storage.loot.predicates.LootItemCondition.DIRECT_CODEC;
        var ops = com.mojang.serialization.JsonOps.INSTANCE;
        var condition = read(TABLES.resolve("steam_pipe.json")).getAsJsonArray("pools")
                .get(0).getAsJsonObject().getAsJsonObject("condition");
        var decoded = codec.parse(ops, condition);
        assertTrue(decoded.error().isEmpty());
        assertEquals(condition, codec.encodeStart(ops, decoded.getOrThrow()).getOrThrow());
        var old = condition.deepCopy();
        old.add("condition", old.remove("type"));
        var unknown = condition.deepCopy();
        unknown.addProperty("type", "missing_mod:unknown_predicate");
        for (var invalid : java.util.List.of(old, unknown)) {
            var rejected = codec.parse(ops, invalid);
            assertTrue(rejected.error().isPresent());
            assertTrue(rejected.result().isEmpty());
        }
    }

    @Test public void packUsesNative263DataFormatAndForgeClientMetadata() throws Exception {
        JsonObject pack = read(ROOT.resolve("pack.mcmeta")).getAsJsonObject("pack");
        assertEquals("[121,0]", pack.getAsJsonArray("min_format").toString());
        assertEquals("[121,0]", pack.getAsJsonArray("max_format").toString());
        assertFalse(pack.has("pack_format"));
        assertFalse(pack.has("supported_formats"));
        assertFalse(pack.has("forge:server_data_pack_format"));
        assertFalse(pack.has("forge:resource_pack_format"));
        assertFalse(pack.has("forge:data_pack_format"));
    }

    @Test public void nativeSingletonModifierSequenceCanonicalizesToAFunctionObject() throws Exception {
        zone.moddev.mc.basesteam.testing.NativeTestBootstrap.registerHolderSetTypes();
        var codec = net.minecraft.world.level.storage.loot.functions.LootItemFunctions.DIRECT_CODEC;
        var ops = com.mojang.serialization.JsonOps.INSTANCE;
        var sequence = JsonParser.parseString("[{\"type\":\"minecraft:set_count\",\"count\":2}]");
        var decoded = codec.parse(ops, sequence).getOrThrow();
        var encoded = codec.encodeStart(ops, decoded).getOrThrow();
        assertTrue(encoded.isJsonObject());
        var trade = new JsonObject();
        trade.add("given_item_modifier", encoded);
        assertThrows(ClassCastException.class, () -> trade.getAsJsonArray("given_item_modifier"));
        var function = encoded.getAsJsonObject();
        assertEquals("minecraft:set_count", function.get("type").getAsString());
        assertEquals(2, function.get("count").getAsInt());
        assertEquals(encoded, codec.encodeStart(ops, codec.parse(ops, encoded).getOrThrow()).getOrThrow());
        var legacy = function.deepCopy();
        legacy.add("function", legacy.remove("type"));
        var unknown = function.deepCopy();
        unknown.addProperty("type", "missing_mod:unregistered_function");
        for (var invalid : java.util.List.of(legacy, unknown)) {
            var rejected = codec.parse(ops, invalid);
            assertTrue(rejected.error().isPresent());
            assertTrue(rejected.result().isEmpty());
        }
    }

    @Test public void wholeNativeTablePreservesEveryModifierAndConditionScopeDespiteLegacySilentLoss() throws Exception {
        zone.moddev.mc.basesteam.testing.NativeTestBootstrap.registerHolderSetTypes();
        var world = net.minecraft.data.registries.VanillaRegistries.createWorldLookup();
        var lookup = net.minecraft.data.registries.VanillaRegistries.createReloadableLookup(world);
        var ops = lookup.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        var codec = net.minecraft.world.level.storage.loot.LootTable.DIRECT_CODEC;
        var source = JsonParser.parseString("""
                {"type":"minecraft:block","modifier":[{"type":"minecraft:set_count","count":7}],
                 "pools":[{"rolls":1,"condition":{"type":"minecraft:survives_explosion"},
                  "modifier":[{"type":"minecraft:set_count","count":5}],
                  "entries":[{"type":"minecraft:item","name":"minecraft:diamond","weight":1,
                   "condition":{"type":"minecraft:all_of","terms":[
                    {"type":"minecraft:survives_explosion"},{"type":"minecraft:random_chance","chance":1.0}]},
                   "modifier":[{"type":"minecraft:set_count","count":3},
                    {"type":"minecraft:set_count","count":{"type":"minecraft:uniform","min":4,"max":6}}]}]}]}
                """).getAsJsonObject();
        var encoded = codec.encodeStart(ops, codec.parse(ops, source).getOrThrow()).getOrThrow().getAsJsonObject();
        assertEquals(7, wholeTableScope(encoded, 0).getAsJsonObject("modifier").get("count").getAsInt());
        assertEquals(5, wholeTableScope(encoded, 1).getAsJsonObject("modifier").get("count").getAsInt());
        var entry = wholeTableScope(encoded, 2);
        assertEquals(1, net.minecraft.world.level.storage.loot.entries.UniformContainerBase.DEFAULT_WEIGHT);
        assertFalse("Native default weight must remain one, not zero", entry.has("weight"));
        assertEquals("minecraft:diamond", entry.get("name").getAsString());
        var modifiers = entry.getAsJsonArray("modifier");
        assertEquals(2, modifiers.size());
        assertEquals(3, modifiers.get(0).getAsJsonObject().get("count").getAsInt());
        assertEquals(JsonParser.parseString("{\"type\":\"minecraft:uniform\",\"min\":4,\"max\":6}"),
                modifiers.get(1).getAsJsonObject().get("count"));
        assertEquals("minecraft:survives_explosion", wholeTableScope(encoded, 1)
                .getAsJsonObject("condition").get("type").getAsString());
        var condition = entry.getAsJsonObject("condition");
        assertEquals("minecraft:all_of", condition.get("type").getAsString());
        var terms = condition.getAsJsonArray("terms");
        assertEquals(2, terms.size());
        assertEquals("minecraft:survives_explosion", terms.get(0).getAsJsonObject().get("type").getAsString());
        assertEquals("minecraft:random_chance", terms.get(1).getAsJsonObject().get("type").getAsString());
        assertEquals(1.0, terms.get(1).getAsJsonObject().get("chance").getAsDouble(), 0.0);
        assertEquals(encoded, codec.encodeStart(ops, codec.parse(ops, encoded).getOrThrow()).getOrThrow());
        for (int scope = 0; scope < 3; scope++) {
            var legacy = source.deepCopy();
            var target = wholeTableScope(legacy, scope);
            target.add("functions", target.remove("modifier"));
            var parsed = codec.parse(ops, legacy);
            assertTrue("Old plural functions must demonstrate silent loss", parsed.error().isEmpty());
            var expectedLoss = encoded.deepCopy();
            wholeTableScope(expectedLoss, scope).remove("modifier");
            assertEquals(expectedLoss, codec.encodeStart(ops, parsed.getOrThrow()).getOrThrow());
        }
        for (int scope = 1; scope < 3; scope++) {
            var legacy = source.deepCopy();
            var target = wholeTableScope(legacy, scope);
            var singular = target.remove("condition").getAsJsonObject();
            var oldConditions = new com.google.gson.JsonArray();
            if (scope == 1) oldConditions.add(singular);
            else oldConditions = singular.getAsJsonArray("terms");
            target.add("conditions", oldConditions);
            var parsed = codec.parse(ops, legacy);
            assertTrue("Old plural conditions must demonstrate silent loss", parsed.error().isEmpty());
            var expectedLoss = encoded.deepCopy();
            wholeTableScope(expectedLoss, scope).remove("condition");
            assertEquals(expectedLoss, codec.encodeStart(ops, parsed.getOrThrow()).getOrThrow());
        }
    }

    private static JsonObject wholeTableScope(JsonObject table, int scope) {
        if (scope == 0) return table;
        var pool = table.getAsJsonArray("pools").get(0).getAsJsonObject();
        if (scope == 1) return pool;
        if (scope == 2) return pool.getAsJsonArray("entries").get(0).getAsJsonObject();
        throw new IllegalArgumentException("Unknown loot scope: " + scope);
    }

    private static JsonObject read(Path path) throws Exception {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }
}
