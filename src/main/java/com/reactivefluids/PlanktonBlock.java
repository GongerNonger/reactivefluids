package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Bioluminescent plankton fluid — dark deep-ocean water that glows
 * when living entities move through it. Light cascades to neighboring
 * plankton blocks, then fades after a few seconds.
 */
public class PlanktonBlock extends TranslucentLiquidBlock {

    /** Glow intensity: 0 = off, 1 = faint edge glow, 2 = medium, 3 = bright, 4 = full */
    public static final int MAX_GLOW = 4;
    public static final IntegerProperty GLOW = IntegerProperty.create("glow", 0, MAX_GLOW);
    private static final int FADE_TICKS = 60;      // 3 seconds to fade
    private static final int CASCADE_RANGE = 3;

    public PlanktonBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
        registerDefaultState(stateDefinition.any().setValue(GLOW, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(GLOW);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity && level instanceof ServerLevel serverLevel) {
            illuminate(serverLevel, pos, CASCADE_RANGE);
        }
    }

    private void illuminate(ServerLevel level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius) continue;
                    BlockPos target = center.offset(dx, dy, dz);
                    BlockState targetState = level.getBlockState(target);
                    if (targetState.getBlock() instanceof PlanktonBlock) {
                        // Glow level based on distance — closer = brighter
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        int glow = Math.max(1, MAX_GLOW - (int) dist);
                        int current = targetState.getValue(GLOW);
                        if (glow > current) {
                            level.setBlock(target, targetState.setValue(GLOW, glow), 3);
                            // Schedule fade — further blocks fade sooner for a wave effect
                            int manhattan = Math.abs(dx) + Math.abs(dz);
                            int fadeTicks = FADE_TICKS - manhattan * 6;
                            level.scheduleTick(target, this, Math.max(20, fadeTicks));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        int glow = state.getValue(GLOW);
        if (glow > 0) {
            // Step down one level at a time for a gradual fade
            level.setBlock(pos, state.setValue(GLOW, glow - 1), 3);
            if (glow - 1 > 0) {
                // Schedule next fade step
                level.scheduleTick(pos, this, 10);
            }
        }
    }

}
