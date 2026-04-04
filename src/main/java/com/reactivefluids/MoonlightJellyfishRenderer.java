package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * GeckoLib renderer for Moonlight Jellyfish.
 * Handles translucent rendering with emissive glow overlay.
 */
public class MoonlightJellyfishRenderer extends GeoEntityRenderer<MoonlightJellyfishEntity> {

    public MoonlightJellyfishRenderer(EntityRendererProvider.Context context) {
        super(context, new MoonlightJellyfishModel());
        this.shadowRadius = 0.2F;
    }

    @Override
    public RenderType getRenderType(MoonlightJellyfishEntity entity, ResourceLocation texture,
                                     MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucentCull(texture);
    }
}
