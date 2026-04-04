package com.reactivefluids;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Necromantic soul particle — inspired by vanilla soul speed particles.
 * Teal-green wispy mote that rises and sways, cycling through 4 animation
 * frames (forming → full → fading → dissipating) via setSpriteFromAge.
 * Self-lit like soul fire.
 */
public class NecroticParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected NecroticParticle(ClientLevel level, double x, double y, double z,
                                double xSpeed, double ySpeed, double zSpeed,
                                SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = xSpeed + (random.nextDouble() - 0.5) * 0.01;
        this.yd = 0.02 + random.nextDouble() * 0.02;
        this.zd = zSpeed + (random.nextDouble() - 0.5) * 0.01;
        // Match vanilla soul lifetime: (8.0 / (random * 0.8 + 0.2)) + 4 = ~12-44 ticks
        this.lifetime = (int)(8.0 / (random.nextDouble() * 0.8 + 0.2)) + 4;
        this.gravity = 0;
        this.quadSize = 0.15F + random.nextFloat() * 0.08F; // match vanilla soul scale(1.5)
        this.hasPhysics = false;
        this.friction = 0.96F; // natural dampening like RisingParticle

        // Teal-green color — the texture already has color, this tints it
        this.rCol = 0.15F + random.nextFloat() * 0.1F;
        this.gCol = 0.85F + random.nextFloat() * 0.15F;
        this.bCol = 0.6F + random.nextFloat() * 0.2F;
        this.alpha = 1.0F;

        this.setSpriteFromAge(sprites); // set initial frame
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites); // animate through frames

        float life = (float) this.age / (float) this.lifetime;

        // Gentle sway
        this.xd += Math.sin(this.age * 0.5) * 0.002;
        this.zd += Math.cos(this.age * 0.4) * 0.002;

        // Fade alpha in last 40% of life
        if (life > 0.6F) {
            this.alpha = Math.max(0, 1.0F - (life - 0.6F) * 2.5F);
        }
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
            return new NecroticParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
