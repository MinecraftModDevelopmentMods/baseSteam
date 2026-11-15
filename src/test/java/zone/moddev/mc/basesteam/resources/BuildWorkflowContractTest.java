package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

public class BuildWorkflowContractTest {
    @Test public void publicPowerPinBindsTheMergedSourceTagAndCurseForgeRequirements() throws Exception {
        Properties pins = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("gradle.properties"))) { pins.load(reader); }
        assertEquals("c5550ce133ce0f5d3fe3e5227f021b390cdc7953", pins.getProperty("basepower_source_commit"));
        assertEquals("2.4.19.121012", pins.getProperty("basepower_source_tag"));
        assertEquals(java.util.Set.of("basepower", "base-metals", "mmdlib"),
                java.util.Set.of(pins.getProperty("cf_requirements").split(",")));
        var lock = JsonParser.parseString(Files.readString(Path.of(".github/native-dependencies.json"))).getAsJsonObject();
        assertEquals("HOSTED_INPUTS_READY", lock.get("status").getAsString());
        var power = lock.getAsJsonArray("dependencies").asList().stream()
                .map(value -> value.getAsJsonObject())
                .filter(value -> value.get("id").getAsString().equals("basepower")).findFirst().orElseThrow();
        assertEquals("PUBLISHED", power.get("state").getAsString());
        assertEquals(pins.getProperty("poweradvantage_sha256"), power.get("sha256").getAsString());
        var source = power.getAsJsonObject("source");
        assertEquals(pins.getProperty("basepower_source_commit"), source.get("commit").getAsString());
        assertEquals(pins.getProperty("basepower_source_tag"), source.get("tag").getAsString());
        assertEquals("master-1.21.1-neo", source.get("branch").getAsString());
    }

    @Test public void usesTheCoordinatedNativeNeoForgeToolchainAndSeparatePowerArtifact() throws Exception {
        Properties pins = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("gradle.properties"))) { pins.load(reader); }
        assertEquals("1.21.1", pins.getProperty("minecraft_version"));
        assertEquals("21.1.265", pins.getProperty("neo_version"));
        assertEquals("[21.1.265,21.2)", pins.getProperty("neo_version_range"));
        assertEquals("21", pins.getProperty("java_version"));
        assertEquals("2.2.20.121012", pins.getProperty("mod_version"));
        assertEquals("2.4.19.121012", pins.getProperty("poweradvantage_version"));
        assertEquals("2.0.2.121012+Neo", pins.getProperty("mmdlib_version"));
        assertEquals("4.0.1.121012+Neo", pins.getProperty("basemetals_version"));
        String build = Files.readString(Path.of("build.gradle"));
        assertTrue(build.contains("id 'net.neoforged.moddev' version '2.0.148'"));
        assertFalse(build.contains("minecraft.mavenizer"));
        assertFalse(build.contains("renamer.classes("));
        assertTrue(build.contains("basePower-${project.poweradvantage_version}.jar"));
        assertTrue(build.contains("filesMatching(archiveTextPatterns + ['**/*.cfg', '**/*.properties', '**/*.js'], normalizeArchiveLineEndings)"));
        assertTrue(build.stripTrailing().endsWith("apply from: 'gradle/reproducible-docs.gradle'"));
        assertTrue(build.contains("inputs.file('gradle/reproducible-docs.gradle')"));
        String docs = Files.readString(Path.of("gradle/reproducible-docs.gradle"));
        assertTrue(docs.contains("javadocTool.get().metadata.installationPath.asFile"));
        assertTrue(docs.contains("'legal/java.base'"));
        assertTrue(docs.contains("'element-list'"));
        assertTrue(docs.contains("['ADDITIONAL_LICENSE_INFO', 'ASSEMBLY_EXCEPTION', 'LICENSE']"));
        assertTrue(docs.contains("filter(FixCrLfFilter, eol: FixCrLfFilter.CrLf.newInstance('lf'))"));
        Properties wrapper = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("gradle/wrapper/gradle-wrapper.properties"))) { wrapper.load(reader); }
        assertTrue(wrapper.getProperty("distributionUrl").endsWith("/gradle-9.8.0-bin.zip"));
        assertEquals("bafd5ce9cfaea0fbccfdc8439a1ac42fbd4cd9c89dc9a988228d8a2639a58e6c",
                wrapper.getProperty("distributionSha256Sum"));
        assertEquals("238e777fcddd7e34f9708186085def2abd6e08e658505b38718d79d74c21abd5",
                java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                        .digest(Files.readAllBytes(Path.of("gradle/wrapper/gradle-wrapper.jar")))));
    }

    @Test public void freezesDependencyBytesAndUsesMavenBaseMetalsByDefault() throws Exception {
        Properties pins = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("gradle.properties"))) { pins.load(reader); }
        assertEquals("E9ED662E15C3510E2A48CE85448D3D39C6EF2667208F29EB281791200507F77E",
                pins.getProperty("poweradvantage_sha256"));
        assertEquals("EE417C82D588B0050A49709B6430CF836315E71E4DB2DF4036D3735454BC5311",
                pins.getProperty("mmdlib_sha256"));
        assertEquals("B4BB1CBCD54DFCF2EC8465ACE82715EDCCC5BF1E5C15E14C627AE7CC58E8FED1",
                pins.getProperty("basemetals_sha256"));
        assertNull(pins.getProperty("basemetals_local_jar"));
        String build = Files.readString(Path.of("build.gradle"));
        assertTrue(build.contains("localRuntime \"zone.moddev.mc.basemetals:BaseMetals:${project.basemetals_version}\""));
        assertFalse(build.contains("basemetals_local_jar"));
        String verification = Files.readString(Path.of("gradle/verify-native-inputs.gradle"));
        assertTrue(verification.contains("BaseMetals-${project.basemetals_version}.jar"));
        assertFalse(verification.contains("basemetals-4.0.1.121012-development.jar"));
    }

    @Test public void nativeMetadataRequiresPowerAndMmdlibButNotBaseMetals() throws Exception {
        String metadata = Files.readString(Path.of("src/main/resources/META-INF/neoforge.mods.toml"));
        assertTrue(metadata.contains("modId=\"basepower\"\ntype=\"required\""));
        assertTrue(metadata.contains("modId=\"mmdlib\"\ntype=\"required\""));
        assertTrue(metadata.contains("modId=\"basemetals\"\ntype=\"optional\""));
        assertTrue(metadata.contains("versionRange=\"[2.0.2.121012,3.0.0)\""));
        assertTrue(metadata.contains("versionRange=\"[4.0.1.121012,5.0.0)\""));
        assertFalse(Files.exists(Path.of("src/main/resources/META-INF/mods.toml")));
    }

    @Test public void hostedJobsUseNativePublicInputsAndRetainTheDispatcherArchiveLayout() throws Exception {
        String ci = Files.readString(Path.of(".github/workflows/ci.yml"));
        String codeql = Files.readString(Path.of(".github/workflows/codeql-analysis.yml"));
        for (String workflow : new String[] {ci, codeql}) {
            assertTrue(workflow.contains("master-1.21.1-neo"));
            assertTrue(workflow.contains("verify-native-dependencies.ps1 -DownloadDirectory"));
            assertTrue(workflow.contains("-I .github/pinned-native-inputs.gradle"));
            assertTrue(workflow.contains("-PpowerAdvantageJar=$POWER_JAR"));
            assertFalse(workflow.contains("Mavenizer"));
            assertFalse(workflow.contains("forge52"));
            assertFalse(workflow.contains(".ci-dependencies/"));
            assertFalse(workflow.contains("runGameTestServer"));
            assertFalse(workflow.contains("verifyEclipseEcj"));
        }
        for (String suffix : new String[] {".jar", "-sources.jar", "-javadoc.jar"}) {
            assertTrue(ci.contains("build/libs/baseSteam-${{ env.CHILD_VERSION }}" + suffix));
        }
        assertTrue(ci.contains("build/release/SHA256SUMS"));
        assertTrue(ci.contains("            CHANGELOG.txt"));
        assertTrue(codeql.contains("--rerun-tasks --no-build-cache"));
        String tag = Files.readString(Path.of(".github/workflows/release-on-tag.yml"));
        assertTrue(tag.contains("verify-native-dependencies.ps1 -ValidateOnly"));
        assertTrue(tag.contains("release_player_sha256"));
        assertTrue(tag.contains("-f head_sha=\"$GITHUB_SHA\" -f branch=\"$release_branch\""));
        String wrapper = Files.readString(Path.of(".github/workflows/validate-gradle-build.yml"));
        assertTrue(wrapper.contains("master-1.21.1-neo"));
        assertTrue(wrapper.contains("gradle/actions/wrapper-validation@"));
        assertFalse(Files.exists(Path.of("gradle/forge52-mavenizer.gradle")));
    }
}
