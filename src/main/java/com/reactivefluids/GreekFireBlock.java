package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Greek Fire — a sticky burning fluid that cannot be extinguished by water.
 * - Sets entities on fire for 10 seconds
 * - When water touches it, the water evaporates and Greek Fire SPREADS
 * - Ignites flammable blocks nearby
 * - Burns with green-tinted flames (particles)
 */
public class GreekFireBlock extends TranslucentLiquidBlock {

    private static final int BURN_TICKS = 15;

    public GreekFireBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            reactWithWater((ServerLevel) level, pos);
            igniteNearby((ServerLevel) level, pos);
            level.scheduleTick(pos, this, BURN_TICKS);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        reactWithWater(level, pos);
        igniteNearby(level, pos);
        level.scheduleTick(pos, this, BURN_TICKS);
    }

    /**
     * Water doesn't extinguish Greek Fire — instead, the water evaporates
     * and is replaced by more Greek Fire source blocks.
     */
    private void reactWithWater(ServerLevel level, BlockPos center) {
        for (BlockPos neighbor : new BlockPos[]{
                center.above(), center.below(),
                center.north(), center.south(),
                center.east(), center.west()}) {

            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.getBlock() == Blocks.WATER) {
                // Evaporate water and replace with Greek Fire
                level.setBlock(neighbor, ModBlocks.GREEK_FIRE_BLOCK.get().defaultBlockState(), 3);
                // Steam particles
                double cx = neighbor.getX() + 0.5;
                double cy = neighbor.getY() + 0.5;
                double cz = neighbor.getZ() + 0.5;
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, cx, cy + 0.5, cz, 6, 0.3, 0.3, 0.3, 0.04);
                level.playSound(null, neighbor, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8f, 0.8f);
            }
        }
    }

    private void igniteNearby(ServerLevel level, BlockPos center) {
        for (BlockPos neighbor : new BlockPos[]{
                center.above(), center.north(), center.south(),
                center.east(), center.west()}) {

            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.isAir()) {
                // Check if adjacent to a flammable block
                for (BlockPos adj : new BlockPos[]{
                        neighbor.above(), neighbor.below(),
                        neighbor.north(), neighbor.south(),
                        neighbor.east(), neighbor.west()}) {
                    if (level.getBlockState(adj).ignitedByLava()) {
                        level.setBlock(neighbor, Blocks.FIRE.defaultBlockState(), 3);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity) {
            entity.setRemainingFireTicks(200); // 10 seconds
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        // Green-tinted fire particles
        for (int i = 0; i < 2; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.9 + random.nextDouble() * 0.4;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.FLAME, x, y, z,
                    (random.nextDouble() - 0.5) * 0.01, 0.04, (random.nextDouble() - 0.5) * 0.01);
        }

        // Smoke
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.0 + random.nextDouble() * 0.5;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.05, 0);
        }

        // Soul fire particles for green tint
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.8 + random.nextDouble() * 0.3;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z,
                    (random.nextDouble() - 0.5) * 0.02, 0.03, (random.nextDouble() - 0.5) * 0.02);
        }
    }
}
