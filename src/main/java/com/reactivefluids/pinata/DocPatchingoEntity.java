package com.reactivefluids.pinata;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Doc Patchingo — the piñata doctor NPC.
 *
 * VP: Heals sick piñatas for a fee. Can be called when a piñata is sick.
 * The player pays chocolate coins, and Doc Patchingo heals the piñata.
 *
 * MC: Right-click with gold/chocolate coin to heal all nearby sick piñatas.
 * Wanders around the garden. Green cross particles when healing.
 */
public class DocPatchingoEntity extends PathfinderMob {

    private int healCooldown;

    public DocPatchingoEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (healCooldown > 0) healCooldown--;

        // Auto-heal nearby very sick piñatas (happiness 0) for free
        if (!level().isClientSide() && tickCount % 100 == 0) {
            autoHealCritical();
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);

            if (healCooldown > 0) {
                player.sendSystemMessage(Component.literal(
                        "Doc Patchingo: \"I need a moment to prepare more medicine!\""));
                return InteractionResult.SUCCESS;
            }

            // Pay to heal all nearby sick piñatas
            boolean hasCoin = stack.getItem() instanceof ChocolateCoinItem
                    || stack.is(Items.GOLD_INGOT);

            if (hasCoin) {
                AABB area = getBoundingBox().inflate(16);
                List<BasePinataEntity> sick = level().getEntitiesOfClass(BasePinataEntity.class, area,
                        p -> p.getHappiness() <= 20);

                if (sick.isEmpty()) {
                    player.sendSystemMessage(Component.literal(
                            "Doc Patchingo: \"All your piñatas look healthy to me!\""));
                } else {
                    if (!player.getAbilities().instabuild) stack.shrink(1);

                    for (BasePinataEntity pinata : sick) {
                        pinata.setHappiness(60);
                        if (level() instanceof ServerLevel sl) {
                            sl.sendParticles(ParticleTypes.HEART,
                                    pinata.getX(), pinata.getY() + pinata.getBbHeight() + 0.5,
                                    pinata.getZ(), 3, 0.2, 0.1, 0.2, 0);
                            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    pinata.getX(), pinata.getY() + 0.5,
                                    pinata.getZ(), 5, 0.3, 0.3, 0.3, 0.02);
                        }
                    }

                    player.sendSystemMessage(Component.literal(
                            "Doc Patchingo: \"All patched up! " + sick.size() + " piñata(s) healed!\""));
                    playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.5F);
                    healCooldown = 600; // 30 second cooldown
                }
                return InteractionResult.SUCCESS;
            }

            // No payment — just give info
            AABB area = getBoundingBox().inflate(16);
            long sickCount = level().getEntitiesOfClass(BasePinataEntity.class, area,
                    p -> p.getHappiness() <= 20).size();
            if (sickCount > 0) {
                player.sendSystemMessage(Component.literal(
                        "Doc Patchingo: \"I see " + sickCount + " sick piñata(s) nearby. "
                                + "Give me a gold ingot or chocolate coin to heal them!\""));
            } else {
                player.sendSystemMessage(Component.literal(
                        "Doc Patchingo: \"Everyone's in good health! Prevention is the best medicine!\""));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(true);
    }

    private void autoHealCritical() {
        // Free auto-heal for critically sick piñatas (happiness 0) — prevent Dastardos
        AABB area = getBoundingBox().inflate(8);
        List<BasePinataEntity> critical = level().getEntitiesOfClass(BasePinataEntity.class, area,
                p -> p.getHappiness() <= 0);
        for (BasePinataEntity p : critical) {
            p.setHappiness(10); // Bring back from brink, but not fully healed
        }
    }

    @Override public boolean isPersistenceRequired() { return true; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean shouldDropExperience() { return false; }
}
