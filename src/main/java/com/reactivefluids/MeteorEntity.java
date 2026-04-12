package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.damagesource.DamageSource;

/**
 * A visual falling meteor entity that streaks down from the sky,
 * spawns trail particles, and explodes on ground impact.
 */
public class MeteorEntity extends Entity {

    private static final int MAX_LIFETIME = 200;
    private static final int DESTROY_RADIUS = 4;
    private static final float ENTITY_DAMAGE = 20.0F;
    private static final double DAMAGE_RADIUS = 5.0;

    public MeteorEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synched data needed — it just falls
    }

    @Override
    public void tick() {
        super.tick();

        // Move the entity
        this.setPos(getX() + getDeltaMovement().x,
                     getY() + getDeltaMovement().y,
                     getZ() + getDeltaMovement().z);

        if (!level().isClientSide()) {
            // Server side: check for ground impact or timeout
            BlockPos below = blockPosition();
            BlockState stateAtPos = level().getBlockState(below);

            if (stateAtPos.isSolid() || onGround()) {
                explode((ServerLevel) level());
                discard();
                return;
            }

            // Also check one block below for near-ground
            BlockPos oneBelow = below.below();
            if (level().getBlockState(oneBelow).isSolid() && getDeltaMovement().y < 0) {
                explode((ServerLevel) level());
                discard();
                return;
            }

            // Safety timeout
            if (tickCount >= MAX_LIFETIME) {
                discard();
                return;
            }
        } else {
            // Client side: spawn trail particles
            double x = getX();
            double y = getY();
            double z = getZ();

            // 3-5 FLAME particles with slight spread
            for (int i = 0; i < 3 + random.nextInt(3); i++) {
                level().addParticle(ParticleTypes.FLAME,
                        x + (random.nextDouble() - 0.5) * 0.6,
                        y + (random.nextDouble() - 0.5) * 0.6,
                        z + (random.nextDouble() - 0.5) * 0.6,
                        0, 0.05, 0);
            }

            // 1-2 LARGE_SMOKE particles
            for (int i = 0; i < 1 + random.nextInt(2); i++) {
                level().addParticle(ParticleTypes.LARGE_SMOKE,
                        x + (random.nextDouble() - 0.5) * 0.4,
                        y + (random.nextDouble() - 0.5) * 0.4,
                        z + (random.nextDouble() - 0.5) * 0.4,
                        0, 0.02, 0);
            }

            // Occasional LAVA drip
            if (random.nextInt(3) == 0) {
                level().addParticle(ParticleTypes.DRIPPING_LAVA,
                        x + (random.nextDouble() - 0.5) * 0.3,
                        y + 0.2,
                        z + (random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0);
            }
        }
    }

    private void explode(ServerLevel level) {
        BlockPos impact = blockPosition();
        double ix = impact.getX() + 0.5;
        double iy = impact.getY() + 1.0;
        double iz = impact.getZ() + 0.5;

        // --- Destroy blocks in a sphere ---
        for (int dx = -DESTROY_RADIUS; dx <= DESTROY_RADIUS; dx++) {
            for (int dy = -DESTROY_RADIUS; dy <= DESTROY_RADIUS; dy++) {
                for (int dz = -DESTROY_RADIUS; dz <= DESTROY_RADIUS; dz++) {
                    if (dx * dx + dy * dy + dz * dz > DESTROY_RADIUS * DESTROY_RADIUS) continue;

                    BlockPos pos = impact.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);

                    if (state.isAir()) continue;
                    float destroySpeed = state.getDestroySpeed(level, pos);
                    if (destroySpeed < 0) continue;   // bedrock, barriers
                    if (destroySpeed > 50.0F) continue; // obsidian

                    // Edge blocks: chance to set fire above
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > DESTROY_RADIUS - 1 && level.random.nextFloat() < 0.3F) {
                        BlockPos above = pos.above();
                        if (level.getBlockState(above).isAir()) {
                            level.setBlock(above, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }

                    // Destroy the block (no drops)
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        // Place fire on crater floor
        for (int dx = -DESTROY_RADIUS; dx <= DESTROY_RADIUS; dx++) {
            for (int dz = -DESTROY_RADIUS; dz <= DESTROY_RADIUS; dz++) {
                if (dx * dx + dz * dz > DESTROY_RADIUS * DESTROY_RADIUS) continue;
                if (level.random.nextFloat() > 0.3F) continue;

                BlockPos firePos = impact.offset(dx, 0, dz);
                while (firePos.getY() > level.getMinBuildHeight() && level.getBlockState(firePos).isAir()) {
                    firePos = firePos.below();
                }
                BlockPos aboveFloor = firePos.above();
                if (level.getBlockState(aboveFloor).isAir()) {
                    level.setBlock(aboveFloor, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        // --- Damage entities ---
        AABB damageBox = new AABB(
                impact.getX() - DAMAGE_RADIUS, impact.getY() - DAMAGE_RADIUS, impact.getZ() - DAMAGE_RADIUS,
                impact.getX() + DAMAGE_RADIUS + 1, impact.getY() + DAMAGE_RADIUS + 1, impact.getZ() + DAMAGE_RADIUS + 1);

        for (Entity entity : level.getEntities((Entity) null, damageBox, e -> e.isAlive())) {
            if (entity instanceof LivingEntity living) {
                living.hurt(level.damageSources().magic(), ENTITY_DAMAGE);
                living.setRemainingFireTicks(100); // 5 seconds of fire
                // Launch upward
                living.setDeltaMovement(living.getDeltaMovement().add(0, 0.8, 0));
                living.hurtMarked = true;
            }
        }

        // --- Impact particles ---
        level.sendParticles(ParticleTypes.LAVA,
                ix, iy, iz, 40, 1.5, 0.5, 1.5, 0.5);
        level.sendParticles(ParticleTypes.FLAME,
                ix, iy, iz, 60, 2.0, 1.0, 2.0, 0.3);
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                ix, iy, iz, 30, 2.0, 1.0, 2.0, 0.1);
        level.sendParticles(ParticleTypes.EXPLOSION,
                ix, iy, iz, 5, 1.0, 0.5, 1.0, 0.0);

        // --- Sound ---
        level.playSound(null, impact, SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 2.0F, 0.7F);
    }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0 * 256.0;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // Short-lived entity, minimal save data
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // Short-lived entity, minimal save data
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

}
