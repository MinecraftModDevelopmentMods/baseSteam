package zone.moddev.mc.basesteam.machines;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import org.junit.Test;
import zone.moddev.mc.basesteam.SteamAdvantage;

import static org.junit.Assert.assertEquals;

public class OilBoilerCrudeFuelTest {
    @Test
    public void bothCrudeOilsUseTheSameDefault() {
        withFuelOverrides(() -> {
            assertEquals(5000.0F, OilBoilerTileEntity.getBurnTimePerBucketFor(oil("crude_oil")), 0.0F);
            assertEquals(5000.0F, OilBoilerTileEntity.getBurnTimePerBucketFor(oil("mineralogy_crude_oil")), 0.0F);
        });
    }

    @Test
    public void explicitFuelOverridesStillWin() {
        withFuelOverrides(() -> {
            SteamAdvantage.fluidBurnValues.put("mineralogy_crude_oil", 0.0F);
            SteamAdvantage.fluidBurnValues.put("fluid.poweradvantage.crude_oil", 1234.0F);
            assertEquals(0.0F, OilBoilerTileEntity.getBurnTimePerBucketFor(oil("mineralogy_crude_oil")), 0.0F);
            assertEquals(1234.0F, OilBoilerTileEntity.getBurnTimePerBucketFor(oil("crude_oil")), 0.0F);
        });
    }

    private static Fluid oil(String name) {
        ResourceLocation sprite = new ResourceLocation("minecraft", "blocks/water_still");
        return new Fluid(name, sprite, sprite);
    }

    private static void withFuelOverrides(Runnable check) {
        Map<String, Float> before = new HashMap<>(SteamAdvantage.fluidBurnValues);
        try {
            SteamAdvantage.fluidBurnValues.clear();
            check.run();
        } finally {
            SteamAdvantage.fluidBurnValues.clear();
            SteamAdvantage.fluidBurnValues.putAll(before);
        }
    }
}
