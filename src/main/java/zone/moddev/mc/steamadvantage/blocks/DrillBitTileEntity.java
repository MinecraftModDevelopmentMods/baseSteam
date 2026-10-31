package zone.moddev.mc.steamadvantage.blocks;

import zone.moddev.mc.steamadvantage.init.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;

public class DrillBitTileEntity extends BlockEntity{

	public final static float ROTATION_PER_TICK = 360f / 20f; // in degrees, 20 ticks per revolution
	public float rotation = 0;
	private Direction direction = Direction.DOWN;
	
	public DrillBitTileEntity() {
		this(BlockPos.ZERO, new DrillBitBlock().defaultBlockState());
	}

	public DrillBitTileEntity(BlockPos pos, BlockState state) {
		super(zone.moddev.mc.steamadvantage.init.Entities.DRILLBIT, pos, state);
	}
	
	public void tick(){
		if(hasLevel() && getLevel().isClientSide){
			rotation = ROTATION_PER_TICK * (getLevel().getGameTime() % 20);
		}
	}
	public void setDirection(Direction dir){
		if(dir == null || dir == this.direction) return;
		this.direction = dir;
		syncDirection();
	}
	
	public Direction.Axis getDirection(){
		return direction.getAxis();
	}
	
	public static void createDrillBitBlock(Level w, BlockPos coord, Direction dir){
		w.setBlockAndUpdate(coord, zone.moddev.mc.steamadvantage.init.Blocks.drillbit.defaultBlockState());
		BlockEntity tile = w.getBlockEntity(coord);
		if(tile instanceof DrillBitTileEntity) ((DrillBitTileEntity) tile).setDirection(dir);
	}
	
	/**
	 * Destroys all drillbits connected to this one
	 */
	public void destroyLine(){
		destroyLine(true);
	}

	public void destroyLine(boolean removeOrigin){
		if(!hasLevel() || getLevel().isClientSide) return;
		List<BlockPos> line = new ArrayList<>();
		line.add(getBlockPos());
		collectLine(line, direction);
		collectLine(line, direction.getOpposite());
		// Detach all helpers before block replacement callbacks can re-enter this line.
		for(BlockPos coord : line) getLevel().removeBlockEntity(coord);
		for(BlockPos coord : line) {
			if(removeOrigin || !coord.equals(getBlockPos())) getLevel().removeBlock(coord, false);
		}
	}
	/**
	 * destroys upstream drillbit
	 */
	private void collectLine(List<BlockPos> line, Direction f){
		BlockPos coord = this.getBlockPos().relative(f);
		while(getLevel().hasChunkAt(coord) && getLevel().getBlockState(coord).getBlock() == Blocks.drillbit){
			line.add(coord);
			coord = coord.relative(f);
		}
	}
	
	@Override
	public CompoundTag save(CompoundTag root){
		super.save(root);
		writeDirectionToNBT(root);
		return root;
	}
	

	@Override
	public void load(CompoundTag root){
		super.load(root);
		readDirectionFromNBT(root);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return save(new CompoundTag());
	}

	/**
	 * Turns the data field NBT into a network packet
	 */
	@Override 
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		CompoundTag nbtTag = new CompoundTag();
		writeDirectionToNBT(nbtTag);
		return new ClientboundBlockEntityDataPacket(this.getBlockPos(), 0, nbtTag);
	}
	/**
	 * Receives the network packet made by <code>getDescriptionPacket()</code>
	 */
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
		readDirectionFromNBT(packet.getTag());
	}

	private void writeDirectionToNBT(CompoundTag root) {
		root.putByte("dir", (byte)direction.get3DDataValue());
	}

	private void readDirectionFromNBT(CompoundTag root) {
		if(root.contains("dir")){
			this.direction = decodeDirection(root.getByte("dir"));
		} else if(root.contains("d")){
			this.direction = decodeDirection(root.getByte("d"));
		}
	}

	private static Direction decodeDirection(int index) {
		return index >= 0 && index < Direction.values().length ? Direction.from3DDataValue(index) : Direction.DOWN;
	}

	private void syncDirection() {
		if(hasLevel() && !getLevel().isClientSide) {
			setChanged();
			BlockState state = getLevel().getBlockState(getBlockPos());
			getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
		}
	}
}
