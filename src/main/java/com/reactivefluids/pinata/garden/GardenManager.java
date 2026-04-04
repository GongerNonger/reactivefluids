package com.reactivefluids.pinata.garden;

import com.reactivefluids.pinata.BasePinataEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import java.util.*;

/**
 * Manages garden boundaries and tracks garden state for piñata attraction.
 *
 * A garden is defined by placing a Garden Plot block. The garden extends
 * in a radius around it, growing as the gardener levels up.
 * The manager tracks:
 * - Garden boundaries (center + radius)
 * - Block counts per type (grass, water, flowers, etc.) for attraction checks
 * - Resident piñatas within the garden
 * - Gardener level and XP
 */
public class GardenManager extends SavedData {

    private static final String DATA_NAME = "reactivefluids_gardens";

    /** All active gardens, keyed by their center block position */
    private final Map<BlockPos, GardenData> gardens = new HashMap<>();

    public GardenManager() {}

    public GardenManager(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag list = tag.getList("Gardens", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag gardenTag = list.getCompound(i);
            GardenData data = GardenData.load(gardenTag);
            gardens.put(data.center, data);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (GardenData data : gardens.values()) {
            list.add(data.save());
        }
        tag.put("Gardens", list);
        return tag;
    }

    public static GardenManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(GardenManager::new, GardenManager::new,
                        net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_MAP_DATA),
                DATA_NAME);
    }

    // --- Garden CRUD ---

    public GardenData createGarden(BlockPos center, UUID ownerUUID) {
        GardenData data = new GardenData(center, ownerUUID);
        gardens.put(center, data);
        setDirty();
        return data;
    }

    public void removeGarden(BlockPos center) {
        gardens.remove(center);
        setDirty();
    }

    public GardenData getGarden(BlockPos center) {
        return gardens.get(center);
    }

    /** Find which garden (if any) contains this position */
    public GardenData getGardenAt(BlockPos pos) {
        for (GardenData garden : gardens.values()) {
            if (garden.contains(pos)) return garden;
        }
        return null;
    }

    public Collection<GardenData> getAllGardens() {
        return gardens.values();
    }

    // --- Block scanning (called periodically, not every tick) ---

    /**
     * Scan the blocks in a garden and update surface counts.
     * Called every ~10 seconds per garden to stay performance-friendly.
     */
    public void scanGardenBlocks(ServerLevel level, GardenData garden) {
        garden.grassCount = 0;
        garden.waterCount = 0;
        garden.flowerCount = 0;
        garden.longGrassCount = 0;
        garden.sandCount = 0;
        garden.snowCount = 0;

        int r = garden.radius;
        BlockPos c = garden.center;

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                // Scan the surface column
                BlockPos surface = level.getHeightmapPos(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        c.offset(x, 0, z));
                BlockPos below = surface.below();
                BlockState groundState = level.getBlockState(below);
                BlockState surfaceState = level.getBlockState(surface);

                // Count ground blocks
                if (groundState.is(Blocks.GRASS_BLOCK)) garden.grassCount++;
                else if (groundState.is(Blocks.WATER)) garden.waterCount++;
                else if (groundState.is(Blocks.SAND)) garden.sandCount++;
                else if (groundState.is(net.minecraft.world.level.block.Blocks.SNOW_BLOCK)) garden.snowCount++;

                // Count surface features
                if (surfaceState.is(Blocks.SHORT_GRASS) || surfaceState.is(Blocks.TALL_GRASS))
                    garden.longGrassCount++;
                if (isFlower(surfaceState)) garden.flowerCount++;
                if (surfaceState.is(Blocks.WATER)) garden.waterCount++;
            }
        }

        setDirty();
    }

    /** Count resident piñatas inside a garden */
    public List<BasePinataEntity> getResidentPinatas(Level level, GardenData garden) {
        AABB box = garden.getBoundingBox();
        List<BasePinataEntity> residents = new ArrayList<>();
        for (Entity entity : level.getEntities(null, box)) {
            if (entity instanceof BasePinataEntity pinata && pinata.isResident()) {
                residents.add(pinata);
            }
        }
        return residents;
    }

    /** Count all piñatas (including visitors/wild) */
    public List<BasePinataEntity> getAllPinatasInGarden(Level level, GardenData garden) {
        AABB box = garden.getBoundingBox();
        List<BasePinataEntity> pinatas = new ArrayList<>();
        for (Entity entity : level.getEntities(null, box)) {
            if (entity instanceof BasePinataEntity pinata) {
                pinatas.add(pinata);
            }
        }
        return pinatas;
    }

    private boolean isFlower(BlockState state) {
        return state.is(Blocks.DANDELION) || state.is(Blocks.POPPY) ||
               state.is(Blocks.OXEYE_DAISY) || state.is(Blocks.BLUE_ORCHID) ||
               state.is(Blocks.ALLIUM) || state.is(Blocks.AZURE_BLUET) ||
               state.is(Blocks.RED_TULIP) || state.is(Blocks.ORANGE_TULIP) ||
               state.is(Blocks.WHITE_TULIP) || state.is(Blocks.PINK_TULIP) ||
               state.is(Blocks.CORNFLOWER) || state.is(Blocks.LILY_OF_THE_VALLEY) ||
               state.is(Blocks.SUNFLOWER) || state.is(Blocks.LILAC) ||
               state.is(Blocks.ROSE_BUSH) || state.is(Blocks.PEONY) ||
               state.is(Blocks.TORCHFLOWER);
    }

    // --- Inner class: Garden Data ---

    public static class GardenData {
        public final BlockPos center;
        public UUID ownerUUID;
        public int radius = 16; // Default 16-block radius (32x32 area)
        public int level = 1;
        public int xp = 0;

        // Block counts (updated by scanGardenBlocks)
        public int grassCount;
        public int waterCount;
        public int flowerCount;
        public int longGrassCount;
        public int sandCount;
        public int snowCount;

        public GardenData(BlockPos center, UUID ownerUUID) {
            this.center = center;
            this.ownerUUID = ownerUUID;
        }

        public boolean contains(BlockPos pos) {
            return Math.abs(pos.getX() - center.getX()) <= radius
                    && Math.abs(pos.getZ() - center.getZ()) <= radius;
        }

        public AABB getBoundingBox() {
            return new AABB(
                    center.getX() - radius, center.getY() - 10, center.getZ() - radius,
                    center.getX() + radius + 1, center.getY() + 30, center.getZ() + radius + 1);
        }

        /** Add XP and check for level up. Returns true if leveled up. */
        public boolean addXP(int amount) {
            xp += amount;
            int xpNeeded = level * 100;
            if (xp >= xpNeeded && level < 50) {
                xp -= xpNeeded;
                level++;
                // Radius grows every 5 levels
                if (level % 5 == 0) radius += 4;
                return true;
            }
            return false;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("CenterX", center.getX());
            tag.putInt("CenterY", center.getY());
            tag.putInt("CenterZ", center.getZ());
            tag.putUUID("Owner", ownerUUID);
            tag.putInt("Radius", radius);
            tag.putInt("Level", level);
            tag.putInt("XP", xp);
            return tag;
        }

        public static GardenData load(CompoundTag tag) {
            BlockPos center = new BlockPos(tag.getInt("CenterX"), tag.getInt("CenterY"), tag.getInt("CenterZ"));
            UUID owner = tag.getUUID("Owner");
            GardenData data = new GardenData(center, owner);
            data.radius = tag.getInt("Radius");
            data.level = tag.getInt("Level");
            data.xp = tag.getInt("XP");
            return data;
        }
    }
}
