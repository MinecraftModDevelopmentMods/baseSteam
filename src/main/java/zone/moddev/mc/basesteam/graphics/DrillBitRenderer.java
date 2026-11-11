package zone.moddev.mc.basesteam.graphics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.blocks.DrillBitTileEntity;

@OnlyIn(Dist.CLIENT)
public class DrillBitRenderer implements BlockEntityRenderer<DrillBitTileEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SteamAdvantage.MODID, "textures/entity/drill_bit.png");

    public DrillBitRenderer(Context dispatcher) {
    }

    @Override
    public void render(DrillBitTileEntity tile, float partialTick, PoseStack matrices,
            MultiBufferSource buffers, int light, int overlay) {
        renderGeometry(tile.getDirection(), tile.rotation + DrillBitTileEntity.ROTATION_PER_TICK * partialTick,
                matrices, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light, overlay);
    }

    static void renderGeometry(Direction.Axis axis, float rotation, PoseStack matrices,
            VertexConsumer vertices, int light, int overlay) {
        matrices.pushPose();
        try {
            matrices.translate(0.5D, 0.5D, 0.5D);
            if (axis != Direction.Axis.Y) {
                matrices.mulPose(Axis.XP.rotationDegrees(90.0F));
                if (axis != Direction.Axis.Z) matrices.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            matrices.mulPose(Axis.YP.rotationDegrees(rotation));
            PoseStack.Pose transform = matrices.last();
            final float radius = 0.25F;
            // Retain the original six quads and UV layout; native buffers supply lighting and overlay.
            vertex(transform, vertices, radius, 0.5F, -radius, 0, 0, 0, 0, -1, light, overlay);
            vertex(transform, vertices, radius, -0.5F, -radius, 0, 1, 0, 0, -1, light, overlay);
            vertex(transform, vertices, -radius, -0.5F, -radius, 0.5F, 1, 0, 0, -1, light, overlay);
            vertex(transform, vertices, -radius, 0.5F, -radius, 0.5F, 0, 0, 0, -1, light, overlay);

            vertex(transform, vertices, -radius, 0.5F, radius, 0, 0, 0, 0, 1, light, overlay);
            vertex(transform, vertices, -radius, -0.5F, radius, 0, 1, 0, 0, 1, light, overlay);
            vertex(transform, vertices, radius, -0.5F, radius, 0.5F, 1, 0, 0, 1, light, overlay);
            vertex(transform, vertices, radius, 0.5F, radius, 0.5F, 0, 0, 0, 1, light, overlay);

            vertex(transform, vertices, -radius, 0.5F, -radius, 0, 0, -1, 0, 0, light, overlay);
            vertex(transform, vertices, -radius, -0.5F, -radius, 0, 1, -1, 0, 0, light, overlay);
            vertex(transform, vertices, -radius, -0.5F, radius, 0.5F, 1, -1, 0, 0, light, overlay);
            vertex(transform, vertices, -radius, 0.5F, radius, 0.5F, 0, -1, 0, 0, light, overlay);

            vertex(transform, vertices, radius, 0.5F, radius, 0, 0, 1, 0, 0, light, overlay);
            vertex(transform, vertices, radius, -0.5F, radius, 0, 1, 1, 0, 0, light, overlay);
            vertex(transform, vertices, radius, -0.5F, -radius, 0.5F, 1, 1, 0, 0, light, overlay);
            vertex(transform, vertices, radius, 0.5F, -radius, 0.5F, 0, 1, 0, 0, light, overlay);

            vertex(transform, vertices, -radius, 0.5F, -radius, 0.5F, 0, 0, 1, 0, light, overlay);
            vertex(transform, vertices, -radius, 0.5F, radius, 0.5F, 0.5F, 0, 1, 0, light, overlay);
            vertex(transform, vertices, radius, 0.5F, radius, 1, 0.5F, 0, 1, 0, light, overlay);
            vertex(transform, vertices, radius, 0.5F, -radius, 1, 0, 0, 1, 0, light, overlay);

            vertex(transform, vertices, -radius, -0.5F, radius, 0.5F, 0, 0, -1, 0, light, overlay);
            vertex(transform, vertices, -radius, -0.5F, -radius, 0.5F, 0.5F, 0, -1, 0, light, overlay);
            vertex(transform, vertices, radius, -0.5F, -radius, 1, 0.5F, 0, -1, 0, light, overlay);
            vertex(transform, vertices, radius, -0.5F, radius, 1, 0, 0, -1, 0, light, overlay);
        } finally {
            matrices.popPose();
        }
    }

    private static void vertex(PoseStack.Pose transform, VertexConsumer vertices,
            float x, float y, float z, float u, float v,
            float normalX, float normalY, float normalZ, int light, int overlay) {
        vertices.addVertex(transform.pose(), x, y, z).setColor(255, 255, 255, 255).setUv(u, v)
                .setOverlay(overlay).setLight(light)
                .setNormal(transform, normalX, normalY, normalZ);
    }
}
