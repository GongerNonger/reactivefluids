package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
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
import org.joml.Vector3f;

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

    /**
     * Client-side particle display when lit — dense bright blue/cyan plankton
     * sparks that make the water visually transform from dark to glowing.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (!state.getValue(LIT)) {
            // Unlit: occasional faint ambient sparkle (1 in 8 chance)
            if (random.nextInt(8) == 0) {
                double x = pos.getX() + random.nextDouble();
                double y = pos.getY() + random.nextDouble() * 0.8 + 0.1;
                double z = pos.getZ() + random.nextDouble();
                // Dim teal dust particle
                level.addParticle(
                    new DustParticleOptions(new Vector3f(0.05f, 0.4f, 0.5f), 0.5f),
                    x, y, z, 0, 0.01, 0);
            }
            return;
        }

        // ===== LIT — bright bioluminescent eruption =====

        // 4-7 bright blue plankton sparks per tick
        int sparkCount = 4 + random.nextInt(4);
        for (int i = 0; i < sparkCount; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble() * 0.9 + 0.1;
            double z = pos.getZ() + random.nextDouble();

            // Randomize between several bright blue/cyan colors
            float r, g, b;
            int colorChoice = random.nextInt(5);
            switch (colorChoice) {
                case 0 -> { r = 0.1f; g = 0.85f; b = 1.0f; }   // electric cyan
                case 1 -> { r = 0.15f; g = 1.0f; b = 0.82f; }   // seafoam
                case 2 -> { r = 0.3f; g = 0.78f; b = 1.0f; }    // pale blue
                case 3 -> { r = 0.0f; g = 0.7f; b = 0.85f; }    // deep teal
                default -> { r = 0.25f; g = 1.0f; b = 0.7f; }   // mint green
            }

            // Larger, brighter dust particles
            float size = 0.6f + random.nextFloat() * 0.6f;
            level.addParticle(
                new DustParticleOptions(new Vector3f(r, g, b), size),
                x, y, z,
                (random.nextDouble() - 0.5) * 0.02,
                random.nextDouble() * 0.03,
                (random.nextDouble() - 0.5) * 0.02);
        }

        // 1-2 END_ROD particles for extra magic
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.5 + random.nextDouble() * 0.5;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.END_ROD, x, y, z,
                    0, 0.02, 0);
        }
    }
}
