package zone.moddev.mc.basesteam.migration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SteamConfigMigrationTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test public void preservesPreviousCommonSettingsAndNeverOverwritesCurrentSettings() throws Exception {
        Path dir = temp.getRoot().toPath();
        Path oldConfig = dir.resolve("steamadvantage-common.toml");
        Path newConfig = dir.resolve("basesteam-common.toml");
        String oldSettings = "[options]\nmusketDamage = 31.0\n";
        assertFalse(SteamConfigMigration.copyLegacyCommonConfig(dir));
        assertFalse(Files.exists(newConfig));
        Files.writeString(oldConfig, oldSettings);
        assertTrue(SteamConfigMigration.copyLegacyCommonConfig(dir));
        assertEquals(oldSettings, Files.readString(newConfig));
        String currentSettings = "[options]\nmusketDamage = 12.0\n";
        Files.writeString(newConfig, currentSettings);
        assertFalse(SteamConfigMigration.copyLegacyCommonConfig(dir));
        assertEquals(currentSettings, Files.readString(newConfig));
        assertEquals(oldSettings, Files.readString(oldConfig));
    }
}
