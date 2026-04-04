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

/** Buzzlegum — rotund striped bee body, small wings, friendly face. Bubblegum-pink/yellow. */
public class BuzzlegumModel extends EntityModel<BasePinataEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "buzzlegum"), "main");
    private final ModelPart body, head, wingL, wingR, legL, legR, stinger;

    public BuzzlegumModel(ModelPart root) {
        body = root.getChild("body"); head = root.getChild("head");
        wingL = root.getChild("wl"); wingR = root.getChild("wr");
        legL = root.getChild("ll"); legR = root.getChild("lr");
        stinger = root.getChild("stinger");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition m = new MeshDefinition(); PartDefinition r = m.getRoot();
        // Rotund body
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -3.0F, -3.0F, 6, 6, 7), PartPose.offset(0, 19, 0));
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 14)
                .addBox(-2.0F, -2.0F, -2.0F, 4, 4, 2), PartPose.offset(0, 17.5F, -3));
        // Wings on top
        r.addOrReplaceChild("wl", CubeListBuilder.create().texOffs(0, 21)
                .addBox(0, 0, -1, 5, 0, 4), PartPose.offset(1.5F, 16, 0));
        r.addOrReplaceChild("wr", CubeListBuilder.create().texOffs(0, 21)
                .addBox(-5, 0, -1, 5, 0, 4), PartPose.offset(-1.5F, 16, 0));
        // Tiny legs
        r.addOrReplaceChild("ll", CubeListBuilder.create().texOffs(20, 0)
                .addBox(-0.5F, 0, -0.5F, 1, 2, 1), PartPose.offset(2, 22, 0));
        r.addOrReplaceChild("lr", CubeListBuilder.create().texOffs(20, 0)
                .addBox(-0.5F, 0, -0.5F, 1, 2, 1), PartPose.offset(-2, 22, 0));
        // Stinger
        r.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(20, 4)
                .addBox(-0.5F, -0.5F, 0, 1, 1, 2), PartPose.offset(0, 19, 4));
        return LayerDefinition.create(m, 32, 32);
    }

    @Override
    public void setupAnim(BasePinataEntity e, float ls, float la, float age, float hy, float hp) {
        head.yRot = hy * 0.017F;
        // Fast wing buzz
        float buzz = (float) Math.sin(age * 2.5F) * 0.7F;
        wingL.zRot = -buzz - 0.2F; wingR.zRot = buzz + 0.2F;
        // Hover bob
        float bob = (float) Math.sin(age * 0.5F) * 0.3F;
        body.y = 19 + bob; head.y = 17.5F + bob;
        wingL.y = 16 + bob; wingR.y = 16 + bob;
        legL.y = 22 + bob; legR.y = 22 + bob;
        stinger.y = 19 + bob;
        // Legs dangle
        legL.xRot = 0.1F + (float) Math.sin(age * 0.3F) * 0.1F;
        legR.xRot = 0.1F - (float) Math.sin(age * 0.3F) * 0.1F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer b, int l, int o, int c) {
        body.render(ps,b,l,o,c); head.render(ps,b,l,o,c);
        wingL.render(ps,b,l,o,c); wingR.render(ps,b,l,o,c);
        legL.render(ps,b,l,o,c); legR.render(ps,b,l,o,c); stinger.render(ps,b,l,o,c);
    }
}
