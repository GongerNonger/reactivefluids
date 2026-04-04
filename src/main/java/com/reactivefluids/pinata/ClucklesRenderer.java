package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ClucklesRenderer extends MobRenderer<ClucklesEntity, ClucklesModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/cluckles.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/cluckles_sour.png");

    public ClucklesRenderer(EntityRendererProvider.Context context) {
        super(context, new ClucklesModel(context.bakeLayer(ClucklesModel.LAYER)), 0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(ClucklesEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
