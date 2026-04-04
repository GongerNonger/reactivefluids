package com.reactivefluids;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import java.lang.reflect.Field;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * A conjured spectral wolf — fights for the caster, translucent, temporary.
 * Based on D&D 5e Conjure Animals.
 */
public class SpectralWolfEntity extends Wolf {

    public static final int DURATION_TICKS = 2400; // 2 minutes
    public static final int VARIANT_COUNT = 9; // 9 wolf texture variants in 1.21.1

    private static final EntityDataAccessor<Integer> DATA_TICKS_REMAINING =
            SynchedEntityData.defineId(SpectralWolfEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(SpectralWolfEntity.class, EntityDataSerializers.INT);

    public SpectralWolfEntity(EntityType<? extends Wolf> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TICKS_REMAINING, DURATION_TICKS);
        builder.define(DATA_VARIANT, 0);
    }

    public int getTicksRemaining() {
        return entityData.get(DATA_TICKS_REMAINING);
    }

    public int getVariantIndex() {
        return entityData.get(DATA_VARIANT);
    }

    public void setVariantIndex(int variant) {
        entityData.set(DATA_VARIANT, variant % VARIANT_COUNT);
    }

    /** Set collar color — Wolf.setCollarColor and DATA_COLLAR_COLOR are private in 1.21.1. */
    @SuppressWarnings("unchecked")
    public void setSpectralCollarColor(DyeColor color) {
        try {
            Field f = Wolf.class.getDeclaredField("DATA_COLLAR_COLOR");
            f.setAccessible(true);
            EntityDataAccessor<Integer> accessor = (EntityDataAccessor<Integer>) f.get(null);
            this.entityData.set(accessor, color.getId());
        } catch (Exception e) {
            // Fallback: try obfuscated field names
            for (Field f : Wolf.class.getDeclaredFields()) {
                if (f.getType() == EntityDataAccessor.class && java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    try {
                        f.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        EntityDataAccessor<Integer> accessor = (EntityDataAccessor<Integer>) f.get(null);
                        // Test if this is the collar color accessor (try setting it)
                        int old = this.entityData.get(accessor);
                        if (old == DyeColor.RED.getId()) { // Default collar is red
                            this.entityData.set(accessor, color.getId());
                            return;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            int remaining = entityData.get(DATA_TICKS_REMAINING) - 1;
            entityData.set(DATA_TICKS_REMAINING, remaining);

            if (remaining <= 0) {
                spawnPoof();
                discard();
                return;
            }

            // Warning flicker in last 5 seconds
            if (remaining <= 100 && remaining % 20 == 0) {
                level().playSound(null, this, SoundEvents.WOLF_WHINE,
                        SoundSource.NEUTRAL, 0.4F, 1.5F);
            }
        }

        // Client particles
        if (level().isClientSide()) {
            if (random.nextInt(4) == 0) {
                double px = getX() + (random.nextDouble() - 0.5) * getBbWidth();
                double py = getY() + random.nextDouble() * getBbHeight();
                double pz = getZ() + (random.nextDouble() - 0.5) * getBbWidth();
                level().addParticle(ModParticles.SPECTRAL.get(), px, py, pz, 0, 0.02, 0);
            }
        }
    }

    private void spawnPoof() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    getX(), getY() + getBbHeight() / 2, getZ(),
                    15, getBbWidth() / 2, getBbHeight() / 2, getBbWidth() / 2, 0.05);
        }
    }

    // --- Overrides to make it a conjured creature ---

    @Override
    public boolean isFood(ItemStack stack) { return false; }

    @Override
    public boolean canMate(Animal other) { return false; }

    @Override
    public boolean canBeLeashed() { return false; }

    @Override
    public boolean shouldDropExperience() { return false; }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource source) {
        // No drops
    }

    @Override
    public boolean isPersistenceRequired() { return true; }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Nullable
    @Override
    public Wolf getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        // No interactions (no sitting, feeding, etc.)
        return InteractionResult.PASS;
    }

    // --- Save / Load ---

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SpectralTicksRemaining", entityData.get(DATA_TICKS_REMAINING));
        tag.putInt("SpectralVariant", entityData.get(DATA_VARIANT));
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
    }
}
