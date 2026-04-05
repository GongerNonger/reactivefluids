package com.reactivefluids.pinata;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Optional;

/**
 * Generic GeckoLib renderer for piñata species with Bedrock geo models.
 * Adds procedural idle + walk animations by manipulating bones in actuallyRender.
 */
public class PinataGeoRenderer extends GeoEntityRenderer<BasePinataEntity> {

    private final String species;
    private final boolean hasAnimation;
    private BakedGeoModel currentModel;

    public PinataGeoRenderer(EntityRendererProvider.Context context, String species, boolean hasAnimation) {
        super(context, new PinataGeoModel(species, hasAnimation));
        this.species = species;
        this.hasAnimation = hasAnimation;
        this.shadowRadius = 0.4F;
    }

    public static EntityRendererProvider<BasePinataEntity> provider(String species) {
        return ctx -> new PinataGeoRenderer(ctx, species, false);
    }

    public static EntityRendererProvider<BasePinataEntity> providerAnimated(String species) {
        return ctx -> new PinataGeoRenderer(ctx, species, true);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, BasePinataEntity entity, BakedGeoModel model,
                               RenderType renderType, MultiBufferSource bufferSource,
                               VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, int colour) {
        this.currentModel = model;

        if (!isReRender) {
            float age = entity.tickCount + partialTick;
            float walkSpeed = entity.walkAnimation.speed(partialTick);
            float walkPos = entity.walkAnimation.position(partialTick);

            switch (species) {
                case "whirlm" -> animateWhirlm(age, walkSpeed, walkPos);
                case "sparrowmint" -> animateBird(age, walkSpeed, walkPos);
                case "bunnycomb" -> animateBunnycomb(age, walkSpeed, walkPos);
                case "mousemallow" -> animateMousemallow(age, walkSpeed, walkPos);
                case "custacean" -> animateCustacean(age, walkSpeed, walkPos);
                case "parmadillo" -> animateParmadillo(age, walkSpeed, walkPos);
                case "pengum" -> animatePengum(age, walkSpeed, walkPos);
            }
        }

        super.actuallyRender(poseStack, entity, model, renderType, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay, colour);
    }

    private void animateWhirlm(float age, float walkSpeed, float walkPos) {
        float idle = age * 0.1F;
        rotateBone("body1", Mth.sin(idle) * 0.15F, 0, 0);
        rotateBone("body2", Mth.sin(idle - 0.5F) * 0.12F, 0, 0);
        rotateBone("body3", Mth.sin(idle - 1.0F) * 0.1F, 0, 0);
        if (walkSpeed > 0.01F) {
            addRotation("body1", 0, Mth.sin(walkPos * 0.5F) * walkSpeed * 0.3F, 0);
            addRotation("body2", 0, -Mth.sin(walkPos * 0.5F) * walkSpeed * 0.3F, 0);
        }
    }

    private void animateBird(float age, float walkSpeed, float walkPos) {
        float idle = age * 0.08F;
        rotateBone("head", Mth.sin(idle) * 0.1F, Mth.sin(idle * 0.7F) * 0.08F, 0);
        rotateBone("left_wing", 0, 0, Mth.sin(idle * 1.5F) * 0.1F);
        rotateBone("right_wing", 0, 0, -Mth.sin(idle * 1.5F) * 0.1F);
        rotateBone("tail", Mth.sin(idle * 0.6F) * 0.05F, Mth.sin(idle * 0.8F) * 0.15F, 0);
        if (walkSpeed > 0.01F) {
            rotateBone("feet", Mth.sin(walkPos * 0.8F) * walkSpeed * 0.4F, 0, 0);
            translateBone("body", 0, Mth.abs(Mth.sin(walkPos * 0.8F)) * walkSpeed * 0.5F, 0);
        }
    }

