package com.reactivefluids;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for Moonlight Jellyfish.
 * References the Manus-generated .geo.json and .animation.json files.
 */
public class MoonlightJellyfishModel extends GeoModel<MoonlightJellyfishEntity> {

    @Override
    public ResourceLocation getModelResource(MoonlightJellyfishEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID,
                "geo/moonlight_jellyfish.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MoonlightJellyfishEntity entity) {
        if (entity.isGreenVariant()) {
            return ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID,
                    "textures/entity/moonlight_jellyfish_green.png");
        }
        return ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID,
                "textures/entity/moonlight_jellyfish.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MoonlightJellyfishEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID,
                "animations/moonlight_jellyfish.animation.json");
    }
}
