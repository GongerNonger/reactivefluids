package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Renders the meteor as a glowing magma-like billboard quad
 * (two perpendicular planes) with pulsing size and emissive glow.
 */
public class MeteorRenderer extends EntityRenderer<MeteorEntity> {

    private static final ResourceLocation METEOR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/meteor.png");

    public MeteorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MeteorEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int light) {
        poseStack.pushPose();

        // Pulsing size
        float size = 0.75F + (float) Math.sin(entity.tickCount * 0.5) * 0.1F;

        // Bright orange-yellow core color
        float r = 1.0F;
        float g = 0.7F;
        float b = 0.2F;
        float a = 1.0F;

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucentEmissive(METEOR_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();

        // Horizontal plane (billboard quad)
        vertex(vc, mat, pose, -size, -size, 0, 0, 0, r, g, b, a);
        vertex(vc, mat, pose,  size, -size, 0, 1, 0, r, g, b, a);
        vertex(vc, mat, pose,  size,  size, 0, 1, 1, r, g, b, a);
        vertex(vc, mat, pose, -size,  size, 0, 0, 1, r, g, b, a);

        // Vertical perpendicular plane
        vertex(vc, mat, pose, 0, -size, -size, 0, 0, r, g, b, a);
        vertex(vc, mat, pose, 0, -size,  size, 1, 0, r, g, b, a);
        vertex(vc, mat, pose, 0,  size,  size, 1, 1, r, g, b, a);
        vertex(vc, mat, pose, 0,  size, -size, 0, 1, r, g, b, a);

        // Outer glow layer (larger, more transparent)
        float glowSize = size * 2.0F;
        float ga = 0.35F;

        vertex(vc, mat, pose, -glowSize, -glowSize, 0, 0, 0, r, g, b, ga);
        vertex(vc, mat, pose,  glowSize, -glowSize, 0, 1, 0, r, g, b, ga);
        vertex(vc, mat, pose,  glowSize,  glowSize, 0, 1, 1, r, g, b, ga);
        vertex(vc, mat, pose, -glowSize,  glowSize, 0, 0, 1, r, g, b, ga);

        vertex(vc, mat, pose, 0, -glowSize, -glowSize, 0, 0, r, g, b, ga);
        vertex(vc, mat, pose, 0, -glowSize,  glowSize, 1, 0, r, g, b, ga);
        vertex(vc, mat, pose, 0,  glowSize,  glowSize, 1, 1, r, g, b, ga);
        vertex(vc, mat, pose, 0,  glowSize, -glowSize, 0, 1, r, g, b, ga);

        poseStack.popPose();
    }

    private void vertex(VertexConsumer vc, Matrix4f mat, PoseStack.Pose pose,
                         float x, float y, float z, float u, float v,
                         float r, float g, float b, float a) {
        vc.addVertex(mat, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880) // full brightness (emissive)
                .setNormal(pose, 0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(MeteorEntity entity) {
        return METEOR_TEXTURE;
    }
}
