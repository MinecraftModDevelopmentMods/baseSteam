package zone.moddev.mc.basesteam.machines;

import zone.moddev.mc.basesteam.testing.NativeMachineFixture;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import cyano.poweradvantage.api.fluid.FluidStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.Identifier;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.init.Entities;
import zone.moddev.mc.basesteam.init.Power;
import static org.junit.Assert.*;

public class SteamMachinePersistenceTest {
    @org.junit.Rule public final NativeMachineFixture machineTypes = new NativeMachineFixture();
    @BeforeClass public static void bootstrapNativeTypes() {
        NativeTestBootstrap.start();
    }

    @Test public void levelsAndDecayRetainExactBinaryFractions() {
        CoalBoilerTileEntity coal = new CoalBoilerTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_boiler_coal"));
        coal.setEnergy(375.0F, Power.steam_power);
        coal.getTank().fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1500), true);
        assertEquals(0.375F, coal.getSteamLevel(), 0.0F);
        assertEquals(0.375F, coal.getWaterLevel(), 0.0F);
        ElectricBoilerTileEntity electric = new ElectricBoilerTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_boiler_electric"));
        electric.setEnergy(37.5F, Power.steam_power);
        electric.setEnergy(60.0F, ElectricBoilerTileEntity.ELECTRIC_POWER);
        assertEquals(0.375F, electric.getSteamLevel(), 0.0F);
        assertEquals(0.375F, electric.getElectricityLevel(), 0.0F);
        SteamTankTileEntity tank = new SteamTankTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_tank"));
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
        CoalBoilerTileEntity coal = new CoalBoilerTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_boiler_coal"));
        coal.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), tag));
        CompoundTag saved = coal.saveWithFullMetadata(NativeTestBootstrap.registries());
        assertEquals("basesteam:steam_boiler_coal", saved.getStringOr("id", ""));
        assertEquals(127, saved.getIntOr("BurnTime", 0));
        assertEquals(1600, saved.getIntOr("BurnTimeTotal", 0));
        CoalBoilerTileEntity restored = new CoalBoilerTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_boiler_coal"));
        restored.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), saved));
        assertEquals(37.5F, restored.getEnergy(Power.steam_power), 0.0F);
        assertEquals(1500, restored.getTank().getFluidAmount());
        tag.putFloat("Electricity", 60.0F);
        ElectricBoilerTileEntity electric = new ElectricBoilerTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_boiler_electric"));
        electric.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), tag));
        ElectricBoilerTileEntity electricRestored = new ElectricBoilerTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_boiler_electric"));
        electricRestored.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), electric.saveWithoutMetadata(NativeTestBootstrap.registries())));
        assertEquals(37.5F, electricRestored.getEnergy(Power.steam_power), 0.0F);
        assertEquals(60.0F, electricRestored.getEnergy(ElectricBoilerTileEntity.ELECTRIC_POWER), 0.0F);
        assertEquals(1500, electricRestored.getTank().getFluidAmount());
    }

    @Test public void pumpAliasAndGeothermalMisspellingRemainReadable() {
        CompoundTag tag = state(37.5F);
        tag.put("TankOut", water(750));
        tag.putByte("NextPump", (byte) 11);
        SteamPumpTileEntity pump = new SteamPumpTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_pump"));
        pump.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), tag));
        CompoundTag saved = pump.saveWithoutMetadata(NativeTestBootstrap.registries());
        assertEquals(750, saved.getCompoundOrEmpty("Tank").getIntOr("Amount", 0));
        assertEquals(11, saved.getByteOr("NextPump", (byte)0));
        SteamPumpTileEntity pumpRestored = new SteamPumpTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_pump"));
        pumpRestored.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), saved));
        assertEquals(750, pumpRestored.getTank().getFluidAmount());
        assertEquals(37.5F, pumpRestored.getEnergy(Power.steam_power), 0.0F);
        tag.putFloat("Tempoerature", 412.5F);
        GeothermalBoilerTileEntity geothermal = new GeothermalBoilerTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_boiler_geothermal"));
        geothermal.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), tag));
        assertEquals(412.5F, geothermal.saveWithoutMetadata(NativeTestBootstrap.registries()).getFloatOr("Temperature", 0.0F), 0.0F);
        tag.putFloat("Temperature", 500.0F);
        geothermal.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), tag));
        assertEquals(500.0F, geothermal.saveWithoutMetadata(NativeTestBootstrap.registries()).getFloatOr("Temperature", 0.0F), 0.0F);
    }

    @Test public void stillKeepsSeparateTanksAndExplicitlyEmptyReloads() {
        CompoundTag tag = state(37.5F);
        tag.put("TankIn", water(625));
        tag.put("TankOut", water(375));
        SteamStillTileEntity still = new SteamStillTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_still"));
        still.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), tag));
        SteamStillTileEntity restored = new SteamStillTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_still"));
        restored.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), still.saveWithoutMetadata(NativeTestBootstrap.registries())));
        assertEquals(625, restored.getInputTank().getFluidAmount());
        assertEquals(375, restored.getOutputTank().getFluidAmount());
        assertEquals(37.5F, restored.getEnergy(Power.steam_power), 0.0F);
        tag.put("TankIn", water(0));
        tag.put("TankOut", water(0));
        restored.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, NativeTestBootstrap.registries(), tag));
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

}
