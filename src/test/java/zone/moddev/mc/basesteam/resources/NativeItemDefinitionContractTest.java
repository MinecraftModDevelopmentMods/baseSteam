package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.conditional.ComponentMatches;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.*;

public class NativeItemDefinitionContractTest {
    private static final Path ROOT = Path.of("src/main/resources/assets/basesteam/items");

    @BeforeClass public static void bootstrap() {
        NativeTestBootstrap.start();
        ItemModels.bootstrap();
        ConditionalItemModelProperties.bootstrap();
    }

    @Test public void allTwentyOneDefinitionsDecodeWithTheNativeClientCodec() throws Exception {
        try (var files = Files.list(ROOT)) {
            var definitions = files.filter(path -> path.toString().endsWith(".json")).toList();
            assertEquals(21, definitions.size());
            for (Path path : definitions) {
                ClientItem item = ClientItem.CODEC.parse(JsonOps.INSTANCE,
                        JsonParser.parseString(Files.readString(path))).getOrThrow();
                assertNotNull(path.toString(), item.model());
            }
        }
    }

    @Test public void loadedPredicateUsesTheOriginalByteAndNeverChangesCustomData() throws Exception {
        JsonObject model = JsonParser.parseString(Files.readString(ROOT.resolve("musket.json")))
                .getAsJsonObject().getAsJsonObject("model");
        assertEquals("basesteam:item/musket_loaded", model.getAsJsonObject("on_true").get("model").getAsString());
        var loading = model.getAsJsonObject("on_false");
        assertEquals("minecraft:using_item", loading.get("property").getAsString());
        assertEquals("basesteam:item/musket_loading", loading.getAsJsonObject("on_true").get("model").getAsString());
        assertEquals("basesteam:item/musket_unloaded", loading.getAsJsonObject("on_false").get("model").getAsString());
        var predicate = ComponentMatches.MAP_CODEC.codec().parse(JsonOps.INSTANCE, model).getOrThrow();
        ItemStack stack = new ItemStack(Items.STICK);
        assertFalse(predicate.get(stack, null, null, 0, ItemDisplayContext.GUI));
        for (byte state : new byte[] {0, 1, 2, -1, 127}) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                tag.putByte("loaded", state);
                tag.putString("owner", "preserved");
            });
            CustomData before = stack.get(DataComponents.CUSTOM_DATA);
            assertEquals(state == 1, predicate.get(stack, null, null, 0, ItemDisplayContext.GUI));
            assertSame(before, stack.get(DataComponents.CUSTOM_DATA));
            assertEquals("preserved", before.copyTag().getStringOr("owner", ""));
        }
    }
}
