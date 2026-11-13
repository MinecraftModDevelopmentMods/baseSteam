package zone.moddev.mc.basesteam.gametest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

final class NativeLootTableProof {
    private static final String FIXTURES = "/loot-baseline-262/";
    private static final String MANIFEST_SHA256 = "DDA294A1509FA8ED837F5EBF9F6DDD9F7BBBAE857A824212B6D74F489999C094";

    private NativeLootTableProof() {}

    static void verify(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var ops = server.reloadableRegistries().lookup().createSerializationContext(JsonOps.INSTANCE);
        var resources = server.getResourceManager().listResources("loot_table", key ->
                key.getNamespace().equals("basesteam") && key.getPath().endsWith(".json"));
        var golden = goldens(helper);
        helper.assertTrue(resources.keySet().equals(golden.keySet()) && resources.size() == 15,
                "The actual Steam loot-table inventory differs from accepted 26.2");
        JsonObject chest = null;
        JsonObject pipe = null;
        int fields = 0;
        for (var entry : resources.entrySet()) {
            var source = SteamNativeGameTests.readJson(entry.getValue());
            var expected = golden.get(entry.getKey());
            helper.assertTrue(expected.equals(source), "Accepted loot values or effect paths changed: " + entry.getKey());
            roundTrip(helper, ops, source, entry.getKey().toString());
            String path = entry.getKey().getPath();
            var tableId = Identifier.fromNamespaceAndPath("basesteam",
                    path.substring("loot_table/".length(), path.length() - ".json".length()));
            var loaded = server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, tableId));
            helper.assertTrue(loaded != LootTable.EMPTY, "Missing actual native Steam loot table: " + tableId);
            var encoded = LootTable.DIRECT_CODEC.encodeStart(ops, loaded).getOrThrow();
            helper.assertTrue(canonical(expected, "").equals(canonical(encoded, "")),
                    "Actual loaded table lost an accepted modifier, condition or quantity: " + tableId);
            fields += semanticFields(source);
            if (path.equals("loot_table/chests/advantage_parts.json")) chest = source;
            if (path.equals("loot_table/blocks/steam_pipe.json")) pipe = source;
        }
        helper.assertTrue(chest != null && pipe != null, "Missing production whole-table loss witnesses");
        Path folder = Path.of("config/additional-loot-tables/basesteam/chests");
        Set<String> names = Set.of("abandoned_mineshaft.json", "simple_dungeon.json", "village_blacksmith.json",
                "stronghold_corridor.json", "stronghold_crossing.json", "nether_bridge.json");
        try (var files = Files.list(folder)) {
            helper.assertTrue(files.filter(file -> file.getFileName().toString().endsWith(".json"))
                    .map(file -> file.getFileName().toString()).collect(java.util.stream.Collectors.toSet()).equals(names),
                    "Expected all six generated Steam ALT templates");
        } catch (IOException exception) { throw new UncheckedIOException(exception); }
        for (String name : names) {
            var template = readFile(folder.resolve(name));
            fields += semanticFields(template);
            var projection = nativeAltProjection(helper, template, chest);
            roundTrip(helper, ops, projection, "generated ALT template " + name);
        }
        helper.assertTrue(fields == 42, "Missing modifier/condition scopes in the whole-table corpus: " + fields);
        assertSilentLoss(helper, ops, chest, pipe);
        com.mojang.logging.LogUtils.getLogger().info(
                "BASESTEAM_NATIVE_LOOT_TABLES_PASS production=15 generated=6 golden=15 semanticFields=42 codecLossControls=5");
    }

    private static Map<Identifier, JsonObject> goldens(GameTestHelper helper) {
        byte[] bytes = fixture("manifest.json");
        helper.assertTrue(sha256(bytes).equals(MANIFEST_SHA256), "Accepted loot manifest changed");
        var manifest = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
        helper.assertTrue(manifest.get("acceptedJarSha256").getAsString()
                .equals("EDD10D6ED4A623F6EAA364D21480FDA3B777FB15C2195D437882DB09C0F415AB"),
                "Wrong accepted Steam JAR baseline");
        Map<Identifier, JsonObject> result = new HashMap<>();
        for (var value : manifest.getAsJsonArray("entries")) {
            var entry = value.getAsJsonObject();
            byte[] original = fixture(entry.get("original").getAsString());
            byte[] nativeBytes = fixture(entry.get("native").getAsString());
            helper.assertTrue(sha256(original).equals(entry.get("originalSha256").getAsString())
                            && sha256(nativeBytes).equals(entry.get("nativeSha256").getAsString()),
                    "Accepted loot fixture drift: " + entry.get("resource"));
            String path = entry.get("resource").getAsString().substring("data/basesteam/".length());
            helper.assertTrue(result.put(Identifier.fromNamespaceAndPath("basesteam", path),
                    JsonParser.parseString(new String(nativeBytes, StandardCharsets.UTF_8)).getAsJsonObject()) == null,
                    "Duplicate accepted loot fixture: " + path);
        }
        helper.assertTrue(result.size() == 15, "Incomplete accepted loot corpus");
        return result;
    }

    private static JsonObject roundTrip(GameTestHelper helper, DynamicOps<JsonElement> ops, JsonObject source, String label) {
        var codec = LootTable.DIRECT_CODEC;
        var encoded = codec.encodeStart(ops, codec.parse(ops, source).getOrThrow()).getOrThrow();
        helper.assertTrue(canonical(source, "").equals(canonical(encoded, "")), "Whole-table semantic loss: " + label);
        helper.assertTrue(encoded.equals(codec.encodeStart(ops, codec.parse(ops, encoded).getOrThrow()).getOrThrow()),
                "Unstable native whole-table roundtrip: " + label);
        return encoded.getAsJsonObject();
    }

    private static JsonObject nativeAltProjection(GameTestHelper helper, JsonObject template, JsonObject chest) {
        var result = template.deepCopy();
        helper.assertTrue(result.keySet().equals(Set.of("pools")) && result.getAsJsonArray("pools").size() == 1,
                "Unexpected generated ALT table structure");
        var pool = result.getAsJsonArray("pools").get(0).getAsJsonObject();
        helper.assertTrue(pool.remove("__comment").getAsString().equals("25% chance of a baseSteam item"),
                "Changed legacy ALT comment");
        var entries = pool.getAsJsonArray("entries");
        helper.assertTrue(entries.size() == 6, "Changed generated ALT entry inventory");
        helper.assertTrue(entries.get(4).getAsJsonObject().remove("entryName").getAsString().equals("basesteam:musket_plain")
                        && entries.get(5).getAsJsonObject().remove("entryName").getAsString().equals("basesteam:musket_enchanted"),
                "Changed legacy ALT entry labels");
        var acceptedPool = chest.getAsJsonArray("pools").get(0).getAsJsonObject();
        helper.assertTrue(pool.keySet().equals(Set.of("rolls", "entries"))
                        && pool.get("rolls").equals(acceptedPool.get("rolls"))
                        && canonical(entries, "").equals(canonical(acceptedPool.get("entries"), "")),
                "Generated ALT quantities, items, weights or modifiers differ from accepted Steam loot");
        return result;
    }

    private static void assertSilentLoss(GameTestHelper helper, DynamicOps<JsonElement> ops, JsonObject chest, JsonObject pipe) {
        var codec = LootTable.DIRECT_CODEC;
        var positive = roundTrip(helper, ops, chest, "production chest negative-control baseline");
        for (int index : new int[] {1, 2, 3, 5}) {
            var legacy = chest.deepCopy();
            var entry = legacy.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(index).getAsJsonObject();
            entry.add("functions", entry.remove("modifier"));
            var decoded = codec.parse(ops, legacy);
            helper.assertTrue(decoded.error().isEmpty(), "Legacy functions must expose silent whole-table loss");
            var expectedLoss = positive.deepCopy();
            expectedLoss.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries")
                    .get(index).getAsJsonObject().remove("modifier");
            helper.assertTrue(expectedLoss.equals(codec.encodeStart(ops, decoded.getOrThrow()).getOrThrow()),
                    "Legacy function loss affected an unexpected production path");
        }
        var legacy = pipe.deepCopy();
        var pool = legacy.getAsJsonArray("pools").get(0).getAsJsonObject();
        var oldConditions = new JsonArray();
        oldConditions.add(pool.remove("condition"));
        pool.add("conditions", oldConditions);
        var decoded = codec.parse(ops, legacy);
        helper.assertTrue(decoded.error().isEmpty(), "Legacy conditions must expose silent whole-table loss");
        var expectedLoss = roundTrip(helper, ops, pipe, "production block negative-control baseline").deepCopy();
        expectedLoss.getAsJsonArray("pools").get(0).getAsJsonObject().remove("condition");
        helper.assertTrue(expectedLoss.equals(codec.encodeStart(ops, decoded.getOrThrow()).getOrThrow()),
                "Legacy condition loss affected an unexpected production path");
    }

    private static JsonElement canonical(JsonElement value, String member) {
        if (member.equals("modifier") && value.isJsonArray() && value.getAsJsonArray().size() == 1)
            return canonical(value.getAsJsonArray().get(0), "");
        if (value.isJsonObject()) {
            var result = new JsonObject();
            for (var entry : value.getAsJsonObject().entrySet()) {
                String key = entry.getKey();
                var child = entry.getValue();
                if (key.equals("pools") && child.isJsonArray() && child.getAsJsonArray().isEmpty()) continue;
                if (child.isJsonPrimitive()) {
                    var primitive = child.getAsJsonPrimitive();
                    if (primitive.isNumber() && ((key.equals("weight") && primitive.getAsDouble() == 1)
                            || ((key.equals("quality") || key.equals("bonus_rolls")) && primitive.getAsDouble() == 0))) continue;
                    if (key.equals("add") && primitive.isBoolean() && !primitive.getAsBoolean()) continue;
                    if (key.equals("type") && primitive.isString()) {
                        result.add(key, new JsonPrimitive(Identifier.parse(primitive.getAsString()).toString()));
                        continue;
                    }
                }
                result.add(key, canonical(child, key));
            }
            return result;
        }
        if (value.isJsonArray()) {
            var result = new JsonArray();
            for (var child : value.getAsJsonArray()) result.add(canonical(child, ""));
            return result;
        }
        return value.deepCopy();
    }

    private static int semanticFields(JsonElement value) {
        int count = 0;
        if (value.isJsonObject()) for (var entry : value.getAsJsonObject().entrySet()) {
            if (entry.getKey().equals("modifier") || entry.getKey().equals("condition")) count++;
            count += semanticFields(entry.getValue());
        } else if (value.isJsonArray()) for (var child : value.getAsJsonArray()) count += semanticFields(child);
        return count;
    }

    private static byte[] fixture(String path) {
        try (var input = NativeLootTableProof.class.getResourceAsStream(FIXTURES + path)) {
            if (input == null) throw new IllegalStateException("Missing GameTest-only golden fixture: " + path);
            return input.readAllBytes();
        } catch (IOException exception) { throw new UncheckedIOException(exception); }
    }

    private static JsonObject readFile(Path file) {
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException exception) { throw new UncheckedIOException(exception); }
    }

    private static String sha256(byte[] bytes) {
        try { return HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (NoSuchAlgorithmException exception) { throw new AssertionError(exception); }
    }
}