    private void animateBunnycomb(float age, float walkSpeed, float walkPos) {
        float idle = age * 0.08F;
        rotateBone("head", Mth.sin(idle) * 0.08F, Mth.sin(idle * 0.6F) * 0.1F, 0);
        rotateBone("left_ear", Mth.sin(idle * 1.2F) * 0.1F, 0, Mth.sin(idle * 0.9F) * 0.05F);
        rotateBone("right_ear", Mth.sin(idle * 1.2F + 0.5F) * 0.1F, 0, -Mth.sin(idle * 0.9F + 0.5F) * 0.05F);
        rotateBone("tail", 0, Mth.sin(idle * 1.5F) * 0.2F, 0);
        if (walkSpeed > 0.01F) {
            float hop = Mth.sin(walkPos * 0.6F);
            translateBone("bone", 0, Mth.abs(hop) * walkSpeed * 1.5F, 0);
            rotateBone("F_left_leg", hop * walkSpeed * 0.5F, 0, 0);
            rotateBone("F_right_leg", -hop * walkSpeed * 0.5F, 0, 0);
            rotateBone("B_left_legs", -hop * walkSpeed * 0.6F, 0, 0);
            rotateBone("B_right_legs", hop * walkSpeed * 0.6F, 0, 0);
        }
    }

    private void animateMousemallow(float age, float walkSpeed, float walkPos) {
        float idle = age * 0.1F;
        rotateBone("head", Mth.sin(idle * 0.8F) * 0.1F, Mth.sin(idle * 0.5F) * 0.15F, 0);
        rotateBone("bigode", 0, 0, Mth.sin(idle * 3.0F) * 0.05F);
        rotateBone("bigode2", 0, 0, -Mth.sin(idle * 3.0F) * 0.05F);
        rotateBone("left_ear", Mth.sin(idle * 1.5F) * 0.08F, 0, 0);
        rotateBone("right_ear", Mth.sin(idle * 1.5F + 1.0F) * 0.08F, 0, 0);
        rotateBone("tail", Mth.sin(idle * 0.7F) * 0.1F, Mth.sin(idle * 0.5F) * 0.15F, 0);
        rotateBone("tail2", 0, Mth.sin(idle * 0.5F + 0.3F) * 0.1F, 0);
        rotateBone("tail3", 0, Mth.sin(idle * 0.5F + 0.6F) * 0.1F, 0);
        rotateBone("tail4", 0, Mth.sin(idle * 0.5F + 0.9F) * 0.1F, 0);
        if (walkSpeed > 0.01F) {
            float step = Mth.sin(walkPos * 0.8F) * walkSpeed;
            rotateBone("F_left_legs", step * 0.5F, 0, 0);
            rotateBone("F_right_legs", -step * 0.5F, 0, 0);
            rotateBone("B_left_legs", -step * 0.5F, 0, 0);
            rotateBone("B_right_legs", step * 0.5F, 0, 0);
        }
    }

    private void animateCustacean(float age, float walkSpeed, float walkPos) {
        float idle = age * 0.08F;
        rotateBone("eye1", Mth.sin(idle) * 0.1F, Mth.sin(idle * 0.7F) * 0.1F, 0);
        rotateBone("eye2", Mth.sin(idle + 0.5F) * 0.1F, Mth.sin(idle * 0.7F + 0.3F) * 0.1F, 0);
        rotateBone("claw_left", 0, Mth.sin(idle * 0.6F) * 0.15F, 0);
        rotateBone("claw_right", 0, -Mth.sin(idle * 0.6F + 0.8F) * 0.15F, 0);
        rotateBone("body", 0, 0, Mth.sin(idle * 0.4F) * 0.03F);
        for (int i = 1; i <= 8; i++) {
            float phase = (i % 2 == 0) ? 1.0F : -1.0F;
            if (walkSpeed > 0.01F) {
                rotateBone("leg" + i, Mth.sin(walkPos * 1.2F + i * 0.5F) * walkSpeed * phase * 0.5F, 0, 0);
            } else {
                rotateBone("leg" + i, Mth.sin(idle * 0.5F + i * 0.4F) * 0.05F, 0, 0);
            }
        }
    }

