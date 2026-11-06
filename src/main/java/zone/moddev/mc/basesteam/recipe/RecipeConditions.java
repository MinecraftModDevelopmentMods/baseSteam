package zone.moddev.mc.basesteam.recipe;

import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import zone.moddev.mc.basepower.PowerAdvantage;
import zone.moddev.mc.basepower.RecipeMode;
import zone.moddev.mc.basesteam.SteamAdvantage;

/** Registers the data-pack conditions used by SteamAdvantage recipes. */
public final class RecipeConditions {
    private static boolean registered;

    private RecipeConditions() {}

    public static synchronized void register() {
        if (registered) return;
        CraftingHelper.register(new ResourceLocation(SteamAdvantage.MODID, "recipe_mode"), json -> {
            RecipeMode expected = RecipeMode.valueOf(JsonUtils.getString(json, "mode").toUpperCase());
            return () -> PowerAdvantage.recipeMode == expected;
        });
        CraftingHelper.register(new ResourceLocation(SteamAdvantage.MODID, "musket_enabled"),
                json -> () -> SteamAdvantage.MUSKET_ENABLE);
        registered = true;
    }
}
