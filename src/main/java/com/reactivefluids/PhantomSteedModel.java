package com.reactivefluids;

import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/**
 * Thin subclass of HorseModel that exposes the protected body/headParts fields
 * so the renderer can access saddle sub-parts for two-pass rendering.
 */
public class PhantomSteedModel<T extends AbstractHorse> extends HorseModel<T> {

    public PhantomSteedModel(ModelPart root) {
        super(root);
    }

    public ModelPart getBody() { return this.body; }
    public ModelPart getHeadParts() { return this.headParts; }
}
