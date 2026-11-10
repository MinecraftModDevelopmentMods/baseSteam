package zone.moddev.mc.basesteam.resources;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

public class BuildWorkflowContractTest {
    @Test public void everyHostedBuildUsesPinnedToolchainProjectProperties() throws Exception {
        assertTrue(Files.isRegularFile(Paths.get("gradle/forge47-mavenizer.gradle")));
        java.util.Properties pins = new java.util.Properties();
        try (java.io.Reader reader = Files.newBufferedReader(Paths.get("gradle.properties"), StandardCharsets.UTF_8)) {
            pins.load(reader);
        }
        for (String key : new String[] {"poweradvantage_source_ref", "poweradvantage_deobf_sha256",
                "poweradvantage_player_sha256", "poweradvantage_test_bootstrap_sha256",
                "poweradvantage_test_bootstrap_source_sha256", "basemetals_source_commit", "basemetals_deobf_sha256",
                "basemetals_player_sha256", "orespawn_source_commit", "orespawn_deobf_sha256",
                "orespawn_player_sha256"}) {
            assertTrue(key, pins.getProperty(key, "").matches(
                    key.endsWith("_sha256") ? "[A-F0-9]{64}" : "[a-f0-9]{40}"));
        }
        for (String name : new String[] {"ci.yml", "codeql-analysis.yml"}) {
            String source = new String(Files.readAllBytes(Paths.get(".github/workflows", name)),
                    StandardCharsets.UTF_8);
            assertTrue(name, source.contains("ref: ${{ env.BASEMETALS_SOURCE_REF }}"));
            assertTrue(name, source.contains("sed -n 's/^basemetals_version=//p'"));
            assertTrue(name, source.contains("BaseMetals-$bm_version-deobf.jar"));
            assertTrue(name, source.contains("repository: " + pins.getProperty("poweradvantage_source_repository")));
            assertTrue(name, source.contains("basePower-" + pins.getProperty("poweradvantage_version") + "-deobf.jar"));
            assertTrue(name, source.contains("ref: ${{ env.ORESPAWN_SOURCE_REF }}"));
            assertTrue(name, source.contains("ref: ${{ env.POWERADVANTAGE_SOURCE_REF }}"));
            assertTrue(name, source.contains("The exact basePower 1.20.1 dependency pin is pending."));
            assertTrue(name, source.contains("The exact Base Metals 1.20.1 dependency pin is pending."));
            assertTrue(name, source.contains("The exact OreSpawn 1.20.1 dependency pin is pending."));
            assertTrue(name, source.contains("^[a-f0-9]{40}$"));
            assertTrue(name, source.contains("^[A-F0-9]{64}$"));
            int pinCheck = source.indexOf("Validate coordinated dependency pins");
            assertTrue(name, pinCheck >= 0 && pinCheck < source.indexOf("Check out pinned Base Metals")
                    && pinCheck < source.indexOf("Check out basePower 1.20.1"));
            assertFalse(name, source.contains("-Ppoweradvantage_source_ref="));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.paths="));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.auto-detect=false"));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.auto-download=false"));
            assertFalse(name, source.contains("-Dorg.gradle.java.installations."));
            assertTrue(name, source.contains("jar unitBootstrapJar verifyReleaseDependencies"));
            assertTrue(name, source.contains("-PforgeCapabilityBootstrapJar=${{ steps.dependencies.outputs.bootstrap }}"));
            assertTrue(name, source.contains("mavenizer=\"$orespawn/ci-fixtures/tools/minecraft-mavenizer-0.5.21-orespawn-compat.jar\""));
            assertTrue(name, source.contains("416DCA4797BC55826F0DDCF03FDC405F1990FC15C2133FA27A1F143BF7976177"));
            int toolCheck = source.indexOf("| sha256sum -c -");
            assertTrue(name, toolCheck >= 0 && toolCheck < source.indexOf("(cd \"$basemetals\""));
            assertTrue(name, source.contains(
                    "(cd \"$basemetals\" && ./gradlew $MMD_GRADLE_TOOLCHAINS jar --no-daemon --stacktrace)"));
            String toolArg = "\"-Pforge47MavenizerJar=$mavenizer\"";
            int powerStart = source.indexOf("(cd \"$power\"");
            int powerEnd = source.indexOf("\n          powerjar=", powerStart);
            assertTrue(name, powerStart >= 0 && powerEnd > powerStart);
            String powerBuild = source.substring(powerStart, powerEnd);
            assertTrue(name, powerBuild.contains(toolArg));
            assertFalse(name, powerBuild.contains("MavenizerScript"));
            assertEquals(name, 1L, source.lines().filter(line -> line.contains(toolArg)).count());
            assertTrue(name, source.contains("\"-Pforge47MavenizerJar=${{ steps.dependencies.outputs.mavenizer }}\""));
            for (String line : source.split("\\R")) {
                if (line.contains("./gradlew ") && !line.contains("chmod")) {
                    assertTrue(name + ": " + line, line.contains("./gradlew $MMD_GRADLE_TOOLCHAINS "));
                }
            }
        }
    }
}
