package zone.moddev.mc.basesteam.machines;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import static org.junit.Assert.*;

public class SteamOperationalPumpSourceTest {
    private static final BlockPos SOURCE = new BlockPos(8, 80, 8);

    @BeforeClass public static void bootstrap() {
        NativeTestBootstrap.start();
    }

    @Test public void nativePickupRetainsTheSlabAndOnlyClearsItsWater() {
        BlockState wet = Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
        PickupWorld world = new PickupWorld(wet);
        assertSame(Fluids.WATER, SteamPumpTileEntity.pickupSource(world.world, SOURCE, wet));
        assertEquals(1, world.writes);
        assertEquals(wet.setValue(BlockStateProperties.WATERLOGGED, false), world.state);
        assertSame(Fluids.EMPTY, SteamPumpTileEntity.pickupSource(world.world, SOURCE, world.state));
        assertEquals("A dry host cannot produce another bucket", 1, world.writes);
    }

    @Test public void nativePickupConsumesOrdinaryWaterAndLavaSources() {
        for (Block block : new Block[]{Blocks.WATER, Blocks.LAVA}) {
            BlockState source = block.defaultBlockState();
            PickupWorld world = new PickupWorld(source);
            assertTrue(source.getFluidState().isSource());
            assertSame(source.getFluidState().getType(), SteamPumpTileEntity.pickupSource(world.world, SOURCE, source));
            assertEquals(1, world.writes);
            assertEquals(Blocks.AIR.defaultBlockState(), world.state);
        }
    }

    @Test public void unsupportedModSourceIsNeverReplacedOrCredited() {
        Block host = new Block(Block.Properties.of(Material.STONE)) {
            @Override public FluidState getFluidState(BlockState state) { return Fluids.WATER.defaultFluidState(); }
        };
        BlockState source = host.defaultBlockState();
        PickupWorld world = new PickupWorld(source);
        assertTrue(source.getFluidState().isSource());
        assertSame(Fluids.EMPTY, SteamPumpTileEntity.pickupSource(world.world, SOURCE, source));
        assertSame(source, world.state);
        assertEquals(0, world.writes);
    }

    @Test public void flowingAndRefusedSourcesCannotProduceABucket() {
        BlockState flowing = Blocks.WATER.defaultBlockState().setValue(BlockStateProperties.LEVEL, 1);
        PickupWorld world = new PickupWorld(flowing);
        assertFalse(flowing.getFluidState().isEmpty());
        assertFalse(flowing.getFluidState().isSource());
        assertSame(Fluids.EMPTY, SteamPumpTileEntity.pickupSource(world.world, SOURCE, flowing));
        assertEquals(0, world.writes);
        RefusingSource host = new RefusingSource();
        BlockState refused = host.defaultBlockState();
        PickupWorld refusedWorld = new PickupWorld(refused);
        assertTrue(refused.getFluidState().isSource());
        assertSame(Fluids.EMPTY, SteamPumpTileEntity.pickupSource(refusedWorld.world, SOURCE, refused));
        assertEquals(1, host.pickups);
        assertSame(refused, refusedWorld.state);
        assertEquals(0, refusedWorld.writes);
    }

    @Test public void pumpUsesNativePickupBeforeCreditWithoutDestructiveFallback() throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = getClass().getResourceAsStream(
                "/zone/moddev/mc/basesteam/machines/SteamPumpTileEntity.class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node, 0);
        }
        MethodNode tick = method(node, "tickUpdate"), pickup = method(node, "pickupSource");
        int extraction = -1, fill = -1, index = 0;
        boolean nativePickup = false, nativeGuard = false;
        for (MethodNode candidate : new MethodNode[]{tick, pickup}) {
            for (AbstractInsnNode instruction : candidate.instructions.toArray()) {
                if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode field = (FieldInsnNode) instruction;
                    assertFalse("Caller must not erase waterlogged hosts with AIR",
                            field.getOpcode() == Opcodes.GETSTATIC && field.owner.equals("net/minecraft/world/level/block/Blocks")
                                    && field.name.equals("AIR"));
                } else if (instruction instanceof TypeInsnNode) {
                    TypeInsnNode type = (TypeInsnNode) instruction;
                    if (type.getOpcode() == Opcodes.INSTANCEOF && type.desc.equals("net/minecraft/world/level/block/BucketPickup")) nativeGuard = true;
                } else if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    assertFalse("No destructive fallback when native pickup is unavailable",
                            call.name.equals("removeBlock") || call.name.equals("destroyBlock"));
                    if (candidate == pickup) {
                        assertFalse("Pickup policy delegates block mutation to the native handler",
                                call.name.equals("setBlock") || call.name.equals("setBlockAndUpdate"));
                        if (call.owner.equals("net/minecraft/world/level/block/BucketPickup") && call.name.equals("pickupBlock")) nativePickup = true;
                    } else {
                        if (call.name.equals("pickupSource")) extraction = index;
                        if (call.owner.equals("cyano/poweradvantage/api/fluid/FluidTank") && call.name.equals("fill")) fill = index;
                    }
                }
                index++;
            }
        }
        assertTrue("Unsupported blocks are rejected before native pickup", nativeGuard && nativePickup);
        assertTrue("Successful native pickup precedes the final bucket credit", extraction >= 0 && fill > extraction);
    }

    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(method -> method.name.equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("Missing pump method " + name));
    }

    private static final class RefusingSource extends Block implements BucketPickup {
        private int pickups;
        private RefusingSource() { super(Block.Properties.of(Material.STONE)); }
        @Override public FluidState getFluidState(BlockState state) { return Fluids.WATER.defaultFluidState(); }
        @Override public net.minecraft.world.item.ItemStack pickupBlock(LevelAccessor world, BlockPos pos, BlockState state) {
            pickups++;
            return net.minecraft.world.item.ItemStack.EMPTY;
        }
        @Override public java.util.Optional<net.minecraft.sounds.SoundEvent> getPickupSound() { return java.util.Optional.empty(); }
    }

    private static final class PickupWorld {
        private BlockState state;
        private int writes;
        private final LevelAccessor world;

        private PickupWorld(BlockState initial) {
            // Forge initializes BlockState's cached fluid in the post-registration bake.
            initial.initCache();
            state = initial;
            world = (LevelAccessor) Proxy.newProxyInstance(LevelAccessor.class.getClassLoader(), new Class<?>[]{LevelAccessor.class},
                    (proxy, method, arguments) -> {
                        if (method.getName().equals("setBlock")) {
                            assertEquals(SOURCE, arguments[0]);
                            state = (BlockState) arguments[1];
                            state.initCache();
                            writes++;
                            return true;
                        }
                        throw new AssertionError("Unexpected native pickup world call: " + method.getName());
                    });
        }
    }
}
