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

/** Lickatoad — wide squat frog, big eyes on top, strong back legs. Lollipop-green. */
public class LickatoadModel extends EntityModel<BasePinataEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "lickatoad"), "main");
    private final ModelPart body, head, legFL, legFR, legBL, legBR;

    public LickatoadModel(ModelPart root) {
        body = root.getChild("body"); head = root.getChild("head");
        legFL = root.getChild("lfl"); legFR = root.getChild("lfr");
        legBL = root.getChild("lbl"); legBR = root.getChild("lbr");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition m = new MeshDefinition(); PartDefinition r = m.getRoot();
        // Wide squat body
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.5F, -2.5F, -3.0F, 7, 4, 6), PartPose.offset(0, 20.5F, 0));
        // Head with big eye bumps
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 11)
                .addBox(-3.0F, -2.0F, -3.0F, 6, 3, 3)
                // Eye bumps
                .texOffs(26, 0).addBox(-3.0F, -3.5F, -2.0F, 2, 2, 2)
                .texOffs(26, 0).addBox(1.0F, -3.5F, -2.0F, 2, 2, 2),
                PartPose.offset(0, 19.5F, -3));
        // Front legs (small)
        r.addOrReplaceChild("lfl", CubeListBuilder.create().texOffs(18, 11)
                .addBox(-0.5F, 0, -0.5F, 1, 2, 1), PartPose.offset(3, 22, -2));
        r.addOrReplaceChild("lfr", CubeListBuilder.create().texOffs(18, 11)
                .addBox(-0.5F, 0, -0.5F, 1, 2, 1), PartPose.offset(-3, 22, -2));
        // Back legs (large, powerful)
        r.addOrReplaceChild("lbl", CubeListBuilder.create().texOffs(22, 11)
                .addBox(-1, 0, -1, 2, 3, 3), PartPose.offset(3.5F, 21, 2));
        r.addOrReplaceChild("lbr", CubeListBuilder.create().texOffs(22, 11)
                .addBox(-1, 0, -1, 2, 3, 3), PartPose.offset(-3.5F, 21, 2));
        return LayerDefinition.create(m, 32, 16);
    }

    @Override
    public void setupAnim(BasePinataEntity e, float ls, float la, float age, float hy, float hp) {
        head.yRot = hy * 0.017F;
        float hop = (float) Math.sin(ls * 1.5F) * la;
        legBL.xRot = hop * 0.5F; legBR.xRot = hop * 0.5F;
        legFL.xRot = -hop * 0.3F; legFR.xRot = -hop * 0.3F;
        // Throat pulse
        body.yScale = 1.0F + (float) Math.sin(age * 0.3F) * 0.03F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer b, int l, int o, int c) {
        body.render(ps,b,l,o,c); head.render(ps,b,l,o,c);
        legFL.render(ps,b,l,o,c); legFR.render(ps,b,l,o,c);
        legBL.render(ps,b,l,o,c); legBR.render(ps,b,l,o,c);
    }
}
