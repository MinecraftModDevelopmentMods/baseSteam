package zone.moddev.mc.basesteam.resources;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import zone.moddev.mc.basesteam.testing.NativeMachineFixture;
import zone.moddev.mc.basesteam.testing.NativeFuelWorld;
import zone.moddev.mc.basesteam.machines.BlastFurnaceTileEntity;
import zone.moddev.mc.basesteam.machines.CoalBoilerTileEntity;
import cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine;
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
    @org.junit.Rule public final NativeMachineFixture machineTypes = new NativeMachineFixture();
    private static Map<TagKey<Item>, List<Holder<Item>>> previousTags;
    private static Map<TagKey<Item>, List<Holder<Item>>> nativeFuelTags;
    private static NativeFuelWorld fuelWorld;

    @BeforeClass public static void bootstrapNativeFuel() throws ReflectiveOperationException {
        NativeTestBootstrap.start();
        fuelWorld = NativeFuelWorld.create();
        // Standalone tests need the actual vanilla item tags before the native fuel map.
        try (MultiPackResourceManager resources = new MultiPackResourceManager(
                PackType.SERVER_DATA, List.of(ServerPacksSource.createVanillaPackSource().fullResources()))) {
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
            nativeFuelTags = Map.copyOf(loaded);
            NativeTestBootstrap.bindItemTags(nativeFuelTags);
        }
    }

    @org.junit.Before public void bindFuelTagsAfterTheActualMachineFixturesAreInstalled() {
        NativeTestBootstrap.bindItemTags(nativeFuelTags);
    }

    @AfterClass public static void restoreNativeTags() {
        if (previousTags != null) {
            NativeTestBootstrap.bindItemTags(previousTags);
        }
    }

    @Test public void keepsExactVanillaAndEmptyFuelValues() {
        assertEquals(0, FurnaceFuelHelper.getBurnTime(ItemStack.EMPTY, fuelWorld));
        assertEquals(0, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.CRIMSON_PLANKS), fuelWorld));
        assertEquals(300, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.OAK_PLANKS), fuelWorld));
        assertEquals(1600, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.COAL), fuelWorld));
        assertEquals(20000, FurnaceFuelHelper.getBurnTime(new ItemStack(Items.LAVA_BUCKET), fuelWorld));
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
        NativeTestBootstrap.bindRegisteredItemComponents();
        assertEquals(237, FurnaceFuelHelper.getBurnTime(new ItemStack(item), fuelWorld));
    }

    @Test public void nativeConstantFuelKeepsFullIntegerDurationWithoutChangingMachineSpeed() {
        ItemStack coal = new ItemStack(Items.COAL, 7);
        coal.set(net.minecraft.core.component.DataComponents.COOKING_FUEL,
                new net.minecraft.world.item.component.CookingFuel(
                        new net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.Constant(70000),
                        new net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat.Constant(3F)));
        assertEquals(70000, FurnaceFuelHelper.getBurnTime(coal, fuelWorld));
        assertEquals(7, coal.getCount());
    }

    @Test public void forgeEventCanOverrideTheNativeContextualValueWithoutChangingTheStack() {
        ItemStack coal = new ItemStack(Items.COAL, 7);
        int[] calls = {0};
        var bus = net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent.BUS;
        var listener = bus.addListener((net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent event) -> {
            if (event.getItemStack() != coal) return;
            assertEquals(1600, event.getBurnTime());
            assertNull(event.getRecipeType());
            calls[0]++;
            event.setBurnTime(73);
        });
        try {
            assertEquals(73, FurnaceFuelHelper.getBurnTime(coal, fuelWorld));
            assertEquals(1, calls[0]);
            assertEquals(7, coal.getCount());
        } finally { bus.removeListener(listener); }
        assertEquals(1600, FurnaceFuelHelper.getBurnTime(coal, fuelWorld));
    }

    @Test public void nativeSlotPresenceDoesNotBypassTheActualServerFuelValue() {
        ItemStack coal = new ItemStack(Items.COAL);
        coal.set(net.minecraft.core.component.DataComponents.COOKING_FUEL,
                new net.minecraft.world.item.component.CookingFuel(
                        new net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.Constant(0),
                        new net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat.Constant(1F)));
        var boiler = NativeMachineFixture.create(CoalBoilerTileEntity.class);
        assertTrue(FurnaceFuelHelper.isFuel(coal, boiler));
        boiler.setLevel(fuelWorld);
        assertFalse(FurnaceFuelHelper.isFuel(coal, boiler));
        assertFalse(FurnaceFuelHelper.isFuel(ItemStack.EMPTY, boiler));
        assertEquals(1, coal.getCount());
    }

    @Test public void coalBoilerConsumesLastCoalWithoutARemainder() throws Exception {
        assertLastCoal(CoalBoilerTileEntity.class);
    }

    @Test public void blastFurnaceConsumesLastCoalWithoutARemainder() throws Exception {
        assertLastCoal(BlastFurnaceTileEntity.class);
    }

    @Test public void coalBoilerConsumesExactlyOneFromLargerCoalStacks() throws Exception {
        assertLargerCoalStacks(CoalBoilerTileEntity.class);
    }

    @Test public void blastFurnaceConsumesExactlyOneFromLargerCoalStacks() throws Exception {
        assertLargerCoalStacks(BlastFurnaceTileEntity.class);
    }

    @Test public void coalBoilerReturnsExactlyOneBucketForLavaFuel() throws Exception {
        assertLavaRemainder(CoalBoilerTileEntity.class);
    }

    @Test public void blastFurnaceReturnsExactlyOneBucketForLavaFuel() throws Exception {
        assertLavaRemainder(BlastFurnaceTileEntity.class);
    }

    @Test public void bothActualFuelLookupsRejectWaterBucketsWithoutChangingThem() throws Exception {
        ItemStack water = new ItemStack(Items.WATER_BUCKET);
        assertEquals(0, FurnaceFuelHelper.getBurnTime(water, fuelWorld));
        CoalBoilerTileEntity boiler = fueled(CoalBoilerTileEntity.class, water);
        var coalLookup = CoalBoilerTileEntity.class.getDeclaredMethod("getFuelBurnTime");
        coalLookup.setAccessible(true);
        assertEquals(0, coalLookup.invoke(boiler));
        assertSame(water, boiler.getItem(0));
        assertEquals(1, boiler.getItem(0).getCount());
        BlastFurnaceTileEntity furnace = fueled(BlastFurnaceTileEntity.class, water.copy());
        var furnaceLookup = BlastFurnaceTileEntity.class.getDeclaredMethod("getFuelBurnTime", ItemStack.class);
        furnaceLookup.setAccessible(true);
        assertEquals(0, furnaceLookup.invoke(furnace, furnace.getItem(0)));
        assertTrue(furnace.getItem(0).is(Items.WATER_BUCKET));
        assertEquals(1, furnace.getItem(0).getCount());
    }

    private static void assertLastCoal(Class<? extends TileEntitySimplePowerMachine> type) throws Exception {
        ItemStack coal = new ItemStack(Items.COAL);
        assertEquals(1600, FurnaceFuelHelper.getBurnTime(coal, fuelWorld));
        assertNull("Coal has no native crafting remainder", coal.getCraftingRemainder());
        var machine = fueled(type, coal);
        consume(machine);
        assertSame(ItemStack.EMPTY, machine.getItem(0));
        assertEquals(0, machine.getItem(0).getCount());
    }

    private static void assertLargerCoalStacks(Class<? extends TileEntitySimplePowerMachine> type) throws Exception {
        for (int count : new int[]{2, 7}) {
            ItemStack coal = new ItemStack(Items.COAL, count);
            var machine = fueled(type, coal);
            for (int remaining = count - 1; remaining >= 0; remaining--) {
                consume(machine);
                assertEquals(type.getSimpleName(), remaining, machine.getItem(0).getCount());
                if (remaining > 0) {
                    assertSame(coal, machine.getItem(0));
                    assertTrue(machine.getItem(0).is(Items.COAL));
                    assertNull(machine.getItem(0).getCraftingRemainder());
                } else {
                    assertSame(ItemStack.EMPTY, machine.getItem(0));
                }
            }
        }
    }

    private static void assertLavaRemainder(Class<? extends TileEntitySimplePowerMachine> type) throws Exception {
        ItemStack previous = null;
        for (int use = 0; use < 2; use++) {
            ItemStack lava = new ItemStack(Items.LAVA_BUCKET);
            assertEquals(20000, FurnaceFuelHelper.getBurnTime(lava, fuelWorld));
            var template = lava.getCraftingRemainder();
            assertNotNull(template);
            ItemStack expected = template.create();
            assertTrue(expected.is(Items.BUCKET));
            assertEquals(1, expected.getCount());
            var machine = fueled(type, lava);
            consume(machine);
            ItemStack actual = machine.getItem(0);
            assertTrue(ItemStack.isSameItemSameComponents(expected, actual));
            assertEquals(1, actual.getCount());
            assertNotSame(lava, actual);
            assertNotSame(previous, actual);
            assertEquals(0, FurnaceFuelHelper.getBurnTime(actual, fuelWorld));
            previous = actual;
        }
    }

    private static <T extends TileEntitySimplePowerMachine> T fueled(Class<T> type, ItemStack fuel) {
        T machine = NativeMachineFixture.create(type);
        machine.setLevel(fuelWorld);
        int dirty = fuelWorld.dirtyNotifications;
        machine.setItem(0, fuel);
        assertTrue("Native inventory writes must still notify their world", fuelWorld.dirtyNotifications > dirty);
        return machine;
    }

    private static void consume(TileEntitySimplePowerMachine machine) throws Exception {
        var method = machine.getClass().getDeclaredMethod("decrementFuel");
        method.setAccessible(true);
        method.invoke(machine);
    }
}
