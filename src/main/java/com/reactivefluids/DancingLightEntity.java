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
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A single dancing light orb — orbits the caster, places invisible light blocks,
 * and renders as glowing particles. Four of these are spawned per scroll use.
 */
public class DancingLightEntity extends Entity {

    public static final int DURATION_TICKS = 1200; // 1 minute
    public static final int FADE_TICKS = 100;      // 5 second warning
    private static final float ORBIT_RADIUS = 4.0F;
    private static final float BOB_HEIGHT = 0.6F;
    private static final float ORBIT_SPEED = 0.03F; // radians per tick

    private static final EntityDataAccessor<Integer> DATA_TICKS_REMAINING =
            SynchedEntityData.defineId(DancingLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(DancingLightEntity.class, EntityDataSerializers.INT);

    private UUID ownerUUID;
    private float orbitPhase;   // radians offset for this orb
    private BlockPos lastLightPos;

    // 4 warm colors for the 4 orbs
    private static final int[] ORB_COLORS = {
            0xFFDD44, // warm gold
            0xFF8822, // amber orange
            0xFFAA33, // golden yellow
            0xDDFF66  // pale lime
    };

    public DancingLightEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(UUID uuid) { this.ownerUUID = uuid; }
    public void setOrbitPhase(float phase) { this.orbitPhase = phase; }
    public void setColorIndex(int idx) {
        entityData.set(DATA_COLOR, idx % ORB_COLORS.length);
    }

    public int getOrbColor() {
        return ORB_COLORS[entityData.get(DATA_COLOR) % ORB_COLORS.length];
    }

    public int getTicksRemaining() {
        return entityData.get(DATA_TICKS_REMAINING);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TICKS_REMAINING, DURATION_TICKS);
        builder.define(DATA_COLOR, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            int remaining = entityData.get(DATA_TICKS_REMAINING) - 1;
            entityData.set(DATA_TICKS_REMAINING, remaining);

            if (remaining <= 0) {
                removeLightBlock();
                discard();
                return;
            }

            // Find owner
            Entity owner = findOwner();
            if (owner == null || owner.isRemoved()) {
                removeLightBlock();
                discard();
                return;
            }

            // Calculate orbit position around owner
            float age = (DURATION_TICKS - remaining) * ORBIT_SPEED + orbitPhase;
            double targetX = owner.getX() + Math.cos(age) * ORBIT_RADIUS;
            double targetY = owner.getY() + 1.5 + Math.sin(age * 1.7) * BOB_HEIGHT;
            double targetZ = owner.getZ() + Math.sin(age) * ORBIT_RADIUS;

            // Smoothly move toward target
            double dx = targetX - getX();
            double dy = targetY - getY();
            double dz = targetZ - getZ();
            setDeltaMovement(dx * 0.3, dy * 0.3, dz * 0.3);
            move(MoverType.SELF, getDeltaMovement());

            // Place / update light block
            updateLightBlock();
        }

        // Client-side particles
        if (level().isClientSide()) {
            int remaining = getTicksRemaining();
            int color = getOrbColor();
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;

            // Custom dancing light particles
            level().addParticle(ModParticles.DANCING_LIGHT.get(),
                    getX(), getY(), getZ(),
                    (random.nextDouble() - 0.5) * 0.03,
                    (random.nextDouble() - 0.5) * 0.03,
                    (random.nextDouble() - 0.5) * 0.03);

            // Secondary glow particles
            if (random.nextInt(2) == 0) {
                level().addParticle(ModParticles.DANCING_LIGHT.get(),
                        getX() + (random.nextDouble() - 0.5) * 0.4,
                        getY() + (random.nextDouble() - 0.5) * 0.4,
                        getZ() + (random.nextDouble() - 0.5) * 0.4,
                        0, 0.015, 0);
            }

            // Extra sparkle during fade
            if (remaining <= FADE_TICKS) {
                for (int i = 0; i < 2; i++) {
                    level().addParticle(ParticleTypes.ENCHANT,
                            getX() + (random.nextDouble() - 0.5) * 0.5,
                            getY() + (random.nextDouble() - 0.5) * 0.5,
                            getZ() + (random.nextDouble() - 0.5) * 0.5,
                            0, -0.05, 0);
                }
            }
        }
    }

    @Nullable
    private Entity findOwner() {
        if (ownerUUID == null) return null;
        if (level() instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(ownerUUID);
        }
        return null;
    }

    private void updateLightBlock() {
        BlockPos currentPos = blockPosition();
        if (currentPos.equals(lastLightPos)) return;

        // Remove old light
        removeLightBlock();

        // Place new light if position is air
        BlockState current = level().getBlockState(currentPos);
        if (current.isAir()) {
            level().setBlock(currentPos,
                    Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 12),
                    3);
            lastLightPos = currentPos;
        }
    }

    private void removeLightBlock() {
        if (lastLightPos != null && level() instanceof ServerLevel) {
            BlockState state = level().getBlockState(lastLightPos);
            if (state.is(Blocks.LIGHT)) {
                level().setBlock(lastLightPos, Blocks.AIR.defaultBlockState(), 3);
            }
            lastLightPos = null;
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        removeLightBlock();
        super.remove(reason);
    }

    // --- No collision, invisible hitbox, no pushing ---

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean isNoGravity() { return true; }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64 * 64;
    }

    // --- Serialization ---

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_TICKS_REMAINING, tag.getInt("TicksRemaining"));
        entityData.set(DATA_COLOR, tag.getInt("ColorIndex"));
        orbitPhase = tag.getFloat("OrbitPhase");
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        if (tag.contains("LightX")) {
            lastLightPos = new BlockPos(tag.getInt("LightX"), tag.getInt("LightY"), tag.getInt("LightZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("TicksRemaining", entityData.get(DATA_TICKS_REMAINING));
        tag.putInt("ColorIndex", entityData.get(DATA_COLOR));
        tag.putFloat("OrbitPhase", orbitPhase);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        if (lastLightPos != null) {
            tag.putInt("LightX", lastLightPos.getX());
            tag.putInt("LightY", lastLightPos.getY());
            tag.putInt("LightZ", lastLightPos.getZ());
        }
    }
}
