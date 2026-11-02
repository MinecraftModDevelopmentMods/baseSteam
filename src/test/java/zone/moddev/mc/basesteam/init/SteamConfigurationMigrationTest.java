package zone.moddev.mc.basesteam.init;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.init.Bootstrap;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import zone.moddev.mc.basepower.compat.LegacyIdentity;

import static org.junit.Assert.*;

public class SteamConfigurationMigrationTest {
    @Rule public final TemporaryFolder temporary = new TemporaryFolder();
    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Test public void configImportPreservesOptionsAndNeverOverwritesEitherAuthority() throws Exception {
        Path config = temporary.newFolder().toPath();
        Path old = config.resolve("steamadvantage.cfg"), current = config.resolve("basesteam.cfg");
        byte[] original = "options {\r\n I:musket_reload_time=173\r\n S:fluid_fuel_values=oil=8123;customfuel=9001\r\n}"
                .getBytes(StandardCharsets.UTF_8);
        Files.write(old, original);
        assertEquals(current.toFile(), LegacyIdentity.migrateConfiguration(current.toFile(), "steamadvantage"));
        assertArrayEquals(original, Files.readAllBytes(old));
        assertArrayEquals(original, Files.readAllBytes(current));
        byte[] edited = "current administrator configuration".getBytes(StandardCharsets.UTF_8);
        Files.write(current, edited);
        LegacyIdentity.migrateConfiguration(current.toFile(), "steamadvantage");
        assertArrayEquals(edited, Files.readAllBytes(current));
        assertArrayEquals(original, Files.readAllBytes(old));
    }

    @Test public void originalLootImportsCustomNumbersAndRetainsExternalAndEnchantmentIdentities() throws Exception {
        Path config = temporary.newFolder().toPath();
        Path old = config.resolve("additional-loot-tables/steamadvantage/chests/custom.json");
        Files.createDirectories(old.getParent());
        byte[] original;
        try (InputStream fixture = getClass().getResourceAsStream("/migration/steamadvantage-loot.json")) {
            assertNotNull(fixture);
            Files.copy(fixture, old);
            original = Files.readAllBytes(old);
        }
        LegacyIdentity.migrateAdditionalLootTables(config, "steamadvantage", "basesteam");
        Path current = config.resolve("additional-loot-tables/basesteam/chests/custom.json");
        JsonObject document = new JsonParser().parse(new String(Files.readAllBytes(current), StandardCharsets.UTF_8))
                .getAsJsonObject();
        JsonObject pool = document.getAsJsonArray("pools").get(0).getAsJsonObject();
        assertEquals(3, pool.get("rolls").getAsInt());
        JsonArray entries = pool.getAsJsonArray("entries");
        assertEquals("basesteam:musket", entries.get(0).getAsJsonObject().get("name").getAsString());
        assertEquals(19, entries.get(0).getAsJsonObject().get("weight").getAsInt());
        assertEquals("steamadvantage:rapid_reload", entries.get(0).getAsJsonObject()
                .getAsJsonArray("functions").get(0).getAsJsonObject().getAsJsonArray("enchantments").get(0).getAsString());
        assertEquals("basesteam:steam_governor", entries.get(1).getAsJsonObject().get("name").getAsString());
        assertEquals("basemetals:steel_ingot", entries.get(2).getAsJsonObject().get("name").getAsString());
        assertEquals("orespawn:external_fixture", entries.get(3).getAsJsonObject().get("name").getAsString());
        assertEquals("othersteamadvantage:musket", entries.get(4).getAsJsonObject().get("name").getAsString());
        assertFalse("Only the canonical loot file remains active", Files.exists(old));
        assertArrayEquals(original, Files.readAllBytes(old.resolveSibling("custom.json.before-basesteam")));
        byte[] imported = Files.readAllBytes(current);
        LegacyIdentity.migrateAdditionalLootTables(config, "steamadvantage", "basesteam");
        assertArrayEquals(imported, Files.readAllBytes(current));
    }

    @Test public void existingCanonicalLootWinsAndDefaultWriterDoesNotClobberIt() throws Exception {
        Path config = temporary.newFolder().toPath();
        Path old = config.resolve("additional-loot-tables/steamadvantage/chests/simple_dungeon.json");
        Path current = config.resolve("additional-loot-tables/basesteam/chests/simple_dungeon.json");
        Files.createDirectories(old.getParent()); Files.createDirectories(current.getParent());
        byte[] legacy = "{\"name\":\"steamadvantage:musket\",\"weight\":29}".getBytes(StandardCharsets.UTF_8);
        byte[] authoritative = "{\"pools\":[],\"custom\":47}".getBytes(StandardCharsets.UTF_8);
        Files.write(old, legacy); Files.write(current, authoritative);
        LegacyIdentity.migrateAdditionalLootTables(config, "steamadvantage", "basesteam");
        TreasureChests.writeLootFile(current, "must not replace administrator rules");
        assertArrayEquals(authoritative, Files.readAllBytes(current));
        assertFalse(Files.exists(old));
        assertArrayEquals(legacy, Files.readAllBytes(old.resolveSibling("simple_dungeon.json.before-basesteam")));
    }

    @Test public void absentLootDefaultsAreWrittenOnlyOnce() throws Exception {
        Path file = temporary.newFolder().toPath().resolve("basesteam/chests/new.json");
        TreasureChests.writeLootFile(file, "{\"name\":\"basesteam:steam_pipe\"}");
        byte[] first = Files.readAllBytes(file);
        TreasureChests.writeLootFile(file, "different defaults");
        assertArrayEquals(first, Files.readAllBytes(file));
    }

    @Test public void invalidLegacyJsonStopsBeforeDisablingTheOriginalFile() throws Exception {
        Path config = temporary.newFolder().toPath();
        Path old = config.resolve("additional-loot-tables/steamadvantage/chests/broken.json");
        Files.createDirectories(old.getParent());
        byte[] original = "{\"pools\":".getBytes(StandardCharsets.UTF_8);
        Files.write(old, original);
        assertThrows(IllegalStateException.class,
                () -> LegacyIdentity.migrateAdditionalLootTables(config, "steamadvantage", "basesteam"));
        assertArrayEquals(original, Files.readAllBytes(old));
        assertFalse(Files.exists(config.resolve("additional-loot-tables/basesteam/chests/broken.json")));
    }
}
