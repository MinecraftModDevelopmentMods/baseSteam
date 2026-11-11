package zone.moddev.mc.basesteam.machines;

import cyano.poweradvantage.api.ConduitType;
import zone.moddev.mc.basesteam.init.Blocks;
import zone.moddev.mc.basesteam.init.Power;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import java.util.List;

import static zone.moddev.mc.basesteam.util.SoundHelper.playSoundAtTileEntity;

public class SteamElevatorTileEntity extends cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine{

	public static final float STEAM_PER_ELEVATOR_MOVE = 32f;
	public static final int MAX_RANGE = 16;
	private static final double smallAmount = 0.00390625;

	private final ItemStack[] inventory = new ItemStack[0]; // slot 0 is input, other slots are output
	private final int[] dataSyncArray = new int[1];
	
	
	private boolean above = false;
	
	/** Detached convenience constructor for tests; native factories always pass position and state. */
	public SteamElevatorTileEntity() {
		this(net.minecraft.core.BlockPos.ZERO, new SteamElevatorBlock().defaultBlockState());
	}

	public SteamElevatorTileEntity(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
		super(zone.moddev.mc.basesteam.init.Entities.STEAM_ELEVATOR, pos, state, Power.steam_power, STEAM_PER_ELEVATOR_MOVE*2, "tile.basesteam.steam_elevator.name");
	}

	private boolean redstone = true;
	
	@Override
	public void tickUpdate(boolean isServerWorld) {
		if(isServerWorld){
			
			// controlled by redstone
			if(redstone != above){
				if(redstone) {
					above = moveUp();
				} else {
					above = !moveDown();
				}
			}
		}
	}

	private boolean moveUp(){
		if(this.getEnergy(Power.steam_power) < STEAM_PER_ELEVATOR_MOVE) return false;
		this.subtractEnergy(STEAM_PER_ELEVATOR_MOVE, Power.steam_power);
		playSoundAtTileEntity( SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 0.5f, 1f, this);
		playSoundAtTileEntity( SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5f, 1f, this);
		int dist = 0;
		BlockPos p = this.getBlockPos().above();
		while(p.getY() < 256 && dist < MAX_RANGE && getLevel().isEmptyBlock(p)){
			dist++;
			p = p.above();
		}
		BlockPos topPos = p.below(2);
		p = p.below(3); // make room for character to stand in
		boolean top = true;
		for(; p.getY() > getBlockPos().getY(); p = p.below()){
			if(top){
				// platform top
				getLevel().setBlockAndUpdate(p, Blocks.steam_elevator_platforms[4]);
				top = false;
			} else {
				// shaft
				getLevel().setBlockAndUpdate(p, Blocks.steam_elevator_platforms[0]);
			}
		}
		// move people
		List<Entity> passengers = getLevel().getEntitiesOfClass(Entity.class,
				new AABB(getBlockPos().getX(),getBlockPos().getY()+1,getBlockPos().getZ(),
						getBlockPos().getX()+1, topPos.getY(), getBlockPos().getZ()+1));

		for(Entity e : passengers){
			e.teleportTo(e.getX(),topPos.getY()+smallAmount, e.getZ());
			e.fallDistance = 0;
		}
		return true;
	}

	private boolean moveDown(){
		if(this.getEnergy(Power.steam_power) < STEAM_PER_ELEVATOR_MOVE) return false;
		this.subtractEnergy(STEAM_PER_ELEVATOR_MOVE, Power.steam_power);
		playSoundAtTileEntity( SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 0.5f, 1f, this);
		playSoundAtTileEntity( SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5f, 1f, this);
		// scan up the piston and clear it
		BlockPos destination = this.getBlockPos().above();
		BlockPos p = destination;
		while(getLevel().getBlockState(p).getBlock() == Blocks.steam_elevator_platform){
			p = p.above();
		}
		// remove piston
		BlockPos i = p.below();
		for(; i.getY() > getBlockPos().getY(); i = i.below()){
			getLevel().removeBlock(i, false);
		}
		// move people
		List<Entity> passengers = getLevel().getEntitiesOfClass(Entity.class,
				new AABB(p.getX(),p.getY(),p.getZ(), p.getX()+1, p.getY()+2, p.getZ()+1));
		
		for(Entity e : passengers){
			e.teleportTo(e.getX(), destination.getY()+smallAmount,e.getZ());
			e.fallDistance = 0;
		}
		return true;
	}
	

	private float oldSteam = 0;
	
	@Override
	public void powerUpdate(){
		super.powerUpdate();
		
		redstone = hasRedstoneSignal();
		
		if(oldSteam != this.getEnergy(Power.steam_power)){
			this.sync();
			oldSteam = this.getEnergy(Power.steam_power);
		}
	}

	

	
	private boolean hasRedstoneSignal() {
		return getLevel().hasNeighborSignal(getBlockPos());
	}
	
	
	public float getSteamLevel(){
		return this.getEnergy(Power.steam_power) / this.getEnergyCapacity(Power.steam_power);
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
		if(above){
			dataSyncArray[0] = 1;
		} else {
			dataSyncArray[0] = 0;
		}
	}

	@Override
	public void onDataFieldUpdate() {
		this.above = dataSyncArray[0] != 0;
	}

	

	
	@Override
	protected void saveAdditional(ValueOutput tagRoot){
		super.saveAdditional(tagRoot);
		tagRoot.putBoolean("up",above);
	}
	
	@Override
	protected void loadAdditional(ValueInput tagRoot){
		super.loadAdditional(tagRoot);
		if(tagRoot.read("up", com.mojang.serialization.Codec.BOOL).isPresent()){
			above = tagRoot.getBooleanOr("up", false);
		}
	}



	public int getComparatorOutput() {
		if(above) return 15;
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

	@Override
	public boolean stillValid(Player player) {
		return true;
	}
}
