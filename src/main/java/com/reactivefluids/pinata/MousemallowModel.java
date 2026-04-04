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
 * Mousemallow model — tiny round mouse with big ears and long tail.
 * VP reference: oval body, two large circular ears, small round snout,
 * whiskers, four tiny feet, long curving tail.
 * Pink/white marshmallow coloring.
 */
public class MousemallowModel extends EntityModel<MousemallowEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "mousemallow"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart earLeft;
    private final ModelPart earRight;
    private final ModelPart tail;
    private final ModelPart legLeft;
    private final ModelPart legRight;

    public MousemallowModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.earLeft = root.getChild("ear_left");
        this.earRight = root.getChild("ear_right");
        this.tail = root.getChild("tail");
        this.legLeft = root.getChild("leg_left");
        this.legRight = root.getChild("leg_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Body — small oval
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, -2.0F, -2.5F, 4, 4, 5, CubeDeformation.NONE),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        // Head — round with snout
        partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-1.5F, -1.5F, -2.5F, 3, 3, 3, CubeDeformation.NONE)
                        // Snout
                        .texOffs(18, 0)
                        .addBox(-0.5F, 0.0F, -3.5F, 1, 1, 1, CubeDeformation.NONE),
                PartPose.offset(0.0F, 20.0F, -2.5F));

        // Big round ears (VP signature)
        partDefinition.addOrReplaceChild("ear_left",
                CubeListBuilder.create()
                        .texOffs(12, 10)
                        .addBox(-1.0F, -2.0F, 0.0F, 2, 2, 1, CubeDeformation.NONE),
                PartPose.offset(1.5F, 18.5F, -2.0F));

        partDefinition.addOrReplaceChild("ear_right",
                CubeListBuilder.create()
                        .texOffs(12, 10)
                        .addBox(-1.0F, -2.0F, 0.0F, 2, 2, 1, CubeDeformation.NONE),
                PartPose.offset(-1.5F, 18.5F, -2.0F));

        // Long thin tail
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(18, 3)
                        .addBox(-0.5F, -0.5F, 0.0F, 1, 1, 5, CubeDeformation.NONE),
                PartPose.offset(0.0F, 21.0F, 2.5F));

        // Tiny legs (left pair + right pair)
        partDefinition.addOrReplaceChild("leg_left",
                CubeListBuilder.create()
                        .texOffs(18, 10)
                        .addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1, CubeDeformation.NONE),
                PartPose.offset(1.5F, 23.0F, 0.0F));

        partDefinition.addOrReplaceChild("leg_right",
                CubeListBuilder.create()
                        .texOffs(18, 10)
                        .addBox(-0.5F, 0.0F, -0.5F, 1, 1, 1, CubeDeformation.NONE),
                PartPose.offset(-1.5F, 23.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 32, 16);
    }

    @Override
    public void setupAnim(MousemallowEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float headYaw, float headPitch) {
        head.yRot = headYaw * ((float) Math.PI / 180F);
        head.xRot = headPitch * ((float) Math.PI / 180F) * 0.5F;

        // Twitchy ear movement
        float twitch = (float) Math.sin(ageInTicks * 0.5F) * 0.15F;
        earLeft.zRot = -twitch;
        earRight.zRot = twitch;

        // Quick scurrying legs
        float scurry = (float) Math.sin(limbSwing * 1.5F) * limbSwingAmount * 0.8F;
        legLeft.xRot = scurry;
        legRight.xRot = -scurry;

        // Tail sway
        tail.yRot = (float) Math.sin(ageInTicks * 0.3F) * 0.3F;
        tail.xRot = 0.3F; // Slightly upward curve
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, int color) {
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
        head.render(poseStack, buffer, packedLight, packedOverlay, color);
        earLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        earRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        tail.render(poseStack, buffer, packedLight, packedOverlay, color);
        legLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        legRight.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
