package zone.moddev.mc.basesteam.machines;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.basesteam.SteamAdvantage;

import static org.junit.Assert.*;

public class OilBoilerLegacyFuelOverrideTest {
    private static final String[] OILS = {"crude_oil", "refined_oil"};
    private Map<String, Float> previousOverrides;

    @BeforeClass public static void bootstrap() { Bootstrap.register(); }

    @Before public void preserveConfiguration() {
        previousOverrides = new HashMap<>(SteamAdvantage.fluidBurnValues);
        SteamAdvantage.fluidBurnValues.clear();
    }

    @After public void restoreConfiguration() {
        SteamAdvantage.fluidBurnValues.clear();
        SteamAdvantage.fluidBurnValues.putAll(previousOverrides);
    }

    @Test public void legacyOilKeysReachTheBurnCalculationAndCache() throws Exception {
        for (String name : OILS) {
            Fluid fluid = fluid(name, "basepower");
            float configured = name.equals("crude_oil") ? 8123f : 29123f;
            SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage." + name, configured);
            assertEquals(configured, lookup(fluid), 0f);
            assertEquals(configured, burnTime(fluid), 0.01f);
            assertEquals("The cached value must retain the override", configured, burnTime(fluid), 0.01f);
        }
    }

    @Test public void rawNamesWinOverCurrentAndLegacyKeys() throws Exception {
        for (String name : OILS) {
            Fluid fluid = fluid(name, "basepower");
            SteamAdvantage.fluidBurnValues.put(name, 12000f);
            SteamAdvantage.fluidBurnValues.put("fluid.basepower." + name, 9000f);
            SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage." + name, 6000f);
            assertEquals(12000f, lookup(fluid), 0f);
            assertEquals(12000f, burnTime(fluid), 0.01f);
        }
    }

    @Test public void currentKeysWinOverLegacyKeys() throws Exception {
        for (String name : OILS) {
            Fluid fluid = fluid(name, "basepower");
            SteamAdvantage.fluidBurnValues.put("fluid.basepower." + name, 9000f);
            SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage." + name, 6000f);
            assertEquals(9000f, lookup(fluid), 0f);
            assertEquals(9000f, burnTime(fluid), 0.01f);
        }
    }

    @Test public void legacyZeroDisablesBothOilsInsteadOfFallingThrough() throws Exception {
        for (String name : OILS) {
            Fluid fluid = fluid(name, "basepower");
            SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage." + name, 0f);
            assertEquals(0f, lookup(fluid), 0f);
            assertEquals(0f, burnTime(fluid), 0f);
            assertEquals(0f, burnTime(fluid), 0f);
        }
    }

    @Test public void rawZeroStillWinsOverBothPositiveAliases() throws Exception {
        for (String name : OILS) {
            Fluid fluid = fluid(name, "basepower");
            SteamAdvantage.fluidBurnValues.put(name, 0f);
            SteamAdvantage.fluidBurnValues.put("fluid.basepower." + name, 9000f);
            SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage." + name, 6000f);
            assertEquals(0f, burnTime(fluid), 0f);
        }
    }

    @Test public void currentZeroStillWinsOverPositiveLegacyAliases() throws Exception {
        for (String name : OILS) {
            Fluid fluid = fluid(name, "basepower");
            SteamAdvantage.fluidBurnValues.put("fluid.basepower." + name, 0f);
            SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage." + name, 6000f);
            assertEquals(0f, burnTime(fluid), 0f);
        }
    }

    @Test public void foreignFluidsWithTheSameRawNamesDoNotAcquirePowerOverrides() throws Exception {
        for (String name : OILS) {
            Fluid foreign = fluid(name, "othermod");
            SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage." + name, 6000f);
            assertNull(lookup(foreign));
            SteamAdvantage.fluidBurnValues.put("fluid.othermod." + name, 7000f);
            assertEquals(7000f, lookup(foreign), 0f);
        }
    }

    @Test public void unrelatedFluidsAndNearMatchesDoNotAcquireLegacyAliases() throws Exception {
        SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage.lubricant", 6000f);
        SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage.mineralogy_crude_oil", 7000f);
        SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage.crude_oil", 8000f);
        assertNull(lookup(fluid("lubricant", "basepower")));
        assertNull(lookup(fluid("mineralogy_crude_oil", "basepower")));
        assertNull(lookup(fluid("crude_oil", "otherbasepower")));
    }

    @Test public void absentOverridesLeaveTheExistingCrudeOilDefault() throws Exception {
        Fluid fluid = fluid("crude_oil", "basepower");
        assertNull(lookup(fluid));
        assertEquals(5000f, burnTime(fluid), 0f);
    }

    @Test public void lookupDoesNotRewriteConfigurationEntries() throws Exception {
        SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage.crude_oil", 8123f);
        SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage.refined_oil", 29123f);
        SteamAdvantage.fluidBurnValues.put("fluid.basemetals.custom_fuel", 9001f);
        Map<String, Float> original = new HashMap<>(SteamAdvantage.fluidBurnValues);
        for (String name : OILS) burnTime(fluid(name, "basepower"));
        assertEquals(original, SteamAdvantage.fluidBurnValues);
        assertFalse(SteamAdvantage.fluidBurnValues.containsKey("fluid.basepower.crude_oil"));
        assertFalse(SteamAdvantage.fluidBurnValues.containsKey("fluid.basepower.refined_oil"));
    }

    private static Fluid fluid(String name, String owner) {
        ResourceLocation texture = new ResourceLocation(owner, "blocks/" + name);
        Fluid fluid = new Fluid(name, texture, texture);
        fluid.setUnlocalizedName(owner + "." + name);
        return fluid;
    }

    private static Float lookup(Fluid fluid) throws Exception {
        return invoke("getConfiguredBurnTimePerBucketFor", fluid);
    }

    private static Float burnTime(Fluid fluid) throws Exception {
        return invoke("getBurnTimePerBucketFor", fluid);
    }

    private static Float invoke(String name, Fluid fluid) throws Exception {
        Method method = OilBoilerTileEntity.class.getDeclaredMethod(name, Fluid.class);
        method.setAccessible(true);
        return (Float) method.invoke(null, fluid);
    }
}
