package zone.moddev.mc.basesteam.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
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
            PoseStack pose = new PoseStack();
            GUIHelper.positionNeedle(pose, 44F, 40F, 1F, value);
            var transform = pose.last().pose();
            var pivot = transform.transformPosition(new Vector3f(3.5F, 17.5F, 0F));
            assertEquals(44F, pivot.x, 0.0001F);
            assertEquals(40F, pivot.y, 0.0001F);
            assertEquals(1F, pivot.z, 0.0001F);
            var tip = transform.transformPosition(new Vector3f(3.5F, 0F, 0F));
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
}
