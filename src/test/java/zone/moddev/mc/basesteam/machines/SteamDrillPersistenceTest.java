package zone.moddev.mc.basesteam.machines;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import zone.moddev.mc.basesteam.init.Power;
import java.lang.reflect.Field;
import java.io.InputStream;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.BeforeClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SteamDrillPersistenceTest {
    @BeforeClass public static void bootstrapNativeRegistries() {
        NativeTestBootstrap.start();
    }

    @Test public void retainsLegacyProgressUntilWorldIsAttached() {
        SteamDrillTileEntity drill = new SteamDrillTileEntity();
        drill.load(target(94));
        drill.prepareDataFieldsForSync();
        assertEquals(94, drill.getDataFieldArray()[1]);
    }

    @Test public void clearsPreviousProgressOnReadingAnUntargetedTile() {
        SteamDrillTileEntity drill = new SteamDrillTileEntity();
        drill.load(target(94));
        drill.load(new CompoundTag());
        drill.prepareDataFieldsForSync();
        assertEquals(0, drill.getDataFieldArray()[1]);
    }

    @Test public void preservesAllSixFacingsTargetAndWorkUntilWorldIsAttached() throws Exception {
        for (Direction facing : Direction.values()) {
            CompoundTag tag = target(94);
            tag.putByte("Facing", (byte) facing.get3DDataValue());
            CompoundTag state = new CompoundTag();
            state.putString("Name", "minecraft:stone");
            tag.put("TargetState", state);
            SteamDrillTileEntity drill = new SteamDrillTileEntity();
            drill.load(tag);
            assertEquals(facing, field(drill, "savedFacing"));
            assertEquals(new BlockPos(2048, 80, 2049), field(drill, "targetBlockCoord"));
            assertEquals(state, field(drill, "savedTargetState"));
            assertTrue((Boolean) field(drill, "deferred"));
            drill.prepareDataFieldsForSync();
            assertEquals(94, drill.getDataFieldArray()[1]);
        }
    }

    @Test public void loadingAnotherTileDoesNotKeepPreviousFacing() throws Exception {
        SteamDrillTileEntity drill = new SteamDrillTileEntity();
        CompoundTag tag = target(94);
        tag.putByte("Facing", (byte) Direction.UP.get3DDataValue());
        drill.load(tag);
        drill.load(target(12));
        assertNull(field(drill, "savedFacing"));
        tag.putByte("Facing", (byte) 127);
        drill.load(tag);
        assertNull(field(drill, "savedFacing"));
    }

    @Test public void legacyEnergyAndOutputInventorySurviveDeferredRestore() {
        CompoundTag tag = target(94);
        tag.putIntArray("Energy", new int[] { Float.floatToIntBits(37.5F) });
        ListTag items = new ListTag();
        CompoundTag stack = new CompoundTag();
        stack.putString("id", "minecraft:cobblestone");
        stack.putByte("Count", (byte) 23);
        stack.putByte("Slot", (byte) 4);
        items.add(stack);
        tag.put("Items", items);
        SteamDrillTileEntity drill = new SteamDrillTileEntity();
        drill.load(tag);
        assertEquals(37.5F, drill.getEnergy(Power.steam_power), 0.0F);
        ItemStack output = drill.getItem(4);
        assertEquals(net.minecraft.world.item.Items.COBBLESTONE, output.getItem());
        assertEquals(23, output.getCount());
    }

    @Test public void serializationNeverLooksUpTheWorldOrInfersNeighbours() throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = SteamDrillTileEntity.class.getResourceAsStream("SteamDrillTileEntity.class")) {
            new ClassReader(input).accept(node, 0);
        }
        int methods = 0, cachedFacingReads = 0;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("saveAdditional")) continue;
            methods++;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof FieldInsnNode
                        && ((FieldInsnNode) instruction).name.equals("savedFacing")) cachedFacingReads++;
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                assertFalse("Serialization must not reacquire a saving chunk: " + call.name,
                        call.name.equals("getFacing") || call.name.equals("inferFacing")
                        || call.name.equals("powerAdvantageWorld") || call.owner.startsWith("net/minecraft/world/"));
            }
        }
        assertEquals(1, methods);
        assertTrue("Cached facing must still be serialized", cachedFacingReads > 0);
    }

    @Test public void facinglessLegacyTargetsInferAllDirectionsWithoutAnAttachedWorld() throws Exception {
        Method infer = SteamDrillTileEntity.class.getDeclaredMethod("inferFacing");
        infer.setAccessible(true);
        BlockPos origin = new BlockPos(2048, 80, 2048);
        for (Direction direction : Direction.values()) {
            CompoundTag tag = target(94);
            BlockPos target = origin.relative(direction, 3);
            tag.putInt("targetX", target.getX());
            tag.putInt("targetY", target.getY());
            tag.putInt("targetZ", target.getZ());
            SteamDrillTileEntity drill = new SteamDrillTileEntity(origin, new SteamDrillBlock().defaultBlockState());
            drill.load(tag);
            assertNull(field(drill, "savedFacing"));
            assertEquals(direction, infer.invoke(drill));
        }
    }

    private static Object field(SteamDrillTileEntity drill, String name) throws Exception {
        Field field = SteamDrillTileEntity.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(drill);
    }

    private static CompoundTag target(int progress) {
        CompoundTag tag = new CompoundTag();
        tag.putShort("progress", (short) progress);
        tag.putInt("targetX", 2048);
        tag.putInt("targetY", 80);
        tag.putInt("targetZ", 2049);
        return tag;
    }
}
