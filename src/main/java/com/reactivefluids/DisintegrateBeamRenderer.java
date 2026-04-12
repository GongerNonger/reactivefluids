package com.reactivefluids;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** No-op renderer — the beam is rendered via particles spawned in DisintegrateBeamEntity.tick(). */
public class DisintegrateBeamRenderer extends EntityRenderer<DisintegrateBeamEntity, EntityRenderState> {

    public DisintegrateBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
