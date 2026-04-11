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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class LunarJellyfishEntity extends WaterAnimal implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final int GLOW_CYCLE_LENGTH = 80;

    private static final EntityDataAccessor<Integer> DATA_GLOW_TICK =
            SynchedEntityData.defineId(LunarJellyfishEntity.class, EntityDataSerializers.INT);

    public float xBodyRot;
    public float xBodyRotO;
    public float zBodyRot;
    public float zBodyRotO;
    public float tentacleMovement;
    public float oldTentacleMovement;
    public float tentacleAngle;
    public float oldTentacleAngle;
    private float speed;
    private float tentacleSpeed;
    private float tx;
    private float ty;
    private float tz;
    private boolean hasRider;
    private Vec3 pinnedPos;
    private int riderGraceTicks;
    private static final int RIDER_GRACE_TICKS = 10;
    private BlockPos lastLitPos;

    public LunarJellyfishEntity(EntityType<? extends WaterAnimal> type, Level level) {
        super(type, level);
        this.random.setSeed((long) this.getId());
        this.tentacleSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.15F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isRemoved();
    }

    private void updateDynamicLight() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        BlockPos pos = blockPosition();
        if (pos.equals(lastLitPos)) return;
        GlowingWaterBlock.illuminate(serverLevel, pos);
        lastLitPos = pos;
    }

    @Override
    public void push(double x, double y, double z) {
        if (!hasRider) super.push(x, y, z);
    }

    private void updateRiderState() {
        AABB aabb = this.getBoundingBox();
        AABB riderZone = new AABB(
                aabb.minX - 0.15, aabb.maxY - 0.2,
                aabb.minZ - 0.15, aabb.maxX + 0.15,
                aabb.maxY + 1.1, aabb.maxZ + 0.15
        );
        List<LivingEntity> above = this.level().getEntitiesOfClass(
                LivingEntity.class, riderZone,
                e -> e != this && e.isAlive() && !e.isSpectator()
        );
        boolean detected = !above.isEmpty();
        if (detected) {
            if (!this.hasRider) {
                this.pinnedPos = this.position();
            }
            this.hasRider = true;
            this.riderGraceTicks = RIDER_GRACE_TICKS;
        } else if (this.riderGraceTicks > 0) {
            this.riderGraceTicks--;
            this.hasRider = true;
        } else {
            this.hasRider = false;
            this.pinnedPos = null;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GLOW_TICK, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LunarJellyfishRandomMovementGoal(this));
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!level().isClientSide()) {
            int glowTick = entityData.get(DATA_GLOW_TICK) + 1;
            if (glowTick >= GLOW_CYCLE_LENGTH) glowTick = 0;
            entityData.set(DATA_GLOW_TICK, glowTick);
            updateDynamicLight();
        }

        updateRiderState();

        this.xBodyRotO = this.xBodyRot;
        this.zBodyRotO = this.zBodyRot;

        this.oldTentacleMovement = this.tentacleMovement;
        this.oldTentacleAngle = this.tentacleAngle;
        this.tentacleMovement += this.tentacleSpeed;

        if ((double) this.tentacleMovement > Math.PI * 2) {
            if (this.level().isClientSide) {
                this.tentacleMovement = (float) (Math.PI * 2);
            } else {
                this.tentacleMovement -= (float) (Math.PI * 2);
                if (this.random.nextInt(10) == 0) {
                    this.tentacleSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.15F;
                }
                this.level().broadcastEntityEvent(this, (byte) 19);
            }
        }

        if (this.isInWaterOrBubble()) {
            if (this.tentacleMovement < (float) Math.PI) {
                float f = this.tentacleMovement / (float) Math.PI;
                this.tentacleAngle = Mth.sin(f * f * (float) Math.PI) * (float) Math.PI * 0.25F;
                if ((double) f > 0.75) {
                    this.speed = 0.7F;
                } else {
                    this.speed *= 0.8F;
                }
            } else {
                this.tentacleAngle = 0.0F;
                this.speed *= 0.85F;
            }

            if (!this.level().isClientSide) {
                if (this.hasRider) {
                    this.setDeltaMovement(0.0, 0.0, 0.0);
                    this.tx = 0.0F;
                    this.ty = 0.0F;
                    this.tz = 0.0F;
                    if (this.pinnedPos != null) {
                        this.setPos(this.pinnedPos.x, this.pinnedPos.y, this.pinnedPos.z);
                    }
                } else {
                    this.setDeltaMovement(
                            (double) (this.tx * this.speed),
                            (double) (this.ty * this.speed),
                            (double) (this.tz * this.speed));
                }
            }

            if (!this.hasRider) {
                Vec3 vel = this.getDeltaMovement();
                double hDist = vel.horizontalDistance();
                this.yBodyRot += (-((float) Mth.atan2(vel.x, vel.z)) * (180F / (float) Math.PI) - this.yBodyRot) * 0.05F;
                this.setYRot(this.yBodyRot);
                this.zBodyRot = 0.0F;
                float targetPitch = -((float) Mth.atan2(hDist, vel.y)) * (180F / (float) Math.PI);
                targetPitch = Mth.clamp(targetPitch, 45.0F, 135.0F);
                this.xBodyRot += (targetPitch - this.xBodyRot) * 0.03F;
            }
        } else {
            this.tentacleAngle = Mth.abs(Mth.sin(this.tentacleMovement)) * (float) Math.PI * 0.25F;
            this.xBodyRot += (90.0F - this.xBodyRot) * 0.05F;
            this.zBodyRot = 0.0F;
            if (!this.level().isClientSide) {
                double dy = this.getDeltaMovement().y;
                if (this.hasEffect(MobEffects.LEVITATION)) {
                    dy = 0.05 * (double) (this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1);
                } else {
                    dy -= this.getGravity();
                }
                this.setDeltaMovement(0.0, dy * 0.98, 0.0);
            }
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 19) {
            this.tentacleMovement = 0.0F;
        } else {
            super.handleEntityEvent(id);
        }
    }

    public void setMovementVector(float x, float y, float z) {
        this.tx = x;
        this.ty = y;
        this.tz = z;
    }

    public boolean hasMovementVector() {
        return this.tx != 0.0F || this.ty != 0.0F || this.tz != 0.0F;
    }

    public float getGlowIntensity(float partialTick) {
        float phase = (entityData.get(DATA_GLOW_TICK) + partialTick) / GLOW_CYCLE_LENGTH;
        float raw = (Mth.sin(phase * Mth.TWO_PI) + 1.0f) * 0.5f;
        return Mth.clamp(raw, 0.1f, 1.0f);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getFluidState(pos).isEmpty() ? -1.0f : 0.0f;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT_SMALL;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH_SMALL;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GLOW_SQUID_AMBIENT;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected float getSoundVolume() {
        return 0.3F;
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
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            boolean moving = this.getDeltaMovement().lengthSqr() > 0.0008;
            String anim = moving ? "animation.lunar_jellyfish.swim" : "animation.lunar_jellyfish.idle";
            state.getController().setAnimation(RawAnimation.begin().thenLoop(anim));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    static class LunarJellyfishRandomMovementGoal extends Goal {
        private final LunarJellyfishEntity jellyfish;

        LunarJellyfishRandomMovementGoal(LunarJellyfishEntity jf) {
            this.jellyfish = jf;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            if (this.jellyfish.hasRider) {
                this.jellyfish.setMovementVector(0.0F, 0.0F, 0.0F);
                return;
            }
            int idle = this.jellyfish.getNoActionTime();
            if (idle > 100) {
                this.jellyfish.setMovementVector(0.0F, 0.0F, 0.0F);
            } else if (this.jellyfish.getRandom().nextInt(reducedTickDelay(60)) == 0
                    || !this.jellyfish.wasTouchingWater
                    || !this.jellyfish.hasMovementVector()) {
                float angle = this.jellyfish.getRandom().nextFloat() * (float) (Math.PI * 2);
                float xz = Mth.cos(angle) * 0.12F;
                float y = -0.05F + this.jellyfish.getRandom().nextFloat() * 0.12F;
                float zz = Mth.sin(angle) * 0.12F;
                this.jellyfish.setMovementVector(xz, y, zz);
            }
        }
    }
}
