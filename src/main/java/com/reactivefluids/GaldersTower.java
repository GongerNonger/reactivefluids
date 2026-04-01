package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds and defines the structure for Galder's Tower — a conjured
 * two-story stone tower with randomly furnished rooms.
 */
public class GaldersTower {
    public static final int SIZE = 7;
    public static final int HEIGHT = 11;
    public static final int DURATION_TICKS = 24000; // 1 Minecraft day

    public enum RoomType {
        BEDROOM, STUDY, DINING, LOUNGE, WASHROOM, OBSERVATORY, EMPTY;
        private static final RoomType[] VALUES = values();
        public static RoomType random(RandomSource rand) {
            return VALUES[rand.nextInt(VALUES.length)];
        }
    }

    // --- Public API ---

    /** Check if there's enough space above ground for the tower. */
    public static boolean canPlace(ServerLevel level, BlockPos center, Direction doorFacing) {
        for (int ly = 1; ly < HEIGHT; ly++) {
            for (int lx = 0; lx < SIZE; lx++) {
                for (int lz = 0; lz < SIZE; lz++) {
                    BlockPos wp = toWorldPos(center, lx, ly, lz, doorFacing);
                    BlockState bs = level.getBlockState(wp);
                    if (!bs.isAir() && !bs.canBeReplaced()) return false;
                }
            }
        }
        return true;
    }

    /** Build the tower and return all placed block positions. */
    public static List<BlockPos> build(ServerLevel level, BlockPos center, Direction doorFacing,
                                        RoomType floor1, RoomType floor2) {
        List<BlockPos> placed = new ArrayList<>();
        buildShell(level, center, doorFacing, placed);
        placeLadders(level, center, doorFacing, placed);
        furnishRoom(level, center, doorFacing, floor1, 1, placed);
        furnishRoom(level, center, doorFacing, floor2, 6, placed);
        level.playSound(null, center, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.5F, 1.0F);
        level.sendParticles(ParticleTypes.ENCHANT,
                center.getX() + 0.5, center.getY() + 6, center.getZ() + 0.5,
                100, 3.0, 5.0, 3.0, 0.1);
        return placed;
    }

    // --- Coordinate helpers ---

    /**
     * Convert local tower coordinates to world position.
     * Local space: door on NORTH wall (z=0), 7x7 from (0,0,0) to (6,10,6).
     * Rotated so the door faces doorFacing in world space.
     */
    private static BlockPos toWorldPos(BlockPos center, int lx, int ly, int lz, Direction doorFacing) {
        int dx = lx - 3, dz = lz - 3;
        int rx, rz;
        switch (doorFacing) {
            case SOUTH -> { rx = -dx; rz = -dz; }
            case EAST  -> { rx = -dz; rz = dx; }
            case WEST  -> { rx = dz; rz = -dx; }
            default    -> { rx = dx; rz = dz; }
        }
        return center.offset(rx, ly, rz);
    }

    /** Rotate a direction from local space to world space. */
    private static Direction rotateDir(Direction dir, Direction doorFacing) {
        return switch (doorFacing) {
            case SOUTH -> dir.getOpposite();
            case EAST -> dir.getClockWise();
            case WEST -> dir.getCounterClockWise();
            default -> dir;
        };
    }

