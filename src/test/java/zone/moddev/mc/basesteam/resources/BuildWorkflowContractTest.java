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
        assertEquals("26.3", pins.getProperty("minecraft_version"));
        assertEquals("66.0.0", pins.getProperty("forge_version"));
        assertEquals("25", pins.getProperty("java_version"));
        assertEquals("official", pins.getProperty("mapping_channel"));
        assertEquals("26.3", pins.getProperty("mapping_version"));
        assertEquals("2.4.18.2603001", pins.getProperty("poweradvantage_version"));
        assertEquals("c0eb83f7ba91cf8c83caea75e63057407a345949", pins.getProperty("poweradvantage_test_source_ref"));
        for (String key : new String[] {"poweradvantage_source_ref", "poweradvantage_test_source_ref", "poweradvantage_deobf_sha256",
                "poweradvantage_player_sha256", "poweradvantage_test_bootstrap_sha256",
                "poweradvantage_test_bootstrap_source_sha256", "poweradvantage_test_registry_source_sha256",
                "poweradvantage_test_loader_source_sha256",
                "basemetals_source_commit", "basemetals_deobf_sha256",
                "basemetals_player_sha256", "orespawn_source_commit", "orespawn_deobf_sha256",
                "orespawn_player_sha256"}) {
            assertTrue(key, pins.getProperty(key, "").matches(
                    key.endsWith("_sha256") ? "[A-F0-9]{64}" : "[a-f0-9]{40}"));
        }
        assertEquals(pins.getProperty("poweradvantage_deobf_sha256"), pins.getProperty("poweradvantage_player_sha256"));
        String build = Files.readString(Path.of("build.gradle"));
        assertTrue(build.contains("apply from: 'gradle/forge64-mavenizer.gradle'"));
        assertFalse(build.contains("renamer.classes("));
        assertFalse(build.contains("toSrgFile"));
        assertTrue(build.contains("basePower-${project.poweradvantage_version}.jar"));
        assertTrue(build.contains("plugins.hasPlugin('net.minecraftforge.accesstransformers')"));
        assertFalse(Files.exists(Path.of("src/main/resources/META-INF/accesstransformer.cfg")));
    }

    @Test public void hostedJobsCheckPublishedNativeInputsBeforeCompiling() throws Exception {
        for (String name : new String[] {"ci.yml", "codeql-analysis.yml"}) {
            String source = Files.readString(Path.of(".github/workflows", name));
            int pinCheck = source.indexOf("bash .github/verify-published-pins.sh");
            String checkout = name.equals("ci.yml") ? "Check out pinned basePower unit-support source"
                    : "Check out released basePower source";
            assertTrue(name, pinCheck > 0 && pinCheck < source.indexOf(checkout));
            assertTrue(name, source.contains(name.equals("ci.yml") ? "outputs.power_test_ref" : "outputs.power_ref"));
            assertTrue(name, source.contains("branches: [master-26.3"));
            assertTrue(name, source.contains("java-version: '25.0.3+9.0.LTS'"));
            assertFalse(name, source.contains("JAVA_HOME_8_X64"));
            assertFalse(name, source.contains("java-version: '21"));
            assertTrue(name, source.contains("-Pforge64MavenizerJar=$MAVENIZER"));
            assertTrue(name, source.contains("-PpowerAdvantageDeobfJar=$POWER_PLAYER"));
            assertFalse(name, source.contains("forge47"));
        }
        String script = Files.readString(Path.of(".github/verify-published-pins.sh"));
        assertTrue(script.contains("$(value poweradvantage_public_file_id)"));
        assertTrue(script.contains("$(value poweradvantage_test_source_ref)"));
        assertTrue(script.contains("require_merged basePower \"$power_test_ref\""));
        assertTrue(script.contains("$(value basemetals_public_file_id)"));
        assertTrue(script.contains("$(value orespawn_public_file_id)"));
        assertTrue(script.contains("sha256sum -c -"));
    }

    @Test public void sourceAndPackageAuditsEnforceRequiredBaseMetalsWithoutAnOptionalLaunchScope() throws Exception {
        String build = Files.readString(Path.of("build.gradle"));
        assertEquals(2, build.lines().filter(line -> line.contains("verifyRequiredBaseMetalsMetadata(metadata)")).count());
        assertTrue(build.contains("Base Metals must be declared exactly once as REQUIRED on BOTH sides"));
        String readme = Files.readString(Path.of("README.md"));
        assertTrue(readme.contains("Base Metals is REQUIRED"));
        assertFalse(readme.contains("-WithoutBaseMetals"));
        String smoke = Files.readString(Path.of("tools/run-packaged-server-smoke.ps1"));
        int reject = smoke.indexOf("if ($WithoutBaseMetals)");
        assertTrue(reject >= 0 && reject < smoke.indexOf("$project ="));
        assertTrue(smoke.contains("a no-Base-Metals runtime is not supported"));
    }
}
