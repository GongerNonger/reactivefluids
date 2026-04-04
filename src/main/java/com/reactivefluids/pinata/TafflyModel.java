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
 * Taffly model — small round fly with big eyes and translucent wings.
 * VP reference: spherical body, two huge compound eyes (taking up most of head),
 * two thin translucent paper wings on top, six tiny legs underneath.
 * Toffee-brown/amber coloring with darker stripes.
 */
public class TafflyModel extends EntityModel<TafflyEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "taffly"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart legLeft;
    private final ModelPart legRight;

    public TafflyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
        this.legLeft = root.getChild("leg_left");
        this.legRight = root.getChild("leg_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Body — small round
        partDefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, -2.0F, -1.5F, 4, 4, 3, CubeDeformation.NONE),
                PartPose.offset(0.0F, 21.0F, 0.0F));

        // Head — with giant compound eyes
        partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-1.5F, -1.5F, -2.0F, 3, 3, 2, CubeDeformation.NONE)
                        // Left eye (big!)
                        .texOffs(14, 0)
                        .addBox(0.5F, -1.5F, -3.0F, 2, 2, 1, CubeDeformation.NONE)
                        // Right eye
                        .texOffs(14, 0)
                        .addBox(-2.5F, -1.5F, -3.0F, 2, 2, 1, CubeDeformation.NONE),
                PartPose.offset(0.0F, 20.0F, -1.5F));

        // Wings — thin flat panels (translucent paper)
        partDefinition.addOrReplaceChild("wing_left",
                CubeListBuilder.create()
                        .texOffs(0, 14)
                        .addBox(0.0F, 0.0F, -1.0F, 4, 0, 3, CubeDeformation.NONE),
                PartPose.offset(1.5F, 19.0F, 0.0F));

        partDefinition.addOrReplaceChild("wing_right",
                CubeListBuilder.create()
                        .texOffs(0, 14)
                        .addBox(-4.0F, 0.0F, -1.0F, 4, 0, 3, CubeDeformation.NONE),
                PartPose.offset(-1.5F, 19.0F, 0.0F));

        // Tiny dangling legs
        partDefinition.addOrReplaceChild("leg_left",
                CubeListBuilder.create()
                        .texOffs(14, 4)
                        .addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1, CubeDeformation.NONE),
                PartPose.offset(1.0F, 23.0F, 0.0F));

        partDefinition.addOrReplaceChild("leg_right",
                CubeListBuilder.create()
                        .texOffs(14, 4)
                        .addBox(-0.5F, 0.0F, -0.5F, 1, 2, 1, CubeDeformation.NONE),
                PartPose.offset(-1.0F, 23.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 32, 16);
    }

    @Override
    public void setupAnim(TafflyEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float headYaw, float headPitch) {
        head.yRot = headYaw * ((float) Math.PI / 180F);

        // Rapid wing buzz — very fast flapping
        float buzz = (float) Math.sin(ageInTicks * 3.0F) * 0.8F;
        wingLeft.zRot = -buzz - 0.3F;
        wingRight.zRot = buzz + 0.3F;

        // Slight body hover bob
        float bob = (float) Math.sin(ageInTicks * 0.6F) * 0.5F;
        body.y = 21.0F + bob;
        head.y = 20.0F + bob;
        wingLeft.y = 19.0F + bob;
        wingRight.y = 19.0F + bob;
        legLeft.y = 23.0F + bob;
        legRight.y = 23.0F + bob;

        // Dangling legs sway
        legLeft.xRot = (float) Math.sin(ageInTicks * 0.4F) * 0.2F + 0.1F;
        legRight.xRot = (float) Math.sin(ageInTicks * 0.4F + 1.0F) * 0.2F + 0.1F;
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
    }
}
