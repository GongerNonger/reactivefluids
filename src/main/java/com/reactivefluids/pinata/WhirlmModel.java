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
 * Whirlm model — a segmented worm shape, like a piñata paper-wrapped caterpillar.
 * Three body segments + head, with a slight curve animation during movement.
 */
public class WhirlmModel extends EntityModel<BasePinataEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "whirlm"), "main");

    private final ModelPart head;
    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart tail;

    public WhirlmModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body1 = root.getChild("body1");
        this.body2 = root.getChild("body2");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Head — slightly larger front segment with eyes
        partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5F, -2.5F, -3.0F, 5, 5, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 21.5F, -4.0F));

        // Body segment 1 — middle front
        partDefinition.addOrReplaceChild("body1",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-2.0F, -2.0F, -2.0F, 4, 4, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 22.0F, 0.0F));

        // Body segment 2 — middle back
        partDefinition.addOrReplaceChild("body2",
                CubeListBuilder.create()
                        .texOffs(0, 19)
                        .addBox(-2.0F, -2.0F, -2.0F, 4, 4, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 22.0F, 4.0F));

        // Tail — tapered end
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(18, 0)
                        .addBox(-1.5F, -1.5F, -1.5F, 3, 3, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 22.5F, 8.0F));

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(BasePinataEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
        // Worm-like undulation — sinusoidal wave through body segments
        float wave = (float) Math.sin(ageInTicks * 0.3) * 0.15F;
        float moveWave = (float) Math.sin(limbSwing * 0.8) * limbSwingAmount * 0.3F;

        head.yRot = headYaw * ((float) Math.PI / 180F);
        head.xRot = headPitch * ((float) Math.PI / 180F) * 0.5F;

        // Undulation through segments when moving
        body1.yRot = wave + moveWave * 0.5F;
        body2.yRot = -wave + moveWave;
        tail.yRot = wave * 1.5F - moveWave * 0.5F;

        // Slight vertical bob
        float bob = (float) Math.sin(ageInTicks * 0.15) * 0.02F;
        head.y = 21.5F + bob;
        body1.y = 22.0F - bob;
        body2.y = 22.0F + bob;
        tail.y = 22.5F - bob;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, int color) {
        head.render(poseStack, buffer, packedLight, packedOverlay, color);
        body1.render(poseStack, buffer, packedLight, packedOverlay, color);
        body2.render(poseStack, buffer, packedLight, packedOverlay, color);
        tail.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
