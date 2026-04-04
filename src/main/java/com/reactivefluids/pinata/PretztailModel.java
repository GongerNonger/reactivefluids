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

/** Pretztail — sleek fox body, pointy ears, long snout, bushy pretzel-shaped tail. */
public class PretztailModel extends EntityModel<PretztailEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "pretztail"), "main");
    private final ModelPart body, head, earL, earR, tail, legFL, legFR, legBL, legBR;

    public PretztailModel(ModelPart root) {
        body = root.getChild("body"); head = root.getChild("head");
        earL = root.getChild("earL"); earR = root.getChild("earR");
        tail = root.getChild("tail");
        legFL = root.getChild("lfl"); legFR = root.getChild("lfr");
        legBL = root.getChild("lbl"); legBR = root.getChild("lbr");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition m = new MeshDefinition(); PartDefinition r = m.getRoot();
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2.5F, -2.5F, -4.0F, 5, 5, 8), PartPose.offset(0, 17, 0));
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 14)
                .addBox(-2.0F, -2.0F, -3.0F, 4, 4, 3)
                .texOffs(26, 0).addBox(-1.0F, -0.5F, -5.0F, 2, 2, 2), // snout
                PartPose.offset(0, 15.5F, -4));
        r.addOrReplaceChild("earL", CubeListBuilder.create().texOffs(14, 14)
                .addBox(-1, -2, 0, 2, 3, 1), PartPose.offset(1.5F, 13.5F, -3.5F));
        r.addOrReplaceChild("earR", CubeListBuilder.create().texOffs(14, 14)
                .addBox(-1, -2, 0, 2, 3, 1), PartPose.offset(-1.5F, 13.5F, -3.5F));
        // Bushy pretzel tail
        r.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(18, 0)
                .addBox(-1.5F, -2.0F, 0, 3, 3, 6), PartPose.offset(0, 15.5F, 4));
        r.addOrReplaceChild("lfl", CubeListBuilder.create().texOffs(26, 5)
                .addBox(-0.5F, 0, -0.5F, 1, 5, 1), PartPose.offset(2, 19.5F, -3));
        r.addOrReplaceChild("lfr", CubeListBuilder.create().texOffs(26, 5)
                .addBox(-0.5F, 0, -0.5F, 1, 5, 1), PartPose.offset(-2, 19.5F, -3));
        r.addOrReplaceChild("lbl", CubeListBuilder.create().texOffs(26, 5)
                .addBox(-0.5F, 0, -0.5F, 1, 5, 1), PartPose.offset(2, 19.5F, 3));
        r.addOrReplaceChild("lbr", CubeListBuilder.create().texOffs(26, 5)
                .addBox(-0.5F, 0, -0.5F, 1, 5, 1), PartPose.offset(-2, 19.5F, 3));
        return LayerDefinition.create(m, 32, 32);
    }

    @Override
    public void setupAnim(PretztailEntity e, float ls, float la, float age, float hy, float hp) {
        head.yRot = hy * 0.017F; head.xRot = hp * 0.017F * 0.4F;
        float trot = (float) Math.sin(ls * 0.8F) * la * 0.6F;
        legFL.xRot = trot; legFR.xRot = -trot; legBL.xRot = -trot; legBR.xRot = trot;
        tail.yRot = (float) Math.sin(age * 0.15F) * 0.3F;
        tail.xRot = -0.4F + (float) Math.sin(age * 0.1F) * 0.05F;
        earL.zRot = (float) Math.sin(age * 0.25F) * 0.08F;
        earR.zRot = -(float) Math.sin(age * 0.25F) * 0.08F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer b, int l, int o, int c) {
        body.render(ps,b,l,o,c); head.render(ps,b,l,o,c);
        earL.render(ps,b,l,o,c); earR.render(ps,b,l,o,c); tail.render(ps,b,l,o,c);
        legFL.render(ps,b,l,o,c); legFR.render(ps,b,l,o,c);
        legBL.render(ps,b,l,o,c); legBR.render(ps,b,l,o,c);
    }
}
