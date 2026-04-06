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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Fletcher's Fireball — a directional magical projectile fired from a spell scroll.
 * Travels in the player's look direction and explodes on impact.
 * Color is set at spawn time and synced to the client for rendering.
 */
public class FireballEntity extends Entity {

    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(FireballEntity.class, EntityDataSerializers.INT);

    /** Default fire orange when no dye is held in offhand. */
    public static final int DEFAULT_COLOR = 0xFF6600;

    private static final int MAX_LIFETIME = 80;   // 4 seconds at 20 TPS
    private static final int DESTROY_RADIUS = 2;
    private static final float DAMAGE = 12.0F;

    @Nullable private UUID ownerUUID;

    public FireballEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(COLOR, DEFAULT_COLOR);
    }

    public void setColor(int rgb) {
        entityData.set(COLOR, rgb);
    }

    public int getColor() {
        return entityData.get(COLOR);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 start = position();
        Vec3 delta = getDeltaMovement();
        Vec3 end = start.add(delta);

        if (!level().isClientSide()) {
            // Raycast from current position to next position for block hits
            BlockHitResult blockHit = level().clip(new ClipContext(
                    start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

            double closestDistSq = blockHit.getType() != HitResult.Type.MISS
                    ? start.distanceToSqr(blockHit.getLocation()) : Double.MAX_VALUE;

            // Entity collision — sweep the path
            LivingEntity hitEntity = null;
            Vec3 entityImpactPos = null;
            AABB searchBox = new AABB(start, end).inflate(0.3);
            for (Entity e : level().getEntities(this, searchBox,
                    en -> en instanceof LivingEntity && en.isAlive()
                          && (ownerUUID == null || !en.getUUID().equals(ownerUUID)))) {
                Optional<Vec3> hit = e.getBoundingBox().inflate(0.2).clip(start, end);
                if (hit.isPresent()) {
                    double d = start.distanceToSqr(hit.get());
                    if (d < closestDistSq) {
                        closestDistSq = d;
                        hitEntity = (LivingEntity) e;
                        entityImpactPos = hit.get();
                    }
                }
            }

            if (hitEntity != null) {
                hitEntity.hurt(level().damageSources().magic(), DAMAGE);
                hitEntity.setRemainingFireTicks(60);
                explode((ServerLevel) level(), BlockPos.containing(entityImpactPos));
                discard();
                return;
            }

            if (blockHit.getType() != HitResult.Type.MISS) {
                explode((ServerLevel) level(), blockHit.getBlockPos());
                discard();
                return;
            }

            if (tickCount >= MAX_LIFETIME) {
                discard();
                return;
            }
        } else {
            // Client-side: flame trail particles
            double x = getX(), y = getY(), z = getZ();
            for (int i = 0; i < 3; i++) {
                level().addParticle(ParticleTypes.FLAME,
                        x + (random.nextDouble() - 0.5) * 0.3,
                        y + (random.nextDouble() - 0.5) * 0.3,
                        z + (random.nextDouble() - 0.5) * 0.3,
                        0, 0.02, 0);
            }
            level().addParticle(ParticleTypes.SMALL_FLAME,
                    x + (random.nextDouble() - 0.5) * 0.15,
                    y + (random.nextDouble() - 0.5) * 0.15,
                    z + (random.nextDouble() - 0.5) * 0.15,
                    0, 0.01, 0);
        }

        setPos(end.x, end.y, end.z);
    }

    private void explode(ServerLevel level, BlockPos impact) {
        double ix = impact.getX() + 0.5;
        double iy = impact.getY() + 0.5;
        double iz = impact.getZ() + 0.5;

        // Destroy a small sphere of blocks
        for (int dx = -DESTROY_RADIUS; dx <= DESTROY_RADIUS; dx++) {
            for (int dy = -DESTROY_RADIUS; dy <= DESTROY_RADIUS; dy++) {
                for (int dz = -DESTROY_RADIUS; dz <= DESTROY_RADIUS; dz++) {
                    if (dx * dx + dy * dy + dz * dz > DESTROY_RADIUS * DESTROY_RADIUS) continue;
                    BlockPos pos = impact.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    float hardness = state.getDestroySpeed(level, pos);
                    if (hardness < 0 || hardness > 50.0F) continue;
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        // Scatter fire across the crater floor
        for (int dx = -DESTROY_RADIUS; dx <= DESTROY_RADIUS; dx++) {
            for (int dz = -DESTROY_RADIUS; dz <= DESTROY_RADIUS; dz++) {
                if (dx * dx + dz * dz > DESTROY_RADIUS * DESTROY_RADIUS) continue;
                if (level.random.nextFloat() > 0.4F) continue;
                BlockPos firePos = impact.offset(dx, 0, dz);
                BlockPos belowFire = firePos.below();
                if (level.getBlockState(firePos).isAir()
                        && level.getBlockState(belowFire).isFaceSturdy(level, belowFire, Direction.UP)) {
                    level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        // Impact particles
        level.sendParticles(ParticleTypes.LAVA,       ix, iy, iz, 15, 0.6, 0.3, 0.6, 0.3);
        level.sendParticles(ParticleTypes.FLAME,      ix, iy, iz, 25, 0.8, 0.4, 0.8, 0.2);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, ix, iy, iz,  8, 0.6, 0.3, 0.6, 0.05);
        level.sendParticles(ParticleTypes.EXPLOSION,  ix, iy, iz,  2, 0.2, 0.1, 0.2, 0.0);

        level.playSound(null, impact, SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 1.2F, 1.1F);
    }

    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0 * 256.0;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(COLOR, tag.getInt("FireballColor"));
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("FireballColor", entityData.get(COLOR));
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
    }
}
