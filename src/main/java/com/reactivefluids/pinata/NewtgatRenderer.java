package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NewtgatRenderer extends MobRenderer<NewtgatEntity, NewtgatModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/newtgat.png");
    private static final ResourceLocation SOUR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/newtgat_sour.png");

    public NewtgatRenderer(EntityRendererProvider.Context context) {
        super(context, new NewtgatModel(context.bakeLayer(NewtgatModel.LAYER)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(NewtgatEntity entity) {
        return entity.isSour() ? SOUR_TEXTURE : TEXTURE;
    }
}
