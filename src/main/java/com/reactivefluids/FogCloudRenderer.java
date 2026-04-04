package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** No-op renderer — fog cloud is rendered via particles only. */
public class FogCloudRenderer extends EntityRenderer<FogCloudEntity> {

    public FogCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FogCloudEntity entity, float yaw, float partialTick,
                        PoseStack poseStack, MultiBufferSource buffer, int light) {
    }

    @Override
    public ResourceLocation getTextureLocation(FogCloudEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/particle/generic_0.png");
    }
}
