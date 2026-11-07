package zone.moddev.mc.basesteam.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import zone.moddev.mc.basesteam.init.Power;

/** Hidden moving drill helper. It deliberately has no item form or drops. */
public class DrillBitBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.box(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);

    public DrillBitBlock() {
        super(Block.Properties.of(Material.METAL).strength(5.0F, 2000.0F).noOcclusion());
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DrillBitTileEntity(pos, state); }

    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        return type == zone.moddev.mc.basesteam.init.Entities.DRILLBIT
                ? (world, pos, blockState, tile) -> ((DrillBitTileEntity) tile).tick() : null;
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public List<ItemStack> getDrops(BlockState state, LootContext.Builder context) { return Collections.emptyList(); }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity victim) {
        victim.hurt(Power.machine_damage, 2.0F);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (!world.isClientSide && tile instanceof DrillBitTileEntity) {
                ((DrillBitTileEntity) tile).destroyLine(false);
            }
        }
        super.onRemove(state, world, pos, newState, moving);
    }

    @Override
    public void wasExploded(Level world, BlockPos pos, Explosion explosion) {
        destroyNeighbors(world, pos);
        super.wasExploded(world, pos, explosion);
    }

    private void destroyNeighbors(Level world, BlockPos pos) {
        if (world.isClientSide) return;
        for (Direction side : Direction.values()) {
            BlockEntity tile = world.getBlockEntity(pos.relative(side));
            if (tile instanceof DrillBitTileEntity) ((DrillBitTileEntity) tile).destroyLine();
        }
    }
}
