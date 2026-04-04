package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SyrupentRenderer extends MobRenderer<SyrupentEntity, SyrupentModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/syrupent.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/syrupent_sour.png");

    public SyrupentRenderer(EntityRendererProvider.Context context) {
        super(context, new SyrupentModel(context.bakeLayer(SyrupentModel.LAYER)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(SyrupentEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
