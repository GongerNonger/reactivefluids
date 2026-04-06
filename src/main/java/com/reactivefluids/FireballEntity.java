package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

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
    private static final int DESTROY_RADIUS = 3;
    private static final int FIRE_RADIUS = 6;       // fire spreads further than block destruction
    private static final double SPLASH_RADIUS = 4.5; // entity damage radius
    private static final float DAMAGE = 12.0F;
    private static final float SPLASH_DAMAGE = 8.0F; // falloff at edge of splash radius

    @Nullable private UUID ownerUUID;
    @Nullable private BlockPos lightBlockPos;

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
            // Move a light block to follow the fireball so it illuminates surroundings
            if (lightBlockPos != null && level().getBlockState(lightBlockPos).is(Blocks.LIGHT)) {
                level().removeBlock(lightBlockPos, false);
            }
            BlockPos curPos = blockPosition();
            if (level().getBlockState(curPos).isAir()) {
                level().setBlock(curPos,
                        Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15),
                        Block.UPDATE_ALL);
                lightBlockPos = curPos;
            }

            // Raycast from current position to next position for block hits
            BlockHitResult blockHit = level().clip(new ClipContext(
                    start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));

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
                BlockPos hitPos = blockHit.getBlockPos();
                if (level().getFluidState(hitPos).is(FluidTags.WATER)) {
                    explodeInWater((ServerLevel) level(), hitPos);
                } else {
                    explode((ServerLevel) level(), hitPos);
                }
                discard();
                return;
            }

            if (tickCount >= MAX_LIFETIME) {
                discard();
                return;
            }
        } else {
            // Client-side: colored dust trail + flame core
            double x = getX(), y = getY(), z = getZ();

            int c = getColor();
            float r = ((c >> 16) & 0xFF) / 255.0F;
            float g = ((c >> 8)  & 0xFF) / 255.0F;
            float b = ( c        & 0xFF) / 255.0F;
            DustParticleOptions dust = new DustParticleOptions(new Vector3f(r, g, b), 1.4F);

            // Colored smoke trail matching the dye
            for (int i = 0; i < 5; i++) {
                level().addParticle(dust,
                        x + (random.nextDouble() - 0.5) * 0.35,
                        y + (random.nextDouble() - 0.5) * 0.35,
                        z + (random.nextDouble() - 0.5) * 0.35,
                        0, 0.02, 0);
            }
            // White-hot flame core on top
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.FLAME,
                        x + (random.nextDouble() - 0.5) * 0.15,
                        y + (random.nextDouble() - 0.5) * 0.15,
                        z + (random.nextDouble() - 0.5) * 0.15,
                        0, 0.01, 0);
            }
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

        // 3D fire spread — covers the full FIRE_RADIUS sphere so fire ignites
        // leaves, logs, and other flammable blocks above and around the blast.
        // Probability is denser near the core and tapers toward the edge.
        for (int dx = -FIRE_RADIUS; dx <= FIRE_RADIUS; dx++) {
            for (int dy = -FIRE_RADIUS; dy <= FIRE_RADIUS; dy++) {
                for (int dz = -FIRE_RADIUS; dz <= FIRE_RADIUS; dz++) {
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > FIRE_RADIUS * FIRE_RADIUS) continue;
                    BlockPos firePos = impact.offset(dx, dy, dz);
                    BlockState fireState = level.getBlockState(firePos);
                    // Only place fire in air or replaceable blocks (grass, flowers, etc.)
                    if (!fireState.isAir() && !fireState.canBeReplaced()) continue;
                    // Chance scales from ~95% at center to ~60% at edge
                    float chance = (float) (0.95 - 0.35 * Math.sqrt(distSq) / FIRE_RADIUS);
                    if (level.random.nextFloat() > chance) continue;
                    // Only ignite if at least one adjacent block is actually flammable
                    boolean hasFlammableNeighbor = false;
                    for (Direction dir : Direction.values()) {
                        BlockPos neighbor = firePos.relative(dir);
                        if (level.getBlockState(neighbor).getFlammability(level, neighbor, dir.getOpposite()) > 0) {
                            hasFlammableNeighbor = true;
                            break;
                        }
                    }
                    if (hasFlammableNeighbor) {
                        level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        // Splash damage — entities within SPLASH_RADIUS take falloff damage and catch fire
        AABB splashBox = new AABB(
                ix - SPLASH_RADIUS, iy - SPLASH_RADIUS, iz - SPLASH_RADIUS,
                ix + SPLASH_RADIUS, iy + SPLASH_RADIUS, iz + SPLASH_RADIUS);
        for (Entity entity : level.getEntities((Entity) null, splashBox, e -> e instanceof LivingEntity && e.isAlive())) {
            double dist = entity.position().distanceTo(new Vec3(ix, iy, iz));
            if (dist > SPLASH_RADIUS) continue;
            float falloff = 1.0F - (float) (dist / SPLASH_RADIUS);
            float splashDmg = SPLASH_DAMAGE * falloff;
            ((LivingEntity) entity).hurt(level.damageSources().magic(), splashDmg);
            ((LivingEntity) entity).setRemainingFireTicks(40);
        }

        // Impact particles
        level.sendParticles(ParticleTypes.LAVA,       ix, iy, iz, 15, 0.6, 0.3, 0.6, 0.3);
        level.sendParticles(ParticleTypes.FLAME,      ix, iy, iz, 25, 0.8, 0.4, 0.8, 0.2);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, ix, iy, iz,  8, 0.6, 0.3, 0.6, 0.05);
        level.sendParticles(ParticleTypes.EXPLOSION,  ix, iy, iz,  2, 0.2, 0.1, 0.2, 0.0);

        level.playSound(null, impact, SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 1.2F, 1.1F);
    }

    private void explodeInWater(ServerLevel level, BlockPos impact) {
        double ix = impact.getX() + 0.5;
        double iy = impact.getY() + 0.5;
        double iz = impact.getZ() + 0.5;

        // Clear water blocks in a small sphere — leaves stone and other solids untouched
        int waterRadius = 3;
        for (int dx = -waterRadius; dx <= waterRadius; dx++) {
            for (int dy = -waterRadius; dy <= waterRadius; dy++) {
                for (int dz = -waterRadius; dz <= waterRadius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > waterRadius * waterRadius) continue;
                    BlockPos pos = impact.offset(dx, dy, dz);
                    if (level.getFluidState(pos).is(FluidTags.WATER)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        // Steam particles — dense cloud burst
        level.sendParticles(ParticleTypes.CLOUD,       ix, iy, iz, 60, 1.5, 0.8, 1.5, 0.12);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, ix, iy, iz, 20, 1.2, 0.6, 1.2, 0.06);
        level.sendParticles(ParticleTypes.SPLASH,      ix, iy, iz, 50, 1.5, 0.5, 1.5, 0.3);

        // Sizzle — lava-meets-water sound
        level.playSound(null, impact, SoundEvents.FIRE_EXTINGUISH,
                SoundSource.PLAYERS, 1.5F, 0.8F + level.random.nextFloat() * 0.4F);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide() && lightBlockPos != null
                && level().getBlockState(lightBlockPos).is(Blocks.LIGHT)) {
            level().removeBlock(lightBlockPos, false);
        }
        super.remove(reason);
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
