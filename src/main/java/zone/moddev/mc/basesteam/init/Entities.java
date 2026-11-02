package zone.moddev.mc.basesteam.init;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.tileentity.TileEntity;
import zone.moddev.mc.basepower.compat.LegacyIdentity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.blocks.DrillBitTileEntity;
import zone.moddev.mc.basesteam.machines.*;

public class Entities {

	private static boolean initDone = false;
	public static void init(){
		if(initDone) return;
		
		Blocks.init();
		

		registerTile(CoalBoilerTileEntity.class, "steam_boiler_coal");
		registerTile(ElectricBoilerTileEntity.class, "steam_boiler_electric");
		registerTile(GeothermalBoilerTileEntity.class, "steam_boiler_geothermal");
		registerTile(SteamTankTileEntity.class, "steam_tank");
		registerTile(BlastFurnaceTileEntity.class, "steam_furnace");
		registerTile(RockCrusherTileEntity.class, "steam_crusher");
		registerTile(SteamDrillTileEntity.class, "steam_drill");
		registerTile(SteamElevatorTileEntity.class, "steam_elevator");
		registerTile(DrillBitTileEntity.class, "drillbit");
		registerTile(SteamStillTileEntity.class, "steam_still");
		registerTile(SteamPumpTileEntity.class, "steam_pump");
		registerTile(OilBoilerTileEntity.class, "steam_boiler_oil");
		
		
		initDone = true;
	}

	private static void registerTile(Class<? extends TileEntity> type, String name) {
		LegacyIdentity.registerTileEntity(type, SteamAdvantage.MODID + "." + name,
				SteamAdvantage.LEGACY_MODID + "." + name);
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerRenderers(){
		ClientRegistry.bindTileEntitySpecialRenderer(DrillBitTileEntity.class, new zone.moddev.mc.basesteam.graphics.DrillBitRenderer());
	}
}
