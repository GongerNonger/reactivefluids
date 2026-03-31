package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Liquid Nitrogen — an extremely cold fluid.
 * - Freezes water source blocks into ice on contact (neighbor check)
 * - Converts lava source blocks to obsidian, flowing lava to cobblestone
 * - Entities that walk through it get Slowness III and Mining Fatigue II
 * - Emits constant white fog/snow particles
 */
public class LiquidNitrogenBlock extends TranslucentLiquidBlock {

    private static final int FREEZE_TICKS = 10;

    public LiquidNitrogenBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            freezeNeighbors((ServerLevel) level, pos);
            level.scheduleTick(pos, this, FREEZE_TICKS);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        freezeNeighbors(level, pos);
        level.scheduleTick(pos, this, FREEZE_TICKS);
    }

    private void freezeNeighbors(ServerLevel level, BlockPos center) {
        for (BlockPos neighbor : new BlockPos[]{
                center.above(), center.below(),
                center.north(), center.south(),
                center.east(), center.west()}) {

            BlockState neighborState = level.getBlockState(neighbor);
            Block neighborBlock = neighborState.getBlock();

            if (neighborBlock == Blocks.WATER) {
                level.setBlock(neighbor, Blocks.ICE.defaultBlockState(), 3);
                spawnFreezeParticles(level, neighbor);
            } else if (neighborBlock == Blocks.LAVA) {
                if (neighborState.getFluidState().isSource()) {
                    level.setBlock(neighbor, Blocks.OBSIDIAN.defaultBlockState(), 3);
                } else {
                    level.setBlock(neighbor, Blocks.COBBLESTONE.defaultBlockState(), 3);
                }
                spawnFreezeParticles(level, neighbor);
                level.playSound(null, neighbor, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.0f);
            }
        }
    }

    private void spawnFreezeParticles(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.SNOWFLAKE, cx, cy, cz, 8, 0.4, 0.4, 0.4, 0.02);
        level.sendParticles(ParticleTypes.CLOUD, cx, cy + 0.3, cz, 4, 0.3, 0.2, 0.3, 0.01);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, true));
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 80, 1, false, true));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        // Constant cold fog effect
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + 0.9 + random.nextDouble() * 0.3;
        double z = pos.getZ() + random.nextDouble();
        level.addParticle(ParticleTypes.CLOUD, x, y, z, 0, 0.02, 0);

        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SNOWFLAKE,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + 0.8 + random.nextDouble() * 0.4,
                    pos.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.01, 0.02, (random.nextDouble() - 0.5) * 0.01);
        }
    }
}
