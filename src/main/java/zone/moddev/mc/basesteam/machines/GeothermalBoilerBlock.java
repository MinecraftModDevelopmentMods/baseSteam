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

public class GeothermalBoilerBlock extends cyano.poweradvantage.api.simple.BlockSimplePowerMachine{

	public GeothermalBoilerBlock() {
		super(Material.PISTON, 0.75f, Power.steam_power, Fluids.fluidConduit_general);
	}

	@Override
	public PoweredEntity newBlockEntity(net.minecraft.core.BlockPos pos, BlockState state) {
		return new GeothermalBoilerTileEntity(pos, state);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState bs) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState bs, Level world, BlockPos coord) {
		if(world.getBlockEntity(coord) instanceof GeothermalBoilerTileEntity){
			return ((GeothermalBoilerTileEntity)world.getBlockEntity(coord)).getComparatorOutput();
		}
		return 0;
	}
///// Overrides to make this a multi-type block /////
@Override
public boolean isPowerSink(ConduitType type){
	return !ConduitType.areSameType(Power.steam_power, type);
}

	@Override
	public boolean isPowerSource(ConduitType type){
		return ConduitType.areSameType(Power.steam_power, type);
	}
	///// end multi-type overrides /////

}
