package com.reactivefluids.pinata.ai;

import com.reactivefluids.pinata.BasePinataEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Makes a piñata walk toward and consume item entities on the ground.
 * Used for herbivores that eat specific food items (seeds, flowers, fruit).
 * Consuming food increases happiness.
 */
public class EatItemEntityGoal extends Goal {

    private final BasePinataEntity pinata;
    private final Predicate<ItemStack> foodTest;
    private final double speed;
    private final double searchRange;
    private final int happinessGain;
    private ItemEntity targetItem;
    private int eatTimer;

    public EatItemEntityGoal(BasePinataEntity pinata, Predicate<ItemStack> foodTest,
                             double speed, double searchRange, int happinessGain) {
        this.pinata = pinata;
        this.foodTest = foodTest;
        this.speed = speed;
        this.searchRange = searchRange;
        this.happinessGain = happinessGain;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (pinata.getRandom().nextInt(20) != 0) return false; // Don't check every tick
        List<ItemEntity> items = findNearbyFood();
        if (items.isEmpty()) return false;
        targetItem = items.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return targetItem != null && targetItem.isAlive() && !targetItem.hasPickUpDelay()
                && pinata.distanceToSqr(targetItem) <= searchRange * searchRange;
    }

    @Override
    public void start() {
        eatTimer = 0;
    }

    @Override
    public void tick() {
        eatTimer++;
        pinata.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);
        pinata.getNavigation().moveTo(targetItem, speed);

        double reachSq = 1.5;
        if (pinata.distanceToSqr(targetItem) <= reachSq) {
            consumeItem();
        }
    }

    @Override
    public void stop() {
        targetItem = null;
        eatTimer = 0;
    }

    private void consumeItem() {
        if (!pinata.level().isClientSide() && targetItem != null && targetItem.isAlive()) {
            ItemStack stack = targetItem.getItem();
            stack.shrink(1);
            if (stack.isEmpty()) {
                targetItem.discard();
            }
            pinata.addHappiness(happinessGain);
            pinata.playSound(SoundEvents.GENERIC_EAT, 0.8F,
                    1.0F + pinata.getRandom().nextFloat() * 0.3F);
            pinata.spawnHappyParticles();
            targetItem = null;
        }
    }

    private List<ItemEntity> findNearbyFood() {
        AABB searchArea = pinata.getBoundingBox().inflate(searchRange);
        return pinata.level().getEntitiesOfClass(ItemEntity.class, searchArea,
                item -> item.isAlive() && !item.hasPickUpDelay()
                        && foodTest.test(item.getItem()));
    }
}
