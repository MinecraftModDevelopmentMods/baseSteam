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
public class RockCrusherBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine {

	
	public RockCrusherBlock() {
		super(Material.PISTON, 0.75f, Power.steam_power);
	}

	@Override
	public PoweredEntity newBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
		return new RockCrusherTileEntity(pos, state);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState bs) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState bs, Level world, BlockPos coord) {
		if(world.getBlockEntity(coord) instanceof RockCrusherTileEntity){
			return ((RockCrusherTileEntity)world.getBlockEntity(coord)).getComparatorOutput();
		}
		return 0;
	}


	@Override
	public boolean isPowerSink(ConduitType conduitType) {
		return true;
	}

	@Override
	public boolean isPowerSource(ConduitType conduitType) {
		return false;
	}

}
