package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ShellybeanRenderer extends MobRenderer<ShellybeanEntity, ShellybeanModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/shellybean.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/shellybean_sour.png");

    public ShellybeanRenderer(EntityRendererProvider.Context context) {
        super(context, new ShellybeanModel(context.bakeLayer(ShellybeanModel.LAYER)), 0.1F);
    }

    @Override
    public ResourceLocation getTextureLocation(ShellybeanEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
