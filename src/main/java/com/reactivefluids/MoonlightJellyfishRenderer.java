package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.LightTexture;
import org.jetbrains.annotations.Nullable;

/**
 * Moonlight Jellyfish Renderer — translucent base with pulsing emissive glow overlay.
 * Supports two variants: blue (default) and green (rare 10%).
 */
public class MoonlightJellyfishRenderer extends MobRenderer<MoonlightJellyfishEntity, MoonlightJellyfishModel<MoonlightJellyfishEntity>> {

    // Blue variant textures (default)
    private static final ResourceLocation BLUE_BASE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/moonlight_jellyfish.png");
    private static final ResourceLocation BLUE_GLOW =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/moonlight_jellyfish_glow.png");

    // Green variant textures (rare)
    private static final ResourceLocation GREEN_BASE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/moonlight_jellyfish_green.png");
    private static final ResourceLocation GREEN_GLOW =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/moonlight_jellyfish_green_glow.png");

    // Blue tint: semi-transparent pale blue-white
    private static final int BLUE_BASE_COLOR = 0xB0C8DDFF; // ARGB
    // Green tint: semi-transparent pale green-white
    private static final int GREEN_BASE_COLOR = 0xB0C8FFDD; // ARGB

    public MoonlightJellyfishRenderer(EntityRendererProvider.Context context) {
        super(context,
                new MoonlightJellyfishModel<>(context.bakeLayer(MoonlightJellyfishModel.LAYER_LOCATION)),
                0.2F);
    }

    @Override
    public ResourceLocation getTextureLocation(MoonlightJellyfishEntity entity) {
        return entity.isGreenVariant() ? GREEN_BASE : BLUE_BASE;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(MoonlightJellyfishEntity entity, boolean bodyVisible,
                                        boolean translucent, boolean glowing) {
        return null; // custom rendering only
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
            // Bioluminescent green glow: RGB(100, 255, 140)
            glowColor = (glowAlpha << 24) | 0x64FF8C;
        } else {
            // Moonlight blue-white glow: RGB(170, 220, 255)
            glowColor = (glowAlpha << 24) | 0xAADCFF;
        }

        VertexConsumer glowBuffer = bufferSource.getBuffer(
                RenderType.entityTranslucentEmissive(glowTex));
        this.getModel().renderToBuffer(poseStack, glowBuffer, LightTexture.FULL_BRIGHT,
                overlay, glowColor);
    }
}
