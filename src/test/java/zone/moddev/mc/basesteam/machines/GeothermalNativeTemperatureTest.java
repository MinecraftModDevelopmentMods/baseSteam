package zone.moddev.mc.basesteam.machines;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class GeothermalNativeTemperatureTest {
    @BeforeClass public static void bootstrap() { NativeTestBootstrap.start(); }

    @Test public void nativeLavaTemperaturePreservesTheExistingHeatInput() {
        assertEquals(1300, Fluids.LAVA.getAttributes().getTemperature());
        assertEquals(464, GeothermalBoilerTileEntity.thermalEnergy(Blocks.LAVA.defaultBlockState()), 0);
        assertEquals(464, GeothermalBoilerTileEntity.thermalEnergy(Blocks.LAVA.defaultBlockState()
                .setValue(BlockStateProperties.LEVEL, 7)), 0);
    }

    @Test public void ordinaryFireWaterAndColdBlocksKeepTheirExistingHeatInput() {
        assertEquals(200, GeothermalBoilerTileEntity.thermalEnergy(Blocks.FIRE.defaultBlockState()), 0);
        assertEquals(0, GeothermalBoilerTileEntity.thermalEnergy(Blocks.WATER.defaultBlockState()), 0);
        assertEquals(0, GeothermalBoilerTileEntity.thermalEnergy(Blocks.STONE.defaultBlockState()), 0);
    }
}
