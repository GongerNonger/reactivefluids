package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class QuackberryRenderer extends MobRenderer<QuackberryEntity, QuackberryModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/quackberry.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/quackberry_sour.png");

    public QuackberryRenderer(EntityRendererProvider.Context context) {
        super(context, new QuackberryModel(context.bakeLayer(QuackberryModel.LAYER)), 0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(QuackberryEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
