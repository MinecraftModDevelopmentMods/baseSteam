package zone.moddev.mc.basesteam.init;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basepower.compat.LegacyIdentity;
import zone.moddev.mc.basesteam.blocks.DrillBitTileEntity;
import zone.moddev.mc.basesteam.machines.*;

public class Entities {

	private static boolean initDone = false;
	private static boolean dataFixersRegistered = false;

	public static void registerDataFixers(){
		if(dataFixersRegistered) return;
		ModFixs fixes = FMLCommonHandler.instance().getDataFixer().init(SteamAdvantage.MODID, 1);
		fixes.registerFix(FixTypes.BLOCK_ENTITY, LegacyIdentity.tileIds(SteamAdvantage.LEGACY_MODID, SteamAdvantage.MODID, 1));
		fixes.registerFix(FixTypes.ITEM_INSTANCE, LegacyIdentity.itemIds(SteamAdvantage.LEGACY_MODID, SteamAdvantage.MODID, 1));
		dataFixersRegistered = true;
	}

	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		

		registerTileEntity(CoalBoilerTileEntity.class, "steam_boiler_coal");
		registerTileEntity(ElectricBoilerTileEntity.class, "steam_boiler_electric");
		registerTileEntity(GeothermalBoilerTileEntity.class, "steam_boiler_geothermal");
		registerTileEntity(SteamTankTileEntity.class, "steam_tank");
		registerTileEntity(BlastFurnaceTileEntity.class, "steam_furnace");
		registerTileEntity(RockCrusherTileEntity.class, "steam_crusher");
		registerTileEntity(SteamDrillTileEntity.class, "steam_drill");
		registerTileEntity(SteamElevatorTileEntity.class, "steam_elevator");
		registerTileEntity(DrillBitTileEntity.class, "drillbit");
		registerTileEntity(SteamStillTileEntity.class, "steam_still");
		registerTileEntity(SteamPumpTileEntity.class, "steam_pump");
		registerTileEntity(OilBoilerTileEntity.class, "steam_boiler_oil");
		
		
		initDone = true;
	}

	private static void registerTileEntity(Class<? extends TileEntity> tileEntityClass, String path){
		GameRegistry.registerTileEntity(tileEntityClass, new ResourceLocation(SteamAdvantage.MODID, path));
	}

	@SideOnly(Side.CLIENT)
	public static void registerRenderers(){
		ClientRegistry.bindTileEntitySpecialRenderer(DrillBitTileEntity.class, new zone.moddev.mc.basesteam.graphics.DrillBitRenderer());
	}
}
