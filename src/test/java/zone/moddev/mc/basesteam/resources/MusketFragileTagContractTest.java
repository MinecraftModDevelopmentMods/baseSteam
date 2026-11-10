package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.TreeSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.*;

public class MusketFragileTagContractTest {
    @BeforeClass public static void bootstrap() { NativeTestBootstrap.start(); }

    @Test public void vanillaDefaultsMatchThePreviousMaterialRule() throws Exception {
        Path path = Path.of("src/main/resources/data/basesteam/tags/blocks/musket_fragile.json");
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            var tag = JsonParser.parseReader(reader).getAsJsonObject();
            assertFalse("Other packs must be able to extend the fragile block list", tag.get("replace").getAsBoolean());
            var blocks = new TreeSet<String>();
            tag.getAsJsonArray("values").forEach(value ->
                    assertTrue("Duplicate fragile block " + value, blocks.add(value.getAsString())));
            assertTrue(blocks.contains("minecraft:glass_pane"));
            assertTrue(blocks.contains("minecraft:oak_leaves"));
            assertTrue(blocks.contains("minecraft:torch"));
            assertTrue(blocks.contains("minecraft:short_grass"));
            for (String id : blocks) {
                assertTrue("Unregistered Forge 50 block would invalidate the tag: " + id,
                        BuiltInRegistries.BLOCK.containsKey(ResourceLocation.parse(id)));
            }
            assertFalse(blocks.contains("minecraft:tnt"));
            assertFalse(blocks.contains("minecraft:stone"));
            assertFalse(blocks.contains("minecraft:water"));
            // Oracle: the sorted vanilla block IDs matching the six fragile Material
            // values, read from Forge 45.4.0. Normalize the one known vanilla rename.
            assertEquals(216, blocks.size());
            blocks.remove("minecraft:short_grass");
            blocks.add("minecraft:grass");
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(String.join("\n", blocks).getBytes(StandardCharsets.UTF_8));
            assertEquals("F8C8CC9719BEEE0926FA793229D0C190A2EEEF514E0F6500CC4D3C50D61FF2BE", HexFormat.of().withUpperCase().formatHex(digest));
        }
    }
}
