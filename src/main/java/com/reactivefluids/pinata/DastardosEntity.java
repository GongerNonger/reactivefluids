package com.reactivefluids.pinata;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

/**
 * Dastardos — the reaper of sick piñatas.
 *
 * VP behavior:
 * - Appears when a piñata is sick (happiness <= 10) for too long
 * - Flies/hovers toward the sick piñata
 * - Breaks it open (kills it) and laughs
 * - Cannot be killed by normal means (very high health, fast regen)
 * - Can be distracted by tamed Crowla or Sherbat
 * - Countered by curing the piñata before he arrives
 *
 * MC implementation:
 * - Spawned by GardenTickHandler when a piñata is sick for 60+ seconds
 * - Homes in on the sickest piñata
 * - Very high health + regen, immune to most damage
 * - Kills target piñata on contact
 * - Despawns after kill or if no sick piñatas remain
 * - Scary particles (soul/smoke) and sounds
 */
public class DastardosEntity extends PathfinderMob {

    private BasePinataEntity target;
    private int searchCooldown;
    private int lifetimeTicks;
    private static final int MAX_LIFETIME = 2400; // 2 minutes to find and kill

    public DastardosEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.lifetimeTicks = MAX_LIFETIME;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0) // Very hard to kill
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0); // Immune to knockback
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        // Custom targeting handled in tick()
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            // Spooky particles
            if (random.nextInt(3) == 0) {
                level().addParticle(ParticleTypes.SOUL,
                        getX() + (random.nextDouble() - 0.5) * getBbWidth(),
                        getY() + random.nextDouble() * getBbHeight(),
                        getZ() + (random.nextDouble() - 0.5) * getBbWidth(),
                        0, 0.02, 0);
            }
            if (random.nextInt(5) == 0) {
                level().addParticle(ParticleTypes.SMOKE,
                        getX() + (random.nextDouble() - 0.5) * 0.5,
                        getY(), getZ() + (random.nextDouble() - 0.5) * 0.5,
                        0, 0.01, 0);
            }
            return;
        }

        lifetimeTicks--;
        if (lifetimeTicks <= 0) {
            despawnWithEffect();
            return;
        }

        // Regenerate health
        if (tickCount % 20 == 0 && getHealth() < getMaxHealth()) {
            heal(2.0F);
        }

        // Find sick piñata target
        if (target == null || !target.isAlive() || target.getHappiness() > 10) {
            if (searchCooldown <= 0) {
                findSickTarget();
                searchCooldown = 40;
            } else {
                searchCooldown--;
            }

            // No target found → leave
            if (target == null && lifetimeTicks < MAX_LIFETIME - 600) {
                despawnWithEffect();
                return;
            }
        }

        // Chase target
        if (target != null && target.isAlive()) {
            getLookControl().setLookAt(target, 30, 30);
            getNavigation().moveTo(target, 1.3);

            // Close enough to kill
            double reach = (getBbWidth() + target.getBbWidth()) * 0.5 + 0.5;
            if (distanceToSqr(target) < reach * reach) {
                killTarget();
            }
        }
    }

    private void findSickTarget() {
        AABB area = getBoundingBox().inflate(64);
        List<BasePinataEntity> sickPinatas = level().getEntitiesOfClass(
                BasePinataEntity.class, area,
                p -> p.isAlive() && p.getHappiness() <= 10);

        if (!sickPinatas.isEmpty()) {
            // Target the sickest (lowest happiness), closest as tiebreaker
            sickPinatas.sort(Comparator
                    .<BasePinataEntity>comparingInt(BasePinataEntity::getHappiness)
                    .thenComparingDouble(this::distanceToSqr));
            target = sickPinatas.get(0);
        }
    }

    private void killTarget() {
        if (target != null && level() instanceof ServerLevel sl) {
            // Dramatic kill effect
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE,
                    target.getX(), target.getY(), target.getZ(),
                    10, 0.3, 0.3, 0.3, 0.05);

            // Evil laugh sound
            playSound(SoundEvents.WITHER_AMBIENT, 0.8F, 1.5F);

            // Kill the piñata (triggers candy drops)
            target.kill();
            target = null;

            // Dastardos leaves after the kill
            lifetimeTicks = Math.min(lifetimeTicks, 100);
        }
    }

    private void despawnWithEffect() {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL,
                    getX(), getY() + getBbHeight() / 2, getZ(),
                    15, 0.5, 0.5, 0.5, 0.05);
            playSound(SoundEvents.ENDERMAN_TELEPORT, 0.8F, 0.5F);
        }
        discard();
    }

    // Dastardos is very resistant to damage
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Reduce all damage significantly
        return super.hurt(source, amount * 0.2F);
    }

    @Override
    public boolean isPersistenceRequired() { return true; }
    @Override
    public boolean removeWhenFarAway(double distance) { return false; }
    @Override
    public boolean shouldDropExperience() { return false; }
    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) { /* No drops */ }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DastLifetime", lifetimeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("DastLifetime")) lifetimeTicks = tag.getInt("DastLifetime");
    }
}
