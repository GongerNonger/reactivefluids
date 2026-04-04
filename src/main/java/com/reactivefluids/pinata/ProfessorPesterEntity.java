package com.reactivefluids.pinata;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
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

import java.util.Comparator;
import java.util.List;

/**
 * Professor Pester — the main antagonist boss of Viva Piñata.
 *
 * VP behavior:
 * - Appears randomly in established gardens (rare event)
 * - Targets the most valuable piñata in the garden
 * - Cannot be stopped by Captain's Cutlass (he destroys it)
 * - Can be bribed with 500 chocolate coins (16 gold blocks as stand-in)
 * - Scared off by Dragonache or Choclodocus being present
 * - Limeoceros will fight him directly
 * - Destroys target piñata if not stopped
 *
 * MC implementation:
 * - Boss-tier hostile mob with high stats
 * - Homes in on highest-HP piñata (proxy for value)
 * - Very high bribe cost
 * - Checks for Dragonache/Choclodocus/Limeoceros deterrents
 * - Dramatic entrance with thunder sounds and lightning particles
 */
public class ProfessorPesterEntity extends PathfinderMob {

    private BasePinataEntity targetPinata;
    private int searchCooldown;
    private int lifetimeTicks;
    private boolean fleeing;
    private static final int MAX_LIFETIME = 4800; // 4 minutes

    public ProfessorPesterEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.lifetimeTicks = MAX_LIFETIME;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)  // Boss-level
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            // Menacing aura
            if (random.nextInt(3) == 0) {
                level().addParticle(ParticleTypes.LARGE_SMOKE,
                        getX() + (random.nextDouble() - 0.5) * getBbWidth(),
                        getY() + random.nextDouble() * getBbHeight(),
                        getZ() + (random.nextDouble() - 0.5) * getBbWidth(),
                        0, 0.02, 0);
            }
            return;
        }

        lifetimeTicks--;
        if (lifetimeTicks <= 0 || fleeing) {
            despawn();
            return;
        }

        // Check for deterrents
        if (checkDeterrents()) {
            fleeing = true;
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        getX(), getY() + 2, getZ(), 5, 0.3, 0.3, 0.3, 0);
                sl.playSound(null, blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.HOSTILE, 1.5F, 0.5F);
            }
            return;
        }

        // Find target
        if (targetPinata == null || !targetPinata.isAlive()) {
            if (searchCooldown <= 0) {
                findTarget();
                searchCooldown = 60;
            } else {
                searchCooldown--;
            }
        }

        // Chase and destroy
        if (targetPinata != null && targetPinata.isAlive()) {
            getLookControl().setLookAt(targetPinata, 30, 30);
            getNavigation().moveTo(targetPinata, 1.2);

            double reach = (getBbWidth() + targetPinata.getBbWidth()) * 0.5 + 1.0;
            if (distanceToSqr(targetPinata) < reach * reach) {
                destroyPinata();
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Bribe with gold blocks (expensive!)
        if (stack.is(Items.GOLD_BLOCK)) {
            if (!level().isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                fleeing = true;
                playSound(SoundEvents.VILLAGER_YES, 1.0F, 0.5F);
                player.sendSystemMessage(Component.literal(
                        "Professor Pester: \"Hmph! Fine, I'll leave... for now.\""));
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            getX(), getY() + 2, getZ(), 10, 0.5, 0.3, 0.5, 0);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Chocolate coins also work but need more
        if (stack.getItem() instanceof ChocolateCoinItem && stack.getCount() >= 16) {
            if (!level().isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(16);
                }
                fleeing = true;
                playSound(SoundEvents.VILLAGER_YES, 1.0F, 0.5F);
                player.sendSystemMessage(Component.literal(
                        "Professor Pester: \"A generous donation! I'll be back...\""));
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    private boolean checkDeterrents() {
        AABB area = getBoundingBox().inflate(20);
        // Dragonache scares Professor Pester
        if (!level().getEntitiesOfClass(DragonacheEntity.class, area).isEmpty()) return true;
        // Choclodocus scares Professor Pester
        if (!level().getEntitiesOfClass(ChoclodocusEntity.class, area).isEmpty()) return true;
        // Limeoceros fights Professor Pester directly
        var rhinos = level().getEntitiesOfClass(LimeocerosEntity.class, area);
        if (!rhinos.isEmpty()) {
            // Limeoceros charges!
            for (var rhino : rhinos) {
                rhino.getNavigation().moveTo(this, 1.5);
            }
            return true;
        }
        return false;
    }

    private void findTarget() {
        AABB area = getBoundingBox().inflate(40);
        List<BasePinataEntity> pinatas = level().getEntitiesOfClass(BasePinataEntity.class, area,
                p -> p.isResident() && p.isAlive());
        if (pinatas.isEmpty()) {
            fleeing = true;
            return;
        }
        // Target highest HP (proxy for most valuable)
        pinatas.sort(Comparator.comparingDouble(p -> -p.getMaxHealth()));
        targetPinata = pinatas.get(0);
    }

    private void destroyPinata() {
        if (targetPinata != null && level() instanceof ServerLevel sl) {
            // Dramatic destruction
            sl.sendParticles(ParticleTypes.EXPLOSION,
                    targetPinata.getX(), targetPinata.getY() + 0.5, targetPinata.getZ(),
                    5, 0.5, 0.5, 0.5, 0);
            sl.sendParticles(ParticleTypes.SMOKE,
                    targetPinata.getX(), targetPinata.getY(), targetPinata.getZ(),
                    20, 0.5, 0.5, 0.5, 0.05);
            playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0F, 0.8F);

            // Evil dialogue
            for (Player player : sl.players()) {
                if (player.distanceToSqr(this) < 40 * 40) {
                    player.sendSystemMessage(Component.literal(
                            "Professor Pester: \"Another piñata destroyed! Ha ha ha!\""));
                }
            }

            targetPinata.kill();
            targetPinata = null;
            fleeing = true; // Leave after destroying one
        }
    }

    private void despawn() {
        if (level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.LARGE_SMOKE,
                    getX(), getY() + 1, getZ(), 15, 0.5, 0.5, 0.5, 0.05);
            playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 0.5F);
        }
        discard();
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        // Massive reward for killing Professor Pester
        spawnAtLocation(new ItemStack(ModPinataItems.CHOCOLATE_COIN.get(), 32));
        spawnAtLocation(new ItemStack(Items.DIAMOND, 3));
    }

    @Override public boolean isPersistenceRequired() { return true; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PesterLifetime", lifetimeTicks);
        tag.putBoolean("PesterFleeing", fleeing);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lifetimeTicks = tag.getInt("PesterLifetime");
        fleeing = tag.getBoolean("PesterFleeing");
    }
}
