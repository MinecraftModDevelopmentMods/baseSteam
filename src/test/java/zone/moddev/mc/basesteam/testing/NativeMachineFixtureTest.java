package zone.moddev.mc.basesteam.testing;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import zone.moddev.mc.basesteam.init.Entities;
import static org.junit.Assert.*;

public class NativeMachineFixtureTest {
    @Test public void fixtureValidatesAllActualBlocksAndRestoresEveryFieldAfterFailure() throws Throwable {
        NativeTestBootstrap.start();
        Map<Field, Object> previous = new LinkedHashMap<>();
        for (Field field : Entities.class.getFields()) {
            if (field.getType() == BlockEntityType.class) previous.put(field, field.get(null));
        }
        assertEquals(12, previous.size());
        AssertionError sentinel = new AssertionError("fixture failure witness");
        Statement probe = new Statement() {
            @Override public void evaluate() {
                for (var block : NativeMachineFixture.blocks()) {
                    var tile = ((EntityBlock) block).newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
                    assertNotNull(tile);
                    assertTrue(tile.getType().isValid(tile.getBlockState()));
                }
                throw sentinel;
            }
        };
        try {
            new NativeMachineFixture().apply(probe, Description.EMPTY).evaluate();
            fail("Expected the original fixture failure");
        } catch (AssertionError failure) {
            assertSame(sentinel, failure);
        }
        for (var entry : previous.entrySet()) assertSame(entry.getValue(), entry.getKey().get(null));
    }
}
