package com.reactivefluids.pinata;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.reactivefluids.ReactiveFluids;

/**
 * Fudgehog model — matches fudgehog.bbmodel.
 * Spiky hedgehog ball with hump (spines) as child of body, nose as child of head.
 * Idle animation: body breathing scale, head bob, leg shuffle.
 */
public class FudgehogModel extends EntityModel<BasePinataEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "fudgehog"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart legFrontLeft;
    private final ModelPart legFrontRight;
    private final ModelPart legBackLeft;
    private final ModelPart legBackRight;

    public FudgehogModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.legFrontLeft = root.getChild("leg_front_left");
        this.legFrontRight = root.getChild("leg_front_right");
        this.legBackLeft = root.getChild("leg_back_left");
        this.legBackRight = root.getChild("leg_back_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Body — round mass, pivot at (0, 3, 0) in bbmodel
        PartDefinition bodyPart = partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.5F, -3.0F, -4.0F, 7, 6, 8, CubeDeformation.NONE),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        // Hump/spines — child of body (moves with body during breathing)
        bodyPart.addOrReplaceChild("hump",
                CubeListBuilder.create()
                        .texOffs(0, 15)
                        .addBox(-4.0F, -7.0F, -3.5F, 8, 4, 7, CubeDeformation.NONE),
                PartPose.ZERO);

        // Head — pivot at (0, 3, -4) in bbmodel
        PartDefinition headPart = partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(22, 0)
                        .addBox(-1.5F, -2.0F, -3.0F, 3, 3, 3, CubeDeformation.NONE),
                PartPose.offset(0.0F, 21.0F, -4.0F));

        // Nose — child of head
        headPart.addOrReplaceChild("nose",
                CubeListBuilder.create()
                        .texOffs(30, 0)
                        .addBox(-0.5F, -1.0F, -6.0F, 1, 1, 1, CubeDeformation.NONE),
                PartPose.ZERO);

        // Four stubby legs
        partDefinition.addOrReplaceChild("leg_front_left",
                CubeListBuilder.create()
                        .texOffs(22, 7)
                        .addBox(-1.0F, 0.0F, -1.0F, 1, 3, 1, CubeDeformation.NONE),
                PartPose.offset(-2.0F, 21.0F, -2.5F));

        partDefinition.addOrReplaceChild("leg_front_right",
                CubeListBuilder.create()
                        .texOffs(22, 7)
                        .addBox(0.0F, 0.0F, -1.0F, 1, 3, 1, CubeDeformation.NONE),
                PartPose.offset(2.0F, 21.0F, -2.5F));

        partDefinition.addOrReplaceChild("leg_back_left",
                CubeListBuilder.create()
                        .texOffs(22, 7)
                        .addBox(-1.0F, 0.0F, 0.0F, 1, 3, 1, CubeDeformation.NONE),
                PartPose.offset(-2.0F, 21.0F, 2.0F));

        partDefinition.addOrReplaceChild("leg_back_right",
                CubeListBuilder.create()
                        .texOffs(22, 7)
                        .addBox(0.0F, 0.0F, 0.0F, 1, 3, 1, CubeDeformation.NONE),
                PartPose.offset(2.0F, 21.0F, 2.0F));

        return LayerDefinition.create(meshDefinition, 48, 32);
    }

    @Override
    public void setupAnim(BasePinataEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float headYaw, float headPitch) {
        // Head look + idle bob from bbmodel (-10 to +5 degrees, 2s period within 3s loop)
        float idleTime = ageInTicks * 0.033F; // ~3s loop at 20tps
        head.yRot = headYaw * ((float) Math.PI / 180F) * 0.6F;
        head.xRot = headPitch * ((float) Math.PI / 180F) * 0.3F
                + Mth.sin(idleTime * Mth.TWO_PI) * 0.17F; // ±10 degree bob

        // Body breathing scale from bbmodel (±2% scale, catmullrom)
        float breathe = Mth.sin(idleTime * Mth.TWO_PI) * 0.02F;
        body.xScale = 1.0F + breathe;
        body.yScale = 1.0F - breathe;
        body.zScale = 1.0F + breathe;

        // Leg shuffle — idle: ±5 degrees, walk: amplitude from limbSwingAmount
        float idleLeg = Mth.sin(idleTime * Mth.TWO_PI * 2.5F) * 0.087F; // ±5 degrees
        float walkAnim = Mth.sin(limbSwing * 0.8F) * limbSwingAmount * 0.5F;
        legFrontLeft.xRot = walkAnim + idleLeg;
        legFrontRight.xRot = -walkAnim - idleLeg;
        legBackLeft.xRot = -walkAnim + idleLeg;
        legBackRight.xRot = walkAnim - idleLeg;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, int color) {
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
        head.render(poseStack, buffer, packedLight, packedOverlay, color);
        legFrontLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        legFrontRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        legBackLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        legBackRight.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
