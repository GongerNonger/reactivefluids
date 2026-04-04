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

/** Horstachio — full horse, pistachio-green mane/tail, strong legs. Largest basic piñata. */
public class HorstachioModel extends EntityModel<BasePinataEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "horstachio"), "main");
    private final ModelPart body, head, neck, mane, tail, legFL, legFR, legBL, legBR;

    public HorstachioModel(ModelPart root) {
        body = root.getChild("body"); head = root.getChild("head"); neck = root.getChild("neck");
        mane = root.getChild("mane"); tail = root.getChild("tail");
        legFL = root.getChild("lfl"); legFR = root.getChild("lfr");
        legBL = root.getChild("lbl"); legBR = root.getChild("lbr");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition m = new MeshDefinition(); PartDefinition r = m.getRoot();
        // Large barrel body
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -4.0F, -6.0F, 8, 8, 12), PartPose.offset(0, 12, 0));
        // Neck
        r.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 21)
                .addBox(-2.0F, -5.0F, -2.0F, 4, 7, 4), PartPose.offset(0, 10, -6));
        // Head
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(28, 0)
                .addBox(-2.0F, -2.0F, -5.0F, 4, 4, 5)
                .texOffs(40, 0).addBox(-1.5F, 0, -7.0F, 3, 2, 2), // muzzle
                PartPose.offset(0, 6, -7));
        // Mane along neck
        r.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(16, 21)
                .addBox(-0.5F, -6.0F, -1.0F, 1, 6, 3), PartPose.offset(0, 10, -6));
        // Tail
        r.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(40, 10)
                .addBox(-1.0F, -1.0F, 0, 2, 2, 8), PartPose.offset(0, 10, 6));
        // Legs
        r.addOrReplaceChild("lfl", CubeListBuilder.create().texOffs(28, 10)
                .addBox(-1, 0, -1, 2, 8, 2), PartPose.offset(3, 16, -4));
        r.addOrReplaceChild("lfr", CubeListBuilder.create().texOffs(28, 10)
                .addBox(-1, 0, -1, 2, 8, 2), PartPose.offset(-3, 16, -4));
        r.addOrReplaceChild("lbl", CubeListBuilder.create().texOffs(36, 10)
                .addBox(-1, 0, -1, 2, 8, 2), PartPose.offset(3, 16, 4));
        r.addOrReplaceChild("lbr", CubeListBuilder.create().texOffs(36, 10)
                .addBox(-1, 0, -1, 2, 8, 2), PartPose.offset(-3, 16, 4));
        return LayerDefinition.create(m, 64, 32);
    }

    @Override
    public void setupAnim(BasePinataEntity e, float ls, float la, float age, float hy, float hp) {
        head.yRot = hy * 0.017F * 0.5F; head.xRot = hp * 0.017F * 0.3F;
        neck.yRot = head.yRot * 0.5F;
        float gallop = (float) Math.sin(ls * 0.6F) * la * 0.7F;
        legFL.xRot = gallop; legFR.xRot = -gallop;
        legBL.xRot = -gallop; legBR.xRot = gallop;
        tail.xRot = -0.3F + (float) Math.sin(age * 0.1F) * 0.1F;
        tail.yRot = (float) Math.sin(age * 0.08F) * 0.2F;
        mane.xRot = (float) Math.sin(age * 0.15F) * 0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer b, int l, int o, int c) {
        body.render(ps,b,l,o,c); neck.render(ps,b,l,o,c); head.render(ps,b,l,o,c);
        mane.render(ps,b,l,o,c); tail.render(ps,b,l,o,c);
        legFL.render(ps,b,l,o,c); legFR.render(ps,b,l,o,c);
        legBL.render(ps,b,l,o,c); legBR.render(ps,b,l,o,c);
    }
}
