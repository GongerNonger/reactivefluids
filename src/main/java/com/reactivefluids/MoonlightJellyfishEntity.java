package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

/**
 * Moonlight Jellyfish — ambient bioluminescent water creature.
 * Drifts slowly through water, periodically pulsing with a soft glow.
 * The glow cycle is driven by a tick counter synced to clients.
 */
public class MoonlightJellyfishEntity extends WaterAnimal {

    // Glow cycle: 0 → GLOW_CYCLE_LENGTH, resets. Peak glow at cycle midpoint.
    private static final int GLOW_CYCLE_LENGTH = 80; // 4 seconds per pulse

    private static final EntityDataAccessor<Integer> DATA_GLOW_TICK =
            SynchedEntityData.defineId(MoonlightJellyfishEntity.class, EntityDataSerializers.INT);

    public MoonlightJellyfishEntity(EntityType<? extends WaterAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WaterAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.12);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GLOW_TICK, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RandomSwimmingGoal(this, 0.8, 20));
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            int glowTick = entityData.get(DATA_GLOW_TICK) + 1;
            if (glowTick >= GLOW_CYCLE_LENGTH) glowTick = 0;
            entityData.set(DATA_GLOW_TICK, glowTick);
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(0.01F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            // Slow drift — jellyfish are lazy swimmers
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
        } else {
            super.travel(travelVector);
        }
    }

    /**
     * Returns glow intensity 0.0–1.0 based on the pulse cycle.
     * Uses a sine wave for smooth pulsing.
     */
    public float getGlowIntensity(float partialTick) {
        float phase = (entityData.get(DATA_GLOW_TICK) + partialTick) / GLOW_CYCLE_LENGTH;
        // Sine wave: peaks at 0.25 of cycle, troughs at 0.75
        float raw = (Mth.sin(phase * Mth.TWO_PI) + 1.0f) * 0.5f;
        return Mth.clamp(raw, 0.1f, 1.0f); // never fully dark
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getFluidState(pos).isEmpty() ? -1.0f : 0.0f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GLOW_SQUID_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120; // less chatty
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GlowTick", entityData.get(DATA_GLOW_TICK));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GlowTick")) {
            entityData.set(DATA_GLOW_TICK, tag.getInt("GlowTick"));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToPlayer) {
        return false; // persist — they're special
    }
}
