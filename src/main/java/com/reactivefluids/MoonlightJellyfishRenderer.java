package com.reactivefluids;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

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
