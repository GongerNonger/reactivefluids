package com.reactivefluids;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.IOException;
import java.io.InputStream;

/**
 * Entity texture that cycles through animation frames from a vertical spritesheet.
 * Unlike block textures, entity textures don't support .mcmeta animation —
 * this class handles frame cycling manually by uploading each frame to the GPU.
 */
@OnlyIn(Dist.CLIENT)
public class AnimatedEntityTexture extends AbstractTexture {

    private final ResourceLocation spritesheet;
    private final int frameWidth;
    private final int frameHeight;
    private final int frameCount;
    private final int ticksPerFrame;

    private NativeImage[] frames;
    private int currentFrame = 0;
    private int tickCounter = 0;
    private boolean loaded = false;

    public AnimatedEntityTexture(ResourceLocation spritesheet, int frameWidth, int frameHeight,
                                  int frameCount, int ticksPerFrame) {
        this.spritesheet = spritesheet;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameCount = frameCount;
        this.ticksPerFrame = ticksPerFrame;
    }

    @Override
    public void load(ResourceManager resourceManager) throws IOException {
        this.close();

        NativeImage fullImage;
        try (InputStream stream = resourceManager.getResource(spritesheet).orElseThrow().open()) {
            fullImage = NativeImage.read(stream);
        }

        // Split spritesheet into individual frame images
        frames = new NativeImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = new NativeImage(frameWidth, frameHeight, false);
            int yOffset = i * frameHeight;
            for (int y = 0; y < frameHeight; y++) {
                for (int x = 0; x < frameWidth; x++) {
                    if (yOffset + y < fullImage.getHeight() && x < fullImage.getWidth()) {
                        frames[i].setPixelRGBA(x, y, fullImage.getPixelRGBA(x, yOffset + y));
                    }
                }
            }
        }
        fullImage.close();

        // Allocate GL texture at the correct frame size and upload first frame
        TextureUtil.prepareImage(this.getId(), frameWidth, frameHeight);
        frames[0].upload(0, 0, 0, false);
        loaded = true;
    }

    /**
     * Call this every client tick to advance the animation.
     */
    public void tick() {
        if (!loaded || frames == null) return;
        if (++tickCounter >= ticksPerFrame) {
            tickCounter = 0;
            currentFrame = (currentFrame + 1) % frameCount;
            RenderSystem.assertOnRenderThread();
            this.bind();
            frames[currentFrame].upload(0, 0, 0, false);
        }
    }

    @Override
    public void close() {
        if (frames != null) {
            for (NativeImage frame : frames) {
                if (frame != null) frame.close();
            }
            frames = null;
        }
        loaded = false;
    }
}
