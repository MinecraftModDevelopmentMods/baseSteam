package zone.moddev.mc.steamadvantage.resources;

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
import zone.moddev.mc.steamadvantage.blocks.DrillBitBlock;
import zone.moddev.mc.steamadvantage.blocks.DrillBitTileEntity;
import zone.moddev.mc.steamadvantage.init.Entities;
import zone.moddev.mc.steamadvantage.init.Power;
import zone.moddev.mc.steamadvantage.machines.*;
import static org.junit.Assert.*;

public class NativeBlockEntityContractTest {
    @BeforeClass public static void bootstrap() { net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap(); }

    @Test public void everyProductionFactoryOwnsItsActualNativePositionAndState() {
        BlockPos pos = new BlockPos(2053, 81, -2049);
        for (Block block : new Block[] { new CoalBoilerBlock(), new ElectricBoilerBlock(),
                new GeothermalBoilerBlock(), new OilBoilerBlock(), new BlastFurnaceBlock(),
                new RockCrusherBlock(), new SteamDrillBlock(), new SteamElevatorBlock(),
                new SteamPumpBlock(), new SteamStillBlock(), new SteamTankBlock(), new DrillBitBlock() }) {
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
            BlockState state = new SteamDrillBlock().defaultBlockState().setValue(SteamDrillBlock.FACING, direction);
            SteamDrillTileEntity drill = new SteamDrillTileEntity(destination, state);
            CompoundTag saved = new CompoundTag();
            saved.putInt("x", 7); saved.putInt("y", 9); saved.putInt("z", 11);
            saved.putIntArray("Energy", new int[] { Float.floatToIntBits(37.5F) });
            saved.putByte("Facing", (byte) direction.get3DDataValue());
            drill.load(saved);
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
}
