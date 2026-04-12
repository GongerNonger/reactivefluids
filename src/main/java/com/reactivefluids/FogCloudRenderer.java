package com.reactivefluids;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** No-op renderer — fog cloud is rendered via particles only. */
public class FogCloudRenderer extends EntityRenderer<FogCloudEntity, EntityRenderState> {

    public FogCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
