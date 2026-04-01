package com.reactivefluids;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Tracks return positions for players teleported into pocket dimensions,
 * and manages expiry of pocket dimension instances.
 * Stored on the overworld so it's accessible from any dimension.
 */
public class PocketDimensionData extends SavedData {
    private static final String DATA_NAME = "reactivefluids_pocket_dimensions";

    /** Player UUID → where they came from (so they can return). */
    private final Map<UUID, ReturnInfo> returnPositions = new HashMap<>();

    /** Active pocket instances with expiry timers. */
    private final List<PocketInstance> instances = new ArrayList<>();

    /** Counter for allocating build offsets (each invocation builds at a new X offset). */
    private int nextMansionSlot = 0;
    private int nextGrottoSlot = 0;

    public PocketDimensionData() {}

    // =====================================================================
    // Return position management
    // =====================================================================

    public void saveReturnPosition(UUID playerId, ResourceKey<Level> fromDimension,
                                    double x, double y, double z, float yRot, float xRot) {
        returnPositions.put(playerId, new ReturnInfo(fromDimension, x, y, z, yRot, xRot));
        setDirty();
    }

    public ReturnInfo getReturnPosition(UUID playerId) {
        return returnPositions.get(playerId);
    }

    public void removeReturnPosition(UUID playerId) {
        returnPositions.remove(playerId);
        setDirty();
    }

    // =====================================================================
    // Slot allocation (each scroll invocation builds at a new location)
    // =====================================================================

    public int allocateMansionSlot() {
        int slot = nextMansionSlot++;
        setDirty();
        return slot;
    }

    public int allocateGrottoSlot() {
        int slot = nextGrottoSlot++;
        setDirty();
        return slot;
    }

    /** Convert a slot number to a world X offset (1000 blocks apart). */
    public static int slotToX(int slot) {
        return slot * 1000;
    }

    // =====================================================================
    // Instance expiry tracking
    // =====================================================================

    public void addInstance(ResourceKey<Level> dimension, long expiryTick) {
        instances.add(new PocketInstance(dimension, expiryTick));
        setDirty();
    }

    /** Called every 200 ticks from the server tick handler. */
    public void tick(MinecraftServer server) {
        long gameTime = server.overworld().getGameTime();
        Iterator<PocketInstance> it = instances.iterator();
        while (it.hasNext()) {
            PocketInstance instance = it.next();
            if (gameTime < instance.expiryTick) continue;

            // Expired — teleport all players in that dimension back
            ServerLevel pocketLevel = server.getLevel(instance.dimension);
            if (pocketLevel != null) {
                for (ServerPlayer player : new ArrayList<>(pocketLevel.players())) {
                    teleportBack(player, server);
                }
            }

            it.remove();
            setDirty();
        }
    }

    /** Teleport a player back to their saved return position. */
    public void teleportBack(ServerPlayer player, MinecraftServer server) {
        ReturnInfo info = returnPositions.get(player.getUUID());
        if (info == null) {
            // Fallback: send to overworld spawn
            ServerLevel overworld = server.overworld();
            player.teleportTo(overworld,
                    overworld.getSharedSpawnPos().getX() + 0.5,
                    overworld.getSharedSpawnPos().getY(),
                    overworld.getSharedSpawnPos().getZ() + 0.5,
                    0, 0);
        } else {
            ServerLevel targetLevel = server.getLevel(info.dimension);
            if (targetLevel == null) targetLevel = server.overworld();
            player.teleportTo(targetLevel, info.x, info.y, info.z,
                    info.yRot, info.xRot);
            removeReturnPosition(player.getUUID());
        }

        // Effects
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    // =====================================================================
    // Inner types
    // =====================================================================

    public static class ReturnInfo {
        final ResourceKey<Level> dimension;
        final double x, y, z;
        final float yRot, xRot;

        ReturnInfo(ResourceKey<Level> dimension, double x, double y, double z,
                   float yRot, float xRot) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yRot = yRot;
            this.xRot = xRot;
        }
    }

    public static class PocketInstance {
        final ResourceKey<Level> dimension;
        final long expiryTick;

        PocketInstance(ResourceKey<Level> dimension, long expiryTick) {
            this.dimension = dimension;
            this.expiryTick = expiryTick;
        }
    }

    // =====================================================================
    // Serialization
    // =====================================================================

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("nextMansionSlot", nextMansionSlot);
        tag.putInt("nextGrottoSlot", nextGrottoSlot);

        // Save return positions
        ListTag returnList = new ListTag();
        for (Map.Entry<UUID, ReturnInfo> entry : returnPositions.entrySet()) {
            CompoundTag rt = new CompoundTag();
            rt.putUUID("uuid", entry.getKey());
            ReturnInfo info = entry.getValue();
            rt.putString("dim", info.dimension.location().toString());
            rt.putDouble("x", info.x);
            rt.putDouble("y", info.y);
            rt.putDouble("z", info.z);
            rt.putFloat("yRot", info.yRot);
            rt.putFloat("xRot", info.xRot);
            returnList.add(rt);
        }
        tag.put("returns", returnList);

        // Save active instances
        ListTag instanceList = new ListTag();
        for (PocketInstance inst : instances) {
            CompoundTag it = new CompoundTag();
            it.putString("dim", inst.dimension.location().toString());
            it.putLong("expiry", inst.expiryTick);
            instanceList.add(it);
        }
        tag.put("instances", instanceList);

        return tag;
    }

    public static PocketDimensionData load(CompoundTag tag, HolderLookup.Provider provider) {
        PocketDimensionData data = new PocketDimensionData();
        data.nextMansionSlot = tag.getInt("nextMansionSlot");
        data.nextGrottoSlot = tag.getInt("nextGrottoSlot");

        ListTag returnList = tag.getList("returns", 10);
        for (int i = 0; i < returnList.size(); i++) {
            CompoundTag rt = returnList.getCompound(i);
            UUID uuid = rt.getUUID("uuid");
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.parse(rt.getString("dim")));
            data.returnPositions.put(uuid, new ReturnInfo(dim,
                    rt.getDouble("x"), rt.getDouble("y"), rt.getDouble("z"),
                    rt.getFloat("yRot"), rt.getFloat("xRot")));
        }

        ListTag instanceList = tag.getList("instances", 10);
        for (int i = 0; i < instanceList.size(); i++) {
            CompoundTag it = instanceList.getCompound(i);
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.parse(it.getString("dim")));
            data.instances.add(new PocketInstance(dim, it.getLong("expiry")));
        }

        return data;
    }

    public static PocketDimensionData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PocketDimensionData::new, PocketDimensionData::load),
                DATA_NAME);
    }
}
