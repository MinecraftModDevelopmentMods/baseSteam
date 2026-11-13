package zone.moddev.mc.basesteam.resources;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native2612ToolchainContractTest {
    @Test public void targetUsesExactJava25AndForge66() throws Exception {
        Properties pins = new Properties();
        try (var reader = Files.newBufferedReader(Path.of("gradle.properties"))) {
            pins.load(reader);
        }
        assertEquals("26.3", pins.getProperty("minecraft_version"));
        assertEquals("[26.3]", pins.getProperty("minecraft_version_range"));
        assertEquals("66.0.0", pins.getProperty("forge_version"));
        assertEquals("[66.0.0,67)", pins.getProperty("forge_version_range"));
        assertEquals("[66,67)", pins.getProperty("loader_version_range"));
        assertEquals("20260918.230105", pins.getProperty("mcp_version"));
        assertEquals("2.2.19.2603001", pins.getProperty("mod_version"));
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
        assertTrue(tool.contains("4150926F33A696B2088AB2675C1C3B45745CE4F9F2DEB31CCD88C9ADC834062F"));
        assertTrue(tool.contains("829400D74ECB1BF0CC6107D9980B8328365E3F56D9A5342002D8DF340571BDB7"));
        assertTrue(tool.contains("JavaLanguageVersion.of(25)"));
        assertFalse(tool.contains("B36CA046"));
        assertFalse(tool.contains("88F2EE4C"));
    }

    @Test public void nativeContextIntProviderPreservesBoundsAndRejectsUnknownOrMalformedMaps() throws Exception {
        zone.moddev.mc.basesteam.testing.NativeTestBootstrap.start();
        var codec = net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.CODEC;
        var world = zone.moddev.mc.basesteam.testing.NativeFuelWorld.create();
        var ops = world.getServer().reloadableRegistries().lookup()
                .createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        var original = com.google.gson.JsonParser.parseString(
                "{\"type\":\"minecraft:uniform\",\"min\":15,\"max\":25}").getAsJsonObject();
        var decoded = codec.parse(ops, original);
        assertTrue(decoded.error().isEmpty());
        assertTrue(decoded.result().isPresent());
        assertEquals(original, codec.encodeStart(ops, decoded.getOrThrow()).getOrThrow());
        for (String invalid : new String[]{
                "{\"type\":\"missing_mod:unregistered_number_provider\",\"min\":15,\"max\":25}",
                "{\"type\":\"missing_mod:unregistered_number_provider\"}",
                "{\"type\":\"missing_mod:unregistered_number_provider\",\"min\":\"invalid\",\"max\":25}",
                "{\"type\":\"missing_mod:unregistered_number_provider\",\"min\":15,\"max\":[]}"}) {
            var rejected = codec.parse(ops, com.google.gson.JsonParser.parseString(invalid));
            assertTrue(invalid, rejected.error().isPresent());
            assertTrue(invalid, rejected.result().isEmpty());
        }
    }
}
