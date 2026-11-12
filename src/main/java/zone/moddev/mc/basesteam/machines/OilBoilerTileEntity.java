package zone.moddev.mc.basesteam.machines;

import zone.moddev.mc.basepower.util.FluidIdHelper;

import zone.moddev.mc.basesteam.util.LegacyFluidHandler;

import cyano.poweradvantage.api.ConduitType;
import cyano.poweradvantage.api.FluidCategoryRegistry;
import cyano.poweradvantage.api.PowerRequest;
import cyano.poweradvantage.api.fluid.FluidRequest;
import zone.moddev.mc.basepower.init.Fluids;
import zone.moddev.mc.basepower.registry.FuelRegistry;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.init.Power;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import cyano.poweradvantage.api.fluid.FluidStack;
import cyano.poweradvantage.api.fluid.FluidTank;
import cyano.poweradvantage.api.fluid.FluidTankInfo;

import java.util.HashMap;
import java.util.Map;

import static zone.moddev.mc.basesteam.util.SoundHelper.playSoundAtTileEntity;

@SuppressWarnings("deprecation")
public class OilBoilerTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine implements LegacyFluidHandler{


	public static final int FLUID_BURN_ALIQUOT = FluidTank.BUCKET_VOLUME / 10; // number of fluid units burned at a time

	private final FluidTank waterTank;
	private final FluidTank fuelTank;
	private static final Map<Fluid,Float> flammibilityCache = new HashMap<>();
	private int burnTime = 0;
	private int totalBurnTime = 0;

	private final int[] dataSyncArray = new int[6];

	/** Detached convenience constructor for tests; native factories always pass position and state. */
	public OilBoilerTileEntity() {
		this(net.minecraft.core.BlockPos.ZERO, new OilBoilerBlock().defaultBlockState());
	}

	public OilBoilerTileEntity(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
		super(zone.moddev.mc.basesteam.init.Entities.STEAM_BOILER_OIL, pos, state, new ConduitType[]{Power.steam_power, Fluids.fluidConduit_general}, new float[]{1000,1000}, "tile.basesteam.steam_boiler_oil.name");
		waterTank = new FluidTank(FluidTank.BUCKET_VOLUME * 4);
		fuelTank = new FluidTank(FluidTank.BUCKET_VOLUME * 4);
	}

	private boolean redstone = true;

	private int timeSinceSound = 0;

	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			// server-side logic
			if(burnTime > 0){
				burnTime--;
				boilWater();
				// play steam sounds occasionally
				if(getLevel().random.nextInt(100) == 0){
					playSoundAtTileEntity( SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 0.5f, 1f, this);
				}
				if(timeSinceSound > 200){
					if(getWaterTank().getFluidAmount() > 0){
						playSoundAtTileEntity( SoundEvents.LAVA_AMBIENT, SoundSource.AMBIENT, 0.3f, 1f, this);
					}
					timeSinceSound = 0;
				}
				timeSinceSound++;
			} else {
				if(!hasRedstoneSignal() && getFuelTank().getFluidAmount() > 0 && getWaterTank().getFluidAmount() > 0){
					int volume = Math.min(FLUID_BURN_ALIQUOT, getFuelTank().getFluidAmount());
					int fuel = getFuelBurnTime(getFuelTank().getFluid().getFluid(), volume);
					if( fuel > 0 && this.getWaterTank().getFluidAmount() > 0){
						burnTime = fuel;
						totalBurnTime = fuel;
						decrementFuel(volume);
					}
				}
				energyDecay();
			}
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	private boolean hasRedstoneSignal() {
		return getLevel().hasNeighborSignal(getBlockPos());
	}


	private void energyDecay() {
		if(getEnergy(Power.steam_power) > 0){
			subtractEnergy(Power.ENERGY_LOST_PER_TICK,Power.steam_power);
		}
	}


	private int getFuelBurnTime(Fluid fluid, int amount) {
		return (int)(getBurnTimePerBucketFor(fluid) * amount / FluidTank.BUCKET_VOLUME);
	}

