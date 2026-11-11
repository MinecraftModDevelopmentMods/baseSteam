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
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.*;

public class NativeItemAtlasContractTest {
    @BeforeClass public static void bootstrap() {
        NativeTestBootstrap.start();
        SpriteSources.bootstrap();
    }

    @Test public void pinnedNativeAtlasesResolveEveryModelWithoutBlockMaterialCollisions() throws Exception {
        Properties pins = new Properties();
        try (var input = Files.newInputStream(Path.of("gradle.properties"))) { pins.load(input); }
        Path power = pinnedPower(pins, System.getProperty("basesteam.nativePowerJar"));
        var steam = new PathPackResources.PathResourcesSupplier(Path.of("src/main/resources"));
        var parent = new FilePackResources.FileResourcesSupplier(power);
        assertEquals(Identifier.withDefaultNamespace("items"), AtlasIds.ITEMS);
        for (String directory : List.of("blocks", "materials")) {
            assertThrows("Block/material directories must never be accepted by the item atlas", AssertionError.class,
                    () -> requireItemsOnly(List.of(new DirectoryLister(directory, directory + "/"))));
        }
        assertThrows("Even an items source must retain its exact sprite prefix", AssertionError.class,
                () -> requireItemsOnly(List.of(new DirectoryLister("items", ""))));
        try (var resources = new MultiPackResourceManager(PackType.CLIENT_RESOURCES,
                List.of(parent.openPrimary(info("basepower")), steam.openPrimary(info("basesteam"))))) {
            Set<String> blockDirectories = new HashSet<>();
            Set<String> itemDirectories = new HashSet<>();
            Set<Identifier> blockSprites = sprites(resources, AtlasIds.BLOCKS, blockDirectories);
            Set<Identifier> itemSprites = sprites(resources, AtlasIds.ITEMS, itemDirectories);
            Set<Identifier> blockTextures = textures(Path.of("src/main/resources/assets/basesteam/models/block"));
            assertFalse("The block-model texture oracle must not be empty", blockTextures.isEmpty());
            Set<String> duplicatedDirectories = new HashSet<>(blockDirectories);
            duplicatedDirectories.retainAll(itemDirectories);
            duplicatedDirectories.retainAll(Set.of("blocks", "materials"));
            assertTrue("ITEMS must not shadow block/material directories: " + duplicatedDirectories,
                    duplicatedDirectories.isEmpty());
            Set<Identifier> collisions = new HashSet<>(blockSprites);
            collisions.retainAll(itemSprites);
            collisions.retainAll(blockTextures);
            assertTrue("block_or_item must not resolve world-block textures through ITEMS: " + collisions,
                    collisions.isEmpty());
            Set<Identifier> expected = textures(Path.of("src/main/resources/assets/basesteam/models"));
            assertFalse("Inspect the actual Steam model texture declarations", expected.isEmpty());
            assertTrue("The loaded musket retains its original barrel-tip material texture",
                    expected.contains(Identifier.parse("basesteam:materials/steel_dark_hole")));
            assertTrue("The shared Power textures must also be covered", expected.stream()
                    .anyMatch(id -> id.getNamespace().equals("basepower")));
            Set<Identifier> allSprites = new HashSet<>(blockSprites);
            allSprites.addAll(itemSprites);
            Set<Identifier> missing = new HashSet<>(expected);
            missing.removeAll(allSprites);
            assertTrue("Textures absent from both actual native atlas sources: " + missing, missing.isEmpty());
            assertTrue("BlockSimpleModelWrapper requires the block atlas", blockSprites.containsAll(blockTextures));
            assertTrue("Block model materials must not resolve through ITEMS",
                    java.util.Collections.disjoint(blockTextures, itemSprites));
            Set<Identifier> itemIcons = expected.stream().filter(id -> id.getPath().startsWith("items/"))
                    .collect(java.util.stream.Collectors.toSet());
            assertFalse("The legacy generated-item icon oracle must not be empty", itemIcons.isEmpty());
            assertTrue("Native item lookup uses ITEMS", itemSprites.containsAll(itemIcons));
            assertTrue("Legacy generated-item lookup retains BLOCKS compatibility", blockSprites.containsAll(itemIcons));
            assertTrue(blockSprites.contains(Identifier.parse("basesteam:entity/drill_bit")));
            assertFalse(itemSprites.contains(Identifier.parse("basesteam:entity/drill_bit")));
            assertFalse(allSprites.contains(Identifier.parse("basesteam:missing_atlas_negative_control")));
            for (Identifier id : expected) {
                var file = id.withPrefix("textures/").withSuffix(".png");
                try (var input = resources.getResourceOrThrow(file).open()) {
                    assertArrayEquals(id.toString(), new byte[] {(byte)137, 80, 78, 71, 13, 10, 26, 10}, input.readNBytes(8));
                }
            }
        }
        try (var resources = new MultiPackResourceManager(PackType.CLIENT_RESOURCES,
                List.of(steam.openPrimary(info("basesteam"))))) {
            assertEquals("Steam contributes one item-directory definition", 1,
                    resources.getResourceStack(Identifier.withDefaultNamespace("atlases/items.json")).size());
            assertTrue("Shared materials cannot exist without the required Power pack", resources.getResource(
                    Identifier.parse("basepower:textures/materials/wood.png")).isEmpty());
            assertEquals("Without Power, the native item loader offers three icons plus missing-texture fallback", 4,
                    SpriteSourceList.load(resources, AtlasIds.ITEMS).list(resources).size());
            Set<Identifier> available = new HashSet<>(sprites(resources, AtlasIds.BLOCKS, new HashSet<>()));
            available.addAll(sprites(resources, AtlasIds.ITEMS, new HashSet<>()));
            Set<Identifier> missing = textures(Path.of("src/main/resources/assets/basesteam/models"));
            missing.removeAll(available);
            assertTrue("Omitting Power must still expose the missing shared material",
                    missing.contains(Identifier.parse("basesteam:materials/steel_dark_hole")));
        }
    }

