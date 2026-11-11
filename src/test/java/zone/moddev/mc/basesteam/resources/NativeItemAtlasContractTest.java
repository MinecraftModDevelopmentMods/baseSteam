package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.*;

public class NativeItemAtlasContractTest {
    @BeforeClass public static void bootstrap() {
        NativeTestBootstrap.start();
        SpriteSources.bootstrap();
    }

    @Test public void pinnedNativeItemAtlasFindsEveryLegacySteamModelTextureAcrossNamespaces() throws Exception {
        Properties pins = new Properties();
        try (var input = Files.newInputStream(Path.of("gradle.properties"))) { pins.load(input); }
        Path power = Path.of("../../PowerAdvantage/build/libs/basePower-"
                + pins.getProperty("poweradvantage_version") + ".jar");
        assertEquals(pins.getProperty("poweradvantage_deobf_sha256"), HexFormat.of().withUpperCase()
                .formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(power))));
        var steam = new PathPackResources.PathResourcesSupplier(Path.of("src/main/resources"));
        var parent = new FilePackResources.FileResourcesSupplier(power);
        Identifier atlas = Identifier.withDefaultNamespace("atlases/items.json");
        assertEquals(Identifier.withDefaultNamespace("items"), AtlasIds.ITEMS);
        try (var resources = new MultiPackResourceManager(PackType.CLIENT_RESOURCES,
                List.of(parent.openPrimary(info("basepower")), steam.openPrimary(info("basesteam"))))) {
            var definitions = resources.getResourceStack(atlas);
            assertFalse("The native item atlas must be supplied by the exact Power candidate", definitions.isEmpty());
            Map<Identifier, SpriteSource.DiscardableLoader> found = new HashMap<>();
            SpriteSource.Output output = new SpriteSource.Output() {
                @Override public void add(Identifier id, SpriteSource.DiscardableLoader loader) {
                    var replaced = found.put(id, loader);
                    if (replaced != null) replaced.discard();
                }
                @Override public void removeAll(java.util.function.Predicate<Identifier> filter) {
                    found.entrySet().removeIf(entry -> {
                        if (!filter.test(entry.getKey())) return false;
                        entry.getValue().discard();
                        return true;
                    });
                }
            };
            for (var definition : definitions) {
                try (var reader = definition.openAsReader()) {
                    var sources = SpriteSources.FILE_CODEC.parse(
                            new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseReader(reader))).getOrThrow();
                    sources.forEach(source -> source.run(resources, output));
                }
            }
            try {
                Set<Identifier> expected = textures();
                assertFalse("Inspect the actual Steam model texture declarations", expected.isEmpty());
                assertTrue("The loaded musket retains its original barrel-tip material texture",
                        expected.contains(Identifier.parse("basesteam:materials/steel_dark_hole")));
                assertTrue("The shared Power textures must also be covered", expected.stream()
                        .anyMatch(id -> id.getNamespace().equals("basepower")));
                Set<Identifier> missing = new HashSet<>(expected);
                missing.removeAll(found.keySet());
                assertTrue("Textures absent from the actual native item-atlas sources: " + missing, missing.isEmpty());
                assertFalse(found.containsKey(Identifier.parse("basesteam:missing_atlas_negative_control")));
                for (Identifier id : expected) {
                    var file = id.withPrefix("textures/").withSuffix(".png");
                    try (var input = resources.getResourceOrThrow(file).open()) {
                        assertArrayEquals(id.toString(), new byte[] {(byte)137, 80, 78, 71, 13, 10, 26, 10}, input.readNBytes(8));
                    }
                }
            } finally {
                found.values().forEach(SpriteSource.DiscardableLoader::discard);
            }
        }
        try (var resources = new MultiPackResourceManager(PackType.CLIENT_RESOURCES,
                List.of(steam.openPrimary(info("basesteam"))))) {
            assertEquals("Steam contributes only its drill sprite", 1, resources.getResourceStack(atlas).size());
            assertTrue("Shared materials cannot exist without the required Power pack", resources.getResource(
                    Identifier.parse("basepower:textures/materials/wood.png")).isEmpty());
            assertEquals("Without Power, the native loader offers only the drill sprite and missing-texture fallback", 2,
                    SpriteSourceList.load(resources, AtlasIds.ITEMS).list(resources).size());
        }
    }

    private static Set<Identifier> textures() throws IOException {
        Set<Identifier> result = new HashSet<>();
        try (var files = Files.walk(Path.of("src/main/resources/assets/basesteam/models"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                var model = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                if (!model.has("textures")) continue;
                for (var texture : model.getAsJsonObject("textures").entrySet()) {
                    String id = texture.getValue().getAsString();
                    if (!id.startsWith("#")) result.add(Identifier.parse(id));
                }
            }
        }
        return result;
    }

    private static PackLocationInfo info(String mod) {
        return new PackLocationInfo("mod:" + mod, Component.literal(mod), PackSource.DEFAULT, Optional.empty());
    }
}
