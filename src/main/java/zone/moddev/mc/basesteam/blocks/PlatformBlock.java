package zone.moddev.mc.basesteam.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import zone.moddev.mc.basesteam.init.Blocks;

/** Hidden elevator platform segments. HEIGHT preserves the legacy metadata values 0-4. */
public class PlatformBlock extends Block {
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", 0, 4);
    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Shapes.box(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D),
            Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D),
            Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.50D, 1.0D),
            Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D),
            Shapes.block()
    };

    public PlatformBlock() {
        super(Block.Properties.of(Material.METAL).strength(5.0F, 2000.0F).noOcclusion()
                .isRedstoneConductor((state, world, pos) -> false)
                .isSuffocating((state, world, pos) -> false)
                .isViewBlocking((state, world, pos) -> false));
        registerDefaultState(getStateDefinition().any().setValue(HEIGHT, 1));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(HEIGHT); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return SHAPES[state.getValue(HEIGHT)]; }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return SHAPES[state.getValue(HEIGHT)]; }
    @Override public List<ItemStack> getDrops(BlockState state, LootContext.Builder context) { return Collections.emptyList(); }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!canBlockStay(world, pos)) world.destroyBlock(pos, true);
    }

    public boolean canBlockStay(Level world, BlockPos pos) {
        Block below = world.getBlockState(pos.below()).getBlock();
        return below == this || below == Blocks.steam_elevator_platform || below == Blocks.steam_elevator;
    }
}
