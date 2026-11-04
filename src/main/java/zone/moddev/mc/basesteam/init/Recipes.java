package zone.moddev.mc.basesteam.init;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import zone.moddev.mc.basepower.PowerAdvantage;
import zone.moddev.mc.basepower.RecipeMode;
import zone.moddev.mc.basesteam.compat.BaseMetalsCompat;

public class Recipes {

	private static boolean initDone = false;

	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		Items.init();
		
		
		
		RecipeMode recipeMode = PowerAdvantage.recipeMode;
		OreDictionary.registerOre("stick", net.minecraft.init.Items.STICK);
		OreDictionary.registerOre("blockObsidian", net.minecraft.init.Blocks.OBSIDIAN);
		
		if(recipeMode == RecipeMode.APOCALYPTIC){
			BaseMetalsCompat.addCrusherRecipe(Items.steam_governor, new ItemStack(zone.moddev.mc.basepower.init.Items.sprocket,2));

			BaseMetalsCompat.addCrusherRecipe(Blocks.steam_crusher, new ItemStack(Items.steam_governor,2));
			BaseMetalsCompat.addCrusherRecipe(Blocks.steam_furnace, new ItemStack(Items.steam_governor,2));
			BaseMetalsCompat.addCrusherRecipe(Blocks.steam_boiler_coal, new ItemStack(Items.steam_governor,2));
			BaseMetalsCompat.addCrusherRecipe(Blocks.steam_boiler_electric, new ItemStack(Items.steam_governor,2));
			BaseMetalsCompat.addCrusherRecipe(Blocks.steam_boiler_geothermal, new ItemStack(Items.steam_governor,2));
			BaseMetalsCompat.addCrusherRecipe(Blocks.steam_drill, new ItemStack(Items.steam_governor,2));
			BaseMetalsCompat.addCrusherRecipe(Blocks.steam_elevator, new ItemStack(Items.steam_governor,3));
			BaseMetalsCompat.addCrusherRecipe(Blocks.steam_tank, new ItemStack(Items.steam_governor,1));
		}
		
		
		
		initDone = true;
	}
}
