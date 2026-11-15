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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.BaseMappedRegistry;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.neoforged.fml.ModList;
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
    private static Map<ResourceKey<Item>, FurnaceFuel> previousFuels;

    @BeforeClass public static void bootstrapNativeFuel() throws Exception {
        NativeTestBootstrap.start();
        // Standalone tests need the actual vanilla item tags before the native fuel map.
        try (MultiPackResourceManager resources = new MultiPackResourceManager(
                PackType.SERVER_DATA, List.of(ServerPacksSource.createVanillaPackSource()))) {
            TagLoader<Holder<Item>> reader = new TagLoader<>(
                    id -> BuiltInRegistries.ITEM.getHolder(ResourceKey.create(Registries.ITEM, id)), "tags/item");
            Map<TagKey<Item>, List<Holder<Item>>> loaded = new HashMap<>();
            reader.loadAndBuild(resources).forEach((id, tag) -> loaded.put(ItemTags.create(id), List.copyOf(tag)));
            assertTrue(loaded.containsKey(ItemTags.NON_FLAMMABLE_WOOD));
            previousTags = new HashMap<>();
            BuiltInRegistries.ITEM.getTags().forEach(pair ->
                    previousTags.put(pair.getFirst(), pair.getSecond().stream().toList()));
            BuiltInRegistries.ITEM.bindTags(loaded);
        }
        previousFuels = BuiltInRegistries.ITEM.getDataMap(NeoForgeDataMaps.FURNACE_FUELS);
        Map<ResourceKey<Item>, FurnaceFuel> fuels = new HashMap<>();
        var fuelResource = ModList.get().getModFileById("neoforge").getFile()
                .findResource("data", "neoforge", "data_maps", "item", "furnace_fuels.json");
        try (var stream = Files.newInputStream(fuelResource)) {
            var data = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            data.getAsJsonObject("values").entrySet().forEach(entry -> {
                FurnaceFuel fuel = FurnaceFuel.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow();
                if (entry.getKey().startsWith("#")) {
                    var tag = ItemTags.create(ResourceLocation.parse(entry.getKey().substring(1)));
                    BuiltInRegistries.ITEM.getTag(tag).ifPresent(holders -> holders.forEach(holder -> fuels.put(holder.unwrapKey().orElseThrow(), fuel)));
                } else fuels.put(ResourceKey.create(Registries.ITEM, ResourceLocation.parse(entry.getKey())), fuel);
            });
            data.getAsJsonArray("remove").forEach(entry -> {
                String id = entry.getAsString();
                if (id.startsWith("#")) BuiltInRegistries.ITEM.getTag(ItemTags.create(ResourceLocation.parse(id.substring(1))))
                        .ifPresent(holders -> holders.forEach(holder -> fuels.remove(holder.unwrapKey().orElseThrow())));
                else fuels.remove(ResourceKey.create(Registries.ITEM, ResourceLocation.parse(id)));
            });
        }
        setFuelData(fuels);
    }

    @AfterClass public static void restoreNativeTags() throws Exception {
        if (previousTags != null) {
            BuiltInRegistries.ITEM.bindTags(previousTags);
            setFuelData(previousFuels);
        }
    }

    @Test public void keepsExactVanillaAndEmptyFuelValues() {
        assertEquals(0, FurnaceFuelHelper.getBurnTime(ItemStack.EMPTY));
        assertEquals(0, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.CRIMSON_PLANKS)));
        assertEquals(300, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.OAK_PLANKS)));
        assertEquals(1600, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.COAL)));
        assertEquals(20000, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.LAVA_BUCKET)));
    }

    @Test public void retainsTheNeoForgeItemBurnTimeOverride() {
        Item item = new Item(new Item.Properties()) {
            @Override public int getBurnTime(ItemStack stack, RecipeType<?> recipeType) {
                assertNull(recipeType);
                return 237;
            }
        };
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath("basesteam_test", "fuel_override"), item);
        assertEquals(237, FurnaceFuelHelper.getBurnTime(new ItemStack(item)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setFuelData(Map<ResourceKey<Item>, FurnaceFuel> values) throws Exception {
        var field = BaseMappedRegistry.class.getDeclaredField("dataMaps");
        field.setAccessible(true);
        ((Map) field.get(BuiltInRegistries.ITEM)).put(NeoForgeDataMaps.FURNACE_FUELS, values);
    }
}
