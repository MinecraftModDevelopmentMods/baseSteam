package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.*;

public class NativeIngredientCodecContractTest {
    private static Map<TagKey<Item>, List<Holder<Item>>> previousTags;

    @BeforeClass public static void bootstrap() throws Exception {
        NativeTestBootstrap.registerCraftingSerializers();
        try (var resources = new MultiPackResourceManager(PackType.SERVER_DATA,
                List.of(ServerPacksSource.createVanillaPackSource().fullResources()))) {
            TagLoader<Holder<Item>> reader = new TagLoader<>((id, required) -> BuiltInRegistries.ITEM
                    .get(ResourceKey.create(Registries.ITEM, id)).map(holder -> (Holder<Item>) holder), "tags/item");
            Map<TagKey<Item>, List<Holder<Item>>> loaded = new HashMap<>();
            reader.build(reader.load(resources)).forEach((id, values) -> loaded.put(ItemTags.create(id), List.copyOf(values)));
            assertTrue("Load real vanilla tag data", loaded.containsKey(ItemTags.PLANKS));
            previousTags = new HashMap<>();
            BuiltInRegistries.ITEM.freeze();
            BuiltInRegistries.ITEM.getTags().forEach(tag -> previousTags.put(tag.key(), tag.stream().toList()));
            NativeTestBootstrap.bindItemTags(loaded);
        }
    }

    @AfterClass public static void restoreTags() {
        if (previousTags != null) NativeTestBootstrap.bindItemTags(previousTags);
    }

    @Test public void nativeItemStringKeepsItsExactMembershipAndWireShape() {
        Ingredient ingredient = decode("\"minecraft:string\"");
        assertTrue(ingredient.test(new ItemStack(Items.STRING)));
        assertFalse(ingredient.test(new ItemStack(Items.STONE)));
        assertFalse(ingredient.test(ItemStack.EMPTY));
        assertEquals(JsonParser.parseString("\"minecraft:string\""), Ingredient.CODEC.encodeStart(
                NativeTestBootstrap.registries().createSerializationContext(JsonOps.INSTANCE), ingredient).getOrThrow());
    }

    @Test public void nativeTagStringUsesTheActualVanillaTagAndNotAnyItem() {
        Ingredient ingredient = decode("\"#minecraft:planks\"");
        for (Item plank : List.of(Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.CRIMSON_PLANKS))
            assertTrue(ingredient.test(new ItemStack(plank)));
        assertFalse(ingredient.test(new ItemStack(Items.STICK)));
        assertFalse(ingredient.test(new ItemStack(Items.STONE)));
        assertFalse(ingredient.test(ItemStack.EMPTY));
    }

    @Test public void nativeStringArrayMeansAlternativesWithoutAddingOtherItems() {
        Ingredient ingredient = decode("[\"minecraft:string\",\"minecraft:feather\"]");
        assertTrue(ingredient.test(new ItemStack(Items.STRING)));
        assertTrue(ingredient.test(new ItemStack(Items.FEATHER)));
        assertFalse(ingredient.test(new ItemStack(Items.STONE)));
        assertFalse(ingredient.test(ItemStack.EMPTY));
    }

    @Test public void legacyObjectsAndMalformedOrEmptySelectorsAreRejectedByTheNativeCodec() {
        for (String json : List.of("{\"item\":\"minecraft:string\"}", "{\"tag\":\"minecraft:planks\"}",
                "[{\"item\":\"minecraft:string\"}]", "{}", "[]", "null", "1", "\"#\"",
                "\"INVALID:SPACE ID\"", "\"missing_mod:unregistered_item\"")) {
            var result = Ingredient.CODEC.parse(NativeTestBootstrap.registries().createSerializationContext(JsonOps.INSTANCE),
                    JsonParser.parseString(json));
            assertTrue(json + " must produce a native codec error", result.error().isPresent());
            assertTrue(json + " cannot leave an accepted partial ingredient", result.result().isEmpty());
        }
    }

    private static Ingredient decode(String json) {
        return Ingredient.CODEC.parse(NativeTestBootstrap.registries().createSerializationContext(JsonOps.INSTANCE),
                JsonParser.parseString(json)).getOrThrow();
    }
}
