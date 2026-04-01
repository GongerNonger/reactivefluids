package com.reactivefluids;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Renders blocky crystal prisms growing off the crystallized skeleton.
 * Style matches Minecraft's amethyst clusters — rectangular prisms with
 * flat-cut angled tops, not smooth geometry.
 */
@OnlyIn(Dist.CLIENT)
public class CrystalOverlayLayer extends RenderLayer<CrystallizedSkeleton, SkeletonModel<CrystallizedSkeleton>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final RenderType CRYSTAL_TYPE =
            RenderType.entityTranslucentEmissive(TEXTURE);

    /**
     * A blocky crystal prism attached to a body part.
     * x,y,z: base center in model units (1/16 block)
     * rotX,rotY: euler rotation in degrees — orients the prism
     * width: cross-section half-width (square profile)
     * height: total height of the prism
     */
    private record Crystal(float x, float y, float z,
                           float rotX, float rotY,
                           float width, float height) {}

    // Skeleton head: 8x8x8 cube, pivot at y=0 (neck), head extends y=-8 to y=0
    // +Y is down in model space
    private static final Crystal[] HEAD_CRYSTALS = {
            new Crystal( 0.0f, -8.0f,  0.0f,    0,   0,   0.7f, 4.0f),  // center crown, straight up
            new Crystal(-2.5f, -8.0f,  1.0f,   15,  30,   0.5f, 3.0f),  // left-front, tilted
            new Crystal( 2.5f, -8.0f, -0.5f,   10, -20,   0.5f, 2.5f),  // right-back
            new Crystal( 0.5f, -8.0f, -3.0f,   20,   0,   0.4f, 2.0f),  // back
            new Crystal(-1.5f, -8.0f, -2.0f,   12,  15,   0.4f, 1.8f),  // back-left small
    };

    // Body: 8x12x4, pivot at y=0 (top), extends y=0 to y=12
    private static final Crystal[] BODY_CRYSTALS = {
            new Crystal( 0.0f,  2.0f, -2.0f,   80,   0,   0.6f, 2.5f),  // chest center, pointing forward
            new Crystal(-2.0f,  3.0f, -2.0f,   75, -15,   0.5f, 2.0f),  // left chest
            new Crystal( 2.0f,  2.5f, -2.0f,   75,  15,   0.5f, 2.2f),  // right chest
            new Crystal( 0.5f,  5.0f,  2.0f,  -80,   0,   0.5f, 1.8f),  // back spine
    };

    // Arms: 4x12x4, pivot at y=-2 (shoulder), extends y=-2 to y=10
    private static final Crystal[] RIGHT_ARM_CRYSTALS = {
            new Crystal(-2.0f, -1.0f,  0.0f,    0,  90,   0.6f, 3.0f),  // shoulder, pointing outward
            new Crystal(-2.0f,  1.0f,  1.0f,   20,  80,   0.4f, 2.0f),  // shoulder side
            new Crystal(-2.0f,  3.0f, -0.5f,  -10,  85,   0.4f, 1.5f),  // upper arm
    };

    private static final Crystal[] LEFT_ARM_CRYSTALS = {
            new Crystal( 2.0f, -1.0f,  0.0f,    0, -90,   0.6f, 3.0f),
            new Crystal( 2.0f,  1.0f,  1.0f,   20, -80,   0.4f, 2.0f),
            new Crystal( 2.0f,  3.0f, -0.5f,  -10, -85,   0.4f, 1.5f),
    };

    // Legs: 4x12x4, pivot at y=0 (hip), extends y=0 to y=12
    private static final Crystal[] RIGHT_LEG_CRYSTALS = {
            new Crystal( 0.0f,  5.0f, -2.0f,   75,   0,   0.5f, 1.8f),  // knee front
            new Crystal(-0.5f,  7.0f, -1.8f,   80,  10,   0.4f, 1.3f),  // below knee
    };

    private static final Crystal[] LEFT_LEG_CRYSTALS = {
            new Crystal( 0.0f,  5.0f, -2.0f,   75,   0,   0.5f, 1.8f),
            new Crystal( 0.5f,  7.0f, -1.8f,   80, -10,   0.4f, 1.3f),
    };

    public CrystalOverlayLayer(RenderLayerParent<CrystallizedSkeleton, SkeletonModel<CrystallizedSkeleton>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       CrystallizedSkeleton entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        VertexConsumer consumer = bufferSource.getBuffer(CRYSTAL_TYPE);
        HumanoidModel<?> model = (HumanoidModel<?>) this.getParentModel();
        int light = 15728880;

        renderCrystalsOnPart(poseStack, consumer, model.head, HEAD_CRYSTALS, light, ageInTicks);
        renderCrystalsOnPart(poseStack, consumer, model.body, BODY_CRYSTALS, light, ageInTicks);
        renderCrystalsOnPart(poseStack, consumer, model.rightArm, RIGHT_ARM_CRYSTALS, light, ageInTicks);
        renderCrystalsOnPart(poseStack, consumer, model.leftArm, LEFT_ARM_CRYSTALS, light, ageInTicks);
        renderCrystalsOnPart(poseStack, consumer, model.rightLeg, RIGHT_LEG_CRYSTALS, light, ageInTicks);
        renderCrystalsOnPart(poseStack, consumer, model.leftLeg, LEFT_LEG_CRYSTALS, light, ageInTicks);
    }

    private void renderCrystalsOnPart(PoseStack poseStack, VertexConsumer consumer,
                                       ModelPart part, Crystal[] crystals, int light, float ageInTicks) {
        poseStack.pushPose();
        part.translateAndRotate(poseStack);
        // Model units are 1/16 block; render space is in blocks
        poseStack.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);

        for (Crystal c : crystals) {
            renderPrism(poseStack, consumer, c, light, ageInTicks);
        }
        poseStack.popPose();
    }

    /**
     * Renders a single crystal as a blocky rectangular prism — an elongated box
     * with a flat top, oriented by euler rotation. Looks like vanilla amethyst.
     */
    private void renderPrism(PoseStack poseStack, VertexConsumer consumer,
                              Crystal c, int light, float ageInTicks) {
        poseStack.pushPose();
        poseStack.translate(c.x, c.y, c.z);

        // Rotate — crystal grows in -Y direction by default (upward in world),
        // rotX/rotY tilt it. rotX tilts forward/back, rotY tilts left/right.
        poseStack.mulPose(Axis.YP.rotationDegrees(c.rotY));
        poseStack.mulPose(Axis.XP.rotationDegrees(c.rotX));

        float w = c.width;
        float h = c.height;

        // Subtle shimmer
        float shimmer = (float) (Math.sin(ageInTicks * 0.12 + c.x * 3 + c.y * 5) * 0.08 + 0.92);

        // Two-tone: sides are deeper blue, top face is pale ice-white
        int sideR = (int)(110 * shimmer), sideG = (int)(170 * shimmer), sideB = (int)(210 * shimmer);
        int topR  = (int)(190 * shimmer), topG  = (int)(230 * shimmer), topB  = (int)(250 * shimmer);
        int sideA = (int)(200 * shimmer);
        int topA  = (int)(230 * shimmer);

        PoseStack.Pose pose = poseStack.last();

        // Box from (−w, −h, −w) to (+w, 0, +w)
        // 6 faces, 4 vertices each = 24 vertices
        float x0 = -w, x1 = w;
        float y0 = -h, y1 = 0;
        float z0 = -w, z1 = w;

        // Top face (y = y0, pointing -Y = upward in model space)
        quad(pose, consumer, x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1,
                0,-1,0, topR,topG,topB,topA, light);

        // Bottom face (y = y1, facing +Y = downward, hidden against body)
        quad(pose, consumer, x0,y1,z1, x1,y1,z1, x1,y1,z0, x0,y1,z0,
                0,1,0, sideR,sideG,sideB,sideA, light);

        // Front face (z = z0, facing -Z)
        quad(pose, consumer, x0,y0,z0, x0,y1,z0, x1,y1,z0, x1,y0,z0,
                0,0,-1, sideR,sideG,sideB,sideA, light);

        // Back face (z = z1, facing +Z)
        quad(pose, consumer, x1,y0,z1, x1,y1,z1, x0,y1,z1, x0,y0,z1,
                0,0,1, sideR,sideG,sideB,sideA, light);

        // Left face (x = x0, facing -X)
        quad(pose, consumer, x0,y0,z1, x0,y1,z1, x0,y1,z0, x0,y0,z0,
                -1,0,0, sideR,sideG,sideB,sideA, light);

        // Right face (x = x1, facing +X)
        quad(pose, consumer, x1,y0,z0, x1,y1,z0, x1,y1,z1, x1,y0,z1,
                1,0,0, sideR,sideG,sideB,sideA, light);

        poseStack.popPose();
    }

    /** Emit a single quad (4 vertices) with flat shading. */
    private void quad(PoseStack.Pose pose, VertexConsumer consumer,
                      float x0, float y0, float z0,
                      float x1, float y1, float z1,
                      float x2, float y2, float z2,
                      float x3, float y3, float z3,
                      float nx, float ny, float nz,
                      int r, int g, int b, int a, int light) {
        consumer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a)
                .setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a)
                .setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a)
                .setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a)
                .setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, nx, ny, nz);
    }
}
