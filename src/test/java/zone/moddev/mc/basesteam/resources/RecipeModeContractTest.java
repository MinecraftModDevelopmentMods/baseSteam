package zone.moddev.mc.basesteam.resources;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.junit.Test;
import zone.moddev.mc.basepower.PowerAdvantage;
import zone.moddev.mc.basepower.RecipeMode;
import zone.moddev.mc.basesteam.SteamAdvantage;
import static org.junit.Assert.*;

public class RecipeModeContractTest {
    @Test public void everyNativeModeConditionSelectsOnlyItsConfiguredMode() throws Exception {
        RecipeMode previous = PowerAdvantage.recipeMode;
        try {
            for (RecipeMode requested : RecipeMode.values()) {
                JsonObject input = new JsonObject();
                input.addProperty("mode", requested.name());
                ICondition condition = codec("ModeCondition").codec().parse(JsonOps.INSTANCE, input).getOrThrow();
                for (RecipeMode configured : RecipeMode.values()) {
                    PowerAdvantage.recipeMode = configured;
                    assertEquals(requested.name() + " / " + configured.name(), requested == configured,
                            condition.test(ICondition.IContext.EMPTY, JsonOps.INSTANCE));
                }
            }
            JsonObject invalid = new JsonObject();
            invalid.addProperty("mode", "not_a_recipe_mode");
            assertTrue(codec("ModeCondition").codec().parse(JsonOps.INSTANCE, invalid).error().isPresent());
        } finally { PowerAdvantage.recipeMode = previous; }
    }

    @Test public void nativeMusketConditionHonorsBothConfigValuesWithoutChangingModes() throws Exception {
        boolean previous = SteamAdvantage.MUSKET_ENABLE;
        RecipeMode previousMode = PowerAdvantage.recipeMode;
        try {
            ICondition condition = codec("MusketCondition").codec().parse(JsonOps.INSTANCE, new JsonObject()).getOrThrow();
            for (RecipeMode mode : RecipeMode.values()) {
                PowerAdvantage.recipeMode = mode;
                for (boolean enabled : new boolean[] {false, true}) {
                    SteamAdvantage.MUSKET_ENABLE = enabled;
                    assertEquals(enabled, condition.test(ICondition.IContext.EMPTY, JsonOps.INSTANCE));
                    assertEquals(mode, PowerAdvantage.recipeMode);
                }
            }
        } finally {
            SteamAdvantage.MUSKET_ENABLE = previous;
            PowerAdvantage.recipeMode = previousMode;
        }
    }

    @SuppressWarnings("unchecked")
    private static MapCodec<? extends ICondition> codec(String record) throws Exception {
        Class<?> type = Class.forName("zone.moddev.mc.basesteam.recipe.RecipeConditions$" + record);
        var field = type.getDeclaredField("CODEC");
        field.setAccessible(true);
        return (MapCodec<? extends ICondition>) field.get(null);
    }
}
