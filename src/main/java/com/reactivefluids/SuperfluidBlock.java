package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

/**
 * Superfluid (Helium-3) — a zero-friction quantum fluid.
 * - Climbs walls: places copies of itself on adjacent vertical surfaces
 * - Entities inside get zero friction (speed boost, no deceleration)
 * - Extremely fast flow rate
 * - Emits ethereal wispy particles
 */
public class SuperfluidBlock extends TranslucentLiquidBlock {

    private static final int CLIMB_TICKS = 6;
    private static final int MAX_CLIMB_HEIGHT = 8;

    public SuperfluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, CLIMB_TICKS);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        // Only source blocks climb
        if (state.getFluidState().getAmount() < 8) return;

        // Try to climb walls — check all 4 horizontal directions
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos wallPos = pos.relative(dir);
            BlockState wallState = level.getBlockState(wallPos);

            // If there's a solid wall, try to climb up
            if (wallState.isSolidRender(level, wallPos)) {
                tryClimbWall(level, pos, dir);
            }
        }

        level.scheduleTick(pos, this, CLIMB_TICKS);
    }

    private void tryClimbWall(ServerLevel level, BlockPos fluidPos, Direction wallDir) {
        // Climb up along the wall face
        for (int y = 1; y <= MAX_CLIMB_HEIGHT; y++) {
            BlockPos climbPos = fluidPos.above(y);
            BlockPos wallCheck = climbPos.relative(wallDir);
            BlockState climbState = level.getBlockState(climbPos);
            BlockState wallState = level.getBlockState(wallCheck);

            // Need a solid wall next to us and air/replaceable at the climb position
            if (!wallState.isSolidRender(level, wallCheck)) break;

            if (climbState.isAir()) {
                // Place superfluid here (flowing, not source)
                level.setBlock(climbPos, ModBlocks.SUPERFLUID_BLOCK.get().defaultBlockState(), 3);
                // Wispy particles
                double cx = climbPos.getX() + 0.5;
                double cy = climbPos.getY() + 0.5;
                double cz = climbPos.getZ() + 0.5;
                level.sendParticles(ParticleTypes.END_ROD, cx, cy, cz, 3, 0.2, 0.2, 0.2, 0.01);
                break; // Climb one block at a time
            } else if (climbState.getBlock() instanceof SuperfluidBlock) {
                continue; // Already climbed here, try higher
            } else {
                break; // Hit something solid, stop climbing
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // Zero friction — boost entity speed and remove deceleration
        Vec3 movement = entity.getDeltaMovement();
        // Reduce gravity drag in the fluid and give a slight speed boost
        entity.setDeltaMovement(movement.x * 1.05, movement.y * 0.98, movement.z * 1.05);
        entity.resetFallDistance();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        // Ethereal wispy particles — pale blue/white
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.END_ROD, x, y, z,
                    (random.nextDouble() - 0.5) * 0.01,
                    random.nextDouble() * 0.03,
                    (random.nextDouble() - 0.5) * 0.01);
        }

        // Faint snowflake particles (quantum cold)
        if (random.nextInt(5) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.9;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SNOWFLAKE, x, y, z, 0, 0.01, 0);
        }
    }
}
