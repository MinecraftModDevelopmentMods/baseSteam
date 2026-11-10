package zone.moddev.mc.basesteam.machines;

import zone.moddev.mc.basesteam.util.LegacyFluidHandler;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.PowerRequest;
import cyano.poweradvantage.api.fluid.FluidRequest;
import zone.moddev.mc.basepower.init.Fluids;
import zone.moddev.mc.basesteam.init.Power;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;
import cyano.poweradvantage.api.fluid.FluidStack;
import cyano.poweradvantage.api.fluid.FluidTank;
import cyano.poweradvantage.api.fluid.FluidTankInfo;

import static zone.moddev.mc.basesteam.util.SoundHelper.playSoundAtTileEntity;

@SuppressWarnings("deprecation")
public class GeothermalBoilerTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine implements LegacyFluidHandler{

	
	private final FluidTank tank;
	
	private final ItemStack[] inventory;
	
	private float temperature = 0;
	private final float MAX_TEMPERATURE = 2000;
	private final float HEAT_LOSS_FACTOR = 0.5f / MAX_TEMPERATURE;
	private final float THERMAL_RESISTANCE = 0.1f;
	private final float ACTIVATION_TEMPERATURE = 100;
	private final float HEAT_LOSS_PER_STEAM_UNIT = 24f;
	private final float STEAM_UNITS_PER_TEMPERATURE = 0.001f;
	
	private final int[] dataSyncArray = new int[3];
	
	/** Detached convenience constructor for tests; native factories always pass position and state. */
	public GeothermalBoilerTileEntity() {
		this(net.minecraft.core.BlockPos.ZERO, new GeothermalBoilerBlock().defaultBlockState());
	}

	public GeothermalBoilerTileEntity(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
		super(zone.moddev.mc.basesteam.init.Entities.STEAM_BOILER_GEOTHERMAL, pos, state, new ConduitType[]{Power.steam_power,Fluids.fluidConduit_general}, new float[]{100,1000}, "tile.basesteam.steam_boiler_geothermal.name");
		tank = new FluidTank(FluidTank.BUCKET_VOLUME * 2) {
			@Override
			protected void onContentsChanged() {
				if (GeothermalBoilerTileEntity.this.getLevel() != null
						&& !GeothermalBoilerTileEntity.this.getLevel().isClientSide) {
					GeothermalBoilerTileEntity.this.setChanged();
				}
			}
		};
		inventory = new ItemStack[0];
	}
	
	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	private boolean redstone = true;
	
