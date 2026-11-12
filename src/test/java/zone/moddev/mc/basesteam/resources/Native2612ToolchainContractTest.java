package zone.moddev.mc.basesteam.resources;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native2612ToolchainContractTest {
    @Test public void targetUsesExactJava25AndRecommendedForge64() throws Exception {
        Properties pins = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("gradle.properties"))) {
            pins.load(reader);
        }
        assertEquals("26.1.2", pins.getProperty("minecraft_version"));
        assertEquals("[26.1.2]", pins.getProperty("minecraft_version_range"));
        assertEquals("64.1.5", pins.getProperty("forge_version"));
        assertEquals("[64.1.5,65)", pins.getProperty("forge_version_range"));
        assertEquals("[64,65)", pins.getProperty("loader_version_range"));
        assertEquals("20260409.101008", pins.getProperty("mcp_version"));
        assertEquals("2.2.17.2601021", pins.getProperty("mod_version"));
        assertEquals("25", pins.getProperty("java_version"));
        assertEquals("25", pins.getProperty("gradle_java_version"));
        assertEquals("25.0.3+9", pins.getProperty("java_toolchain_version"));
        assertEquals("false", pins.getProperty("reobf"));
        String build = Files.readString(Path.of("build.gradle"));
        assertTrue(build.contains("fgtools.configure('slimelauncher')"));
        assertTrue(build.contains("JavaVersion.VERSION_25"));
        assertFalse(build.contains("JavaLanguageVersion.of(21)"));
        assertFalse(build.contains("JavaLanguageVersion.of(8)"));
    }

    @Test public void targetUsesSealedNativeMavenizerWithoutForge61Repairs() throws Exception {
        String build = Files.readString(Path.of("build.gradle"));
        assertTrue(build.contains("apply from: 'gradle/forge64-mavenizer.gradle'"));
        String tool = Files.readString(Path.of("gradle/forge64-mavenizer.gradle"));
        assertTrue(tool.contains("BB0DACB3DEB2A711CA67DD402D5FDAB5A9847C37C7B9B596DEC7CF37378FA085"));
        assertTrue(tool.contains("21D1F5984E735BCEA1B1EFB9F965655796B39538FC91AAED1C3474004710423D"));
        assertTrue(tool.contains("JavaLanguageVersion.of(25)"));
        assertFalse(tool.contains("B36CA046"));
        assertFalse(tool.contains("88F2EE4C"));
    }

    @Test public void nativeNumberProviderFallbackPreservesBoundsButRejectsMalformedMaps() {
        zone.moddev.mc.basesteam.testing.NativeTestBootstrap.start();
        var codec = net.minecraft.world.level.storage.loot.providers.number.NumberProviders.CODEC;
        var ops = com.mojang.serialization.JsonOps.INSTANCE;
        var original = com.google.gson.JsonParser.parseString(
                "{\"type\":\"minecraft:uniform\",\"min\":15,\"max\":25}").getAsJsonObject();
        var fallback = original.deepCopy();
        fallback.addProperty("type", "missing_mod:unregistered_number_provider");
        var decoded = codec.parse(ops, fallback);
        assertTrue(decoded.error().isEmpty());
        assertTrue(decoded.result().isPresent());
        assertEquals(original, codec.encodeStart(ops, decoded.getOrThrow()).getOrThrow());
        for (String invalid : new String[]{
                "{\"type\":\"missing_mod:unregistered_number_provider\"}",
                "{\"type\":\"missing_mod:unregistered_number_provider\",\"min\":\"invalid\",\"max\":25}",
                "{\"type\":\"missing_mod:unregistered_number_provider\",\"min\":15,\"max\":[]}"}) {
            var rejected = codec.parse(ops, com.google.gson.JsonParser.parseString(invalid));
            assertTrue(invalid, rejected.error().isPresent());
            assertTrue(invalid, rejected.result().isEmpty());
        }
    }
}
