package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
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
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.client.resources.model.sprite.Material;
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
        try (var resources = new MultiPackResourceManager(PackType.CLIENT_RESOURCES,
                List.of(parent.openPrimary(info("basepower")), steam.openPrimary(info("basesteam"))))) {
            Set<Identifier> registered = new HashSet<>();
            Set<Identifier> blockSprites = sprites(resources, AtlasIds.BLOCKS, registered);
            Set<Identifier> itemSprites = sprites(resources, AtlasIds.ITEMS, registered);
            Set<Identifier> blockTextures = textures(Path.of("src/main/resources/assets/basesteam/models/block"));
            assertFalse("The block-model texture oracle must not be empty", blockTextures.isEmpty());
            Set<Identifier> collisions = new HashSet<>(blockSprites);
            collisions.retainAll(itemSprites);
            assertTrue("Native sprite lookup must be unambiguous across both atlases: " + collisions,
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
            for (Identifier id : expected) {
                assertEquals("Native Material retains the exact sprite without an atlas field", id,
                        Material.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString('"' + id.toString() + '"'))
                                .getOrThrow().sprite());
            }
            Set<Identifier> itemIcons = expected.stream().filter(id -> id.getPath().startsWith("items/"))
                    .collect(java.util.stream.Collectors.toSet());
            assertFalse("The legacy generated-item icon oracle must not be empty", itemIcons.isEmpty());
            assertTrue("Native item lookup uses ITEMS", itemSprites.containsAll(itemIcons));
            assertTrue("Native item sprites must not also be registered in BLOCKS",
                    java.util.Collections.disjoint(blockSprites, itemIcons));
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
            assertEquals("Steam contributes one explicit item-atlas definition", 1,
                    resources.getResourceStack(Identifier.withDefaultNamespace("atlases/items.json")).size());
            assertTrue("Shared materials cannot exist without the required Power pack", resources.getResource(
                    Identifier.parse("basepower:textures/materials/wood.png")).isEmpty());
            assertEquals("Without Power, the native item loader offers three icons plus missing-texture fallback", 4,
                    SpriteSourceList.load(resources, AtlasIds.ITEMS).list(resources).size());
            Set<Identifier> registered = new HashSet<>();
            Set<Identifier> available = new HashSet<>(sprites(resources, AtlasIds.BLOCKS, registered));
            available.addAll(sprites(resources, AtlasIds.ITEMS, registered));
            Set<Identifier> missing = textures(Path.of("src/main/resources/assets/basesteam/models"));
            missing.removeAll(available);
            assertTrue("Omitting Power must still expose the missing shared material",
                    missing.contains(Identifier.parse("basepower:materials/wood")));
        }
    }

    @Test public void directoriesForeignNamespacesAndAliasesAreRejectedBeforeNativeLoading() {
        for (String source : List.of(
                "{\"type\":\"minecraft:directory\",\"source\":\"items\",\"prefix\":\"items/\"}",
                "{\"type\":\"minecraft:single\",\"resource\":\"basepower:items/steam_governor\"}",
                "{\"type\":\"single\",\"resource\":\"basesteam:items/steam_governor\"}",
                "{\"type\":\"minecraft:single\",\"resource\":\"basesteam:items/steam_governor\",\"sprite\":\"basesteam:items/alias\"}",
                "{\"type\":\"minecraft:single\",\"resource\":\"basesteam:items/steam_governor\",\"unknown\":true}")) {
            assertThrows(source, AssertionError.class, () -> requireOwnedSingles(
                    atlas(source), "basesteam", true, new HashSet<>(), id -> true));
        }
        assertTrue("The native codec accepts directories, so the ownership contract is an additional required guard",
                SpriteSources.FILE_CODEC.parse(new Dynamic<>(JsonOps.INSTANCE,
                        atlas("{\"type\":\"minecraft:directory\",\"source\":\"items\",\"prefix\":\"items/\"}")))
                        .getOrThrow().getFirst() instanceof DirectoryLister);
    }

    @Test public void missingDuplicateAndCrossAtlasSpritesAreRejected() {
        String source = "{\"type\":\"minecraft:single\",\"resource\":\"basesteam:items/steam_governor\"}";
        assertThrows(AssertionError.class, () -> requireOwnedSingles(
                atlas(source), "basesteam", true, new HashSet<>(), id -> false));
        assertThrows(AssertionError.class, () -> requireOwnedSingles(
                atlas(source + "," + source), "basesteam", true, new HashSet<>(), id -> true));
        Set<Identifier> registered = new HashSet<>();
        requireOwnedSingles(atlas(source), "basesteam", false, registered, id -> true);
        assertThrows(AssertionError.class, () -> requireOwnedSingles(
                atlas(source), "basesteam", true, registered, id -> true));
        for (String id : List.of("basesteam:blocks/steam_pipe", "basesteam:materials/iron", "basesteam:entity/drill_bit")) {
            assertThrows(id, AssertionError.class, () -> requireOwnedSingles(atlas(
                    "{\"type\":\"minecraft:single\",\"resource\":\"" + id + "\"}"),
                    "basesteam", true, new HashSet<>(), value -> true));
        }
    }

    @Test public void malformedNativeAtlasAndMaterialTypesAreRejected() {
        for (String json : List.of("{}", "{\"sources\":[]}", "{\"sources\":{}}",
                "{\"sources\":[],\"extra\":1}", "{\"sources\":[null]}",
                "{\"sources\":[{\"type\":\"minecraft:single\",\"resource\":3}]}")) {
            assertThrows(json, AssertionError.class, () -> requireOwnedSingles(
                    JsonParser.parseString(json).getAsJsonObject(), "basesteam", true, new HashSet<>(), id -> true));
        }
        assertTrue(Material.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"sprite\":3}")).error().isPresent());
        assertTrue(Material.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"sprite\":\"basesteam:materials/iron\",\"force_translucent\":\"bad\"}"))
                .error().isPresent());
        Material material = Material.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(
                "{\"sprite\":\"basesteam:materials/iron\",\"force_translucent\":true}" )).getOrThrow();
        assertEquals(Identifier.parse("basesteam:materials/iron"), material.sprite());
        assertTrue(material.forceTranslucent());
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

    private static Set<Identifier> sprites(ResourceManager resources, Identifier atlas, Set<Identifier> registered)
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
                    String owner = definition.sourcePackId().replace("mod:", "");
                    assertTrue("Only the two verified mod packs may supply these definitions",
                            Set.of("basepower", "basesteam").contains(owner));
                    var sources = requireOwnedSingles(JsonParser.parseReader(reader).getAsJsonObject(), owner,
                            atlas.equals(AtlasIds.ITEMS), registered, id -> resources.getResource(
                                    id.withPrefix("textures/").withSuffix(".png")).isPresent());
                    assertEquals("Exact current native atlas inventory", owner.equals("basesteam")
                            ? (atlas.equals(AtlasIds.ITEMS) ? 3 : 24) : (atlas.equals(AtlasIds.ITEMS) ? 6 : 34), sources.size());
                    for (SpriteSource source : sources) {
                        source.run(resources, output);
                    }
                }
            }
            return new HashSet<>(found.keySet());
        } finally {
            found.values().forEach(SpriteSource.DiscardableLoader::discard);
        }
    }

    private static List<SpriteSource> requireOwnedSingles(JsonObject atlas, String owner, boolean items,
            Set<Identifier> registered, java.util.function.Predicate<Identifier> exists) {
        assertEquals(Set.of("sources"), atlas.keySet());
        assertTrue(atlas.get("sources").isJsonArray());
        assertFalse(atlas.getAsJsonArray("sources").isEmpty());
        for (var value : atlas.getAsJsonArray("sources")) {
            assertTrue(value.isJsonObject());
            var source = value.getAsJsonObject();
            assertEquals(Set.of("type", "resource"), source.keySet());
            assertTrue(source.get("type").isJsonPrimitive() && source.getAsJsonPrimitive("type").isString());
            assertEquals("minecraft:single", source.get("type").getAsString());
            assertTrue(source.get("resource").isJsonPrimitive() && source.getAsJsonPrimitive("resource").isString());
            Identifier id = Identifier.parse(source.get("resource").getAsString());
            assertEquals("Each pack must own every sprite it registers", owner, id.getNamespace());
            assertFalse(id.getPath().contains(".."));
            if (items) assertTrue("Only item sprites belong in ITEMS", id.getPath().startsWith("items/"));
            assertTrue("Duplicate sprite within/across atlases: " + id, registered.add(id));
            assertTrue("Missing physical texture " + id, exists.test(id));
        }
        var sources = SpriteSources.FILE_CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, atlas)).getOrThrow();
        for (SpriteSource source : sources) {
            assertTrue(source instanceof SingleFile);
            assertTrue(((SingleFile)source).spriteId().isEmpty());
        }
        return sources;
    }

    private static JsonObject atlas(String sources) {
        return JsonParser.parseString("{\"sources\":[" + sources + "]}").getAsJsonObject();
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
