package zone.moddev.mc.basesteam.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PoweredEntity;
import zone.moddev.mc.basepower.conduitnetwork.ConduitRegistry;
import zone.moddev.mc.basepower.init.Fluids;
import zone.moddev.mc.basesteam.init.Power;
import net.minecraft.world.level.material.Material;
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
public class SteamStillBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine{

	
	public SteamStillBlock() {
		super(Material.PISTON, 0.75f, Power.steam_power, Fluids.fluidConduit_general);
	}

	@Override
	public PoweredEntity newBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
		return new SteamStillTileEntity(pos, state);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState bs) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState bs, Level world, BlockPos coord) {
		if(world.getBlockEntity(coord) instanceof SteamStillTileEntity){
			return ((SteamStillTileEntity)world.getBlockEntity(coord)).getComparatorOutput();
		}
		return 0;
	}

	
	
	///// Overrides to make this a multi-type block /////


	@Override
	public boolean isPowerSink(ConduitType type){
		return true;
	}

	@Override
	public boolean isPowerSource(ConduitType type){
		return Fluids.isFluidType(type);
	}

	///// end multi-type overrides /////
}
