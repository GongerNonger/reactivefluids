package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Renders the Magic Missile as a glowing billboard sprite that always
 * faces the camera, with an emissive glow overlay on top.
 */
public class MagicMissileRenderer extends EntityRenderer<MagicMissileEntity, EntityRenderState> {

    private static final ResourceLocation BASE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/magic_missile.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/magic_missile_glow.png");

    public MagicMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void render(EntityRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Scale the sprite — small arcane dart
        float scale = 0.15F;
        poseStack.scale(scale, scale, scale);

        // Billboard — face the camera
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        PoseStack.Pose pose = poseStack.last();

        // Base layer — translucent dart
        VertexConsumer base = bufferSource.getBuffer(RenderType.entityTranslucentCull(BASE_TEXTURE));
        drawQuad(base, pose, packedLight, 0xFFFFFFFF);

        // Glow layer — emissive, ignores world lighting
        VertexConsumer glow = bufferSource.getBuffer(RenderType.eyes(GLOW_TEXTURE));
        drawQuad(glow, pose, 0x00F000F0, 0xFFFFFFFF);

        poseStack.popPose();

        super.render(state, poseStack, bufferSource, packedLight);
    }

    private void drawQuad(VertexConsumer consumer, PoseStack.Pose pose,
                          int light, int color) {
        float half = 0.5F;
        Matrix4f matrix = pose.pose();
        consumer.addVertex(matrix, -half, -half, 0).setColor(color)
                .setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, half, -half, 0).setColor(color)
                .setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, half, half, 0).setColor(color)
                .setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(matrix, -half, half, 0).setColor(color)
                .setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, 0, 1, 0);
    }
}
