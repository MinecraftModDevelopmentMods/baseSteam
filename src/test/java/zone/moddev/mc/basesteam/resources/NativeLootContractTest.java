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

    private static JsonObject read(Path path) throws Exception {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        }
    }
}
