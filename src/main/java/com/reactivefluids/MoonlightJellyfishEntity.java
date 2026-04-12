package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Moonlight Jellyfish — ambient bioluminescent water creature.
 * Movement based on vanilla Squid: pulse-driven propulsion with
 * bell contraction/expansion animation. Periodically glows.
 */
public class MoonlightJellyfishEntity extends WaterAnimal implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final int GLOW_CYCLE_LENGTH = 80;

    // 0 = blue (default, 90%), 1 = green (rare, 10%)
    public static final int VARIANT_BLUE = 0;
    public static final int VARIANT_GREEN = 1;

    private static final EntityDataAccessor<Integer> DATA_GLOW_TICK =
            SynchedEntityData.defineId(MoonlightJellyfishEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(MoonlightJellyfishEntity.class, EntityDataSerializers.INT);

    // Squid-style movement and body rotation fields
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
    private float rotateSpeed;
    private float tx;
    private float ty;
    private float tz;
    private BlockPos lastLitPos;

    public MoonlightJellyfishEntity(EntityType<? extends WaterAnimal> type, Level level) {
        super(type, level);
        this.random.setSeed((long) this.getId());
        this.tentacleSpeed = 1.0F / (this.random.nextFloat() + 1.0F) * 0.15F; // slower than squid
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GLOW_TICK, 0);
        builder.define(DATA_VARIANT, VARIANT_BLUE);
    }

    public int getVariant() {
        return entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        entityData.set(DATA_VARIANT, variant);
    }

    public boolean isGreenVariant() {
        return getVariant() == VARIANT_GREEN;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                         EntitySpawnReason spawnType, @Nullable SpawnGroupData groupData) {
        // 10% chance to spawn as rare green variant
        if (this.random.nextFloat() < 0.1F) {
            this.setVariant(VARIANT_GREEN);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, groupData);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new JellyfishRandomMovementGoal(this));
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04; // lighter than squid — more floaty
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Glow cycle
        if (!level().isClientSide()) {
            int glowTick = entityData.get(DATA_GLOW_TICK) + 1;
            if (glowTick >= GLOW_CYCLE_LENGTH) glowTick = 0;
            entityData.set(DATA_GLOW_TICK, glowTick);
            updateDynamicLight();
        }

        // Body rotation tracking (for renderer)
        this.xBodyRotO = this.xBodyRot;
        this.zBodyRotO = this.zBodyRot;

        // Squid-style pulsing movement
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
                this.setDeltaMovement(
                        (double) (this.tx * this.speed),
                        (double) (this.ty * this.speed),
                        (double) (this.tz * this.speed));
            }

            Vec3 vel = this.getDeltaMovement();
            double hDist = vel.horizontalDistance();
            // Yaw: slowly track movement direction
            this.yBodyRot += (-((float) Mth.atan2(vel.x, vel.z)) * (180F / (float) Math.PI) - this.yBodyRot) * 0.05F;
            this.setYRot(this.yBodyRot);
            // No Z roll — jellyfish don't spin like squid
            this.zBodyRot = 0.0F;
            // X pitch: very gentle tilt toward movement, heavily damped to prevent flipping
            float targetPitch = -((float) Mth.atan2(hDist, vel.y)) * (180F / (float) Math.PI);
            // Clamp target so it never goes past 45 degrees from neutral (90 = upright)
            targetPitch = Mth.clamp(targetPitch, 45.0F, 135.0F);
            this.xBodyRot += (targetPitch - this.xBodyRot) * 0.03F; // very slow interpolation
        } else {
            // Out of water — fall upright, tentacles spread
            this.tentacleAngle = Mth.abs(Mth.sin(this.tentacleMovement)) * (float) Math.PI * 0.25F;
            // Slowly return to upright orientation (90 degrees)
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

    private void updateDynamicLight() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        BlockPos pos = blockPosition();
        if (pos.equals(lastLitPos)) return;
        GlowingWaterBlock.illuminate(serverLevel, pos);
        lastLitPos = pos;
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
        tag.putInt("Variant", entityData.get(DATA_VARIANT));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GlowTick")) {
            entityData.set(DATA_GLOW_TICK, tag.getInt("GlowTick"));
        }
        if (tag.contains("Variant")) {
            entityData.set(DATA_VARIANT, tag.getInt("Variant"));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToPlayer) {
        return false;
    }

    // =========================================================================
    // GeckoLib animation
    // =========================================================================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 5, state -> {
            state.getController().setAnimation(
                    RawAnimation.begin().thenLoop("animation.jellyfish.idle"));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // =========================================================================
    // Inner AI goal — squid-style random drifting, but slower/gentler
    // =========================================================================
    static class JellyfishRandomMovementGoal extends Goal {
        private final MoonlightJellyfishEntity jellyfish;

        JellyfishRandomMovementGoal(MoonlightJellyfishEntity jf) {
            this.jellyfish = jf;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            int idle = this.jellyfish.getNoActionTime();
            if (idle > 100) {
                this.jellyfish.setMovementVector(0.0F, 0.0F, 0.0F);
            } else if (this.jellyfish.getRandom().nextInt(reducedTickDelay(60)) == 0
                    || !this.jellyfish.wasTouchingWater
                    || !this.jellyfish.hasMovementVector()) {
                float angle = this.jellyfish.getRandom().nextFloat() * (float) (Math.PI * 2);
                float xz = Mth.cos(angle) * 0.12F;  // gentler than squid's 0.2
                float y = -0.05F + this.jellyfish.getRandom().nextFloat() * 0.12F;
                float zz = Mth.sin(angle) * 0.12F;
                this.jellyfish.setMovementVector(xz, y, zz);
            }
        }
    }
}
