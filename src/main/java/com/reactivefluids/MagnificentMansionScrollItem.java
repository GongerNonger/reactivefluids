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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
 * 7th-level conjuration — Mordenkainen's Magnificent Mansion.
 * Opens a portal to a pocket dimension containing a grand wizard's estate.
 * 45-wide x 40-deep, two full floors with peaked cross-gable roof,
 * deepslate brick aesthetic, imperial staircase, and fully furnished rooms.
 */
public class MagnificentMansionScrollItem extends Item {

    private static final int WIDTH  = 45;
    private static final int DEPTH  = 40;
    private static final int HEIGHT = 25;
    private static final int DURATION_TICKS = 24000;

    // Floor heights (local Y)
    private static final int FLOOR1 = 0;   // ground floor surface
    private static final int CEIL1  = 7;   // ground floor ceiling (6 interior + 1 slab)
    private static final int FLOOR2 = 8;   // second floor surface
    private static final int CEIL2  = 15;  // second floor ceiling

    // Palette constants (reused across methods)
    private static BlockState DEEPSLATE_BRICK;
    private static BlockState DEEPSLATE_TILE;
    private static BlockState POLISHED_DEEPSLATE;
    private static BlockState DARK_OAK_LOG;
    private static BlockState STRIPPED_DARK_OAK;
    private static BlockState DARK_OAK_PLANKS;
    private static BlockState SPRUCE_PLANKS;
    private static BlockState GILDED_BLACKSTONE;
    private static BlockState CRYING_OBSIDIAN;
    private static BlockState POLISHED_BASALT;
    private static BlockState QUARTZ_BLOCK;
    private static BlockState LIGHT_GRAY_GLASS_PANE;
    private static BlockState DEEPSLATE_BRICK_WALL;
    private static BlockState DEEPSLATE_TILE_SLAB;
    private static BlockState DEEPSLATE_TILE_STAIR_N;
    private static BlockState DEEPSLATE_TILE_STAIR_S;
    private static BlockState DEEPSLATE_TILE_STAIR_E;
    private static BlockState DEEPSLATE_TILE_STAIR_W;
    private static BlockState DARK_PRISMARINE;
    private static BlockState CHAIN;
    private static BlockState AIR;

