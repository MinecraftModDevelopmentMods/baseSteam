package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeLootContractTest {
    private static final Path ROOT = Paths.get("src/main/resources");
    private static final Path TABLES = ROOT.resolve("data/basesteam/loot_table/blocks");
    private static final Path GOLDEN = Paths.get("src/test/resources/loot-baseline-262");
    private static final Path GAMETEST_GOLDEN = Paths.get("src/gametest/resources/loot-baseline-262");
    private static final String GOLDEN_MANIFEST_SHA256 = "DDA294A1509FA8ED837F5EBF9F6DDD9F7BBBAE857A824212B6D74F489999C094";

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

    @Test public void accepted262WholeLootCorpusRetainsAllItemCountWeightAndEffectFields() throws Exception {
        assertEquals(GOLDEN_MANIFEST_SHA256, sha256(GOLDEN.resolve("manifest.json")));
        assertEquals(GOLDEN_MANIFEST_SHA256, sha256(GAMETEST_GOLDEN.resolve("manifest.json")));
        var manifest = read(GOLDEN.resolve("manifest.json"));
        assertEquals("EDD10D6ED4A623F6EAA364D21480FDA3B777FB15C2195D437882DB09C0F415AB",
                manifest.get("acceptedJarSha256").getAsString());
        var entries = manifest.getAsJsonArray("entries");
        assertEquals(15, entries.size());
        Set<String> itemNames = new TreeSet<>();
        Set<String> expectedResources = new TreeSet<>();
        for (var value : entries) {
            var entry = value.getAsJsonObject();
            assertEquals(entry.get("originalSha256").getAsString(), sha256(GOLDEN.resolve(entry.get("original").getAsString())));
            assertEquals(entry.get("nativeSha256").getAsString(), sha256(GOLDEN.resolve(entry.get("native").getAsString())));
            assertEquals(entry.get("originalSha256").getAsString(), sha256(GAMETEST_GOLDEN.resolve(entry.get("original").getAsString())));
            assertEquals(entry.get("nativeSha256").getAsString(), sha256(GAMETEST_GOLDEN.resolve(entry.get("native").getAsString())));
            var expected = read(GOLDEN.resolve(entry.get("native").getAsString()));
            String resource = entry.get("resource").getAsString();
            expectedResources.add(resource);
            assertEquals("Accepted quantities, IDs, weights and effect paths changed: " + resource,
                    expected, read(ROOT.resolve(resource)));
            collectItemNames(expected, itemNames);
        }
        try (var files = Files.walk(ROOT.resolve("data/basesteam/loot_table"))) {
            assertEquals(expectedResources, files.filter(Files::isRegularFile)
                    .map(file -> ROOT.relativize(file).toString().replace('\\', '/'))
                    .collect(java.util.stream.Collectors.toSet()));
        }
        zone.moddev.mc.basesteam.testing.NativeTestBootstrap.registerHolderSetTypes();
        var builtinItemKeys = Set.copyOf(net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet());
        var standIns = net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet().stream()
                .map(Object::toString).filter(name -> name.startsWith("minecraft:")
                        && !name.equals("minecraft:air") && !itemNames.contains(name))
                .sorted().limit(itemNames.size()).toList();
        assertEquals(itemNames.size(), standIns.size());
        Map<String, String> forward = new HashMap<>();
        Map<String, String> reverse = new HashMap<>();
        int index = 0;
        for (String name : itemNames) {
            String standIn = standIns.get(index++);
            forward.put(name, standIn);
            assertNull(reverse.put(standIn, name));
        }
        var world = net.minecraft.data.registries.VanillaRegistries.createWorldLookup();
        var lookup = net.minecraft.data.registries.VanillaRegistries.createReloadableLookup(world);
        var ops = lookup.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        var codec = net.minecraft.world.level.storage.loot.LootTable.DIRECT_CODEC;
        for (var value : entries) {
            var entry = value.getAsJsonObject();
            var expected = read(GOLDEN.resolve(entry.get("native").getAsString()));
            var substituted = expected.deepCopy();
            rewriteItemNames(substituted, forward);
            var parsed = codec.parse(ops, substituted).getOrThrow();
            var encoded = codec.encodeStart(ops, parsed).getOrThrow();
            assertEquals(encoded, codec.encodeStart(ops, codec.parse(ops, encoded).getOrThrow()).getOrThrow());
            rewriteItemNames(encoded, reverse);
            assertEquals("Whole native table lost an accepted field: " + entry.get("resource"),
                    canonicalLoot(expected, ""), canonicalLoot(encoded, ""));
        }
        assertEquals("Codec-only bijections must not register item stand-ins", builtinItemKeys,
                Set.copyOf(net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet()));
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private static void collectItemNames(JsonElement value, Set<String> names) {
        if (value.isJsonObject()) {
            var object = value.getAsJsonObject();
            if (object.has("type") && object.get("type").getAsString().equals("minecraft:item"))
                names.add(object.get("name").getAsString());
            for (var entry : object.entrySet()) collectItemNames(entry.getValue(), names);
        } else if (value.isJsonArray()) for (var child : value.getAsJsonArray()) collectItemNames(child, names);
    }

    private static void rewriteItemNames(JsonElement value, Map<String, String> names) {
        if (value.isJsonObject()) {
            var object = value.getAsJsonObject();
            if (object.has("type") && object.get("type").getAsString().equals("minecraft:item")) {
                String original = object.get("name").getAsString();
                assertTrue("Missing bijective codec-only stand-in: " + original, names.containsKey(original));
                object.addProperty("name", names.get(original));
            }
            for (var entry : object.entrySet()) rewriteItemNames(entry.getValue(), names);
        } else if (value.isJsonArray()) for (var child : value.getAsJsonArray()) rewriteItemNames(child, names);
    }

    private static JsonElement canonicalLoot(JsonElement value, String member) {
        if (member.equals("modifier") && value.isJsonArray() && value.getAsJsonArray().size() == 1)
            return canonicalLoot(value.getAsJsonArray().get(0), "");
        if (value.isJsonObject()) {
            var result = new JsonObject();
            for (var entry : value.getAsJsonObject().entrySet()) {
                String key = entry.getKey();
                JsonElement child = entry.getValue();
                if (key.equals("pools") && child.isJsonArray() && child.getAsJsonArray().isEmpty()) continue;
                if (child.isJsonPrimitive()) {
                    var primitive = child.getAsJsonPrimitive();
                    if (primitive.isNumber() && ((key.equals("weight") && primitive.getAsDouble() == 1)
                            || ((key.equals("quality") || key.equals("bonus_rolls")) && primitive.getAsDouble() == 0))) continue;
                    if (key.equals("add") && primitive.isBoolean() && !primitive.getAsBoolean()) continue;
                    if (key.equals("type") && primitive.isString()) {
                        result.add(key, new JsonPrimitive(net.minecraft.resources.Identifier.parse(primitive.getAsString()).toString()));
                        continue;
                    }
                }
                result.add(key, canonicalLoot(child, key));
            }
            return result;
        }
        if (value.isJsonArray()) {
            var result = new JsonArray();
            for (var child : value.getAsJsonArray()) result.add(canonicalLoot(child, ""));
            return result;
        }
        return value.deepCopy();
    }

    private static JsonObject read(Path path) throws Exception {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }
}
