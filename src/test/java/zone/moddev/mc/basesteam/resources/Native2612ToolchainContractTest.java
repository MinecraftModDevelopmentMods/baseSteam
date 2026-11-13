package zone.moddev.mc.basesteam.resources;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native2612ToolchainContractTest {
    @Test public void targetUsesExactJava25AndForge65() throws Exception {
        Properties pins = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("gradle.properties"))) {
            pins.load(reader);
        }
        assertEquals("26.2", pins.getProperty("minecraft_version"));
        assertEquals("[26.2]", pins.getProperty("minecraft_version_range"));
        assertEquals("65.1.0", pins.getProperty("forge_version"));
        assertEquals("[65.1.0,66)", pins.getProperty("forge_version_range"));
        assertEquals("[65,66)", pins.getProperty("loader_version_range"));
        assertEquals("20260616.103818", pins.getProperty("mcp_version"));
        assertEquals("2.2.18.2602001", pins.getProperty("mod_version"));
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
        assertTrue(tool.contains("5ABAB52A3F1BE115381C3CE80D5FD38DBE6C9F4C0B9ECF70A957700EC90FB88B"));
        assertTrue(tool.contains("9437E03FC5E1CB5D605D72F0538C4DF4417B64DB0A57D0AABDC5184781517AC1"));
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
