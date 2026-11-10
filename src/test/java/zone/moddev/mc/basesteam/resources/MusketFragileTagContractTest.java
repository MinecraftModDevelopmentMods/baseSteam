package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.TreeSet;
import org.junit.Test;
import static org.junit.Assert.*;

public class MusketFragileTagContractTest {
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
            assertFalse(blocks.contains("minecraft:tnt"));
            assertFalse(blocks.contains("minecraft:stone"));
            assertFalse(blocks.contains("minecraft:water"));
            // Oracle: the sorted vanilla block IDs matching the six fragile Material
            // values, read from the actual Forge 45.4.0 block registry.
            assertEquals(216, blocks.size());
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(String.join("\n", blocks).getBytes(StandardCharsets.UTF_8));
            assertEquals("F8C8CC9719BEEE0926FA793229D0C190A2EEEF514E0F6500CC4D3C50D61FF2BE", HexFormat.of().withUpperCase().formatHex(digest));
        }
    }
}
