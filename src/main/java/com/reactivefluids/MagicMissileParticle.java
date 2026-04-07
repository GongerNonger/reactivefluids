package com.reactivefluids;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Custom arcane spark particle for Magic Missile trail.
 * Small, bright, fades quickly — leaves a comet-tail trail behind the dart.
 */
public class MagicMissileParticle extends TextureSheetParticle {

    protected MagicMissileParticle(ClientLevel level, double x, double y, double z,
                                    double xSpeed, double ySpeed, double zSpeed,
                                    SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.lifetime = 6 + random.nextInt(4); // very short — quick fade
        this.gravity = 0.0F; // no gravity
        this.quadSize = 0.04F + random.nextFloat() * 0.03F; // small

        // Purple-blue arcane color with slight variation
        this.rCol = 0.45F + random.nextFloat() * 0.15F;
        this.gCol = 0.25F + random.nextFloat() * 0.15F;
        this.bCol = 0.9F + random.nextFloat() * 0.1F;
        this.alpha = 0.9F;

        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        // Rapid shrink and fade
        float life = (float) this.age / (float) this.lifetime;
        this.quadSize *= 0.88F;
        this.alpha = Math.max(0, 0.9F * (1.0F - life));
        // Slow down
        this.xd *= 0.85;
        this.yd *= 0.85;
        this.zd *= 0.85;
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
            return new MagicMissileParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
