package zone.moddev.mc.basesteam.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
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

/** Hidden pipe shaft placed beneath a steam pump. */
public class PumpPipeBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.box(0.0D, 0.25D, 0.0D, 0.75D, 1.0D, 1.0D);

    public PumpPipeBlock() {
        super(Block.Properties.of(Material.METAL).strength(5.0F, 10.0F).noOcclusion());
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public List<ItemStack> getDrops(BlockState state, LootContext.Builder context) { return Collections.emptyList(); }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moving) {
        if (state.getBlock() != newState.getBlock()) destroyShaft(world, pos);
        super.onRemove(state, world, pos, newState, moving);
    }

    @Override
    public void wasExploded(Level world, BlockPos pos, Explosion explosion) {
        destroyShaft(world, pos);
        super.wasExploded(world, pos, explosion);
    }

    private void destroyShaft(Level world, BlockPos pos) {
        if (world.isClientSide) return;
        BlockPos cursor = pos.below();
        while (cursor.getY() > 0 && world.getBlockState(cursor).getBlock() == this) {
            world.destroyBlock(cursor, false);
            cursor = cursor.below();
        }
    }
}
