package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Bioluminescent plankton fluid — dark deep-ocean water that glows
 * when living entities move through it. Light cascades to neighboring
 * plankton blocks, then fades after a few seconds.
 */
public class PlanktonBlock extends TranslucentLiquidBlock {

    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    private static final int FADE_TICKS = 60;      // 3 seconds to fade
    private static final int CASCADE_RANGE = 3;

    public PlanktonBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
        registerDefaultState(stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity && level instanceof ServerLevel serverLevel) {
            // Light up this block and cascade to neighbors
            illuminate(serverLevel, pos, CASCADE_RANGE);

            // Spawn glow particles around the entity
            double ex = entity.getX();
            double ey = entity.getY() + 0.3;
            double ez = entity.getZ();
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    ex, ey, ez, 3, 0.3, 0.2, 0.3, 0.01);
        }
    }

    private void illuminate(ServerLevel level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius) continue;
                    BlockPos target = center.offset(dx, dy, dz);
                    BlockState targetState = level.getBlockState(target);
                    if (targetState.getBlock() instanceof PlanktonBlock
                            && !targetState.getValue(LIT)) {
                        // Outer blocks get a slightly delayed light-up
                        int dist = Math.abs(dx) + Math.abs(dz);
                        level.setBlock(target, targetState.setValue(LIT, true), 3);
                        // Schedule fade — further blocks fade sooner for a wave effect
                        int fadeTicks = FADE_TICKS - dist * 6;
                        level.scheduleTick(target, this, Math.max(20, fadeTicks));
                    }
                }
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        // Fade: turn off the light
        if (state.getValue(LIT)) {
            level.setBlock(pos, state.setValue(LIT, false), 3);
        }
    }
}
