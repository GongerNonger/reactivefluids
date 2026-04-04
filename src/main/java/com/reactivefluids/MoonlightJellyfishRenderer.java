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
 *
 * Rendering strategy:
 * 1. Skip default opaque render (getRenderType returns null)
 * 2. Custom layer renders base model with entityTranslucentCull (translucent body)
 * 3. Second pass renders glow texture with entityTranslucentEmissive at pulsing alpha
 */
public class MoonlightJellyfishRenderer extends MobRenderer<MoonlightJellyfishEntity, MoonlightJellyfishModel<MoonlightJellyfishEntity>> {

    private static final ResourceLocation BASE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/moonlight_jellyfish.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/moonlight_jellyfish_glow.png");

    // Base tint: semi-transparent pale blue-white
    private static final int BASE_COLOR = 0xB0C8DDFF; // ARGB: alpha=176, pale blue

    public MoonlightJellyfishRenderer(EntityRendererProvider.Context context) {
        super(context,
                new MoonlightJellyfishModel<>(context.bakeLayer(MoonlightJellyfishModel.LAYER_LOCATION)),
                0.2F); // small shadow
    }

    @Override
    public ResourceLocation getTextureLocation(MoonlightJellyfishEntity entity) {
        return BASE_TEXTURE;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(MoonlightJellyfishEntity entity, boolean bodyVisible,
                                        boolean translucent, boolean glowing) {
        // Skip default opaque render — we do our own translucent passes
        return null;
    }

    @Override
    public void render(MoonlightJellyfishEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Let MobRenderer set up model pose, animations, etc.
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        int overlay = OverlayTexture.pack(
                OverlayTexture.u(0),
                OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));

        // Pass 1: Translucent base body
        VertexConsumer baseBuffer = bufferSource.getBuffer(
                RenderType.entityTranslucentCull(BASE_TEXTURE));
        this.getModel().renderToBuffer(poseStack, baseBuffer, LightTexture.FULL_BRIGHT,
                overlay, BASE_COLOR);

        // Pass 2: Emissive glow overlay with pulsing alpha
        float glowIntensity = entity.getGlowIntensity(partialTick);
        int glowAlpha = (int) (glowIntensity * 200); // max ~200 of 255
        // Moonlight blue-white glow: RGB(170, 220, 255)
        int glowColor = (glowAlpha << 24) | 0xAADCFF;

        VertexConsumer glowBuffer = bufferSource.getBuffer(
                RenderType.entityTranslucentEmissive(GLOW_TEXTURE));
        this.getModel().renderToBuffer(poseStack, glowBuffer, LightTexture.FULL_BRIGHT,
                overlay, glowColor);
    }
}
