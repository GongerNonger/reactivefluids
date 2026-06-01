package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Renders the Tropical Slime with two layers, mirroring vanilla SlimeRenderer:
 *   1. Inner body (cutout) — uses animated tropical_slime_inner.png
 *   2. Outer shell (translucent) — uses tropical_slime.png
 *
 * The base model renders the inner body; the outer layer is added via RenderLayer.
 */
@OnlyIn(Dist.CLIENT)
public class TropicalSlimeRenderer extends MobRenderer<TropicalSlimeEntity, TropicalSlimeModel<TropicalSlimeEntity>> {

    /** Model layer locations — registered in ClientEvents */
    public static final ModelLayerLocation TROPICAL_SLIME_INNER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "tropical_slime"), "inner");
    public static final ModelLayerLocation TROPICAL_SLIME_OUTER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "tropical_slime"), "outer");

    private static final ResourceLocation INNER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/tropical_slime_inner_animated");
    private static final ResourceLocation OUTER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/tropical_slime.png");

    public TropicalSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new TropicalSlimeModel<>(context.bakeLayer(TROPICAL_SLIME_INNER)), 0.25F);

        // Outer shell layer — translucent, rendered on top of the inner body
        ModelPart outerPart = context.bakeLayer(TROPICAL_SLIME_OUTER);
        this.addLayer(new RenderLayer<TropicalSlimeEntity, TropicalSlimeModel<TropicalSlimeEntity>>(this) {
            private final TropicalSlimeModel<TropicalSlimeEntity> outerModel = new TropicalSlimeModel<>(outerPart);

            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                               TropicalSlimeEntity entity, float limbSwing, float limbSwingAmount,
                               float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                Minecraft minecraft = Minecraft.getInstance();
                boolean glowOutline = minecraft.shouldEntityAppearGlowing(entity) && entity.isInvisible();
                if (!entity.isInvisible() || glowOutline) {
                    VertexConsumer consumer;
                    if (glowOutline) {
                        consumer = buffer.getBuffer(RenderType.outline(OUTER_TEXTURE));
                    } else {
                        consumer = buffer.getBuffer(RenderType.entityTranslucent(OUTER_TEXTURE));
                    }
                    this.getParentModel().copyPropertiesTo(outerModel);
                    outerModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
                    outerModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                    outerModel.renderToBuffer(poseStack, consumer, packedLight,
                            LivingEntityRenderer.getOverlayCoords(entity, 0.0F));
                }
            }
        });
    }

    @Override
    public void render(TropicalSlimeEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.shadowRadius = 0.25F * (float) entity.getSize();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(TropicalSlimeEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = (float) entity.getSize();
        float squish = Mth.lerp(partialTick, entity.oSquish, entity.squish) / (size * 0.5F + 1.0F);
        float stretch = 1.0F / (squish + 1.0F);
        poseStack.scale(stretch * size, 1.0F / stretch * size, stretch * size);
    }

    @Override
    public ResourceLocation getTextureLocation(TropicalSlimeEntity entity) {
        return INNER_TEXTURE;
    }
}
