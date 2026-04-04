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
 * Moonlight Jellyfish model — geometry converted from Manus-generated
 * Blockbench JSON model. Bell dome on top, tentacles hanging below.
 *
 * UV layout matches the Manus 64x64 textures:
 *   Bell dome:   texOffs(0,0)  — 16x8x16 cube
 *   Inner bell:  texOffs(0,16) — 12x4x12 cube
 *   Tentacles:   texOffs(0/4/8/12, 28) — 2x16x1 strips
 *
 * Animation: bell pulses with squid-style tentacleAngle,
 * tentacles curl gently inward during propulsion.
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

        // Bell dome — 16x8x16, matching Manus JSON: from(-8,4,-8) to(8,12,8)
        // In entity model space, pivot at Y=12 (top of bell area)
        // Box extends 8 pixels in each horizontal direction, 8 tall
        partDef.addOrReplaceChild("bell",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        // Inner bell — 12x4x12, matching Manus JSON: from(-6,0,-6) to(6,4,6)
        // Sits inside the dome, pivot at Y=16 (bottom of bell)
        partDef.addOrReplaceChild("inner_bell",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-6.0F, -4.0F, -6.0F, 12.0F, 4.0F, 12.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        // 4 Tentacles — 2x16x1 strips hanging down from bell rim
        // Matching Manus JSON layout: each 2 wide, 16 tall, 1 deep
        // Pivots at Y=16 (bottom of bell), extend downward (+Y)
        partDef.addOrReplaceChild("tentacle1",
                CubeListBuilder.create()
                        .texOffs(0, 28)
                        .addBox(-1.0F, 0.0F, -0.5F, 2.0F, 16.0F, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 16.0F, -5.0F));

        partDef.addOrReplaceChild("tentacle2",
                CubeListBuilder.create()
                        .texOffs(4, 28)
                        .addBox(-1.0F, 0.0F, -0.5F, 2.0F, 16.0F, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 16.0F, -5.0F));

        partDef.addOrReplaceChild("tentacle3",
                CubeListBuilder.create()
                        .texOffs(8, 28)
                        .addBox(-1.0F, 0.0F, -0.5F, 2.0F, 16.0F, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 16.0F, 5.0F));

        partDef.addOrReplaceChild("tentacle4",
                CubeListBuilder.create()
                        .texOffs(12, 28)
                        .addBox(-1.0F, 0.0F, -0.5F, 2.0F, 16.0F, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 16.0F, 5.0F));

        return LayerDefinition.create(meshDef, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        // ageInTicks here is getBob() return = tentacleAngle
        float squeeze = ageInTicks;
        float time = entity.tickCount;

        // Bell pulse — contract during propulsion
        float bellPulse = squeeze * 0.15F;
        bell.xScale = 1.0F - bellPulse;
        bell.zScale = 1.0F - bellPulse;
        bell.yScale = 1.0F + bellPulse * 0.3F;

        innerBell.xScale = bell.xScale;
        innerBell.zScale = bell.zScale;
        innerBell.yScale = bell.yScale;

        // Tentacle animation: gentle inward curl during squeeze + idle sway
        float swaySpeed = 0.08F;
        float swayAmount = 0.08F;
        float p = squeeze * 0.35F;

        // Tentacle 1 at (-X, -Z): inward = +zRot (toward +X), +xRot (toward +Z... but tentacles
        // extend +Y so xRot positive tips toward +Z, zRot negative tips toward +X)
        tentacle1.xRot = p + Mth.sin(time * swaySpeed) * swayAmount;
        tentacle1.zRot = -p + Mth.cos(time * swaySpeed * 0.7F) * swayAmount;

        // Tentacle 2 at (+X, -Z): inward = -zRot (toward -X... wait, zRot positive tips -X)
        tentacle2.xRot = p + Mth.sin(time * swaySpeed + 1.5F) * swayAmount;
        tentacle2.zRot = p + Mth.cos(time * swaySpeed * 0.7F + 1.0F) * swayAmount;

        // Tentacle 3 at (-X, +Z): inward = toward +X (-zRot), toward -Z (-xRot)
        tentacle3.xRot = -p + Mth.sin(time * swaySpeed + 3.0F) * swayAmount;
        tentacle3.zRot = -p + Mth.cos(time * swaySpeed * 0.7F + 2.0F) * swayAmount;

        // Tentacle 4 at (+X, +Z): inward = toward -X (+zRot), toward -Z (-xRot)
        tentacle4.xRot = -p + Mth.sin(time * swaySpeed + 4.5F) * swayAmount;
        tentacle4.zRot = p + Mth.cos(time * swaySpeed * 0.7F + 3.0F) * swayAmount;
    }
}
