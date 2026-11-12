package zone.moddev.mc.basesteam.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import zone.moddev.mc.basesteam.init.Power;

/** Hidden moving drill helper. Its item stays hidden and never drops. */
public class DrillBitBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.box(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);

    public DrillBitBlock() {
        super(SteamBlockProperties.metalHelper("drillbit", 2000.0F));
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

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity victim,
            net.minecraft.world.entity.InsideBlockEffectApplier effects, boolean moving) {
        if (world instanceof net.minecraft.server.level.ServerLevel server) {
            victim.hurtServer(server, Power.machineDamage(world), 2.0F);
        }
    }

    @Override
    public void wasExploded(net.minecraft.server.level.ServerLevel world, BlockPos pos, Explosion explosion) {
        destroyNeighbors(world, pos);
        super.wasExploded(world, pos, explosion);
    }

    private void destroyNeighbors(Level world, BlockPos pos) {
        if (world.isClientSide()) return;
        for (Direction side : Direction.values()) {
            BlockEntity tile = world.getBlockEntity(pos.relative(side));
            if (tile instanceof DrillBitTileEntity) ((DrillBitTileEntity) tile).destroyLine();
        }
    }
}
