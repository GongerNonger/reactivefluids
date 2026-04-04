package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PretztailRenderer extends MobRenderer<PretztailEntity, PretztailModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/pretztail.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/pretztail_sour.png");

    public PretztailRenderer(EntityRendererProvider.Context context) {
        super(context, new PretztailModel(context.bakeLayer(PretztailModel.LAYER)), 0.45F);
    }

    @Override
    public ResourceLocation getTextureLocation(PretztailEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
