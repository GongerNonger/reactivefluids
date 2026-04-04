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
 * Moonlight Jellyfish model — bell dome on top, tentacles hanging below.
 *
 * Orientation: bell at the top, tentacles trail downward. When swimming,
 * tentacles squeeze inward (toward center) to simulate propulsion thrust,
 * then relax outward during the glide phase — matching real jellyfish movement.
 *
 * The tentacle squeeze is driven by the entity's tentacleAngle field
 * (synced from the squid-style AI pulse cycle).
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

        // Bell (dome) sits at the TOP of the model
        // Y=14 means the bottom of the bell is at pixel 14 from top of bounding box
        // Bell extends upward (-Y) by 6 pixels
        partDef.addOrReplaceChild("bell",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -6.0F, -5.0F, 10.0F, 6.0F, 10.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        // Inner bell — inside the dome, slightly smaller
        partDef.addOrReplaceChild("inner_bell",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-4.0F, -4.5F, -4.0F, 8.0F, 4.0F, 8.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        // Tentacles hang BELOW the bell (at Y=16, extending downward into +Y)
        // Pivot is at the bottom rim of the bell so they swing naturally
        float tentacleLen = 10.0F;
        partDef.addOrReplaceChild("tentacle1",
                CubeListBuilder.create()
                        .texOffs(0, 28)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, tentacleLen, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(-2.5F, 16.0F, -2.5F));

        partDef.addOrReplaceChild("tentacle2",
                CubeListBuilder.create()
                        .texOffs(4, 28)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, tentacleLen, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(2.5F, 16.0F, -2.5F));

        partDef.addOrReplaceChild("tentacle3",
                CubeListBuilder.create()
                        .texOffs(8, 28)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, tentacleLen, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(-2.5F, 16.0F, 2.5F));

        partDef.addOrReplaceChild("tentacle4",
                CubeListBuilder.create()
                        .texOffs(12, 28)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, tentacleLen, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(2.5F, 16.0F, 2.5F));

        return LayerDefinition.create(meshDef, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        // Use the entity's squid-style tentacle angle for propulsion animation
        // tentacleAngle goes from 0 (relaxed) to ~PI/4 (contracted)
        // Lerp between old and current for smooth animation
        float squeeze = Mth.lerp(ageInTicks % 1.0F, entity.oldTentacleAngle, entity.tentacleAngle);

        // Bell pulse — contracts when tentacles squeeze (propulsion phase)
        float bellPulse = squeeze * 0.3F;
        bell.xScale = 1.0F - bellPulse;
        bell.zScale = 1.0F - bellPulse;
        bell.yScale = 1.0F + bellPulse * 0.5F; // elongates slightly when contracting

        innerBell.xScale = bell.xScale;
        innerBell.zScale = bell.zScale;
        innerBell.yScale = bell.yScale;

        // Tentacle propulsion animation:
        // During squeeze (high tentacleAngle): tentacles swing INWARD toward center
        // During relax (low tentacleAngle): tentacles hang naturally with gentle sway
        //
        // xRot positive = tips swing toward +Z (backward if facing +Z)
        // zRot = lateral splay

        // Base idle sway (always present, subtle)
        float swaySpeed = 0.08F;
        float swayAmount = 0.1F;

        // Propulsion squeeze — tentacles curl inward
        // Each tentacle needs to rotate toward center based on its position
        float propulsion = squeeze * 1.2F; // amplify for visible effect

        // Tentacle 1: front-left — squeeze pulls it toward +X, +Z (inward)
        tentacle1.xRot = propulsion + Mth.sin(ageInTicks * swaySpeed) * swayAmount;
        tentacle1.zRot = propulsion * 0.5F + Mth.cos(ageInTicks * swaySpeed * 0.7F) * swayAmount * 0.3F;

        // Tentacle 2: front-right — squeeze pulls it toward -X, +Z
        tentacle2.xRot = propulsion + Mth.sin(ageInTicks * swaySpeed + 1.5F) * swayAmount;
        tentacle2.zRot = -propulsion * 0.5F + Mth.cos(ageInTicks * swaySpeed * 0.7F + 1.0F) * swayAmount * 0.3F;

        // Tentacle 3: back-left — squeeze pulls toward +X, -Z
        tentacle3.xRot = -propulsion + Mth.sin(ageInTicks * swaySpeed + 3.0F) * swayAmount;
        tentacle3.zRot = propulsion * 0.5F + Mth.cos(ageInTicks * swaySpeed * 0.7F + 2.0F) * swayAmount * 0.3F;

        // Tentacle 4: back-right — squeeze pulls toward -X, -Z
        tentacle4.xRot = -propulsion + Mth.sin(ageInTicks * swaySpeed + 4.5F) * swayAmount;
        tentacle4.zRot = -propulsion * 0.5F + Mth.cos(ageInTicks * swaySpeed * 0.7F + 3.0F) * swayAmount * 0.3F;
    }
}
