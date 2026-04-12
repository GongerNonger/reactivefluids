package com.reactivefluids;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Persistent invisible entity that maintains a fog cloud area.
 * Continuously spawns dense fog particles client-side and periodically
 * applies Blindness to hostile mobs server-side.
 */
public class FogCloudEntity extends Entity {

    public static final int DURATION_TICKS = 600; // 30 seconds
    private static final int FADE_TICKS = 100;
    private static final int BLINDNESS_DURATION = 200; // 10 seconds

    private static final EntityDataAccessor<Integer> DATA_TICKS_REMAINING =
            SynchedEntityData.defineId(FogCloudEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_RADIUS =
            SynchedEntityData.defineId(FogCloudEntity.class, EntityDataSerializers.FLOAT);

    public FogCloudEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public int getTicksRemaining() {
        return entityData.get(DATA_TICKS_REMAINING);
    }

    public float getRadius() {
        return entityData.get(DATA_RADIUS);
    }

    public void setRadius(float radius) {
        entityData.set(DATA_RADIUS, radius);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TICKS_REMAINING, DURATION_TICKS);
        builder.define(DATA_RADIUS, 10.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            // --- Server side ---
            int remaining = entityData.get(DATA_TICKS_REMAINING) - 1;
            entityData.set(DATA_TICKS_REMAINING, remaining);

            if (remaining <= 0) {
                discard();
                return;
            }

            float radius = getRadius();

            // Every 20 ticks: apply Blindness to hostile mobs in range
            if (remaining % 20 == 0) {
                AABB area = new AABB(
                        getX() - radius, getY() - radius, getZ() - radius,
                        getX() + radius, getY() + radius, getZ() + radius);
                for (Entity entity : level().getEntities(this, area)) {
                    if (entity instanceof Monster monster) {
                        double distSq = entity.position().distanceToSqr(position());
                        if (distSq <= radius * radius) {
                            monster.addEffect(new MobEffectInstance(
                                    MobEffects.BLINDNESS, BLINDNESS_DURATION, 0, false, true));
                        }
                    }
                }
            }

            // Every 40 ticks: ambient hiss sound
            if (remaining % 40 == 0) {
                level().playSound(null, blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.AMBIENT, 0.2F, 1.0F);
            }
        } else {
            // --- Client side ---
            int remaining = getTicksRemaining();
            float radius = getRadius();

            // Determine particle count: 15-25 normally, reduced during fade
            int baseCount = 15 + random.nextInt(11); // 15 to 25
            int particleCount;
            if (remaining <= FADE_TICKS) {
                float fadeFraction = (float) remaining / (float) FADE_TICKS;
                particleCount = Math.max(1, (int) (baseCount * fadeFraction));
            } else {
                particleCount = baseCount;
            }

            for (int i = 0; i < particleCount; i++) {
                double theta = random.nextDouble() * Math.PI * 2;
                double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
                double r = random.nextDouble() * radius;
                double px = getX() + r * Math.sin(phi) * Math.cos(theta);
                double py = getY() + r * Math.sin(phi) * Math.sin(theta);
                double pz = getZ() + r * Math.cos(phi);
                level().addParticle(ModParticles.FOG_CLOUD.get(), px, py, pz, 0, 0, 0);
            }
        }
    }

    // --- No collision, invisible hitbox, no pushing ---

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128 * 128;
    }

    // --- Serialization ---

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_TICKS_REMAINING, tag.getInt("TicksRemaining"));
        if (tag.contains("Radius")) {
            entityData.set(DATA_RADIUS, tag.getFloat("Radius"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksRemaining", entityData.get(DATA_TICKS_REMAINING));
        tag.putFloat("Radius", entityData.get(DATA_RADIUS));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

}
