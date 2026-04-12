package com.reactivefluids;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;

/**
 * A purely visual entity that renders a green disintegration beam
 * from start to end position. Lasts ~1 second then fades and discards.
 */
public class DisintegrateBeamEntity extends Entity {

    public static final int BEAM_LIFETIME = 20; // 1 second

    // Store end point relative to entity position (which is the start)
    private static final EntityDataAccessor<Float> DATA_END_X =
            SynchedEntityData.defineId(DisintegrateBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Y =
            SynchedEntityData.defineId(DisintegrateBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Z =
            SynchedEntityData.defineId(DisintegrateBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_TICKS =
            SynchedEntityData.defineId(DisintegrateBeamEntity.class, EntityDataSerializers.INT);

    public DisintegrateBeamEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setBeamEnd(Vec3 end) {
        entityData.set(DATA_END_X, (float) end.x);
        entityData.set(DATA_END_Y, (float) end.y);
        entityData.set(DATA_END_Z, (float) end.z);
    }

    public Vec3 getBeamEnd() {
        return new Vec3(
                entityData.get(DATA_END_X),
                entityData.get(DATA_END_Y),
                entityData.get(DATA_END_Z));
    }

    public float getBeamProgress() {
        int ticks = entityData.get(DATA_TICKS);
        return Math.min(1.0F, (float) ticks / (float) BEAM_LIFETIME);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_END_X, 0F);
        builder.define(DATA_END_Y, 0F);
        builder.define(DATA_END_Z, 0F);
        builder.define(DATA_TICKS, 0);
    }

    @Override
    public void tick() {
        super.tick();
        int ticks = entityData.get(DATA_TICKS) + 1;
        entityData.set(DATA_TICKS, ticks);

        if (!level().isClientSide() && ticks >= BEAM_LIFETIME) {
            discard();
        }

        // Client-side: spawn custom particles along the beam — thin single-particle line
        if (level().isClientSide()) {
            Vec3 start = position();
            Vec3 end = getBeamEnd();
            // Guard: synced data may not have arrived yet (defaults are 0,0,0)
            if (end.x == 0 && end.y == 0 && end.z == 0) return;
            Vec3 diff = end.subtract(start);
            double length = diff.length();
            if (length < 0.1) return;

            Vec3 dir = diff.normalize();
            float fade = 1.0F - getBeamProgress();

            // Skip the first 2 blocks so particles aren't in the player's face
            double startOffset = Math.min(2.0, length * 0.5);

            // Single particle per step, tight to the line
            for (double d = startOffset; d < length; d += 0.5) {
                Vec3 pos = start.add(dir.scale(d));
                level().addParticle(ModParticles.DISINTEGRATE.get(),
                        pos.x, pos.y, pos.z,
                        0, 0, 0);
            }
        }
    }

    @Override
    public boolean isPickable() { return false; }
    @Override
    public boolean isPushable() { return false; }
    @Override
    public boolean isNoGravity() { return true; }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128 * 128;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_END_X, tag.getFloat("EndX"));
        entityData.set(DATA_END_Y, tag.getFloat("EndY"));
        entityData.set(DATA_END_Z, tag.getFloat("EndZ"));
        entityData.set(DATA_TICKS, tag.getInt("Ticks"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("EndX", entityData.get(DATA_END_X));
        tag.putFloat("EndY", entityData.get(DATA_END_Y));
        tag.putFloat("EndZ", entityData.get(DATA_END_Z));
        tag.putInt("Ticks", entityData.get(DATA_TICKS));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

}
