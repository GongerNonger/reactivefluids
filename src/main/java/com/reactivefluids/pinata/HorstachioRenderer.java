package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HorstachioRenderer extends MobRenderer<HorstachioEntity, HorstachioModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/horstachio.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/horstachio_sour.png");

    public HorstachioRenderer(EntityRendererProvider.Context context) {
        super(context, new HorstachioModel(context.bakeLayer(HorstachioModel.LAYER)), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(HorstachioEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
