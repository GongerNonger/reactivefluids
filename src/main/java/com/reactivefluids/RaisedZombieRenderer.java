package com.reactivefluids;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders raised zombies with a custom necromantic texture.
 */
public class RaisedZombieRenderer extends AbstractZombieRenderer<RaisedZombieEntity, ZombieModel<RaisedZombieEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("reactivefluids", "textures/entity/raised_zombie.png");

    public RaisedZombieRenderer(EntityRendererProvider.Context context) {
        super(context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
    }

    @Override
    public ResourceLocation getTextureLocation(RaisedZombieEntity entity) {
        return TEXTURE;
    }
}
