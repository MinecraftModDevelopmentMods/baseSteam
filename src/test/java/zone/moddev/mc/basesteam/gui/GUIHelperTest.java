package zone.moddev.mc.basesteam.gui;

import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import cyano.poweradvantage.api.simple.SimpleMachineGUI;
import cyano.poweradvantage.api.simple.SimpleMachineGUI.GUIContainer;
import org.junit.Test;
import static org.junit.Assert.*;

public class GUIHelperTest {
    @Test public void readingsKeepTheirScaleButStayInsideTheGauge() {
        assertEquals(0F, GUIHelper.displayLevel(-1F), 0F);
        assertEquals(0F, GUIHelper.displayLevel(0F), 0F);
        assertEquals(0.5F, GUIHelper.displayLevel(0.5F), 0F);
        assertEquals(1F, GUIHelper.displayLevel(1F), 0F);
        assertEquals(1F, GUIHelper.displayLevel(15F), 0F);
    }

    @Test public void needleSweepsLeftToRightThroughTheSamePivot() {
        for (float value : new float[] {0F, 0.25F, 0.5F, 0.75F, 1F, Float.NaN}) {
            Matrix3x2fStack pose = new Matrix3x2fStack(2);
            GUIHelper.positionNeedle(pose, 44F, 40F, value);
            var pivot = pose.transformPosition(new Vector2f(3.5F, 17.5F));
            assertEquals(44F, pivot.x, 0.0001F);
            assertEquals(40F, pivot.y, 0.0001F);
            var tip = pose.transformPosition(new Vector2f(3.5F, 0F));
            // Independent geometry from the previous native vertex implementation.
            double legacyAngle = (1D - (Float.isNaN(value) ? 0D : value)) * Math.PI;
            assertEquals(44D + Math.cos(legacyAngle) * 17.5D, tip.x, 0.0001D);
            assertEquals(40D - Math.sin(legacyAngle) * 17.5D, tip.y, 0.0001D);
        }
    }

    @Test public void nonfiniteReadingsAreSafeToDraw() {
        for (float value : new float[]{Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            assertEquals(0F, GUIHelper.displayLevel(value), 0F);
        }
    }

    @Test public void screenAwareNeedleSubmitsTheOriginalRegionAndRestoresTheMatrixEvenOnFailure() throws Exception {
        var field = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        field.setAccessible(true);
        var unsafe = field.get(null);
        DrawingContainer container = (DrawingContainer) unsafe.getClass().getMethod("allocateInstance", Class.class)
                .invoke(unsafe, DrawingContainer.class);
        container.matrices = new Matrix3x2fStack(2);
        container.matrices.translate(10F, 20F);
        Matrix3x2f before = new Matrix3x2f(container.matrices);
        GUIHelper.drawNeedle(container, 44F, 40F, 1F, 0.75F);
        assertEquals(before, container.matrices);
        assertArrayEquals(new int[] {0, 0, 176, 0, 7, 35}, container.region);
        var pivot = container.submitted.transformPosition(new Vector2f(3.5F, 17.5F));
        assertEquals(54F, pivot.x, 0.0001F);
        assertEquals(60F, pivot.y, 0.0001F);
        container.refuse = true;
        assertThrows(IllegalStateException.class, () -> GUIHelper.drawNeedle(container, 44F, 40F, 1F, 0.75F));
        assertEquals(before, container.matrices);
        assertThrows("The legacy overload cannot draw outside the real decoration callback", IllegalStateException.class,
                () -> GUIHelper.drawingContainer(container));
        assertThrows(IllegalStateException.class, () -> GUIHelper.drawingContainer(null));
    }

    @Test public void legacySignatureDelegatesFromTheNativeCurrentScreenWithoutImmediateGpuCalls() throws Exception {
        var node = new org.objectweb.asm.tree.ClassNode();
        try (var input = GUIHelper.class.getResourceAsStream("GUIHelper.class")) {
            assertNotNull(input);
            new org.objectweb.asm.ClassReader(input).accept(node, 0);
        }
        var method = node.methods.stream().filter(value -> value.name.equals("drawNeedle")
                && value.desc.equals("(FFFF)V")).findFirst().orElseThrow();
        boolean currentScreen = false, delegate = false;
        for (var instruction : method.instructions) {
            if (instruction instanceof org.objectweb.asm.tree.FieldInsnNode field) {
                if (field.owner.equals("net/minecraft/client/Minecraft") && field.name.equals("screen")) currentScreen = true;
            }
            if (instruction instanceof org.objectweb.asm.tree.MethodInsnNode call) {
                assertFalse(call.owner, call.owner.startsWith("com/mojang/blaze3d/"));
                if (call.owner.equals(node.name) && call.name.equals("drawNeedle")
                        && call.desc.equals("(Lcyano/poweradvantage/api/simple/SimpleMachineGUI$GUIContainer;FFFF)V")) delegate = true;
            }
        }
        assertTrue(currentScreen && delegate);
    }

    private static final class DrawingContainer extends GUIContainer {
        private Matrix3x2fStack matrices;
        private Matrix3x2f submitted;
        private int[] region;
        private boolean refuse;

        private DrawingContainer(SimpleMachineGUI owner) {
            owner.super((SimpleMachineGUI.Container) null, null, net.minecraft.network.chat.Component.empty());
        }

        @Override public Matrix3x2fStack getMatrices() { return matrices; }
        @Override public void blit(int x, int y, int u, int v, int width, int height) {
            submitted = new Matrix3x2f(matrices);
            region = new int[] {x, y, u, v, width, height};
            if (refuse) throw new IllegalStateException("needle submission failed");
        }
    }
}
