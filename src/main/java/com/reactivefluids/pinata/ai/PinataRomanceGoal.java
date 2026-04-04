package com.reactivefluids.pinata.ai;

import com.reactivefluids.pinata.BasePinataEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Custom romance goal for piñatas. Unlike vanilla breeding:
 * - Both piñatas must be in "romancing" state (set by feeding romance food)
 * - Both must be residents with happiness > 60
 * - They path toward each other, perform a "dance" (circling + particles)
 * - After the dance, an egg item/entity is spawned (baby after hatching)
 * - Romance cooldown is applied to both
 *
 * The dance lasts ~5 seconds with heart particles and music.
 */
public class PinataRomanceGoal extends Goal {

    private final BasePinataEntity pinata;
    private final double speed;
    private BasePinataEntity partner;
    private int danceTimer;
    private int searchCooldown;

    private static final int DANCE_DURATION = 100; // 5 seconds
    private static final int SEARCH_RANGE = 16;
    private static final int ROMANCE_COOLDOWN = 6000; // 5 minutes

    public PinataRomanceGoal(BasePinataEntity pinata, double speed) {
        this.pinata = pinata;
        this.speed = speed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!pinata.isRomancing() || !pinata.isResident()) return false;
        if (pinata.getHappiness() < 60) return false;
        if (searchCooldown > 0) { searchCooldown--; return false; }
        searchCooldown = 20; // Check every second

        partner = findRomancingPartner();
        return partner != null;
    }

    @Override
    public boolean canContinueToUse() {
        return partner != null && partner.isAlive() && partner.isRomancing()
                && pinata.isRomancing() && danceTimer < DANCE_DURATION;
    }

    @Override
    public void start() {
        danceTimer = 0;
    }

    @Override
    public void tick() {
        danceTimer++;

        // Phase 1 (0-40 ticks): approach each other
        if (danceTimer < 40) {
            pinata.getLookControl().setLookAt(partner, 30.0F, 30.0F);
            pinata.getNavigation().moveTo(partner, speed);
        }
        // Phase 2 (40-100 ticks): dance! Circle and emit particles
        else {
            pinata.getNavigation().stop();
            pinata.getLookControl().setLookAt(partner, 30.0F, 30.0F);

            // Circle motion
            double angle = (danceTimer - 40) * 0.15;
            double radius = 1.5;
            double midX = (pinata.getX() + partner.getX()) / 2;
            double midZ = (pinata.getZ() + partner.getZ()) / 2;

            // Emit heart particles
            if (danceTimer % 5 == 0 && pinata.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART,
                        pinata.getX(), pinata.getY() + pinata.getBbHeight() + 0.5,
                        pinata.getZ(), 1, 0.2, 0.2, 0.2, 0);
                serverLevel.sendParticles(ParticleTypes.HEART,
                        partner.getX(), partner.getY() + partner.getBbHeight() + 0.5,
                        partner.getZ(), 1, 0.2, 0.2, 0.2, 0);
            }

            // Note particles during dance
            if (danceTimer % 8 == 0 && pinata.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.NOTE,
                        midX, pinata.getY() + 1.5, midZ,
                        1, 0.5, 0.3, 0.5, 0);
            }
        }

        // Dance complete!
        if (danceTimer >= DANCE_DURATION) {
            completeRomance();
        }
    }

    @Override
    public void stop() {
        if (partner != null) {
            // If stopped early, cancel romancing state
            if (danceTimer < DANCE_DURATION) {
                pinata.setRomancing(false);
                partner.setRomancing(false);
            }
        }
        partner = null;
        danceTimer = 0;
    }

    private void completeRomance() {
        if (pinata.level() instanceof ServerLevel serverLevel) {
            // Spawn baby
            var baby = pinata.getBreedOffspring(serverLevel, partner);
            if (baby != null) {
                baby.moveTo(
                        (pinata.getX() + partner.getX()) / 2,
                        pinata.getY(),
                        (pinata.getZ() + partner.getZ()) / 2,
                        0, 0);
                baby.setBaby(true);
                serverLevel.addFreshEntity(baby);

                // Celebration particles
                serverLevel.sendParticles(ParticleTypes.FIREWORK,
                        baby.getX(), baby.getY() + 0.5, baby.getZ(),
                        15, 0.3, 0.3, 0.3, 0.1);
                serverLevel.sendParticles(ParticleTypes.HEART,
                        baby.getX(), baby.getY() + 1.0, baby.getZ(),
                        5, 0.3, 0.3, 0.3, 0);
            }

            // Sound
            pinata.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.5F);

            // XP for garden
            // (GardenManager XP integration would go here)

            // Reset both parents
            pinata.setRomancing(false);
            partner.setRomancing(false);
            pinata.romanceCooldown = ROMANCE_COOLDOWN;
            partner.romanceCooldown = ROMANCE_COOLDOWN;
            pinata.addHappiness(10);
            partner.addHappiness(10);
        }
    }

    private BasePinataEntity findRomancingPartner() {
        AABB searchArea = pinata.getBoundingBox().inflate(SEARCH_RANGE);
        List<BasePinataEntity> candidates = pinata.level().getEntitiesOfClass(
                BasePinataEntity.class, searchArea,
                other -> other != pinata
                        && other.getClass() == pinata.getClass()
                        && other.isRomancing()
                        && other.isResident()
                        && other.getHappiness() >= 60
                        && other.romanceCooldown <= 0);

        if (candidates.isEmpty()) return null;
        // Pick nearest
        candidates.sort((a, b) -> Double.compare(
                pinata.distanceToSqr(a), pinata.distanceToSqr(b)));
        return candidates.get(0);
    }
}
