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
 * Tracks active Gonger's Grotto demiplanes and restores blocks when they expire.
 * Persisted per-dimension via SavedData.
 */
public class GrottoData extends SavedData {
    private static final String DATA_NAME = "reactivefluids_grottos";

    private final List<GrottoInstance> grottos = new ArrayList<>();

    public static class GrottoInstance {
        final List<BlockPos> positions;
        final List<BlockState> originalStates;
        final List<BlockState> placedStates;
        final long expiryTick;

        GrottoInstance(List<BlockPos> positions, List<BlockState> originalStates,
                       List<BlockState> placedStates, long expiryTick) {
            this.positions = new ArrayList<>(positions);
            this.originalStates = new ArrayList<>(originalStates);
            this.placedStates = new ArrayList<>(placedStates);
            this.expiryTick = expiryTick;
        }
    }

    public GrottoData() {}

    public void addGrotto(List<BlockPos> positions, List<BlockState> originalStates,
                          List<BlockState> placedStates, long expiryTick) {
        grottos.add(new GrottoInstance(positions, originalStates, placedStates, expiryTick));
        setDirty();
    }

    /** Restore expired grottos. Called from server tick. */
    public void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        Iterator<GrottoInstance> it = grottos.iterator();
        while (it.hasNext()) {
            GrottoInstance grotto = it.next();
            if (gameTime < grotto.expiryTick) continue;

            // Check all positions are loaded
            boolean allLoaded = true;
            for (BlockPos pos : grotto.positions) {
                if (!level.isLoaded(pos)) { allLoaded = false; break; }
            }
            if (!allLoaded) continue;

            // Restore original blocks — only if current block matches what we placed
            BlockPos soundPos = grotto.positions.isEmpty() ? BlockPos.ZERO : grotto.positions.get(0);
            for (int i = 0; i < grotto.positions.size(); i++) {
                BlockPos pos = grotto.positions.get(i);
                BlockState original = grotto.originalStates.get(i);
                BlockState placed = grotto.placedStates.get(i);
                if (level.getBlockState(pos) == placed) {
                    level.setBlock(pos, original, Block.UPDATE_ALL);
                }
            }

            level.playSound(null, soundPos, SoundEvents.WARDEN_EMERGE,
                    SoundSource.BLOCKS, 1.5F, 0.5F);
            it.remove();
            setDirty();
        }
    }

    // --- Serialization ---

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (GrottoInstance grotto : grottos) {
            CompoundTag grottoTag = new CompoundTag();
            grottoTag.putLong("expiry", grotto.expiryTick);

            ListTag posTag = new ListTag();
            ListTag origStateTag = new ListTag();
            ListTag placedStateTag = new ListTag();
            for (int i = 0; i < grotto.positions.size(); i++) {
                BlockPos pos = grotto.positions.get(i);
                CompoundTag pt = new CompoundTag();
                pt.putInt("x", pos.getX());
                pt.putInt("y", pos.getY());
                pt.putInt("z", pos.getZ());
                posTag.add(pt);
                origStateTag.add(NbtUtils.writeBlockState(grotto.originalStates.get(i)));
                placedStateTag.add(NbtUtils.writeBlockState(grotto.placedStates.get(i)));
            }
            grottoTag.put("positions", posTag);
            grottoTag.put("originalStates", origStateTag);
            grottoTag.put("placedStates", placedStateTag);
            list.add(grottoTag);
        }
        tag.put("grottos", list);
        return tag;
    }

    public static GrottoData load(CompoundTag tag, HolderLookup.Provider provider) {
        GrottoData data = new GrottoData();
        ListTag list = tag.getList("grottos", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag grottoTag = list.getCompound(i);
            long expiry = grottoTag.getLong("expiry");

            ListTag posTag = grottoTag.getList("positions", 10);
            ListTag origStateTag = grottoTag.getList("originalStates", 10);
            ListTag placedStateTag = grottoTag.getList("placedStates", 10);

            List<BlockPos> positions = new ArrayList<>();
            List<BlockState> originalStates = new ArrayList<>();
            List<BlockState> placedStates = new ArrayList<>();
            for (int j = 0; j < posTag.size(); j++) {
                CompoundTag pt = posTag.getCompound(j);
                positions.add(new BlockPos(pt.getInt("x"), pt.getInt("y"), pt.getInt("z")));
                originalStates.add(NbtUtils.readBlockState(
                        BuiltInRegistries.BLOCK.asLookup(), origStateTag.getCompound(j)));
                placedStates.add(NbtUtils.readBlockState(
                        BuiltInRegistries.BLOCK.asLookup(), placedStateTag.getCompound(j)));
            }
            data.grottos.add(new GrottoInstance(positions, originalStates, placedStates, expiry));
        }
        return data;
    }

    public static GrottoData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(GrottoData::new, GrottoData::load),
                DATA_NAME);
    }
}
