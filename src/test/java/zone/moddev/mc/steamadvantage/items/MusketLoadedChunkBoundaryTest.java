package zone.moddev.mc.steamadvantage.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.Test;
import static org.junit.Assert.*;

public class MusketLoadedChunkBoundaryTest {
    @Test public void allSixDirectionsStopBeforeUnloadedNativeBlocks() {
        Vec3 origin = new Vec3(8.5, 80.5, 8.5);
        for (Direction direction : Direction.values()) {
            Vec3 end = origin.add(direction.getStepX() * 64, direction.getStepY() * 64, direction.getStepZ() * 64);
            Vec3 actual = MusketItem.loadedRayEnd(origin, end,
                    pos -> pos.getX() >= 0 && pos.getX() < 16 && pos.getZ() >= 0 && pos.getZ() < 16
                            && pos.getY() >= 64 && pos.getY() < 96);
            assertTrue(direction.toString(), origin.distanceToSqr(actual) < origin.distanceToSqr(end));
            BlockPos stopped = new BlockPos(actual);
            assertTrue(stopped.getX() >= 0 && stopped.getX() < 16);
            assertTrue(stopped.getZ() >= 0 && stopped.getZ() < 16);
            assertTrue(stopped.getY() >= 64 && stopped.getY() < 96);
        }
    }

    @Test public void loadedShotsKeepTheFullOriginalRange() {
        Vec3 origin = new Vec3(-16.5, 80.5, -32.5);
        Vec3 end = origin.add(64, 0, 0);
        assertEquals(end, MusketItem.loadedRayEnd(origin, end, pos -> true));
        assertEquals(origin, MusketItem.loadedRayEnd(origin, end, pos -> false));
    }

    @Test public void aGrazingDiagonalCannotSkipTheFirstMissingBlock() {
        Vec3 origin = new Vec3(15.99999, 80.5, 15.9);
        Vec3 end = origin.add(1, 0, 64);
        Vec3 actual = MusketItem.loadedRayEnd(origin, end, pos -> pos.getX() < 16 && pos.getZ() < 16);
        assertTrue(actual.x < 16);
        assertTrue(actual.z < 16);
        assertTrue(origin.distanceToSqr(actual) < 0.01D);
    }

    @Test public void aBoundaryBesideTheMuzzleCannotReverseTheRay() {
        Vec3 origin = new Vec3(15.999999, 80.5, 8.5);
        assertEquals(origin, MusketItem.loadedRayEnd(origin, origin.add(64, 0, 0), pos -> pos.getX() < 16));
    }
}
