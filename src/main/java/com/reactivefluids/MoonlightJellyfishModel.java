package com.reactivefluids;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Moonlight Jellyfish model — dome-shaped bell with 4 trailing tentacles.
 * 64x64 texture. Bell pulses open/closed with swim animation.
 * Tentacles trail behind and sway gently.
 */
@OnlyIn(Dist.CLIENT)
public class MoonlightJellyfishModel<T extends MoonlightJellyfishEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "moonlight_jellyfish"),
                    "main");

    private final ModelPart root;
    private final ModelPart bell;
    private final ModelPart innerBell;
    private final ModelPart tentacle1;
    private final ModelPart tentacle2;
    private final ModelPart tentacle3;
    private final ModelPart tentacle4;

    public MoonlightJellyfishModel(ModelPart root) {
        this.root = root;
        this.bell = root.getChild("bell");
        this.innerBell = root.getChild("inner_bell");
        this.tentacle1 = root.getChild("tentacle1");
        this.tentacle2 = root.getChild("tentacle2");
        this.tentacle3 = root.getChild("tentacle3");
        this.tentacle4 = root.getChild("tentacle4");
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDef = new MeshDefinition();
        PartDefinition partDef = meshDef.getRoot();

        // Bell (dome) — flattened sphere, 10x6x10
        partDef.addOrReplaceChild("bell",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -6.0F, -5.0F, 10.0F, 6.0F, 10.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        // Inner bell — slightly smaller, translucent membrane
        partDef.addOrReplaceChild("inner_bell",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-4.0F, -4.5F, -4.0F, 8.0F, 4.0F, 8.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        // 4 Tentacles — thin trailing strands
        float tentacleLen = 8.0F;
        partDef.addOrReplaceChild("tentacle1",
                CubeListBuilder.create()
                        .texOffs(0, 28)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, tentacleLen, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(-2.5F, 18.0F, -2.5F));

        partDef.addOrReplaceChild("tentacle2",
                CubeListBuilder.create()
                        .texOffs(4, 28)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, tentacleLen, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(2.5F, 18.0F, -2.5F));

        partDef.addOrReplaceChild("tentacle3",
                CubeListBuilder.create()
                        .texOffs(8, 28)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, tentacleLen, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(-2.5F, 18.0F, 2.5F));

        partDef.addOrReplaceChild("tentacle4",
                CubeListBuilder.create()
                        .texOffs(12, 28)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, tentacleLen, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(2.5F, 18.0F, 2.5F));

        return LayerDefinition.create(meshDef, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Bell pulse — gentle squeeze animation
        float pulse = Mth.sin(ageInTicks * 0.15F) * 0.08F;
        bell.xScale = 1.0F + pulse;
        bell.zScale = 1.0F + pulse;
        bell.yScale = 1.0F - pulse * 0.5F;

        innerBell.xScale = bell.xScale;
        innerBell.zScale = bell.zScale;
        innerBell.yScale = bell.yScale;

        // Tentacles sway — each offset slightly in phase
        float swaySpeed = 0.12F;
        float swayAmount = 0.15F;
        tentacle1.xRot = Mth.sin(ageInTicks * swaySpeed) * swayAmount;
        tentacle1.zRot = Mth.cos(ageInTicks * swaySpeed * 0.7F) * swayAmount * 0.5F;

        tentacle2.xRot = Mth.sin(ageInTicks * swaySpeed + 1.5F) * swayAmount;
        tentacle2.zRot = Mth.cos(ageInTicks * swaySpeed * 0.7F + 1.0F) * swayAmount * 0.5F;

        tentacle3.xRot = Mth.sin(ageInTicks * swaySpeed + 3.0F) * swayAmount;
        tentacle3.zRot = Mth.cos(ageInTicks * swaySpeed * 0.7F + 2.0F) * swayAmount * 0.5F;

        tentacle4.xRot = Mth.sin(ageInTicks * swaySpeed + 4.5F) * swayAmount;
        tentacle4.zRot = Mth.cos(ageInTicks * swaySpeed * 0.7F + 3.0F) * swayAmount * 0.5F;
    }
}
