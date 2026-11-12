package zone.moddev.mc.basesteam.resources;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.util.FurnaceFuelHelper;
import static org.junit.Assert.*;

public class NativeFuelContractTest {
    private static Map<TagKey<Item>, List<Holder<Item>>> previousTags;

    @BeforeClass public static void bootstrapNativeFuel() {
        NativeTestBootstrap.start();
        // Standalone tests need the actual vanilla item tags before the native fuel map.
        try (MultiPackResourceManager resources = new MultiPackResourceManager(
                PackType.SERVER_DATA, List.of(ServerPacksSource.createVanillaPackSource()))) {
            TagLoader<Holder<Item>> reader = new TagLoader<>(
                    (id, required) -> BuiltInRegistries.ITEM.get(ResourceKey.create(Registries.ITEM, id))
                            .map(holder -> (Holder<Item>)holder), "tags/item");
            Map<TagKey<Item>, List<Holder<Item>>> loaded = new HashMap<>();
            reader.build(reader.load(resources)).forEach((id, tag) -> loaded.put(ItemTags.create(id), List.copyOf(tag)));
            assertTrue(loaded.containsKey(ItemTags.NON_FLAMMABLE_WOOD));
            previousTags = new HashMap<>();
            BuiltInRegistries.ITEM.freeze();
            BuiltInRegistries.ITEM.getTags().forEach(tag ->
                    previousTags.put(tag.key(), tag.stream().toList()));
            NativeTestBootstrap.bindItemTags(loaded);
        }
    }

    @AfterClass public static void restoreNativeTags() {
        if (previousTags != null) {
            NativeTestBootstrap.bindItemTags(previousTags);
        }
    }

    @Test public void keepsExactVanillaAndEmptyFuelValues() {
        assertEquals(0, FurnaceFuelHelper.getBurnTime(ItemStack.EMPTY));
        assertEquals(0, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.CRIMSON_PLANKS)));
        assertEquals(300, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.OAK_PLANKS)));
        assertEquals(1600, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.COAL)));
        assertEquals(20000, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.LAVA_BUCKET)));
    }

    @Test public void retainsTheForgeItemBurnTimeOverride() {
        ((net.minecraft.core.MappedRegistry<Item>)BuiltInRegistries.ITEM).unfreeze();
        Item item = new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("basesteam_test", "fuel_override")))) {
            @Override public int getBurnTime(ItemStack stack, RecipeType<?> recipeType) {
                assertNull(recipeType);
                return 237;
            }
        };
        ForgeRegistries.ITEMS.register(Identifier.fromNamespaceAndPath("basesteam_test", "fuel_override"), item);
        BuiltInRegistries.ITEM.freeze();
        assertEquals(237, FurnaceFuelHelper.getBurnTime(new ItemStack(item)));
    }
}