	private static Float getBurnTimePerBucketFor(Fluid fluid){
		if(fluid == null) return 0f;
		if(flammibilityCache.containsKey(fluid)){
			return flammibilityCache.get(fluid)*1000;
		} else {
			// first, check configured overrides
			Float fuelPerBucket = getConfiguredBurnTimePerBucketFor(fluid);
			if(fuelPerBucket != null){
				flammibilityCache.put(fluid,0.001F*fuelPerBucket);
				return fuelPerBucket;
			}
			// second, check universal bucket fuel registry
			int vol = FluidTank.BUCKET_VOLUME;
			ItemStack bucket = fluid.getBucket() == net.minecraft.world.item.Items.AIR
					? ItemStack.EMPTY : new ItemStack(fluid.getBucket());
			if(bucket.isEmpty()){
				flammibilityCache.put(fluid,0f);
				return 0f;
			}
			Float burnTicksPerAmount = (float) FuelRegistry.getActualBurntimeForItem(bucket) / (float) vol;
			if(burnTicksPerAmount > 0){
				flammibilityCache.put(fluid,burnTicksPerAmount);
				return 1000*burnTicksPerAmount;
			}

			// it just isn't a fuel
			flammibilityCache.put(fluid,0f);
		}
		// no containers (buckets) for this fluid
		return 0f;


	}

	private static Float getConfiguredBurnTimePerBucketFor(Fluid fluid) {
		Identifier id = net.minecraftforge.registries.ForgeRegistries.FLUIDS.getKey(fluid);
		if (id != null) {
			if (SteamAdvantage.fluidBurnValues.containsKey(id.toString())) return SteamAdvantage.fluidBurnValues.get(id.toString());
			if (SteamAdvantage.fluidBurnValues.containsKey(id.getPath())) return SteamAdvantage.fluidBurnValues.get(id.getPath());
		}
		if (FluidCategoryRegistry.matches(FluidCategoryRegistry.CRUDE_OIL, fluid)) {
			for (String alias : FluidCategoryRegistry.getAliases(FluidCategoryRegistry.CRUDE_OIL)) {
				Float configured = SteamAdvantage.fluidBurnValues.get(alias);
				if (configured != null) return configured;
			}
		}
		return null;
	}


	private void decrementFuel(int vol) {
		this.getFuelTank().drain(vol, true);
	}


	private void boilWater() {
		if(getWaterTank().getFluidAmount() >= 1 && (getEnergyCapacity(Power.steam_power) - getEnergy(Power.steam_power)) >= 1){
			getWaterTank().drain(1, true);
			addEnergy(1.25f,Power.steam_power); // oil-burning furnace is 25% more efficient than normal furnace
		}
	}

	private float oldEnergy = 0;
	private int oldBurnTime = 0;
	private int oldWater = 0;
	@Override
	public void powerUpdate(){
		super.powerUpdate();
		// powerUpdate occurs once every 8 world ticks and is scheduled such that neighboring 
		// machines don't powerUpdate in the same world tick. To reduce network congestion, 
		// I'm doing the synchonization logic here instead of in the tickUpdate method
		boolean updateFlag = false;

		if(oldEnergy != getEnergy(Power.steam_power)){
			oldEnergy = getEnergy(Power.steam_power);
			updateFlag = true;
		}
		if(oldBurnTime != burnTime){
			oldBurnTime = burnTime;
			updateFlag = true;
		}
		if(oldWater != getWaterTank().getFluidAmount()){
			oldWater = getWaterTank().getFluidAmount();
			updateFlag = true;
		}

		redstone = hasRedstoneSignal();

		if(updateFlag){
			super.sync();
		}
	}


	public float getSteamLevel(){
		return this.getEnergy(Power.steam_power) / this.getEnergyCapacity(Power.steam_power);
	}

	public float getBurnLevel(){
		if(burnTime == 0){
			return 0;
		} else if (totalBurnTime == 0){
			return 1;
		}
		return Math.max(0,Math.min(1,((float)burnTime)/((float)totalBurnTime)));
	}

	public FluidTank getWaterTank(){
		return waterTank;
	}
	public FluidTank getFuelTank(){
		return fuelTank;
	}

	@Override
	protected ItemStack[] getInventory() {
		return null;
	}

	@Override
	public int[] getDataFieldArray() {
		return dataSyncArray;
	}

