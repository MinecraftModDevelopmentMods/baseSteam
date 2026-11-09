package zone.moddev.mc.basesteam.machines;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PoweredEntity;
import zone.moddev.mc.basesteam.init.Power;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;


/**
 * @author DrCyano
 *
 */
public class BlastFurnaceBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine{

	
	public BlastFurnaceBlock() {
		super(Material.PISTON, 0.75f, Power.steam_power);
	}

	@Override
	public PoweredEntity newBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
		return new BlastFurnaceTileEntity(pos, state);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState bs) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState bs, Level world, BlockPos coord) {
		if(world.getBlockEntity(coord) instanceof BlastFurnaceTileEntity){
			return ((BlastFurnaceTileEntity)world.getBlockEntity(coord)).getComparatorOutput();
		}
		return 0;
	}


	@Override
	public boolean isPowerSink(ConduitType powerType) {
		return ConduitType.areSameType(Power.steam_power,powerType);
	}

	@Override
	public boolean isPowerSource(ConduitType powerType) {
		return false;
	}

}
