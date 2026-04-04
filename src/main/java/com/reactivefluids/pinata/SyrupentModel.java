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
 * Syrupent model — multi-segment snake with raised head.
 * VP reference: 5-6 connected body segments forming a serpentine S-curve,
 * raised triangular head with forked paper tongue.
 * Golden-amber/syrup coloring with darker diamond back pattern.
 */
public class SyrupentModel extends EntityModel<SyrupentEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "syrupent"), "main");

    private final ModelPart head;
    private final ModelPart neck;
    private final ModelPart body1;
    private final ModelPart body2;
    private final ModelPart body3;
    private final ModelPart tail;

    public SyrupentModel(ModelPart root) {
        this.head = root.getChild("head");
        this.neck = root.getChild("neck");
        this.body1 = root.getChild("body1");
        this.body2 = root.getChild("body2");
        this.body3 = root.getChild("body3");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        // Head — triangular, raised
        partDefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, -2.0F, -3.0F, 4, 3, 4, CubeDeformation.NONE)
                        // Tongue
                        .texOffs(24, 0)
                        .addBox(-0.5F, 0.0F, -5.0F, 1, 0, 2, CubeDeformation.NONE),
                PartPose.offset(0.0F, 18.0F, -6.0F));

        // Neck — raised section
        partDefinition.addOrReplaceChild("neck",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-1.5F, -1.5F, -2.0F, 3, 3, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 20.0F, -3.0F));

        // Body segments — get progressively lower
        partDefinition.addOrReplaceChild("body1",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2.0F, -2.0F, -2.0F, 4, 3, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 21.5F, 1.0F));

        partDefinition.addOrReplaceChild("body2",
                CubeListBuilder.create()
                        .texOffs(0, 24)
                        .addBox(-2.0F, -1.5F, -2.0F, 4, 3, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 22.0F, 5.0F));

        partDefinition.addOrReplaceChild("body3",
                CubeListBuilder.create()
                        .texOffs(17, 8)
                        .addBox(-1.5F, -1.5F, -2.0F, 3, 3, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 22.0F, 9.0F));

        // Tail — tapered
        partDefinition.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(17, 16)
                        .addBox(-1.0F, -1.0F, 0.0F, 2, 2, 4, CubeDeformation.NONE),
                PartPose.offset(0.0F, 22.5F, 12.0F));

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(SyrupentEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float headYaw, float headPitch) {
        head.yRot = headYaw * ((float) Math.PI / 180F);
        head.xRot = headPitch * ((float) Math.PI / 180F) * 0.3F - 0.2F; // Slightly raised

        // Serpentine slither — S-curve wave through body
        float wave = ageInTicks * 0.3F;
        float moveAmp = Math.min(limbSwingAmount * 0.5F, 0.3F);
        float idleAmp = 0.1F;
        float amp = idleAmp + moveAmp;

        neck.yRot = (float) Math.sin(wave) * amp * 0.5F;
        body1.yRot = (float) Math.sin(wave - 1.0F) * amp;
        body2.yRot = (float) Math.sin(wave - 2.0F) * amp;
        body3.yRot = (float) Math.sin(wave - 3.0F) * amp;
        tail.yRot = (float) Math.sin(wave - 4.0F) * amp * 1.2F;

        // Head bob — tongue flick
        if ((int)(ageInTicks) % 80 < 5) {
            head.xRot += (float) Math.sin(ageInTicks * 2.0F) * 0.1F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, int color) {
        head.render(poseStack, buffer, packedLight, packedOverlay, color);
        neck.render(poseStack, buffer, packedLight, packedOverlay, color);
        body1.render(poseStack, buffer, packedLight, packedOverlay, color);
        body2.render(poseStack, buffer, packedLight, packedOverlay, color);
        body3.render(poseStack, buffer, packedLight, packedOverlay, color);
        tail.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
