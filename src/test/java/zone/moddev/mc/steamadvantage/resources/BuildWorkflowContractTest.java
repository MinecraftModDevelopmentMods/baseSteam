package zone.moddev.mc.steamadvantage.resources;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

public class BuildWorkflowContractTest {
    @Test public void everyHostedBuildUsesPinnedToolchainProjectProperties() throws Exception {
        java.util.Properties pins = new java.util.Properties();
        try (java.io.Reader reader = Files.newBufferedReader(Paths.get("gradle.properties"), StandardCharsets.UTF_8)) {
            pins.load(reader);
        }
        String powerSource = pins.getProperty("poweradvantage_source_ref");
        String powerHash = pins.getProperty("poweradvantage_deobf_sha256");
        if ("API_PIN_PROVISIONAL".equals(System.getProperty("steamadvantage.apiPinMode"))) {
            assertEquals("PENDING_PARENT_POWER_COMMIT", powerSource);
            assertEquals("5D0B363F38A44FCEDC829DF3CC33CC5AD3D8595A2341B256C6F3FDA7BED7EBD6", powerHash);
        } else {
            assertEquals("fc4c47f2ed8cfebbaa010e6a0e807137a3329f95", powerSource);
            assertEquals("BDF486E16C1FE3B4CE2FE1ED78670AABA594063BCD2D4251265124B17FA9E767", powerHash);
        }
        assertTrue("Coordinated native API hash is required", powerHash.matches("[A-F0-9]{64}"));
        String properties = new String(Files.readAllBytes(Paths.get("gradle.properties")),
                StandardCharsets.UTF_8);
        assertTrue(properties.contains("poweradvantage_source_ref=" + powerSource));
        assertTrue(properties.contains("poweradvantage_deobf_sha256=" + powerHash));
        for (String name : new String[] {"build.gradle", "README.md"}) {
            String source = new String(Files.readAllBytes(Paths.get(name)), StandardCharsets.UTF_8);
            assertTrue(name, source.contains(powerSource));
            assertTrue(name, source.contains(powerHash));
        }
        for (String name : new String[] {"ci.yml", "codeql-analysis.yml"}) {
            String source = new String(Files.readAllBytes(Paths.get(".github/workflows", name)),
                    StandardCharsets.UTF_8);
            assertTrue(name, source.contains("ref: dacfaa3af2e3310d5cb6b081da79f09260ae103d"));
            assertTrue(name, source.contains("BaseMetals-3.1.0.117011-deobf.jar"));
            assertTrue(name, source.contains("ref: b56e2b62a1a6ea4d1dd96f8646a87801a817dbdf"));
            assertTrue(name, source.contains("ref: ${{ env.POWERADVANTAGE_SOURCE_REF }}"));
            assertTrue(name, source.contains("Final Power 1.17 dependency pin is pending."));
            assertTrue(name, source.contains("^[a-f0-9]{40}$"));
            assertTrue(name, source.contains("^[A-F0-9]{64}$"));
            assertTrue(name, source.indexOf("Validate final Power source pin")
                    < source.indexOf("Check out PowerAdvantage 1.17.1"));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.paths="));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.auto-detect=false"));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.auto-download=false"));
            assertFalse(name, source.contains("-Dorg.gradle.java.installations."));
            assertTrue(name, source.contains("jar unitBootstrapJar verifyReleaseDependencies"));
            assertTrue(name, source.contains("-PforgeCapabilityBootstrapJar=${{ steps.dependencies.outputs.bootstrap }}"));
            for (String line : source.split("\\R")) {
                if (line.contains("./gradlew ") && !line.contains("chmod")) {
                    assertTrue(name + ": " + line, line.contains("./gradlew $MMD_GRADLE_TOOLCHAINS "));
                }
            }
        }
    }
}
