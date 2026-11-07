package zone.moddev.mc.basesteam.resources;

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
        for (String key : new String[] {"poweradvantage_source_ref", "poweradvantage_deobf_sha256",
                "basemetals_source_commit", "basemetals_deobf_sha256"}) {
            assertTrue(key, pins.getProperty(key, "").matches(
                    key.endsWith("_sha256") ? "[A-F0-9]{64}" : "[a-f0-9]{40}"));
        }
        for (String name : new String[] {"ci.yml", "codeql-analysis.yml"}) {
            String source = new String(Files.readAllBytes(Paths.get(".github/workflows", name)),
                    StandardCharsets.UTF_8);
            assertTrue(name, source.contains("ref: ${{ env.BASEMETALS_SOURCE_REF }}"));
            assertTrue(name, source.contains("BaseMetals-" + pins.getProperty("basemetals_version") + "-deobf.jar"));
            assertTrue(name, source.contains("repository: " + pins.getProperty("poweradvantage_source_repository")));
            assertTrue(name, source.contains("basePower-" + pins.getProperty("poweradvantage_version") + "-deobf.jar"));
            assertTrue(name, source.contains("ref: b56e2b62a1a6ea4d1dd96f8646a87801a817dbdf"));
            assertTrue(name, source.contains("ref: ${{ env.POWERADVANTAGE_SOURCE_REF }}"));
            assertTrue(name, source.contains("The exact basePower 1.17 dependency pin is pending."));
            assertTrue(name, source.contains("The exact Base Metals 1.17 dependency pin is pending."));
            assertTrue(name, source.contains("^[a-f0-9]{40}$"));
            assertTrue(name, source.contains("^[A-F0-9]{64}$"));
            int pinCheck = source.indexOf("Validate coordinated dependency pins");
            assertTrue(name, pinCheck >= 0 && pinCheck < source.indexOf("Check out pinned Base Metals")
                    && pinCheck < source.indexOf("Check out basePower 1.17.1"));
            assertFalse(name, source.contains("-Ppoweradvantage_source_ref="));
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
