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

/** Quackberry — flat-billed duck, wide body, stubby wings, flat webbed feet. Blue-purple. */
public class QuackberryModel extends EntityModel<QuackberryEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "quackberry"), "main");
    private final ModelPart body, head, wingL, wingR, legL, legR, tail;

    public QuackberryModel(ModelPart root) {
        body = root.getChild("body"); head = root.getChild("head");
        wingL = root.getChild("wl"); wingR = root.getChild("wr");
        legL = root.getChild("ll"); legR = root.getChild("lr"); tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition m = new MeshDefinition(); PartDefinition r = m.getRoot();
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.5F, -3.0F, -4.0F, 7, 5, 8), PartPose.offset(0, 19, 0));
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 14)
                .addBox(-2.0F, -2.0F, -2.0F, 4, 4, 3)
                .texOffs(22, 0).addBox(-1.5F, 0.0F, -4.0F, 3, 1, 2), // flat bill
                PartPose.offset(0, 16, -4));
        r.addOrReplaceChild("wl", CubeListBuilder.create().texOffs(0, 22)
                .addBox(0, -1, -1, 1, 3, 5), PartPose.offset(3.5F, 17.5F, -1));
        r.addOrReplaceChild("wr", CubeListBuilder.create().texOffs(0, 22)
                .addBox(-1, -1, -1, 1, 3, 5), PartPose.offset(-3.5F, 17.5F, -1));
        r.addOrReplaceChild("ll", CubeListBuilder.create().texOffs(22, 4)
                .addBox(-1, 0, -1.5F, 2, 2, 3), PartPose.offset(2, 22, 0));
        r.addOrReplaceChild("lr", CubeListBuilder.create().texOffs(22, 4)
                .addBox(-1, 0, -1.5F, 2, 2, 3), PartPose.offset(-2, 22, 0));
        r.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(14, 14)
                .addBox(-1.5F, -1, 0, 3, 2, 2), PartPose.offset(0, 17.5F, 4));
        return LayerDefinition.create(m, 32, 32);
    }

    @Override
    public void setupAnim(QuackberryEntity e, float ls, float la, float age, float hy, float hp) {
        head.yRot = hy * 0.017F; head.xRot = hp * 0.017F * 0.4F;
        float waddle = (float) Math.sin(ls * 0.8F) * la * 0.4F;
        legL.xRot = waddle; legR.xRot = -waddle;
        wingL.zRot = -(float) Math.sin(age * 0.2F) * 0.15F - 0.1F;
        wingR.zRot = (float) Math.sin(age * 0.2F) * 0.15F + 0.1F;
        tail.xRot = (float) Math.sin(age * 0.15F) * 0.15F - 0.2F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer b, int l, int o, int c) {
        body.render(ps,b,l,o,c); head.render(ps,b,l,o,c);
        wingL.render(ps,b,l,o,c); wingR.render(ps,b,l,o,c);
        legL.render(ps,b,l,o,c); legR.render(ps,b,l,o,c); tail.render(ps,b,l,o,c);
    }
}
