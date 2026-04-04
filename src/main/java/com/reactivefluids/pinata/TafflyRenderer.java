package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings({"unchecked", "rawtypes"})
public class TafflyRenderer extends MobRenderer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/taffly.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/taffly_sour.png");

    public TafflyRenderer(EntityRendererProvider.Context context) {
        super(context, new TafflyModel(context.bakeLayer(TafflyModel.LAYER)), 0.2F);
    }

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.world.entity.Entity entity) {
        return ((BasePinataEntity)entity).isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
