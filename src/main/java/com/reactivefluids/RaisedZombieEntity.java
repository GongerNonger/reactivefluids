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
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * A raised zombie minion — fights for the caster until destroyed.
 * Created by casting Raise Dead on stacked rotten flesh blocks.
 */
public class RaisedZombieEntity extends Zombie {

    public static final int DURATION_TICKS = 24000; // 20 minutes

    private static final EntityDataAccessor<Integer> DATA_TICKS_REMAINING =
            SynchedEntityData.defineId(RaisedZombieEntity.class, EntityDataSerializers.INT);

    private UUID ownerUUID;

    public RaisedZombieEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28);
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TICKS_REMAINING, DURATION_TICKS);
    }

    public int getTicksRemaining() {
        return entityData.get(DATA_TICKS_REMAINING);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1, true));
        this.goalSelector.addGoal(2, new FollowOwnerGoal());
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                target -> !(target instanceof Creeper) && !(target instanceof Ghast)));
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
                level().playSound(null, this, SoundEvents.ZOMBIE_AMBIENT,
                        SoundSource.NEUTRAL, 0.4F, 0.8F);
            }
        }
    }

    private void spawnPoof() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    getX(), getY() + getBbHeight() / 2, getZ(),
                    20, getBbWidth() / 2, getBbHeight() / 2, getBbWidth() / 2, 0.05);
        }
    }

    /** Follow the owner when not attacking. */
    private class FollowOwnerGoal extends Goal {
        @Override
        public boolean canUse() {
            if (ownerUUID == null || getTarget() != null) return false;
            Player owner = level().getPlayerByUUID(ownerUUID);
            return owner != null && distanceToSqr(owner) > 100;
        }

        @Override
        public void start() {
            Player owner = level().getPlayerByUUID(ownerUUID);
            if (owner != null) getNavigation().moveTo(owner, 1.2);
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = level().getPlayerByUUID(ownerUUID);
            return owner != null && distanceToSqr(owner) > 16 && !getNavigation().isDone();
        }

        @Override
        public void tick() {
            Player owner = level().getPlayerByUUID(ownerUUID);
            if (owner != null) getNavigation().moveTo(owner, 1.2);
        }
    }

    @Override public boolean shouldDropExperience() { return false; }
    @Override public boolean isPersistenceRequired() { return true; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected void dropAllDeathLoot(ServerLevel level, DamageSource source) { }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false; // Raised undead don't burn in sunlight
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("RaisedTicksRemaining", entityData.get(DATA_TICKS_REMAINING));
        if (ownerUUID != null) tag.putUUID("RaisedOwner", ownerUUID);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("RaisedTicksRemaining")) {
            entityData.set(DATA_TICKS_REMAINING, tag.getInt("RaisedTicksRemaining"));
        }
        if (tag.hasUUID("RaisedOwner")) {
            ownerUUID = tag.getUUID("RaisedOwner");
        }
    }
}
