package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Crystal solution fluid block — grows crystal blocks on adjacent
 * solid surfaces near source blocks. Crystallizes skeletons that
 * stand in it for 30 seconds.
 */
public class CrystalSolutionBlock extends TranslucentLiquidBlock {

    private static final int CRYSTAL_GROW_CHANCE = 15; // 1-in-N per random tick
    private static final int CRYSTALLIZE_TICKS = 600;  // 30 seconds

    public CrystalSolutionBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    /**
     * Grow crystals around source blocks via random ticks.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);

        // Only source blocks grow crystals
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isSource()) return;

        if (random.nextInt(CRYSTAL_GROW_CHANCE) != 0) return;

        // Pick a random adjacent position (including diagonals in the same Y, +1, -1)
        int dx = random.nextInt(3) - 1;
        int dy = random.nextInt(3) - 1;
        int dz = random.nextInt(3) - 1;
        if (dx == 0 && dy == 0 && dz == 0) return;

        BlockPos target = pos.offset(dx, dy, dz);
        BlockState targetState = level.getBlockState(target);

        // Can only grow into air or replaceable blocks
        if (!targetState.isAir() && !targetState.canBeReplaced()) return;

        // Must have at least one solid face adjacent to the crystal position
        boolean hasSupport = false;
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = target.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.isFaceSturdy(level, neighbor, dir.getOpposite())
                    || neighborState.getBlock() instanceof CrystalBlock) {
                hasSupport = true;
                break;
            }
        }

        if (hasSupport) {
            level.setBlock(target, ModBlocks.CRYSTAL_BLOCK.get().defaultBlockState(), 3);
        }
    }

    /**
     * Crystallize skeletons that stand in the solution.
     */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof Skeleton skeleton
                && !(entity instanceof CrystallizedSkeleton)
                && level instanceof ServerLevel serverLevel) {
            // Track time in crystal solution using persistent data
            var data = skeleton.getPersistentData();
            int ticks = data.getInt("crystal_soak_time") + 1;
            data.putInt("crystal_soak_time", ticks);

            if (ticks >= CRYSTALLIZE_TICKS) {
                // Convert to crystallized skeleton
                CrystallizedSkeleton crystal = skeleton.convertTo(
                        ModEntities.CRYSTALLIZED_SKELETON.get(), true);
                if (crystal != null) {
                    crystal.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(pos),
                            net.minecraft.world.entity.MobSpawnType.CONVERSION, null);
                }
            }
        }
    }
}
