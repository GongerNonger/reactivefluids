package com.reactivefluids;

import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders raised skeletons using the standard skeleton model and texture.
 */
public class RaisedSkeletonRenderer extends HumanoidMobRenderer<RaisedSkeletonEntity, SkeletonModel<RaisedSkeletonEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    public RaisedSkeletonRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON_INNER_ARMOR)),
                new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(RaisedSkeletonEntity entity) {
        return TEXTURE;
    }
}
