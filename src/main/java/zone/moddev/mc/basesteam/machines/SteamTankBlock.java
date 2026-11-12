package zone.moddev.mc.basesteam.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PoweredEntity;
import zone.moddev.mc.basesteam.init.Power;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SteamTankBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine {

	public SteamTankBlock(){
		super(machineProperties(0.75F).setId(zone.moddev.mc.basesteam.blocks.SteamBlockProperties.key("steam_tank")), Power.steam_power);
	}

	@Override
	public PoweredEntity newBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
		return new SteamTankTileEntity(pos, state);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState bs) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState bs, Level world, BlockPos coord) {
		BlockEntity te = world.getBlockEntity(coord);
		if(te instanceof SteamTankTileEntity){
			return (int)(15 * ((SteamTankTileEntity)te).getSteamLevel());
		} else{
			return 0;
		}
	}

	@Override
	public boolean isPowerSink(ConduitType conduitType) {
		return true;
	}

	@Override
	public boolean isPowerSource(ConduitType conduitType) {
		return true;
	}
}
