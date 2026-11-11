package zone.moddev.mc.basesteam.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PoweredEntity;
import zone.moddev.mc.basepower.conduitnetwork.ConduitRegistry;
import zone.moddev.mc.basepower.init.Fluids;
import zone.moddev.mc.basesteam.init.Blocks;
import zone.moddev.mc.basesteam.init.Power;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;


/**
 * The coal boiler is a bit more complicated because it consumes fluids (water) and produces steam, 
 * making it a multi-type powered block.
 * @author DrCyano
 *
 */
public class SteamPumpBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine{

	
	public SteamPumpBlock() {
		super(machineProperties(0.75F).setId(zone.moddev.mc.basesteam.blocks.SteamBlockProperties.key("steam_pump")), Power.steam_power, Fluids.fluidConduit_general);
	}

	@Override
	public PoweredEntity newBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
		return new SteamPumpTileEntity(pos, state);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState bs) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState bs, Level world, BlockPos coord) {
		if(world.getBlockEntity(coord) instanceof SteamPumpTileEntity){
			return ((SteamPumpTileEntity)world.getBlockEntity(coord)).getComparatorOutput();
		}
		return 0;
	}

	
	
	///// Overrides to make this a multi-type block /////
	@Override
	public boolean isPowerSink(ConduitType type){
		return ConduitType.areSameType(Power.steam_power, type);
	}

	@Override
	public boolean isPowerSource(ConduitType type){
		return !ConduitType.areSameType(Power.steam_power, type);
	}
	@Override
	public void wasExploded(net.minecraft.server.level.ServerLevel w, BlockPos coord, Explosion boom){
		destroyPipe(w, coord);
		super.wasExploded(w, coord, boom);
	}
	
	
	static void destroyPipe(Level w, BlockPos coord) {
		if(w.isClientSide()) return;
		// destroy connected drill bits
		BlockPos c = coord.below();
		while(c.getY() > 0 && w.getBlockState(c).getBlock() == Blocks.pump_pipe_steam){
			w.removeBlock(c, false);
			c = c.below();
		}
	}


	
	///// end multi-type overrides /////
}
