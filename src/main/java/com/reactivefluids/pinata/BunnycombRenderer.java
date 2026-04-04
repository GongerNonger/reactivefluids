package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BunnycombRenderer extends MobRenderer<BunnycombEntity, BunnycombModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/bunnycomb.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/bunnycomb_sour.png");

    public BunnycombRenderer(EntityRendererProvider.Context context) {
        super(context, new BunnycombModel(context.bakeLayer(BunnycombModel.LAYER)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(BunnycombEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
