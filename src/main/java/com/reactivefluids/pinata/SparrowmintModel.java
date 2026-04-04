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
 * Sparrowmint model — round bird body with stubby wings and legs.
 * VP reference: ball-shaped body, small pointed beak, two paper-fold wings,
 * short legs, fan-shaped tail feathers. Mint-green coloring.
 */
public class SparrowmintModel extends EntityModel<BasePinataEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "sparrowmint"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart legLeft;
    private final ModelPart legRight;
    private final ModelPart tail;

    public SparrowmintModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
        this.legLeft = root.getChild("leg_left");
        this.legRight = root.getChild("leg_right");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Body — round/oval main mass
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, -3.0F, -3.0F, 6, 6, 7, CubeDeformation.NONE),
                PartPose.offset(0.0F, 19.0F, 0.0F));

        // Head — slightly smaller sphere on front
        partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 14)
                        .addBox(-2.0F, -2.0F, -3.0F, 4, 4, 3, CubeDeformation.NONE)
                        // Beak
                        .texOffs(26, 0)
                        .addBox(-0.5F, -0.5F, -5.0F, 1, 1, 2, CubeDeformation.NONE),
                PartPose.offset(0.0F, 17.5F, -3.0F));

        // Wings — flat panels on sides
        partDefinition.addOrReplaceChild("wing_left",
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(0.0F, -1.0F, -1.0F, 1, 4, 5, CubeDeformation.NONE),
                PartPose.offset(3.0F, 17.5F, -1.0F));

        partDefinition.addOrReplaceChild("wing_right",
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(-1.0F, -1.0F, -1.0F, 1, 4, 5, CubeDeformation.NONE),
                PartPose.offset(-3.0F, 17.5F, -1.0F));

        // Legs — stubby sticks
        partDefinition.addOrReplaceChild("leg_left",
                CubeListBuilder.create()
                        .texOffs(26, 4)
                        .addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1, CubeDeformation.NONE),
                PartPose.offset(1.5F, 22.0F, 0.0F));

        partDefinition.addOrReplaceChild("leg_right",
                CubeListBuilder.create()
                        .texOffs(26, 4)
                        .addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1, CubeDeformation.NONE),
                PartPose.offset(-1.5F, 22.0F, 0.0F));

        // Tail feathers — flat fan
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(13, 14)
                        .addBox(-2.0F, -1.0F, 0.0F, 4, 2, 3, CubeDeformation.NONE),
                PartPose.offset(0.0F, 18.0F, 4.0F));

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(BasePinataEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float headYaw, float headPitch) {
        head.yRot = headYaw * ((float) Math.PI / 180F);
        head.xRot = headPitch * ((float) Math.PI / 180F) * 0.5F;

        // Wing flap
        float flapSpeed = entity.getDeltaMovement().horizontalDistanceSqr() > 0.01 ? 1.5F : 0.3F;
        float flap = (float) Math.sin(ageInTicks * flapSpeed) * 0.4F;
        wingLeft.zRot = -flap - 0.2F;
        wingRight.zRot = flap + 0.2F;

        // Leg walk
        legLeft.xRot = (float) Math.sin(limbSwing * 0.6F) * limbSwingAmount * 0.6F;
        legRight.xRot = -(float) Math.sin(limbSwing * 0.6F) * limbSwingAmount * 0.6F;

        // Tail bob
        tail.xRot = (float) Math.sin(ageInTicks * 0.1F) * 0.1F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, int color) {
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
        head.render(poseStack, buffer, packedLight, packedOverlay, color);
        wingLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        wingRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        legLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        legRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        tail.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
