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

/** Cluckles — plump chicken, red comb, fan tail, small wings. Choc-chip cookie coloring. */
public class ClucklesModel extends EntityModel<BasePinataEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "cluckles"), "main");
    private final ModelPart body, head, comb, wingL, wingR, legL, legR, tail;

    public ClucklesModel(ModelPart root) {
        body = root.getChild("body"); head = root.getChild("head");
        comb = root.getChild("comb");
        wingL = root.getChild("wl"); wingR = root.getChild("wr");
        legL = root.getChild("ll"); legR = root.getChild("lr"); tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition m = new MeshDefinition(); PartDefinition r = m.getRoot();
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -3.0F, -3.0F, 6, 6, 7), PartPose.offset(0, 18, 0));
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 14)
                .addBox(-1.5F, -2.0F, -2.0F, 3, 3, 2)
                .texOffs(26, 0).addBox(-0.5F, -0.5F, -3.0F, 1, 1, 1), // beak
                PartPose.offset(0, 15.5F, -3));
        r.addOrReplaceChild("comb", CubeListBuilder.create().texOffs(10, 14)
                .addBox(-0.5F, -2.0F, -1.0F, 1, 2, 2), PartPose.offset(0, 13.5F, -2.5F));
        r.addOrReplaceChild("wl", CubeListBuilder.create().texOffs(0, 20)
                .addBox(0, -1, -1, 1, 3, 4), PartPose.offset(3, 17, -1));
        r.addOrReplaceChild("wr", CubeListBuilder.create().texOffs(0, 20)
                .addBox(-1, -1, -1, 1, 3, 4), PartPose.offset(-3, 17, -1));
        r.addOrReplaceChild("ll", CubeListBuilder.create().texOffs(26, 3)
                .addBox(-0.5F, 0, -1, 1, 3, 2), PartPose.offset(1.5F, 21, 0));
        r.addOrReplaceChild("lr", CubeListBuilder.create().texOffs(26, 3)
                .addBox(-0.5F, 0, -1, 1, 3, 2), PartPose.offset(-1.5F, 21, 0));
        r.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(14, 14)
                .addBox(-1.5F, -3, 0, 3, 3, 2), PartPose.offset(0, 17, 4));
        return LayerDefinition.create(m, 32, 32);
    }

    @Override
    public void setupAnim(BasePinataEntity e, float ls, float la, float age, float hy, float hp) {
        head.yRot = hy * 0.017F; head.xRot = hp * 0.017F * 0.3F;
        comb.yRot = head.yRot; comb.xRot = head.xRot;
        float peck = (float) Math.sin(ls * 0.8F) * la * 0.5F;
        legL.xRot = peck; legR.xRot = -peck;
        wingL.zRot = -(float) Math.sin(age * 0.2F) * 0.1F - 0.1F;
        wingR.zRot = (float) Math.sin(age * 0.2F) * 0.1F + 0.1F;
        tail.xRot = -0.5F + (float) Math.sin(age * 0.1F) * 0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer b, int l, int o, int c) {
        body.render(ps,b,l,o,c); head.render(ps,b,l,o,c); comb.render(ps,b,l,o,c);
        wingL.render(ps,b,l,o,c); wingR.render(ps,b,l,o,c);
        legL.render(ps,b,l,o,c); legR.render(ps,b,l,o,c); tail.render(ps,b,l,o,c);
    }
}
