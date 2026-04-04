package com.reactivefluids.pinata.garden;

import com.reactivefluids.pinata.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Handles periodic garden ticking — scans blocks, spawns attracted piñatas.
 *
 * Called from ReactiveFluids.onServerTick every 200 ticks (~10 seconds).
 * For each garden:
 *   1. Re-scan block counts
 *   2. Check attraction conditions for each species
 *   3. Spawn visiting piñatas at the garden edge if conditions are met
 */
public class GardenTickHandler {

    /** Maximum piñatas per garden to prevent overload */
    private static final int MAX_PINATAS_PER_GARDEN = 30;

    /** Spawn chance per check per species (1/N) */
    private static final int SPAWN_CHANCE = 5; // 20% chance per 10-second check

    public static void tickGardens(ServerLevel level) {
        GardenManager manager = GardenManager.get(level);

        for (GardenManager.GardenData garden : manager.getAllGardens()) {
            // Scan blocks
            manager.scanGardenBlocks(level, garden);

            // Count current piñatas
            var currentPinatas = manager.getAllPinatasInGarden(level, garden);
            if (currentPinatas.size() >= MAX_PINATAS_PER_GARDEN) continue;

            // Check each species' attraction conditions
            for (AttractionRule rule : ATTRACTION_RULES) {
                if (level.random.nextInt(SPAWN_CHANCE) != 0) continue;
                if (rule.test(garden, currentPinatas)) {
                    spawnVisitor(level, garden, rule.entityType);
                }
            }
        }
    }

    private static void spawnVisitor(ServerLevel level, GardenManager.GardenData garden,
                                     Supplier<EntityType<?>> entityType) {
        // Spawn at a random edge position
        int side = level.random.nextInt(4);
        int r = garden.radius;
        BlockPos center = garden.center;
        int x, z;

        switch (side) {
            case 0 -> { x = center.getX() - r + level.random.nextInt(r * 2); z = center.getZ() - r; }
            case 1 -> { x = center.getX() - r + level.random.nextInt(r * 2); z = center.getZ() + r; }
            case 2 -> { x = center.getX() - r; z = center.getZ() - r + level.random.nextInt(r * 2); }
            default -> { x = center.getX() + r; z = center.getZ() - r + level.random.nextInt(r * 2); }
        }

        BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(x, 0, z));

        var entity = entityType.get().create(level, null, spawnPos, MobSpawnType.EVENT, false, false);
        if (entity instanceof BasePinataEntity pinata) {
            pinata.setLifecycle(BasePinataEntity.LIFECYCLE_VISITOR);
            pinata.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    level.random.nextFloat() * 360, 0);
            level.addFreshEntity(pinata);
        }
    }

    // --- Attraction Rules ---
    // Each rule defines what garden conditions attract a species

    private static final List<AttractionRule> ATTRACTION_RULES = new ArrayList<>();

    static {
        // Whirlm: 4+ grass blocks
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.WHIRLM.get(),
                (g, p) -> g.grassCount >= 4));

        // Sparrowmint: 1+ Whirlm resident
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.SPARROWMINT.get(),
                (g, p) -> p.stream().anyMatch(e -> e instanceof WhirlmEntity && e.isResident())));

        // Fudgehog: 10+ long grass
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.FUDGEHOG.get(),
                (g, p) -> g.longGrassCount >= 10));

        // Mousemallow: 4+ grass
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.MOUSEMALLOW.get(),
                (g, p) -> g.grassCount >= 4));

        // Syrupent: 6+ long grass
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.SYRUPENT.get(),
                (g, p) -> g.longGrassCount >= 6
                        && p.stream().anyMatch(e -> e instanceof MousemallowEntity)));

        // Taffly: 2+ flowers
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.TAFFLY.get(),
                (g, p) -> g.flowerCount >= 2));

        // Bunnycomb: 3+ flowers
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.BUNNYCOMB.get(),
                (g, p) -> g.flowerCount >= 3));

        // Quackberry: 4+ water
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.QUACKBERRY.get(),
                (g, p) -> g.waterCount >= 4));

        // Shellybean: 1+ flower
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.SHELLYBEAN.get(),
                (g, p) -> g.flowerCount >= 1));

        // Newtgat: water + grass
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.NEWTGAT.get(),
                (g, p) -> g.waterCount >= 2 && g.grassCount >= 4));

        // Lickatoad: 1+ Taffly resident
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.LICKATOAD.get(),
                (g, p) -> p.stream().anyMatch(e -> e instanceof TafflyEntity && e.isResident())));

        // Pretztail: 2+ Bunnycomb residents
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.PRETZTAIL.get(),
                (g, p) -> p.stream().filter(e -> e instanceof BunnycombEntity && e.isResident()).count() >= 2));

        // Buzzlegum: 4+ flowers
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.BUZZLEGUM.get(),
                (g, p) -> g.flowerCount >= 4));

        // Cluckles: 2+ grass
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.CLUCKLES.get(),
                (g, p) -> g.grassCount >= 2));

        // Horstachio: large garden, 20+ grass
        ATTRACTION_RULES.add(new AttractionRule(
                () -> ModPinataEntities.HORSTACHIO.get(),
                (g, p) -> g.grassCount >= 20 && g.level >= 3));
    }

    @FunctionalInterface
    interface AttractionTest {
        boolean test(GardenManager.GardenData garden, List<BasePinataEntity> currentPinatas);
    }

    static class AttractionRule {
        final Supplier<EntityType<?>> entityType;
        final AttractionTest condition;

        AttractionRule(Supplier<EntityType<?>> entityType, AttractionTest condition) {
            this.entityType = entityType;
            this.condition = condition;
        }

        boolean test(GardenManager.GardenData garden, List<BasePinataEntity> currentPinatas) {
            // Don't spawn if there are already 3+ of this species
            long existing = currentPinatas.stream()
                    .filter(p -> p.getType() == entityType.get()).count();
            if (existing >= 3) return false;
            return condition.test(garden, currentPinatas);
        }
    }
}
