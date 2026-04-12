package com.reactivefluids;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Magic Missile projectile — a homing arcane bolt that never misses.
 * Seeks the nearest hostile mob and deals moderate damage on impact.
 * Spawns glowing purple-blue trail particles as it flies.
 */
public class MagicMissileEntity extends Entity {

    private static final float DAMAGE = 6.0F; // ~1d4+1 per missile
    private static final double SPEED = 0.8;
    private static final int MAX_LIFE = 60; // 3 seconds before expiring
    private static final double HOMING_STRENGTH = 0.15;

    private LivingEntity target;
    private int life;

    public MagicMissileEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true; // pass through blocks
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        // No synched data needed
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}

    @Override
    public void tick() {
        super.tick();
        this.life++;

        if (this.life > MAX_LIFE) {
            this.discard();
            return;
        }

        // Homing behavior — steer toward target
        if (this.target != null && this.target.isAlive()) {
            Vec3 toTarget = this.target.position().add(0, this.target.getBbHeight() * 0.5, 0)
                    .subtract(this.position()).normalize();
            Vec3 currentVel = this.getDeltaMovement().normalize();

            // Blend current direction with target direction
            Vec3 newDir = currentVel.scale(1.0 - HOMING_STRENGTH)
                    .add(toTarget.scale(HOMING_STRENGTH)).normalize();
            this.setDeltaMovement(newDir.scale(SPEED));

            // Check for impact
            double distSq = this.distanceToSqr(this.target);
            if (distSq < 1.5) {
                onHitTarget(this.target);
                return;
            }
        } else {
            // No target — fly straight
            if (this.getDeltaMovement().lengthSqr() < 0.01) {
                this.discard();
                return;
            }
        }

        // Move
        this.move(MoverType.SELF, this.getDeltaMovement());

        // Trail particles — custom Manus-textured arcane sparks
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ModParticles.MAGIC_MISSILE.get(),
                    this.getX(), this.getY(), this.getZ(),
                    2, 0.02, 0.02, 0.02, 0.01);
        }
    }

    private void onHitTarget(LivingEntity target) {
        if (this.level() instanceof ServerLevel serverLevel) {
            // Damage
            target.hurt(this.damageSources().magic(), DAMAGE);

            // Impact burst — custom arcane sparks
            serverLevel.sendParticles(ModParticles.MAGIC_MISSILE.get(),
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    10, 0.3, 0.3, 0.3, 0.08);

            // Impact sound
            serverLevel.playSound(null, target.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.5F);
        }
        this.discard();
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

}
