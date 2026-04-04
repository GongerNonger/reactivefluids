package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BuzzlegumRenderer extends MobRenderer<BuzzlegumEntity, BuzzlegumModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/buzzlegum.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/buzzlegum_sour.png");

    public BuzzlegumRenderer(EntityRendererProvider.Context context) {
        super(context, new BuzzlegumModel(context.bakeLayer(BuzzlegumModel.LAYER)), 0.9F);
    }

    @Override
    public ResourceLocation getTextureLocation(BuzzlegumEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
