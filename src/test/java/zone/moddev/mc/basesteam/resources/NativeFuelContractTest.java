package zone.moddev.mc.basesteam.resources;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagContainer;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.StaticTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraftforge.common.ForgeHooks;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.util.FurnaceFuelHelper;
import static org.junit.Assert.*;

public class NativeFuelContractTest {
    @BeforeClass public static void bootstrapNativeFuel() {
        net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap();
        // Standalone tests need the actual vanilla item tags before the native fuel map.
        try (SimpleReloadableResourceManager resources =
                     new SimpleReloadableResourceManager(PackType.SERVER_DATA)) {
            resources.add(new VanillaPackResources(null, "minecraft"));
            TagLoader<Item> reader =
                    new TagLoader<>(Registry.ITEM::getOptional, "tags/items");
            TagCollection<Item> items = reader.loadAndBuild(resources);
            assertTrue(items.getAllTags().containsKey(new ResourceLocation("minecraft:non_flammable_wood")));
            TagContainer current = SerializationTags.getInstance();
            TagContainer loaded = new TagContainer.Builder()
                    .add(Registry.ITEM_REGISTRY, items).build();
            StaticTags.resetAll(loaded);
            SerializationTags.bind(loaded);
        }
        ForgeHooks.updateBurns();
    }

    @Test public void keepsExactVanillaAndEmptyFuelValues() {
        assertEquals(0, FurnaceFuelHelper.getBurnTime(ItemStack.EMPTY));
        assertEquals(1600, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.COAL)));
        assertEquals(20000, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.LAVA_BUCKET)));
    }

    @Test public void retainsTheForgeItemBurnTimeOverride() {
        Item item = new Item(new Item.Properties()) {
            @Override public int getBurnTime(ItemStack stack, RecipeType<?> recipeType) {
                assertNull(recipeType);
                return 237;
            }
        };
        assertEquals(237, FurnaceFuelHelper.getBurnTime(new ItemStack(item)));
    }
}
