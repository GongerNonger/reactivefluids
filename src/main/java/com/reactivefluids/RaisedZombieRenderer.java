package com.reactivefluids;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Renders raised zombies using the standard zombie model and texture.
 */
public class RaisedZombieRenderer extends AbstractZombieRenderer<RaisedZombieEntity, ZombieModel<RaisedZombieEntity>> {

    public RaisedZombieRenderer(EntityRendererProvider.Context context) {
        super(context,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
    }
}
