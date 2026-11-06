package zone.moddev.mc.basesteam.resources;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;

public class BuildWorkflowContractTest {
    @Test public void everyHostedBuildUsesPinnedToolchainProjectProperties() throws Exception {
        for (String name : new String[] {"ci.yml", "codeql-analysis.yml"}) {
            String source = new String(Files.readAllBytes(Paths.get(".github/workflows", name)),
                    StandardCharsets.UTF_8);
            assertTrue(name, source.contains("ref: ac1781e7cade15045ffe0135077087b4d1722603"));
            assertTrue(name, source.contains("BaseMetals-3.0.2.115021-deobf.jar"));
            int powerPin = source.indexOf("s/^poweradvantage_source_ref=//p");
            int powerCheckout = source.indexOf("repository: MinecraftModDevelopmentMods/basePower");
            assertTrue(name + " must read the pin before checkout", powerPin >= 0 && powerPin < powerCheckout);
            assertTrue(name, source.contains("^[a-f0-9]{40}$"));
            assertTrue(name, source.contains("ref: ${{ steps.power-pin.outputs.ref }}"));
            assertTrue(name, source.contains("basePower-2.4.7.115021-deobf.jar"));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.paths="));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.auto-detect=false"));
            assertTrue(name, source.contains("-Porg.gradle.java.installations.auto-download=false"));
            assertFalse(name, source.contains("-Dorg.gradle.java.installations."));
            for (String line : source.split("\\R")) {
                if (line.contains("./gradlew ") && !line.contains("chmod")) {
                    assertTrue(name + ": " + line, line.contains("./gradlew $MMD_GRADLE_TOOLCHAINS "));
                }
            }
        }
    }
}
