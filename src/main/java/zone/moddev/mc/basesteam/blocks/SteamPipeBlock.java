package zone.moddev.mc.basesteam.blocks;

import zone.moddev.mc.basesteam.init.Power;

public class SteamPipeBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerConduit{

	public SteamPipeBlock() {
		super(SteamBlockProperties.pistonLike("steam_pipe"), 2f/16f, Power.steam_power);
	}
	
}
