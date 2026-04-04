package com.reactivefluids.pinata.ai;

import com.reactivefluids.pinata.BasePinataEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

/**
 * Species conflict AI — two piñata species that don't get along.
 * When they're near each other, they fight (reduce each other's happiness).
 *
 * VP conflict pairs include:
 * - Barkbark vs Kittyfloss (dog vs cat)
 * - Lickatoad vs Newtgat (frog vs newt)
 * - Quackberry vs Swanana (duck vs swan)
 * etc.
 *
 * Fights reduce happiness and can make piñatas sick.
 * Bonboon piñata (when tamed) can prevent fights.
 */
public class SpeciesConflictGoal extends Goal {

    private final BasePinataEntity fighter;
    private final Supplier<EntityType<?>> enemyType;
    private final double aggroRange;
    private BasePinataEntity enemy;
    private int fightTimer;
    private int cooldown;

    private static final int FIGHT_DURATION = 60; // 3 seconds
    private static final int FIGHT_COOLDOWN = 400; // 20 seconds

    public SpeciesConflictGoal(BasePinataEntity fighter, Supplier<EntityType<?>> enemyType, double aggroRange) {
        this.fighter = fighter;
        this.enemyType = enemyType;
        this.aggroRange = aggroRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) { cooldown--; return false; }
        if (!fighter.isResident()) return false;

        // Find enemy
        AABB area = fighter.getBoundingBox().inflate(aggroRange);
        List<BasePinataEntity> enemies = fighter.level().getEntitiesOfClass(
                BasePinataEntity.class, area,
                e -> e.getType() == enemyType.get() && e.isAlive());
        if (enemies.isEmpty()) return false;

        enemy = enemies.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return enemy != null && enemy.isAlive() && fightTimer < FIGHT_DURATION
                && fighter.distanceToSqr(enemy) < aggroRange * aggroRange * 4;
    }

    @Override
    public void start() {
        fightTimer = 0;
    }

    @Override
    public void tick() {
        fightTimer++;
        fighter.getLookControl().setLookAt(enemy, 30, 30);
        fighter.getNavigation().moveTo(enemy, 1.2);

        // Fighting — exchange blows every second
        if (fightTimer % 20 == 0) {
            double reach = (fighter.getBbWidth() + enemy.getBbWidth()) * 0.5 + 1.0;
            if (fighter.distanceToSqr(enemy) < reach * reach) {
                // Both take "hits"
                fighter.addHappiness(-5);
                enemy.addHappiness(-5);

                // Fight particles
                if (fighter.level() instanceof ServerLevel sl) {
                    double midX = (fighter.getX() + enemy.getX()) / 2;
                    double midY = Math.max(fighter.getY(), enemy.getY()) + 0.5;
                    double midZ = (fighter.getZ() + enemy.getZ()) / 2;
                    sl.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                            midX, midY, midZ, 2, 0.3, 0.2, 0.3, 0);
                    sl.sendParticles(ParticleTypes.CRIT,
                            midX, midY, midZ, 5, 0.3, 0.3, 0.3, 0.1);
                }

                fighter.playSound(SoundEvents.PLAYER_ATTACK_WEAK, 0.6F,
                        0.8F + fighter.getRandom().nextFloat() * 0.4F);
            }
        }
    }

    @Override
    public void stop() {
        // Loser (lower happiness) gets sick marker
        if (enemy != null && fightTimer >= FIGHT_DURATION) {
            BasePinataEntity loser = fighter.getHappiness() < enemy.getHappiness() ? fighter : enemy;
            loser.addHappiness(-10); // Extra penalty for loser
        }
        enemy = null;
        fightTimer = 0;
        cooldown = FIGHT_COOLDOWN + fighter.getRandom().nextInt(200);
    }
}
