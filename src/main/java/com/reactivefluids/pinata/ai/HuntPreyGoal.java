package com.reactivefluids.pinata.ai;

import com.reactivefluids.pinata.BasePinataEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

/**
 * Piñata predator AI — hunts and eats a specific prey species.
 * In VP, many piñatas require eating other piñatas to become residents or romance.
 * e.g., Sparrowmint eats Whirlms, Syrupent eats Mousemallows.
 *
 * When the predator catches the prey, the prey is killed (drops candy)
 * and the predator gains happiness.
 */
public class HuntPreyGoal extends Goal {

    private final BasePinataEntity hunter;
    private final Supplier<EntityType<?>> preyType;
    private final double speed;
    private final double huntRange;
    private LivingEntity target;
    private int cooldown;
    private int huntTimer;

    public HuntPreyGoal(BasePinataEntity hunter, Supplier<EntityType<?>> preyType, double speed, double huntRange) {
        this.hunter = hunter;
        this.preyType = preyType;
        this.speed = speed;
        this.huntRange = huntRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        // Only hunt when hungry (not at max happiness) and is a resident
        if (hunter.getHappiness() >= 90) return false;

        List<LivingEntity> prey = findNearbyPrey();
        if (prey.isEmpty()) return false;

        // Target the nearest prey
        target = prey.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive()) return false;
        if (hunter.distanceToSqr(target) > huntRange * huntRange * 4) return false;
        if (huntTimer > 400) return false; // Give up after 20 seconds
        return true;
    }

    @Override
    public void start() {
        huntTimer = 0;
    }

    @Override
    public void tick() {
        huntTimer++;
        hunter.getLookControl().setLookAt(target, 30.0F, 30.0F);
        hunter.getNavigation().moveTo(target, speed);

        // Check if close enough to eat
        double reachSq = (hunter.getBbWidth() + target.getBbWidth()) * 0.5 + 0.5;
        if (hunter.distanceToSqr(target) <= reachSq * reachSq) {
            eatPrey();
        }
    }

    @Override
    public void stop() {
        target = null;
        cooldown = 200 + hunter.getRandom().nextInt(200); // 10-20 second cooldown
        huntTimer = 0;
    }

    private void eatPrey() {
        if (!hunter.level().isClientSide()) {
            // Kill the prey (it drops candy via BasePinataEntity death logic)
            target.kill();
            // Hunter gains happiness from eating
            hunter.addHappiness(15);
            // Play eating sound
            hunter.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT,
                    1.0F, 1.0F + hunter.getRandom().nextFloat() * 0.2F);
        }
        target = null;
    }

    private List<LivingEntity> findNearbyPrey() {
        AABB searchArea = hunter.getBoundingBox().inflate(huntRange);
        return hunter.level().getEntitiesOfClass(LivingEntity.class, searchArea,
                entity -> entity.getType() == preyType.get() && entity.isAlive()
                        && hunter.distanceToSqr(entity) <= huntRange * huntRange);
    }
}
