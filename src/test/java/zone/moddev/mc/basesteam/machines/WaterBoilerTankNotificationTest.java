package zone.moddev.mc.basesteam.machines;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.*;
import cyano.poweradvantage.api.IPowerMachine;
import zone.moddev.mc.basepower.init.Fluids;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import cyano.poweradvantage.api.fluid.FluidStack;
import cyano.poweradvantage.api.fluid.FluidTank;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Save notifications only; native restart reports remain the disk-persistence evidence. */
@RunWith(Parameterized.class)
public class WaterBoilerTankNotificationTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> boilers() { return Arrays.asList(new Object[][]{{"electric"}, {"geothermal"}}); }
    private final String type;
    private Probe probe;
    public WaterBoilerTankNotificationTest(String type) { this.type = type; }
    @BeforeClass public static void bootstrap() {
        NativeTestBootstrap.start();
        if (zone.moddev.mc.basesteam.init.Entities.STEAM_BOILER_ELECTRIC == null)
            zone.moddev.mc.basesteam.init.Entities.STEAM_BOILER_ELECTRIC = net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(ElectricBoilerTileEntity::new, net.minecraft.world.level.block.Blocks.STONE).build(null);
        if (zone.moddev.mc.basesteam.init.Entities.STEAM_BOILER_GEOTHERMAL == null)
            zone.moddev.mc.basesteam.init.Entities.STEAM_BOILER_GEOTHERMAL = net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(GeothermalBoilerTileEntity::new, net.minecraft.world.level.block.Blocks.STONE).build(null);
    }
    @Before public void createRealBoiler() {
        probe = type.equals("electric") ? new ElectricProbe() : new GeothermalProbe();
        probe.save().remember(probe.tank());
    }

    @Test public void publicAcceptedFillRequestsSavingBeforeAnyTick() throws Exception {
        FluidStack offer = water(5);
        assertEquals(5, fill(offer, true));
        assertEquals(5, offer.amount);
        saved(5);
    }

    @Test public void networkAcceptedWaterAlsoRequestsSavingBeforeAnyTick() {
        assertEquals(5, probe.machine().addEnergy(5, Fluids.fluidToConduitType(net.minecraft.world.level.material.Fluids.WATER)), 0);
        saved(5);
    }

    @Test public void directTankFillUsesTheSameOwnerNotification() {
        assertEquals(5, probe.tank().fill(water(5), true));
        saved(5);
    }

    @Test public void publicRealDrainRequestsSaving() throws Exception {
        stock(10);
        assertEquals(5, drain(5, true).amount);
        saved(5);
    }

    @Test public void directRealDrainUsesTheSameOwnerNotification() {
        stock(10);
        assertEquals(5, probe.tank().drain(5, true).amount);
        saved(5);
    }

    @Test public void drainingToEmptyRequestsSavingOfTheNativeEmptyRepresentation() {
        stock(5);
        assertEquals(5, probe.tank().drain(5, true).amount);
        saved(0);
        assertNull(FluidStack.loadFluidStackFromNBT(probe.save().savedTank));
    }

    @Test public void directAndPublicSimulationsNeverRequestSaving() throws Exception {
        stock(10);
        assertEquals(5, probe.tank().fill(water(5), false));
        assertEquals(5, probe.tank().drain(5, false).amount);
        assertEquals(5, fill(water(5), false));
        assertEquals(5, drain(5, false).amount);
        unchanged(10);
    }

    @Test public void aClientFillChangesOnlyItsLocalTankWithoutRequestingServerSaving() throws Exception {
        stock(10); probe.side(true);
        assertEquals(5, fill(water(5), true));
        assertEquals(15, probe.tank().getFluidAmount());
        assertEquals(0, probe.save().dirtyCalls);
        assertEquals(10, probe.save().savedTank.getInt("Amount"));
    }

    @Test public void aClientDirectDrainDoesNotRequestServerSaving() {
        stock(10); probe.side(true);
        assertEquals(5, probe.tank().drain(5, true).amount);
        assertEquals(5, probe.tank().getFluidAmount());
        assertEquals(0, probe.save().dirtyCalls);
        assertEquals(10, probe.save().savedTank.getInt("Amount"));
    }

    private static FluidStack water(int amount) { return new FluidStack(net.minecraft.world.level.material.Fluids.WATER, amount); }
    private void stock(int amount) { probe.tank().setFluid(water(amount)); probe.save().remember(probe.tank()); }
    private void saved(int amount) {
        assertTrue("Real server-side tank mutation must notify its owner immediately", probe.save().dirtyCalls > 0);
        assertEquals(amount, probe.tank().getFluidAmount());
        assertEquals(amount, probe.save().savedTank.getInt("Amount"));
    }
    private void unchanged(int amount) {
        assertEquals(0, probe.save().dirtyCalls);
        assertEquals(amount, probe.tank().getFluidAmount());
        assertEquals(amount, probe.save().savedTank.getInt("Amount"));
    }
    private int fill(FluidStack offer, boolean execute) throws Exception {
        return ((Number) probe.machine().getClass().getMethod("fill", net.minecraft.core.Direction.class, FluidStack.class, boolean.class)
                .invoke(probe.machine(), null, offer, execute)).intValue();
    }
    private FluidStack drain(int amount, boolean execute) throws Exception {
        return (FluidStack) probe.machine().getClass().getMethod("drain", net.minecraft.core.Direction.class, int.class, boolean.class)
                .invoke(probe.machine(), null, amount, execute);
    }
    private interface Probe {
        FluidTank tank();
        SaveWitness save();
        IPowerMachine machine();
        void side(boolean client);
    }
    private static final class SaveWitness {
        int dirtyCalls;
        CompoundTag savedTank;
        void remember(FluidTank tank) { dirtyCalls = 0; savedTank = tank.writeToNBT(new CompoundTag()).copy(); }
        void changed(FluidTank tank) { dirtyCalls++; savedTank = tank.writeToNBT(new CompoundTag()).copy(); }
    }
    private static final class ElectricProbe extends ElectricBoilerTileEntity implements Probe {
        private final SaveWitness witness = new SaveWitness();
        private Level testWorld = world(false);
        @Override public Level getLevel() { return testWorld; }
        @Override public void setChanged() { witness.changed(getTank()); }
        @Override public FluidTank tank() { return getTank(); }
        @Override public SaveWitness save() { return witness; }
        @Override public IPowerMachine machine() { return this; }
        @Override public void side(boolean client) { testWorld = world(client); }
    }
    private static final class GeothermalProbe extends GeothermalBoilerTileEntity implements Probe {
        private final SaveWitness witness = new SaveWitness();
        private Level testWorld = world(false);
        @Override public Level getLevel() { return testWorld; }
        @Override public void setChanged() { witness.changed(getTank()); }
        @Override public FluidTank tank() { return getTank(); }
        @Override public SaveWitness save() { return witness; }
        @Override public IPowerMachine machine() { return this; }
        @Override public void side(boolean client) { testWorld = world(client); }
    }
    private static Level world(boolean client) {
        try {
            Class<?> allocator = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field field = allocator.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            SaveWorld world = (SaveWorld) allocator.getMethod("allocateInstance", Class.class).invoke(field.get(null), SaveWorld.class);
            java.lang.reflect.Field clientField = Level.class.getDeclaredField("isClientSide");
            clientField.setAccessible(true);
            clientField.setBoolean(world, client);
            return world;
        } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
    }
    private static final class SaveWorld extends net.minecraft.server.level.ServerLevel {
        private SaveWorld() { super(null, null, null, null, null, null, null, false, 0L, java.util.Collections.emptyList(), false, null); }
    }
}
