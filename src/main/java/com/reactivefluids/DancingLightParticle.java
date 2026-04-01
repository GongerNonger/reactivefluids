package com.reactivefluids;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Custom dancing light particle — warm golden glow that bobs gently,
 * pulsates in size, and fades softly.
 */
public class DancingLightParticle extends TextureSheetParticle {

    private final float baseSize;
    private final float pulseOffset;

    protected DancingLightParticle(ClientLevel level, double x, double y, double z,
                                    double xSpeed, double ySpeed, double zSpeed,
                                    SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = xSpeed + (random.nextDouble() - 0.5) * 0.02;
        this.yd = ySpeed + 0.01 + random.nextDouble() * 0.02;
        this.zd = zSpeed + (random.nextDouble() - 0.5) * 0.02;
        this.lifetime = 20 + random.nextInt(15);
        this.gravity = 0;
        this.baseSize = 0.1F + random.nextFloat() * 0.05F;
        this.quadSize = baseSize;
        this.pulseOffset = random.nextFloat() * (float) Math.PI * 2.0F;

        // Warm golden palette with variation
        float warmth = random.nextFloat();
        if (warmth < 0.3F) {
            // Bright white-yellow core
            this.rCol = 1.0F;
            this.gCol = 0.95F;
            this.bCol = 0.7F;
        } else if (warmth < 0.7F) {
            // Golden amber
            this.rCol = 1.0F;
            this.gCol = 0.75F + random.nextFloat() * 0.15F;
            this.bCol = 0.2F + random.nextFloat() * 0.2F;
        } else {
            // Warm orange
            this.rCol = 1.0F;
            this.gCol = 0.5F + random.nextFloat() * 0.2F;
            this.bCol = 0.1F;
        }
        this.alpha = 0.95F;

        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();

        float life = (float) this.age / (float) this.lifetime;

        // Gentle size pulsation
        float pulse = (float) Math.sin((this.age + pulseOffset) * 0.3) * 0.02F;
        this.quadSize = baseSize + pulse;

        // Fade out in last third of life
        if (life > 0.6F) {
            this.alpha = Math.max(0, 1.0F - (life - 0.6F) * 2.5F);
        }

        // Very gentle drift
        this.xd *= 0.98;
        this.zd *= 0.98;
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
            return new DancingLightParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
