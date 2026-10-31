package zone.moddev.mc.steamadvantage.blocks;

import zone.moddev.mc.steamadvantage.init.Power;
import net.minecraft.world.level.material.Material;

public class SteamPipeBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerConduit{

	public SteamPipeBlock() {
		super(Material.PISTON, 0.75f, 2f/16f, Power.steam_power);
	}
	
}
