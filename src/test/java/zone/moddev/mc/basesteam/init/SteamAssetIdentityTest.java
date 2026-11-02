package zone.moddev.mc.basesteam.init;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import zone.moddev.mc.basesteam.SteamAdvantage;

import static org.junit.Assert.*;

public class SteamAssetIdentityTest {
    @Test public void everyRenamedModelAndTextureReferenceResolves() throws Exception {
        Path root = Paths.get("src/main/resources/assets/basesteam");
        assertTrue(Files.isDirectory(root));
        assertFalse("The old active asset tree must not remain", Files.exists(root.resolveSibling("steamadvantage")));
        List<Path> documents;
        try (Stream<Path> walk = Files.walk(root)) {
            documents = walk.filter(path -> path.toString().endsWith(".json")).collect(Collectors.toList());
        }
        assertTrue("Check the real asset set, not an empty directory", documents.size() > 50);
        for (Path file : documents) {
            String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            assertFalse(file.toString(), source.contains("steamadvantage:"));
            assertFalse(file.toString(), source.contains("poweradvantage:"));
            checkReferences(new JsonParser().parse(source), file.toString());
        }
    }

    @Test public void sourceMetadataAndEnglishNamesUseTheNewIdentity() throws Exception {
        JsonObject mod = new JsonParser().parse(new String(Files.readAllBytes(Paths.get("src/main/resources/mcmod.info")),
                StandardCharsets.UTF_8)).getAsJsonArray().get(0).getAsJsonObject();
        assertEquals("basesteam", mod.get("modid").getAsString());
        assertEquals("baseSteam", mod.get("name").getAsString());
        assertEquals(SteamAdvantage.MODID, mod.get("modid").getAsString());
        String english = new String(Files.readAllBytes(Paths.get("src/main/resources/assets/basesteam/lang/en_US.lang")),
                StandardCharsets.UTF_8);
        assertTrue(english.contains("tile.basesteam.steam_drill.name="));
        assertTrue(english.contains("item.basesteam.musket.name="));
        assertFalse(english.contains("tile.steamadvantage."));
        assertFalse(english.contains("item.steamadvantage."));
    }

    private void checkReferences(JsonElement value, String source) throws Exception {
        if (value.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                JsonElement child = entry.getValue();
                if (entry.getKey().equals("textures") && child.isJsonObject()) {
                    for (Map.Entry<String, JsonElement> texture : child.getAsJsonObject().entrySet()) {
                        if (texture.getValue().isJsonPrimitive()) {
                            requireAsset(texture.getValue().getAsString(), "textures/", ".png", source);
                        }
                    }
                } else if ((entry.getKey().equals("parent") || entry.getKey().equals("model"))
                        && child.isJsonPrimitive()) {
                    String reference = child.getAsString();
                    int separator = reference.indexOf(':');
                    if (separator > 0) {
                        String name = reference.substring(separator + 1);
                        if (!name.startsWith("block/") && !name.startsWith("item/")) {
                            reference = reference.substring(0, separator + 1) + "block/" + name;
                        }
                        requireAsset(reference, "models/", ".json", source);
                    }
                }
                checkReferences(child, source);
            }
        } else if (value.isJsonArray()) {
            for (JsonElement child : value.getAsJsonArray()) checkReferences(child, source);
        }
    }

    private void requireAsset(String reference, String folder, String suffix, String source) throws Exception {
        int separator = reference.indexOf(':');
        if (separator < 0) return;
        String namespace = reference.substring(0, separator);
        if (!namespace.equals("basesteam") && !namespace.equals("basepower")) return;
        String path = "/assets/" + namespace + "/" + folder + reference.substring(separator + 1) + suffix;
        try (InputStream asset = getClass().getResourceAsStream(path)) {
            assertNotNull(source + " references missing " + path, asset);
        }
    }
}
