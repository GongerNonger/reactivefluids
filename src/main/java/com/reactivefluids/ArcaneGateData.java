package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks active Arcane Gate portal pairs, teleports entities between them,
 * and removes them on expiry. Persisted per-dimension via SavedData.
 */
public class ArcaneGateData extends SavedData {
    private static final String DATA_NAME = "reactivefluids_arcane_gates";

    /** Cooldown in ticks before an entity can be teleported again (prevents loops). */
    private static final int TELEPORT_COOLDOWN = 40; // 2 seconds

    private final List<GateInstance> gates = new ArrayList<>();

    public static class GateInstance {
        final BlockPos entry;
        final BlockPos exit;
        final long expiryTick;

        GateInstance(BlockPos entry, BlockPos exit, long expiryTick) {
            this.entry = entry.immutable();
            this.exit = exit.immutable();
            this.expiryTick = expiryTick;
        }
    }

    public ArcaneGateData() {}

    public void addGate(BlockPos entry, BlockPos exit, long expiryTick) {
        gates.add(new GateInstance(entry, exit, expiryTick));
        setDirty();
    }

    /** Teleport entities and expire gates. Called from server tick. */
    public void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        Iterator<GateInstance> it = gates.iterator();
        while (it.hasNext()) {
            GateInstance gate = it.next();

            // Check if both portals are in loaded chunks
            if (!level.isLoaded(gate.entry) || !level.isLoaded(gate.exit)) continue;

            if (gameTime >= gate.expiryTick) {
                // Expire — remove glass blocks
                removePortalBlocks(level, gate.entry);
                removePortalBlocks(level, gate.exit);

                level.playSound(null, gate.entry, SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.BLOCKS, 1.0F, 0.6F);
                level.playSound(null, gate.exit, SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.BLOCKS, 1.0F, 0.6F);

                it.remove();
                setDirty();
                continue;
            }

            // Teleport entities at entry -> exit
            teleportEntitiesAt(level, gate.entry, gate.exit);
            // Teleport entities at exit -> entry
            teleportEntitiesAt(level, gate.exit, gate.entry);
        }
    }

    private void teleportEntitiesAt(ServerLevel level, BlockPos from, BlockPos to) {
        // Check the 2-tall column at 'from' for entities
        AABB portalBox = new AABB(
                from.getX(), from.getY(), from.getZ(),
                from.getX() + 1.0, from.getY() + 2.0, from.getZ() + 1.0);

        List<Entity> entities = level.getEntities((Entity) null, portalBox,
                e -> e.isAlive() && !e.isPassenger());

        for (Entity entity : entities) {
            // Check cooldown via portal cooldown field
            if (entity.isOnPortalCooldown()) continue;

            Vec3 dest = new Vec3(to.getX() + 0.5, to.getY(), to.getZ() + 0.5);
            entity.teleportTo(dest.x, dest.y, dest.z);
            entity.setPortalCooldown(TELEPORT_COOLDOWN);

            level.playSound(null, to, SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS, 0.8F, 1.0F);
        }
    }

    private void removePortalBlocks(ServerLevel level, BlockPos base) {
        if (level.getBlockState(base).is(Blocks.PURPLE_STAINED_GLASS)) {
            level.setBlock(base, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        BlockPos above = base.above();
        if (level.getBlockState(above).is(Blocks.PURPLE_STAINED_GLASS)) {
            level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    // --- Serialization ---

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (GateInstance gate : gates) {
            CompoundTag gateTag = new CompoundTag();
            gateTag.putLong("expiry", gate.expiryTick);
            gateTag.putInt("entryX", gate.entry.getX());
            gateTag.putInt("entryY", gate.entry.getY());
            gateTag.putInt("entryZ", gate.entry.getZ());
            gateTag.putInt("exitX", gate.exit.getX());
            gateTag.putInt("exitY", gate.exit.getY());
            gateTag.putInt("exitZ", gate.exit.getZ());
            list.add(gateTag);
        }
        tag.put("gates", list);
        return tag;
    }

    public static ArcaneGateData load(CompoundTag tag, HolderLookup.Provider provider) {
        ArcaneGateData data = new ArcaneGateData();
        ListTag list = tag.getList("gates", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag gateTag = list.getCompound(i);
            BlockPos entry = new BlockPos(
                    gateTag.getInt("entryX"), gateTag.getInt("entryY"), gateTag.getInt("entryZ"));
            BlockPos exit = new BlockPos(
                    gateTag.getInt("exitX"), gateTag.getInt("exitY"), gateTag.getInt("exitZ"));
            long expiry = gateTag.getLong("expiry");
            data.gates.add(new GateInstance(entry, exit, expiry));
        }
        return data;
    }

    public static ArcaneGateData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ArcaneGateData::new, ArcaneGateData::load),
                DATA_NAME);
    }
}
