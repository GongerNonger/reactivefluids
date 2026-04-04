package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings({"unchecked", "rawtypes"})
public class NewtgatRenderer extends MobRenderer {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/newtgat.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/newtgat_sour.png");

    public NewtgatRenderer(EntityRendererProvider.Context context) {
        super(context, new NewtgatModel(context.bakeLayer(NewtgatModel.LAYER)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.world.entity.Entity entity) {
        return ((BasePinataEntity)entity).isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
