package com.reactivefluids;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Custom disintegrate particle — green energy shard that spirals
 * outward, shrinks, and fades with a slight tumble rotation.
 */
public class DisintegrateParticle extends TextureSheetParticle {

    private final float rotSpeed;

    protected DisintegrateParticle(ClientLevel level, double x, double y, double z,
                                    double xSpeed, double ySpeed, double zSpeed,
                                    SpriteSet sprites) {
        super(level, x, y, z);
        // Minimal drift — particles should stay on the beam line
        this.xd = xSpeed * 0.02;
        this.yd = ySpeed * 0.02;
        this.zd = zSpeed * 0.02;
        this.lifetime = 6 + random.nextInt(5); // short-lived for crisp beam
        this.gravity = 0F; // no drift
        this.quadSize = 0.1F + random.nextFloat() * 0.04F;
        this.rotSpeed = (random.nextFloat() - 0.5F) * 0.4F;
        this.oRoll = random.nextFloat() * (float) Math.PI * 2.0F;
        this.roll = this.oRoll;

        // Green tint with variation
        this.rCol = 0.2F + random.nextFloat() * 0.3F;
        this.gCol = 0.8F + random.nextFloat() * 0.2F;
        this.bCol = 0.2F + random.nextFloat() * 0.2F;
        this.alpha = 0.95F;

        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += this.rotSpeed;

        // Fade out quickly — keep beam crisp
        float life = (float) this.age / (float) this.lifetime;
        this.alpha = Math.max(0, 1.0F - life);
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
            return new DisintegrateParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
