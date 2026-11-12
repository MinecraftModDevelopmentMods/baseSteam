package zone.moddev.mc.basesteam.resources;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.material.PushReaction;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import zone.moddev.mc.basesteam.blocks.PlatformBlock;
import zone.moddev.mc.basesteam.blocks.SteamTrackBlock;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class NativeBlockPropertiesTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> cases() {
        return Arrays.asList(new Object[][] {
                {"steam_track", -1}, {"platform_height_0", 0}, {"platform_height_1", 1},
                {"platform_height_2", 2}, {"platform_height_3", 3}, {"platform_height_4", 4}
        });
    }

    @Parameterized.Parameter public String name;
    @Parameterized.Parameter(1) public int height;
    private Block block;

    @BeforeClass public static void bootstrapNativeRegistries() { NativeTestBootstrap.start(); }

    @Before public void createNativeBlockAndStateCaches() {
        block = height < 0 ? new SteamTrackBlock() : new PlatformBlock();
        for (BlockState state : block.getStateDefinition().getPossibleStates()) state.initCache();
    }

    @Test public void remainsNonSolid() {
        for (BlockState state : states()) assertFalse(state.toString(), state.canOcclude());
    }

    @Test public void remainsNonOpaque() {
        for (BlockState state : states()) {
            assertFalse(state.toString(), state.isSolidRender());
        }
    }

    @Test public void retainsNativeOpacityForTheLegacyShape() {
        // Native opacity is 1, not 0, for a non-solid block with a full-cube outline.
        int expected = height < 0 || height == 4 ? 1 : 0;
        for (BlockState state : states()) {
            assertEquals(state.toString(), expected, state.getLightDampening());
        }
    }

    @Test public void remainsANonNormalCube() {
        for (BlockState state : states()) {
            assertFalse(state.toString(), state.isRedstoneConductor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }
    }

    @Test public void doesNotConductWeakPowerOnAnyFace() {
        for (BlockState state : states()) {
            for (Direction face : Direction.values()) {
                // The native hook delegates to isRedstoneConductor; these constant shapes need no world lookup.
                assertFalse(state + " face=" + face, state.shouldCheckWeakPower(null, BlockPos.ZERO, face));
            }
        }
    }

    @Test public void retainsExactNativeStrengthAndHarvestLevel() {
        for (BlockState state : states()) {
            assertEquals(height < 0 ? 0.75F : 5.0F,
                    state.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO), 0.0F);
            assertFalse("Native tags must not add a mandatory harvest tool or tier",
                    state.requiresCorrectToolForDrops());
        }
    }

    @Test public void retainsPistonReactionAndHiddenHelperLootPolicy() {
        for (BlockState state : states()) {
            assertFalse("A solid helper must not be washed away",
                    state.canBeReplaced(net.minecraft.world.level.material.Fluids.WATER));
            assertEquals(height < 0 ? PushReaction.BLOCK : PushReaction.NORMAL, state.getPistonPushReaction());
        }
        if (height >= 0) assertTrue("Hidden helpers have no native loot-table key", block.getLootTable().isEmpty());
    }

    @Test public void fullCollisionHelpersNeitherSuffocateNorBlockTheView() {
        for (BlockState state : states()) {
            assertFalse(state.toString(), state.isSuffocating(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
            assertFalse(state.toString(), state.isViewBlocking(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }
    }

    @Test public void preservesOutlineAndCollisionShapes() {
        VoxelShape expected = height < 0 || height == 4 ? Shapes.block()
                : height == 0 ? Shapes.box(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D)
                : Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, height * 0.25D, 1.0D);
        if (height < 0) assertEquals(64, states().size());
        for (BlockState state : states()) {
            assertFalse(state + " outline", Shapes.joinIsNotEmpty(expected,
                    state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO), BooleanOp.NOT_SAME));
            assertFalse(state + " collision", Shapes.joinIsNotEmpty(expected,
                    state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO), BooleanOp.NOT_SAME));
        }
    }

    private Collection<BlockState> states() {
        return height < 0 ? block.getStateDefinition().getPossibleStates()
                : Collections.singletonList(block.defaultBlockState().setValue(PlatformBlock.HEIGHT, height));
    }
}
