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

        // ageInTicks here is actually getBob() return — the tentacle angle
        // (same pattern as vanilla squid).
        float squeeze = ageInTicks;
        float time = entity.tickCount;

        // Bell pulse — gentle contract during propulsion
        float bellPulse = squeeze * 0.15F; // subtle
        bell.xScale = 1.0F - bellPulse;
        bell.zScale = 1.0F - bellPulse;
        bell.yScale = 1.0F + bellPulse * 0.3F;

        innerBell.xScale = bell.xScale;
        innerBell.zScale = bell.zScale;
        innerBell.yScale = bell.yScale;

        // Tentacle propulsion: curl inward toward center during squeeze.
        //
        // Coordinate refresher (model space, no renderer flip):
        //   +Y = down, tentacles extend in +Y from pivot at Y=16
        //   xRot positive = tip rotates toward +Z
        //   zRot positive = tip rotates toward -X (for +Y-extending parts)
        //
        // "Inward" means each tentacle tip curls toward the model's center
        // (the Y axis at X=0, Z=0).
        float swaySpeed = 0.08F;
        float swayAmount = 0.08F;
        float p = squeeze * 0.35F; // gentle inward curl

        // Tentacle 1 at (-X, -Z): inward = toward +X and +Z
        //   +X: zRot negative (tip toward +X for a -X tentacle)
        //   +Z: xRot positive (tip toward +Z)
        tentacle1.xRot = p + Mth.sin(time * swaySpeed) * swayAmount;
        tentacle1.zRot = -p + Mth.cos(time * swaySpeed * 0.7F) * swayAmount;

        // Tentacle 2 at (+X, -Z): inward = toward -X and +Z
        //   -X: zRot positive
        //   +Z: xRot positive
        tentacle2.xRot = p + Mth.sin(time * swaySpeed + 1.5F) * swayAmount;
        tentacle2.zRot = p + Mth.cos(time * swaySpeed * 0.7F + 1.0F) * swayAmount;

        // Tentacle 3 at (-X, +Z): inward = toward +X and -Z
        //   +X: zRot negative
        //   -Z: xRot negative
        tentacle3.xRot = -p + Mth.sin(time * swaySpeed + 3.0F) * swayAmount;
        tentacle3.zRot = -p + Mth.cos(time * swaySpeed * 0.7F + 2.0F) * swayAmount;

        // Tentacle 4 at (+X, +Z): inward = toward -X and -Z
        //   -X: zRot positive
        //   -Z: xRot negative
        tentacle4.xRot = -p + Mth.sin(time * swaySpeed + 4.5F) * swayAmount;
        tentacle4.zRot = p + Mth.cos(time * swaySpeed * 0.7F + 3.0F) * swayAmount;
    }
}
