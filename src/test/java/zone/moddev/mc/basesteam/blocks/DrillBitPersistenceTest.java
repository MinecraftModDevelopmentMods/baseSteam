package zone.moddev.mc.basesteam.blocks;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import org.junit.Test;
import static org.junit.Assert.*;

public class DrillBitPersistenceTest {
    @org.junit.BeforeClass public static void bootstrapNativeRegistries() {
        NativeTestBootstrap.start();
    }

    @Test public void allSixDirectionsSurviveNativeSaveAndChunkUpdateTag() {
        for (Direction direction : Direction.values()) {
            DrillBitTileEntity tile = new DrillBitTileEntity();
            tile.setDirection(direction);

            var registries = NativeTestBootstrap.registries();
            CompoundTag saved = tile.saveWithoutMetadata(registries);
            assertEquals(direction.get3DDataValue(), saved.getByte("dir"));
            DrillBitTileEntity restored = new DrillBitTileEntity();
            restored.loadWithComponents(saved, registries);
            assertEquals(direction.get3DDataValue(), restored.getUpdateTag(registries).getByte("dir"));

            DrillBitTileEntity client = new DrillBitTileEntity();
            client.handleUpdateTag(tile.getUpdateTag(registries), registries);
            assertEquals(direction.get3DDataValue(), client.getUpdateTag(registries).getByte("dir"));
        }
    }

    @Test public void oldDirectionKeyRemainsReadableAndCanonicalKeyWins() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("d", (byte) Direction.EAST.get3DDataValue());
        DrillBitTileEntity tile = new DrillBitTileEntity();
        tile.loadWithComponents(tag, NativeTestBootstrap.registries());
        assertEquals(Direction.Axis.X, tile.getDirection());
        tag.putByte("dir", (byte) Direction.NORTH.get3DDataValue());
        tile.loadWithComponents(tag, NativeTestBootstrap.registries());
        assertEquals(Direction.Axis.Z, tile.getDirection());
    }

    @Test public void invalidDirectionFallsBackToDown() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("dir", (byte) 127);
        DrillBitTileEntity tile = new DrillBitTileEntity();
        tile.loadWithComponents(tag, NativeTestBootstrap.registries());
        assertEquals(Direction.DOWN.get3DDataValue(), tile.getUpdateTag(NativeTestBootstrap.registries()).getByte("dir"));
    }
}
