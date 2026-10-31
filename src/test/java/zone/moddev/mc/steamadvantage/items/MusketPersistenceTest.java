package zone.moddev.mc.steamadvantage.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

public class MusketPersistenceTest {
    @BeforeClass public static void bootstrapNativeRegistries() {
        net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test public void loadingCycleKeepsLegacyByteValuesAndOtherNbt() {
        ItemStack stack = new ItemStack(net.minecraft.world.item.Items.STICK);
        CompoundTag tag = new CompoundTag();
        tag.putString("owner", "legacy");
        stack.setTag(tag);
        assertTrue(MusketItem.isNotLoaded(stack));
        MusketItem.startLoad(stack);
        assertEquals(2, stack.getTag().getByte("loaded"));
        assertTrue(MusketItem.isAlmostLoaded(stack));
        MusketItem.finishLoad(stack);
        assertEquals(1, stack.getTag().getByte("loaded"));
        assertTrue(MusketItem.isLoaded(stack.copy()));
        MusketItem.unload(stack);
        assertEquals(0, stack.getTag().getByte("loaded"));
        assertTrue(MusketItem.isNotLoaded(stack));
        assertEquals("legacy", stack.getTag().getString("owner"));
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
