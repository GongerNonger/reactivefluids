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

/** Shellybean — snail with spiral shell, eye stalks, soft body. Pastel jelly-bean colors. */
public class ShellybeanModel extends EntityModel<BasePinataEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "shellybean"), "main");
    private final ModelPart body, shell, eyeL, eyeR;

    public ShellybeanModel(ModelPart root) {
        body = root.getChild("body"); shell = root.getChild("shell");
        eyeL = root.getChild("eyeL"); eyeR = root.getChild("eyeR");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition m = new MeshDefinition(); PartDefinition r = m.getRoot();
        // Soft body / foot
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2.5F, -1.0F, -4.0F, 5, 2, 8), PartPose.offset(0, 23, 0));
        // Spiral shell on top
        r.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 11)
                .addBox(-3.0F, -4.0F, -2.5F, 6, 4, 5, new CubeDeformation(0.2F)),
                PartPose.offset(0, 22, 0));
        // Eye stalks
        r.addOrReplaceChild("eyeL", CubeListBuilder.create().texOffs(18, 0)
                .addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1), PartPose.offset(1.5F, 22, -3.5F));
        r.addOrReplaceChild("eyeR", CubeListBuilder.create().texOffs(18, 0)
                .addBox(-0.5F, -3.0F, -0.5F, 1, 3, 1), PartPose.offset(-1.5F, 22, -3.5F));
        return LayerDefinition.create(m, 32, 16);
    }

    @Override
    public void setupAnim(BasePinataEntity e, float ls, float la, float age, float hy, float hp) {
        // Eye stalks sway gently
        float sway = (float) Math.sin(age * 0.15F) * 0.15F;
        eyeL.xRot = -0.2F + sway;
        eyeR.xRot = -0.2F - sway;
        eyeL.zRot = sway * 0.5F;
        eyeR.zRot = -sway * 0.5F;
        // Shell bobs slightly
        shell.y = 22F + (float) Math.sin(age * 0.08F) * 0.1F;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer b, int l, int o, int c) {
        body.render(ps,b,l,o,c); shell.render(ps,b,l,o,c);
        eyeL.render(ps,b,l,o,c); eyeR.render(ps,b,l,o,c);
    }
}
