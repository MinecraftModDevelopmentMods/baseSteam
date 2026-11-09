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

    @Test public void allSixDirectionsSurviveNativeUpdatePackets() {
        for (Direction direction : Direction.values()) {
            DrillBitTileEntity tile = new DrillBitTileEntity();
            tile.setDirection(direction);
            CompoundTag packet = tile.getUpdatePacket().getTag();
            assertEquals(direction.get3DDataValue(), packet.getByte("dir"));
            DrillBitTileEntity restored = new DrillBitTileEntity();
            restored.onDataPacket(null, tile.getUpdatePacket());
            assertEquals(direction.getAxis(), restored.getDirection());
            assertEquals(direction.get3DDataValue(), restored.getUpdatePacket().getTag().getByte("dir"));
        }
    }

    @Test public void allSixDirectionsSurviveNativeSaveAndChunkUpdateTag() {
        for (Direction direction : Direction.values()) {
            DrillBitTileEntity tile = new DrillBitTileEntity();
            tile.setDirection(direction);

            CompoundTag saved = tile.saveWithoutMetadata();
            assertEquals(direction.get3DDataValue(), saved.getByte("dir"));
            DrillBitTileEntity restored = new DrillBitTileEntity();
            restored.load(saved);
            assertEquals(direction.get3DDataValue(), restored.getUpdateTag().getByte("dir"));

            DrillBitTileEntity client = new DrillBitTileEntity();
            client.handleUpdateTag(tile.getUpdateTag());
            assertEquals(direction.get3DDataValue(), client.getUpdatePacket().getTag().getByte("dir"));
        }
    }

    @Test public void oldDirectionKeyRemainsReadableAndCanonicalKeyWins() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("d", (byte) Direction.EAST.get3DDataValue());
        DrillBitTileEntity tile = new DrillBitTileEntity();
        tile.load(tag);
        assertEquals(Direction.Axis.X, tile.getDirection());
        tag.putByte("dir", (byte) Direction.NORTH.get3DDataValue());
        tile.load(tag);
        assertEquals(Direction.Axis.Z, tile.getDirection());
    }

    @Test public void invalidDirectionFallsBackToDown() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("dir", (byte) 127);
        DrillBitTileEntity tile = new DrillBitTileEntity();
        tile.load(tag);
        assertEquals(Direction.DOWN.get3DDataValue(), tile.getUpdatePacket().getTag().getByte("dir"));
    }
}
