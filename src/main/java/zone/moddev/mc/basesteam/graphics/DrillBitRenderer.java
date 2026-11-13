package zone.moddev.mc.basesteam.graphics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import zone.moddev.mc.basesteam.SteamAdvantage;
import zone.moddev.mc.basesteam.blocks.DrillBitTileEntity;

public class DrillBitRenderer implements BlockEntityRenderer<DrillBitTileEntity, DrillBitRenderer.State> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(SteamAdvantage.MODID, "textures/entity/drill_bit.png");

    public DrillBitRenderer(Context dispatcher) {
    }

    public static final class State extends BlockEntityRenderState {
        Direction.Axis axis = Direction.Axis.Y;
        float rotation;
    }

    @Override public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(DrillBitTileEntity tile, State state, float partialTick, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTick, camera, breaking);
        state.axis = tile.getDirection();
        state.rotation = tile.rotation + DrillBitTileEntity.ROTATION_PER_TICK * partialTick;
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        matrices.pushPose();
        try {
            positionGeometry(state.axis, state.rotation, matrices);
            int light = state.lightCoords;
            collector.submitCustomGeometry(matrices, RenderTypes.entityTranslucent(TEXTURE),
                    (pose, vertices) -> renderQuads(pose, vertices, light,
                            net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY));
        } finally {
            matrices.popPose();
        }
    }

    static void renderGeometry(Direction.Axis axis, float rotation, PoseStack matrices,
            VertexConsumer vertices, int light, int overlay) {
        matrices.pushPose();
        try {
            positionGeometry(axis, rotation, matrices);
            renderQuads(matrices.last(), vertices, light, overlay);
        } finally {
            matrices.popPose();
        }
    }

    private static void positionGeometry(Direction.Axis axis, float rotation, PoseStack matrices) {
        matrices.translate(0.5D, 0.5D, 0.5D);
        if (axis != Direction.Axis.Y) {
            matrices.rotate(Axis.XP.rotationDegrees(90.0F));
            if (axis != Direction.Axis.Z) matrices.rotate(Axis.ZP.rotationDegrees(90.0F));
        }
        matrices.rotate(Axis.YP.rotationDegrees(rotation));
    }

    private static void renderQuads(PoseStack.Pose transform, VertexConsumer vertices, int light, int overlay) {
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
    }

    private static void vertex(PoseStack.Pose transform, VertexConsumer vertices,
            float x, float y, float z, float u, float v,
            float normalX, float normalY, float normalZ, int light, int overlay) {
        vertices.addVertex(transform.pose(), x, y, z).setColor(255, 255, 255, 255).setUv(u, v)
                .setOverlay(overlay).setLight(light)
                .setNormal(transform, normalX, normalY, normalZ);
    }
}