    private void animateParmadillo(float age, float walkSpeed, float walkPos) {
        float idle = age * 0.07F;
        rotateBone("head", Mth.sin(idle) * 0.1F, Mth.sin(idle * 0.6F) * 0.08F, 0);
        rotateBone("leftear", Mth.sin(idle * 2.0F) * 0.08F, 0, 0);
        rotateBone("rightear", Mth.sin(idle * 2.0F + 1.0F) * 0.08F, 0, 0);
        rotateBone("tail", 0, Mth.sin(idle * 0.8F) * 0.15F, Mth.sin(idle * 0.5F) * 0.05F);
        // Shell breathe
        Optional<GeoBone> shellOpt = currentModel.getBone("shell");
        shellOpt.ifPresent(shell -> {
            float breathe = Mth.sin(idle * 0.5F) * 0.01F;
            shell.updateScale(1.0F + breathe, 1.0F - breathe, 1.0F + breathe);
        });
        if (walkSpeed > 0.01F) {
            float step = Mth.sin(walkPos * 0.7F) * walkSpeed;
            rotateBone("frontleg1", step * 0.4F, 0, 0);
            rotateBone("frontleg2", -step * 0.4F, 0, 0);
            rotateBone("backleg1", -step * 0.4F, 0, 0);
            rotateBone("backleg2", step * 0.4F, 0, 0);
        }
    }

    private void animatePengum(float age, float walkSpeed, float walkPos) {
        float idle = age * 0.08F;
        rotateBone("head", Mth.sin(idle * 0.6F) * 0.08F, Mth.sin(idle * 0.4F) * 0.15F, 0);
        rotateBone("beak_down", Mth.sin(idle * 2.0F) * 0.03F, 0, 0);
        rotateBone("left_flap", 0, 0, Mth.sin(idle * 0.5F) * 0.08F);
        rotateBone("right_flap", 0, 0, -Mth.sin(idle * 0.5F) * 0.08F);
        rotateBone("hair1", Mth.sin(idle * 0.8F) * 0.05F, 0, Mth.sin(idle) * 0.05F);
        rotateBone("hair2", Mth.sin(idle * 0.8F + 0.3F) * 0.05F, 0, Mth.sin(idle + 0.3F) * 0.05F);
        rotateBone("hair3", Mth.sin(idle * 0.8F + 0.6F) * 0.05F, 0, Mth.sin(idle + 0.6F) * 0.05F);
        if (walkSpeed > 0.01F) {
            rotateBone("body", 0, 0, Mth.sin(walkPos * 0.8F) * walkSpeed * 0.15F);
            rotateBone("left_leg", Mth.sin(walkPos * 0.8F) * walkSpeed * 0.5F, 0, 0);
            rotateBone("right_leg", -Mth.sin(walkPos * 0.8F) * walkSpeed * 0.5F, 0, 0);
            rotateBone("left_flap", 0, 0, Mth.sin(walkPos * 0.8F) * walkSpeed * 0.3F);
            rotateBone("right_flap", 0, 0, -Mth.sin(walkPos * 0.8F) * walkSpeed * 0.3F);
        }
    }

    // --- Bone helpers ---

    private void rotateBone(String name, float x, float y, float z) {
        if (currentModel == null) return;
        currentModel.getBone(name).ifPresent(bone -> {
            bone.updateRotation(x, y, z);
        });
    }

    private void addRotation(String name, float x, float y, float z) {
        if (currentModel == null) return;
        currentModel.getBone(name).ifPresent(bone -> {
            bone.updateRotation(bone.getRotX() + x, bone.getRotY() + y, bone.getRotZ() + z);
        });
    }

    private void translateBone(String name, float x, float y, float z) {
        if (currentModel == null) return;
        currentModel.getBone(name).ifPresent(bone -> {
            bone.updatePosition(x, y, z);
        });
    }
}
