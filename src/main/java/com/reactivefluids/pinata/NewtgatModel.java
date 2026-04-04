package com.reactivefluids.pinata;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import com.reactivefluids.ReactiveFluids;

/** Newtgat — flat lizard body, paddle tail, 4 splayed legs. Nougat-brown/orange. */
public class NewtgatModel extends EntityModel<BasePinataEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "newtgat"), "main");
    private final ModelPart body, head, tail, legFL, legFR, legBL, legBR;

    public NewtgatModel(ModelPart root) {
        body = root.getChild("body"); head = root.getChild("head"); tail = root.getChild("tail");
        legFL = root.getChild("lfl"); legFR = root.getChild("lfr");
        legBL = root.getChild("lbl"); legBR = root.getChild("lbr");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition m = new MeshDefinition(); PartDefinition r = m.getRoot();
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2.0F, -1.5F, -3.0F, 4, 3, 6), PartPose.offset(0, 22, 0));
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-1.5F, -1.0F, -2.5F, 3, 2, 3), PartPose.offset(0, 21.5F, -3));
        r.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(14, 0)
                .addBox(-1.0F, -0.5F, 0, 2, 1, 5), PartPose.offset(0, 22, 3));
        r.addOrReplaceChild("lfl", CubeListBuilder.create().texOffs(14, 7)
                .addBox(0, 0, -0.5F, 2, 2, 1), PartPose.offset(2, 22.5F, -2));
        r.addOrReplaceChild("lfr", CubeListBuilder.create().texOffs(14, 7)
                .addBox(-2, 0, -0.5F, 2, 2, 1), PartPose.offset(-2, 22.5F, -2));
        r.addOrReplaceChild("lbl", CubeListBuilder.create().texOffs(14, 7)
                .addBox(0, 0, -0.5F, 2, 2, 1), PartPose.offset(2, 22.5F, 2));
        r.addOrReplaceChild("lbr", CubeListBuilder.create().texOffs(14, 7)
                .addBox(-2, 0, -0.5F, 2, 2, 1), PartPose.offset(-2, 22.5F, 2));
        return LayerDefinition.create(m, 32, 16);
    }

    @Override
    public void setupAnim(BasePinataEntity e, float ls, float la, float age, float hy, float hp) {
        head.yRot = hy * 0.017F;
        float walk = (float) Math.sin(ls * 0.8F) * la * 0.6F;
        legFL.yRot = walk; legFR.yRot = -walk; legBL.yRot = -walk; legBR.yRot = walk;
        tail.yRot = (float) Math.sin(age * 0.2F + ls * 0.3F) * 0.3F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer b, int l, int o, int c) {
        body.render(ps,b,l,o,c); head.render(ps,b,l,o,c); tail.render(ps,b,l,o,c);
        legFL.render(ps,b,l,o,c); legFR.render(ps,b,l,o,c);
        legBL.render(ps,b,l,o,c); legBR.render(ps,b,l,o,c);
    }
}
