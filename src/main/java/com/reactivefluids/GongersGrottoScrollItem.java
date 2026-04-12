package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

import java.util.List;

/**
 * 7th-level conjuration -- Gonger's Glorious Grotto.
 * Opens a portal to a pocket dimension containing a surreal floating island
 * with a rose quartz tower, a lush pond, friendly skeletons, crystal grotto,
 * garden paths, and enchanting/brewing facilities. Lasts one Minecraft day.
 */
public class GongersGrottoScrollItem extends Item {

    private static final int DURATION_TICKS = 24000;

    // Island platform dimensions
    private static final int ISLAND_RADIUS = 32;
    private static final int BASE_Y = 64;

    // Tower dimensions -- radius 7 for a 15-wide cylindrical tower
    private static final int TOWER_RADIUS = 7;
    private static final int TOWER_HEIGHT = 30;
    private static final int FLOOR_HEIGHT = 10; // each floor is 10 blocks tall
    private static final int TOWER_CX = 10;
    private static final int TOWER_CZ = 10;

    // Pond dimensions
    private static final int POND_CX = -12;
    private static final int POND_CZ = 10;
    private static final int POND_RADIUS = 8;

    // Garden / gazebo centre
    private static final int GARDEN_CX = 0;
    private static final int GARDEN_CZ = 4;

    // Grotto (waterfall cave) location -- north edge
    private static final int GROTTO_CX = 2;
    private static final int GROTTO_CZ = -24;

    // Well location
    private static final int WELL_CX = 8;
    private static final int WELL_CZ = -6;

    public GongersGrottoScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        MinecraftServer server = serverPlayer.server;
        ServerLevel grottoLevel = server.getLevel(ModDimensions.GROTTO);
        if (grottoLevel == null) return InteractionResult.FAIL;

        PocketDimensionData pocketData = PocketDimensionData.get(server);
        RandomSource random = level.getRandom();

        // Allocate a build slot
        int slot = pocketData.allocateGrottoSlot();
        int baseX = PocketDimensionData.slotToX(slot);
        BlockPos origin = new BlockPos(baseX, BASE_Y, 0);

        // Build the grotto in the pocket dimension
        buildIsland(grottoLevel, origin, random);
        buildTower(grottoLevel, origin, random);
        buildPond(grottoLevel, origin, random);
        buildEnchantingArea(grottoLevel, origin);
        buildBrewingArea(grottoLevel, origin);
        spawnFriendlySkeletons(grottoLevel, origin, random);
        addVegetation(grottoLevel, origin, random);

        // Place return portal near the island center
        BlockPos portalPos = origin.offset(0, 1, -2);
        grottoLevel.setBlock(portalPos, ModBlocks.RETURN_PORTAL.get().defaultBlockState(),
                Block.UPDATE_ALL);
        grottoLevel.setBlock(portalPos.above(), ModBlocks.RETURN_PORTAL.get().defaultBlockState(),
                Block.UPDATE_ALL);

        // Save return position and register expiry
        pocketData.saveReturnPosition(serverPlayer.getUUID(),
                serverPlayer.level().dimension(),
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                serverPlayer.getYRot(), serverPlayer.getXRot());

        long expiryTick = server.overworld().getGameTime() + DURATION_TICKS;
        pocketData.addInstance(ModDimensions.GROTTO, expiryTick);

