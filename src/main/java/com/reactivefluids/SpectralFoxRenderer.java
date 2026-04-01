package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Renders spectral foxes as translucent, self-lit ghostly versions.
 * Uses entityTranslucent (writes depth) to avoid see-through artifacts.
 */
public class SpectralFoxRenderer extends MobRenderer<SpectralFoxEntity, FoxModel<SpectralFoxEntity>> {

    /** Fox variant textures — red fox and snow fox. */
    private static final ResourceLocation[] VARIANT_TEXTURES = {
            ResourceLocation.withDefaultNamespace("textures/entity/fox/fox.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox.png"),
    };

    /** Ghostly blue-white tint. ARGB: alpha=180, R=120, G=170, B=255 */
    private static final int GHOST_COLOR = 0xB478AAFF;

    public SpectralFoxRenderer(EntityRendererProvider.Context context) {
        super(context, new FoxModel<>(context.bakeLayer(ModelLayers.FOX)), 0.0F);

        this.addLayer(new RenderLayer<SpectralFoxEntity, FoxModel<SpectralFoxEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int light,
                               SpectralFoxEntity entity, float limbSwing, float limbSwingAmount,
                               float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                ResourceLocation tex = getTextureLocation(entity);
                VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentCull(tex));
                int overlay = OverlayTexture.pack(
                        OverlayTexture.u(0),
                        OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));
                getParentModel().renderToBuffer(poseStack, consumer, 15728880, overlay, GHOST_COLOR);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(SpectralFoxEntity entity) {
        int idx = entity.getVariantIndex();
        return VARIANT_TEXTURES[idx % VARIANT_TEXTURES.length];
    }

    @Nullable
    @Override
    protected RenderType getRenderType(SpectralFoxEntity entity, boolean bodyVisible,
                                        boolean translucent, boolean glowing) {
        return null; // Custom layer handles rendering
    }
}
