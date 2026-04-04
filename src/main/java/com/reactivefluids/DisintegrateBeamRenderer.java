package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** No-op renderer — the beam is rendered via particles spawned in DisintegrateBeamEntity.tick(). */
public class DisintegrateBeamRenderer extends EntityRenderer<DisintegrateBeamEntity> {

    public DisintegrateBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DisintegrateBeamEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int light) {
        // Intentionally empty — particles handle the visual
    }

    @Override
    public ResourceLocation getTextureLocation(DisintegrateBeamEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/particle/generic_0.png");
    }
}
