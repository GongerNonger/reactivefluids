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
 * Renders the fireball as two perpendicular billboard quads with an outer glow.
 * Color is read from the entity's synced data and applied as a tint,
 * so dye-colored fireballs render in their respective color.
 */
public class FireballRenderer extends EntityRenderer<FireballEntity> {

    private static final ResourceLocation FIREBALL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/fireball.png");

    public FireballRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FireballEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int light) {
        poseStack.pushPose();

        // Pulsing core
        float pulse = (float) Math.sin(entity.tickCount * 0.4 + partialTick * 0.4) * 0.08F;
        float size = 0.45F + pulse;

        // Extract RGB from the synced color int
        int color = entity.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8)  & 0xFF) / 255.0F;
        float b = ((color)       & 0xFF) / 255.0F;

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucentEmissive(FIREBALL_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();

        // Core — two perpendicular quads
        quad(vc, mat, pose, -size, -size, 0,  size, -size, 0,  size, size, 0,  -size, size, 0,
                0, 0, 1, 1, r, g, b, 1.0F);
        quad(vc, mat, pose, 0, -size, -size,  0, -size, size,  0, size, size,  0, size, -size,
                0, 0, 1, 1, r, g, b, 1.0F);

        // Outer glow — larger, translucent, slightly brighter
        float gs = size * 2.2F;
        float gr = Math.min(1.0F, r * 1.3F);
        float gg = Math.min(1.0F, g * 1.3F);
        float gb = Math.min(1.0F, b * 1.3F);
        quad(vc, mat, pose, -gs, -gs, 0,  gs, -gs, 0,  gs, gs, 0,  -gs, gs, 0,
                0, 0, 1, 1, gr, gg, gb, 0.3F);
        quad(vc, mat, pose, 0, -gs, -gs,  0, -gs, gs,  0, gs, gs,  0, gs, -gs,
                0, 0, 1, 1, gr, gg, gb, 0.3F);

        poseStack.popPose();
    }

    private void quad(VertexConsumer vc, Matrix4f mat, PoseStack.Pose pose,
                      float x0, float y0, float z0,
                      float x1, float y1, float z1,
                      float x2, float y2, float z2,
                      float x3, float y3, float z3,
                      float u0, float v0, float u1, float v1,
                      float r, float g, float b, float a) {
        vertex(vc, mat, pose, x0, y0, z0, u0, v0, r, g, b, a);
        vertex(vc, mat, pose, x1, y1, z1, u1, v0, r, g, b, a);
        vertex(vc, mat, pose, x2, y2, z2, u1, v1, r, g, b, a);
        vertex(vc, mat, pose, x3, y3, z3, u0, v1, r, g, b, a);
    }

    private void vertex(VertexConsumer vc, Matrix4f mat, PoseStack.Pose pose,
                        float x, float y, float z, float u, float v,
                        float r, float g, float b, float a) {
        vc.addVertex(mat, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880) // full brightness — emissive
                .setNormal(pose, 0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(FireballEntity entity) {
        return FIREBALL_TEXTURE;
    }
}
