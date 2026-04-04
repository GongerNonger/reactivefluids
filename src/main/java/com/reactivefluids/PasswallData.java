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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks active Passwall tunnels and restores blocks when they expire.
 * Persisted per-dimension via SavedData.
 */
public class PasswallData extends SavedData {
    private static final String DATA_NAME = "reactivefluids_passwalls";

    private final List<PasswallInstance> passwalls = new ArrayList<>();

    public static class PasswallInstance {
        final List<BlockPos> positions;
        final List<BlockState> originalStates;
        final long expiryTick;

        PasswallInstance(List<BlockPos> positions, List<BlockState> states, long expiryTick) {
            this.positions = new ArrayList<>(positions);
            this.originalStates = new ArrayList<>(states);
            this.expiryTick = expiryTick;
        }
    }

    public PasswallData() {}

    public void addPasswall(List<BlockPos> positions, List<BlockState> states, long expiryTick) {
        passwalls.add(new PasswallInstance(positions, states, expiryTick));
        setDirty();
    }

    /** Restore expired passwalls. Called from server tick. */
    public void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        Iterator<PasswallInstance> it = passwalls.iterator();
        while (it.hasNext()) {
            PasswallInstance pw = it.next();
            if (gameTime < pw.expiryTick) continue;

            // Check all positions are loaded
            boolean allLoaded = true;
            for (BlockPos pos : pw.positions) {
                if (!level.isLoaded(pos)) { allLoaded = false; break; }
            }
            if (!allLoaded) continue;

            // Restore original blocks
            BlockPos soundPos = pw.positions.isEmpty() ? BlockPos.ZERO : pw.positions.get(0);
            for (int i = 0; i < pw.positions.size(); i++) {
                BlockPos pos = pw.positions.get(i);
                BlockState original = pw.originalStates.get(i);
                // Only restore if the position is still air (player might have placed blocks)
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, original, Block.UPDATE_ALL);
                }
            }

            level.playSound(null, soundPos, SoundEvents.RESPAWN_ANCHOR_CHARGE,
                    SoundSource.BLOCKS, 1.0F, 0.8F);
            it.remove();
            setDirty();
        }
    }

    // --- Serialization ---

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (PasswallInstance pw : passwalls) {
            CompoundTag pwTag = new CompoundTag();
            pwTag.putLong("expiry", pw.expiryTick);

            ListTag posTag = new ListTag();
            ListTag stateTag = new ListTag();
            for (int i = 0; i < pw.positions.size(); i++) {
                BlockPos pos = pw.positions.get(i);
                CompoundTag pt = new CompoundTag();
                pt.putInt("x", pos.getX());
                pt.putInt("y", pos.getY());
                pt.putInt("z", pos.getZ());
                posTag.add(pt);
                stateTag.add(NbtUtils.writeBlockState(pw.originalStates.get(i)));
            }
            pwTag.put("positions", posTag);
            pwTag.put("states", stateTag);
            list.add(pwTag);
        }
        tag.put("passwalls", list);
        return tag;
    }

    public static PasswallData load(CompoundTag tag, HolderLookup.Provider provider) {
        PasswallData data = new PasswallData();
        ListTag list = tag.getList("passwalls", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag pwTag = list.getCompound(i);
            long expiry = pwTag.getLong("expiry");

            ListTag posTag = pwTag.getList("positions", 10);
            ListTag stateTag = pwTag.getList("states", 10);

            List<BlockPos> positions = new ArrayList<>();
            List<BlockState> states = new ArrayList<>();
            for (int j = 0; j < posTag.size(); j++) {
                CompoundTag pt = posTag.getCompound(j);
                positions.add(new BlockPos(pt.getInt("x"), pt.getInt("y"), pt.getInt("z")));
                states.add(NbtUtils.readBlockState(
                        BuiltInRegistries.BLOCK.asLookup(), stateTag.getCompound(j)));
            }
            data.passwalls.add(new PasswallInstance(positions, states, expiry));
        }
        return data;
    }

    public static PasswallData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PasswallData::new, PasswallData::load),
                DATA_NAME);
    }
}
