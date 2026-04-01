package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * The dancing light entity is rendered purely via particles (spawned in tick()),
 * so this renderer intentionally draws nothing.
 */
public class DancingLightRenderer extends EntityRenderer<DancingLightEntity> {

    public DancingLightRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DancingLightEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int light) {
        // No model — rendered entirely via particles in DancingLightEntity.tick()
    }

    @Override
    public ResourceLocation getTextureLocation(DancingLightEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/particle/generic_0.png");
    }
}
