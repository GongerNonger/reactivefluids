package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings({"unchecked", "rawtypes"})
public class LickatoadRenderer extends MobRenderer {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/lickatoad.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/lickatoad_sour.png");

    public LickatoadRenderer(EntityRendererProvider.Context context) {
        super(context, new LickatoadModel(context.bakeLayer(LickatoadModel.LAYER)), 0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.world.entity.Entity entity) {
        return ((BasePinataEntity)entity).isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
