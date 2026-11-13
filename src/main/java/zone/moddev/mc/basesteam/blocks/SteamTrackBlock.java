package zone.moddev.mc.basesteam.blocks;

import cyano.poweradvantage.api.simple.BlockSimplePowerConduit;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.BlockGetter;
import zone.moddev.mc.basesteam.init.Power;

public class SteamTrackBlock extends BlockSimplePowerConduit {
    public SteamTrackBlock() {
        super(SteamBlockProperties.pistonLike("steam_track").noOcclusion()
                        .isRedstoneConductor((state, world, pos) -> false)
                        .isSuffocating((state, world, pos) -> false)
                        .isViewBlocking((state, world, pos, nearPlane) -> false),
                2.0F / 16.0F, Power.steam_power);
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return Shapes.block(); }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return Shapes.block(); }
}
