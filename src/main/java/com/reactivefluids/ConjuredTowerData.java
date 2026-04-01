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
 * Tracks active conjured towers and handles their expiration.
 * Persisted per-dimension via SavedData.
 */
public class ConjuredTowerData extends SavedData {
    private static final String DATA_NAME = "reactivefluids_conjured_towers";

    private final List<TowerInstance> towers = new ArrayList<>();

    public static class TowerInstance {
        final List<BlockPos> blocks;
        final long expiryTick;

        TowerInstance(List<BlockPos> blocks, long expiryTick) {
            this.blocks = new ArrayList<>(blocks);
            this.expiryTick = expiryTick;
        }
    }

    public ConjuredTowerData() {}

    public void addTower(List<BlockPos> blocks, long expiryTick) {
        towers.add(new TowerInstance(blocks, expiryTick));
        setDirty();
    }

    /** Remove expired towers. Called periodically from server tick. */
    public void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        Iterator<TowerInstance> it = towers.iterator();
        while (it.hasNext()) {
            TowerInstance tower = it.next();
            if (gameTime < tower.expiryTick) continue;

            // Check all blocks are in loaded chunks
            boolean allLoaded = true;
            for (BlockPos pos : tower.blocks) {
                if (!level.isLoaded(pos)) { allLoaded = false; break; }
            }
            if (!allLoaded) continue; // retry next tick

            // Remove tower blocks
            BlockPos soundPos = tower.blocks.isEmpty() ? BlockPos.ZERO : tower.blocks.get(0);
            for (BlockPos pos : tower.blocks) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
            level.playSound(null, soundPos, SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.BLOCKS, 1.0F, 0.5F);
            it.remove();
            setDirty();
        }
    }

    // --- Serialization ---

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (TowerInstance tower : towers) {
            CompoundTag towerTag = new CompoundTag();
            towerTag.putLong("expiry", tower.expiryTick);
            ListTag blocksTag = new ListTag();
            for (BlockPos pos : tower.blocks) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("x", pos.getX());
                posTag.putInt("y", pos.getY());
                posTag.putInt("z", pos.getZ());
                blocksTag.add(posTag);
            }
            towerTag.put("blocks", blocksTag);
            list.add(towerTag);
        }
        tag.put("towers", list);
        return tag;
    }

    public static ConjuredTowerData load(CompoundTag tag, HolderLookup.Provider provider) {
        ConjuredTowerData data = new ConjuredTowerData();
        ListTag list = tag.getList("towers", 10); // 10 = TAG_COMPOUND
        for (int i = 0; i < list.size(); i++) {
            CompoundTag towerTag = list.getCompound(i);
            long expiry = towerTag.getLong("expiry");
            ListTag blocksTag = towerTag.getList("blocks", 10);
            List<BlockPos> blocks = new ArrayList<>();
            for (int j = 0; j < blocksTag.size(); j++) {
                CompoundTag posTag = blocksTag.getCompound(j);
                blocks.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
            }
            data.towers.add(new TowerInstance(blocks, expiry));
        }
        return data;
    }

    public static ConjuredTowerData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ConjuredTowerData::new, ConjuredTowerData::load),
                DATA_NAME);
    }
}
