package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SparrowmintRenderer extends MobRenderer<SparrowmintEntity, SparrowmintModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/sparrowmint.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/sparrowmint_sour.png");

    public SparrowmintRenderer(EntityRendererProvider.Context context) {
        super(context, new SparrowmintModel(context.bakeLayer(SparrowmintModel.LAYER)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(SparrowmintEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
