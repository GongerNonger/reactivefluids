package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks active Control Water partings and restores water when they expire.
 * Persisted per-dimension via SavedData.
 */
public class ControlWaterData extends SavedData {
    private static final String DATA_NAME = "reactivefluids_control_water";

    private final List<PartingInstance> partings = new ArrayList<>();

    public static class PartingInstance {
        final List<BlockPos> positions;
        final List<BlockState> originalStates;
        final long expiryTick;

        PartingInstance(List<BlockPos> positions, List<BlockState> states, long expiryTick) {
            this.positions = new ArrayList<>(positions);
            this.originalStates = new ArrayList<>(states);
            this.expiryTick = expiryTick;
        }
    }

    public ControlWaterData() {}

    public void addParting(List<BlockPos> positions, List<BlockState> states, long expiryTick) {
        partings.add(new PartingInstance(positions, states, expiryTick));
        setDirty();
    }

    /** Restore expired water partings. Called from server tick. */
    public void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        Iterator<PartingInstance> it = partings.iterator();
        while (it.hasNext()) {
            PartingInstance parting = it.next();
            if (gameTime < parting.expiryTick) continue;

            // Check all positions are loaded
            boolean allLoaded = true;
            for (BlockPos pos : parting.positions) {
                if (!level.isLoaded(pos)) { allLoaded = false; break; }
            }
            if (!allLoaded) continue;

            // Restore original water blocks
            BlockPos soundPos = parting.positions.isEmpty() ? BlockPos.ZERO : parting.positions.get(0);
            for (int i = 0; i < parting.positions.size(); i++) {
                BlockPos pos = parting.positions.get(i);
                BlockState original = parting.originalStates.get(i);
                // Restore if position is still barrier or air (from corridor interior)
                BlockState current = level.getBlockState(pos);
                if (current.is(Blocks.BARRIER) || current.isAir()) {
                    level.setBlock(pos, original, Block.UPDATE_ALL);
                }
            }

            level.playSound(null, soundPos, SoundEvents.GENERIC_SPLASH,
                    SoundSource.BLOCKS, 1.5F, 0.8F);
            it.remove();
            setDirty();
        }
    }

    // --- Serialization ---

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (PartingInstance parting : partings) {
            CompoundTag partingTag = new CompoundTag();
            partingTag.putLong("expiry", parting.expiryTick);

            ListTag posTag = new ListTag();
            ListTag stateTag = new ListTag();
            for (int i = 0; i < parting.positions.size(); i++) {
                BlockPos pos = parting.positions.get(i);
                CompoundTag pt = new CompoundTag();
                pt.putInt("x", pos.getX());
                pt.putInt("y", pos.getY());
                pt.putInt("z", pos.getZ());
                posTag.add(pt);
                stateTag.add(NbtUtils.writeBlockState(parting.originalStates.get(i)));
            }
            partingTag.put("positions", posTag);
            partingTag.put("states", stateTag);
            list.add(partingTag);
        }
        tag.put("partings", list);
        return tag;
    }

    public static ControlWaterData load(CompoundTag tag, HolderLookup.Provider provider) {
        ControlWaterData data = new ControlWaterData();
        ListTag list = tag.getList("partings", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag partingTag = list.getCompound(i);
            long expiry = partingTag.getLong("expiry");

            ListTag posTag = partingTag.getList("positions", 10);
            ListTag stateTag = partingTag.getList("states", 10);

            List<BlockPos> positions = new ArrayList<>();
            List<BlockState> states = new ArrayList<>();
            for (int j = 0; j < posTag.size(); j++) {
                CompoundTag pt = posTag.getCompound(j);
                positions.add(new BlockPos(pt.getInt("x"), pt.getInt("y"), pt.getInt("z")));
                states.add(NbtUtils.readBlockState(
                        BuiltInRegistries.BLOCK.asLookup(), stateTag.getCompound(j)));
            }
            data.partings.add(new PartingInstance(positions, states, expiry));
        }
        return data;
    }

    public static ControlWaterData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ControlWaterData::new, ControlWaterData::load),
                DATA_NAME);
    }
}
