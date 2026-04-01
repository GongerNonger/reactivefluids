package com.reactivefluids;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A conjured spectral axolotl — follows the caster and periodically heals them.
 * Ghostly healer companion based on the axolotl's vanilla regeneration ability.
 */
public class SpectralAxolotlEntity extends Axolotl {

    public static final int DURATION_TICKS = 2400; // 2 minutes
    public static final int VARIANT_COUNT = 5; // lucy, wild, gold, cyan, blue
    private static final int HEAL_INTERVAL = 60;   // Heal every 3 seconds

    private static final EntityDataAccessor<Integer> DATA_TICKS_REMAINING =
            SynchedEntityData.defineId(SpectralAxolotlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(SpectralAxolotlEntity.class, EntityDataSerializers.INT);

    private UUID ownerUUID;

    public SpectralAxolotlEntity(EntityType<? extends Axolotl> type, Level level) {
        super(type, level);
        // Prevent drying out on land
        this.setAirSupply(this.getMaxAirSupply());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Axolotl.createAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TICKS_REMAINING, DURATION_TICKS);
        builder.define(DATA_VARIANT, 0);
    }

    public int getVariantIndex() { return entityData.get(DATA_VARIANT); }
    public void setVariantIndex(int v) { entityData.set(DATA_VARIANT, v % VARIANT_COUNT); }

    public int getTicksRemaining() {
        return entityData.get(DATA_TICKS_REMAINING);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal());
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        // Prevent drying out — spectral creatures don't need water
        this.setAirSupply(this.getMaxAirSupply());

        if (!level().isClientSide()) {
            int remaining = entityData.get(DATA_TICKS_REMAINING) - 1;
            entityData.set(DATA_TICKS_REMAINING, remaining);

            if (remaining <= 0) {
                spawnPoof();
                discard();
                return;
            }

            // Heal the owner periodically
            if (ownerUUID != null && remaining % HEAL_INTERVAL == 0) {
                Player owner = level().getPlayerByUUID(ownerUUID);
                if (owner != null && distanceToSqr(owner) < 100) { // within 10 blocks
                    owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, true));

                    // Healing particles
                    if (level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HEART,
                                getX(), getY() + getBbHeight(), getZ(),
                                3, 0.3, 0.2, 0.3, 0.0);
                        serverLevel.sendParticles(ParticleTypes.HEART,
                                owner.getX(), owner.getY() + owner.getBbHeight(), owner.getZ(),
                                2, 0.3, 0.2, 0.3, 0.0);
                    }
                }
            }

            if (remaining <= 100 && remaining % 20 == 0) {
                level().playSound(null, this, SoundEvents.AXOLOTL_IDLE_WATER,
                        SoundSource.NEUTRAL, 0.4F, 1.5F);
            }
        }

        if (level().isClientSide() && random.nextInt(4) == 0) {
            double px = getX() + (random.nextDouble() - 0.5) * getBbWidth();
            double py = getY() + random.nextDouble() * getBbHeight();
            double pz = getZ() + (random.nextDouble() - 0.5) * getBbWidth();
            level().addParticle(ModParticles.SPECTRAL.get(), px, py, pz, 0, 0.02, 0);
        }
    }

    private void spawnPoof() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    getX(), getY() + getBbHeight() / 2, getZ(),
                    15, getBbWidth() / 2, getBbHeight() / 2, getBbWidth() / 2, 0.05);
        }
    }

    /** Follow the owner. */
    private class FollowOwnerGoal extends Goal {
        @Override
        public boolean canUse() {
            if (ownerUUID == null) return false;
            Player owner = level().getPlayerByUUID(ownerUUID);
            return owner != null && distanceToSqr(owner) > 25; // > 5 blocks
        }

        @Override
        public void start() {
            Player owner = level().getPlayerByUUID(ownerUUID);
            if (owner != null) getNavigation().moveTo(owner, 1.2);
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = level().getPlayerByUUID(ownerUUID);
            return owner != null && distanceToSqr(owner) > 9 && !getNavigation().isDone();
        }

        @Override
        public void tick() {
            Player owner = level().getPlayerByUUID(ownerUUID);
            if (owner != null) getNavigation().moveTo(owner, 1.2);
        }
    }

    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override public boolean canMate(Animal other) { return false; }
    @Override public boolean canBeLeashed() { return false; }
    @Override public boolean shouldDropExperience() { return false; }
    @Override public boolean isPersistenceRequired() { return true; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) { }

    @Nullable
    @Override
    public Axolotl getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpectralTicksRemaining", entityData.get(DATA_TICKS_REMAINING));
        tag.putInt("SpectralVariant", entityData.get(DATA_VARIANT));
        if (ownerUUID != null) tag.putUUID("SpectralOwner", ownerUUID);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SpectralTicksRemaining")) {
            entityData.set(DATA_TICKS_REMAINING, tag.getInt("SpectralTicksRemaining"));
        }
        if (tag.contains("SpectralVariant")) {
            entityData.set(DATA_VARIANT, tag.getInt("SpectralVariant"));
        }
        if (tag.hasUUID("SpectralOwner")) {
            ownerUUID = tag.getUUID("SpectralOwner");
        }
    }
}
