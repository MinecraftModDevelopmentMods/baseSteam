package zone.moddev.mc.basesteam.items;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

public class MusketPersistenceTest {
    @BeforeClass public static void bootstrapNativeRegistries() {
        NativeTestBootstrap.start();
    }

    @Test public void loadingCycleKeepsLegacyByteValuesWearAndOtherComponents() {
        ItemStack stack = new ItemStack(Items.CROSSBOW);
        CompoundTag tag = new CompoundTag();
        tag.putString("owner", "legacy");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.setDamageValue(123);
        assertTrue(MusketItem.isNotLoaded(stack));
        MusketItem.startLoad(stack);
        assertEquals(2, data(stack).getByteOr("loaded", (byte)0));
        assertTrue(MusketItem.isAlmostLoaded(stack));
        MusketItem.finishLoad(stack);
        assertEquals(1, data(stack).getByteOr("loaded", (byte)0));
        assertTrue(MusketItem.isLoaded(stack.copy()));
        var registries = NativeTestBootstrap.registries();
        ItemStack restored = ItemStack.CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
                ItemStack.CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack).getOrThrow()).getOrThrow();
        assertTrue(MusketItem.isLoaded(restored));
        assertEquals(123, restored.getDamageValue());
        MusketItem.unload(stack);
        assertEquals(0, data(stack).getByteOr("loaded", (byte)0));
        assertTrue(MusketItem.isNotLoaded(stack));
        assertEquals("legacy", data(stack).getStringOr("owner", ""));
        assertEquals(123, stack.getDamageValue());
        assertTrue(MusketItem.isLoaded(restored));
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.get(DataComponents.CUSTOM_DATA).copyTag();
    }

    @Test public void rayIntersectionSupportsEveryAxisAndRejectsTargetsBehind() {
        Vec3 origin = new Vec3(0, 0, 0);
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            Vec3 ray = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            Vec3 center = ray.scale(5);
            AABB ahead = new AABB(center.x - 0.5, center.y - 0.5, center.z - 0.5,
                    center.x + 0.5, center.y + 0.5, center.z + 0.5);
            assertTrue(MusketItem.rayIntersectsBoundingBox(origin, ray, ahead));
            assertFalse(MusketItem.rayIntersectsBoundingBox(origin, ray.scale(-1), ahead));
        }
    }
}
