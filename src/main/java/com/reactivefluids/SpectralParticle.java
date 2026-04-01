package com.reactivefluids;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Custom spectral particle — blue-white ethereal wisp that drifts
 * gently upward, shrinking and fading smoothly.
 */
public class SpectralParticle extends TextureSheetParticle {

    protected SpectralParticle(ClientLevel level, double x, double y, double z,
                                double xSpeed, double ySpeed, double zSpeed,
                                SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = xSpeed + (random.nextDouble() - 0.5) * 0.02;
        this.yd = ySpeed + random.nextDouble() * 0.03;
        this.zd = zSpeed + (random.nextDouble() - 0.5) * 0.02;
        this.lifetime = 10 + random.nextInt(8);
        this.gravity = -0.01F; // very slight upward drift
        this.quadSize = 0.06F + random.nextFloat() * 0.05F;

        // Blue-white color range
        this.rCol = 0.7F + random.nextFloat() * 0.2F;
        this.gCol = 0.8F + random.nextFloat() * 0.2F;
        this.bCol = 1.0F;
        this.alpha = 0.7F;

        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();

        // Shrink and fade smoothly
        float life = (float) this.age / (float) this.lifetime;
        this.quadSize *= 0.95F;
        this.alpha = Math.max(0, 0.7F * (1.0F - life * life));

        // Gentle drift dampening
        this.xd *= 0.92;
        this.zd *= 0.92;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880; // Full brightness — self-lit
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                        double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed) {
            return new SpectralParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
