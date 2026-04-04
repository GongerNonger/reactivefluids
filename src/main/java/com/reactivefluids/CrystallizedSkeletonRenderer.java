package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Renders a crystallized skeleton with a glowing crystal overlay and laser beam.
 * Beam rendering uses the same coordinate-space approach as GuardianRenderer.
 */
@OnlyIn(Dist.CLIENT)
public class CrystallizedSkeletonRenderer extends SkeletonRenderer<CrystallizedSkeleton> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
    private static final ResourceLocation BEAM_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/guardian_beam.png");
    private static final RenderType BEAM_RENDER_TYPE =
            RenderType.entityCutoutNoCull(BEAM_TEXTURE);

    public CrystallizedSkeletonRenderer(EntityRendererProvider.Context context) {
        super(context);
        // 3D crystal spikes growing off body parts (uses entityTranslucentEmissive for glow)
        this.addLayer(new CrystalOverlayLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(CrystallizedSkeleton entity) {
        return TEXTURE;
    }

    @Override
    public void render(CrystallizedSkeleton entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        // Render the laser beam if charging
        LivingEntity target = entity.getLaserTarget();
        if (target != null) {
            float scale = entity.getLaserScale(partialTick);
            float animTime = (entity.tickCount + partialTick);
            renderLaserBeam(entity, target, partialTick, scale, animTime, poseStack, bufferSource);
        }
    }

    /**
     * Interpolate a world position for the given entity at the given y-offset.
     * Mirrors GuardianRenderer.getPosition().
     */
    private Vec3 getPosition(LivingEntity entity, double yOffset, float partialTick) {
        double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTick, entity.yOld, entity.getY()) + yOffset;
        double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        return new Vec3(x, y, z);
    }

    private void renderLaserBeam(CrystallizedSkeleton entity, LivingEntity target,
                                  float partialTick, float scale, float animTime,
                                  PoseStack poseStack, MultiBufferSource bufferSource) {
        float eyeHeight = entity.getEyeHeight();

        // Compute interpolated world positions -- same approach as GuardianRenderer
        Vec3 eyePos = getPosition(entity, eyeHeight, partialTick);
        Vec3 targetPos = getPosition(target, (double) target.getBbHeight() * 0.5, partialTick);
        Vec3 diff = targetPos.subtract(eyePos);
        float beamLength = (float) (diff.length() + 1.0);
        diff = diff.normalize();

        // Compute rotation angles from the direction vector (Guardian method)
        // acos(y) gives the angle from the +Y axis (pitch in XP rotation)
        // atan2(z, x) gives the horizontal angle from +X axis (yaw in YP rotation)
        float pitchAngle = (float) Math.acos(diff.y);
        float yawAngle = (float) Math.atan2(diff.z, diff.x);

        poseStack.pushPose();

        // Translate to the entity's eye height (local space origin is at entity feet)
        poseStack.translate(0.0F, eyeHeight, 0.0F);

        // Apply rotations -- Guardian convention:
        // YP rotation converts from atan2 angle to Minecraft's coordinate system
        // XP rotation tilts the beam up/down
        poseStack.mulPose(Axis.YP.rotationDegrees(((float) (Math.PI / 2) - yawAngle) * (180.0F / (float) Math.PI)));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitchAngle * (180.0F / (float) Math.PI)));

        // --- Animation parameters ---
        // Charge scale (0 to 1+) controls beam growth and color intensity
        float chargeScale = Math.min(scale, 1.0F);
        float chargeScaleSq = chargeScale * chargeScale;

        // Pulsing width: base size grows with charge, oscillates with a sine wave
        float pulse = Mth.sin(animTime * 0.6F) * 0.15F + 1.0F; // oscillates 0.85 to 1.15
        float outerWidth = (0.15F + 0.15F * chargeScale) * pulse;
        float innerWidth = outerWidth * 0.45F;

        // Scrolling UV offset for animated beam texture
        float scrollSpeed = 0.05F * -1.5F;
        float uvScroll = animTime * scrollSpeed;
        float uvTile = beamLength * 2.5F;

        // --- Colors ---
        // Outer glow: cyan/blue, brightens with charge
        int outerR = 64 + (int) (chargeScaleSq * 70.0F);   // 64 -> 134
        int outerG = 140 + (int) (chargeScaleSq * 115.0F);  // 140 -> 255
        int outerB = 220 + (int) (chargeScaleSq * 35.0F);   // 220 -> 255

        // Inner core: bright white-cyan, always bright
        int innerR = 180 + (int) (chargeScaleSq * 75.0F);   // 180 -> 255
        int innerG = 230 + (int) (chargeScaleSq * 25.0F);   // 230 -> 255
        int innerB = 255;

        // Guardian-style rotating beam cross-section using trig
        // Outer beam (4 vertices per face, 2 faces = softer glow)
        float outerAngle = uvScroll;
        float ox0 = Mth.cos(outerAngle + 0.0F) * outerWidth;
        float oz0 = Mth.sin(outerAngle + 0.0F) * outerWidth;
        float ox1 = Mth.cos(outerAngle + (float) (Math.PI / 2)) * outerWidth;
        float oz1 = Mth.sin(outerAngle + (float) (Math.PI / 2)) * outerWidth;
        float ox2 = Mth.cos(outerAngle + (float) Math.PI) * outerWidth;
        float oz2 = Mth.sin(outerAngle + (float) Math.PI) * outerWidth;
        float ox3 = Mth.cos(outerAngle + (float) (Math.PI * 3.0 / 2.0)) * outerWidth;
        float oz3 = Mth.sin(outerAngle + (float) (Math.PI * 3.0 / 2.0)) * outerWidth;

        // Inner core (tighter, rotates at a different rate for visual depth)
        float innerAngle = uvScroll * 1.3F + (float) (Math.PI / 4);
        float ix0 = Mth.cos(innerAngle + 0.0F) * innerWidth;
        float iz0 = Mth.sin(innerAngle + 0.0F) * innerWidth;
        float ix1 = Mth.cos(innerAngle + (float) (Math.PI / 2)) * innerWidth;
        float iz1 = Mth.sin(innerAngle + (float) (Math.PI / 2)) * innerWidth;
        float ix2 = Mth.cos(innerAngle + (float) Math.PI) * innerWidth;
        float iz2 = Mth.sin(innerAngle + (float) Math.PI) * innerWidth;
        float ix3 = Mth.cos(innerAngle + (float) (Math.PI * 3.0 / 2.0)) * innerWidth;
        float iz3 = Mth.sin(innerAngle + (float) (Math.PI * 3.0 / 2.0)) * innerWidth;

        // UV coordinates
        float uvScrollOffset = -1.0F + (animTime * 0.05F * 0.5F) % 1.0F;
        float uvEnd = uvTile + uvScrollOffset;

        VertexConsumer consumer = bufferSource.getBuffer(BEAM_RENDER_TYPE);
        PoseStack.Pose pose = poseStack.last();

        // --- Outer glow: two crossed quads ---
        // Face 1
        vertex(consumer, pose, ox2, beamLength, oz2, outerR, outerG, outerB, 0.4999F, uvEnd);
        vertex(consumer, pose, ox2, 0.0F, oz2, outerR, outerG, outerB, 0.4999F, uvScrollOffset);
        vertex(consumer, pose, ox0, 0.0F, oz0, outerR, outerG, outerB, 0.0F, uvScrollOffset);
        vertex(consumer, pose, ox0, beamLength, oz0, outerR, outerG, outerB, 0.0F, uvEnd);

        // Face 2
        vertex(consumer, pose, ox1, beamLength, oz1, outerR, outerG, outerB, 0.4999F, uvEnd);
        vertex(consumer, pose, ox1, 0.0F, oz1, outerR, outerG, outerB, 0.4999F, uvScrollOffset);
        vertex(consumer, pose, ox3, 0.0F, oz3, outerR, outerG, outerB, 0.0F, uvScrollOffset);
        vertex(consumer, pose, ox3, beamLength, oz3, outerR, outerG, outerB, 0.0F, uvEnd);

        // --- Inner core: two crossed quads ---
        // Face 1
        vertex(consumer, pose, ix2, beamLength, iz2, innerR, innerG, innerB, 0.4999F, uvEnd);
        vertex(consumer, pose, ix2, 0.0F, iz2, innerR, innerG, innerB, 0.4999F, uvScrollOffset);
        vertex(consumer, pose, ix0, 0.0F, iz0, innerR, innerG, innerB, 0.0F, uvScrollOffset);
        vertex(consumer, pose, ix0, beamLength, iz0, innerR, innerG, innerB, 0.0F, uvEnd);

        // Face 2
        vertex(consumer, pose, ix1, beamLength, iz1, innerR, innerG, innerB, 0.4999F, uvEnd);
        vertex(consumer, pose, ix1, 0.0F, iz1, innerR, innerG, innerB, 0.4999F, uvScrollOffset);
        vertex(consumer, pose, ix3, 0.0F, iz3, innerR, innerG, innerB, 0.0F, uvScrollOffset);
        vertex(consumer, pose, ix3, beamLength, iz3, innerR, innerG, innerB, 0.0F, uvEnd);

        // --- End cap: diamond shape at the tip (Guardian-style) ---
        float capSize = outerWidth * 1.414F;
        float capAngle = uvScroll * 0.7F;
        float cx0 = Mth.cos(capAngle + (float) (Math.PI * 3.0 / 4.0)) * capSize;
        float cz0 = Mth.sin(capAngle + (float) (Math.PI * 3.0 / 4.0)) * capSize;
        float cx1 = Mth.cos(capAngle + (float) (Math.PI / 4)) * capSize;
        float cz1 = Mth.sin(capAngle + (float) (Math.PI / 4)) * capSize;
        float cx2 = Mth.cos(capAngle + (float) (Math.PI * 5.0 / 4.0)) * capSize;
        float cz2 = Mth.sin(capAngle + (float) (Math.PI * 5.0 / 4.0)) * capSize;
        float cx3 = Mth.cos(capAngle + (float) (Math.PI * 7.0 / 4.0)) * capSize;
        float cz3 = Mth.sin(capAngle + (float) (Math.PI * 7.0 / 4.0)) * capSize;

        // Alternating frame for cap texture
        float capV = (entity.tickCount % 2 == 0) ? 0.5F : 0.0F;
        vertex(consumer, pose, cx0, beamLength, cz0, innerR, innerG, innerB, 0.5F, capV + 0.5F);
        vertex(consumer, pose, cx1, beamLength, cz1, innerR, innerG, innerB, 1.0F, capV + 0.5F);
        vertex(consumer, pose, cx3, beamLength, cz3, innerR, innerG, innerB, 1.0F, capV);
        vertex(consumer, pose, cx2, beamLength, cz2, innerR, innerG, innerB, 0.5F, capV);

        poseStack.popPose();
    }

    /**
     * Helper to emit a single vertex -- mirrors GuardianRenderer.vertex().
     */
    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                                float x, float y, float z,
                                int r, int g, int b,
                                float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