        // Effects at departure
        ServerLevel departLevel = (ServerLevel) serverPlayer.level();
        departLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                serverPlayer.getX(), serverPlayer.getY() + 1, serverPlayer.getZ(),
                60, 0.5, 1.0, 0.5, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_EMERGE,
                SoundSource.PLAYERS, 2.0F, 0.7F);
        level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.5F, 0.8F);

        // Teleport to the island center
        double teleX = origin.getX() + 0.5;
        double teleY = origin.getY() + 1;
        double teleZ = origin.getZ() + 0.5;
        serverPlayer.teleportTo(grottoLevel, teleX, teleY, teleZ, 0, 0);

        // Consume scroll
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.gongers_grotto_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    // =====================================================================
    //  UTILITY: surface height lookup
    // =====================================================================

    /** Find the Y of the topmost non-air block at a given XZ on the island. */
    private static int surfaceY(ServerLevel level, int absX, int absZ) {
        for (int y = BASE_Y + 12; y >= BASE_Y - 8; y--) {
            BlockPos p = new BlockPos(absX, y, absZ);
            if (!level.getBlockState(p).isAir()) return y;
        }
        return BASE_Y;
    }

    /** Terrain height at a given dx/dz from origin, using sine-wave hills. */
    private static int terrainHeight(int dx, int dz, double dist) {
        if (dist >= ISLAND_RADIUS - 4) return 0;
        if (dist < 4) return 0; // flat near spawn for portal safety
        double wave = Math.sin(dx * 0.18) * Math.cos(dz * 0.18) * 2.5
                    + Math.sin(dx * 0.09 + 1.3) * Math.sin(dz * 0.11 + 0.7) * 1.8
                    + Math.cos(dx * 0.25 - dz * 0.15) * 0.8;
        return Math.max(0, (int) wave);
    }

    // =====================================================================
    //  ISLAND -- tapered stalactite shape, radius 32
    // =====================================================================

    private static void buildIsland(ServerLevel level, BlockPos origin, RandomSource random) {
        // Pre-calculate stream path (from near tower toward pond)
        int streamStartX = TOWER_CX - TOWER_RADIUS - 3;
        int streamStartZ = TOWER_CZ - 2;
        int streamEndX = POND_CX + POND_RADIUS + 2;
        int streamEndZ = POND_CZ - 2;

        for (int dx = -ISLAND_RADIUS; dx <= ISLAND_RADIUS; dx++) {
            for (int dz = -ISLAND_RADIUS; dz <= ISLAND_RADIUS; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > ISLAND_RADIUS) continue;

                int tHeight = terrainHeight(dx, dz, dist);
                BlockPos surfacePos = origin.offset(dx, tHeight, dz);

                // -- Surface block selection based on distance from center --
                BlockState surface;
                if (dist > ISLAND_RADIUS - 1.5) {
                    surface = Blocks.DIRT.defaultBlockState();
                } else if (dist > ISLAND_RADIUS - 4) {
                    surface = random.nextInt(3) == 0
                            ? Blocks.COARSE_DIRT.defaultBlockState()
                            : Blocks.DIRT.defaultBlockState();
                } else {
                    int roll = random.nextInt(20);
                    if (roll < 1) surface = Blocks.PODZOL.defaultBlockState();
                    else if (roll < 3) surface = Blocks.MOSS_BLOCK.defaultBlockState();
                    else if (roll < 4) surface = Blocks.COARSE_DIRT.defaultBlockState();
                    else if (roll < 5) surface = Blocks.ROOTED_DIRT.defaultBlockState();
                    else surface = Blocks.GRASS_BLOCK.defaultBlockState();
                }
                level.setBlock(surfacePos, surface, Block.UPDATE_ALL);

                // -- Subsurface layers: taper thickness based on distance from center --
                // Center gets full depth (8), edges get only 2-3
                int maxDepth = (int) Math.max(2, 8 - (dist / ISLAND_RADIUS) * 6);
                for (int depth = 1; depth <= maxDepth; depth++) {
                    BlockState layerBlock;
                    if (depth <= 2) {
                        layerBlock = Blocks.DIRT.defaultBlockState();
                    } else if (depth <= 4) {
                        layerBlock = Blocks.STONE.defaultBlockState();
                    } else if (depth <= 6) {
                        layerBlock = Blocks.DEEPSLATE.defaultBlockState();
                    } else {
                        layerBlock = random.nextInt(3) == 0
                                ? Blocks.DRIPSTONE_BLOCK.defaultBlockState()
                                : Blocks.DEEPSLATE.defaultBlockState();
                    }
                    level.setBlock(surfacePos.below(depth), layerBlock, Block.UPDATE_ALL);
                }

                // -- Clear air column above surface --
                for (int ay = 1; ay <= 40; ay++) {
                    level.setBlock(surfacePos.above(ay), Blocks.AIR.defaultBlockState(),
                            Block.UPDATE_ALL);
                }

                // -- Edge overhangs: 1-2 block extension beyond the main island --
                if (dist > ISLAND_RADIUS - 2.5 && dist <= ISLAND_RADIUS) {
                    addEdgeVines(level, surfacePos, dx, dz, random);

                    // Waterfall cascades: 3-4 water sources at the rim
                    if (random.nextInt(50) == 0) {
                        level.setBlock(surfacePos.above(1),
                                Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }

                // -- Underside stalactites and amethyst geode pockets --
                BlockPos undersideBase = surfacePos.below(maxDepth);
                if (dist < ISLAND_RADIUS - 3) {
                    // Pointed dripstone stalactites
                    if (random.nextInt(6) == 0) {
                        int hangLen = 1 + random.nextInt(5);
                        for (int h = 1; h <= hangLen; h++) {
                            BlockPos hangPos = undersideBase.below(h);
                            if (h == hangLen) {
                                level.setBlock(hangPos,
                                        Blocks.POINTED_DRIPSTONE.defaultBlockState()
                                                .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
                                                .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP),
                                        Block.UPDATE_ALL);
                            } else if (h == hangLen - 1 && hangLen > 2) {
                                level.setBlock(hangPos,
                                        Blocks.POINTED_DRIPSTONE.defaultBlockState()
                                                .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
                                                .setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.FRUSTUM),
                                        Block.UPDATE_ALL);
                            } else {
                                level.setBlock(hangPos,
                                        Blocks.DRIPSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                            }
                        }
                    }

                    // Amethyst clusters embedded in underside
                    if (random.nextInt(20) == 0) {
                        level.setBlock(undersideBase.below(1),
                                Blocks.AMETHYST_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                        level.setBlock(undersideBase.below(2),
                                Blocks.AMETHYST_CLUSTER.defaultBlockState()
                                        .setValue(AmethystClusterBlock.FACING, Direction.DOWN),
                                Block.UPDATE_ALL);
                    }

                    // Glow lichen on underside
                    if (random.nextInt(12) == 0) {
                        level.setBlock(undersideBase.below(1),
                                Blocks.GLOW_LICHEN.defaultBlockState()
                                        .setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true),
                                Block.UPDATE_ALL);
                    }
                }

                // -- Stream: 1-wide water trench from tower area to pond --
                if (isOnStream(dx, dz, streamStartX, streamStartZ, streamEndX, streamEndZ)) {
                    // Dig a 1-deep trench and fill with water
                    level.setBlock(surfacePos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                    level.setBlock(surfacePos.below(1), Blocks.CLAY.defaultBlockState(), Block.UPDATE_ALL);
                }

                // -- Half-buried mossy cobblestone boulders --
                if (dist > 6 && dist < ISLAND_RADIUS - 6 && random.nextInt(120) == 0) {
                    placeBoulder(level, surfacePos, random);
                }

                // -- Moss carpet on some surface areas near pond --
                if (random.nextInt(30) == 0 && dist < ISLAND_RADIUS - 4) {
                    double pondDist = Math.sqrt((dx - POND_CX) * (dx - POND_CX)
                            + (dz - POND_CZ) * (dz - POND_CZ));
                    if (pondDist < POND_RADIUS + 6) {
                        BlockPos above = surfacePos.above(1);
                        if (level.getBlockState(above).isAir()) {
                            level.setBlock(above, Blocks.MOSS_CARPET.defaultBlockState(),
                                    Block.UPDATE_ALL);
                        }
                    }
                }
            }
        }
    }

    /** Check if a point is on the stream path (within 1 block of the line). */
    private static boolean isOnStream(int dx, int dz, int x1, int z1, int x2, int z2) {
        double lineLen = Math.sqrt((x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1));
        if (lineLen == 0) return false;
        // Distance from point to line segment
        double t = Math.max(0, Math.min(1,
                ((dx - x1) * (x2 - x1) + (dz - z1) * (z2 - z1)) / (lineLen * lineLen)));
        double projX = x1 + t * (x2 - x1);
        double projZ = z1 + t * (z2 - z1);
        double distToLine = Math.sqrt((dx - projX) * (dx - projX) + (dz - projZ) * (dz - projZ));
        return distToLine < 0.8;
    }

    /** Place a 2-3 block mossy cobblestone boulder cluster with a slab on top. */
    private static void placeBoulder(ServerLevel level, BlockPos surfacePos, RandomSource random) {
        // Core block
        level.setBlock(surfacePos.above(1), Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
                Block.UPDATE_ALL);
        // 1-2 adjacent blocks
        if (random.nextBoolean()) {
            level.setBlock(surfacePos.above(1).east(), Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
                    Block.UPDATE_ALL);
        }
        if (random.nextBoolean()) {
            level.setBlock(surfacePos.above(1).north(), Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
                    Block.UPDATE_ALL);
        }
        // Slab on top
        level.setBlock(surfacePos.above(2),
                Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.BOTTOM),
                Block.UPDATE_ALL);
    }

    /** Place vine blocks on the side(s) of a surface edge block that face outward. */
    private static void addEdgeVines(ServerLevel level, BlockPos surfacePos,
                                     int dx, int dz, RandomSource random) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int nx = dx + dir.getStepX();
            int nz = dz + dir.getStepZ();
            double neighborDist = Math.sqrt(nx * nx + nz * nz);
            if (neighborDist > ISLAND_RADIUS) {
                int vineLen = 2 + random.nextInt(5);
                for (int v = 1; v <= vineLen; v++) {
                    BlockPos vinePos = surfacePos.below(v).relative(dir);
                    BlockState existing = level.getBlockState(vinePos);
                    if (existing.isAir()) {
                        BlockState vineState = Blocks.VINE.defaultBlockState();
                        Direction attach = dir.getOpposite();
                        BooleanProperty faceProp = getVineFaceProp(attach);
                        if (faceProp != null) {
                            vineState = vineState.setValue(faceProp, true);
                        }
                        level.setBlock(vinePos, vineState, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private static BooleanProperty getVineFaceProp(Direction dir) {
        return switch (dir) {
            case NORTH -> VineBlock.NORTH;
            case SOUTH -> VineBlock.SOUTH;
            case EAST  -> VineBlock.EAST;
            case WEST  -> VineBlock.WEST;
            default    -> null;
        };
    }

    // =====================================================================
    //  ROSE QUARTZ TOWER -- radius 7, height 30, three full floors
    //  Vertical stripe pattern walls, conical pink terracotta roof,
    //  arched doorway, spiral stairs, detailed interiors
    // =====================================================================

    private static void buildTower(ServerLevel level, BlockPos origin, RandomSource random) {
        // Find actual surface height at tower center for proper grounding
        int towerSurfaceY = surfaceY(level, origin.getX() + TOWER_CX, origin.getZ() + TOWER_CZ);
        BlockPos towerBase = new BlockPos(origin.getX() + TOWER_CX, towerSurfaceY, origin.getZ() + TOWER_CZ);

        // -- Wider base platform (R9) at the bottom 2 blocks --
        for (int dx = -9; dx <= 9; dx++) {
            for (int dz = -9; dz <= 9; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 9.5) continue;
                boolean isOuter = dist > 7.5;
                for (int h = 0; h <= 1; h++) {
                    BlockPos pos = towerBase.offset(dx, h, dz);
                    if (isOuter) {
                        // Stepped stairs on the outer ring
                        if (h == 0) {
                            level.setBlock(pos, Blocks.SMOOTH_QUARTZ.defaultBlockState(), Block.UPDATE_ALL);
                        } else {
                            level.setBlock(pos, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState()
                                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM), Block.UPDATE_ALL);
                        }
                    } else {
                        level.setBlock(pos, Blocks.SMOOTH_QUARTZ.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        // -- Main cylindrical shell: walls and floors for 3 storeys --
        for (int ly = 1; ly <= TOWER_HEIGHT; ly++) {
            int floorIndex = (ly - 1) / FLOOR_HEIGHT; // 0, 1, or 2
            int floorLocalY = (ly - 1) % FLOOR_HEIGHT; // 0-9 within each floor
            boolean isFloorSlab = (floorLocalY == 0);

            for (int dx = -TOWER_RADIUS; dx <= TOWER_RADIUS; dx++) {
                for (int dz = -TOWER_RADIUS; dz <= TOWER_RADIUS; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > TOWER_RADIUS + 0.5) continue;

                    boolean isWall = dist > TOWER_RADIUS - 1.2;
                    BlockPos pos = towerBase.offset(dx, ly + 1, dz);

                    if (isFloorSlab) {
                        if (isWall) {
                            level.setBlock(pos, pickWallBlock(dx, dz, ly), Block.UPDATE_ALL);
                        } else {
                            level.setBlock(pos, Blocks.CHERRY_PLANKS.defaultBlockState(),
                                    Block.UPDATE_ALL);
                        }
                    } else if (isWall) {
                        // Windows: 1x2 pink stained glass on cardinal directions every 3 rows
                        boolean isCardinal = (Math.abs(dx) <= 1 && Math.abs(dz) == TOWER_RADIUS)
                                || (Math.abs(dz) <= 1 && Math.abs(dx) == TOWER_RADIUS);
                        boolean isWindowRow = (floorLocalY == 3 || floorLocalY == 4
                                || floorLocalY == 7 || floorLocalY == 8);
                        if (isCardinal && isWindowRow && (dx == 0 || dz == 0)) {
                            level.setBlock(pos,
                                    Blocks.PINK_STAINED_GLASS_PANE.defaultBlockState(),
                                    Block.UPDATE_ALL);
                        } else {
                            // Cornice/belt: quartz stairs facing outward at every 3rd row
                            if (floorLocalY == 0 || floorLocalY == 9) {
                                // Belt detail row
                                Direction facing = getOutwardFacing(dx, dz);
                                if (facing != null && dist > TOWER_RADIUS - 0.8) {
                                    level.setBlock(pos,
                                            Blocks.QUARTZ_STAIRS.defaultBlockState()
                                                    .setValue(StairBlock.FACING, facing)
                                                    .setValue(StairBlock.HALF, Half.TOP),
                                            Block.UPDATE_ALL);
                                } else {
                                    level.setBlock(pos, pickWallBlock(dx, dz, ly), Block.UPDATE_ALL);
                                }
                            } else {
                                level.setBlock(pos, pickWallBlock(dx, dz, ly), Block.UPDATE_ALL);
                            }
                        }
                    } else {
                        // Interior air
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        // -- Ceiling slab above floor 3 --
        for (int dx = -TOWER_RADIUS; dx <= TOWER_RADIUS; dx++) {
            for (int dz = -TOWER_RADIUS; dz <= TOWER_RADIUS; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > TOWER_RADIUS + 0.5) continue;
                level.setBlock(towerBase.offset(dx, TOWER_HEIGHT + 2, dz),
                        Blocks.SMOOTH_QUARTZ.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        // -- Conical roof with pink terracotta + cut copper accent strips --
        for (int roofY = 0; roofY <= 9; roofY++) {
            int roofRadius = TOWER_RADIUS + 2 - roofY;
            if (roofRadius < 0) roofRadius = 0;
            for (int dx = -roofRadius; dx <= roofRadius; dx++) {
                for (int dz = -roofRadius; dz <= roofRadius; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > roofRadius + 0.5) continue;
                    BlockPos pos = towerBase.offset(dx, TOWER_HEIGHT + 3 + roofY, dz);
                    // Accent strips of exposed cut copper every 3rd row
                    boolean isAccent = (roofY % 3 == 1) && dist > roofRadius - 1.5;
                    BlockState roofBlock = isAccent
                            ? Blocks.EXPOSED_CUT_COPPER.defaultBlockState()
                            : Blocks.PINK_TERRACOTTA.defaultBlockState();
                    level.setBlock(pos, roofBlock, Block.UPDATE_ALL);
                }
            }
        }

        // -- Spire: end rod + sea lantern at the very tip --
        BlockPos spireBase = towerBase.above(TOWER_HEIGHT + 13);
        level.setBlock(spireBase, Blocks.END_ROD.defaultBlockState()
                .setValue(EndRodBlock.FACING, Direction.UP), Block.UPDATE_ALL);
        level.setBlock(spireBase.above(1), Blocks.SEA_LANTERN.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(spireBase.above(2), Blocks.END_ROD.defaultBlockState()
                .setValue(EndRodBlock.FACING, Direction.UP), Block.UPDATE_ALL);

        // -- Arched doorway (south face, ground level) --
        for (int dy = 2; dy <= 6; dy++) {
            for (int ddx = -1; ddx <= 1; ddx++) {
                level.setBlock(towerBase.offset(ddx, dy, -TOWER_RADIUS),
                        Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        // Quartz pillar frame
        for (int dy = 2; dy <= 6; dy++) {
            level.setBlock(towerBase.offset(-2, dy, -TOWER_RADIUS),
                    Blocks.QUARTZ_PILLAR.defaultBlockState()
                            .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y),
                    Block.UPDATE_ALL);
            level.setBlock(towerBase.offset(2, dy, -TOWER_RADIUS),
                    Blocks.QUARTZ_PILLAR.defaultBlockState()
                            .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y),
                    Block.UPDATE_ALL);
        }
        // Lintel above the door (arch shape)
        for (int ddx = -1; ddx <= 1; ddx++) {
            level.setBlock(towerBase.offset(ddx, 7, -TOWER_RADIUS),
                    Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        }

        // -- Spiral staircases: floor 1->2 and floor 2->3 --
        buildSpiralStairs(level, towerBase, 3, 12);  // ground to second
        buildSpiralStairs(level, towerBase, 13, 22); // second to third

        // -- Interior furnishing --
        buildTowerFloor1(level, towerBase);
        buildTowerFloor2(level, towerBase);
        buildTowerFloor3(level, towerBase);
    }

    /** Determine the outward-facing direction for a wall position. */
    private static Direction getOutwardFacing(int dx, int dz) {
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (Math.abs(dz) > Math.abs(dx)) {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return null;
    }

    /**
     * Wall block selection using vertical stripe pattern based on angular position.
     * 3-wide stripes of: smooth quartz, quartz bricks, quartz pillar, pink terracotta, cherry planks.
     */
    private static BlockState pickWallBlock(int dx, int dz, int ly) {
        // Compute angular stripe from atan2
        double angle = Math.atan2(dz, dx);
        int stripe = ((int) Math.floor((angle + Math.PI) / (Math.PI / 5))) % 5;
        return switch (stripe) {
            case 0 -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case 1 -> Blocks.QUARTZ_BRICKS.defaultBlockState();
            case 2 -> Blocks.QUARTZ_PILLAR.defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
            case 3 -> Blocks.PINK_TERRACOTTA.defaultBlockState();
            default -> Blocks.CHERRY_PLANKS.defaultBlockState();
        };
    }

    /** Spiral staircase along the inside of the tower wall. */
    private static void buildSpiralStairs(ServerLevel level, BlockPos towerBase,
                                           int startY, int endY) {
        // 16 positions around the inner wall ring at radius ~5
        int[][] offsets = {
            {-4, -4}, {-2, -5}, { 0, -5}, { 2, -5},
            { 4, -4}, { 5, -2}, { 5,  0}, { 5,  2},
            { 4,  4}, { 2,  5}, { 0,  5}, {-2,  5},
            {-4,  4}, {-5,  2}, {-5,  0}, {-5, -2}
        };
        int steps = endY - startY;
        for (int i = 0; i < steps; i++) {
            int ly = startY + i;
            int[] off = offsets[i % offsets.length];
            BlockPos stairPos = towerBase.offset(off[0], ly + 1, off[1]);
            // Determine facing based on circular direction
            int nextIdx = (i + 1) % offsets.length;
            Direction facing = Direction.SOUTH; // default
            int ddx = offsets[nextIdx][0] - off[0];
            int ddz = offsets[nextIdx][1] - off[1];
            if (Math.abs(ddx) > Math.abs(ddz)) {
                facing = ddx > 0 ? Direction.EAST : Direction.WEST;
            } else {
                facing = ddz > 0 ? Direction.SOUTH : Direction.NORTH;
            }
            level.setBlock(stairPos,
                    Blocks.QUARTZ_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, facing),
                    Block.UPDATE_ALL);
            // Clear air above each stair for head clearance
            level.setBlock(stairPos.above(1), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(stairPos.above(2), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** Ground floor: Grand entry hall with crafting, furnaces, chests, barrels, chandeliers. */
    private static void buildTowerFloor1(ServerLevel level, BlockPos towerBase) {
        int floorY = towerBase.getY() + 3; // standing level (above floor slab)

        // Pink carpet runner down the center aisle
        for (int dz = -5; dz <= 5; dz++) {
            BlockPos pos = new BlockPos(towerBase.getX(), floorY, towerBase.getZ() + dz);
            if (level.getBlockState(pos).isAir() || level.getBlockState(pos.below()).is(Blocks.CHERRY_PLANKS)) {
                level.setBlock(pos, Blocks.PINK_CARPET.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        // Crafting table
        level.setBlock(towerBase.offset(-4, 3, 4),
                Blocks.CRAFTING_TABLE.defaultBlockState(), Block.UPDATE_ALL);

        // Furnaces flanking the workbench
        level.setBlock(towerBase.offset(-4, 3, 3),
                Blocks.FURNACE.defaultBlockState()
                        .setValue(FurnaceBlock.FACING, Direction.EAST), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(-4, 3, 2),
                Blocks.FURNACE.defaultBlockState()
                        .setValue(FurnaceBlock.FACING, Direction.EAST), Block.UPDATE_ALL);

        // Storage chests
        level.setBlock(towerBase.offset(4, 3, 4),
                Blocks.CHEST.defaultBlockState()
                        .setValue(ChestBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(4, 3, 3),
                Blocks.CHEST.defaultBlockState()
                        .setValue(ChestBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(4, 3, 2),
                Blocks.CHEST.defaultBlockState()
                        .setValue(ChestBlock.FACING, Direction.WEST), Block.UPDATE_ALL);

        // Barrels for extra storage
        level.setBlock(towerBase.offset(4, 3, 0),
                Blocks.BARREL.defaultBlockState()
                        .setValue(BarrelBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(4, 3, -1),
                Blocks.BARREL.defaultBlockState()
                        .setValue(BarrelBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(-4, 3, 0),
                Blocks.BARREL.defaultBlockState()
                        .setValue(BarrelBlock.FACING, Direction.EAST), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(-4, 3, -1),
                Blocks.BARREL.defaultBlockState()
                        .setValue(BarrelBlock.FACING, Direction.EAST), Block.UPDATE_ALL);

        // Hanging chain+lantern chandeliers (4 chandeliers)
        int ceilY = 11; // relative to towerBase, ceiling of floor 1
        int[][] chandelierPositions = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
        for (int[] cp : chandelierPositions) {
            level.setBlock(towerBase.offset(cp[0], ceilY + 1, cp[1]),
                    Blocks.CHAIN.defaultBlockState()
                            .setValue(ChainBlock.AXIS, Direction.Axis.Y), Block.UPDATE_ALL);
            level.setBlock(towerBase.offset(cp[0], ceilY, cp[1]),
                    Blocks.CHAIN.defaultBlockState()
                            .setValue(ChainBlock.AXIS, Direction.Axis.Y), Block.UPDATE_ALL);
            level.setBlock(towerBase.offset(cp[0], ceilY - 1, cp[1]),
                    Blocks.LANTERN.defaultBlockState()
                            .setValue(LanternBlock.HANGING, true), Block.UPDATE_ALL);
        }
        // Central grand chandelier
        level.setBlock(towerBase.offset(0, ceilY + 1, 0),
                Blocks.CHAIN.defaultBlockState()
                        .setValue(ChainBlock.AXIS, Direction.Axis.Y), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(0, ceilY, 0),
                Blocks.CHAIN.defaultBlockState()
                        .setValue(ChainBlock.AXIS, Direction.Axis.Y), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(0, ceilY - 1, 0),
                Blocks.LANTERN.defaultBlockState()
                        .setValue(LanternBlock.HANGING, true), Block.UPDATE_ALL);
    }

    /** Second floor: Enchanting library with 15+ bookshelves, lectern, desk, purple carpet. */
    private static void buildTowerFloor2(ServerLevel level, BlockPos towerBase) {
        int y = 13; // standing level on floor 2 (above floor slab at 12)

        // Enchanting table in the centre
        level.setBlock(towerBase.offset(0, y, 0),
                Blocks.ENCHANTING_TABLE.defaultBlockState(), Block.UPDATE_ALL);

        // Ring of bookshelves at distance 2 (full enchanting power)
        int[][] shelfPos = {
            {-2, -2}, {-1, -2}, {0, -2}, {1, -2}, {2, -2},
            {-2, -1},                               {2, -1},
            {-2,  0},                               {2,  0},
            {-2,  1},                               {2,  1},
            {-2,  2}, {-1,  2}, {0,  2}, {1,  2}, {2,  2}
        };
        for (int[] sp : shelfPos) {
            // Double-stacked bookshelves for tall library feel
            level.setBlock(towerBase.offset(sp[0], y, sp[1]),
                    Blocks.BOOKSHELF.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(towerBase.offset(sp[0], y + 1, sp[1]),
                    Blocks.BOOKSHELF.defaultBlockState(), Block.UPDATE_ALL);
        }

        // Purple carpet around the enchanting table
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                level.setBlock(towerBase.offset(dx, y, dz),
                        Blocks.PURPLE_CARPET.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        // Lectern
        level.setBlock(towerBase.offset(4, y, -4),
                Blocks.LECTERN.defaultBlockState()
                        .setValue(LecternBlock.FACING, Direction.WEST), Block.UPDATE_ALL);

        // Desk: fence post + pressure plate
        level.setBlock(towerBase.offset(-4, y, -4),
                Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(-4, y + 1, -4),
                Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), Block.UPDATE_ALL);

        // Additional bookshelves along outer walls
        for (int dx = -5; dx <= 5; dx += 2) {
            if (Math.abs(dx) < 3) continue;
            level.setBlock(towerBase.offset(dx, y, 4),
                    Blocks.BOOKSHELF.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(towerBase.offset(dx, y + 1, 4),
                    Blocks.BOOKSHELF.defaultBlockState(), Block.UPDATE_ALL);
        }

        // Soul lanterns hanging from ceiling
        int ceilY = 21;
        int[][] lanternPos = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] lp : lanternPos) {
            level.setBlock(towerBase.offset(lp[0], ceilY, lp[1]),
                    Blocks.CHAIN.defaultBlockState()
                            .setValue(ChainBlock.AXIS, Direction.Axis.Y), Block.UPDATE_ALL);
            level.setBlock(towerBase.offset(lp[0], ceilY - 1, lp[1]),
                    Blocks.SOUL_LANTERN.defaultBlockState()
                            .setValue(LanternBlock.HANGING, true), Block.UPDATE_ALL);
        }
    }

    /** Third floor / Observatory: bed, brewing, cauldrons, telescope, balcony. */
    private static void buildTowerFloor3(ServerLevel level, BlockPos towerBase) {
        int y = 23; // standing level on floor 3

        // Carpet across interior
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 5.5 || d < 0.5) continue;
                BlockPos floorPos = towerBase.offset(dx, y - 1, dz);
                if (level.getBlockState(floorPos).is(Blocks.CHERRY_PLANKS)) {
                    level.setBlock(towerBase.offset(dx, y, dz),
                            Blocks.PINK_CARPET.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        // Bed (facing north)
        level.setBlock(towerBase.offset(-4, y, 3),
                Blocks.WHITE_BED.defaultBlockState()
                        .setValue(BedBlock.FACING, Direction.NORTH)
                        .setValue(BedBlock.PART, BedPart.FOOT), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(-4, y, 2),
                Blocks.WHITE_BED.defaultBlockState()
                        .setValue(BedBlock.FACING, Direction.NORTH)
                        .setValue(BedBlock.PART, BedPart.HEAD), Block.UPDATE_ALL);

        // Brewing station: 2 brewing stands
        level.setBlock(towerBase.offset(4, y, 4),
                Blocks.BREWING_STAND.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(4, y, 3),
                Blocks.BREWING_STAND.defaultBlockState(), Block.UPDATE_ALL);

        // 2 cauldrons (water, level 3)
        level.setBlock(towerBase.offset(4, y, 2),
                Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, 3), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(3, y, 4),
                Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, 3), Block.UPDATE_ALL);

        // Barrels for ingredient storage
        level.setBlock(towerBase.offset(3, y, 3),
                Blocks.BARREL.defaultBlockState()
                        .setValue(BarrelBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(3, y, 2),
                Blocks.BARREL.defaultBlockState()
                        .setValue(BarrelBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);

        // End rod "telescope" (3 end rods stacked vertically pointing up)
        for (int h = 0; h < 3; h++) {
            level.setBlock(towerBase.offset(0, y + h, 0),
                    Blocks.END_ROD.defaultBlockState()
                            .setValue(EndRodBlock.FACING, Direction.UP), Block.UPDATE_ALL);
        }

        // Lanterns + carpet accents
        int ceilY = 31;
        int[][] lanternPos = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}};
        for (int[] lp : lanternPos) {
            level.setBlock(towerBase.offset(lp[0], ceilY, lp[1]),
                    Blocks.LANTERN.defaultBlockState()
                            .setValue(LanternBlock.HANGING, true), Block.UPDATE_ALL);
        }

        // -- Balcony: cut into north wall of floor 3 --
        for (int ddx = -2; ddx <= 2; ddx++) {
            for (int ddy = 0; ddy <= 3; ddy++) {
                level.setBlock(towerBase.offset(ddx, y + ddy, -TOWER_RADIUS),
                        Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        // Balcony floor extends 2 blocks outward
        for (int ddx = -3; ddx <= 3; ddx++) {
            for (int ddz = 1; ddz <= 2; ddz++) {
                level.setBlock(towerBase.offset(ddx, y - 1, -TOWER_RADIUS - ddz),
                        Blocks.QUARTZ_SLAB.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        // Balcony railing
        for (int ddx = -3; ddx <= 3; ddx++) {
            level.setBlock(towerBase.offset(ddx, y, -TOWER_RADIUS - 2),
                    Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.setBlock(towerBase.offset(-3, y, -TOWER_RADIUS - 1),
                Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(towerBase.offset(3, y, -TOWER_RADIUS - 1),
                Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
        // Lantern on balcony
        level.setBlock(towerBase.offset(0, y, -TOWER_RADIUS - 2),
                Blocks.LANTERN.defaultBlockState()
                        .setValue(LanternBlock.HANGING, false), Block.UPDATE_ALL);
    }

    // =====================================================================
    //  POND -- organic irregular shape, 2-3 blocks deep, dock, waterfall
    // =====================================================================

    private static void buildPond(ServerLevel level, BlockPos origin, RandomSource random) {
        int pondSurfaceY = surfaceY(level, origin.getX() + POND_CX, origin.getZ() + POND_CZ);
        BlockPos pondCenter = new BlockPos(origin.getX() + POND_CX, pondSurfaceY, origin.getZ() + POND_CZ);

        for (int dx = -POND_RADIUS - 1; dx <= POND_RADIUS + 1; dx++) {
            for (int dz = -POND_RADIUS - 1; dz <= POND_RADIUS + 1; dz++) {
                // Organic irregular shape using sine-wave radius offset
                double angle = Math.atan2(dz, dx);
                double organicR = POND_RADIUS
                        - 1.2 * Math.sin(angle * 3.0 + 1.0)
                        - 0.6 * Math.cos(angle * 5.0 + 2.5)
                        + 0.4 * Math.sin(angle * 7.0);
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > organicR + 0.5) continue;

                boolean isBorder = dist > organicR - 1.8;
                BlockPos pos = pondCenter.offset(dx, 0, dz);

                if (isBorder) {
                    // Natural bank: mossy cobblestone stairs waterlogged, moss blocks
                    if (random.nextInt(3) == 0) {
                        level.setBlock(pos, Blocks.MOSS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                    } else {
                        Direction facing = getOutwardFacing(dx, dz);
                        if (facing == null) facing = Direction.NORTH;
                        level.setBlock(pos,
                                Blocks.MOSSY_COBBLESTONE_STAIRS.defaultBlockState()
                                        .setValue(StairBlock.FACING, facing)
                                        .setValue(StairBlock.WATERLOGGED, true),
                                Block.UPDATE_ALL);
                    }
                } else {
                    // Dig pond: 2-3 blocks deep
                    int depth = (dist < organicR * 0.5) ? 3 : 2;

                    // Bottom materials: clay, mud, sand in patches
                    BlockState bottomBlock;
                    int bottomRoll = (Math.abs(dx * 7 + dz * 13)) % 4;
                    bottomBlock = switch (bottomRoll) {
                        case 0 -> Blocks.CLAY.defaultBlockState();
                        case 1 -> Blocks.MUD.defaultBlockState();
                        case 2 -> Blocks.SAND.defaultBlockState();
                        default -> Blocks.CLAY.defaultBlockState();
                    };
                    level.setBlock(pos.below(depth), bottomBlock, Block.UPDATE_ALL);

                    // Sea lanterns scattered under the water for ethereal glow
                    if (random.nextInt(8) == 0) {
                        level.setBlock(pos.below(depth), Blocks.SEA_LANTERN.defaultBlockState(),
                                Block.UPDATE_ALL);
                    }

                    // Fill water
                    for (int wd = depth - 1; wd >= 0; wd--) {
                        level.setBlock(pos.below(wd), Blocks.WATER.defaultBlockState(),
                                Block.UPDATE_ALL);
                    }

                    // Clear air above
                    level.setBlock(pos.above(1), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

                    // Surface decorations: lily pads, big dripleaf
                    if (random.nextInt(8) == 0) {
                        level.setBlock(pos.above(1), Blocks.LILY_PAD.defaultBlockState(),
                                Block.UPDATE_ALL);
                    } else if (random.nextInt(15) == 0) {
                        level.setBlock(pos.above(1),
                                Blocks.BIG_DRIPLEAF.defaultBlockState()
                                        .setValue(BigDripleafBlock.FACING, Direction.NORTH),
                                Block.UPDATE_ALL);
                    }
                }
            }
        }

        // -- Sugar cane at the north edge --
        for (int i = 0; i < 6; i++) {
            int ddx = -3 + i;
            BlockPos caneBase = pondCenter.offset(ddx, 1, -POND_RADIUS + 1);
            if (!level.getBlockState(caneBase).isAir()) continue;
            BlockState below = level.getBlockState(caneBase.below());
            if (below.is(Blocks.MOSS_BLOCK) || below.is(Blocks.SAND) || below.is(Blocks.CLAY)
                    || below.is(Blocks.DIRT) || below.is(Blocks.GRASS_BLOCK)) {
                int height = 1 + random.nextInt(3);
                for (int h = 0; h < height; h++) {
                    level.setBlock(caneBase.above(h),
                            Blocks.SUGAR_CANE.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        // -- Wooden dock: cherry planks on fence post pilings, L-shaped --
        buildDock(level, pondCenter, random);

        // -- Waterfall corner: mossy cobblestone stack with water on top --
        BlockPos waterfallBase = pondCenter.offset(POND_RADIUS - 2, 0, POND_RADIUS - 2);
        for (int h = 0; h <= 5; h++) {
            level.setBlock(waterfallBase.above(h),
                    Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
            if (h < 3) {
                level.setBlock(waterfallBase.offset(1, h, 0),
                        Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(waterfallBase.offset(0, h, 1),
                        Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        // Water source on top flowing into pond
        level.setBlock(waterfallBase.above(6), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(waterfallBase.offset(1, 4, 0),
                Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
    }

    /** Cherry plank dock on fence post pilings, L-shaped, with trapdoors on sides. */
    private static void buildDock(ServerLevel level, BlockPos pondCenter, RandomSource random) {
        // Main arm running east into the pond
        int dockX = POND_RADIUS;
        for (int step = 0; step < 6; step++) {
            BlockPos plankPos = pondCenter.offset(dockX - step, 0, -2);
            // Fence post pilings underneath
            level.setBlock(plankPos.below(1), Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(plankPos.below(2), Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
            // Cherry plank deck (3 wide)
            for (int w = -1; w <= 1; w++) {
                level.setBlock(plankPos.offset(0, 0, w),
                        Blocks.CHERRY_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
            }
            // Trapdoors on the sides for detail
            if (step > 0 && step < 5) {
                level.setBlock(plankPos.offset(0, 0, -2),
                        Blocks.CHERRY_TRAPDOOR.defaultBlockState()
                                .setValue(TrapDoorBlock.FACING, Direction.SOUTH)
                                .setValue(TrapDoorBlock.OPEN, true),
                        Block.UPDATE_ALL);
                level.setBlock(plankPos.offset(0, 0, 2),
                        Blocks.CHERRY_TRAPDOOR.defaultBlockState()
                                .setValue(TrapDoorBlock.FACING, Direction.NORTH)
                                .setValue(TrapDoorBlock.OPEN, true),
                        Block.UPDATE_ALL);
            }
        }
        // L-shaped turn
        for (int step = 0; step < 3; step++) {
            BlockPos turnPos = pondCenter.offset(dockX - 5, 0, -2 + step);
            for (int w = -1; w <= 0; w++) {
                level.setBlock(turnPos.offset(w, 0, 0),
                        Blocks.CHERRY_PLANKS.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        // Barrel and lantern at the end of the dock
        level.setBlock(pondCenter.offset(dockX, 1, -2),
                Blocks.BARREL.defaultBlockState()
                        .setValue(BarrelBlock.FACING, Direction.UP), Block.UPDATE_ALL);
        level.setBlock(pondCenter.offset(dockX, 1, -1),
                Blocks.LANTERN.defaultBlockState()
                        .setValue(LanternBlock.HANGING, false), Block.UPDATE_ALL);
    }

    // =====================================================================
    //  ENCHANTING AREA -- secondary outdoor nook in the garden
    // =====================================================================

    private static void buildEnchantingArea(ServerLevel level, BlockPos origin) {
        // Small garden enchanting accent (main enchanting is tower floor 2)
        BlockPos center = origin.offset(GARDEN_CX - 8, 1, GARDEN_CZ - 8);
        level.setBlock(center, Blocks.ENCHANTING_TABLE.defaultBlockState(), Block.UPDATE_ALL);
        // Minimal bookshelf ring
        for (int[] off : new int[][]{{-2, 0}, {2, 0}, {0, -2}, {0, 2}}) {
            level.setBlock(center.offset(off[0], 0, off[1]),
                    Blocks.BOOKSHELF.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.setBlock(center.above(1),
                Blocks.END_ROD.defaultBlockState()
                        .setValue(EndRodBlock.FACING, Direction.UP), Block.UPDATE_ALL);
    }

    // =====================================================================
    //  BREWING AREA -- builds garden structures, grotto, paths, well
    // =====================================================================

    private static void buildBrewingArea(ServerLevel level, BlockPos origin) {
        buildGardenGazebo(level, origin);
        buildWaterfallGrotto(level, origin);
        buildGardenPaths(level, origin);
        buildWell(level, origin);
        buildFlowerBeds(level, origin);
    }

    /** Quartz gazebo/shrine: 5x5 footprint, quartz pillars, checkerboard floor. */
    private static void buildGardenGazebo(ServerLevel level, BlockPos origin) {
        int gy = surfaceY(level, origin.getX() + GARDEN_CX, origin.getZ() + GARDEN_CZ) + 1;
        BlockPos gc = new BlockPos(origin.getX() + GARDEN_CX, gy, origin.getZ() + GARDEN_CZ);

        // Checkerboard floor: cherry planks + smooth quartz
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockState floorBlock = ((dx + dz) % 2 == 0)
                        ? Blocks.CHERRY_PLANKS.defaultBlockState()
                        : Blocks.SMOOTH_QUARTZ.defaultBlockState();
                level.setBlock(gc.offset(dx, 0, dz), floorBlock, Block.UPDATE_ALL);
            }
        }

        // Four quartz pillars at corners, 3 high
        int[][] corners = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        for (int[] c : corners) {
            for (int h = 1; h <= 3; h++) {
                level.setBlock(gc.offset(c[0], h, c[1]),
                        Blocks.QUARTZ_PILLAR.defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y),
                        Block.UPDATE_ALL);
            }
        }

        // Roof: quartz slabs with exposed copper stair trim
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean isEdge = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                if (isEdge) {
                    Direction facing = Direction.NORTH;
                    if (dz == 2) facing = Direction.NORTH;
                    else if (dz == -2) facing = Direction.SOUTH;
                    else if (dx == 2) facing = Direction.WEST;
                    else if (dx == -2) facing = Direction.EAST;
                    level.setBlock(gc.offset(dx, 4, dz),
                            Blocks.EXPOSED_CUT_COPPER_STAIRS.defaultBlockState()
                                    .setValue(StairBlock.FACING, facing)
                                    .setValue(StairBlock.HALF, Half.TOP),
                            Block.UPDATE_ALL);
                } else {
                    level.setBlock(gc.offset(dx, 4, dz),
                            Blocks.QUARTZ_SLAB.defaultBlockState()
                                    .setValue(SlabBlock.TYPE, SlabType.TOP),
                            Block.UPDATE_ALL);
                }
            }
        }

        // End rod on roof peak
        level.setBlock(gc.offset(0, 5, 0),
                Blocks.END_ROD.defaultBlockState()
                        .setValue(EndRodBlock.FACING, Direction.UP), Block.UPDATE_ALL);

        // Oak stair benches on open sides
        level.setBlock(gc.offset(0, 1, -2),
                Blocks.OAK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        level.setBlock(gc.offset(0, 1, 2),
                Blocks.OAK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
        level.setBlock(gc.offset(-2, 1, 0),
                Blocks.OAK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.EAST), Block.UPDATE_ALL);
        level.setBlock(gc.offset(2, 1, 0),
                Blocks.OAK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.WEST), Block.UPDATE_ALL);

        // Central amethyst cluster as focal point
        level.setBlock(gc.offset(0, 1, 0),
                Blocks.AMETHYST_CLUSTER.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, Direction.UP), Block.UPDATE_ALL);
    }

    /** Waterfall Grotto: 7-wide x 5-deep crystal cave cut into the north edge. */
    private static void buildWaterfallGrotto(ServerLevel level, BlockPos origin) {
        int grottoY = surfaceY(level, origin.getX() + GROTTO_CX, origin.getZ() + GROTTO_CZ);
        BlockPos gc = new BlockPos(origin.getX() + GROTTO_CX, grottoY, origin.getZ() + GROTTO_CZ);

        // Hollow out the cave: 7 wide, 5 deep, 4 tall
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = 1; dy <= 4; dy++) {
                for (int dz = 0; dz <= 5; dz++) {
                    level.setBlock(gc.offset(dx, dy, dz),
                            Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        // Floor: calcite + smooth basalt checkerboard
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = 0; dz <= 5; dz++) {
                BlockState floorBlock = ((dx + dz) % 2 == 0)
                        ? Blocks.CALCITE.defaultBlockState()
                        : Blocks.SMOOTH_BASALT.defaultBlockState();
                level.setBlock(gc.offset(dx, 0, dz), floorBlock, Block.UPDATE_ALL);
            }
        }

        // Ceiling: smooth basalt with glow lichen + hanging chains
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = 0; dz <= 5; dz++) {
                level.setBlock(gc.offset(dx, 5, dz),
                        Blocks.SMOOTH_BASALT.defaultBlockState(), Block.UPDATE_ALL);
                // Glow lichen on ceiling
                if ((dx + dz) % 3 == 0) {
                    level.setBlock(gc.offset(dx, 4, dz),
                            Blocks.GLOW_LICHEN.defaultBlockState()
                                    .setValue(MultifaceBlock.getFaceProperty(Direction.UP), true),
                            Block.UPDATE_ALL);
                }
            }
        }
        // Hanging chains from ceiling
        level.setBlock(gc.offset(-2, 4, 2),
                Blocks.CHAIN.defaultBlockState()
                        .setValue(ChainBlock.AXIS, Direction.Axis.Y), Block.UPDATE_ALL);
        level.setBlock(gc.offset(2, 4, 3),
                Blocks.CHAIN.defaultBlockState()
                        .setValue(ChainBlock.AXIS, Direction.Axis.Y), Block.UPDATE_ALL);
        level.setBlock(gc.offset(0, 4, 4),
                Blocks.CHAIN.defaultBlockState()
                        .setValue(ChainBlock.AXIS, Direction.Axis.Y), Block.UPDATE_ALL);

        // Walls: amethyst blocks + calcite
        for (int dy = 1; dy <= 4; dy++) {
            for (int dz = 0; dz <= 5; dz++) {
                BlockState wallBlock = (dy % 2 == 0)
                        ? Blocks.AMETHYST_BLOCK.defaultBlockState()
                        : Blocks.CALCITE.defaultBlockState();
                level.setBlock(gc.offset(-4, dy, dz), wallBlock, Block.UPDATE_ALL);
                level.setBlock(gc.offset(4, dy, dz), wallBlock, Block.UPDATE_ALL);
            }
        }
        // Back wall
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = 1; dy <= 4; dy++) {
                BlockState wallBlock = (dy % 2 == 0)
                        ? Blocks.CALCITE.defaultBlockState()
                        : Blocks.AMETHYST_BLOCK.defaultBlockState();
                level.setBlock(gc.offset(dx, dy, 5), wallBlock, Block.UPDATE_ALL);
            }
        }

        // Amethyst clusters on walls (small, medium, large)
        level.setBlock(gc.offset(-3, 2, 2),
                Blocks.SMALL_AMETHYST_BUD.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, Direction.EAST), Block.UPDATE_ALL);
        level.setBlock(gc.offset(3, 2, 3),
                Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        level.setBlock(gc.offset(-2, 3, 5),
                Blocks.LARGE_AMETHYST_BUD.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        level.setBlock(gc.offset(1, 2, 5),
                Blocks.AMETHYST_CLUSTER.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        level.setBlock(gc.offset(0, 1, 4),
                Blocks.AMETHYST_CLUSTER.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, Direction.UP), Block.UPDATE_ALL);

        // Soul campfire for meditation space
        level.setBlock(gc.offset(0, 1, 3),
                Blocks.SOUL_CAMPFIRE.defaultBlockState()
                        .setValue(CampfireBlock.LIT, true), Block.UPDATE_ALL);

        // Pink carpet meditation circle
        for (int[] cp : new int[][]{{-1, 3}, {1, 3}, {0, 2}, {0, 4}, {-1, 2}, {1, 2}}) {
            level.setBlock(gc.offset(cp[0], 1, cp[1]),
                    Blocks.PINK_CARPET.defaultBlockState(), Block.UPDATE_ALL);
        }

        // End rod accent lighting
        level.setBlock(gc.offset(-3, 1, 0),
                Blocks.END_ROD.defaultBlockState()
                        .setValue(EndRodBlock.FACING, Direction.UP), Block.UPDATE_ALL);
        level.setBlock(gc.offset(3, 1, 0),
                Blocks.END_ROD.defaultBlockState()
                        .setValue(EndRodBlock.FACING, Direction.UP), Block.UPDATE_ALL);

        // Small pool inside (1 block deep, sea lantern underneath)
        level.setBlock(gc.offset(-2, 0, 1), Blocks.SEA_LANTERN.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(gc.offset(-2, 1, 1), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(gc.offset(-1, 0, 1), Blocks.SEA_LANTERN.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(gc.offset(-1, 1, 1), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);

        // Water cascading over the entrance from source blocks above
        for (int ddx = -2; ddx <= 2; ddx++) {
            level.setBlock(gc.offset(ddx, 5, 0),
                    Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** Garden paths: 2-wide dirt path with coarse dirt/gravel edges, soul lantern fence posts. */
    private static void buildGardenPaths(ServerLevel level, BlockPos origin) {
        // Path from portal/spawn toward the tower
        placePath(level, origin, 0, 0, TOWER_CX - TOWER_RADIUS - 2, TOWER_CZ);
        // Path from spawn toward the pond
        placePath(level, origin, 0, 0, POND_CX + POND_RADIUS + 2, POND_CZ);
        // Path from spawn to gazebo
        placePath(level, origin, 0, 0, GARDEN_CX, GARDEN_CZ);
        // Path from gazebo toward grotto
        placePath(level, origin, GARDEN_CX, GARDEN_CZ, GROTTO_CX, GROTTO_CZ + 6);
        // Path from gazebo to well
        placePath(level, origin, GARDEN_CX, GARDEN_CZ, WELL_CX, WELL_CZ);

        // Soul lantern fence posts every 8 blocks along main paths
        placeLanternsAlongPath(level, origin, 0, 0, TOWER_CX - TOWER_RADIUS - 2, TOWER_CZ, 8);
        placeLanternsAlongPath(level, origin, 0, 0, POND_CX + POND_RADIUS + 2, POND_CZ, 8);
        placeLanternsAlongPath(level, origin, GARDEN_CX, GARDEN_CZ, GROTTO_CX, GROTTO_CZ + 6, 8);

        // Note blocks in the garden area
        for (int[] nb : new int[][]{{3, -3}, {-3, 5}, {6, 6}, {-6, -2}}) {
            BlockPos nbPos = origin.offset(GARDEN_CX + nb[0], 1, GARDEN_CZ + nb[1]);
            if (level.getBlockState(nbPos).isAir()) {
                level.setBlock(nbPos, Blocks.NOTE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        // Sculk sensors scattered for ambient atmosphere (5-6 total)
        for (int[] ss : new int[][]{{10, -10}, {-10, 10}, {18, -6}, {-16, 14}, {-6, -18}, {20, 12}}) {
            double d = Math.sqrt(ss[0] * ss[0] + ss[1] * ss[1]);
            if (d > ISLAND_RADIUS - 3) continue;
            BlockPos ssPos = origin.offset(ss[0], 1, ss[1]);
            if (level.getBlockState(ssPos).isAir()) {
                level.setBlock(ssPos, Blocks.SCULK_SENSOR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        // Benches at scenic spots (oak stairs facing outward at island edges)
        int[][] benchSpots = {{-20, 0}, {0, -20}, {20, 0}, {0, 20}, {-14, -14}};
        Direction[] benchFacings = {Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int i = 0; i < benchSpots.length; i++) {
            double d = Math.sqrt(benchSpots[i][0] * benchSpots[i][0]
                    + benchSpots[i][1] * benchSpots[i][1]);
            if (d > ISLAND_RADIUS - 4) continue;
            BlockPos bp = origin.offset(benchSpots[i][0], 1, benchSpots[i][1]);
            if (level.getBlockState(bp).isAir() && !level.getBlockState(bp.below()).isAir()) {
                level.setBlock(bp, Blocks.OAK_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, benchFacings[i]), Block.UPDATE_ALL);
            }
        }
    }

    /** Lays a 2-wide dirt path between two island-relative points. */
    private static void placePath(ServerLevel level, BlockPos origin,
                                   int x1, int z1, int x2, int z2) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(z2 - z1));
        if (steps == 0) return;

        // Calculate perpendicular direction for 2-wide path
        double dx = x2 - x1;
        double dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dz * dz);
        int perpX = (int) Math.round(-dz / len);
        int perpZ = (int) Math.round(dx / len);

        for (int i = 0; i <= steps; i++) {
            int px = x1 + Math.round((float) (x2 - x1) * i / steps);
            int pz = z1 + Math.round((float) (z2 - z1) * i / steps);

            for (int w = 0; w <= 1; w++) {
                int pathX = px + perpX * w;
                int pathZ = pz + perpZ * w;
                for (int scanY = BASE_Y + 8; scanY >= BASE_Y - 1; scanY--) {
                    BlockPos scanPos = new BlockPos(origin.getX() + pathX, scanY, origin.getZ() + pathZ);
                    BlockState below = level.getBlockState(scanPos.below());
                    if (!below.isAir() && level.getBlockState(scanPos).isAir()) {
                        // Main path: dirt path blocks
                        level.setBlock(scanPos.below(), Blocks.DIRT_PATH.defaultBlockState(),
                                Block.UPDATE_ALL);
                        break;
                    }
                }
            }
            // Random edge decoration (gravel/coarse dirt) on one side
            if (i % 3 == 0) {
                int edgeX = px + perpX * 2;
                int edgeZ = pz + perpZ * 2;
                for (int scanY = BASE_Y + 8; scanY >= BASE_Y - 1; scanY--) {
                    BlockPos scanPos = new BlockPos(origin.getX() + edgeX, scanY, origin.getZ() + edgeZ);
                    BlockState below = level.getBlockState(scanPos.below());
                    if (!below.isAir() && level.getBlockState(scanPos).isAir()) {
                        BlockState edgeBlock = (i % 6 == 0)
                                ? Blocks.GRAVEL.defaultBlockState()
                                : Blocks.COARSE_DIRT.defaultBlockState();
                        level.setBlock(scanPos.below(), edgeBlock, Block.UPDATE_ALL);
                        break;
                    }
                }
            }
            // Occasional mossy cobblestone slab inset
            if (i % 7 == 0) {
                for (int scanY = BASE_Y + 8; scanY >= BASE_Y - 1; scanY--) {
                    BlockPos scanPos = new BlockPos(origin.getX() + px, scanY, origin.getZ() + pz);
                    if (!level.getBlockState(scanPos.below()).isAir()
                            && level.getBlockState(scanPos).isAir()) {
                        level.setBlock(scanPos,
                                Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState()
                                        .setValue(SlabBlock.TYPE, SlabType.BOTTOM),
                                Block.UPDATE_ALL);
                        break;
                    }
                }
            }
        }
    }

    /** Place soul lanterns on fence posts every `interval` blocks along a path. */
    private static void placeLanternsAlongPath(ServerLevel level, BlockPos origin,
                                                int x1, int z1, int x2, int z2,
                                                int interval) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(z2 - z1));
        if (steps == 0) return;
        double dx = x2 - x1;
        double dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dz * dz);
        int perpX = (int) Math.round(-dz / len);
        int perpZ = (int) Math.round(dx / len);

        for (int i = 0; i <= steps; i += interval) {
            int px = x1 + Math.round((float) (x2 - x1) * i / steps);
            int pz = z1 + Math.round((float) (z2 - z1) * i / steps);
            // Offset to the side of the path
            int offX = px + perpX * 2;
            int offZ = pz + perpZ * 2;
            for (int scanY = BASE_Y + 8; scanY >= BASE_Y - 1; scanY--) {
                BlockPos scanPos = new BlockPos(origin.getX() + offX, scanY, origin.getZ() + offZ);
                if (!level.getBlockState(scanPos.below()).isAir()
                        && level.getBlockState(scanPos).isAir()) {
                    level.setBlock(scanPos,
                            Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
                    level.setBlock(scanPos.above(1),
                            Blocks.SOUL_LANTERN.defaultBlockState()
                                    .setValue(LanternBlock.HANGING, false), Block.UPDATE_ALL);
                    break;
                }
            }
        }
    }

    /** Cobblestone well with fence post, chain, and lantern. */
    private static void buildWell(ServerLevel level, BlockPos origin) {
        int wellY = surfaceY(level, origin.getX() + WELL_CX, origin.getZ() + WELL_CZ) + 1;
        BlockPos wc = new BlockPos(origin.getX() + WELL_CX, wellY, origin.getZ() + WELL_CZ);

        // 3x3 cobblestone wall ring with water center
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    level.setBlock(wc, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    level.setBlock(wc.offset(dx, 0, dz),
                            Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
                    level.setBlock(wc.offset(dx, 1, dz),
                            Blocks.COBBLESTONE_WALL.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
        // Oak fence post above with chain + lantern
        level.setBlock(wc.above(2), Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(wc.above(3),
                Blocks.CHAIN.defaultBlockState()
                        .setValue(ChainBlock.AXIS, Direction.Axis.Y), Block.UPDATE_ALL);
        level.setBlock(wc.above(4),
                Blocks.LANTERN.defaultBlockState()
                        .setValue(LanternBlock.HANGING, false), Block.UPDATE_ALL);
    }

    /** Flower beds with pink petals, alliums, lilacs, peonies, rose bushes, cherry saplings. */
    private static void buildFlowerBeds(ServerLevel level, BlockPos origin) {
        // Dedicated flower bed areas around the garden
        int[][] bedCenters = {
            {GARDEN_CX + 6, GARDEN_CZ + 2},
            {GARDEN_CX - 6, GARDEN_CZ + 3},
            {GARDEN_CX + 3, GARDEN_CZ - 5},
            {GARDEN_CX - 3, GARDEN_CZ + 8},
            {TOWER_CX - TOWER_RADIUS - 4, TOWER_CZ + 4}
        };

        RandomSource random = RandomSource.create();
        for (int[] bed : bedCenters) {
            int bedY = surfaceY(level, origin.getX() + bed[0], origin.getZ() + bed[1]);
            BlockPos center = new BlockPos(origin.getX() + bed[0], bedY + 1, origin.getZ() + bed[1]);

            // Pearlescent froglight embedded in soil (1 per bed)
            level.setBlock(center.below(1), Blocks.PEARLESCENT_FROGLIGHT.defaultBlockState(),
                    Block.UPDATE_ALL);

            // Flowers in a 3x3 area around the froglight
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos flowerPos = center.offset(dx, 0, dz);
                    if (!level.getBlockState(flowerPos).isAir()) continue;
                    if (level.getBlockState(flowerPos.below()).isAir()) continue;

                    int roll = random.nextInt(7);
                    switch (roll) {
                        case 0 -> {
                            // Double-tall: lilac
                            if (level.getBlockState(flowerPos.above()).isAir()) {
                                level.setBlock(flowerPos, Blocks.LILAC.defaultBlockState()
                                        .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER),
                                        Block.UPDATE_ALL);
                                level.setBlock(flowerPos.above(), Blocks.LILAC.defaultBlockState()
                                        .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                                        Block.UPDATE_ALL);
                            }
                        }
                        case 1 -> {
                            // Double-tall: peony
                            if (level.getBlockState(flowerPos.above()).isAir()) {
                                level.setBlock(flowerPos, Blocks.PEONY.defaultBlockState()
                                        .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER),
                                        Block.UPDATE_ALL);
                                level.setBlock(flowerPos.above(), Blocks.PEONY.defaultBlockState()
                                        .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                                        Block.UPDATE_ALL);
                            }
                        }
                        case 2 -> {
                            // Double-tall: rose bush
                            if (level.getBlockState(flowerPos.above()).isAir()) {
                                level.setBlock(flowerPos, Blocks.ROSE_BUSH.defaultBlockState()
                                        .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER),
                                        Block.UPDATE_ALL);
                                level.setBlock(flowerPos.above(), Blocks.ROSE_BUSH.defaultBlockState()
                                        .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                                        Block.UPDATE_ALL);
                            }
                        }
                        case 3 -> level.setBlock(flowerPos,
                                Blocks.ALLIUM.defaultBlockState(), Block.UPDATE_ALL);
                        case 4 -> level.setBlock(flowerPos,
                                Blocks.PINK_TULIP.defaultBlockState(), Block.UPDATE_ALL);
                        case 5 -> level.setBlock(flowerPos,
                                Blocks.CHERRY_SAPLING.defaultBlockState(), Block.UPDATE_ALL);
                        default -> level.setBlock(flowerPos,
                                Blocks.ALLIUM.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    // =====================================================================
    //  FRIENDLY SKELETONS -- 10 spread around the island
    // =====================================================================

    private static void spawnFriendlySkeletons(ServerLevel level, BlockPos origin,
                                                RandomSource random) {
        int[][] positions = {
            // Tower entrance (2)
            {TOWER_CX - 4, TOWER_CZ - TOWER_RADIUS - 3},
            {TOWER_CX + 4, TOWER_CZ - TOWER_RADIUS - 3},
            // Pond dock (1)
            {POND_CX + POND_RADIUS + 2, POND_CZ - 2},
            // Garden (3)
            {GARDEN_CX - 5, GARDEN_CZ + 5},
            {GARDEN_CX + 6, GARDEN_CZ - 5},
            {GARDEN_CX, GARDEN_CZ + 10},
            // Gazebo (1)
            {GARDEN_CX + 1, GARDEN_CZ + 1},
            // Grotto entrance (1)
            {GROTTO_CX + 4, GROTTO_CZ + 7},
            // Wandering (2)
            {-18, 8},
            {16, -14},
        };

        for (int[] pos : positions) {
            double dist = Math.sqrt(pos[0] * pos[0] + pos[1] * pos[1]);
            if (dist > ISLAND_RADIUS - 3) continue;

            double x = origin.getX() + pos[0] + 0.5;
            double z = origin.getZ() + pos[1] + 0.5;
            // Find surface for proper Y
            int sy = surfaceY(level, (int) (origin.getX() + pos[0]), (int) (origin.getZ() + pos[1]));
            double y = sy + 1;

            Skeleton skeleton = new Skeleton(EntityType.SKELETON, level);
            skeleton.moveTo(x, y, z, random.nextFloat() * 360, 0);
            skeleton.targetSelector.removeAllGoals(goal -> true);
            skeleton.setPersistenceRequired();
            skeleton.setInvulnerable(true);
            level.addFreshEntity(skeleton);
        }
    }

    // =====================================================================
    //  VEGETATION -- cherry trees, dark oak trees, flowers, grass, etc.
    // =====================================================================

    private static void addVegetation(ServerLevel level, BlockPos origin, RandomSource random) {
        // -- Cherry trees (10-12 hand-built) --
        int[][] cherryPositions = {
            {-18, -8}, {-16, 6}, {-20, 2}, {-12, 16}, {-22, -12},
            {18, -10}, {16, 14}, {22, 4}, {20, -16},
            {8, 22}, {-8, 24}, {-14, -16}
        };
        for (int[] pos : cherryPositions) {
            double dist = Math.sqrt(pos[0] * pos[0] + pos[1] * pos[1]);
            if (dist > ISLAND_RADIUS - 5) continue;
            plantCherryTree(level, origin, pos[0], pos[1], random);
        }

        // -- Dark oak trees (3-4 for contrast) --
        int[][] darkOakPositions = {
            {-20, 14}, {20, -14}, {-6, -24}, {24, 8}
        };
        for (int[] pos : darkOakPositions) {
            double dist = Math.sqrt(pos[0] * pos[0] + pos[1] * pos[1]);
            if (dist > ISLAND_RADIUS - 5) continue;
            plantDarkOakTree(level, origin, pos[0], pos[1], random);
        }

        // -- Azalea bushes --
        for (int i = 0; i < 25; i++) {
            int dx = random.nextIntBetweenInclusive(-ISLAND_RADIUS + 4, ISLAND_RADIUS - 4);
            int dz = random.nextIntBetweenInclusive(-ISLAND_RADIUS + 4, ISLAND_RADIUS - 4);
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > ISLAND_RADIUS - 5) continue;
            int sy = surfaceY(level, origin.getX() + dx, origin.getZ() + dz);
            BlockPos pos = new BlockPos(origin.getX() + dx, sy + 1, origin.getZ() + dz);
            if (level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
                boolean flowering = random.nextBoolean();
                level.setBlock(pos,
                        (flowering ? Blocks.FLOWERING_AZALEA : Blocks.AZALEA)
                                .defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        // -- Scattered flowers (pink tulips, alliums, etc.) --
        for (int i = 0; i < 70; i++) {
            int dx = random.nextIntBetweenInclusive(-ISLAND_RADIUS + 3, ISLAND_RADIUS - 3);
            int dz = random.nextIntBetweenInclusive(-ISLAND_RADIUS + 3, ISLAND_RADIUS - 3);
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > ISLAND_RADIUS - 4) continue;
            int sy = surfaceY(level, origin.getX() + dx, origin.getZ() + dz);
            BlockPos flowerPos = new BlockPos(origin.getX() + dx, sy + 1, origin.getZ() + dz);
            if (!level.getBlockState(flowerPos).isAir()) continue;
            BlockState below = level.getBlockState(flowerPos.below());
            if (!below.is(Blocks.GRASS_BLOCK) && !below.is(Blocks.PODZOL)
                    && !below.is(Blocks.MOSS_BLOCK)) continue;

            Block flower = switch (random.nextInt(8)) {
                case 0 -> Blocks.ROSE_BUSH;
                case 1 -> Blocks.PINK_TULIP;
                case 2 -> Blocks.ALLIUM;
                case 3 -> Blocks.AZURE_BLUET;
                case 4 -> Blocks.OXEYE_DAISY;
                case 5 -> Blocks.PEONY;
                case 6 -> Blocks.LILAC;
                default -> Blocks.CORNFLOWER;
            };

            if (flower == Blocks.ROSE_BUSH || flower == Blocks.PEONY
                    || flower == Blocks.LILAC) {
                if (level.getBlockState(flowerPos.above()).isAir()) {
                    level.setBlock(flowerPos,
                            flower.defaultBlockState()
                                    .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER),
                            Block.UPDATE_ALL);
                    level.setBlock(flowerPos.above(),
                            flower.defaultBlockState()
                                    .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                            Block.UPDATE_ALL);
                }
            } else {
                level.setBlock(flowerPos, flower.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        // -- Tall grass and ferns --
        for (int i = 0; i < 80; i++) {
            int dx = random.nextIntBetweenInclusive(-ISLAND_RADIUS + 4, ISLAND_RADIUS - 4);
            int dz = random.nextIntBetweenInclusive(-ISLAND_RADIUS + 4, ISLAND_RADIUS - 4);
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > ISLAND_RADIUS - 5) continue;
            int sy = surfaceY(level, origin.getX() + dx, origin.getZ() + dz);
            BlockPos pos = new BlockPos(origin.getX() + dx, sy + 1, origin.getZ() + dz);
            if (!level.getBlockState(pos).isAir()) continue;
            BlockState below = level.getBlockState(pos.below());
            if (!below.is(Blocks.GRASS_BLOCK) && !below.is(Blocks.PODZOL)) continue;

            Block grass = switch (random.nextInt(4)) {
                case 0 -> Blocks.TALL_GRASS;
                case 1 -> Blocks.FERN;
                case 2 -> Blocks.LARGE_FERN;
                default -> Blocks.SHORT_GRASS;
            };

            if (grass == Blocks.TALL_GRASS || grass == Blocks.LARGE_FERN) {
                if (level.getBlockState(pos.above()).isAir()) {
                    level.setBlock(pos,
                            grass.defaultBlockState()
                                    .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER),
                            Block.UPDATE_ALL);
                    level.setBlock(pos.above(),
                            grass.defaultBlockState()
                                    .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                            Block.UPDATE_ALL);
                }
            } else {
                level.setBlock(pos, grass.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        // -- Glow lichen patches near the pond and grotto --
        for (int i = 0; i < 30; i++) {
            int dx = POND_CX + random.nextIntBetweenInclusive(-14, 14);
            int dz = POND_CZ + random.nextIntBetweenInclusive(-14, 14);
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > ISLAND_RADIUS - 4) continue;
            int sy = surfaceY(level, origin.getX() + dx, origin.getZ() + dz);
            BlockPos lichenPos = new BlockPos(origin.getX() + dx, sy + 1, origin.getZ() + dz);
            if (!level.getBlockState(lichenPos).isAir()) continue;
            level.setBlock(lichenPos,
                    Blocks.GLOW_LICHEN.defaultBlockState()
                            .setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true),
                    Block.UPDATE_ALL);
        }

        // -- End rod accent lights near the tower --
        int[][] endRodPositions = {
            {TOWER_CX - TOWER_RADIUS - 3, TOWER_CZ},
            {TOWER_CX, TOWER_CZ - TOWER_RADIUS - 3},
            {TOWER_CX + TOWER_RADIUS + 3, TOWER_CZ},
            {TOWER_CX, TOWER_CZ + TOWER_RADIUS + 3},
        };
        for (int[] er : endRodPositions) {
            double dist = Math.sqrt(er[0] * er[0] + er[1] * er[1]);
            if (dist > ISLAND_RADIUS - 4) continue;
            int sy = surfaceY(level, origin.getX() + er[0], origin.getZ() + er[1]);
            BlockPos erPos = new BlockPos(origin.getX() + er[0], sy + 1, origin.getZ() + er[1]);
            if (level.getBlockState(erPos).isAir()) {
                level.setBlock(erPos, Blocks.END_ROD.defaultBlockState()
                        .setValue(EndRodBlock.FACING, Direction.UP), Block.UPDATE_ALL);
            }
        }

        // -- Decorative amethyst clusters at 8-10 spots --
        int[][] amethystSpots = {
            {-6, -6}, {6, 6}, {-10, 18}, {18, -10}, {-20, -6},
            {14, 20}, {-16, -16}, {22, -2}, {-4, 26}, {10, -22}
        };
        for (int[] ac : amethystSpots) {
            double dist = Math.sqrt(ac[0] * ac[0] + ac[1] * ac[1]);
            if (dist > ISLAND_RADIUS - 4) continue;
            int sy = surfaceY(level, origin.getX() + ac[0], origin.getZ() + ac[1]);
            BlockPos acPos = new BlockPos(origin.getX() + ac[0], sy + 1, origin.getZ() + ac[1]);
            if (level.getBlockState(acPos).isAir()
                    && !level.getBlockState(acPos.below()).isAir()) {
                level.setBlock(acPos, Blocks.AMETHYST_CLUSTER.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, Direction.UP), Block.UPDATE_ALL);
            }
        }
    }

    /**
     * Build a cherry tree by hand: trunk 5-8 tall, 1-2 horizontal branches,
     * irregular multi-layer canopy, hanging lanterns and glow berry vines.
     */
    private static void plantCherryTree(ServerLevel level, BlockPos origin,
                                         int dx, int dz, RandomSource random) {
        int sy = surfaceY(level, origin.getX() + dx, origin.getZ() + dz);
        BlockPos treeBase = new BlockPos(origin.getX() + dx, sy + 1, origin.getZ() + dz);

        int height = 5 + random.nextInt(4); // 5-8

        // Main trunk
        for (int y = 0; y < height; y++) {
            level.setBlock(treeBase.above(y),
                    Blocks.CHERRY_LOG.defaultBlockState(), Block.UPDATE_ALL);
        }

        // 1-2 horizontal branch logs at the top
        Direction branchDir1 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        level.setBlock(treeBase.above(height - 1).relative(branchDir1),
                Blocks.CHERRY_LOG.defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS,
                                branchDir1.getAxis() == Direction.Axis.X
                                        ? Direction.Axis.X : Direction.Axis.Z),
                Block.UPDATE_ALL);
        if (random.nextBoolean()) {
            Direction branchDir2 = branchDir1.getClockWise();
            level.setBlock(treeBase.above(height - 2).relative(branchDir2),
                    Blocks.CHERRY_LOG.defaultBlockState()
                            .setValue(RotatedPillarBlock.AXIS,
                                    branchDir2.getAxis() == Direction.Axis.X
                                            ? Direction.Axis.X : Direction.Axis.Z),
                    Block.UPDATE_ALL);
        }

        // Irregular canopy: NOT spherical, multi-layer with gaps
        // Layer 1: radius 4-5 at height-1 and height
        int canopyR1 = 4 + random.nextInt(2);
        for (int cdx = -canopyR1; cdx <= canopyR1; cdx++) {
            for (int cdz = -canopyR1; cdz <= canopyR1; cdz++) {
                double cdist = Math.sqrt(cdx * cdx + cdz * cdz);
                if (cdist > canopyR1 + 0.3) continue;
                // Random gaps for irregular look
                if (cdist > canopyR1 - 1 && random.nextInt(3) == 0) continue;
                if (cdist > 2 && random.nextInt(8) == 0) continue;

                for (int ly = height - 1; ly <= height; ly++) {
                    BlockPos leafPos = treeBase.above(ly).offset(cdx, 0, cdz);
                    if (level.getBlockState(leafPos).isAir()
                            || level.getBlockState(leafPos).is(Blocks.CHERRY_LEAVES)) {
                        level.setBlock(leafPos, Blocks.CHERRY_LEAVES.defaultBlockState()
                                .setValue(LeavesBlock.PERSISTENT, true), Block.UPDATE_ALL);
                    }
                }
            }
        }

        // Layer 2: radius 2-3 at height+1
        int canopyR2 = 2 + random.nextInt(2);
        for (int cdx = -canopyR2; cdx <= canopyR2; cdx++) {
            for (int cdz = -canopyR2; cdz <= canopyR2; cdz++) {
                double cdist = Math.sqrt(cdx * cdx + cdz * cdz);
                if (cdist > canopyR2 + 0.3) continue;
                if (random.nextInt(6) == 0) continue;
                BlockPos leafPos = treeBase.above(height + 1).offset(cdx, 0, cdz);
                if (level.getBlockState(leafPos).isAir()) {
                    level.setBlock(leafPos, Blocks.CHERRY_LEAVES.defaultBlockState()
                            .setValue(LeavesBlock.PERSISTENT, true), Block.UPDATE_ALL);
                }
            }
        }

        // Top cap: 1-2 leaves at height+2
        level.setBlock(treeBase.above(height + 2),
                Blocks.CHERRY_LEAVES.defaultBlockState()
                        .setValue(LeavesBlock.PERSISTENT, true), Block.UPDATE_ALL);

        // Hang lanterns from lowest canopy edges (1-2 per tree)
        int lanternCount = 0;
        for (int cdx = -canopyR1; cdx <= canopyR1 && lanternCount < 2; cdx++) {
            for (int cdz = -canopyR1; cdz <= canopyR1 && lanternCount < 2; cdz++) {
                double cdist = Math.sqrt(cdx * cdx + cdz * cdz);
                if (cdist < canopyR1 - 1 || cdist > canopyR1) continue;
                BlockPos leafPos = treeBase.above(height - 1).offset(cdx, 0, cdz);
                BlockPos hangPos = leafPos.below(1);
                if (level.getBlockState(leafPos).is(Blocks.CHERRY_LEAVES)
                        && level.getBlockState(hangPos).isAir() && random.nextInt(4) == 0) {
                    level.setBlock(hangPos, Blocks.LANTERN.defaultBlockState()
                            .setValue(LanternBlock.HANGING, true), Block.UPDATE_ALL);
                    lanternCount++;
                }
            }
        }

        // Glow berries (cave vines) trailing from outer canopy edges
        for (int cdx = -canopyR1; cdx <= canopyR1; cdx++) {
            for (int cdz = -canopyR1; cdz <= canopyR1; cdz++) {
                double cdist = Math.sqrt(cdx * cdx + cdz * cdz);
                if (cdist < canopyR1 - 1.5 || cdist > canopyR1) continue;
                if (random.nextInt(4) != 0) continue;
                BlockPos leafPos = treeBase.above(height - 1).offset(cdx, 0, cdz);
                BlockPos hangPos = leafPos.below(1);
                if (level.getBlockState(leafPos).is(Blocks.CHERRY_LEAVES)
                        && level.getBlockState(hangPos).isAir()) {
                    level.setBlock(hangPos, Blocks.CAVE_VINES.defaultBlockState()
                            .setValue(CaveVinesBlock.BERRIES, true), Block.UPDATE_ALL);
                    // Second vine segment
                    if (random.nextBoolean() && level.getBlockState(hangPos.below()).isAir()) {
                        level.setBlock(hangPos.below(),
                                Blocks.CAVE_VINES_PLANT.defaultBlockState()
                                        .setValue(CaveVinesPlantBlock.BERRIES, true),
                                Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    /** Build a dark oak tree: 2x2 trunk, 6-8 tall, wide irregular canopy. */
    private static void plantDarkOakTree(ServerLevel level, BlockPos origin,
                                          int dx, int dz, RandomSource random) {
        int sy = surfaceY(level, origin.getX() + dx, origin.getZ() + dz);
        BlockPos treeBase = new BlockPos(origin.getX() + dx, sy + 1, origin.getZ() + dz);

        int height = 6 + random.nextInt(3); // 6-8

        // 2x2 trunk
        for (int y = 0; y < height; y++) {
            for (int tdx = 0; tdx <= 1; tdx++) {
                for (int tdz = 0; tdz <= 1; tdz++) {
                    level.setBlock(treeBase.above(y).offset(tdx, 0, tdz),
                            Blocks.DARK_OAK_LOG.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }

        // Wide canopy: radius 3-4, irregular
        int canopyR = 3 + random.nextInt(2);
        for (int cdx = -canopyR; cdx <= canopyR + 1; cdx++) {
            for (int cdz = -canopyR; cdz <= canopyR + 1; cdz++) {
                double cdist = Math.sqrt(cdx * cdx + cdz * cdz);
                if (cdist > canopyR + 0.8) continue;
                if (cdist > canopyR - 0.5 && random.nextBoolean()) continue;

                // Two layers of leaves
                for (int ly = height - 1; ly <= height; ly++) {
                    BlockPos leafPos = treeBase.above(ly).offset(cdx, 0, cdz);
                    if (level.getBlockState(leafPos).isAir()) {
                        level.setBlock(leafPos, Blocks.DARK_OAK_LEAVES.defaultBlockState()
                                .setValue(LeavesBlock.PERSISTENT, true), Block.UPDATE_ALL);
                    }
                }
            }
        }

        // Smaller top layer
        for (int cdx = -1; cdx <= 2; cdx++) {
            for (int cdz = -1; cdz <= 2; cdz++) {
                if (random.nextInt(5) == 0) continue;
                BlockPos leafPos = treeBase.above(height + 1).offset(cdx, 0, cdz);
                if (level.getBlockState(leafPos).isAir()) {
                    level.setBlock(leafPos, Blocks.DARK_OAK_LEAVES.defaultBlockState()
                            .setValue(LeavesBlock.PERSISTENT, true), Block.UPDATE_ALL);
                }
            }
        }
    }
}
