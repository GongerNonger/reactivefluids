package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

/**
 * Moonlight Jellyfish Renderer — translucent base with pulsing emissive glow overlay.
 * Uses squid-style setupRotations for proper water orientation (bell on top, tentacles below).
 */
public class MoonlightJellyfishRenderer extends MobRenderer<MoonlightJellyfishEntity, MoonlightJellyfishModel<MoonlightJellyfishEntity>> {

    private static final ResourceLocation BLUE_BASE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/moonlight_jellyfish.png");
    private static final ResourceLocation BLUE_GLOW =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/moonlight_jellyfish_glow.png");
    private static final ResourceLocation GREEN_BASE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/moonlight_jellyfish_green.png");
    private static final ResourceLocation GREEN_GLOW =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/moonlight_jellyfish_green_glow.png");

    private static final int BLUE_BASE_COLOR = 0xB0C8DDFF;
    private static final int GREEN_BASE_COLOR = 0xB0C8FFDD;

    public MoonlightJellyfishRenderer(EntityRendererProvider.Context context) {
        super(context,
                new MoonlightJellyfishModel<>(context.bakeLayer(MoonlightJellyfishModel.LAYER_LOCATION)),
                0.2F);
    }

    @Override
    public ResourceLocation getTextureLocation(MoonlightJellyfishEntity entity) {
        return entity.isGreenVariant() ? GREEN_BASE : BLUE_BASE;
    }

    /**
     * Custom orientation: bell on top, tentacles below.
     * Flip 180 on X so tentacles hang down, then apply yaw so
     * the front faces the movement direction.
     * Slight tilt toward movement direction for organic feel.
     */
    @Override
    protected void setupRotations(MoonlightJellyfishEntity entity, PoseStack poseStack,
                                   float ageInTicks, float rotationYaw, float partialTick, float scale) {
        poseStack.translate(0.0F, 0.5F, 0.0F);
        // Yaw: face direction of movement
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - rotationYaw));
        // Flip 180 on X axis: this puts bell on top, tentacles hanging down
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        // Slight tilt based on vertical movement — nose up when rising, down when sinking
        float verticalTilt = (float) (entity.getDeltaMovement().y * -30.0);
        verticalTilt = Mth.clamp(verticalTilt, -15.0F, 15.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(verticalTilt));
        poseStack.translate(0.0F, -1.2F, 0.0F);
    }

    @Override
    protected float getBob(MoonlightJellyfishEntity entity, float partialTick) {
        return Mth.lerp(partialTick, entity.oldTentacleAngle, entity.tentacleAngle);
    }

    @Nullable
    @Override
    protected RenderType getRenderType(MoonlightJellyfishEntity entity, boolean bodyVisible,
                                        boolean translucent, boolean glowing) {
        return null;
    }

    @Override
    public void render(MoonlightJellyfishEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        boolean green = entity.isGreenVariant();
        ResourceLocation baseTex = green ? GREEN_BASE : BLUE_BASE;
        ResourceLocation glowTex = green ? GREEN_GLOW : BLUE_GLOW;
        int baseColor = green ? GREEN_BASE_COLOR : BLUE_BASE_COLOR;

        int overlay = OverlayTexture.pack(
                OverlayTexture.u(0),
                OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));

        // Pass 1: Translucent base body
        VertexConsumer baseBuffer = bufferSource.getBuffer(
                RenderType.entityTranslucentCull(baseTex));
        this.getModel().renderToBuffer(poseStack, baseBuffer, LightTexture.FULL_BRIGHT,
                overlay, baseColor);

        // Pass 2: Emissive glow overlay with pulsing alpha
        float glowIntensity = entity.getGlowIntensity(partialTick);
        int glowAlpha = (int) (glowIntensity * 200);
        int glowColor;
        if (green) {
            glowColor = (glowAlpha << 24) | 0x64FF8C;
        } else {
            glowColor = (glowAlpha << 24) | 0xAADCFF;
        }

        VertexConsumer glowBuffer = bufferSource.getBuffer(
                RenderType.entityTranslucentEmissive(glowTex));
        this.getModel().renderToBuffer(poseStack, glowBuffer, LightTexture.FULL_BRIGHT,
                overlay, glowColor);
    }
}
