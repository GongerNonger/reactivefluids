package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Generic GeckoLib GeoModel for piñata species that have Bedrock geo.json models.
 * Looks for model at: assets/reactivefluids/geo/entity/pinata/{species}.geo.json
 * Looks for texture at: assets/reactivefluids/textures/entity/pinata/{species}.png
 * No animations file (idle animations handled procedurally).
 */
public class PinataGeoModel extends GeoModel<BasePinataEntity> {

    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final ResourceLocation animation;

    public PinataGeoModel(String species) {
        this(species, false);
    }

    public PinataGeoModel(String species, boolean hasAnimation) {
        this.model = ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID,
                "geo/entity/pinata/" + species + ".geo.json");
        this.texture = ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID,
                "textures/entity/pinata/" + species + ".png");
        this.animation = hasAnimation ? ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID,
                "animations/entity/pinata/" + species + ".animation.json") : null;
    }

    @Override
    public ResourceLocation getModelResource(BasePinataEntity entity) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(BasePinataEntity entity) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(BasePinataEntity entity) {
        return animation;
    }
}
