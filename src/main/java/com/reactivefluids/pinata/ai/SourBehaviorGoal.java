package com.reactivefluids.pinata.ai;

import com.reactivefluids.pinata.BasePinataEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Sour piñata hostile behavior — makes sour piñatas cause trouble in gardens.
 *
 * Behaviors vary by species but share common patterns:
 * - Wander aggressively toward garden features
 * - Destroy/eat flowers, seeds, or other items
 * - Attack other piñatas
 * - Steal eggs
 *
 * Sour piñatas can be tamed by feeding them specific items,
 * which triggers a cocoon transformation into a friendly resident.
 */
public class SourBehaviorGoal extends Goal {

    public enum SourAction {
        DESTROY_FLOWERS,  // Shellybean, Profitamole
        ATTACK_PINATAS,   // Mallowolf, Cocoadile
        EAT_SICK,         // Crowla
        STEAL_ITEMS,      // Macaraccoon
        START_FIGHTS,     // Bonboon
        GENERIC_MISCHIEF  // Default
    }

    private final BasePinataEntity sour;
    private final SourAction action;
    private final double speed;
    private BlockPos targetBlock;
    private BasePinataEntity targetEntity;
    private int actionTimer;
    private int cooldown;

    public SourBehaviorGoal(BasePinataEntity sour, SourAction action, double speed) {
        this.sour = sour;
        this.action = action;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!sour.isSour()) return false;
        if (cooldown > 0) { cooldown--; return false; }
        return findTarget();
    }

    @Override
    public boolean canContinueToUse() {
        if (!sour.isSour()) return false;
        if (actionTimer > 200) return false;
        if (action == SourAction.DESTROY_FLOWERS && targetBlock != null) {
            return sour.level().getBlockState(targetBlock).is(net.minecraft.tags.BlockTags.FLOWERS);
        }
        if (targetEntity != null) return targetEntity.isAlive();
        return targetBlock != null;
    }

    @Override
    public void start() {
        actionTimer = 0;
    }

    @Override
    public void tick() {
        actionTimer++;

        switch (action) {
            case DESTROY_FLOWERS -> tickDestroyFlowers();
            case ATTACK_PINATAS -> tickAttackPinatas();
            case START_FIGHTS -> tickStartFights();
            default -> tickGenericMischief();
        }
    }

    @Override
    public void stop() {
        targetBlock = null;
        targetEntity = null;
        actionTimer = 0;
        cooldown = 100 + sour.getRandom().nextInt(200);
    }

    private void tickDestroyFlowers() {
        if (targetBlock == null) return;
        sour.getNavigation().moveTo(
                targetBlock.getX() + 0.5, targetBlock.getY(), targetBlock.getZ() + 0.5, speed);

        double dist = sour.distanceToSqr(targetBlock.getX() + 0.5,
                targetBlock.getY(), targetBlock.getZ() + 0.5);
        if (dist < 2.0) {
            if (sour.level() instanceof ServerLevel serverLevel) {
                serverLevel.destroyBlock(targetBlock, false);
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        targetBlock.getX() + 0.5, targetBlock.getY() + 0.5,
                        targetBlock.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0);
                sour.playSound(SoundEvents.GRASS_BREAK, 1.0F, 0.8F);
            }
            targetBlock = null;
        }
    }

    private void tickAttackPinatas() {
        if (targetEntity == null) return;
        sour.getLookControl().setLookAt(targetEntity, 30, 30);
        sour.getNavigation().moveTo(targetEntity, speed * 1.2);

        double reach = (sour.getBbWidth() + targetEntity.getBbWidth()) * 0.5 + 0.5;
        if (sour.distanceToSqr(targetEntity) < reach * reach) {
            sour.doHurtTarget(targetEntity);
            targetEntity.addHappiness(-10);
            targetEntity = null;
        }
    }

    private void tickStartFights() {
        // Make two nearby piñatas fight each other
        if (targetEntity == null) return;
        sour.getNavigation().moveTo(targetEntity, speed);

        if (sour.distanceToSqr(targetEntity) < 4.0) {
            // Find another piñata near the target and make them aggro
            AABB area = targetEntity.getBoundingBox().inflate(4);
            List<BasePinataEntity> nearby = sour.level().getEntitiesOfClass(
                    BasePinataEntity.class, area,
                    p -> p != targetEntity && p != sour && !p.isSour() && p.isResident());
            if (!nearby.isEmpty()) {
                BasePinataEntity victim = nearby.get(0);
                targetEntity.addHappiness(-5);
                victim.addHappiness(-5);
                if (sour.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                            targetEntity.getX(), targetEntity.getY() + 1,
                            targetEntity.getZ(), 3, 0.3, 0.2, 0.3, 0);
                }
            }
            targetEntity = null;
        }
    }

    private void tickGenericMischief() {
        // Wander around being menacing, smoke particles
        if (actionTimer % 20 == 0 && sour.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SMOKE,
                    sour.getX(), sour.getY() + sour.getBbHeight(),
                    sour.getZ(), 2, 0.2, 0.1, 0.2, 0.01);
        }
    }

    private boolean findTarget() {
        switch (action) {
            case DESTROY_FLOWERS -> {
                return findNearbyFlower();
            }
            case ATTACK_PINATAS, START_FIGHTS -> {
                return findNearbyPinata();
            }
            default -> {
                return sour.getRandom().nextInt(5) == 0;
            }
        }
    }

    private boolean findNearbyFlower() {
        BlockPos pos = sour.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-8, -2, -8), pos.offset(8, 2, 8))) {
            if (sour.level().getBlockState(p).is(net.minecraft.tags.BlockTags.FLOWERS)) {
                targetBlock = p.immutable();
                return true;
            }
        }
        return false;
    }

    private boolean findNearbyPinata() {
        AABB area = sour.getBoundingBox().inflate(12);
        List<BasePinataEntity> targets = sour.level().getEntitiesOfClass(
                BasePinataEntity.class, area,
                p -> p != sour && !p.isSour() && p.isResident());
        if (targets.isEmpty()) return false;
        targetEntity = targets.get(sour.getRandom().nextInt(targets.size()));
        return true;
    }
}
