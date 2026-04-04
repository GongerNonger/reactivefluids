package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import javax.annotation.Nullable;

/**
 * Renders spectral wolves as translucent, self-lit ghostly versions
 * using vanilla wolf variant textures with a blue-tinted translucent emissive effect.
 * Extends MobRenderer directly (not WolfRenderer) to avoid collar/variant layers.
 *
 * Translucency is achieved by returning null from getRenderType (skip opaque render)
 * and using a custom RenderLayer that calls renderToBuffer with an ARGB color
 * that has alpha < 255.
 */
public class SpectralWolfRenderer extends MobRenderer<SpectralWolfEntity, WolfModel<SpectralWolfEntity>> {

    /** All 9 wolf variant textures from 1.21.1 — each wolf picks one at spawn. */
    private static final ResourceLocation[] VARIANT_TEXTURES = {
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_ashen.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_black.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_chestnut.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_rusty.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_snowy.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_spotted.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_striped.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_woods.png"),
    };

    private static final ResourceLocation COLLAR_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf_collar.png");

    /** Ghostly blue-white, ~55% translucent. ARGB: alpha=140, R=140, G=190, B=255 */
    private static final int GHOST_COLOR = 0x8C8CBEFF;

    public SpectralWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new WolfModel<>(context.bakeLayer(ModelLayers.WOLF)), 0.0F);

        // Body layer — translucent emissive with ghostly blue tint
        this.addLayer(new RenderLayer<SpectralWolfEntity, WolfModel<SpectralWolfEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int light,
                               SpectralWolfEntity entity, float limbSwing, float limbSwingAmount,
                               float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                ResourceLocation tex = getTextureLocation(entity);
                VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentCull(tex));
                int overlay = OverlayTexture.pack(
                        OverlayTexture.u(0),
                        OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));
                getParentModel().renderToBuffer(poseStack, consumer, 15728880, overlay, GHOST_COLOR);
            }
        });

        // Collar layer — translucent emissive tinted with the wolf's collar color
        this.addLayer(new RenderLayer<SpectralWolfEntity, WolfModel<SpectralWolfEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int light,
                               SpectralWolfEntity entity, float limbSwing, float limbSwingAmount,
                               float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                if (!entity.isTame()) return;
                DyeColor collarColor = entity.getCollarColor();
                int rgb = collarColor.getTextureDiffuseColor(); // 0xFFRRGGBB
                // Extract RGB and apply translucent alpha
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int color = (0xAA << 24) | (r << 16) | (g << 8) | b; // ~67% alpha
                VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentCull(COLLAR_TEXTURE));
                int overlay = OverlayTexture.pack(
                        OverlayTexture.u(0),
                        OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));
                getParentModel().renderToBuffer(poseStack, consumer, 15728880, overlay, color);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(SpectralWolfEntity entity) {
        int idx = entity.getVariantIndex();
        return VARIANT_TEXTURES[idx % VARIANT_TEXTURES.length];
    }

    @Nullable
    @Override
    protected RenderType getRenderType(SpectralWolfEntity entity, boolean bodyVisible,
                                        boolean translucent, boolean glowing) {
        // Return null — skip the default opaque render; our custom layer handles it
        return null;
    }

    /**
     * WolfModel.setupAnim() sets tail.xRot = ageInTicks (the return of getBob).
     * Vanilla WolfRenderer overrides this to return Wolf.getTailAngle() so the tail
     * hangs at the correct angle. Without this override, the tail spins 360.
     */
    @Override
    protected float getBob(SpectralWolfEntity entity, float partialTick) {
        return entity.getTailAngle();
    }
}
