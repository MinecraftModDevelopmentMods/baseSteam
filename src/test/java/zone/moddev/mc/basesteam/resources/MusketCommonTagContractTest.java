package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.junit.Test;
import zone.moddev.mc.basesteam.items.MusketItem;
import static org.junit.Assert.*;

public class MusketCommonTagContractTest {
    @Test public void runtimeAmmoLookupMatchesTheExportedCommonCartridgeTag() throws Exception {
        assertEquals(ResourceLocation.parse("c:ammo/blackpowder"), tag("BLACKPOWDER_AMMO_TAG").location());
        var data = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/c/tags/item/ammo/blackpowder.json"))).getAsJsonObject();
        assertFalse(data.get("replace").getAsBoolean());
        assertEquals("basesteam:blackpowder_cartridge", data.getAsJsonArray("values").get(0).getAsString());
    }

    @Test public void runtimeRepairLookupUsesTheCommonSteelIngotTag() throws Exception {
        assertEquals(ResourceLocation.parse("c:ingots/steel"), tag("STEEL_INGOT_TAG").location());
    }

    private static TagKey<?> tag(String name) throws Exception {
        var field = MusketItem.class.getDeclaredField(name);
        field.setAccessible(true);
        return (TagKey<?>) field.get(null);
    }
}