	@Override
	public void prepareDataFieldsForSync() {
		dataSyncArray[0] = Float.floatToRawIntBits(this.getEnergy(Power.steam_power));
		dataSyncArray[1] = this.getWaterTank().getFluidAmount();
		dataSyncArray[2] = this.burnTime;
		dataSyncArray[3] = this.totalBurnTime;
		dataSyncArray[4] = this.getFuelTank().getFluidAmount();
		dataSyncArray[5] = (getFuelTank().getFluid() != null && getFuelTank().getFluid().getFluid() != null 
				? FluidIdHelper.getFluidId(getFuelTank().getFluid().getFluid())
						: FluidIdHelper.getFluidId(net.minecraft.world.level.material.Fluids.WATER));
	}

	@Override
	public void onDataFieldUpdate() {
		this.setEnergy(Float.intBitsToFloat(dataSyncArray[0]), Power.steam_power);
		this.getWaterTank().setFluid(new FluidStack(net.minecraft.world.level.material.Fluids.WATER,dataSyncArray[1]));
		this.burnTime = dataSyncArray[2];
		this.totalBurnTime = dataSyncArray[3];
		this.getFuelTank().setFluid(new FluidStack(FluidIdHelper.getFluid(dataSyncArray[5]),dataSyncArray[4]));
	}


	/**
	 * Handles data saving and loading
	 * @param tagRoot An NBT tag
	 */
	@Override
	protected void saveAdditional(ValueOutput tagRoot) {
		super.saveAdditional(tagRoot);
		CompoundTag tankTag = new CompoundTag();
		this.getWaterTank().writeToNBT(tankTag);
		tagRoot.store("WaterTank", CompoundTag.CODEC, tankTag);
		CompoundTag tankTag2 = new CompoundTag();
		this.getFuelTank().writeToNBT(tankTag2);
		tagRoot.store("FuelTank", CompoundTag.CODEC, tankTag2);
		if(this.burnTime > 0)tagRoot.putInt("BurnTime", this.burnTime);
		if(this.totalBurnTime > 0)tagRoot.putInt("BurnTimeTotal", this.totalBurnTime);
	}
	/**
	 * Handles data saving and loading
	 * @param tagRoot An NBT tag
	 */
	@Override
	protected void loadAdditional(ValueInput tagRoot) {
		super.loadAdditional(tagRoot);
		if (tagRoot.read("WaterTank", CompoundTag.CODEC).isPresent()) {
			CompoundTag tankTag = tagRoot.read("WaterTank", CompoundTag.CODEC).orElseGet(CompoundTag::new);
			getWaterTank().readFromNBT(tankTag);
			if(tankTag.contains("Empty")){
				// empty the tank if NBT says its empty (not default behavior of Tank.read(...) )
				getWaterTank().setFluid(null);
			}
		}
		if (tagRoot.read("FuelTank", CompoundTag.CODEC).isPresent()) {
			CompoundTag tankTag = tagRoot.read("FuelTank", CompoundTag.CODEC).orElseGet(CompoundTag::new);
			getFuelTank().readFromNBT(tankTag);
			if(tankTag.contains("Empty")){
				// empty the tank if NBT says its empty (not default behavior of Tank.read(...) )
				getFuelTank().setFluid(null);
			}
		}
		if(tagRoot.read("BurnTime", com.mojang.serialization.Codec.INT).isPresent()){
			this.burnTime = tagRoot.getIntOr("BurnTime", 0);
		} else {
			this.burnTime = 0;
		}
		if(tagRoot.read("BurnTimeTotal", com.mojang.serialization.Codec.INT).isPresent()){
			this.totalBurnTime = tagRoot.getIntOr("BurnTimeTotal", 0);
		} else {
			this.totalBurnTime = 0;
		}
	}

