package com.reactivefluids.pinata;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.level.Level;

/**
 * Storkos — the stork NPC that delivers piñata eggs after romance.
 *
 * VP behavior: flies in after a successful romance dance,
 * delivers an egg to the piñata's house, then flies away.
 *
 * MC implementation: Spawned by PinataRomanceGoal after successful romance.
 * Flies toward the romance location, drops a cosmetic particle effect,
 * then despawns. (The actual baby is spawned directly by the romance goal.)
 * Storkos exists purely for the visual spectacle.
 */
public class StorkosEntity extends PathfinderMob {

    private int lifetimeTicks = 200; // 10 seconds of presence
    private double targetX, targetY, targetZ;
    private boolean delivered;

    public StorkosEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public void setDeliveryTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        lifetimeTicks--;

        if (level().isClientSide()) {
            // Flying trail particles
            if (random.nextInt(3) == 0) {
                level().addParticle(ParticleTypes.END_ROD,
                        getX(), getY() + 0.5, getZ(), 0, -0.01, 0);
            }
            return;
        }

        if (lifetimeTicks <= 0) {
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CLOUD,
                        getX(), getY() + 1, getZ(), 10, 0.3, 0.3, 0.3, 0.02);
            }
            discard();
            return;
        }

        // Fly toward target
        if (!delivered) {
            getNavigation().moveTo(targetX, targetY, targetZ, 1.5);
            double dist = distanceToSqr(targetX, targetY, targetZ);
            if (dist < 4.0) {
                // Deliver!
                delivered = true;
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HEART,
                            targetX, targetY + 1.5, targetZ,
                            10, 0.5, 0.3, 0.5, 0);
                    sl.sendParticles(ParticleTypes.FIREWORK,
                            targetX, targetY + 1, targetZ,
                            5, 0.3, 0.2, 0.3, 0.05);
                }
                playSound(SoundEvents.CHICKEN_EGG, 1.0F, 0.8F);
                lifetimeTicks = Math.min(lifetimeTicks, 60); // Leave soon after delivery
            }
        }
    }

    @Override public boolean isPersistenceRequired() { return true; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean shouldDropExperience() { return false; }
}
