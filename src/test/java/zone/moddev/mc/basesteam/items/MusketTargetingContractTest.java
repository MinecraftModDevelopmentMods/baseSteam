package zone.moddev.mc.basesteam.items;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import static org.junit.Assert.*;

public class MusketTargetingContractTest {
    @BeforeClass public static void bootstrap() {
        NativeTestBootstrap.start();
    }

    @Test public void nearerTerrainRejectsTargetsBehindItOnEveryAxis() {
        Vec3 origin = Vec3.ZERO;
        for (Direction direction : Direction.values()) {
            Vec3 ray = new Vec3(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            assertFalse(direction.toString(), MusketItem.rayHitBeforeTerrain(origin, ray.scale(3), around(ray.scale(5))).isPresent());
            Optional<Vec3> hit = MusketItem.rayHitBeforeTerrain(origin, ray.scale(8), around(ray.scale(5)));
            assertTrue(direction.toString(), hit.isPresent());
            assertEquals(4.5 * 4.5, origin.distanceToSqr(hit.get()), 0.000001);
        }
    }

    @Test public void terrainWinsAnExactTieWithTheEntitySurface() {
        assertFalse(MusketItem.rayHitBeforeTerrain(Vec3.ZERO, new Vec3(0, 0, 4.5),
                around(new Vec3(0, 0, 5))).isPresent());
    }

    @Test public void finiteSegmentRejectsOffRayBehindAndBeyondRangeTargets() {
        Vec3 end = new Vec3(0, 0, 64);
        assertFalse(MusketItem.rayHitBeforeTerrain(Vec3.ZERO, end, around(new Vec3(2, 0, 5))).isPresent());
        assertFalse(MusketItem.rayHitBeforeTerrain(Vec3.ZERO, end, around(new Vec3(0, 0, -5))).isPresent());
        assertFalse(MusketItem.rayHitBeforeTerrain(Vec3.ZERO, end, around(new Vec3(0, 0, 65))).isPresent());
    }

    @Test public void overlappingEntityStartsAtZeroDistance() {
        Optional<Vec3> hit = MusketItem.rayHitBeforeTerrain(Vec3.ZERO, new Vec3(0, 0, 8), around(Vec3.ZERO));
        assertTrue(hit.isPresent());
        assertEquals(Vec3.ZERO, hit.get());
    }

    @Test public void targetingUsesLoadedServerEntitiesAndTerrainBeforeEntitySelection() throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = getClass().getResourceAsStream("/zone/moddev/mc/basesteam/items/MusketItem.class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node, 0);
        }
        MethodNode trace = node.methods.stream().filter(method -> method.name.equals("rayTraceBlocksAndEntities"))
                .findFirst().orElseThrow(() -> new AssertionError("Missing targeting method"));
        boolean terrain = false, loadedEntities = false, boundedSelection = false, spectatorFilter = false;
        for (AbstractInsnNode instruction : trace.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.name.equals("clip")) terrain = true;
            if (call.owner.equals("net/minecraft/server/level/ServerLevel") && call.name.equals("getAllEntities")) {
                assertTrue("Terrain must be traced before selecting entities", terrain);
                loadedEntities = true;
            }
            if (call.name.equals("rayHitBeforeTerrain")) boundedSelection = true;
            if (call.name.equals("isSpectator")) spectatorFilter = true;
        }
        assertTrue("Server targeting must use the loaded-entity collection", loadedEntities);
        assertTrue("Entity selection must be bounded by the actual terrain hit", boundedSelection);
        assertTrue("Loaded-entity iteration must preserve the previous spectator exclusion", spectatorFilter);
    }

    private static AABB around(Vec3 center) {
        return new AABB(center.x - 0.5, center.y - 0.5, center.z - 0.5,
                center.x + 0.5, center.y + 0.5, center.z + 0.5);
    }
}
