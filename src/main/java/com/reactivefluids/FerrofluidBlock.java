package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FlowingFluid;
import org.joml.Vector3f;

/**
 * Ferrofluid — a magnetic fluid that responds to redstone signals.
 * - When powered by redstone, it becomes "magnetized" (state property)
 * - Magnetized ferrofluid solidifies into iron blocks (spiky formation)
 * - When redstone turns off, iron blocks revert to ferrofluid
 * - Emits dark metallic particles, with sparking when magnetized
 */
public class FerrofluidBlock extends TranslucentLiquidBlock {

    public static final BooleanProperty MAGNETIZED = BooleanProperty.create("magnetized");
    private static final int CHECK_TICKS = 4;

    public FerrofluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
        registerDefaultState(stateDefinition.any().setValue(MAGNETIZED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MAGNETIZED);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, CHECK_TICKS);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide()) {
            boolean powered = level.hasNeighborSignal(pos);
            boolean currentlyMagnetized = state.getValue(MAGNETIZED);

            if (powered && !currentlyMagnetized) {
                // Become magnetized — solidify into iron block
                solidify((ServerLevel) level, pos);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        boolean powered = level.hasNeighborSignal(pos);
        if (powered && !state.getValue(MAGNETIZED)) {
            solidify(level, pos);
        }
        level.scheduleTick(pos, this, CHECK_TICKS);
    }

    /**
     * When magnetized, the ferrofluid solidifies into an iron block.
     * Looks like a spiky ferrofluid formation rising up.
     */
    private void solidify(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.IRON_BLOCK.defaultBlockState(), 3);

        // Spike: also solidify the block above if it's air (spiky formation)
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.getBlock() instanceof FerrofluidBlock) {
            level.setBlock(above, Blocks.IRON_BLOCK.defaultBlockState(), 3);
        }

        // Metallic spark particles
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, cx, cy + 0.5, cz, 12, 0.3, 0.4, 0.3, 0.05);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        // Dark metallic dust particles
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.8 + 0.1;
            double z = pos.getZ() + random.nextDouble();
            // Dark gunmetal gray
            level.addParticle(
                    new DustParticleOptions(new Vector3f(0.15f, 0.15f, 0.2f), 0.8f),
                    x, y, z, 0, 0.01, 0);
        }

        // Near redstone — sparking
        if (level.hasNeighborSignal(pos) && random.nextInt(2) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.5 + random.nextDouble() * 0.5;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                    (random.nextDouble() - 0.5) * 0.05, 0.05, (random.nextDouble() - 0.5) * 0.05);
        }
    }
}
