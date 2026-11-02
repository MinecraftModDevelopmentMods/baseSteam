package zone.moddev.mc.basesteam.blocks;

import zone.moddev.mc.basesteam.init.Power;
import net.minecraft.block.material.Material;

public class SteamPipeBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerConduit{

	public SteamPipeBlock() {
		super(Material.PISTON, 0.75f, 2f/16f, Power.steam_power);
	}
	
}
