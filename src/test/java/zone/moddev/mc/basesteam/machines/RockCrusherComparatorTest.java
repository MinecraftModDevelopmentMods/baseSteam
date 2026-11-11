package zone.moddev.mc.basesteam.machines;

import zone.moddev.mc.basesteam.testing.NativeMachineFixture;

import zone.moddev.mc.basesteam.testing.NativeTestBootstrap;
import static org.junit.Assert.assertEquals;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.BeforeClass;
import org.junit.Test;

public class RockCrusherComparatorTest {
    @org.junit.Rule public final NativeMachineFixture machineTypes = new NativeMachineFixture();
    @BeforeClass public static void bootstrap() {
        NativeTestBootstrap.start();
    }
    @Test public void emptyInputHasNoComparatorSignal() { assertSignal(0, 0); }
    @Test public void oneItemGivesSignalOne() { assertSignal(1, 1); }
    @Test public void halfAStackGivesSignalEight() { assertSignal(32, 8); }
    @Test public void aFullStackGivesSignalFifteen() { assertSignal(64, 15); }

    private static void assertSignal(int count, int expected) {
        RockCrusherTileEntity crusher = new RockCrusherTileEntity(net.minecraft.core.BlockPos.ZERO, NativeMachineFixture.state("steam_crusher"));
        crusher.setItem(0, count == 0 ? ItemStack.EMPTY : new ItemStack(Items.COAL, count));
        assertEquals("Actual crusher comparator at " + count + " input items", expected, crusher.getComparatorOutput());
    }
}
