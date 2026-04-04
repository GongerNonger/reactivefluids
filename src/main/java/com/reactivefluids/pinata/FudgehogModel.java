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
 * Fudgehog model — spiky ball hedgehog with a small face peeking out.
 * VP reference: round body covered in paper spines on top/sides,
 * smooth belly, small snout with beady eyes. Short stubby legs.
 * Chocolate-brown base with caramel/fudge spines.
 */
public class FudgehogModel extends EntityModel<FudgehogEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "fudgehog"), "main");

    private final ModelPart body;
    private final ModelPart spines;
    private final ModelPart head;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;

    public FudgehogModel(ModelPart root) {
        this.body = root.getChild("body");
        this.spines = root.getChild("spines");
        this.head = root.getChild("head");
        this.legFrontLeft = root.getChild("leg_front_left");
        this.legFrontRight = root.getChild("leg_front_right");
        this.legBackLeft = root.getChild("leg_back_left");
        this.legBackRight = root.getChild("leg_back_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Body — round mass
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.5F, -3.0F, -4.0F, 7, 6, 8, CubeDeformation.NONE),
                PartPose.offset(0.0F, 19.0F, 0.0F));

        // Spines — slightly larger overlay on top/back (puffed up paper quills)
        partDefinition.addOrReplaceChild("spines",
                CubeListBuilder.create()
                        .texOffs(0, 15)
                        .addBox(-4.0F, -4.0F, -3.0F, 8, 4, 7, new CubeDeformation(0.3F)),
                PartPose.offset(0.0F, 19.0F, 0.0F));

        // Head — small snout poking out front
        partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(22, 0)
                        .addBox(-1.5F, -1.5F, -3.0F, 3, 3, 3, CubeDeformation.NONE)
                        // Nose
                        .texOffs(30, 0)
                        .addBox(-0.5F, 0.0F, -4.0F, 1, 1, 1, CubeDeformation.NONE),
                PartPose.offset(0.0F, 18.5F, -4.0F));

        // Four stubby legs
        partDefinition.addOrReplaceChild("leg_front_left",
                CubeListBuilder.create()
                        .texOffs(22, 7)
                        .addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1, CubeDeformation.NONE),
                PartPose.offset(2.5F, 22.0F, -2.5F));

        partDefinition.addOrReplaceChild("leg_front_right",
                CubeListBuilder.create()
                        .texOffs(22, 7)
                        .addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1, CubeDeformation.NONE),
                PartPose.offset(-2.5F, 22.0F, -2.5F));

        partDefinition.addOrReplaceChild("leg_back_left",
                CubeListBuilder.create()
                        .texOffs(22, 7)
                        .addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1, CubeDeformation.NONE),
                PartPose.offset(2.5F, 22.0F, 2.5F));

        partDefinition.addOrReplaceChild("leg_back_right",
                CubeListBuilder.create()
                        .texOffs(22, 7)
                        .addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1, CubeDeformation.NONE),
                PartPose.offset(-2.5F, 22.0F, 2.5F));

        return LayerDefinition.create(meshDefinition, 48, 32);
    }

    @Override
    public void setupAnim(FudgehogEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float headYaw, float headPitch) {
        head.yRot = headYaw * ((float) Math.PI / 180F) * 0.6F;
        head.xRot = headPitch * ((float) Math.PI / 180F) * 0.3F;

        // Waddle walk — legs alternate
        float walkAnim = (float) Math.sin(limbSwing * 0.8F) * limbSwingAmount * 0.5F;
        legFrontLeft.xRot = walkAnim;
        legFrontRight.xRot = -walkAnim;
        legBackLeft.xRot = -walkAnim;
        legBackRight.xRot = walkAnim;

        // Spines bristle when entity takes damage (puff up)
        float bristle = (float) Math.sin(ageInTicks * 0.05F) * 0.02F;
        spines.y = 19.0F + bristle;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, int color) {
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
        spines.render(poseStack, buffer, packedLight, packedOverlay, color);
        head.render(poseStack, buffer, packedLight, packedOverlay, color);
        legFrontLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        legFrontRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        legBackLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        legBackRight.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
