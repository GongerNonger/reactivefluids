package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A skeleton that has been crystallized by crystal solution.
 * Slower but tougher, cannot use bow, attacks with a crystal laser beam.
 */
public class CrystallizedSkeleton extends Skeleton {

    /** Synched entity data: ID of the entity being targeted by the laser (0 = none) */
    private static final EntityDataAccessor<Integer> DATA_LASER_TARGET =
            SynchedEntityData.defineId(CrystallizedSkeleton.class, EntityDataSerializers.INT);

    /** How long the laser charges before firing (ticks) */
    public static final int LASER_CHARGE_TICKS = 40; // 2 seconds

    /** Cached client-side attack target */
    private LivingEntity cachedLaserTarget;
    private int clientLaserTime;

    public CrystallizedSkeleton(EntityType<? extends CrystallizedSkeleton> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)     // tougher (skeleton = 20)
                .add(Attributes.MOVEMENT_SPEED, 0.18)  // slower (skeleton = 0.25)
                .add(Attributes.ARMOR, 8.0)            // crystal armor
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_LASER_TARGET, 0);
    }

    @Override
    protected void registerGoals() {
        // Don't call super — we replace all goals to remove bow behavior entirely
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RestrictSunGoal(this));
        this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Wolf.class, 6.0F, 1.0, 1.2));
        this.goalSelector.addGoal(4, new CrystalLaserGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    /** Prevent the skeleton from switching to bow/melee based on held weapon. */
    @Override
    public void reassessWeaponGoal() {
        // No-op — we always use the crystal laser
    }

    // ===== Laser target synching (Guardian-style) =====

    public void setLaserTarget(int entityId) {
        this.entityData.set(DATA_LASER_TARGET, entityId);
    }

    public boolean hasLaserTarget() {
        return this.entityData.get(DATA_LASER_TARGET) != 0;
    }

    public LivingEntity getLaserTarget() {
        if (!this.level().isClientSide()) {
            return this.getTarget();
        }
        // Client side — cache the target lookup
        int id = this.entityData.get(DATA_LASER_TARGET);
        if (id == 0) {
            this.cachedLaserTarget = null;
            return null;
        }
        if (this.cachedLaserTarget == null || this.cachedLaserTarget.getId() != id) {
            var entity = this.level().getEntity(id);
            this.cachedLaserTarget = entity instanceof LivingEntity le ? le : null;
        }
        return this.cachedLaserTarget;
    }

    public float getLaserScale(float partialTick) {
        return (this.clientLaserTime + partialTick) / LASER_CHARGE_TICKS;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_LASER_TARGET.equals(key)) {
            this.clientLaserTime = 0;
            this.cachedLaserTarget = null;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && hasLaserTarget()) {
            this.clientLaserTime++;
        }
    }

    /**
     * Fire the laser — deal damage and spawn particles along the beam.
     */
    public void fireLaser(LivingEntity target) {
        if (this.level() instanceof ServerLevel serverLevel) {
            // Deal damage
            DamageSource source = this.damageSources().indirectMagic(this, this);
            target.hurt(source, 6.0f);

            // Spawn particles along the beam line
            Vec3 start = this.getEyePosition();
            Vec3 end = target.position().add(0, target.getBbHeight() / 2.0, 0);
            Vec3 dir = end.subtract(start);
            double length = dir.length();
            Vec3 step = dir.normalize().scale(0.5);

            Vec3 pos = start;
            for (double d = 0; d < length; d += 0.5) {
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.END_ROD,
                        pos.x, pos.y, pos.z,
                        1, 0.05, 0.05, 0.05, 0.0);
                pos = pos.add(step);
            }

            // Impact burst
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLASH,
                    end.x, end.y, end.z,
                    1, 0, 0, 0, 0);

            this.playSound(SoundEvents.GUARDIAN_ATTACK, 1.0F, 1.5F);
        }
    }

    @Override
    protected boolean isSunBurnTick() {
        // Crystallized skeletons don't burn in sunlight — the crystal protects them
        return false;
    }
}
