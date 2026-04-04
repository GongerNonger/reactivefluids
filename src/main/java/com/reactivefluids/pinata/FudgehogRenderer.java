package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FudgehogRenderer extends MobRenderer<FudgehogEntity, FudgehogModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/fudgehog.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/fudgehog_sour.png");

    public FudgehogRenderer(EntityRendererProvider.Context context) {
        super(context, new FudgehogModel(context.bakeLayer(FudgehogModel.LAYER)), 0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(FudgehogEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
