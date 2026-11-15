package zone.moddev.mc.basesteam.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import zone.moddev.mc.basepower.compat.CrushingRecipes;

/** Shared, reload-aware crushing lookup. Missing or removed recipes stay missing. */
public final class BaseMetalsCompat {
    private BaseMetalsCompat() {}

    public static boolean hasCrusherRecipe(Level world, ItemStack input) {
        return getCrusherRecipeOutput(world, input) != null;
    }

    public static ItemStack getCrusherRecipeOutput(Level world, ItemStack input) {
        return CrushingRecipes.find(world, input);
    }

    public static ItemStack getCrusherRecipeOutput(Level world, BlockState input) {
        return CrushingRecipes.find(world, input);
    }
}
