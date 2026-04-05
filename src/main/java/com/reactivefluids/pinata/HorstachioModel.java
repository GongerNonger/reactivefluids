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
 * Horstachio model — matches horstachio.bbmodel.
 * Large horse with neck/head hierarchy, hoof feathers as children of legs.
 * Idle animation: head look, tail swish, leg shift.
 * Walk animation: gallop with body bob.
 */
public class HorstachioModel extends EntityModel<BasePinataEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "horstachio"), "main");

    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart legFL, legFR, legBL, legBR;

    public HorstachioModel(ModelPart root) {
        body = root.getChild("body");
        neck = root.getChild("neck");
        head = root.getChild("head");
        tail = root.getChild("tail");
        legFL = root.getChild("leg_front_left");
        legFR = root.getChild("leg_front_right");
        legBL = root.getChild("leg_back_left");
        legBR = root.getChild("leg_back_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition m = new MeshDefinition();
        PartDefinition r = m.getRoot();

        // Body — pivot at (0, 4, 0) in bbmodel
        r.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0F, -4.0F, -6.0F, 8, 8, 12),
                PartPose.offset(0, 12, 0));

        // Neck — pivot at (0, 8, -3), contains mane as child
        PartDefinition neckPart = r.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(0, 21)
                .addBox(-2.0F, -7.0F, -2.0F, 4, 7, 4),
                PartPose.offset(0, 8, -3));

        // Mane — child of neck
        neckPart.addOrReplaceChild("mane", CubeListBuilder.create()
                .texOffs(16, 21)
                .addBox(-0.5F, -6.0F, -1.0F, 1, 6, 3),
                PartPose.ZERO);

        // Head — pivot at (0, 15, -3), contains muzzle as child
        PartDefinition headPart = r.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(28, 0)
                .addBox(-2.0F, -4.0F, -3.0F, 4, 4, 5),
                PartPose.offset(0, 1, -3));

        // Muzzle — child of head
        headPart.addOrReplaceChild("muzzle", CubeListBuilder.create()
                .texOffs(40, 0)
                .addBox(-1.5F, -2.0F, -5.0F, 3, 2, 2),
                PartPose.ZERO);

        // Tail — pivot at (0, 6, 6)
        r.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(40, 10)
                .addBox(-1.0F, -2.0F, 0.0F, 2, 2, 8),
                PartPose.offset(0, 10, 6));

        // Legs with feather children — front left, pivot at (-3, 0, -4)
        PartDefinition lfl = r.addOrReplaceChild("leg_front_left", CubeListBuilder.create()
                .texOffs(28, 10)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2),
                PartPose.offset(-3, 16, -4));
        lfl.addOrReplaceChild("feather_front_left", CubeListBuilder.create()
                .texOffs(44, 10)
                .addBox(-1.5F, 6.0F, -1.5F, 3, 2, 3, new CubeDeformation(0.5F)),
                PartPose.ZERO);

        PartDefinition lfr = r.addOrReplaceChild("leg_front_right", CubeListBuilder.create()
                .texOffs(28, 10)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2),
                PartPose.offset(3, 16, -4));
        lfr.addOrReplaceChild("feather_front_right", CubeListBuilder.create()
                .texOffs(44, 10)
                .addBox(-1.5F, 6.0F, -1.5F, 3, 2, 3, new CubeDeformation(0.5F)),
                PartPose.ZERO);

        PartDefinition lbl = r.addOrReplaceChild("leg_back_left", CubeListBuilder.create()
                .texOffs(36, 10)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2),
                PartPose.offset(-3, 16, 4));
        lbl.addOrReplaceChild("feather_back_left", CubeListBuilder.create()
                .texOffs(44, 10)
                .addBox(-1.5F, 6.0F, -1.5F, 3, 2, 3, new CubeDeformation(0.5F)),
                PartPose.ZERO);

        PartDefinition lbr = r.addOrReplaceChild("leg_back_right", CubeListBuilder.create()
                .texOffs(36, 10)
                .addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2),
                PartPose.offset(3, 16, 4));
        lbr.addOrReplaceChild("feather_back_right", CubeListBuilder.create()
                .texOffs(44, 10)
                .addBox(-1.5F, 6.0F, -1.5F, 3, 2, 3, new CubeDeformation(0.5F)),
                PartPose.ZERO);

        return LayerDefinition.create(m, 64, 32);
    }

    @Override
    public void setupAnim(BasePinataEntity e, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float headYaw, float headPitch) {
        float idleTime = ageInTicks * 0.033F; // ~3s loop
        float isMoving = limbSwingAmount > 0.01F ? 1.0F : 0.0F;

        // --- Idle animation ---
        // Head: yaw look ±10 degrees
        head.yRot = headYaw * 0.017F * 0.5F
                + Mth.sin(idleTime * Mth.TWO_PI) * 0.17F * (1.0F - isMoving);
        head.xRot = headPitch * 0.017F * 0.3F;

        // Neck: gentle yaw ±5 degrees
        neck.yRot = Mth.sin(idleTime * Mth.TWO_PI * 0.67F) * 0.087F * (1.0F - isMoving);

        // Tail: swish yaw ±20 degrees with ±5 degree roll
        float tailPhase = idleTime * Mth.TWO_PI * 1.67F;
        tail.yRot = Mth.sin(tailPhase) * 0.35F;
        tail.zRot = Mth.sin(tailPhase) * 0.087F;

        // Idle leg shifts: ±5 degrees front, ±3 degrees back (alternating)
        float idleLeg = Mth.sin(idleTime * Mth.TWO_PI) * 0.087F * (1.0F - isMoving);

        // --- Walk animation ---
        // Body bob: +0.5 units at stride peaks
        float walkPhase = limbSwing * 0.6F;
        body.y = 12.0F - Mth.abs(Mth.sin(walkPhase)) * limbSwingAmount * 0.5F;

        // Head bob during walk: -5 degrees at stride peaks
        head.xRot += -Mth.abs(Mth.sin(walkPhase)) * limbSwingAmount * 0.087F;

        // Legs: gallop ±25/-15 degrees, diagonal pairs
        float gallop = Mth.sin(walkPhase) * limbSwingAmount;
        legFL.xRot = gallop * 0.44F + idleLeg;        // ±25 degrees
        legFR.xRot = -gallop * 0.44F - idleLeg;
        legBL.xRot = -gallop * 0.44F + idleLeg;
        legBR.xRot = gallop * 0.44F - idleLeg;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer b, int l, int o, int c) {
        body.render(ps, b, l, o, c);
        neck.render(ps, b, l, o, c);
        head.render(ps, b, l, o, c);
        tail.render(ps, b, l, o, c);
        legFL.render(ps, b, l, o, c);
        legFR.render(ps, b, l, o, c);
        legBL.render(ps, b, l, o, c);
        legBR.render(ps, b, l, o, c);
    }
}
