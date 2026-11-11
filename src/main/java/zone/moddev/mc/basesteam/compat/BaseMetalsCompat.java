package zone.moddev.mc.basesteam.compat;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import zone.moddev.mc.basemetals.recipe.CrushingRecipe;

/** Accesses Base Metals' data-driven 1.13 crushing recipes without embedding it. */
public final class BaseMetalsCompat {
    private BaseMetalsCompat() {}

    public static boolean isLoaded() { return ModList.get().isLoaded("basemetals"); }

    /** 1.13 additions are supplied as JSON crushing recipes, not mutable runtime registrations. */
    public static void addCrusherRecipe(String input, ItemStack output) {}
    public static void addCrusherRecipe(Item input, ItemStack output) {}
    public static void addCrusherRecipe(Block input, ItemStack output) {}

    public static boolean hasCrusherRecipe(Level world, ItemStack input) {
        return getCrusherRecipeOutput(world, input) != null;
    }

    public static ItemStack getCrusherRecipeOutput(Level world, ItemStack input) {
        if (world == null || input == null || input.isEmpty()) return null;
        if (isLoaded()) {
            ItemStack result = LoadedCrushingRecipes.find(world, input);
            if (result != null) return result;
        }
        return getVanillaCrusherOutput(input);
    }

    // Resolve the optional recipe type only after Forge confirms Base Metals is installed.
    private static final class LoadedCrushingRecipes {
        private static ItemStack find(Level world, ItemStack input) {
            SingleRecipeInput inventory = new SingleRecipeInput(input.copy());
            for (var candidate : world.getRecipeManager().getAllRecipesFor(CrushingRecipe.TYPE.get())) {
                CrushingRecipe crushing = candidate.value();
                if (crushing.matches(inventory, world)) {
                    return crushing.getResultItem(world.registryAccess()).copy();
                }
            }
            return null;
        }
    }

    public static ItemStack getCrusherRecipeOutput(Level world, BlockState input) {
        return input == null ? null : getCrusherRecipeOutput(world, new ItemStack(input.getBlock()));
    }

    private static ItemStack getVanillaCrusherOutput(ItemStack input) {
        Item item = input.getItem();
        if (item == net.minecraft.world.level.block.Blocks.STONE.asItem()) return new ItemStack(net.minecraft.world.level.block.Blocks.COBBLESTONE);
        if (item == net.minecraft.world.level.block.Blocks.COBBLESTONE.asItem()) return new ItemStack(net.minecraft.world.level.block.Blocks.GRAVEL);
        if (item == net.minecraft.world.level.block.Blocks.GRAVEL.asItem()) return new ItemStack(net.minecraft.world.level.block.Blocks.SAND);
        if (item == net.minecraft.world.level.block.Blocks.SANDSTONE.asItem()) return new ItemStack(net.minecraft.world.level.block.Blocks.SAND, 4);
        if (item == net.minecraft.world.level.block.Blocks.RED_SANDSTONE.asItem()) return new ItemStack(net.minecraft.world.level.block.Blocks.RED_SAND, 4);
        if (item == net.minecraft.world.level.block.Blocks.GLASS.asItem()) return new ItemStack(net.minecraft.world.level.block.Blocks.SAND);
        if (item == net.minecraft.world.level.block.Blocks.GLOWSTONE.asItem()) return new ItemStack(net.minecraft.world.item.Items.GLOWSTONE_DUST, 4);
        if (item == net.minecraft.world.level.block.Blocks.SUGAR_CANE.asItem()) return new ItemStack(net.minecraft.world.item.Items.SUGAR, 2);
        if (item == net.minecraft.world.item.Items.BONE) return new ItemStack(net.minecraft.world.item.Items.BONE_MEAL, 3);
        if (item == net.minecraft.world.item.Items.BLAZE_ROD) return new ItemStack(net.minecraft.world.item.Items.BLAZE_POWDER, 2);
        return null;
    }
}
