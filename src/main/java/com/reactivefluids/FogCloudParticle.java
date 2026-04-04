package com.reactivefluids;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Custom fog cloud particle -- large, slow-drifting, translucent white-gray
 * billows that overlap to form a dense persistent fog.
 */
public class FogCloudParticle extends TextureSheetParticle {

    protected FogCloudParticle(ClientLevel level, double x, double y, double z,
                                double xSpeed, double ySpeed, double zSpeed,
                                SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = (random.nextDouble() - 0.5) * 0.002;
        this.yd = (random.nextDouble() - 0.5) * 0.001;
        this.zd = (random.nextDouble() - 0.5) * 0.002;
        this.lifetime = 40 + random.nextInt(30);
        this.gravity = 0;
        this.quadSize = 1.5F + random.nextFloat() * 1.0F;

        // White-gray tint
        this.rCol = 0.85F + random.nextFloat() * 0.10F;
        this.gCol = 0.85F + random.nextFloat() * 0.10F;
        this.bCol = 0.90F + random.nextFloat() * 0.10F;
        this.alpha = 0.7F;

        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();

        // Fade alpha in last third of life
        float life = (float) this.age / (float) this.lifetime;
        if (life > 0.66F) {
            float fadeProgress = (life - 0.66F) / 0.34F;
            this.alpha = Math.max(0, 0.7F * (1.0F - fadeProgress));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880; // Full brightness -- self-lit
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
            return new FogCloudParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
