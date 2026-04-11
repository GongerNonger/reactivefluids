package com.reactivefluids;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LunarJellyfishModel extends GeoModel<LunarJellyfishEntity> {

    @Override
    public ResourceLocation getModelResource(LunarJellyfishEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID,
                "geo/lunar_jellyfish.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LunarJellyfishEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID,
                "textures/entity/lunar_jellyfish.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LunarJellyfishEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID,
                "animations/lunar_jellyfish.animation.json");
    }
}
