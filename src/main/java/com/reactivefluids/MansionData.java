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
 * Tracks active Magnificent Mansion instances and handles their expiration.
 * Persisted per-dimension via SavedData.
 */
public class MansionData extends SavedData {
    private static final String DATA_NAME = "reactivefluids_mansions";

    private final List<MansionInstance> mansions = new ArrayList<>();

    public static class MansionInstance {
        final List<BlockPos> blocks;
        final long expiryTick;

        MansionInstance(List<BlockPos> blocks, long expiryTick) {
            this.blocks = new ArrayList<>(blocks);
            this.expiryTick = expiryTick;
        }
    }

    public MansionData() {}

    public void addMansion(List<BlockPos> blocks, long expiryTick) {
        mansions.add(new MansionInstance(blocks, expiryTick));
        setDirty();
    }

    /** Remove expired mansions. Called periodically from server tick. */
    public void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        Iterator<MansionInstance> it = mansions.iterator();
        while (it.hasNext()) {
            MansionInstance mansion = it.next();
            if (gameTime < mansion.expiryTick) continue;

            // Check all blocks are in loaded chunks
            boolean allLoaded = true;
            for (BlockPos pos : mansion.blocks) {
                if (!level.isLoaded(pos)) { allLoaded = false; break; }
            }
            if (!allLoaded) continue; // retry next tick

            // Remove mansion blocks
            BlockPos soundPos = mansion.blocks.isEmpty() ? BlockPos.ZERO : mansion.blocks.get(0);
            for (BlockPos pos : mansion.blocks) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
            level.playSound(null, soundPos, SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.BLOCKS, 1.5F, 0.5F);
            it.remove();
            setDirty();
        }
    }

    // --- Serialization ---

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (MansionInstance mansion : mansions) {
            CompoundTag mansionTag = new CompoundTag();
            mansionTag.putLong("expiry", mansion.expiryTick);
            ListTag blocksTag = new ListTag();
            for (BlockPos pos : mansion.blocks) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("x", pos.getX());
                posTag.putInt("y", pos.getY());
                posTag.putInt("z", pos.getZ());
                blocksTag.add(posTag);
            }
            mansionTag.put("blocks", blocksTag);
            list.add(mansionTag);
        }
        tag.put("mansions", list);
        return tag;
    }

    public static MansionData load(CompoundTag tag, HolderLookup.Provider provider) {
        MansionData data = new MansionData();
        ListTag list = tag.getList("mansions", 10); // 10 = TAG_COMPOUND
        for (int i = 0; i < list.size(); i++) {
            CompoundTag mansionTag = list.getCompound(i);
            long expiry = mansionTag.getLong("expiry");
            ListTag blocksTag = mansionTag.getList("blocks", 10);
            List<BlockPos> blocks = new ArrayList<>();
            for (int j = 0; j < blocksTag.size(); j++) {
                CompoundTag posTag = blocksTag.getCompound(j);
                blocks.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
            }
            data.mansions.add(new MansionInstance(blocks, expiry));
        }
        return data;
    }

    public static MansionData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(MansionData::new, MansionData::load),
                DATA_NAME);
    }
}
