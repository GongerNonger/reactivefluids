package com.reactivefluids;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * The dancing light entity is rendered purely via particles (spawned in tick()),
 * so this renderer intentionally draws nothing.
 */
public class DancingLightRenderer extends EntityRenderer<DancingLightEntity, EntityRenderState> {

    public DancingLightRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
