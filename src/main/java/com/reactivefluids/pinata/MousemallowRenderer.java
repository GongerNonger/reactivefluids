package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MousemallowRenderer extends MobRenderer<MousemallowEntity, MousemallowModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/mousemallow.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/mousemallow_sour.png");

    public MousemallowRenderer(EntityRendererProvider.Context context) {
        super(context, new MousemallowModel(context.bakeLayer(MousemallowModel.LAYER)), 0.2F);
    }

    @Override
    public ResourceLocation getTextureLocation(MousemallowEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
