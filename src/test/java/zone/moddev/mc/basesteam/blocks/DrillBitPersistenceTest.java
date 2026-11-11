package zone.moddev.mc.basesteam.blocks;

import zone.moddev.mc.basesteam.testing.NativeMachineFixture;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import org.junit.Test;
import static org.junit.Assert.*;

public class DrillBitPersistenceTest {
    @org.junit.Rule public final NativeMachineFixture machineTypes = new NativeMachineFixture();
    @org.junit.BeforeClass public static void bootstrapNativeRegistries() {
        NativeTestBootstrap.start();
    }

    @Test public void allSixDirectionsSurviveNativeSaveAndChunkUpdateTag() {
        for (Direction direction : Direction.values()) {
            DrillBitTileEntity tile = new DrillBitTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("drillbit"));
            tile.setDirection(direction);

            var registries = NativeTestBootstrap.registries();
            CompoundTag saved = tile.saveWithoutMetadata(registries);
            assertEquals(direction.get3DDataValue(), saved.getByteOr("dir", (byte)0));
            DrillBitTileEntity restored = new DrillBitTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("drillbit"));
            restored.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, registries, saved));
            assertEquals(direction.get3DDataValue(), restored.getUpdateTag(registries).getByteOr("dir", (byte)0));

            DrillBitTileEntity client = new DrillBitTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("drillbit"));
            client.handleUpdateTag(net.minecraft.world.level.storage.TagValueInput.create(
                    net.minecraft.util.ProblemReporter.DISCARDING, registries, tile.getUpdateTag(registries)), registries);
            assertEquals(direction.get3DDataValue(), client.getUpdateTag(registries).getByteOr("dir", (byte)0));
        }
    }

    @Test public void oldDirectionKeyRemainsReadableAndCanonicalKeyWins() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("d", (byte) Direction.EAST.get3DDataValue());
        DrillBitTileEntity tile = new DrillBitTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("drillbit"));
        tile.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), tag));
        assertEquals(Direction.Axis.X, tile.getDirection());
        tag.putByte("dir", (byte) Direction.NORTH.get3DDataValue());
        tile.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), tag));
        assertEquals(Direction.Axis.Z, tile.getDirection());
    }

    @Test public void invalidDirectionFallsBackToDown() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("dir", (byte) 127);
        DrillBitTileEntity tile = new DrillBitTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("drillbit"));
        tile.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), tag));
        assertEquals(Direction.DOWN.get3DDataValue(), tile.getUpdateTag(NativeTestBootstrap.registries()).getByteOr("dir", (byte)0));
    }
}
