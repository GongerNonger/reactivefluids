package com.reactivefluids;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Guardian-style laser attack for the crystallized skeleton.
 * Charges up while looking at the target, then fires a damaging beam.
 */
public class CrystalLaserGoal extends Goal {

    private final CrystallizedSkeleton skeleton;
    private int attackTime;
    private static final int ATTACK_DURATION = CrystallizedSkeleton.LASER_CHARGE_TICKS;
    private static final int COOLDOWN = 60; // 3 seconds between attacks

    public CrystalLaserGoal(CrystallizedSkeleton skeleton) {
        this.skeleton = skeleton;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.skeleton.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.skeleton.getTarget();
        return target != null && target.isAlive()
                && this.skeleton.distanceToSqr(target) < 20.0 * 20.0;
    }

    @Override
    public void start() {
        this.attackTime = -10; // brief delay before starting charge
        this.skeleton.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.skeleton.setLaserTarget(0);
        this.skeleton.setTarget(null);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.skeleton.getTarget();
        if (target == null) return;

        // Always face the target
        this.skeleton.getLookControl().setLookAt(target, 90.0F, 90.0F);

        double distSqr = this.skeleton.distanceToSqr(target);

        // Move towards target if too far, back away if too close
        if (distSqr > 12.0 * 12.0) {
            this.skeleton.getNavigation().moveTo(target, 1.0);
        } else if (distSqr < 4.0 * 4.0) {
            this.skeleton.getNavigation().stop();
            // Back away slightly
            this.skeleton.getMoveControl().setWantedPosition(
                    this.skeleton.getX() - (target.getX() - this.skeleton.getX()) * 0.5,
                    this.skeleton.getY(),
                    this.skeleton.getZ() - (target.getZ() - this.skeleton.getZ()) * 0.5,
                    0.8);
        } else {
            this.skeleton.getNavigation().stop();
        }

        // Check line of sight
        if (!this.skeleton.hasLineOfSight(target)) {
            this.skeleton.setLaserTarget(0);
            this.attackTime = -10;
            return;
        }

        this.attackTime++;

        // Start laser charge-up
        if (this.attackTime == 0) {
            this.skeleton.setLaserTarget(target.getId());
        }

        // Fire!
        if (this.attackTime >= ATTACK_DURATION) {
            this.skeleton.fireLaser(target);
            this.skeleton.setLaserTarget(0);
            this.attackTime = -COOLDOWN;
        }
    }
}
