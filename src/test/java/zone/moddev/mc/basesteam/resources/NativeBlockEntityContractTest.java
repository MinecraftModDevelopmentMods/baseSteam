package zone.moddev.mc.basesteam.resources;

import zone.moddev.mc.basesteam.testing.NativeMachineFixture;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.blocks.DrillBitBlock;
import zone.moddev.mc.basesteam.blocks.DrillBitTileEntity;
import zone.moddev.mc.basesteam.init.Entities;
import zone.moddev.mc.basesteam.init.Power;
import zone.moddev.mc.basesteam.machines.*;
import static org.junit.Assert.*;

public class NativeBlockEntityContractTest {
    @org.junit.Rule public final NativeMachineFixture machineTypes = new NativeMachineFixture();
    @BeforeClass public static void bootstrap() { NativeTestBootstrap.start(); }

    @Test public void everyProductionFactoryOwnsItsActualNativePositionAndState() {
        BlockPos pos = new BlockPos(2053, 81, -2049);
        for (Block block : NativeMachineFixture.blocks()) {
            BlockState state = block.defaultBlockState();
            BlockEntity tile = ((EntityBlock) block).newBlockEntity(pos, state);
            assertNotNull(block.getClass().getName(), tile);
            assertEquals(pos, tile.getBlockPos());
            assertSame(state, tile.getBlockState());
        }
    }

    @Test public void drillLoadCannotReplaceDestinationNativePositionOrFacing() {
        BlockPos destination = new BlockPos(-2050, 83, 2048);
        for (Direction direction : Direction.values()) {
            BlockState state = NativeMachineFixture.state("steam_drill").setValue(SteamDrillBlock.FACING, direction);
            SteamDrillTileEntity drill = new SteamDrillTileEntity(destination, state);
            CompoundTag saved = new CompoundTag();
            saved.putInt("x", 7); saved.putInt("y", 9); saved.putInt("z", 11);
            saved.putIntArray("Energy", new int[] { Float.floatToIntBits(37.5F) });
            saved.putByte("Facing", (byte) direction.get3DDataValue());
            drill.loadWithComponents(saved, NativeTestBootstrap.registries());
            assertEquals(destination, drill.getBlockPos());
            assertSame(state, drill.getBlockState());
            assertEquals(direction, drill.getBlockState().getValue(SteamDrillBlock.FACING));
            assertEquals(37.5F, drill.getEnergy(Power.steam_power), 0);
        }
    }

    @Test public void helperTickerIsExplicitAndRejectsUnrelatedNativeTypes() {
        DrillBitBlock block = new DrillBitBlock();
        BlockEntityType<DrillBitTileEntity> previous = Entities.DRILLBIT;
        try {
            Entities.DRILLBIT = BlockEntityType.Builder.of(DrillBitTileEntity::new, block).build(null);
            assertNotNull(block.getTicker(null, block.defaultBlockState(), Entities.DRILLBIT));
            assertNull(block.getTicker(null, block.defaultBlockState(), BlockEntityType.FURNACE));
            DrillBitTileEntity tile = new DrillBitTileEntity(new BlockPos(17, 80, 31), block.defaultBlockState());
            block.getTicker(null, block.defaultBlockState(), Entities.DRILLBIT)
                    .tick(null, tile.getBlockPos(), tile.getBlockState(), tile);
            assertEquals(0, tile.rotation, 0);
        } finally { Entities.DRILLBIT = previous; }
    }

    @Test public void nativeConstructorStillRejectsAMismatchedTypeAndBlock() {
        BlockEntityType<DrillBitTileEntity> previous = Entities.DRILLBIT;
        try {
            Entities.DRILLBIT = BlockEntityType.Builder.of(DrillBitTileEntity::new,
                    net.minecraft.world.level.block.Blocks.STONE).build(null);
            assertThrows(IllegalStateException.class, () -> new DrillBitTileEntity(
                    BlockPos.ZERO, NativeMachineFixture.state("drillbit")));
        } finally { Entities.DRILLBIT = previous; }
    }
}
