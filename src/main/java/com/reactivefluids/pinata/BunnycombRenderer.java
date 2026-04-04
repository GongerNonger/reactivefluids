package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BunnycombRenderer extends MobRenderer {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/bunnycomb.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/bunnycomb_sour.png");

    public BunnycombRenderer(EntityRendererProvider.Context context) {
        super(context, new BunnycombModel(context.bakeLayer(BunnycombModel.LAYER)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.world.entity.Entity entity) {
        return ((BasePinataEntity)entity).isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
