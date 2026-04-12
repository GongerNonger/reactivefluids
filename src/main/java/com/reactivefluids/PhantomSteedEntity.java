package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * A conjured phantom horse — very fast, translucent, vanishes on any damage.
 * Based on D&D 5e Phantom Steed spell.
 */
public class PhantomSteedEntity extends AbstractHorse {

    public static final int DURATION_TICKS = 12000; // 10 minutes
    public static final int FADE_TICKS = 100;       // 5 second warning

    private static final EntityDataAccessor<Integer> DATA_TICKS_REMAINING =
            SynchedEntityData.defineId(PhantomSteedEntity.class, EntityDataSerializers.INT);

    public PhantomSteedEntity(EntityType<? extends AbstractHorse> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.55)
                .add(Attributes.JUMP_STRENGTH, 1.0);
    }

    // --- Synched data ---

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TICKS_REMAINING, DURATION_TICKS);
    }

    public int getTicksRemaining() {
        return entityData.get(DATA_TICKS_REMAINING);
    }

    // --- AI ---

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    // --- Tick / lifetime ---

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            int remaining = entityData.get(DATA_TICKS_REMAINING) - 1;
            entityData.set(DATA_TICKS_REMAINING, remaining);

            if (remaining <= 0) {
                ejectPassengers();
                spawnPoof();
                discard();
                return;
            }

            // Warning sounds during fade
            if (remaining <= FADE_TICKS && remaining % 20 == 0) {
                level().playSound(null, this, SoundEvents.PHANTOM_FLAP,
                        SoundSource.NEUTRAL, 0.6F, 1.5F);
            }
        }

        // Client-side spectral aura — particles stay close to body, don't obstruct rider
        if (level().isClientSide()) {
            double hw = getBbWidth() * 0.6;

            // Subtle body wisps — drift sideways/downward, never up into the rider
            if (random.nextInt(3) == 0) {
                double px = getX() + (random.nextDouble() - 0.5) * getBbWidth();
                double py = getY() + random.nextDouble() * getBbHeight() * 0.6; // lower half only
                double pz = getZ() + (random.nextDouble() - 0.5) * getBbWidth();
                double vx = (random.nextDouble() - 0.5) * 0.02;
                double vy = -0.01 - random.nextDouble() * 0.01; // drift DOWN
                double vz = (random.nextDouble() - 0.5) * 0.02;
                level().addParticle(ModParticles.SPECTRAL.get(), px, py, pz, vx, vy, vz);
            }

            // Hoof trail — soul fire at ground level behind each leg
            if (random.nextInt(2) == 0) {
                double angle = random.nextDouble() * Math.PI * 2;
                double dist = 0.3 + random.nextDouble() * 0.3;
                double px = getX() + Math.cos(angle) * dist;
                double pz = getZ() + Math.sin(angle) * dist;
                level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        px, getY() + 0.05, pz,
                        0, -0.02, 0); // sink into ground
            }

            // Occasional ground mist around hooves
            if (random.nextInt(5) == 0) {
                double px = getX() + (random.nextDouble() - 0.5) * getBbWidth() * 1.2;
                double pz = getZ() + (random.nextDouble() - 0.5) * getBbWidth() * 1.2;
                level().addParticle(ParticleTypes.SOUL,
                        px, getY() + 0.1, pz,
                        (random.nextDouble() - 0.5) * 0.01, 0.005, (random.nextDouble() - 0.5) * 0.01);
            }

            // Extra particles when fading
            int remaining = getTicksRemaining();
            if (remaining <= FADE_TICKS) {
                for (int i = 0; i < 3; i++) {
                    double px = getX() + (random.nextDouble() - 0.5) * getBbWidth() * 2;
                    double py = getY() + random.nextDouble() * getBbHeight();
                    double pz = getZ() + (random.nextDouble() - 0.5) * getBbWidth() * 2;
                    level().addParticle(ParticleTypes.ENCHANT, px, py, pz, 0, -0.1, 0);
                }
            }
        }
    }

    // --- Damage = instant poof ---

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide()) return true;
        ejectPassengers();
        spawnPoof();
        discard();
        return true;
    }

    private void spawnPoof() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    getX(), getY() + getBbHeight() / 2, getZ(),
                    30, getBbWidth() / 2, getBbHeight() / 2, getBbWidth() / 2, 0.05);
            level().playSound(null, this, SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.NEUTRAL, 1.0F, 0.8F);
        }
    }

    // --- Horse overrides ---

    @Override
    public boolean isTamed() { return true; }

    @Override
    public boolean isSaddled() { return true; }

    @Override
    public boolean isBaby() { return false; }

    @Override
    public boolean isFood(ItemStack stack) { return false; }

    @Override
    public boolean canMate(Animal other) { return false; }

    @Override
    public boolean canBeLeashed() { return false; }

    @Override
    public boolean canPerformRearing() { return true; }

    @Override
    public boolean shouldDropExperience() { return false; }

    @Override
    public boolean isPersistenceRequired() { return true; }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false; // No fall damage — ghostly
    }

    @Override
    protected void randomizeAttributes(RandomSource random) {
        // Fixed stats
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.isVehicle() && !player.isSecondaryUseActive()) {
            if (!this.level().isClientSide()) {
                player.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // --- Sounds ---

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() { return null; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SOUL_SAND_STEP, 0.3F, 1.0F);
    }

    // --- Save / Load ---

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TicksRemaining", entityData.get(DATA_TICKS_REMAINING));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TicksRemaining")) {
            entityData.set(DATA_TICKS_REMAINING, tag.getInt("TicksRemaining"));
        }
    }
}
