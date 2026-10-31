package zone.moddev.mc.steamadvantage.machines;

import cyano.poweradvantage.api.*;
import cyano.poweradvantage.api.simple.TileEntitySimplePowerMachine;
import zone.moddev.mc.poweradvantage.conduitnetwork.ConduitRegistry;
import zone.moddev.mc.steamadvantage.blocks.DrillBitTileEntity;
import zone.moddev.mc.steamadvantage.init.Power;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import java.util.Random;

public class SteamDrillBlock extends GUIBlock implements ITypedConduit {
	private final ConduitType[] type = {Power.steam_power};
	/**
	 * Blockstate property
	 */
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	public SteamDrillBlock(){
		super(Material.PISTON, 0.75f);
		this.registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.DOWN));
	}

	/**
	 * Override of default block behavior
	 */
	@Override
	public void onPlace(final BlockState state, final Level world, final BlockPos coord, final BlockState oldState, final boolean moving) {
		super.onPlace(state, world, coord, oldState, moving);
		ConduitRegistry.getInstance().conduitBlockPlacedEvent(world, coord, Power.steam_power);
	}
	/**
	 * This method is called when the block is removed from the world by an entity.
	 */
	@Override
	public void onRemove(BlockState state, Level w, BlockPos coord, BlockState newState, boolean isMoving){
		if (state.getBlock() != newState.getBlock()) {
			destroyNeighbors(w, coord, state);
			ConduitRegistry.getInstance().conduitBlockRemovedEvent(w, coord, Power.steam_power);
		}
		super.onRemove(state, w, coord, newState, isMoving);
	}
	/**
	 * This method is called when the block is destroyed by an explosion.
	 */
	@Override
	public void wasExploded(Level w, BlockPos coord, Explosion boom){
		destroyNeighbors(w,coord,w.getBlockState(coord));
		ConduitRegistry.getInstance().conduitBlockRemovedEvent(w, coord, Power.steam_power);
		super.wasExploded(w, coord, boom);
	}



	private void destroyNeighbors(Level w, BlockPos coord, BlockState state) {
		if(w.isClientSide) return;
		// destroy connected drill bits
		for(int i = 0; i < 6; i++){
			BlockPos pos = coord.relative(Direction.from3DDataValue(i));
			if(w.getBlockEntity(pos) instanceof DrillBitTileEntity){
				DrillBitTileEntity te = (DrillBitTileEntity)w.getBlockEntity(pos);
				te.destroyLine();
			}
		}
	}
	/**
	 * Creates a TileEntity for this block when the block is placed into the 
	 * world.
	 * @return A new TileEntity instance, probably one that extends 
	 * <b>TileEntitySimplePowerConsumer</b>.
	 */
	@Override
	public PoweredEntity newBlockEntity(net.minecraft.core.BlockPos pos, BlockState state){
		return new SteamDrillTileEntity(pos, state);
	}

	/**
	 * Used to decides whether or not a conduit should connect to this block 
	 * based on its energy type.
	 * @return The type of energy for this block 
	 */
	@Override
	public ConduitType[] getTypes() {
		return type;
	}


	@Override
	public boolean canAcceptConnection(PowerConnectorContext connection){
		return ConduitType.areSameType(Power.steam_power, connection.powerType);
	}
@Override
	public boolean isPowerSink(ConduitType t){
		return true;
	}
	@Override
	public boolean isPowerSource(ConduitType t){
		return false;
	}

	/**
	 * Override of default block behavior
	 */
	/**
	 * Creates the blockstate of this block when it is placed in the world
	 */
	@Override
	public BlockState getStateForPlacement(final BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
	}

	/**
	 * Creates the blockstate of this block when it is placed in the world
	 */
	@Override
	public void setPlacedBy(final Level world, final BlockPos coord, final BlockState bs,
			final LivingEntity placer, final ItemStack srcItemStack) {
		if (srcItemStack.hasCustomHoverName()) {
			final BlockEntity tileEntity = world.getBlockEntity(coord);
			if (tileEntity instanceof PoweredEntity){
				((PoweredEntity)tileEntity).setCustomInventoryName(srcItemStack.getDisplayName().getString());
			}
		}
	}





	/**
	 * Destroys the TileEntity associated with this block when this block 
	 * breaks.
	 */
	/**
	 * This method tells Minecraft whether this block has a signal that can be 
	 * measured by a redstone comparator.
	 * <br><br>
	 * You are encouraged to integrate redstone control into your machines. 
	 * This means outputin a redstone signal with the 
	 * <code>getComparatorInputOverride(...)</code> method and (in your 
	 * TileEntity class) reading the redstone input with the 
	 * <code>World.isBlockPowered(...)</code> method.<br><br>
	 * Typically, machines output a redstone value proportional to the amount of 
	 * stuff in their inventory and are disabled when they receive a redstone 
	 * signal.
	 * @return true if this block can be measured by a redstone comparator, 
	 * false otherwise
	 */
	@Override
	public boolean hasAnalogOutputSignal(BlockState bs){
		return true;
	}

	/**
	 * This method gets the output for a redstone comparator placed against this 
	 * block.
	 * <br><br>
	 * You are encouraged to integrate redstone control into your machines. 
	 * This means outputin a redstone signal with the 
	 * <code>getComparatorInputOverride(...)</code> method and (in your 
	 * TileEntity class) reading the redstone input with the 
	 * <code>World.isBlockPowered(...)</code> method.<br><br>
	 * Typically, machines output a redstone value proportional to the amount of 
	 * stuff in their inventory and are disabled when they receive a redstone 
	 * signal.
	 * @param world World object
	 * @param coord Coordinates of this block
	 * @return a number from 0 to 15
	 */
	@Override
	public int getAnalogOutputSignal(BlockState bs, final Level world, final BlockPos coord){
		BlockEntity te = world.getBlockEntity(coord);
		if(te instanceof SteamDrillTileEntity){
			return ((SteamDrillTileEntity)te).getComparatorOutput();
		} else {
			return 0;
		}
	}

	/**
	 * Creates a blockstate
	 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
		builder.add(FACING);
	}


}
