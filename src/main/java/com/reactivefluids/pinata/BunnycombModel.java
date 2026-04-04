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

/**
 * Bunnycomb — round fluffy rabbit body, long upright ears, cotton tail.
 * VP ref: honeycomb-pattern paper, warm yellow/orange, big feet.
 */
public class BunnycombModel extends EntityModel<BasePinataEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "bunnycomb"), "main");

    private final ModelPart body, head, earLeft, earRight, legLeft, legRight, tail;

    public BunnycombModel(ModelPart root) {
        body = root.getChild("body"); head = root.getChild("head");
        earLeft = root.getChild("ear_left"); earRight = root.getChild("ear_right");
        legLeft = root.getChild("leg_left"); legRight = root.getChild("leg_right");
        tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -3.0F, -3.5F, 6, 6, 7), PartPose.offset(0, 18, 0));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 14)
                .addBox(-2.0F, -2.0F, -3.0F, 4, 4, 3)
                .texOffs(26, 0).addBox(-0.5F, 0.5F, -3.5F, 1, 1, 1), // nose
                PartPose.offset(0, 16.5F, -3.5F));
        root.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(14, 14)
                .addBox(-1.0F, -5.0F, 0.0F, 2, 5, 1), PartPose.offset(1.5F, 14.5F, -2.5F));
        root.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(14, 14)
                .addBox(-1.0F, -5.0F, 0.0F, 2, 5, 1), PartPose.offset(-1.5F, 14.5F, -2.5F));
        root.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(20, 14)
                .addBox(-1.0F, 0.0F, -1.5F, 2, 3, 3), PartPose.offset(2.5F, 21, 1));
        root.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(20, 14)
                .addBox(-1.0F, 0.0F, -1.5F, 2, 3, 3), PartPose.offset(-2.5F, 21, 1));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(26, 4)
                .addBox(-1.0F, -1.0F, 0.0F, 2, 2, 2), PartPose.offset(0, 17, 3.5F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(BasePinataEntity e, float limbSwing, float limbSwingAmount,
                          float age, float headYaw, float headPitch) {
        head.yRot = headYaw * 0.017F; head.xRot = headPitch * 0.017F * 0.5F;
        float hop = (float) Math.sin(limbSwing * 1.2F) * limbSwingAmount;
        legLeft.xRot = hop * 0.6F; legRight.xRot = -hop * 0.6F;
        earLeft.zRot = (float) Math.sin(age * 0.3F) * 0.1F;
        earRight.zRot = -(float) Math.sin(age * 0.3F) * 0.1F;
        tail.yRot = (float) Math.sin(age * 0.4F) * 0.2F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer buf, int light, int overlay, int color) {
        body.render(ps, buf, light, overlay, color); head.render(ps, buf, light, overlay, color);
        earLeft.render(ps, buf, light, overlay, color); earRight.render(ps, buf, light, overlay, color);
        legLeft.render(ps, buf, light, overlay, color); legRight.render(ps, buf, light, overlay, color);
        tail.render(ps, buf, light, overlay, color);
    }
}