    private static void place(ServerLevel level, BlockPos pos, BlockState state, List<BlockPos> placed) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
        placed.add(pos.immutable());
    }

    private static void placeAt(ServerLevel level, BlockPos center, int lx, int ly, int lz,
                                 Direction df, BlockState state, List<BlockPos> placed) {
        place(level, toWorldPos(center, lx, ly, lz, df), state, placed);
    }

    // --- Shell construction ---

    private static void buildShell(ServerLevel level, BlockPos center, Direction doorFacing,
                                    List<BlockPos> placed) {
        BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        Direction doorDir = rotateDir(Direction.NORTH, doorFacing);

        for (int ly = 0; ly < HEIGHT; ly++) {
            for (int lx = 0; lx < SIZE; lx++) {
                for (int lz = 0; lz < SIZE; lz++) {
                    boolean edgeX = (lx == 0 || lx == 6);
                    boolean edgeZ = (lz == 0 || lz == 6);
                    boolean isEdge = edgeX || edgeZ;
                    boolean isFloor = (ly == 0 || ly == 5 || ly == 10);
                    BlockPos wp = toWorldPos(center, lx, ly, lz, doorFacing);

                    if (isFloor) {
                        if (ly == 5 && lx == 5 && lz == 5) {
                            // Ladder hole in middle floor
                            level.setBlock(wp, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        } else {
                            place(level, wp, ly == 5 ? planks : stone, placed);
                        }
                    } else if (isEdge) {
                        if (lx == 3 && lz == 0 && (ly == 1 || ly == 2)) {
                            // Door on north wall
                            BlockState door = Blocks.OAK_DOOR.defaultBlockState()
                                    .setValue(DoorBlock.FACING, doorDir)
                                    .setValue(DoorBlock.HALF, ly == 1
                                            ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
                            place(level, wp, door, placed);
                        } else if (isWindowPos(lx, lz, ly)) {
                            place(level, wp, glass, placed);
                        } else {
                            place(level, wp, stone, placed);
                        }
                    } else {
                        // Interior — clear to air, don't track
                        level.setBlock(wp, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private static boolean isWindowPos(int lx, int lz, int ly) {
        if (ly != 2 && ly != 3 && ly != 7 && ly != 8) return false;
        if (lz == 0 && (lx == 2 || lx == 4)) return true;
        if (lz == 6 && (lx == 2 || lx == 4)) return true;
        if (lx == 6 && (lz == 2 || lz == 4)) return true;
        if (lx == 0 && (lz == 2 || lz == 4)) return true;
        return false;
    }

    private static void placeLadders(ServerLevel level, BlockPos center, Direction doorFacing,
                                      List<BlockPos> placed) {
        Direction facing = rotateDir(Direction.WEST, doorFacing);
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing);
        for (int ly = 1; ly <= 9; ly++) {
            placeAt(level, center, 5, ly, 5, doorFacing, ladder, placed);
        }
    }

    // --- Room furnishing ---

    private static void furnishRoom(ServerLevel level, BlockPos center, Direction df,
                                     RoomType type, int fy, List<BlockPos> placed) {
        switch (type) {
            case BEDROOM     -> bedroom(level, center, df, fy, placed);
            case STUDY       -> study(level, center, df, fy, placed);
            case DINING      -> dining(level, center, df, fy, placed);
            case LOUNGE      -> lounge(level, center, df, fy, placed);
            case WASHROOM    -> washroom(level, center, df, fy, placed);
            case OBSERVATORY -> observatory(level, center, df, fy, placed);
            case EMPTY       -> empty(level, center, df, fy, placed);
        }
    }

    private static void bedroom(ServerLevel level, BlockPos center, Direction df, int fy,
                                 List<BlockPos> placed) {
        Direction bedDir = rotateDir(Direction.SOUTH, df);
        placeAt(level, center, 2, fy, 4, df,
                Blocks.WHITE_BED.defaultBlockState()
                        .setValue(BedBlock.FACING, bedDir)
                        .setValue(BedBlock.PART, BedPart.FOOT), placed);
        placeAt(level, center, 2, fy, 5, df,
                Blocks.WHITE_BED.defaultBlockState()
                        .setValue(BedBlock.FACING, bedDir)
                        .setValue(BedBlock.PART, BedPart.HEAD), placed);
        placeAt(level, center, 1, fy, 5, df, Blocks.CHEST.defaultBlockState(), placed);
        placeAt(level, center, 4, fy, 5, df, Blocks.FURNACE.defaultBlockState(), placed);
        placeAt(level, center, 2, fy, 2, df, Blocks.RED_CARPET.defaultBlockState(), placed);
        placeAt(level, center, 3, fy, 2, df, Blocks.RED_CARPET.defaultBlockState(), placed);
        placeAt(level, center, 3, fy, 3, df, Blocks.RED_CARPET.defaultBlockState(), placed);
        placeAt(level, center, 1, fy, 1, df, Blocks.LANTERN.defaultBlockState(), placed);
    }

    private static void study(ServerLevel level, BlockPos center, Direction df, int fy,
                               List<BlockPos> placed) {
        for (int x = 1; x <= 4; x++) {
            placeAt(level, center, x, fy, 5, df, Blocks.BOOKSHELF.defaultBlockState(), placed);
            placeAt(level, center, x, fy + 1, 5, df, Blocks.BOOKSHELF.defaultBlockState(), placed);
        }
        placeAt(level, center, 3, fy, 3, df, Blocks.LECTERN.defaultBlockState(), placed);
        placeAt(level, center, 1, fy, 1, df, Blocks.CRAFTING_TABLE.defaultBlockState(), placed);
        placeAt(level, center, 4, fy, 1, df, Blocks.LANTERN.defaultBlockState(), placed);
    }

    private static void dining(ServerLevel level, BlockPos center, Direction df, int fy,
                                List<BlockPos> placed) {
        placeAt(level, center, 3, fy, 3, df, Blocks.OAK_FENCE.defaultBlockState(), placed);
        placeAt(level, center, 3, fy + 1, 3, df,
                Blocks.OAK_PRESSURE_PLATE.defaultBlockState(), placed);
        placeAt(level, center, 1, fy, 5, df, Blocks.SMOKER.defaultBlockState(), placed);
        placeAt(level, center, 4, fy, 5, df, Blocks.BARREL.defaultBlockState(), placed);
        Direction chairN = rotateDir(Direction.SOUTH, df);
        Direction chairS = rotateDir(Direction.NORTH, df);
        placeAt(level, center, 3, fy, 2, df,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, chairN), placed);
        placeAt(level, center, 3, fy, 4, df,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, chairS), placed);
        placeAt(level, center, 1, fy, 1, df, Blocks.LANTERN.defaultBlockState(), placed);
    }

    private static void lounge(ServerLevel level, BlockPos center, Direction df, int fy,
                                List<BlockPos> placed) {
        for (int x = 2; x <= 4; x++) {
            for (int z = 2; z <= 4; z++) {
                placeAt(level, center, x, fy, z, df,
                        Blocks.BLUE_CARPET.defaultBlockState(), placed);
            }
        }
        Direction fN = rotateDir(Direction.SOUTH, df);
        Direction fE = rotateDir(Direction.WEST, df);
        placeAt(level, center, 2, fy, 5, df,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, fN), placed);
        placeAt(level, center, 3, fy, 5, df,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, fN), placed);
        placeAt(level, center, 1, fy, 3, df,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, fE), placed);
        placeAt(level, center, 1, fy, 4, df,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, fE), placed);
        placeAt(level, center, 4, fy, 1, df, Blocks.FLOWER_POT.defaultBlockState(), placed);
        placeAt(level, center, 3, fy, 1, df, Blocks.LANTERN.defaultBlockState(), placed);
    }

    private static void washroom(ServerLevel level, BlockPos center, Direction df, int fy,
                                  List<BlockPos> placed) {
        placeAt(level, center, 3, fy, 3, df,
                Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, 3), placed);
        placeAt(level, center, 1, fy, 5, df, Blocks.CAMPFIRE.defaultBlockState(), placed);
        Direction benchDir = rotateDir(Direction.WEST, df);
        placeAt(level, center, 4, fy, 4, df,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, benchDir), placed);
        placeAt(level, center, 4, fy, 3, df,
                Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, benchDir), placed);
        placeAt(level, center, 1, fy, 1, df, Blocks.LANTERN.defaultBlockState(), placed);
    }

    private static void observatory(ServerLevel level, BlockPos center, Direction df, int fy,
                                     List<BlockPos> placed) {
        // Glass ceiling
        int ceilY = fy + 4;
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                if (x == 5 && z == 5) continue; // preserve ladder hole
                placeAt(level, center, x, ceilY, z, df,
                        Blocks.GLASS.defaultBlockState(), placed);
            }
        }
        // Telescope (end rods pointing up)
        placeAt(level, center, 3, fy, 3, df, Blocks.END_ROD.defaultBlockState(), placed);
        placeAt(level, center, 3, fy + 1, 3, df, Blocks.END_ROD.defaultBlockState(), placed);
        placeAt(level, center, 1, fy, 5, df, Blocks.CARTOGRAPHY_TABLE.defaultBlockState(), placed);
        placeAt(level, center, 1, fy, 1, df, Blocks.LANTERN.defaultBlockState(), placed);
    }

    private static void empty(ServerLevel level, BlockPos center, Direction df, int fy,
                               List<BlockPos> placed) {
        placeAt(level, center, 3, fy, 3, df, Blocks.LANTERN.defaultBlockState(), placed);
    }
}
