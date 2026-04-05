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
 * Whirlm model — segmented worm with tapering body segments.
 * Matches whirlm.bbmodel: head, body1, body2 (tapered), body3 (smaller), tail.
 */
public class WhirlmModel extends EntityModel<BasePinataEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "whirlm"), "main");

    private final ModelPart head;
    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart body3;
    private final ModelPart tail;

    public WhirlmModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body1 = root.getChild("body1");
        this.body2 = root.getChild("body2");
        this.body3 = root.getChild("body3");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Head — front segment, pivot at (0, 5, 0) in bbmodel = (0, 19, -4) in MC coords
        partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.5F, -5.0F, -2.0F, 5, 5, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 19.0F, -4.0F));

        // Body segment 1 — pivot at (0, 4, 0)
        partDefinition.addOrReplaceChild("body1",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-2.0F, -4.0F, -2.0F, 4, 4, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 20.0F, 0.0F));

        // Body segment 2 — tapered (3.6 wide), pivot at (0, -4, 0) -> y=28 in MC
        partDefinition.addOrReplaceChild("body2",
                CubeListBuilder.create()
                        .texOffs(0, 19)
                        .addBox(-1.8F, -3.6F, -1.8F, 4, 4, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 21.6F, 4.0F));

        // Body segment 3 — even smaller (3 wide), new element
        partDefinition.addOrReplaceChild("body3",
                CubeListBuilder.create()
                        .texOffs(18, 10)
                        .addBox(-1.5F, -3.0F, -1.5F, 3, 3, 3, CubeDeformation.NONE),
                PartPose.offset(0.0F, 22.0F, 7.0F));

        // Tail — tapered end
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(18, 0)
                        .addBox(-1.5F, -3.0F, -2.0F, 3, 3, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 21.0F, 10.0F));

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(BasePinataEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
        // Idle animation from bbmodel — catmullrom-style wave through segments
        float idleTime = ageInTicks * 0.05F; // Scale to match 2s loop at 20 tps
        float moveWave = Mth.sin(limbSwing * 0.8F) * limbSwingAmount * 0.3F;

        // Head: bobs up/down ±10 degrees over 2s
        head.yRot = headYaw * ((float) Math.PI / 180F);
        head.xRot = headPitch * ((float) Math.PI / 180F) * 0.5F
                + Mth.sin(idleTime * Mth.TWO_PI) * 0.17F; // ~10 degrees

        // Body1: delayed wave, ±8 degrees
        body1.xRot = Mth.sin((idleTime - 0.1F) * Mth.TWO_PI) * 0.14F + moveWave * 0.5F;

        // Body2: further delayed, ±6 degrees
        body2.xRot = Mth.sin((idleTime - 0.2F) * Mth.TWO_PI) * 0.10F + moveWave * 0.7F;

        // Body3: further delayed
        body3.xRot = Mth.sin((idleTime - 0.3F) * Mth.TWO_PI) * 0.08F + moveWave * 0.8F;

        // Tail: yaw wag ±15 degrees
        tail.yRot = Mth.sin(idleTime * Mth.TWO_PI) * 0.26F - moveWave * 0.5F;

        // Movement undulation
        body1.yRot = moveWave * 0.3F;
        body2.yRot = -moveWave * 0.5F;
        body3.yRot = moveWave * 0.4F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, int color) {
        head.render(poseStack, buffer, packedLight, packedOverlay, color);
        body1.render(poseStack, buffer, packedLight, packedOverlay, color);
        body2.render(poseStack, buffer, packedLight, packedOverlay, color);
        body3.render(poseStack, buffer, packedLight, packedOverlay, color);
        tail.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