	private int timeSinceSound = 0;
	
	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			// server-side logic
			if(!redstone && tank.getFluidAmount() > 0 && temperature >= ACTIVATION_TEMPERATURE){
				boilWater();
				// play steam sounds occasionally
				if(getLevel().random.nextInt(100) == 0){
					playSoundAtTileEntity( SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 0.5f, 1f, this);
				}
				if(timeSinceSound > 200){
					if(getTank().getFluidAmount() > 0){
						playSoundAtTileEntity( SoundEvents.LAVA_AMBIENT, SoundSource.AMBIENT, 0.3f, 1f, this);
					}
					timeSinceSound = 0;
				}
				timeSinceSound++;
			} else {
				energyDecay();
			}
		}
	}



	private boolean hasRedstoneSignal() {
		return getLevel().hasNeighborSignal(getBlockPos());
	}


	private void energyDecay() {
		if(getEnergy(Power.steam_power) > 0){
			subtractEnergy(Power.ENERGY_LOST_PER_TICK,Power.steam_power);
		}
	}
	
	private void updateTemperature(){
		float themalInput = 0;
		for(Direction f : Direction.values()){
			BlockPos p = getBlockPos().relative(f);
			if(p.getY() >= 0 && p.getY() < 256){
				themalInput += thermalEnergy(getLevel().getBlockState(p));
			}
		}
		float tSqr = temperature * temperature;
		float delta = THERMAL_RESISTANCE * (themalInput - tSqr * HEAT_LOSS_FACTOR);
		temperature = Math.max(0, Math.min(MAX_TEMPERATURE, temperature + delta) - 1);
	}
	
	static float thermalEnergy(BlockState bs){
		Block b = bs.getBlock();
		if(b == net.minecraft.world.level.block.Blocks.FIRE){
			return 200;
		} else if(bs.getFluidState().getType().isSame(net.minecraft.world.level.material.Fluids.LAVA)) {
			return (bs.getFluidState().getType().getFluidType().getTemperature() - 372) * 0.5f;
		} else {
			return 0;
		}
	}
	

	private void boilWater() {
		if(getTank().getFluidAmount() >= 1 && (getEnergyCapacity(Power.steam_power) - getEnergy(Power.steam_power)) >= 1
				&& temperature >= ACTIVATION_TEMPERATURE){
			float steamDelta = Math.min(STEAM_UNITS_PER_TEMPERATURE * temperature,getEnergyCapacity(Power.steam_power) - getEnergy(Power.steam_power));
			float heatDelta = HEAT_LOSS_PER_STEAM_UNIT * steamDelta;
			int waterDelta = (int)steamDelta + 1;
			getTank().drain(waterDelta, true);
			temperature -= heatDelta;
			addEnergy(steamDelta,Power.steam_power);
		}
	}

	private float oldEnergy = 0;
	private float oldTemp = 0;
	private int oldWater = 0;
	@Override
	public void powerUpdate(){
		super.powerUpdate();
		redstone = hasRedstoneSignal();
		updateTemperature();
		// powerUpdate occurs once every 8 world ticks and is scheduled such that neighboring 
		// machines don't powerUpdate in the same world tick. To reduce network congestion, 
		// I'm doing the synchonization logic here instead of in the tickUpdate method
		boolean updateFlag = false;

		if(oldEnergy != getEnergy(Power.steam_power)){
			oldEnergy = getEnergy(Power.steam_power);
			updateFlag = true;
		}
		if(oldTemp != temperature){
			oldTemp = temperature;
			updateFlag = true;
		}
		if(oldWater != getTank().getFluidAmount()){
			oldWater = getTank().getFluidAmount();
			updateFlag = true;
		}
		
		
		if(updateFlag){
			super.sync();
		}
	}
	
	public float getWaterLevel(){
		return ((float)getTank().getFluidAmount()) / ((float)getTank().getCapacity());
	}
	
	public float getSteamLevel(){
		return this.getEnergy(Power.steam_power) / this.getEnergyCapacity(Power.steam_power);
	}
	
	public float getTemperatureLevel(){
		return temperature / MAX_TEMPERATURE;
	}
	
	public FluidTank getTank(){
		return tank;
	}

	@Override
	protected ItemStack[] getInventory() {
		return inventory;
	}

	@Override
	public int[] getDataFieldArray() {
		return dataSyncArray;
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataSyncArray[0] = Float.floatToRawIntBits(this.getEnergy(Power.steam_power));
		dataSyncArray[1] = this.getTank().getFluidAmount();
		dataSyncArray[2] = Float.floatToIntBits(temperature);
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), Power.steam_power);
		this.getTank().setFluid(new FluidStack(net.minecraft.world.level.material.Fluids.WATER,dataSyncArray[1]));
		this.temperature = Float.intBitsToFloat(dataSyncArray[2]);
	}
	

	/**
	 * Handles data saving and loading
	 * @param tagRoot An NBT tag
	 */
	@Override
    protected void saveAdditional(final CompoundTag tagRoot, HolderLookup.Provider registries) {
		super.saveAdditional(tagRoot, registries);
        CompoundTag tankTag = new CompoundTag();
        this.getTank().writeToNBT(tankTag);
        tagRoot.put("Tank", tankTag);
		if(this.temperature > 0)tagRoot.putFloat("Temperature", this.temperature);
	}
	/**
	 * Handles data saving and loading
	 * @param tagRoot An NBT tag
	 */
	@Override
	protected void loadAdditional(final CompoundTag tagRoot, HolderLookup.Provider registries) {
		super.loadAdditional(tagRoot, registries);
		if (tagRoot.contains("Tank")) {
			CompoundTag tankTag = tagRoot.getCompound("Tank");
			getTank().readFromNBT(tankTag);
			if(tankTag.contains("Empty")){
				// empty the tank if NBT says its empty (not default behavior of Tank.read(...) )
				getTank().setFluid(null);
			}
		}
		if(tagRoot.contains("Temperature")){
			this.temperature = tagRoot.getFloat("Temperature");
		} else if(tagRoot.contains("Tempoerature")){
			this.temperature = tagRoot.getFloat("Tempoerature");
		} else {
			this.temperature = 0;
		}
	}
	
	public int getComparatorOutput() {
		return Math.min(Math.max((int)(15 * temperature / MAX_TEMPERATURE),1),15);
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
	
	/**
	 * Adds "energy" as a fluid to the FluidTank returned by getTank(). This implementation ignores 
	 * all non-fluid energy types.
	 * @param amount amount of energy/fluid to add
	 * @param type the type of energy/fluid being added.
	 * @return The amount that was actually added
	 */
	@Override
	public float addEnergy(float amount, ConduitType type){
		if(Fluids.isFluidType(type)){
			if(Fluids.conduitTypeToFluid(type) == net.minecraft.world.level.material.Fluids.WATER){
				if(this.canFill(null, Fluids.conduitTypeToFluid(type))){
					return this.fill(null, new FluidStack(Fluids.conduitTypeToFluid(type),(int)amount), true);
				} else {
					return 0;
				}
			} else {
				return 0;
			}
		}else{
			return super.addEnergy(amount, type);
		}
	}
    /**
     * Sets the tank contents using the energy API method
	 * @param amount amount of energy/fluid to add
	 * @param type the type of energy/fluid being added.
     */
	@Override
	public void setEnergy(float amount,ConduitType type) {
		if(Fluids.isFluidType(type) && !ConduitType.areSameType(Fluids.fluidConduit_general,type)){
			getTank().setFluid(new FluidStack(Fluids.conduitTypeToFluid(type),(int)amount));
		}else{
			super.setEnergy(amount, type);
		}
	}
	/**
	 * Subtracts "energy" as a fluid to the FluidTank returned by getTank(). This implementation 
	 * ignores all non-fluid energy types.
	 * @param amount amount of energy/fluid to add
	 * @param type the type of energy/fluid being added.
	 * @return The amount that was actually added
	 */
    @Override
	public float subtractEnergy(float amount, ConduitType type){
		if(Fluids.isFluidType(type)){
			if(this.canDrain(null, Fluids.conduitTypeToFluid(type))){
				return this.drain(null, new FluidStack(Fluids.conduitTypeToFluid(type),(int)amount), true).amount;
			} else {
				return 0;
			}
		} else {
			return super.subtractEnergy(amount, type);
		}
	}
	
	@Override
	public PowerRequest getPowerRequest(ConduitType offer) {
		if(Fluids.conduitTypeToFluid(offer) == net.minecraft.world.level.material.Fluids.WATER){
			PowerRequest request = new FluidRequest(FluidRequest.MEDIUM_PRIORITY+1,
					(getTank().getCapacity() - getTank().getFluidAmount()),
					this);
			return request;
		} else {
			return PowerRequest.REQUEST_NOTHING;
		}
	}

	///// end multi-type overrides /////

	
	
	///// IFluidHandler /////

	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public int fill(Direction face, FluidStack fluid, boolean forReal) {
		if (fluid == null || fluid.amount <= 0 || !canFill(face, fluid.getFluid())
				|| getTank().getFluidAmount() >= getTank().getCapacity()) return 0;
		if(getTank().getFluidAmount() <= 0 || getTank().getFluid().getFluid().equals(fluid.getFluid())){
			return getTank().fill(fluid, forReal);
		} else {
			return 0;
		}
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public FluidStack drain(Direction face, FluidStack fluid, boolean forReal) {
		if (fluid == null || fluid.amount <= 0) return null;
		if(getTank().getFluidAmount() > 0 && getTank().getFluid().getFluid().equals(fluid.getFluid())){
			return getTank().drain(fluid.amount,forReal);
		} else {
			return null;
		}
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param amount The amount of fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public FluidStack drain(Direction face, int amount, boolean forReal) {
		if (amount <= 0) return null;
		if(getTank().getFluidAmount() > 0 ){
			return getTank().drain(amount,forReal);
		} else {
			return null;
		}
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 */
	@Override
	public boolean canFill(Direction face, Fluid fluid) {
		if(fluid != net.minecraft.world.level.material.Fluids.WATER) return false;
		if(getTank().getFluid() == null) return true;
		return getTank().getFluidAmount() <= getTank().getCapacity() && fluid.equals(getTank().getFluid().getFluid());
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 */
	@Override
	public boolean canDrain(Direction face, Fluid fluid) {
		if(getTank().getFluid() == null) return false;
		return getTank().getFluidAmount() > 0 && fluid.equals(getTank().getFluid().getFluid());
	}
	
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @return array of FluidTankInfo describing all of the FluidTanks
	 */
	@Override
	public FluidTankInfo[] getTankInfo(Direction face) {
		FluidTankInfo[] arr = new FluidTankInfo[1];
		arr[0] = getTank().getInfo();
		return arr;
	}

	///// end of IFluidHandler methods /////
	
	@Override
	public boolean canPlaceItem(final int slot, final ItemStack item) {
		return false;
	}
}