    @Test public void missingVerifiedPowerPathIsRejected() {
        assertThrows(AssertionError.class, () -> pinnedPower(new Properties(), null));
    }

    @Test public void wrongPowerBytesAreRejectedEvenInAPathWithSpaces() throws Exception {
        Path directory = Files.createTempDirectory("baseSteam dependency with spaces ");
        Path wrong = directory.resolve("basePower.jar");
        try {
            Files.write(wrong, new byte[] {1, 2, 3});
            Properties pins = new Properties();
            pins.setProperty("poweradvantage_deobf_sha256", "0".repeat(64));
            assertThrows(AssertionError.class, () -> pinnedPower(pins, wrong.toString()));
        } finally {
            Files.deleteIfExists(wrong);
            Files.deleteIfExists(directory);
        }
    }

    private static Path pinnedPower(Properties pins, String configured) throws Exception {
        assertNotNull("Gradle must supply its verified Power dependency to the atlas test", configured);
        Path power = Path.of(configured).toAbsolutePath().normalize();
        assertTrue("Missing verified Power dependency: " + power, Files.isRegularFile(power));
        assertEquals(pins.getProperty("poweradvantage_deobf_sha256"), HexFormat.of().withUpperCase()
                .formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(power))));
        return power;
    }

    private static Set<Identifier> sprites(ResourceManager resources, Identifier atlas, Set<String> directories)
            throws Exception {
        var definitions = resources.getResourceStack(atlas.withPrefix("atlases/").withSuffix(".json"));
        assertFalse("Missing native atlas " + atlas, definitions.isEmpty());
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
        try {
            for (var definition : definitions) {
                try (var reader = definition.openAsReader()) {
                    var sources = SpriteSources.FILE_CODEC.parse(
                            new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseReader(reader))).getOrThrow();
                    if (atlas.equals(AtlasIds.ITEMS)) requireItemsOnly(sources);
                    for (SpriteSource source : sources) {
                        if (source instanceof DirectoryLister directory) {
                            assertEquals(directory.sourcePath() + "/", directory.idPrefix());
                            directories.add(directory.sourcePath());
                        }
                        source.run(resources, output);
                    }
                }
            }
            return new HashSet<>(found.keySet());
        } finally {
            found.values().forEach(SpriteSource.DiscardableLoader::discard);
        }
    }

    private static void requireItemsOnly(List<SpriteSource> sources) {
        assertEquals("Only the native items directory belongs in this atlas", 1, sources.size());
        assertTrue("Single block/material sprites must not shadow the block atlas", sources.getFirst() instanceof DirectoryLister);
        DirectoryLister source = (DirectoryLister)sources.getFirst();
        assertEquals("items", source.sourcePath());
        assertEquals("items/", source.idPrefix());
    }

    private static Set<Identifier> textures(Path root) throws IOException {
        Set<Identifier> result = new HashSet<>();
        try (var files = Files.walk(root)) {
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
