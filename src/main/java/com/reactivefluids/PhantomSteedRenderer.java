package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HorseModel;
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
 * Renders the phantom steed as a translucent, self-lit ghostly horse.
 * Uses entityTranslucentCull to hide inner face z-clipping on neck/legs.
 */
public class PhantomSteedRenderer extends MobRenderer<PhantomSteedEntity, HorseModel<PhantomSteedEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("reactivefluids", "textures/entity/phantom_steed.png");

    /** Ghostly blue-white, ~70% opaque. ARGB: alpha=180, R=120, G=170, B=255 */
    private static final int GHOST_COLOR = 0xB478AAFF;

    public PhantomSteedRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseModel<>(context.bakeLayer(ModelLayers.HORSE)), 0.0F);

        this.addLayer(new RenderLayer<PhantomSteedEntity, HorseModel<PhantomSteedEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int light,
                               PhantomSteedEntity entity, float limbSwing, float limbSwingAmount,
                               float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                int overlay = OverlayTexture.pack(
                        OverlayTexture.u(0),
                        OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));
                VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
                getParentModel().renderToBuffer(poseStack, consumer, 15728880, overlay, GHOST_COLOR);
            }
        });
    }

    @Override
    protected void scale(PhantomSteedEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.1F, 1.1F, 1.1F);
    }

    @Override
    public ResourceLocation getTextureLocation(PhantomSteedEntity entity) {
        return TEXTURE;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(PhantomSteedEntity entity, boolean bodyVisible,
                                        boolean translucent, boolean glowing) {
        return null;
    }

    @Override
    protected float getWhiteOverlayProgress(PhantomSteedEntity entity, float partialTick) {
        int remaining = entity.getTicksRemaining();
        if (remaining <= PhantomSteedEntity.FADE_TICKS) {
            return (remaining % 20 < 10) ? 0.5f : 0.0f;
        }
        return 0.0f;
    }
}
