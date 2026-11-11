package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.init.Enchantments;
import zone.moddev.mc.basesteam.init.Items;
import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.*;

public class NativeEnchantmentContractTest {
    private static final List<ResourceKey<Enchantment>> KEYS = List.of(
            Enchantments.high_explosive, Enchantments.powderless, Enchantments.rapid_reload, Enchantments.recoil);
    private static RegistryAccess registries;
    private static Map<TagKey<Item>, List<Holder<Item>>> previousTags;

    @BeforeClass public static void decodeActualDataIntoNativeRegistryHolders() throws Exception {
        NativeTestBootstrap.start();
        Items.init();
        Items.allItems.forEach((id, item) -> {
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath("basesteam", id);
            if (!ForgeRegistries.ITEMS.containsKey(key)) ForgeRegistries.ITEMS.register(key, item);
            assertSame(item, ForgeRegistries.ITEMS.getValue(key));
        });
        previousTags = new HashMap<>();
        BuiltInRegistries.ITEM.getTags().forEach(pair ->
                previousTags.put(pair.getFirst(), pair.getSecond().stream().toList()));
        var tags = new HashMap<>(previousTags);
        tags.put(ItemTags.create(ResourceLocation.parse("basesteam:muskets")),
                List.of(Items.blackpowder_musket.builtInRegistryHolder()));
        BuiltInRegistries.ITEM.bindTags(tags);
        var enchantments = new MappedRegistry<>(Registries.ENCHANTMENT, Lifecycle.stable());
        registries = new RegistryAccess.ImmutableRegistryAccess(Stream.concat(
                NativeTestBootstrap.registries().registries(),
                Stream.of(new RegistryAccess.RegistryEntry<>(Registries.ENCHANTMENT, enchantments))));
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        for (var key : KEYS) {
            try (var reader = Files.newBufferedReader(Path.of(
                    "src/main/resources/data/basesteam/enchantment", key.location().getPath() + ".json"))) {
                Enchantment value = Enchantment.DIRECT_CODEC.parse(ops, JsonParser.parseReader(reader)).getOrThrow();
                enchantments.register(key, value, RegistrationInfo.BUILT_IN);
            }
        }
        enchantments.freeze();
    }

    @AfterClass public static void restoreTags() {
        if (previousTags != null) BuiltInRegistries.ITEM.bindTags(previousTags);
    }

    @Test public void nativeDefinitionsRetainEveryLegacyCostLevelWeightAndSlot() {
        int[][] specs = {{5, 5, 7, 7, 17, 7, 2}, {1, 1, 20, 0, 50, 0, 8},
                {2, 1, 15, 0, 50, 0, 4}, {10, 5, 2, 5, 12, 5, 1}};
        for (int index = 0; index < KEYS.size(); index++) {
            var key = KEYS.get(index);
            Enchantment value = holder(key).value();
            int[] spec = specs[index];
            assertEquals(spec[0], value.getWeight());
            assertEquals(spec[1], value.getMaxLevel());
            assertEquals(spec[6], value.getAnvilCost());
            for (int level = 1; level <= spec[1]; level++) {
                assertEquals(spec[2] + spec[3] * (level - 1), value.getMinCost(level));
                assertEquals(spec[4] + spec[5] * (level - 1), value.getMaxCost(level));
            }
            assertTrue(value.isPrimaryItem(new ItemStack(Items.blackpowder_musket)));
            assertFalse(value.isSupportedItem(new ItemStack(net.minecraft.world.item.Items.STONE)));
            for (EquipmentSlot slot : EquipmentSlot.values())
                assertEquals(slot == EquipmentSlot.MAINHAND, value.matchingSlot(slot));
            assertTrue(value.effects().isEmpty());
            for (var other : KEYS)
                assertEquals(!key.equals(other), Enchantment.areCompatible(holder(key), holder(other)));
        }
    }

    @Test public void allFourActualHoldersSurviveNativeSaveAndEveryLoadedStateChange() {
        ItemStack stack = new ItemStack(Items.blackpowder_musket);
        stack.setDamageValue(123);
        for (var key : KEYS) stack.enchant(holder(key), holder(key).value().getMaxLevel());
        var original = stack.get(DataComponents.ENCHANTMENTS);
        zone.moddev.mc.basesteam.items.MusketItem.startLoad(stack);
        zone.moddev.mc.basesteam.items.MusketItem.finishLoad(stack);
        zone.moddev.mc.basesteam.items.MusketItem.unload(stack);
        ItemStack restored = ItemStack.parse(registries, stack.save(registries)).orElseThrow();
        assertEquals(original, restored.get(DataComponents.ENCHANTMENTS));
        assertEquals(123, restored.getDamageValue());
        for (var key : KEYS)
            assertEquals(holder(key).value().getMaxLevel(), Enchantments.level(key, restored, registries));
    }

    @Test public void absentDataRegistryAndUnknownKeysFailInsteadOfSilentlyDroppingEffects() {
        var stack = new ItemStack(Items.blackpowder_musket);
        assertThrows(IllegalStateException.class,
                () -> Enchantments.level(Enchantments.recoil, stack, RegistryAccess.EMPTY));
        var missing = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("basesteam:missing"));
        assertThrows(IllegalStateException.class, () -> Enchantments.level(missing, stack, registries));
    }

    private static Holder.Reference<Enchantment> holder(ResourceKey<Enchantment> key) {
        return registries.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
    }
}
