package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Foam block produced by the elephant's toothpaste reaction
 * (hydrogen peroxide + potassium iodide). Grows upward rapidly
 * via scheduled ticks, simulating the dramatic foam eruption.
 */
public class FoamBlock extends Block {

    private static final int MAX_HEIGHT = 15;
    private static final int MIN_GROW_TICKS = 2;
    private static final int MAX_GROW_TICKS = 4;

    public FoamBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            int delay = MIN_GROW_TICKS + level.getRandom().nextInt(MAX_GROW_TICKS - MIN_GROW_TICKS + 1);
            level.scheduleTick(pos, this, delay);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Only the topmost foam in a column should grow
        if (level.getBlockState(pos.above()).getBlock() instanceof FoamBlock) return;

        // Count foam height below
        int height = 1;
        BlockPos check = pos.below();
        while (level.getBlockState(check).getBlock() instanceof FoamBlock && height < MAX_HEIGHT + 5) {
            height++;
            check = check.below();
        }

        if (height >= MAX_HEIGHT) return;

        // Grow upward — can push through fluid or replace air
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if (aboveState.isAir() || !aboveState.getFluidState().isEmpty() || aboveState.canBeReplaced()) {
            level.setBlock(abovePos, defaultBlockState(), 3);

            // Eruption particles
            double cx = abovePos.getX() + 0.5;
            double cy = abovePos.getY() + 0.5;
            double cz = abovePos.getZ() + 0.5;
            level.sendParticles(ParticleTypes.CLOUD, cx, cy + 0.3, cz, 6, 0.25, 0.15, 0.25, 0.06);

            // Chance to spread sideways for a more organic shape
            if (height > 3 && random.nextFloat() < 0.25f) {
                Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockPos sidePos = abovePos.relative(dir);
                BlockState sideState = level.getBlockState(sidePos);
                if (sideState.isAir() || sideState.canBeReplaced()) {
                    level.setBlock(sidePos, defaultBlockState(), 3);
                    level.sendParticles(ParticleTypes.CLOUD,
                            sidePos.getX() + 0.5, sidePos.getY() + 0.5, sidePos.getZ() + 0.5,
                            3, 0.2, 0.1, 0.2, 0.04);
                }
            }
        }
    }
}
