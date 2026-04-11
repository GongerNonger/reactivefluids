package com.reactivefluids;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = ReactiveFluids.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class NightSkyboxRenderer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/environment/night_skybox.png");

    private static final float SKY_DISTANCE = 100.0f;
    private static final float U_STRIDE = 1.0f / 3.0f;
    private static final float V_STRIDE = 1.0f / 2.0f;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float starBrightness = level.getStarBrightness(partialTick);
        if (starBrightness <= 0.01f) return;
        float rainFactor = 1.0f - level.getRainLevel(partialTick);
        float alpha = starBrightness * rainFactor * 2.0f;
        if (alpha > 1.0f) alpha = 1.0f;
        if (alpha <= 0.01f) return;

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        // Pin the cube to the camera position so it travels with the player.
        // At AFTER_SKY the poseStack is in world space (vanilla's sky push/pops
        // have already balanced out), so drawing around origin without this
        // translate would leave the cube sitting at world (0,0,0).
        poseStack.translate(camPos.x, camPos.y, camPos.z);

        // Apply vanilla's celestial rotation on top of the camera translation.
        // YP(-90) aligns the cubemap's "north" face with Minecraft +X, then XP
        // rotates the whole sphere through its 24000-tick daily cycle.
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(level.getTimeOfDay(partialTick) * 360.0f));

        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, TEXTURE);

        Matrix4f matrix = poseStack.last().pose();
        int color = 0xFFFFFF | ((int) (alpha * 255) << 24);

        BufferBuilder buffer = Tesselator.getInstance()
                .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        drawCubemap(buffer, matrix, SKY_DISTANCE, color);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        poseStack.popPose();
    }

    private static void drawCubemap(BufferBuilder buf, Matrix4f m, float s, int color) {
        // UV grid (384×256, 128×128 per face):
        //   row 0: [bottom 0,0] [top 1,0] [south 2,0]
        //   row 1: [west 0,1]   [north 1,1] [east 2,1]
        float bu0 = 0f,           bu1 = U_STRIDE,       bv0 = 0f,          bv1 = V_STRIDE;       // bottom
        float tu0 = U_STRIDE,     tu1 = 2f * U_STRIDE,  tv0 = 0f,          tv1 = V_STRIDE;       // top
        float su0 = 2f * U_STRIDE,su1 = 1f,             sv0 = 0f,          sv1 = V_STRIDE;       // south
        float wu0 = 0f,           wu1 = U_STRIDE,       wv0 = V_STRIDE,    wv1 = 1f;             // west
        float nu0 = U_STRIDE,     nu1 = 2f * U_STRIDE,  nv0 = V_STRIDE,    nv1 = 1f;             // north
        float eu0 = 2f * U_STRIDE,eu1 = 1f,             ev0 = V_STRIDE,    ev1 = 1f;             // east

        // TOP  (+Y)
        quad(buf, m,
                -s, +s, +s, tu0, tv0,
                +s, +s, +s, tu1, tv0,
                +s, +s, -s, tu1, tv1,
                -s, +s, -s, tu0, tv1,
                color);

        // BOTTOM (-Y) — painted black, mostly unseen
        quad(buf, m,
                -s, -s, -s, bu0, bv0,
                +s, -s, -s, bu1, bv0,
                +s, -s, +s, bu1, bv1,
                -s, -s, +s, bu0, bv1,
                color);

        // NORTH (-Z)
        quad(buf, m,
                -s, +s, -s, nu0, nv0,
                +s, +s, -s, nu1, nv0,
                +s, -s, -s, nu1, nv1,
                -s, -s, -s, nu0, nv1,
                color);

        // SOUTH (+Z)
        quad(buf, m,
                +s, +s, +s, su0, sv0,
                -s, +s, +s, su1, sv0,
                -s, -s, +s, su1, sv1,
                +s, -s, +s, su0, sv1,
                color);

        // EAST (+X)
        quad(buf, m,
                +s, +s, -s, eu0, ev0,
                +s, +s, +s, eu1, ev0,
                +s, -s, +s, eu1, ev1,
                +s, -s, -s, eu0, ev1,
                color);

        // WEST (-X)
        quad(buf, m,
                -s, +s, +s, wu0, wv0,
                -s, +s, -s, wu1, wv0,
                -s, -s, -s, wu1, wv1,
                -s, -s, +s, wu0, wv1,
                color);
    }

    private static void quad(BufferBuilder buf, Matrix4f m,
                             float x1, float y1, float z1, float u1, float v1,
                             float x2, float y2, float z2, float u2, float v2,
                             float x3, float y3, float z3, float u3, float v3,
                             float x4, float y4, float z4, float u4, float v4,
                             int color) {
        buf.addVertex(m, x1, y1, z1).setUv(u1, v1).setColor(color);
        buf.addVertex(m, x2, y2, z2).setUv(u2, v2).setColor(color);
        buf.addVertex(m, x3, y3, z3).setUv(u3, v3).setColor(color);
        buf.addVertex(m, x4, y4, z4).setUv(u4, v4).setColor(color);
    }
}
