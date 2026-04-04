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

    private static final int MAX_HEIGHT = 20;
    private static final int MIN_GROW_TICKS = 2;
    private static final int MAX_GROW_TICKS = 4;

    public FoamBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            // Only schedule upward growth if this block replaced a fluid (reaction product).
            // Player-placed foam and sideways-bloomed foam do NOT start new eruption columns.
            boolean isReactionBase = !oldState.getFluidState().isEmpty();
            if (isReactionBase) {
                int delay = MIN_GROW_TICKS + level.getRandom().nextInt(MAX_GROW_TICKS - MIN_GROW_TICKS + 1);
                level.scheduleTick(pos, this, delay);
            }
        }
    }

    /**
     * Count total foam height in this column — walk down to find the
     * lowest foam block, then count up from there.
     */
    private int countColumnHeight(ServerLevel level, BlockPos pos) {
        // Walk down to the base of the foam column
        BlockPos base = pos;
        while (level.getBlockState(base.below()).getBlock() instanceof FoamBlock) {
            base = base.below();
        }
        // Count upward from base
        int height = 0;
        BlockPos cur = base;
        while (level.getBlockState(cur).getBlock() instanceof FoamBlock) {
            height++;
            cur = cur.above();
            if (height > MAX_HEIGHT + 5) break; // safety
        }
        return height;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Only the topmost foam in a column should grow
        if (level.getBlockState(pos.above()).getBlock() instanceof FoamBlock) return;

        int height = countColumnHeight(level, pos);
        if (height >= MAX_HEIGHT) return;

        // Grow upward — can push through fluid or replace air
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if (aboveState.isAir() || !aboveState.getFluidState().isEmpty() || aboveState.canBeReplaced()) {
            level.setBlock(abovePos, defaultBlockState(), 3);
            // Schedule growth tick on the new block (onPlace won't do it since oldState is air)
            int delay = MIN_GROW_TICKS + random.nextInt(MAX_GROW_TICKS - MIN_GROW_TICKS + 1);
            level.scheduleTick(abovePos, this, delay);

            // Eruption particles
            double cx = abovePos.getX() + 0.5;
            double cy = abovePos.getY() + 0.5;
            double cz = abovePos.getZ() + 0.5;
            level.sendParticles(ParticleTypes.CLOUD, cx, cy + 0.3, cz, 6, 0.25, 0.15, 0.25, 0.06);

            // Bloom outward — chance and radius increase with height for a mushroom/cone shape
            float bloomChance;
            int bloomCount;
            if (height < 4) {
                bloomChance = 0.1f;
                bloomCount = 1;
            } else if (height < 10) {
                bloomChance = 0.4f;
                bloomCount = 1 + random.nextInt(2);  // 1-2 directions
            } else {
                bloomChance = 0.65f;
                bloomCount = 2 + random.nextInt(2);  // 2-3 directions
            }

            if (random.nextFloat() < bloomChance) {
                for (int i = 0; i < bloomCount; i++) {
                    Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    // Sometimes extend 2 blocks out at higher elevations
                    int reach = (height > 8 && random.nextFloat() < 0.3f) ? 2 : 1;
                    BlockPos sidePos = abovePos.relative(dir, reach);
                    BlockState sideState = level.getBlockState(sidePos);
                    if (sideState.isAir() || sideState.canBeReplaced()) {
                        level.setBlock(sidePos, defaultBlockState(), 2);
                        level.sendParticles(ParticleTypes.CLOUD,
                                sidePos.getX() + 0.5, sidePos.getY() + 0.5, sidePos.getZ() + 0.5,
                                3, 0.2, 0.1, 0.2, 0.04);
                        // Fill gap block if reaching 2 out
                        if (reach == 2) {
                            BlockPos midPos = abovePos.relative(dir, 1);
                            BlockState midState = level.getBlockState(midPos);
                            if (midState.isAir() || midState.canBeReplaced()) {
                                level.setBlock(midPos, defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }
    }
}
