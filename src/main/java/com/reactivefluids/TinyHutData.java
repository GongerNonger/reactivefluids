package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks active Tiny Hut domes and handles their expiration.
 * Persisted per-dimension via SavedData.
 */
public class TinyHutData extends SavedData {
    private static final String DATA_NAME = "reactivefluids_tiny_huts";

    private final List<HutInstance> huts = new ArrayList<>();

    public static class HutInstance {
        final List<BlockPos> blocks;
        final BlockPos center;
        final long expiryTick;

        HutInstance(List<BlockPos> blocks, BlockPos center, long expiryTick) {
            this.blocks = new ArrayList<>(blocks);
            this.center = center;
            this.expiryTick = expiryTick;
        }
    }

    public TinyHutData() {}

    public void addHut(List<BlockPos> blocks, BlockPos center, long expiryTick) {
        huts.add(new HutInstance(blocks, center, expiryTick));
        setDirty();
    }

    /**
     * Returns true if the given player position is inside any active hut dome
     * (within 5.0 blocks of a hut center).
     */
    public boolean isPlayerInsideHut(BlockPos playerPos) {
        for (HutInstance hut : huts) {
            double dist = Math.sqrt(hut.center.distSqr(playerPos));
            if (dist < 5.0) return true;
        }
        return false;
    }

    /** Remove expired huts. Called periodically from server tick. */
    public void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        Iterator<HutInstance> it = huts.iterator();
        while (it.hasNext()) {
            HutInstance hut = it.next();
            if (gameTime < hut.expiryTick) continue;

            // Check all blocks are in loaded chunks
            boolean allLoaded = true;
            for (BlockPos pos : hut.blocks) {
                if (!level.isLoaded(pos)) { allLoaded = false; break; }
            }
            if (!allLoaded) continue; // retry next tick

            // Remove hut blocks
            BlockPos soundPos = hut.blocks.isEmpty() ? BlockPos.ZERO : hut.blocks.get(0);
            for (BlockPos pos : hut.blocks) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
            level.playSound(null, soundPos, SoundEvents.GLASS_BREAK,
                    SoundSource.BLOCKS, 1.0F, 0.8F);
            it.remove();
            setDirty();
        }
    }

    // --- Serialization ---

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (HutInstance hut : huts) {
            CompoundTag hutTag = new CompoundTag();
            hutTag.putLong("expiry", hut.expiryTick);
            hutTag.putInt("centerX", hut.center.getX());
            hutTag.putInt("centerY", hut.center.getY());
            hutTag.putInt("centerZ", hut.center.getZ());
            ListTag blocksTag = new ListTag();
            for (BlockPos pos : hut.blocks) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("x", pos.getX());
                posTag.putInt("y", pos.getY());
                posTag.putInt("z", pos.getZ());
                blocksTag.add(posTag);
            }
            hutTag.put("blocks", blocksTag);
            list.add(hutTag);
        }
        tag.put("huts", list);
        return tag;
    }

    public static TinyHutData load(CompoundTag tag, HolderLookup.Provider provider) {
        TinyHutData data = new TinyHutData();
        ListTag list = tag.getList("huts", 10); // 10 = TAG_COMPOUND
        for (int i = 0; i < list.size(); i++) {
            CompoundTag hutTag = list.getCompound(i);
            long expiry = hutTag.getLong("expiry");
            BlockPos center = new BlockPos(
                    hutTag.getInt("centerX"),
                    hutTag.getInt("centerY"),
                    hutTag.getInt("centerZ"));
            ListTag blocksTag = hutTag.getList("blocks", 10);
            List<BlockPos> blocks = new ArrayList<>();
            for (int j = 0; j < blocksTag.size(); j++) {
                CompoundTag posTag = blocksTag.getCompound(j);
                blocks.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
            }
            data.huts.add(new HutInstance(blocks, center, expiry));
        }
        return data;
    }

    public static TinyHutData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(TinyHutData::new, TinyHutData::load),
                DATA_NAME);
    }
}
