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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A conjured spectral fox — pounces on enemies for the caster, translucent, temporary.
 */
public class SpectralFoxEntity extends Fox {

    public static final int DURATION_TICKS = 2400; // 2 minutes
    public static final int VARIANT_COUNT = 2; // red fox, snow fox

    private static final EntityDataAccessor<Integer> DATA_TICKS_REMAINING =
            SynchedEntityData.defineId(SpectralFoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(SpectralFoxEntity.class, EntityDataSerializers.INT);

    private UUID ownerUUID;

    public SpectralFoxEntity(EntityType<? extends Fox> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Fox.createAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.38);
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

    public int getTicksRemaining() {
        return entityData.get(DATA_TICKS_REMAINING);
    }

    public int getVariantIndex() { return entityData.get(DATA_VARIANT); }
    public void setVariantIndex(int v) { entityData.set(DATA_VARIANT, v % VARIANT_COUNT); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.5F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal());
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // Target hostile mobs but never creepers or ghasts (same logic as tamed wolves)
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                target -> !(target instanceof Creeper) && !(target instanceof Ghast)));
    }

    /** Equip a random weapon — foxes carry items in their mouth (mainhand). */
    public void equipRandomWeapon() {
        ItemStack weapon;
        int roll = random.nextInt(100);
        if (roll < 3) {
            weapon = new ItemStack(Items.MACE);             // 3% — rare
        } else if (roll < 15) {
            weapon = new ItemStack(Items.DIAMOND_SWORD);    // 12%
        } else if (roll < 35) {
            weapon = new ItemStack(Items.IRON_AXE);         // 20%
        } else if (roll < 60) {
            weapon = new ItemStack(Items.IRON_SWORD);       // 25%
        } else {
            weapon = new ItemStack(Items.STONE_SWORD);      // 40%
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F); // Spectral — never drop
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

            if (remaining <= 100 && remaining % 20 == 0) {
                level().playSound(null, this, SoundEvents.FOX_AGGRO,
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

    /** Follow the owner when not attacking. */
    private class FollowOwnerGoal extends Goal {
        @Override
        public boolean canUse() {
            if (ownerUUID == null || getTarget() != null) return false;
            Player owner = level().getPlayerByUUID(ownerUUID);
            return owner != null && distanceToSqr(owner) > 100; // > 10 blocks
        }

        @Override
        public void start() {
            Player owner = level().getPlayerByUUID(ownerUUID);
            if (owner != null) {
                getNavigation().moveTo(owner, 1.3);
            }
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = level().getPlayerByUUID(ownerUUID);
            return owner != null && distanceToSqr(owner) > 16 && !getNavigation().isDone();
        }

        @Override
        public void tick() {
            Player owner = level().getPlayerByUUID(ownerUUID);
            if (owner != null) {
                getNavigation().moveTo(owner, 1.3);
            }
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
    public Fox getBreedOffspring(ServerLevel level, AgeableMob other) { return null; }

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