	public int getComparatorOutput() {
		return 15 * this.getFuelTank().getFluidAmount() / this.getFuelTank().getCapacity();
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
			if(this.canFill(null, Fluids.conduitTypeToFluid(type))){
				return this.fill(null, new FluidStack(Fluids.conduitTypeToFluid(type),(int)amount), true);
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
			Fluid f = Fluids.conduitTypeToFluid(type);
			if(f == net.minecraft.world.level.material.Fluids.WATER){
				this.getWaterTank().setFluid(new FluidStack(f,(int)amount));
			}else{
				this.getFuelTank().setFluid(new FluidStack(f,(int)amount));
			}
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
				FluidStack drained = this.drain(null, new FluidStack(Fluids.conduitTypeToFluid(type),(int)amount), true);
				return drained == null ? 0 : -1 * drained.amount;
			} else {
				return 0;
			}
		}else{
			return super.subtractEnergy(amount, type);
		}
	}

	@Override
	public PowerRequest getPowerRequest(ConduitType offer) {
		if(this.redstone) return PowerRequest.REQUEST_NOTHING;
		if(Fluids.conduitTypeToFluid(offer) == net.minecraft.world.level.material.Fluids.WATER){
			PowerRequest request = new FluidRequest(FluidRequest.MEDIUM_PRIORITY+1,
					(getWaterTank().getCapacity() - getWaterTank().getFluidAmount()),
					this);
			return request;
		} else if(getBurnTimePerBucketFor(Fluids.conduitTypeToFluid(offer)) > 0 
				&& (getFuelTank().getFluidAmount() <= 0 || getFuelTank().getFluid().getFluid().equals(Fluids.conduitTypeToFluid(offer)))){
			PowerRequest request = new FluidRequest(FluidRequest.MEDIUM_PRIORITY+1,
					(getFuelTank().getCapacity() - getFuelTank().getFluidAmount()),
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
		if(fluid == null) return 0;
		if(fluid.getFluid() == net.minecraft.world.level.material.Fluids.WATER){
			if(getWaterTank().getFluidAmount() <= 0 || getWaterTank().getFluid().getFluid().equals(fluid.getFluid())){
				return getWaterTank().fill(fluid, forReal);
			}
		} else if(getBurnTimePerBucketFor(fluid.getFluid()) > 0){
			if(getFuelTank().getFluidAmount() <= 0 || getFuelTank().getFluid().getFluid().equals(fluid.getFluid())){
				return getFuelTank().fill(fluid, forReal);
			}
		}
		return 0;
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 * @param forReal if true, then the fluid in the tank will change
	 */
	@Override
	public FluidStack drain(Direction face, FluidStack fluid, boolean forReal) {
		if(fluid == null) return null;
		if(fluid.getFluid() == net.minecraft.world.level.material.Fluids.WATER && getWaterTank().getFluidAmount() > 0 && getWaterTank().getFluid().getFluid().equals(net.minecraft.world.level.material.Fluids.WATER)){
			return getWaterTank().drain(fluid.amount,forReal);
		} else if(getFuelTank().getFluidAmount() > 0 && getFuelTank().getFluid().getFluid().equals(fluid.getFluid())){
			return getFuelTank().drain(fluid.amount,forReal);
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
		if(getWaterTank().getFluidAmount() > 0 ){
			return getWaterTank().drain(amount,forReal);
		} else if(getFuelTank().getFluidAmount() > 0 ){
			return getFuelTank().drain(amount,forReal);
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
		if(fluid == net.minecraft.world.level.material.Fluids.WATER) {
			if(getWaterTank().getFluidAmount() <= 0) return true;
			return getWaterTank().getFluidAmount() <= getWaterTank().getCapacity() && fluid.equals(getWaterTank().getFluid().getFluid());
		} else if(getBurnTimePerBucketFor(fluid) > 0){
			if(getFuelTank().getFluidAmount() <= 0) return true;
			return getFuelTank().getFluidAmount() <= getFuelTank().getCapacity() && fluid.equals(getFuelTank().getFluid().getFluid());
		}
		return false;
	}
	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @param fluid The fluid being added/removed
	 */
	@Override
	public boolean canDrain(Direction face, Fluid fluid) {
		if(fluid == null) return false;
		if(fluid == net.minecraft.world.level.material.Fluids.WATER) {
			return getWaterTank().getFluidAmount() > 0 && fluid.equals(getWaterTank().getFluid().getFluid());
		} else {
			return getFuelTank().getFluidAmount() > 0 && fluid.equals(getFuelTank().getFluid().getFluid());
		}
	}

	/**
	 * Implementation of IFluidHandler
	 * @param face Face of the block being polled
	 * @return array of FluidTankInfo describing all of the FluidTanks
	 */
	@Override
	public FluidTankInfo[] getTankInfo(Direction face) {
		FluidTankInfo[] arr = new FluidTankInfo[2];
		arr[0] = getWaterTank().getInfo();
		arr[1] = getFuelTank().getInfo();
		return arr;
	}

	///// end of IFluidHandler methods /////

	@Override
	public boolean canPlaceItem(final int slot, final ItemStack item) {
		return false; // no inventory
	}
}
