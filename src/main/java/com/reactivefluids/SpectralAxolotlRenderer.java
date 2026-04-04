package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AxolotlModel;
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
 * Renders spectral axolotls as translucent, self-lit ghostly healers.
 * Pink-tinted to match the healing theme.
 */
public class SpectralAxolotlRenderer extends MobRenderer<SpectralAxolotlEntity, AxolotlModel<SpectralAxolotlEntity>> {

    /** All 5 axolotl variant textures. */
    private static final ResourceLocation[] VARIANT_TEXTURES = {
            ResourceLocation.withDefaultNamespace("textures/entity/axolotl/axolotl_lucy.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/axolotl/axolotl_wild.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/axolotl/axolotl_gold.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/axolotl/axolotl_cyan.png"),
            ResourceLocation.withDefaultNamespace("textures/entity/axolotl/axolotl_blue.png"),
    };

    /** Ghostly blue-white tint. ARGB: alpha=180, R=120, G=170, B=255 */
    private static final int GHOST_COLOR = 0xB478AAFF;

    public SpectralAxolotlRenderer(EntityRendererProvider.Context context) {
        super(context, new AxolotlModel<>(context.bakeLayer(ModelLayers.AXOLOTL)), 0.0F);

        this.addLayer(new RenderLayer<SpectralAxolotlEntity, AxolotlModel<SpectralAxolotlEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int light,
                               SpectralAxolotlEntity entity, float limbSwing, float limbSwingAmount,
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
    public ResourceLocation getTextureLocation(SpectralAxolotlEntity entity) {
        int idx = entity.getVariantIndex();
        return VARIANT_TEXTURES[idx % VARIANT_TEXTURES.length];
    }

    @Nullable
    @Override
    protected RenderType getRenderType(SpectralAxolotlEntity entity, boolean bodyVisible,
                                        boolean translucent, boolean glowing) {
        return null; // Custom layer handles rendering
    }
}
