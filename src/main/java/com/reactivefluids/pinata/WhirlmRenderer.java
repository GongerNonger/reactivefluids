package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Whirlm piñata entity.
 * Uses the segmented worm model with piñata-paper texture.
 */
public class WhirlmRenderer extends MobRenderer<WhirlmEntity, WhirlmModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/whirlm.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/whirlm_sour.png");

    public WhirlmRenderer(EntityRendererProvider.Context context) {
        super(context, new WhirlmModel(context.bakeLayer(WhirlmModel.LAYER)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(WhirlmEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
