package zone.moddev.mc.basesteam.machines;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import cyano.poweradvantage.api.fluid.FluidStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.init.Entities;
import zone.moddev.mc.basesteam.init.Power;
import static org.junit.Assert.*;

public class SteamMachinePersistenceTest {
    @BeforeClass public static void bootstrapNativeTypes() {
        NativeTestBootstrap.start();
        Entities.STEAM_BOILER_COAL = type("steam_boiler_coal", CoalBoilerTileEntity::new);
        Entities.STEAM_BOILER_ELECTRIC = type("steam_boiler_electric", ElectricBoilerTileEntity::new);
        Entities.STEAM_BOILER_GEOTHERMAL = type("steam_boiler_geothermal", GeothermalBoilerTileEntity::new);
        Entities.STEAM_PUMP = type("steam_pump", SteamPumpTileEntity::new);
        Entities.STEAM_STILL = type("steam_still", SteamStillTileEntity::new);
        Entities.STEAM_TANK = type("steam_tank", SteamTankTileEntity::new);
    }

    @Test public void levelsAndDecayRetainExactBinaryFractions() {
        CoalBoilerTileEntity coal = new CoalBoilerTileEntity();
        coal.setEnergy(375.0F, Power.steam_power);
        coal.getTank().fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1500), true);
        assertEquals(0.375F, coal.getSteamLevel(), 0.0F);
        assertEquals(0.375F, coal.getWaterLevel(), 0.0F);
        ElectricBoilerTileEntity electric = new ElectricBoilerTileEntity();
        electric.setEnergy(37.5F, Power.steam_power);
        electric.setEnergy(60.0F, ElectricBoilerTileEntity.ELECTRIC_POWER);
        assertEquals(0.375F, electric.getSteamLevel(), 0.0F);
        assertEquals(0.375F, electric.getElectricityLevel(), 0.0F);
        SteamTankTileEntity tank = new SteamTankTileEntity();
        tank.setEnergy(3750.0F, Power.steam_power);
        assertEquals(0.375F, tank.getSteamLevel(), 0.0F);
        tank.tickUpdate(true);
        assertEquals(3749.9375F, tank.getEnergy(Power.steam_power), 0.0F);
        assertEquals(0.0625F, Power.ENERGY_LOST_PER_TICK, 0.0F);
        assertEquals(0.03125F, ElectricBoilerTileEntity.ELECTRICITY_TO_STEAM, 0.0F);
        assertEquals(2.5F, SteamDrillTileEntity.ENERGY_COST_DRILLBIT, 0.0F);
        assertEquals(10.0F, SteamDrillTileEntity.ENERGY_COST_MOVE, 0.0F);
        assertEquals(1.0F, SteamDrillTileEntity.ENERGY_COST_PROGRESS_TICK, 0.0F);
        assertEquals(32.0F, SteamElevatorTileEntity.STEAM_PER_ELEVATOR_MOVE, 0.0F);
        assertEquals(125, RockCrusherTileEntity.TICKS_PER_ACTION);
    }

    @Test public void coalAndElectricStateSurviveNativeSaveLoad() {
        CompoundTag tag = state(37.5F);
        tag.put("Tank", water(1500));
        tag.putInt("BurnTime", 127);
        tag.putInt("BurnTimeTotal", 1600);
        CoalBoilerTileEntity coal = new CoalBoilerTileEntity();
        coal.loadWithComponents(tag, NativeTestBootstrap.registries());
        CompoundTag saved = coal.saveWithFullMetadata(NativeTestBootstrap.registries());
        assertEquals("basesteam:steam_boiler_coal", saved.getString("id"));
        assertEquals(127, saved.getInt("BurnTime"));
        assertEquals(1600, saved.getInt("BurnTimeTotal"));
        CoalBoilerTileEntity restored = new CoalBoilerTileEntity();
        restored.loadWithComponents(saved, NativeTestBootstrap.registries());
        assertEquals(37.5F, restored.getEnergy(Power.steam_power), 0.0F);
        assertEquals(1500, restored.getTank().getFluidAmount());
        tag.putFloat("Electricity", 60.0F);
        ElectricBoilerTileEntity electric = new ElectricBoilerTileEntity();
        electric.loadWithComponents(tag, NativeTestBootstrap.registries());
        ElectricBoilerTileEntity electricRestored = new ElectricBoilerTileEntity();
        electricRestored.loadWithComponents(electric.saveWithoutMetadata(NativeTestBootstrap.registries()), NativeTestBootstrap.registries());
        assertEquals(37.5F, electricRestored.getEnergy(Power.steam_power), 0.0F);
        assertEquals(60.0F, electricRestored.getEnergy(ElectricBoilerTileEntity.ELECTRIC_POWER), 0.0F);
        assertEquals(1500, electricRestored.getTank().getFluidAmount());
    }

    @Test public void pumpAliasAndGeothermalMisspellingRemainReadable() {
        CompoundTag tag = state(37.5F);
        tag.put("TankOut", water(750));
        tag.putByte("NextPump", (byte) 11);
        SteamPumpTileEntity pump = new SteamPumpTileEntity();
        pump.loadWithComponents(tag, NativeTestBootstrap.registries());
        CompoundTag saved = pump.saveWithoutMetadata(NativeTestBootstrap.registries());
        assertEquals(750, saved.getCompound("Tank").getInt("Amount"));
        assertEquals(11, saved.getByte("NextPump"));
        SteamPumpTileEntity pumpRestored = new SteamPumpTileEntity();
        pumpRestored.loadWithComponents(saved, NativeTestBootstrap.registries());
        assertEquals(750, pumpRestored.getTank().getFluidAmount());
        assertEquals(37.5F, pumpRestored.getEnergy(Power.steam_power), 0.0F);
        tag.putFloat("Tempoerature", 412.5F);
        GeothermalBoilerTileEntity geothermal = new GeothermalBoilerTileEntity();
        geothermal.loadWithComponents(tag, NativeTestBootstrap.registries());
        assertEquals(412.5F, geothermal.saveWithoutMetadata(NativeTestBootstrap.registries()).getFloat("Temperature"), 0.0F);
        tag.putFloat("Temperature", 500.0F);
        geothermal.loadWithComponents(tag, NativeTestBootstrap.registries());
        assertEquals(500.0F, geothermal.saveWithoutMetadata(NativeTestBootstrap.registries()).getFloat("Temperature"), 0.0F);
    }

    @Test public void stillKeepsSeparateTanksAndExplicitlyEmptyReloads() {
        CompoundTag tag = state(37.5F);
        tag.put("TankIn", water(625));
        tag.put("TankOut", water(375));
        SteamStillTileEntity still = new SteamStillTileEntity();
        still.loadWithComponents(tag, NativeTestBootstrap.registries());
        SteamStillTileEntity restored = new SteamStillTileEntity();
        restored.loadWithComponents(still.saveWithoutMetadata(NativeTestBootstrap.registries()), NativeTestBootstrap.registries());
        assertEquals(625, restored.getInputTank().getFluidAmount());
        assertEquals(375, restored.getOutputTank().getFluidAmount());
        assertEquals(37.5F, restored.getEnergy(Power.steam_power), 0.0F);
        tag.put("TankIn", water(0));
        tag.put("TankOut", water(0));
        restored.loadWithComponents(tag, NativeTestBootstrap.registries());
        assertEquals(0, restored.getInputTank().getFluidAmount());
        assertEquals(0, restored.getOutputTank().getFluidAmount());
    }

    private static CompoundTag state(float steam) {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("Energy", new int[] {Float.floatToIntBits(steam)});
        return tag;
    }

    private static CompoundTag water(int amount) {
        CompoundTag tag = new CompoundTag();
        tag.putString("FluidName", "minecraft:water");
        tag.putInt("Amount", amount);
        return tag;
    }

    private static <T extends BlockEntity> BlockEntityType<T> type(String id, BlockEntityType.BlockEntitySupplier<T> factory) {
        ForgeRegistry<BlockEntityType<?>> registry =
                (ForgeRegistry<BlockEntityType<?>>) ForgeRegistries.BLOCK_ENTITY_TYPES;
        boolean locked = registry.isLocked();
        registry.unfreeze();
        try {
            BlockEntityType<T> type = BlockEntityType.Builder.of(factory, Blocks.STONE).build(null);
            registry.register(ResourceLocation.fromNamespaceAndPath("basesteam", id), type);
            return type;
        } finally {
            if (locked) registry.freeze();
        }
    }
}
