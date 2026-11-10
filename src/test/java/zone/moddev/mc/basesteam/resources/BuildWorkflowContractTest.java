package zone.moddev.mc.basesteam.resources;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

public class BuildWorkflowContractTest {
    @Test public void localPortUsesOfficialRuntimeMappingsAndRetainsExactPins() throws Exception {
        Properties pins = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("gradle.properties"))) {
            pins.load(reader);
        }
        assertEquals("1.20.6", pins.getProperty("minecraft_version"));
        assertEquals("50.2.0", pins.getProperty("forge_version"));
        assertEquals("21", pins.getProperty("java_version"));
        assertEquals("official", pins.getProperty("mapping_channel"));
        assertEquals("1.20.6", pins.getProperty("mapping_version"));
        assertEquals("2.4.13.120061", pins.getProperty("poweradvantage_version"));
        for (String key : new String[] {"poweradvantage_source_ref", "poweradvantage_deobf_sha256",
                "poweradvantage_player_sha256", "poweradvantage_test_bootstrap_sha256",
                "poweradvantage_test_bootstrap_source_sha256", "poweradvantage_test_registry_source_sha256",
                "basemetals_source_commit", "basemetals_deobf_sha256",
                "basemetals_player_sha256", "orespawn_source_commit", "orespawn_deobf_sha256",
                "orespawn_player_sha256"}) {
            assertTrue(key, pins.getProperty(key, "").matches(
                    key.endsWith("_sha256") ? "[A-F0-9]{64}" : "[a-f0-9]{40}"));
        }
        assertEquals(pins.getProperty("poweradvantage_deobf_sha256"), pins.getProperty("poweradvantage_player_sha256"));
        String build = Files.readString(Path.of("build.gradle"));
        assertTrue(build.contains("apply from: 'gradle/forge50-mavenizer.gradle'"));
        assertFalse(build.contains("renamer.classes("));
        assertFalse(build.contains("toSrgFile"));
        assertTrue(build.contains("basePower-${project.poweradvantage_version}.jar"));
    }

    @Test public void thisLocalPortDoesNotOptIntoTheHistoricalHostedJobs() throws Exception {
        for (String name : new String[] {"ci.yml", "codeql-analysis.yml", "validate-gradle-build.yml"}) {
            String source = Files.readString(Path.of(".github/workflows", name));
            assertFalse(name, source.contains("master-1.20.6"));
            assertFalse(name, source.contains("feature/basesteam-1.20.6-port"));
        }
    }
}
