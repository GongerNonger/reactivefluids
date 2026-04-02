package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

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

        // Ghostly translucent body layer
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

        // Item-in-mouth layer (reproduces FoxHeldItemLayer for our entity type)
        ItemInHandRenderer itemRenderer = context.getItemInHandRenderer();
        this.addLayer(new RenderLayer<SpectralFoxEntity, FoxModel<SpectralFoxEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int light,
                               SpectralFoxEntity entity, float limbSwing, float limbSwingAmount,
                               float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                ItemStack held = entity.getItemBySlot(EquipmentSlot.MAINHAND);
                if (held.isEmpty()) return;

                boolean sleeping = entity.isSleeping();
                boolean baby = entity.isBaby();
                poseStack.pushPose();
                if (baby) {
                    poseStack.scale(0.75F, 0.75F, 0.75F);
                    poseStack.translate(0.0F, 0.5F, 0.209375F);
                }

                poseStack.translate(
                        getParentModel().head.x / 16.0F,
                        getParentModel().head.y / 16.0F,
                        getParentModel().head.z / 16.0F);
                float roll = entity.getHeadRollAngle(partialTick);
                poseStack.mulPose(Axis.ZP.rotation(roll));
                poseStack.mulPose(Axis.YP.rotationDegrees(netHeadYaw));
                poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));
                if (baby) {
                    if (sleeping) {
                        poseStack.translate(0.4F, 0.26F, 0.15F);
                    } else {
                        poseStack.translate(0.06F, 0.26F, -0.5F);
                    }
                } else if (sleeping) {
                    poseStack.translate(0.46F, 0.26F, 0.22F);
                } else {
                    poseStack.translate(0.06F, 0.27F, -0.5F);
                }

                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                if (sleeping) {
                    poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                }

                itemRenderer.renderItem(entity, held, ItemDisplayContext.GROUND, false, poseStack, buffer, light);
                poseStack.popPose();
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
