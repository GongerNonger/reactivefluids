package com.reactivefluids.pinata.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;

import java.util.EnumSet;
import java.util.function.Supplier;

/**
 * Makes a piñata wander toward nearby blocks of a specific type.
 * Used for species that are attracted to certain garden features:
 * e.g., Fudgehog attracted to long grass, Quackberry attracted to water.
 *
 * Scans periodically (not every tick) for performance.
 */
public class AttractedToBlockGoal extends Goal {

    private final PathfinderMob mob;
    private final Supplier<Block> targetBlock;
    private final double speed;
    private final int searchRadius;
    private BlockPos targetPos;
    private int scanCooldown;

    public AttractedToBlockGoal(PathfinderMob mob, Supplier<Block> targetBlock, double speed, int searchRadius) {
        this.mob = mob;
        this.targetBlock = targetBlock;
        this.speed = speed;
        this.searchRadius = searchRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (scanCooldown > 0) {
            scanCooldown--;
            return false;
        }
        scanCooldown = 40 + mob.getRandom().nextInt(40); // Scan every 2-4 seconds

        targetPos = findNearestBlock();
        return targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null) return false;
        if (!mob.level().getBlockState(targetPos).is(targetBlock.get())) return false;
        return mob.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5) > 2.0;
    }

    @Override
    public void tick() {
        mob.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, speed);
    }

    @Override
    public void stop() {
        targetPos = null;
    }

    private BlockPos findNearestBlock() {
        BlockPos mobPos = mob.blockPosition();
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                mobPos.offset(-searchRadius, -2, -searchRadius),
                mobPos.offset(searchRadius, 2, searchRadius))) {
            if (mob.level().getBlockState(pos).is(targetBlock.get())) {
                double dist = mobPos.distSqr(pos);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = pos.immutable();
                }
            }
        }
        return nearest;
    }
}