    private static void initPalette(Direction f) {
        DEEPSLATE_BRICK = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        DEEPSLATE_TILE = Blocks.DEEPSLATE_TILES.defaultBlockState();
        POLISHED_DEEPSLATE = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        DARK_OAK_LOG = Blocks.DARK_OAK_LOG.defaultBlockState();
        STRIPPED_DARK_OAK = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
        DARK_OAK_PLANKS = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        SPRUCE_PLANKS = Blocks.SPRUCE_PLANKS.defaultBlockState();
        GILDED_BLACKSTONE = Blocks.GILDED_BLACKSTONE.defaultBlockState();
        CRYING_OBSIDIAN = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        POLISHED_BASALT = Blocks.POLISHED_BASALT.defaultBlockState();
        QUARTZ_BLOCK = Blocks.QUARTZ_BLOCK.defaultBlockState();
        LIGHT_GRAY_GLASS_PANE = Blocks.LIGHT_GRAY_STAINED_GLASS_PANE.defaultBlockState();
        DEEPSLATE_BRICK_WALL = Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState();
        DEEPSLATE_TILE_SLAB = Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState();
        DEEPSLATE_TILE_STAIR_N = Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, rotateDir(Direction.NORTH, f));
        DEEPSLATE_TILE_STAIR_S = Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, rotateDir(Direction.SOUTH, f));
        DEEPSLATE_TILE_STAIR_E = Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, rotateDir(Direction.EAST, f));
        DEEPSLATE_TILE_STAIR_W = Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, rotateDir(Direction.WEST, f));
        DARK_PRISMARINE = Blocks.DARK_PRISMARINE.defaultBlockState();
        CHAIN = Blocks.CHAIN.defaultBlockState();
        AIR = Blocks.AIR.defaultBlockState();
    }

    public MagnificentMansionScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        MinecraftServer server = serverPlayer.server;
        ServerLevel mansionLevel = server.getLevel(ModDimensions.MANSION);
        if (mansionLevel == null) return InteractionResult.FAIL;

        PocketDimensionData pocketData = PocketDimensionData.get(server);

        int slot = pocketData.allocateMansionSlot();
        int baseX = PocketDimensionData.slotToX(slot);
        BlockPos origin = new BlockPos(baseX, 64, 0);

        buildMansion(mansionLevel, origin, Direction.SOUTH);

        // Place return portal at the entrance
        BlockPos portalPos = origin.offset(WIDTH / 2, 1, -1);
        mansionLevel.setBlock(portalPos, ModBlocks.RETURN_PORTAL.get().defaultBlockState(),
                Block.UPDATE_ALL);
        mansionLevel.setBlock(portalPos.above(), ModBlocks.RETURN_PORTAL.get().defaultBlockState(),
                Block.UPDATE_ALL);

        pocketData.saveReturnPosition(serverPlayer.getUUID(),
                serverPlayer.level().dimension(),
                serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                serverPlayer.getYRot(), serverPlayer.getXRot());

        long expiryTick = server.overworld().getGameTime() + DURATION_TICKS;
        pocketData.addInstance(ModDimensions.MANSION, expiryTick);

        ServerLevel departLevel = (ServerLevel) serverPlayer.level();
        departLevel.sendParticles(ParticleTypes.END_ROD,
                serverPlayer.getX(), serverPlayer.getY() + 1, serverPlayer.getZ(),
                40, 0.5, 1.0, 0.5, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.5F, 0.8F);

        double teleX = origin.getX() + WIDTH / 2.0 + 0.5;
        double teleY = origin.getY() + 1;
        double teleZ = origin.getZ() + 1.5;
        serverPlayer.teleportTo(mansionLevel, teleX, teleY, teleZ, 0, 0);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.magnificent_mansion_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    // =====================================================================
    // Coordinate rotation
    // =====================================================================

    private static BlockPos toWorld(BlockPos origin, int lx, int ly, int lz, Direction facing) {
        int dx = lx - WIDTH / 2;
        int dz = lz;
        int rx, rz;
        switch (facing) {
            case NORTH -> { rx = -dx; rz = -dz; }
            case EAST  -> { rx = -dz; rz =  dx; }
            case WEST  -> { rx =  dz; rz = -dx; }
            default    -> { rx =  dx; rz =  dz; } // SOUTH
        }
        return origin.offset(rx, ly, rz);
    }

    private static Direction rotateDir(Direction dir, Direction facing) {
        return switch (facing) {
            case NORTH -> dir.getOpposite();
            case EAST  -> dir.getClockWise();
            case WEST  -> dir.getCounterClockWise();
            default    -> dir;
        };
    }

    // =====================================================================
    // Placement helpers
    // =====================================================================

    private static void place(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    private static void placeAt(ServerLevel level, BlockPos origin, int lx, int ly, int lz,
                                 Direction f, BlockState state) {
        place(level, toWorld(origin, lx, ly, lz, f), state);
    }

    /** Place a stair block with specific facing and optional upside-down */
    private static BlockState stair(Block block, Direction facing, Direction f, boolean upsideDown) {
        BlockState s = block.defaultBlockState()
                .setValue(StairBlock.FACING, rotateDir(facing, f));
        if (upsideDown) {
            s = s.setValue(StairBlock.HALF, Half.TOP);
        }
        return s;
    }

    /** Place a slab block with optional top half */
    private static BlockState slab(Block block, boolean top) {
        BlockState s = block.defaultBlockState();
        if (top) {
            s = s.setValue(SlabBlock.TYPE, SlabType.TOP);
        }
        return s;
    }

    private static BlockState hangingLantern() {
        return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    }

    private static BlockState hangingSoulLantern() {
        return Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    }

    // =====================================================================
    // Mansion construction — top-level dispatcher
    // =====================================================================

    private static void buildMansion(ServerLevel level, BlockPos origin, Direction facing) {
        initPalette(facing);

        buildGrounds(level, origin, facing);
        buildShell(level, origin, facing);
        buildExteriorDetail(level, origin, facing);
        buildRoof(level, origin, facing);
        buildPorch(level, origin, facing);

        // Ground floor
        buildGroundFloorInteriorWalls(level, origin, facing);
        buildGrandStaircase(level, origin, facing);
        furnishFoyer(level, origin, facing);
        furnishDiningHall(level, origin, facing);
        furnishKitchen(level, origin, facing);
        furnishGreatHall(level, origin, facing);
        furnishArmory(level, origin, facing);
        furnishWashroom(level, origin, facing);

        // Second floor
        buildSecondFloorInteriorWalls(level, origin, facing);
        furnishMasterBedroom(level, origin, facing);
        furnishGuestBedroom(level, origin, facing);
        furnishLibrary(level, origin, facing);
        furnishBrewingLab(level, origin, facing);
        furnishLounge(level, origin, facing);

        // Exterior
        buildGarden(level, origin, facing);
        buildPerimeterWall(level, origin, facing);
    }

    // =====================================================================
    // Grounds / platform
    // =====================================================================

    private static void buildGrounds(ServerLevel level, BlockPos origin, Direction f) {
        int pad = 10;

        for (int lx = -pad; lx < WIDTH + pad; lx++) {
            for (int lz = -pad; lz < DEPTH + pad; lz++) {
                // Foundation
                placeAt(level, origin, lx, -3, lz, f, Blocks.STONE.defaultBlockState());
                placeAt(level, origin, lx, -2, lz, f, Blocks.STONE.defaultBlockState());
                placeAt(level, origin, lx, -1, lz, f, Blocks.DIRT.defaultBlockState());

                boolean insideMansion = lx >= 0 && lx < WIDTH && lz >= 0 && lz < DEPTH;
                if (!insideMansion) {
                    placeAt(level, origin, lx, 0, lz, f, Blocks.GRASS_BLOCK.defaultBlockState());
                }
            }
        }

        // Dirt path walkways
        int mid = WIDTH / 2;
        // Front approach - 5-wide gravel path
        for (int lz = -pad; lz < 0; lz++) {
            for (int lx = mid - 2; lx <= mid + 2; lx++) {
                placeAt(level, origin, lx, 0, lz, f, Blocks.GRAVEL.defaultBlockState());
            }
            // Mossy stone brick slab borders
            placeAt(level, origin, mid - 3, 0, lz, f,
                    Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState());
            placeAt(level, origin, mid + 3, 0, lz, f,
                    Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState());
        }
        // Soul lanterns on fence posts along front path
        for (int lz = -pad + 1; lz < -1; lz += 4) {
            for (int side = -1; side <= 1; side += 2) {
                int lx = mid + side * 4;
                placeAt(level, origin, lx, 1, lz, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
                placeAt(level, origin, lx, 2, lz, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
                placeAt(level, origin, lx, 3, lz, f, Blocks.SOUL_LANTERN.defaultBlockState());
            }
        }

        // Rear garden paths
        for (int lz = DEPTH; lz < DEPTH + pad; lz++) {
            for (int lx = mid - 1; lx <= mid + 1; lx++) {
                placeAt(level, origin, lx, 0, lz, f, Blocks.DIRT_PATH.defaultBlockState());
            }
        }
        // Cross path at DEPTH+5
        for (int lx = mid - 8; lx <= mid + 8; lx++) {
            placeAt(level, origin, lx, 0, DEPTH + 5, f, Blocks.DIRT_PATH.defaultBlockState());
        }
    }

    // =====================================================================
    // Outer shell (both floors)
    // =====================================================================

    private static void buildShell(ServerLevel level, BlockPos origin, Direction f) {
        // ---- Ground floor (FLOOR1) interior floor: dark oak planks ----
        for (int lx = 1; lx < WIDTH - 1; lx++) {
            for (int lz = 1; lz < DEPTH - 1; lz++) {
                placeAt(level, origin, lx, FLOOR1, lz, f, DARK_OAK_PLANKS);
            }
        }

        // ---- First floor ceiling / second floor structural slab ----
        for (int lx = 0; lx < WIDTH; lx++) {
            for (int lz = 0; lz < DEPTH; lz++) {
                placeAt(level, origin, lx, CEIL1, lz, f, DEEPSLATE_TILE);
            }
        }

        // ---- Second floor surface: spruce planks ----
        for (int lx = 1; lx < WIDTH - 1; lx++) {
            for (int lz = 1; lz < DEPTH - 1; lz++) {
                placeAt(level, origin, lx, FLOOR2, lz, f, SPRUCE_PLANKS);
            }
        }

        // ---- Second floor ceiling ----
        for (int lx = 0; lx < WIDTH; lx++) {
            for (int lz = 0; lz < DEPTH; lz++) {
                placeAt(level, origin, lx, CEIL2, lz, f, DEEPSLATE_TILE);
            }
        }

        // ---- Outer walls — both floors ----
        // West wall (lx=0) and East wall (lx=WIDTH-1)
        for (int lz = 0; lz < DEPTH; lz++) {
            for (int ly = 0; ly <= CEIL2; ly++) {
                for (int side = 0; side <= 1; side++) {
                    int lx = (side == 0) ? 0 : WIDTH - 1;
                    placeAt(level, origin, lx, ly, lz, f, getWallBlock(lx, ly, lz, true));
                }
            }
        }

        // North wall (lz=0) — front facade
        int mid = WIDTH / 2;
        for (int lx = 0; lx < WIDTH; lx++) {
            for (int ly = 0; ly <= CEIL2; ly++) {
                // Doorway: 5-wide, 5-tall centered opening
                boolean isDoorway = (lx >= mid - 2 && lx <= mid + 2)
                        && (ly >= 1 && ly <= 5);
                if (isDoorway) {
                    placeAt(level, origin, lx, ly, 0, f, AIR);
                } else {
                    placeAt(level, origin, lx, ly, 0, f, getWallBlock(lx, ly, 0, false));
                }
            }
        }

        // South wall (lz=DEPTH-1) — rear wall
        for (int lx = 0; lx < WIDTH; lx++) {
            for (int ly = 0; ly <= CEIL2; ly++) {
                // Rear door: 3-wide, 4-tall
                boolean isRearDoor = (lx >= mid - 1 && lx <= mid + 1)
                        && (ly >= 1 && ly <= 4);
                if (isRearDoor) {
                    placeAt(level, origin, lx, ly, DEPTH - 1, f, AIR);
                } else {
                    placeAt(level, origin, lx, ly, DEPTH - 1, f, getWallBlock(lx, ly, DEPTH - 1, false));
                }
            }
        }
    }

    /** Determines wall block based on position — adds variety to avoid flat walls. */
    private static BlockState getWallBlock(int lx, int ly, int lz, boolean isSideWall) {
        // Floor and ceiling bands
        if (ly == FLOOR1 || ly == CEIL1 || ly == CEIL2) return DEEPSLATE_TILE;
        // Base course
        if (ly == 1) return POLISHED_DEEPSLATE;
        // Pilaster every 5 blocks (dark oak log columns)
        int pilasterInterval = 5;
        boolean isPilaster;
        if (isSideWall) {
            isPilaster = (lz % pilasterInterval == 0);
        } else {
            isPilaster = (lx % pilasterInterval == 0);
        }
        if (isPilaster) return DARK_OAK_LOG;
        // Gilded blackstone accent at ceiling band
        if (ly == CEIL1 - 1 || ly == CEIL2 - 1) return DEEPSLATE_TILE;
        // Default wall
        return DEEPSLATE_BRICK;
    }

    // =====================================================================
    // Exterior detail: pilasters, windows, buttresses
    // =====================================================================

    private static void buildExteriorDetail(ServerLevel level, BlockPos origin, Direction f) {
        int mid = WIDTH / 2;

        // --- Pilasters on side walls (extruded 1 block outward) ---
        for (int lz = 0; lz < DEPTH; lz += 5) {
            for (int ly = 0; ly <= CEIL2; ly++) {
                // West side pilaster at lx=-1
                placeAt(level, origin, -1, ly, lz, f, DARK_OAK_LOG);
                // East side pilaster at lx=WIDTH
                placeAt(level, origin, WIDTH, ly, lz, f, DARK_OAK_LOG);
            }
        }

        // --- Pilasters on front and rear walls ---
        for (int lx = 0; lx < WIDTH; lx += 5) {
            for (int ly = 0; ly <= CEIL2; ly++) {
                placeAt(level, origin, lx, ly, -1, f, DARK_OAK_LOG);
                placeAt(level, origin, lx, ly, DEPTH, f, DARK_OAK_LOG);
            }
        }

        // --- Corner buttresses (2 blocks out on each axis) ---
        int[][] corners = {{-1, -1}, {-1, DEPTH}, {WIDTH, -1}, {WIDTH, DEPTH}};
        for (int[] c : corners) {
            for (int ly = 0; ly <= CEIL2; ly++) {
                placeAt(level, origin, c[0], ly, c[1], f, DEEPSLATE_BRICK_WALL);
            }
        }
        // Extended buttresses: 2 blocks out from corners
        for (int ly = 0; ly <= CEIL2 - 2; ly++) {
            placeAt(level, origin, -2, ly, 0, f, DEEPSLATE_BRICK_WALL);
            placeAt(level, origin, -2, ly, DEPTH - 1, f, DEEPSLATE_BRICK_WALL);
            placeAt(level, origin, WIDTH + 1, ly, 0, f, DEEPSLATE_BRICK_WALL);
            placeAt(level, origin, WIDTH + 1, ly, DEPTH - 1, f, DEEPSLATE_BRICK_WALL);
            placeAt(level, origin, 0, ly, -2, f, DEEPSLATE_BRICK_WALL);
            placeAt(level, origin, WIDTH - 1, ly, -2, f, DEEPSLATE_BRICK_WALL);
            placeAt(level, origin, 0, ly, DEPTH + 1, f, DEEPSLATE_BRICK_WALL);
            placeAt(level, origin, WIDTH - 1, ly, DEPTH + 1, f, DEEPSLATE_BRICK_WALL);
        }

        // --- Recessed windows on side walls ---
        // Windows between pilasters, recessed 1 block inward with frame
        for (int lz = 2; lz < DEPTH - 2; lz++) {
            if (lz % 5 == 0) continue; // skip pilaster positions
            if ((lz % 5 == 2) || (lz % 5 == 3)) {
                // Ground floor windows at ly=3,4
                placeWindow(level, origin, 0, 3, lz, f, true);
                placeWindow(level, origin, WIDTH - 1, 3, lz, f, true);
                // Second floor windows at ly=FLOOR2+2, FLOOR2+3
                placeWindow(level, origin, 0, FLOOR2 + 2, lz, f, true);
                placeWindow(level, origin, WIDTH - 1, FLOOR2 + 2, lz, f, true);
            }
        }

        // --- Front facade windows ---
        for (int lx = 2; lx < WIDTH - 2; lx++) {
            if (lx % 5 == 0) continue;
            if (lx >= mid - 2 && lx <= mid + 2) continue; // skip doorway
            if ((lx % 5 == 2) || (lx % 5 == 3)) {
                placeWindow(level, origin, lx, 3, 0, f, false);
                placeWindow(level, origin, lx, FLOOR2 + 2, 0, f, false);
            }
        }

        // --- Rear facade windows ---
        for (int lx = 2; lx < WIDTH - 2; lx++) {
            if (lx % 5 == 0) continue;
            if (lx >= mid - 1 && lx <= mid + 1) continue; // skip rear door
            if ((lx % 5 == 2) || (lx % 5 == 3)) {
                placeWindow(level, origin, lx, 3, DEPTH - 1, f, false);
                placeWindow(level, origin, lx, FLOOR2 + 2, DEPTH - 1, f, false);
            }
        }

        // --- Crying obsidian accents at key spots ---
        // Flanking the front door at top
        placeAt(level, origin, mid - 3, 5, 0, f, CRYING_OBSIDIAN);
        placeAt(level, origin, mid + 3, 5, 0, f, CRYING_OBSIDIAN);
        // Above the front door lintel
        placeAt(level, origin, mid, 6, 0, f, CRYING_OBSIDIAN);
        // Rear door accents
        placeAt(level, origin, mid - 2, 4, DEPTH - 1, f, CRYING_OBSIDIAN);
        placeAt(level, origin, mid + 2, 4, DEPTH - 1, f, CRYING_OBSIDIAN);

        // --- Gilded blackstone trim at entrance ---
        for (int lx = mid - 3; lx <= mid + 3; lx++) {
            placeAt(level, origin, lx, 6, 0, f, GILDED_BLACKSTONE);
        }
    }

    /** Places a window with frame at a wall position. */
    private static void placeWindow(ServerLevel level, BlockPos origin,
                                     int lx, int ly, int lz, Direction f, boolean isSideWall) {
        // Glass pane at the given position and one above
        placeAt(level, origin, lx, ly, lz, f, LIGHT_GRAY_GLASS_PANE);
        placeAt(level, origin, lx, ly + 1, lz, f, LIGHT_GRAY_GLASS_PANE);
    }

    // =====================================================================
    // Roof (cross-gable with dormers)
    // =====================================================================

    private static void buildRoof(ServerLevel level, BlockPos origin, Direction f) {
        int roofBase = CEIL2 + 1;
        int mid = WIDTH / 2;

        // Main ridge runs along X axis (east-west).
        // Slopes descend north and south from the ridge.
        // Ridge height: roofBase + (DEPTH/2) capped so roof is proportional.
        int halfDepth = DEPTH / 2; // 20
        int maxRoofRise = 9; // cap roof height for aesthetics

        // Build main roof slopes
        for (int step = 0; step < maxRoofRise; step++) {
            int ly = roofBase + step;
            int northZ = step;        // north slope moves inward
            int southZ = DEPTH - 1 - step; // south slope moves inward

            for (int lx = -1; lx <= WIDTH; lx++) {
                // North slope - stairs facing south (you look at them from north)
                if (northZ < halfDepth) {
                    placeAt(level, origin, lx, ly, northZ - 1, f, DEEPSLATE_TILE_STAIR_S);
                }
                // South slope - stairs facing north
                if (southZ >= halfDepth) {
                    placeAt(level, origin, lx, ly, southZ + 1, f, DEEPSLATE_TILE_STAIR_N);
                }
            }

            // Fill solid between slopes at this level
            int fillStart = Math.max(northZ, 0);
            int fillEnd = Math.min(southZ, DEPTH - 1);
            for (int lx = 0; lx < WIDTH; lx++) {
                for (int lz = fillStart; lz <= fillEnd; lz++) {
                    placeAt(level, origin, lx, ly, lz, f, DARK_PRISMARINE);
                }
            }
        }

        // Ridge cap along top
        int ridgeY = roofBase + maxRoofRise;
        int ridgeZStart = maxRoofRise;
        int ridgeZEnd = DEPTH - 1 - maxRoofRise;
        for (int lx = -1; lx <= WIDTH; lx++) {
            for (int lz = ridgeZStart - 1; lz <= ridgeZEnd + 1; lz++) {
                placeAt(level, origin, lx, ridgeY, lz, f, DEEPSLATE_TILE_SLAB);
            }
        }

        // Gable end caps (north and south triangles)
        for (int step = 0; step < maxRoofRise; step++) {
            int ly = roofBase + step;
            int zNorth = step;
            int zSouth = DEPTH - 1 - step;
            // North gable fill
            for (int lx = 0; lx < WIDTH; lx++) {
                placeAt(level, origin, lx, ly, -1, f, STRIPPED_DARK_OAK);
            }
            // South gable fill
            for (int lx = 0; lx < WIDTH; lx++) {
                placeAt(level, origin, lx, ly, DEPTH, f, STRIPPED_DARK_OAK);
            }
        }

        // Overhang: bottom slabs extending 1 block past walls at roofBase
        for (int lx = -1; lx <= WIDTH; lx++) {
            placeAt(level, origin, lx, roofBase, -1, f,
                    slab(Blocks.DEEPSLATE_TILE_SLAB, false));
            placeAt(level, origin, lx, roofBase, DEPTH, f,
                    slab(Blocks.DEEPSLATE_TILE_SLAB, false));
        }
        for (int lz = 0; lz < DEPTH; lz++) {
            placeAt(level, origin, -1, roofBase, lz, f,
                    slab(Blocks.DEEPSLATE_TILE_SLAB, false));
            placeAt(level, origin, WIDTH, roofBase, lz, f,
                    slab(Blocks.DEEPSLATE_TILE_SLAB, false));
        }

        // --- Dormers (3-wide mini-gables with windows) ---
        // North side: 3 dormers
        int[] dormerXPositions = {8, mid, WIDTH - 9};
        for (int dx : dormerXPositions) {
            buildDormer(level, origin, dx, roofBase + 2, 2, f, true);
        }
        // South side: 3 dormers
        for (int dx : dormerXPositions) {
            buildDormer(level, origin, dx, roofBase + 2, DEPTH - 3, f, false);
        }
    }

    private static void buildDormer(ServerLevel level, BlockPos origin,
                                     int cx, int baseY, int cz, Direction f, boolean facingNorth) {
        // 3-wide dormer box protruding from roof slope
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                // Side walls
                if (dx == -1 || dx == 1) {
                    placeAt(level, origin, cx + dx, baseY + dy, cz, f, DEEPSLATE_BRICK);
                }
            }
            // Roof of dormer
            placeAt(level, origin, cx + dx, baseY + 3, cz, f, DEEPSLATE_TILE_SLAB);
        }
        // Window in the center
        placeAt(level, origin, cx, baseY, cz, f, LIGHT_GRAY_GLASS_PANE);
        placeAt(level, origin, cx, baseY + 1, cz, f, LIGHT_GRAY_GLASS_PANE);
        placeAt(level, origin, cx, baseY + 2, cz, f, DEEPSLATE_BRICK);
    }

    // =====================================================================
    // Porch
    // =====================================================================

    private static void buildPorch(ServerLevel level, BlockPos origin, Direction f) {
        int mid = WIDTH / 2;

        // Porch floor: 9 wide, 4 deep, raised 0 blocks (same as mansion floor)
        for (int lx = mid - 4; lx <= mid + 4; lx++) {
            for (int lz = -4; lz < 0; lz++) {
                placeAt(level, origin, lx, 0, lz, f, POLISHED_DEEPSLATE);
            }
        }

        // Steps leading up
        for (int lx = mid - 4; lx <= mid + 4; lx++) {
            placeAt(level, origin, lx, 0, -5, f,
                    stair(Blocks.POLISHED_DEEPSLATE_STAIRS, Direction.SOUTH, f, false));
        }

        // Quartz columns flanking entrance, 6 high
        int[] colX = {mid - 4, mid - 2, mid + 2, mid + 4};
        for (int cx : colX) {
            for (int ly = 1; ly <= 6; ly++) {
                placeAt(level, origin, cx, ly, -2, f, Blocks.QUARTZ_PILLAR.defaultBlockState());
            }
        }

        // Porch roof beam
        for (int lx = mid - 5; lx <= mid + 5; lx++) {
            placeAt(level, origin, lx, 7, -2, f, DEEPSLATE_TILE);
            placeAt(level, origin, lx, 7, -3, f, DEEPSLATE_TILE);
        }
        // Eave overhang
        for (int lx = mid - 6; lx <= mid + 6; lx++) {
            placeAt(level, origin, lx, 7, -4, f,
                    stair(Blocks.DEEPSLATE_TILE_STAIRS, Direction.SOUTH, f, false));
        }

        // Lanterns on outer columns
        placeAt(level, origin, mid - 4, 7, -2, f, Blocks.LANTERN.defaultBlockState());
        placeAt(level, origin, mid + 4, 7, -2, f, Blocks.LANTERN.defaultBlockState());

        // Gilded blackstone accents on column bases
        placeAt(level, origin, mid - 4, 1, -2, f, GILDED_BLACKSTONE);
        placeAt(level, origin, mid + 4, 1, -2, f, GILDED_BLACKSTONE);
    }

    // =====================================================================
    // Garden
    // =====================================================================

    private static void buildGarden(ServerLevel level, BlockPos origin, Direction f) {
        int mid = WIDTH / 2;

        // --- Fountain at rear garden center ---
        int fz = DEPTH + 7;
        // Outer basin: concentric rings of polished andesite stairs (inverted)
        for (int lx = mid - 3; lx <= mid + 3; lx++) {
            for (int lz = fz - 3; lz <= fz + 3; lz++) {
                placeAt(level, origin, lx, 0, lz, f, Blocks.POLISHED_ANDESITE.defaultBlockState());
            }
        }
        // Basin walls: inverted stairs
        for (int lx = mid - 3; lx <= mid + 3; lx++) {
            placeAt(level, origin, lx, 1, fz - 3, f,
                    stair(Blocks.POLISHED_ANDESITE_STAIRS, Direction.SOUTH, f, true));
            placeAt(level, origin, lx, 1, fz + 3, f,
                    stair(Blocks.POLISHED_ANDESITE_STAIRS, Direction.NORTH, f, true));
        }
        for (int lz = fz - 2; lz <= fz + 2; lz++) {
            placeAt(level, origin, mid - 3, 1, lz, f,
                    stair(Blocks.POLISHED_ANDESITE_STAIRS, Direction.EAST, f, true));
            placeAt(level, origin, mid + 3, 1, lz, f,
                    stair(Blocks.POLISHED_ANDESITE_STAIRS, Direction.WEST, f, true));
        }
        // Water fill
        for (int lx = mid - 2; lx <= mid + 2; lx++) {
            for (int lz = fz - 2; lz <= fz + 2; lz++) {
                placeAt(level, origin, lx, 1, lz, f, Blocks.WATER.defaultBlockState());
            }
        }
        // Central column
        placeAt(level, origin, mid, 1, fz, f, Blocks.STONE_BRICK_WALL.defaultBlockState());
        placeAt(level, origin, mid, 2, fz, f, Blocks.STONE_BRICK_WALL.defaultBlockState());
        placeAt(level, origin, mid, 3, fz, f, Blocks.STONE_BRICK_WALL.defaultBlockState());
        placeAt(level, origin, mid, 4, fz, f, Blocks.WATER.defaultBlockState());

        // --- Azalea hedges (2-tall) flanking garden path ---
        for (int lz = DEPTH; lz <= DEPTH + 4; lz++) {
            for (int side = -1; side <= 1; side += 2) {
                int hx = mid + side * 3;
                placeAt(level, origin, hx, 1, lz, f, Blocks.AZALEA_LEAVES.defaultBlockState());
                placeAt(level, origin, hx, 2, lz, f, Blocks.AZALEA_LEAVES.defaultBlockState());
            }
        }

        // --- Flower beds ---
        // Left bed
        for (int lz = DEPTH + 1; lz <= DEPTH + 3; lz++) {
            for (int lx = mid - 7; lx <= mid - 5; lx++) {
                placeAt(level, origin, lx, 0, lz, f, Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }
        placeAt(level, origin, mid - 7, 1, DEPTH + 1, f, Blocks.ALLIUM.defaultBlockState());
        placeAt(level, origin, mid - 6, 1, DEPTH + 1, f, Blocks.BLUE_ORCHID.defaultBlockState());
        placeAt(level, origin, mid - 5, 1, DEPTH + 1, f, Blocks.ALLIUM.defaultBlockState());
        placeAt(level, origin, mid - 7, 1, DEPTH + 2, f, Blocks.BLUE_ORCHID.defaultBlockState());
        placeAt(level, origin, mid - 6, 1, DEPTH + 2, f, Blocks.ALLIUM.defaultBlockState());
        placeAt(level, origin, mid - 5, 1, DEPTH + 2, f, Blocks.BLUE_ORCHID.defaultBlockState());
        placeAt(level, origin, mid - 7, 1, DEPTH + 3, f, Blocks.ALLIUM.defaultBlockState());
        placeAt(level, origin, mid - 6, 1, DEPTH + 3, f, Blocks.CORNFLOWER.defaultBlockState());
        placeAt(level, origin, mid - 5, 1, DEPTH + 3, f, Blocks.ALLIUM.defaultBlockState());

        // Right bed
        for (int lz = DEPTH + 1; lz <= DEPTH + 3; lz++) {
            for (int lx = mid + 5; lx <= mid + 7; lx++) {
                placeAt(level, origin, lx, 0, lz, f, Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }
        placeAt(level, origin, mid + 5, 1, DEPTH + 1, f, Blocks.BLUE_ORCHID.defaultBlockState());
        placeAt(level, origin, mid + 6, 1, DEPTH + 1, f, Blocks.ALLIUM.defaultBlockState());
        placeAt(level, origin, mid + 7, 1, DEPTH + 1, f, Blocks.BLUE_ORCHID.defaultBlockState());
        placeAt(level, origin, mid + 5, 1, DEPTH + 2, f, Blocks.ALLIUM.defaultBlockState());
        placeAt(level, origin, mid + 6, 1, DEPTH + 2, f, Blocks.BLUE_ORCHID.defaultBlockState());
        placeAt(level, origin, mid + 7, 1, DEPTH + 2, f, Blocks.ALLIUM.defaultBlockState());
        placeAt(level, origin, mid + 5, 1, DEPTH + 3, f, Blocks.CORNFLOWER.defaultBlockState());
        placeAt(level, origin, mid + 6, 1, DEPTH + 3, f, Blocks.ALLIUM.defaultBlockState());
        placeAt(level, origin, mid + 7, 1, DEPTH + 3, f, Blocks.CORNFLOWER.defaultBlockState());

        // --- Soul lantern posts flanking fountain ---
        for (int side = -1; side <= 1; side += 2) {
            int lx = mid + side * 5;
            placeAt(level, origin, lx, 1, fz, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
            placeAt(level, origin, lx, 2, fz, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
            placeAt(level, origin, lx, 3, fz, f, Blocks.SOUL_LANTERN.defaultBlockState());
        }
    }

    // =====================================================================
    // Perimeter wall
    // =====================================================================

    private static void buildPerimeterWall(ServerLevel level, BlockPos origin, Direction f) {
        int pad = 9; // wall at 9 blocks out
        // North and south runs
        for (int lx = -pad; lx <= WIDTH + pad - 1; lx++) {
            placeAt(level, origin, lx, 1, -pad, f, DEEPSLATE_BRICK_WALL);
            placeAt(level, origin, lx, 1, DEPTH + pad - 1, f, DEEPSLATE_BRICK_WALL);
        }
        // East and west runs
        for (int lz = -pad; lz <= DEPTH + pad - 1; lz++) {
            placeAt(level, origin, -pad, 1, lz, f, DEEPSLATE_BRICK_WALL);
            placeAt(level, origin, WIDTH + pad - 1, 1, lz, f, DEEPSLATE_BRICK_WALL);
        }
        // Gate pillars at front
        int mid = WIDTH / 2;
        for (int ly = 1; ly <= 3; ly++) {
            placeAt(level, origin, mid - 3, ly, -pad, f, POLISHED_DEEPSLATE);
            placeAt(level, origin, mid + 3, ly, -pad, f, POLISHED_DEEPSLATE);
        }
        placeAt(level, origin, mid - 3, 4, -pad, f, Blocks.SOUL_LANTERN.defaultBlockState());
        placeAt(level, origin, mid + 3, 4, -pad, f, Blocks.SOUL_LANTERN.defaultBlockState());
        // Remove wall at gate opening
        for (int lx = mid - 2; lx <= mid + 2; lx++) {
            placeAt(level, origin, lx, 1, -pad, f, AIR);
        }
    }

    // =====================================================================
    // Ground floor interior walls
    // =====================================================================

    /*
     * Ground floor layout (45 wide x 40 deep):
     *
     *  lz=0           : North exterior wall (front entrance centered)
     *  lz=1..10       : FOYER zone (center), side alcoves
     *  lz=11          : Transverse wall 1 (foyer back wall, grand arch center)
     *  lz=12..26      : Main wing zone
     *      lx=1..13   : LEFT WING (Dining hall lz=12..19, Kitchen lz=20..26)
     *      lx=14..30  : CENTER (Grand staircase lz=12..20, Great Hall lz=21..26)
     *      lx=31..43  : RIGHT WING (Armory lz=12..19, Washroom lz=20..26)
     *  lz=27          : Transverse wall 2
     *  lz=28..38      : GREAT HALL / THRONE ROOM (rear center)
     *  lz=39          : South exterior wall
     */

    private static void buildGroundFloorInteriorWalls(ServerLevel level, BlockPos origin, Direction f) {
        BlockState wall = DEEPSLATE_BRICK;
        BlockState accent = POLISHED_DEEPSLATE;
        BlockState log = DARK_OAK_LOG;

        int mid = WIDTH / 2; // 22

        // ---- Transverse wall at lz=11 (foyer back wall) ----
        // Grand arch: 9-wide opening (lx=18..26) at ly=1-5
        for (int lx = 1; lx < WIDTH - 1; lx++) {
            for (int ly = 1; ly < CEIL1; ly++) {
                boolean inArch = lx >= 18 && lx <= 26 && ly <= 5;
                if (!inArch) {
                    placeAt(level, origin, lx, ly, 11, f, wall);
                }
            }
            // Arch lintel
            if (lx >= 17 && lx <= 27) {
                placeAt(level, origin, lx, 6, 11, f, log);
            }
        }
        // Doorways in the transverse wall for left and right wings
        // Left doorway at lx=5..7
        for (int ly = 1; ly <= 4; ly++) {
            for (int lx = 5; lx <= 7; lx++) {
                placeAt(level, origin, lx, ly, 11, f, AIR);
            }
        }
        // Right doorway at lx=37..39
        for (int ly = 1; ly <= 4; ly++) {
            for (int lx = 37; lx <= 39; lx++) {
                placeAt(level, origin, lx, ly, 11, f, AIR);
            }
        }

        // ---- Longitudinal wall lx=14 from lz=11 to lz=27 (left wing east wall) ----
        for (int lz = 12; lz <= 26; lz++) {
            for (int ly = 1; ly < CEIL1; ly++) {
                boolean door = lz >= 14 && lz <= 16 && ly <= 4;
                boolean door2 = lz >= 22 && lz <= 24 && ly <= 4;
                if (!door && !door2) {
                    placeAt(level, origin, 14, ly, lz, f, wall);
                }
            }
        }

        // ---- Longitudinal wall lx=30 from lz=11 to lz=27 (right wing west wall) ----
        for (int lz = 12; lz <= 26; lz++) {
            for (int ly = 1; ly < CEIL1; ly++) {
                boolean door = lz >= 14 && lz <= 16 && ly <= 4;
                boolean door2 = lz >= 22 && lz <= 24 && ly <= 4;
                if (!door && !door2) {
                    placeAt(level, origin, 30, ly, lz, f, wall);
                }
            }
        }

        // ---- Kitchen/dining divider wall at lz=20 (left wing only, lx=1..13) ----
        for (int lx = 1; lx <= 13; lx++) {
            for (int ly = 1; ly < CEIL1; ly++) {
                boolean door = lx >= 6 && lx <= 8 && ly <= 4;
                if (!door) {
                    placeAt(level, origin, lx, ly, 20, f, wall);
                }
            }
        }

        // ---- Armory/washroom divider at lz=20 (right wing only, lx=31..43) ----
        for (int lx = 31; lx <= 43; lx++) {
            for (int ly = 1; ly < CEIL1; ly++) {
                boolean door = lx >= 36 && lx <= 38 && ly <= 4;
                if (!door) {
                    placeAt(level, origin, lx, ly, 20, f, wall);
                }
            }
        }

        // ---- Transverse wall at lz=27 (rear hall front wall) ----
        // Wide opening center (lx=18..26)
        for (int lx = 1; lx < WIDTH - 1; lx++) {
            for (int ly = 1; ly < CEIL1; ly++) {
                boolean inArch = lx >= 18 && lx <= 26 && ly <= 5;
                if (!inArch) {
                    placeAt(level, origin, lx, ly, 27, f, wall);
                }
            }
            if (lx >= 17 && lx <= 27) {
                placeAt(level, origin, lx, 6, 27, f, log);
            }
        }
        // Side doorways in lz=27 wall
        for (int ly = 1; ly <= 4; ly++) {
            for (int lx = 5; lx <= 7; lx++) {
                placeAt(level, origin, lx, ly, 27, f, AIR);
            }
            for (int lx = 37; lx <= 39; lx++) {
                placeAt(level, origin, lx, ly, 27, f, AIR);
            }
        }

        // ---- Accent: polished deepslate base course on interior walls ----
        for (int lx = 1; lx < WIDTH - 1; lx++) {
            placeAt(level, origin, lx, 1, 11, f, accent);
            placeAt(level, origin, lx, 1, 27, f, accent);
        }
        for (int lz = 12; lz <= 26; lz++) {
            placeAt(level, origin, 14, 1, lz, f, accent);
            placeAt(level, origin, 30, 1, lz, f, accent);
        }
    }

    // =====================================================================
    // Grand staircase (imperial style)
    // =====================================================================

    private static void buildGrandStaircase(ServerLevel level, BlockPos origin, Direction f) {
        /*
         * Imperial staircase layout (center zone, lx=17..27, lz=12..20):
         * - 5-wide central run goes south from lz=12 to lz=16 (rising from ly=1 to ly=4)
         * - Full-width landing at lz=16, ly=4
         * - Splits: left run (lx=15..17) goes south lz=17..20 rising to ly=8
         *           right run (lx=27..29) goes south lz=17..20 rising to ly=8
         */

        int mid = WIDTH / 2; // 22
        BlockState riser = DARK_OAK_PLANKS;
        BlockState banister = Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState();

        // --- Central run: 5-wide (lx=20..24), lz=12..15, rises from ly=1 to ly=4 ---
        for (int step = 0; step < 4; step++) {
            int ly = 1 + step;
            int lz = 12 + step;
            for (int lx = 20; lx <= 24; lx++) {
                for (int sy = 1; sy < ly; sy++) {
                    placeAt(level, origin, lx, sy, lz, f, riser);
                }
                placeAt(level, origin, lx, ly, lz, f,
                        stair(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, f, false));
            }
        }

        // --- Landing at lz=16..17, ly=4, full width lx=16..28 ---
        for (int lx = 16; lx <= 28; lx++) {
            for (int lz = 16; lz <= 17; lz++) {
                for (int sy = 1; sy <= 4; sy++) {
                    placeAt(level, origin, lx, sy, lz, f, riser);
                }
            }
        }

        // --- Left run: lx=16..18, lz=18..21, rises from ly=5 to ly=8 ---
        for (int step = 0; step < 4; step++) {
            int ly = 5 + step;
            int lz = 18 + step;
            for (int lx = 16; lx <= 18; lx++) {
                for (int sy = 1; sy < ly; sy++) {
                    placeAt(level, origin, lx, sy, lz, f, riser);
                }
                placeAt(level, origin, lx, ly, lz, f,
                        stair(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, f, false));
            }
        }

        // --- Right run: lx=26..28, lz=18..21, rises from ly=5 to ly=8 ---
        for (int step = 0; step < 4; step++) {
            int ly = 5 + step;
            int lz = 18 + step;
            for (int lx = 26; lx <= 28; lx++) {
                for (int sy = 1; sy < ly; sy++) {
                    placeAt(level, origin, lx, sy, lz, f, riser);
                }
                placeAt(level, origin, lx, ly, lz, f,
                        stair(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, f, false));
            }
        }

        // --- Remove second floor above stairwell ---
        for (int lz = 12; lz <= 21; lz++) {
            for (int lx = 15; lx <= 29; lx++) {
                placeAt(level, origin, lx, CEIL1, lz, f, AIR);
                placeAt(level, origin, lx, FLOOR2, lz, f, AIR);
            }
        }

        // --- Banister posts every 2 steps on central run ---
        for (int step = 0; step < 4; step += 2) {
            int ly = 2 + step;
            int lz = 12 + step;
            placeAt(level, origin, 19, ly, lz, f, banister);
            placeAt(level, origin, 25, ly, lz, f, banister);
            placeAt(level, origin, 19, ly + 1, lz, f, Blocks.LANTERN.defaultBlockState());
            placeAt(level, origin, 25, ly + 1, lz, f, Blocks.LANTERN.defaultBlockState());
        }

        // --- Banister posts on split runs ---
        for (int step = 0; step < 4; step += 2) {
            int ly = 6 + step;
            int lz = 18 + step;
            // Left run outer
            placeAt(level, origin, 15, ly, lz, f, banister);
            placeAt(level, origin, 15, ly + 1, lz, f, Blocks.LANTERN.defaultBlockState());
            // Right run outer
            placeAt(level, origin, 29, ly, lz, f, banister);
            placeAt(level, origin, 29, ly + 1, lz, f, Blocks.LANTERN.defaultBlockState());
        }

        // --- Second floor railings around the stairwell opening ---
        for (int lx = 15; lx <= 29; lx++) {
            placeAt(level, origin, lx, FLOOR2 + 1, 12, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
        }
        for (int lz = 12; lz <= 21; lz++) {
            placeAt(level, origin, 15, FLOOR2 + 1, lz, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
            placeAt(level, origin, 29, FLOOR2 + 1, lz, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
        }
    }

    // =====================================================================
    // Chandelier helper
    // =====================================================================

    /** Small chandelier: chain + fence + 4 end rods + lanterns below */
    private static void placeSmallChandelier(ServerLevel level, BlockPos origin,
                                              int cx, int cy, int cz, Direction f) {
        placeAt(level, origin, cx, cy, cz, f, CHAIN);
        placeAt(level, origin, cx, cy - 1, cz, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
        // End rods pointing outward (they point up/down by default, which looks like candles)
        placeAt(level, origin, cx + 1, cy - 1, cz, f, Blocks.END_ROD.defaultBlockState());
        placeAt(level, origin, cx - 1, cy - 1, cz, f, Blocks.END_ROD.defaultBlockState());
        placeAt(level, origin, cx, cy - 1, cz + 1, f, Blocks.END_ROD.defaultBlockState());
        placeAt(level, origin, cx, cy - 1, cz - 1, f, Blocks.END_ROD.defaultBlockState());
        // Lanterns below end rods
        placeAt(level, origin, cx + 1, cy - 2, cz, f, hangingLantern());
        placeAt(level, origin, cx - 1, cy - 2, cz, f, hangingLantern());
        placeAt(level, origin, cx, cy - 2, cz + 1, f, hangingLantern());
        placeAt(level, origin, cx, cy - 2, cz - 1, f, hangingLantern());
    }

    /** Large 3x3 chandelier for the great hall */
    private static void placeLargeChandelier(ServerLevel level, BlockPos origin,
                                              int cx, int cy, int cz, Direction f) {
        // Central chain column
        placeAt(level, origin, cx, cy, cz, f, CHAIN);
        placeAt(level, origin, cx, cy - 1, cz, f, CHAIN);
        placeAt(level, origin, cx, cy - 2, cz, f, Blocks.DARK_OAK_FENCE.defaultBlockState());

        // Ring of fences at cy-2
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                placeAt(level, origin, cx + dx, cy - 2, cz + dz, f,
                        Blocks.DARK_OAK_FENCE.defaultBlockState());
            }
        }

        // End rods extending outward from the ring
        placeAt(level, origin, cx + 2, cy - 2, cz, f, Blocks.END_ROD.defaultBlockState());
        placeAt(level, origin, cx - 2, cy - 2, cz, f, Blocks.END_ROD.defaultBlockState());
        placeAt(level, origin, cx, cy - 2, cz + 2, f, Blocks.END_ROD.defaultBlockState());
        placeAt(level, origin, cx, cy - 2, cz - 2, f, Blocks.END_ROD.defaultBlockState());

        // Lanterns hanging below
        placeAt(level, origin, cx + 2, cy - 3, cz, f, hangingLantern());
        placeAt(level, origin, cx - 2, cy - 3, cz, f, hangingLantern());
        placeAt(level, origin, cx, cy - 3, cz + 2, f, hangingLantern());
        placeAt(level, origin, cx, cy - 3, cz - 2, f, hangingLantern());
        // Corner lanterns
        placeAt(level, origin, cx + 1, cy - 3, cz + 1, f, hangingLantern());
        placeAt(level, origin, cx - 1, cy - 3, cz + 1, f, hangingLantern());
        placeAt(level, origin, cx + 1, cy - 3, cz - 1, f, hangingLantern());
        placeAt(level, origin, cx - 1, cy - 3, cz - 1, f, hangingLantern());
    }

    // =====================================================================
    // Fireplace helper
    // =====================================================================

    private static void placeFireplace(ServerLevel level, BlockPos origin,
                                        int lx, int ly, int lz, Direction f,
                                        Direction wallFacing) {
        // 3-wide, 3-tall opening framed in polished blackstone stairs
        // wallFacing = direction the fireplace faces INTO the room
        // The fireplace is built INTO the wall at lz

        // Mantle (top slab)
        for (int dx = -1; dx <= 1; dx++) {
            placeAt(level, origin, lx + dx, ly + 3, lz, f,
                    slab(Blocks.POLISHED_BLACKSTONE_SLAB, false));
        }

        // Frame: polished blackstone stairs
        // Left pillar
        placeAt(level, origin, lx - 1, ly, lz, f,
                stair(Blocks.POLISHED_BLACKSTONE_STAIRS, wallFacing, f, false));
        placeAt(level, origin, lx - 1, ly + 1, lz, f,
                stair(Blocks.POLISHED_BLACKSTONE_STAIRS, wallFacing, f, false));
        placeAt(level, origin, lx - 1, ly + 2, lz, f,
                stair(Blocks.POLISHED_BLACKSTONE_STAIRS, wallFacing, f, true));
        // Right pillar
        placeAt(level, origin, lx + 1, ly, lz, f,
                stair(Blocks.POLISHED_BLACKSTONE_STAIRS, wallFacing, f, false));
        placeAt(level, origin, lx + 1, ly + 1, lz, f,
                stair(Blocks.POLISHED_BLACKSTONE_STAIRS, wallFacing, f, false));
        placeAt(level, origin, lx + 1, ly + 2, lz, f,
                stair(Blocks.POLISHED_BLACKSTONE_STAIRS, wallFacing, f, true));

        // Opening (air + campfire at bottom)
        placeAt(level, origin, lx, ly, lz, f, Blocks.CAMPFIRE.defaultBlockState());
        placeAt(level, origin, lx, ly + 1, lz, f, AIR);
        placeAt(level, origin, lx, ly + 2, lz, f, AIR);

        // Back wall of fireplace (deeper into the wall)
        // Chimney above
        for (int dy = 3; dy <= 6; dy++) {
            placeAt(level, origin, lx, ly + dy, lz, f, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        }
    }

    // =====================================================================
    // Ground floor rooms
    // =====================================================================

    private static void furnishFoyer(ServerLevel level, BlockPos origin, Direction f) {
        // Foyer: lx=1..43, lz=1..10
        int mid = WIDTH / 2;

        // Checkerboard floor (polished basalt + quartz)
        for (int lx = 1; lx < WIDTH - 1; lx++) {
            for (int lz = 1; lz <= 10; lz++) {
                BlockState tile = ((lx + lz) % 2 == 0) ? POLISHED_BASALT : QUARTZ_BLOCK;
                placeAt(level, origin, lx, FLOOR1, lz, f, tile);
            }
        }

        // Red carpet runner down center
        for (int lz = 1; lz <= 10; lz++) {
            for (int lx = mid - 2; lx <= mid + 2; lx++) {
                placeAt(level, origin, lx, 1, lz, f, Blocks.RED_CARPET.defaultBlockState());
            }
        }

        // Chandeliers
        placeSmallChandelier(level, origin, mid - 8, CEIL1, 5, f);
        placeSmallChandelier(level, origin, mid, CEIL1, 5, f);
        placeSmallChandelier(level, origin, mid + 8, CEIL1, 5, f);

        // Banners on walls flanking the arch (lz=10)
        for (int dx = -10; dx <= 10; dx += 5) {
            if (Math.abs(dx) < 3) continue;
            placeAt(level, origin, mid + dx, 4, 10, f, Blocks.PURPLE_BANNER.defaultBlockState());
        }

        // Potted plants flanking entrance
        placeAt(level, origin, mid - 5, 1, 2, f, Blocks.POTTED_FERN.defaultBlockState());
        placeAt(level, origin, mid + 5, 1, 2, f, Blocks.POTTED_FERN.defaultBlockState());
        placeAt(level, origin, mid - 5, 1, 8, f, Blocks.POTTED_AZURE_BLUET.defaultBlockState());
        placeAt(level, origin, mid + 5, 1, 8, f, Blocks.POTTED_AZURE_BLUET.defaultBlockState());

        // Wall lanterns
        placeAt(level, origin, 2, 4, 3, f, Blocks.LANTERN.defaultBlockState());
        placeAt(level, origin, WIDTH - 3, 4, 3, f, Blocks.LANTERN.defaultBlockState());
        placeAt(level, origin, 2, 4, 8, f, Blocks.LANTERN.defaultBlockState());
        placeAt(level, origin, WIDTH - 3, 4, 8, f, Blocks.LANTERN.defaultBlockState());
    }

    private static void furnishDiningHall(ServerLevel level, BlockPos origin, Direction f) {
        // Dining hall: lx=1..13, lz=12..19

        // Long 10-seat table (fence + pressure plate)
        for (int lz = 13; lz <= 18; lz++) {
            for (int lx = 5; lx <= 10; lx++) {
                placeAt(level, origin, lx, 1, lz, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
                placeAt(level, origin, lx, 2, lz, f, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
            }
        }

        // Food on table
        placeAt(level, origin, 6, 2, 14, f, Blocks.CAKE.defaultBlockState());
        placeAt(level, origin, 8, 2, 16, f, Blocks.PUMPKIN.defaultBlockState());
        placeAt(level, origin, 7, 2, 15, f, Blocks.MELON.defaultBlockState());

        // Chairs with armrests (stair + trapdoors on sides)
        Direction chairE = rotateDir(Direction.EAST, f);
        Direction chairW = rotateDir(Direction.WEST, f);
        for (int lz = 13; lz <= 18; lz++) {
            // Left side chairs
            placeAt(level, origin, 4, 1, lz, f,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, chairE));
            // Right side chairs
            placeAt(level, origin, 11, 1, lz, f,
                    Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, chairW));
        }
        // Head and foot chairs
        placeAt(level, origin, 7, 1, 12, f,
                stair(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, f, false));
        placeAt(level, origin, 7, 1, 19, f,
                stair(Blocks.DARK_OAK_STAIRS, Direction.NORTH, f, false));

        // Fireplace on west wall
        placeFireplace(level, origin, 2, 1, 16, f, Direction.EAST);

        // Chandeliers
        placeSmallChandelier(level, origin, 7, CEIL1, 14, f);
        placeSmallChandelier(level, origin, 7, CEIL1, 17, f);

        // Wall lanterns
        placeAt(level, origin, 1, 4, 13, f, Blocks.LANTERN.defaultBlockState());
        placeAt(level, origin, 1, 4, 18, f, Blocks.LANTERN.defaultBlockState());

        // Brown carpet border
        for (int lz = 12; lz <= 19; lz++) {
            placeAt(level, origin, 1, 1, lz, f, Blocks.BROWN_CARPET.defaultBlockState());
            placeAt(level, origin, 13, 1, lz, f, Blocks.BROWN_CARPET.defaultBlockState());
        }
    }

    private static void furnishKitchen(ServerLevel level, BlockPos origin, Direction f) {
        // Kitchen: lx=1..13, lz=21..26

        // Counter along south wall
        placeAt(level, origin, 2, 1, 25, f, Blocks.FURNACE.defaultBlockState());
        placeAt(level, origin, 3, 1, 25, f, Blocks.FURNACE.defaultBlockState());
        placeAt(level, origin, 4, 1, 25, f, Blocks.FURNACE.defaultBlockState());
        placeAt(level, origin, 5, 1, 25, f, Blocks.SMOKER.defaultBlockState());
        placeAt(level, origin, 6, 1, 25, f, Blocks.BLAST_FURNACE.defaultBlockState());
        placeAt(level, origin, 7, 1, 25, f, Blocks.CRAFTING_TABLE.defaultBlockState());
        placeAt(level, origin, 8, 1, 25, f, Blocks.CRAFTING_TABLE.defaultBlockState());

        // Barrel storage along west wall
        for (int lz = 21; lz <= 24; lz++) {
            placeAt(level, origin, 1, 1, lz, f, Blocks.BARREL.defaultBlockState());
            placeAt(level, origin, 1, 2, lz, f, Blocks.BARREL.defaultBlockState());
            placeAt(level, origin, 2, 1, lz, f, Blocks.BARREL.defaultBlockState());
        }

        // Cauldrons
        placeAt(level, origin, 10, 1, 25, f, Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, 3));
        placeAt(level, origin, 11, 1, 25, f, Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, 3));
        placeAt(level, origin, 12, 1, 25, f, Blocks.CAULDRON.defaultBlockState());

        // Composter
        placeAt(level, origin, 9, 1, 25, f, Blocks.COMPOSTER.defaultBlockState());

        // Central island (table)
        for (int lx = 5; lx <= 9; lx++) {
            placeAt(level, origin, lx, 1, 22, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
            placeAt(level, origin, lx, 2, 22, f, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
        }

        // Lighting
        placeSmallChandelier(level, origin, 7, CEIL1, 23, f);
        placeAt(level, origin, 12, 4, 21, f, Blocks.LANTERN.defaultBlockState());
    }

    private static void furnishGreatHall(ServerLevel level, BlockPos origin, Direction f) {
        // Great Hall / Throne Room: full width lx=1..43, lz=28..38
        int mid = WIDTH / 2;

        // Carpet runner from entrance to throne
        for (int lz = 28; lz <= 37; lz++) {
            for (int lx = mid - 2; lx <= mid + 2; lx++) {
                placeAt(level, origin, lx, 1, lz, f, Blocks.RED_CARPET.defaultBlockState());
            }
        }

        // Throne at far end (quartz stairs with quartz slab armrests, wool banner behind)
        placeAt(level, origin, mid, 1, 37, f,
                stair(Blocks.QUARTZ_STAIRS, Direction.NORTH, f, false));
        // Armrests
        placeAt(level, origin, mid - 1, 1, 37, f,
                slab(Blocks.QUARTZ_SLAB, false));
        placeAt(level, origin, mid + 1, 1, 37, f,
                slab(Blocks.QUARTZ_SLAB, false));
        // Banner behind throne (colored wool as substitute)
        placeAt(level, origin, mid, 3, 38, f, Blocks.PURPLE_WOOL.defaultBlockState());
        placeAt(level, origin, mid, 4, 38, f, Blocks.PURPLE_WOOL.defaultBlockState());
        placeAt(level, origin, mid - 1, 3, 38, f, Blocks.BLACK_WOOL.defaultBlockState());
        placeAt(level, origin, mid + 1, 3, 38, f, Blocks.BLACK_WOOL.defaultBlockState());

        // Large chandeliers
        placeLargeChandelier(level, origin, mid - 8, CEIL1, 33, f);
        placeLargeChandelier(level, origin, mid, CEIL1, 33, f);
        placeLargeChandelier(level, origin, mid + 8, CEIL1, 33, f);

        // Fireplace on south wall
        placeFireplace(level, origin, mid - 10, 1, 38, f, Direction.NORTH);
        placeFireplace(level, origin, mid + 10, 1, 38, f, Direction.NORTH);

        // Bookshelves along east and west walls
        for (int lz = 29; lz <= 37; lz += 2) {
            placeAt(level, origin, 1, 1, lz, f, Blocks.BOOKSHELF.defaultBlockState());
            placeAt(level, origin, 1, 2, lz, f, Blocks.BOOKSHELF.defaultBlockState());
            placeAt(level, origin, WIDTH - 2, 1, lz, f, Blocks.BOOKSHELF.defaultBlockState());
            placeAt(level, origin, WIDTH - 2, 2, lz, f, Blocks.BOOKSHELF.defaultBlockState());
        }

        // Banners along walls
        for (int lz = 29; lz <= 37; lz += 4) {
            placeAt(level, origin, 2, 4, lz, f, Blocks.BLUE_BANNER.defaultBlockState());
            placeAt(level, origin, WIDTH - 3, 4, lz, f, Blocks.BLUE_BANNER.defaultBlockState());
        }

        // Wall lanterns
        for (int lz = 29; lz <= 37; lz += 3) {
            placeAt(level, origin, 1, 4, lz, f, Blocks.LANTERN.defaultBlockState());
            placeAt(level, origin, WIDTH - 2, 4, lz, f, Blocks.LANTERN.defaultBlockState());
        }
    }

    private static void furnishArmory(ServerLevel level, BlockPos origin, Direction f) {
        // Armory: lx=31..43, lz=12..19

        // Polished deepslate floor
        for (int lx = 31; lx <= 43; lx++) {
            for (int lz = 12; lz <= 19; lz++) {
                placeAt(level, origin, lx, FLOOR1, lz, f, POLISHED_DEEPSLATE);
            }
        }

        // Workstations
        placeAt(level, origin, 33, 1, 13, f, Blocks.ANVIL.defaultBlockState());
        placeAt(level, origin, 35, 1, 13, f, Blocks.GRINDSTONE.defaultBlockState());
        placeAt(level, origin, 37, 1, 13, f, Blocks.SMITHING_TABLE.defaultBlockState());
        placeAt(level, origin, 39, 1, 13, f, Blocks.STONECUTTER.defaultBlockState());

        // Weapon racks (fences on walls)
        for (int lz = 14; lz <= 18; lz += 2) {
            placeAt(level, origin, 43, 2, lz, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
            placeAt(level, origin, 43, 3, lz, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
        }

        // Chest storage
        placeAt(level, origin, 32, 1, 18, f, Blocks.CHEST.defaultBlockState());
        placeAt(level, origin, 33, 1, 18, f, Blocks.CHEST.defaultBlockState());
        placeAt(level, origin, 34, 1, 18, f, Blocks.BARREL.defaultBlockState());
        placeAt(level, origin, 35, 1, 18, f, Blocks.BARREL.defaultBlockState());

        // Trophy banners
        placeAt(level, origin, 43, 3, 13, f, Blocks.RED_BANNER.defaultBlockState());
        placeAt(level, origin, 43, 3, 15, f, Blocks.BLUE_BANNER.defaultBlockState());
        placeAt(level, origin, 43, 3, 17, f, Blocks.YELLOW_BANNER.defaultBlockState());

        // Lighting
        placeSmallChandelier(level, origin, 37, CEIL1, 15, f);
        placeAt(level, origin, 43, 4, 19, f, Blocks.LANTERN.defaultBlockState());
    }

    private static void furnishWashroom(ServerLevel level, BlockPos origin, Direction f) {
        // Washroom: lx=31..43, lz=21..26

        // Water cauldrons
        placeAt(level, origin, 35, 1, 24, f, Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, 3));
        placeAt(level, origin, 36, 1, 24, f, Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, 3));
        placeAt(level, origin, 37, 1, 24, f, Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, 3));
        placeAt(level, origin, 38, 1, 24, f, Blocks.CAULDRON.defaultBlockState());

        // Stripped log fixtures
        placeAt(level, origin, 34, 1, 23, f, STRIPPED_DARK_OAK);
        placeAt(level, origin, 39, 1, 23, f, STRIPPED_DARK_OAK);

        // Mirror (polished andesite on wall)
        for (int lx = 35; lx <= 38; lx++) {
            placeAt(level, origin, lx, 3, 26, f, Blocks.POLISHED_ANDESITE.defaultBlockState());
            placeAt(level, origin, lx, 4, 26, f, Blocks.POLISHED_ANDESITE.defaultBlockState());
        }

        // Lighting
        placeAt(level, origin, 37, 4, 26, f, Blocks.SEA_LANTERN.defaultBlockState());
        placeAt(level, origin, 33, 4, 22, f, Blocks.LANTERN.defaultBlockState());
        placeAt(level, origin, 41, 4, 22, f, Blocks.LANTERN.defaultBlockState());

        // Decorative pots
        placeAt(level, origin, 32, 1, 22, f, Blocks.POTTED_FERN.defaultBlockState());
        placeAt(level, origin, 42, 1, 22, f, Blocks.POTTED_BAMBOO.defaultBlockState());
    }

    // =====================================================================
    // Second floor interior walls
    // =====================================================================

    /*
     * Second floor layout (45 wide x 40 deep):
     *
     *  lz=1..14       : Front section
     *      lx=1..14   : Master bedroom
     *      lx=15..29  : Open stairwell / balcony overlooking foyer
     *      lx=30..43  : Guest bedroom
     *  lz=15          : Transverse wall (doors to front rooms)
     *  lz=16..26      : Middle section
     *      lx=1..21   : Library / Study (with enchanting)
     *      lx=22..43  : Brewing Lab
     *  lz=27          : Transverse wall 2
     *  lz=28..38      : Rear section
     *      lx=1..21   : Lounge
     *      lx=22..43  : Rear balcony area
     */

    private static void buildSecondFloorInteriorWalls(ServerLevel level, BlockPos origin, Direction f) {
        BlockState wall = DEEPSLATE_BRICK;

        // ---- Transverse wall at lz=15 ----
        for (int lx = 1; lx < WIDTH - 1; lx++) {
            for (int ly = FLOOR2 + 1; ly < CEIL2; ly++) {
                // Skip the stairwell area
                if (lx >= 15 && lx <= 29) continue;
                // Door into master bedroom at lx=7..9
                boolean door1 = lx >= 7 && lx <= 9 && ly <= FLOOR2 + 4;
                // Door into guest bedroom at lx=35..37
                boolean door2 = lx >= 35 && lx <= 37 && ly <= FLOOR2 + 4;
                if (!door1 && !door2) {
                    placeAt(level, origin, lx, ly, 15, f, wall);
                }
            }
        }

        // ---- Longitudinal wall lx=14 from lz=1 to lz=14 (master/stairwell divider) ----
        for (int lz = 1; lz <= 14; lz++) {
            for (int ly = FLOOR2 + 1; ly < CEIL2; ly++) {
                boolean door = lz >= 6 && lz <= 8 && ly <= FLOOR2 + 4;
                if (!door) {
                    placeAt(level, origin, 14, ly, lz, f, wall);
                }
            }
        }

        // ---- Longitudinal wall lx=30 from lz=1 to lz=14 (stairwell/guest divider) ----
        for (int lz = 1; lz <= 14; lz++) {
            for (int ly = FLOOR2 + 1; ly < CEIL2; ly++) {
                boolean door = lz >= 6 && lz <= 8 && ly <= FLOOR2 + 4;
                if (!door) {
                    placeAt(level, origin, 30, ly, lz, f, wall);
                }
            }
        }

        // ---- Transverse wall at lz=27 (second floor rear) ----
        for (int lx = 1; lx < WIDTH - 1; lx++) {
            for (int ly = FLOOR2 + 1; ly < CEIL2; ly++) {
                boolean door1 = lx >= 10 && lx <= 12 && ly <= FLOOR2 + 4;
                boolean door2 = lx >= 32 && lx <= 34 && ly <= FLOOR2 + 4;
                if (!door1 && !door2) {
                    placeAt(level, origin, lx, ly, 27, f, wall);
                }
            }
        }

        // ---- Longitudinal wall lx=21 from lz=16 to lz=38 (library/brewing and lounge divider) ----
        for (int lz = 16; lz <= 38; lz++) {
            for (int ly = FLOOR2 + 1; ly < CEIL2; ly++) {
                boolean door1 = lz >= 20 && lz <= 22 && ly <= FLOOR2 + 4;
                boolean door2 = lz >= 31 && lz <= 33 && ly <= FLOOR2 + 4;
                if (!door1 && !door2) {
                    placeAt(level, origin, 21, ly, lz, f, wall);
                }
            }
        }

        // ---- Rear balcony: open south wall on second floor, lx=22..43, lz=DEPTH-1 ----
        for (int lx = 22; lx <= 43; lx++) {
            placeAt(level, origin, lx, FLOOR2 + 1, DEPTH - 1, f,
                    Blocks.DARK_OAK_FENCE.defaultBlockState());
            placeAt(level, origin, lx, FLOOR2 + 2, DEPTH - 1, f, AIR);
            placeAt(level, origin, lx, FLOOR2 + 3, DEPTH - 1, f, AIR);
        }
        // Balcony floor extension
        for (int lx = 22; lx <= 43; lx++) {
            placeAt(level, origin, lx, FLOOR2, DEPTH, f, DARK_OAK_PLANKS);
            placeAt(level, origin, lx, FLOOR2 + 1, DEPTH, f,
                    Blocks.DARK_OAK_FENCE.defaultBlockState());
        }
        // Corner posts
        for (int ly = FLOOR2 + 1; ly <= FLOOR2 + 3; ly++) {
            placeAt(level, origin, 22, ly, DEPTH, f, POLISHED_DEEPSLATE);
            placeAt(level, origin, 43, ly, DEPTH, f, POLISHED_DEEPSLATE);
        }
    }

    // =====================================================================
    // Second floor rooms
    // =====================================================================

    private static void furnishMasterBedroom(ServerLevel level, BlockPos origin, Direction f) {
        // Master bedroom: lx=1..13, lz=1..14
        Direction bedDir = rotateDir(Direction.NORTH, f);

        // Carpet throughout
        for (int lx = 1; lx <= 13; lx++) {
            for (int lz = 1; lz <= 14; lz++) {
                placeAt(level, origin, lx, FLOOR2, lz, f, Blocks.PURPLE_CARPET.defaultBlockState());
            }
        }

        // Large canopy bed: 3-wide at lz=4..5, lx=5..7
        for (int lx = 5; lx <= 7; lx++) {
            placeAt(level, origin, lx, FLOOR2 + 1, 5, f,
                    Blocks.RED_BED.defaultBlockState()
                            .setValue(BedBlock.FACING, bedDir)
                            .setValue(BedBlock.PART, BedPart.HEAD));
            placeAt(level, origin, lx, FLOOR2 + 1, 4, f,
                    Blocks.RED_BED.defaultBlockState()
                            .setValue(BedBlock.FACING, bedDir)
                            .setValue(BedBlock.PART, BedPart.FOOT));
        }
        // Canopy posts (fence pillars at bed corners)
        int[] canopyX = {4, 8};
        for (int cx : canopyX) {
            for (int ly = FLOOR2 + 1; ly <= FLOOR2 + 4; ly++) {
                placeAt(level, origin, cx, ly, 3, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
                placeAt(level, origin, cx, ly, 6, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
            }
        }
        // Canopy top (wool)
        for (int lx = 4; lx <= 8; lx++) {
            for (int lz = 3; lz <= 6; lz++) {
                placeAt(level, origin, lx, FLOOR2 + 5, lz, f, Blocks.PURPLE_WOOL.defaultBlockState());
            }
        }

        // Nightstands
        placeAt(level, origin, 4, FLOOR2 + 1, 4, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
        placeAt(level, origin, 4, FLOOR2 + 2, 4, f, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
        placeAt(level, origin, 8, FLOOR2 + 1, 4, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
        placeAt(level, origin, 8, FLOOR2 + 2, 4, f, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
        // Lanterns on nightstands
        placeAt(level, origin, 4, FLOOR2 + 3, 4, f, Blocks.LANTERN.defaultBlockState());
        placeAt(level, origin, 8, FLOOR2 + 3, 4, f, Blocks.LANTERN.defaultBlockState());

        // Bookshelves along north wall
        for (int lx = 1; lx <= 12; lx++) {
            placeAt(level, origin, lx, FLOOR2 + 1, 1, f, Blocks.BOOKSHELF.defaultBlockState());
            placeAt(level, origin, lx, FLOOR2 + 2, 1, f, Blocks.BOOKSHELF.defaultBlockState());
        }

        // Fireplace on west wall
        placeFireplace(level, origin, 2, FLOOR2 + 1, 10, f, Direction.EAST);

        // Chest and wardrobe
        placeAt(level, origin, 11, FLOOR2 + 1, 3, f, Blocks.CHEST.defaultBlockState());
        placeAt(level, origin, 12, FLOOR2 + 1, 3, f, Blocks.CHEST.defaultBlockState());
        placeAt(level, origin, 12, FLOOR2 + 1, 5, f, Blocks.BARREL.defaultBlockState());

        // Desk
        placeAt(level, origin, 10, FLOOR2 + 1, 12, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
        placeAt(level, origin, 11, FLOOR2 + 1, 12, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
        placeAt(level, origin, 10, FLOOR2 + 2, 12, f, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
        placeAt(level, origin, 11, FLOOR2 + 2, 12, f, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
        // Chair
        placeAt(level, origin, 10, FLOOR2 + 1, 13, f,
                stair(Blocks.DARK_OAK_STAIRS, Direction.NORTH, f, false));

        // Chandelier
        placeSmallChandelier(level, origin, 7, CEIL2, 8, f);

        // Wall lanterns
        placeAt(level, origin, 1, FLOOR2 + 4, 8, f, Blocks.LANTERN.defaultBlockState());
        placeAt(level, origin, 13, FLOOR2 + 4, 8, f, Blocks.LANTERN.defaultBlockState());
    }

    private static void furnishGuestBedroom(ServerLevel level, BlockPos origin, Direction f) {
        // Guest bedroom: lx=31..43, lz=1..14
        Direction bedDir = rotateDir(Direction.NORTH, f);

        // Carpet
        for (int lx = 31; lx <= 43; lx++) {
            for (int lz = 1; lz <= 14; lz++) {
                placeAt(level, origin, lx, FLOOR2, lz, f, Blocks.LIGHT_BLUE_CARPET.defaultBlockState());
            }
        }

        // Bed (2-wide)
        for (int lx = 36; lx <= 37; lx++) {
            placeAt(level, origin, lx, FLOOR2 + 1, 4, f,
                    Blocks.CYAN_BED.defaultBlockState()
                            .setValue(BedBlock.FACING, bedDir)
                            .setValue(BedBlock.PART, BedPart.HEAD));
            placeAt(level, origin, lx, FLOOR2 + 1, 3, f,
                    Blocks.CYAN_BED.defaultBlockState()
                            .setValue(BedBlock.FACING, bedDir)
                            .setValue(BedBlock.PART, BedPart.FOOT));
        }

        // Nightstand
        placeAt(level, origin, 35, FLOOR2 + 1, 3, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
        placeAt(level, origin, 35, FLOOR2 + 2, 3, f, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
        placeAt(level, origin, 35, FLOOR2 + 3, 3, f, Blocks.LANTERN.defaultBlockState());

        // Chest
        placeAt(level, origin, 40, FLOOR2 + 1, 2, f, Blocks.CHEST.defaultBlockState());
        placeAt(level, origin, 41, FLOOR2 + 1, 2, f, Blocks.CHEST.defaultBlockState());

        // Desk (two stairs facing each other)
        placeAt(level, origin, 40, FLOOR2 + 1, 8, f,
                stair(Blocks.DARK_OAK_STAIRS, Direction.WEST, f, false));
        placeAt(level, origin, 41, FLOOR2 + 1, 8, f,
                stair(Blocks.DARK_OAK_STAIRS, Direction.EAST, f, false));
        // Chair
        placeAt(level, origin, 40, FLOOR2 + 1, 9, f,
                stair(Blocks.DARK_OAK_STAIRS, Direction.NORTH, f, false));

        // Bookshelf
        for (int lz = 1; lz <= 6; lz++) {
            placeAt(level, origin, 43, FLOOR2 + 1, lz, f, Blocks.BOOKSHELF.defaultBlockState());
            placeAt(level, origin, 43, FLOOR2 + 2, lz, f, Blocks.BOOKSHELF.defaultBlockState());
        }

        // Chandelier
        placeSmallChandelier(level, origin, 37, CEIL2, 7, f);

        // Lantern
        placeAt(level, origin, 31, FLOOR2 + 4, 6, f, Blocks.LANTERN.defaultBlockState());

        // Flower pot
        placeAt(level, origin, 32, FLOOR2 + 1, 12, f, Blocks.POTTED_POPPY.defaultBlockState());
    }

    private static void furnishLibrary(ServerLevel level, BlockPos origin, Direction f) {
        // Library/Study: lx=1..20, lz=16..26

        // Blue carpet
        for (int lx = 1; lx <= 20; lx++) {
            for (int lz = 16; lz <= 26; lz++) {
                placeAt(level, origin, lx, FLOOR2, lz, f, Blocks.BLUE_CARPET.defaultBlockState());
            }
        }

        // Bookshelves around the walls for max enchanting (15+ within 5x5x2 of table)
        // North wall (lz=16)
        for (int lx = 1; lx <= 19; lx++) {
            placeAt(level, origin, lx, FLOOR2 + 1, 16, f, Blocks.BOOKSHELF.defaultBlockState());
            placeAt(level, origin, lx, FLOOR2 + 2, 16, f, Blocks.BOOKSHELF.defaultBlockState());
            placeAt(level, origin, lx, FLOOR2 + 3, 16, f, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        }
        // West wall shelves (lx=1)
        for (int lz = 17; lz <= 25; lz++) {
            placeAt(level, origin, 1, FLOOR2 + 1, lz, f, Blocks.BOOKSHELF.defaultBlockState());
            placeAt(level, origin, 1, FLOOR2 + 2, lz, f, Blocks.BOOKSHELF.defaultBlockState());
        }
        // South wall shelves (lz=26)
        for (int lx = 1; lx <= 19; lx++) {
            placeAt(level, origin, lx, FLOOR2 + 1, 26, f, Blocks.BOOKSHELF.defaultBlockState());
            placeAt(level, origin, lx, FLOOR2 + 2, 26, f, Blocks.BOOKSHELF.defaultBlockState());
        }
        // Freestanding shelf rows
        for (int lz = 18; lz <= 24; lz += 3) {
            for (int lx = 3; lx <= 5; lx++) {
                placeAt(level, origin, lx, FLOOR2 + 1, lz, f, Blocks.BOOKSHELF.defaultBlockState());
                placeAt(level, origin, lx, FLOOR2 + 2, lz, f, Blocks.BOOKSHELF.defaultBlockState());
            }
        }

        // Enchanting table with 2-block clearance
        placeAt(level, origin, 10, FLOOR2 + 1, 21, f, Blocks.ENCHANTING_TABLE.defaultBlockState());

        // Lectern
        placeAt(level, origin, 15, FLOOR2 + 1, 20, f, Blocks.LECTERN.defaultBlockState());

        // Reading desk
        placeAt(level, origin, 14, FLOOR2 + 1, 23, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
        placeAt(level, origin, 15, FLOOR2 + 1, 23, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
        placeAt(level, origin, 14, FLOOR2 + 2, 23, f, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
        placeAt(level, origin, 15, FLOOR2 + 2, 23, f, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
        // Chair
        placeAt(level, origin, 14, FLOOR2 + 1, 24, f,
                stair(Blocks.DARK_OAK_STAIRS, Direction.NORTH, f, false));

        // Chandeliers
        placeSmallChandelier(level, origin, 7, CEIL2, 20, f);
        placeSmallChandelier(level, origin, 15, CEIL2, 20, f);

        // Wall lanterns
        placeAt(level, origin, 1, FLOOR2 + 4, 21, f, Blocks.LANTERN.defaultBlockState());
        placeAt(level, origin, 20, FLOOR2 + 4, 21, f, Blocks.LANTERN.defaultBlockState());
    }

    private static void furnishBrewingLab(ServerLevel level, BlockPos origin, Direction f) {
        // Brewing Lab: lx=22..43, lz=16..26

        // Brewing stands
        placeAt(level, origin, 26, FLOOR2 + 1, 18, f, Blocks.BREWING_STAND.defaultBlockState());
        placeAt(level, origin, 28, FLOOR2 + 1, 18, f, Blocks.BREWING_STAND.defaultBlockState());
        placeAt(level, origin, 30, FLOOR2 + 1, 18, f, Blocks.BREWING_STAND.defaultBlockState());

        // Cauldrons
        placeAt(level, origin, 26, FLOOR2 + 1, 20, f, Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, 3));
        placeAt(level, origin, 28, FLOOR2 + 1, 20, f, Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, 3));
        placeAt(level, origin, 30, FLOOR2 + 1, 20, f, Blocks.CAULDRON.defaultBlockState());

        // Barrel storage
        for (int lz = 17; lz <= 22; lz++) {
            placeAt(level, origin, 22, FLOOR2 + 1, lz, f, Blocks.BARREL.defaultBlockState());
            placeAt(level, origin, 22, FLOOR2 + 2, lz, f, Blocks.BARREL.defaultBlockState());
            placeAt(level, origin, 23, FLOOR2 + 1, lz, f, Blocks.BARREL.defaultBlockState());
        }

        // Chests
        placeAt(level, origin, 38, FLOOR2 + 1, 18, f, Blocks.CHEST.defaultBlockState());
        placeAt(level, origin, 39, FLOOR2 + 1, 18, f, Blocks.CHEST.defaultBlockState());
        placeAt(level, origin, 40, FLOOR2 + 1, 18, f, Blocks.CHEST.defaultBlockState());

        // Crafting
        placeAt(level, origin, 36, FLOOR2 + 1, 17, f, Blocks.CRAFTING_TABLE.defaultBlockState());
        placeAt(level, origin, 37, FLOOR2 + 1, 17, f, Blocks.COMPOSTER.defaultBlockState());

        // Soul lanterns for ambiance
        placeSmallChandelier(level, origin, 28, CEIL2, 19, f);
        placeAt(level, origin, 22, FLOOR2 + 4, 24, f, Blocks.SOUL_LANTERN.defaultBlockState());
        placeAt(level, origin, 42, FLOOR2 + 4, 24, f, Blocks.SOUL_LANTERN.defaultBlockState());
        placeAt(level, origin, 32, CEIL2 - 1, 23, f, CHAIN);
        placeAt(level, origin, 32, CEIL2 - 2, 23, f, hangingSoulLantern());

        // Potted plants
        placeAt(level, origin, 40, FLOOR2 + 1, 25, f, Blocks.POTTED_WITHER_ROSE.defaultBlockState());
        placeAt(level, origin, 41, FLOOR2 + 1, 25, f, Blocks.POTTED_BROWN_MUSHROOM.defaultBlockState());
        placeAt(level, origin, 42, FLOOR2 + 1, 24, f, Blocks.POTTED_RED_MUSHROOM.defaultBlockState());
    }

    private static void furnishLounge(ServerLevel level, BlockPos origin, Direction f) {
        // Lounge: lx=1..20, lz=28..38

        // Carpet
        for (int lx = 1; lx <= 20; lx++) {
            for (int lz = 28; lz <= 38; lz++) {
                placeAt(level, origin, lx, FLOOR2, lz, f, Blocks.CYAN_CARPET.defaultBlockState());
            }
        }

        // Sofa cluster: U-shaped seating
        Direction seatS = rotateDir(Direction.SOUTH, f);
        Direction seatE = rotateDir(Direction.EAST, f);
        Direction seatW = rotateDir(Direction.WEST, f);
        Direction seatN = rotateDir(Direction.NORTH, f);

        for (int lx = 5; lx <= 11; lx++) {
            placeAt(level, origin, lx, FLOOR2 + 1, 30, f,
                    Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, seatS));
        }
        for (int lz = 31; lz <= 34; lz++) {
            placeAt(level, origin, 5, FLOOR2 + 1, lz, f,
                    Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, seatE));
            placeAt(level, origin, 11, FLOOR2 + 1, lz, f,
                    Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, seatW));
        }
        // Armrests (trapdoors on sides of end seats)
        // Coffee table
        for (int lx = 7; lx <= 9; lx++) {
            placeAt(level, origin, lx, FLOOR2 + 1, 32, f, Blocks.DARK_OAK_FENCE.defaultBlockState());
            placeAt(level, origin, lx, FLOOR2 + 2, 32, f, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
        }

        // Note block and jukebox
        placeAt(level, origin, 15, FLOOR2 + 1, 30, f, Blocks.NOTE_BLOCK.defaultBlockState());
        placeAt(level, origin, 17, FLOOR2 + 1, 30, f, Blocks.JUKEBOX.defaultBlockState());

        // Flower pots
        placeAt(level, origin, 2, FLOOR2 + 1, 29, f, Blocks.POTTED_POPPY.defaultBlockState());
        placeAt(level, origin, 19, FLOOR2 + 1, 29, f, Blocks.POTTED_AZURE_BLUET.defaultBlockState());
        placeAt(level, origin, 2, FLOOR2 + 1, 37, f, Blocks.POTTED_CORNFLOWER.defaultBlockState());
        placeAt(level, origin, 19, FLOOR2 + 1, 37, f, Blocks.POTTED_ALLIUM.defaultBlockState());

        // Chandelier
        placeSmallChandelier(level, origin, 10, CEIL2, 33, f);

        // Wall lanterns
        placeAt(level, origin, 1, FLOOR2 + 4, 33, f, Blocks.LANTERN.defaultBlockState());
        placeAt(level, origin, 20, FLOOR2 + 4, 33, f, Blocks.LANTERN.defaultBlockState());

        // Balcony chairs (facing south toward the opening)
        placeAt(level, origin, 4, FLOOR2 + 1, 38, f,
                Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, seatS));
        placeAt(level, origin, 10, FLOOR2 + 1, 38, f,
                Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, seatS));
        placeAt(level, origin, 16, FLOOR2 + 1, 38, f,
                Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, seatS));
    }
}
