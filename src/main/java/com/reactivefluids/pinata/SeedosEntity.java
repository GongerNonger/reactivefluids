package com.reactivefluids.pinata;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Seedos — the friendly seed-giving NPC from Viva Piñata.
 *
 * Wanders around gardens giving free random seeds to players
 * who interact with him. Has a cooldown per player.
 *
 * VP reference: eccentric gardener character who walks around
 * scattering seeds. Wears a plant/leaf-themed outfit.
 */
public class SeedosEntity extends PathfinderMob {

    private int interactionCooldown;

    private static final List<Item> SEED_POOL = List.of(
            Items.WHEAT_SEEDS,
            Items.BEETROOT_SEEDS,
            Items.MELON_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.TORCHFLOWER_SEEDS,
            Items.DANDELION,
            Items.POPPY,
            Items.OXEYE_DAISY,
            Items.BLUE_ORCHID,
            Items.ALLIUM,
            Items.CORNFLOWER,
            Items.LILY_OF_THE_VALLEY,
            Items.SUNFLOWER,
            Items.OAK_SAPLING,
            Items.BIRCH_SAPLING,
            Items.SPRUCE_SAPLING,
            Items.SWEET_BERRIES,
            Items.CARROT,
            Items.POTATO
    );

    public SeedosEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (interactionCooldown > 0) interactionCooldown--;

        // Seedos scatters particles as he walks
        if (level().isClientSide() && getDeltaMovement().horizontalDistanceSqr() > 0.001) {
            if (random.nextInt(5) == 0) {
                level().addParticle(ParticleTypes.COMPOSTER,
                        getX() + (random.nextDouble() - 0.5) * 0.5,
                        getY() + 0.5,
                        getZ() + (random.nextDouble() - 0.5) * 0.5,
                        0, 0.02, 0);
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide()) {
            if (interactionCooldown > 0) {
                player.sendSystemMessage(Component.literal(
                        "Seedos: \"Come back later, I'm sorting my seeds!\""));
                return InteractionResult.SUCCESS;
            }

            // Give 1-3 random seeds
            int count = 1 + random.nextInt(3);
            for (int i = 0; i < count; i++) {
                Item seed = SEED_POOL.get(random.nextInt(SEED_POOL.size()));
                ItemStack stack = new ItemStack(seed, 1 + random.nextInt(3));
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }

            // Dialogue
            String[] quotes = {
                    "Seedos: \"Here, try these! Plant them carefully!\"",
                    "Seedos: \"Seeds, seeds, wonderful seeds!\"",
                    "Seedos: \"Every garden starts with a single seed!\"",
                    "Seedos: \"I've got pockets full of potential!\"",
                    "Seedos: \"These ones are extra special, I can feel it!\""
            };
            player.sendSystemMessage(Component.literal(
                    quotes[random.nextInt(quotes.length)]));

            // Effects
            playSound(SoundEvents.VILLAGER_TRADE, 1.0F, 1.2F);
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        getX(), getY() + 1.5, getZ(),
                        8, 0.3, 0.3, 0.3, 0.02);
            }

            interactionCooldown = 1200; // 60 seconds
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(true);
    }

    @Override
    public boolean isPersistenceRequired() { return true; }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    public boolean shouldDropExperience() { return false; }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource source) {
        // Drop a bunch of seeds on death
        for (int i = 0; i < 10; i++) {
            Item seed = SEED_POOL.get(random.nextInt(SEED_POOL.size()));
            spawnAtLocation(new ItemStack(seed, 1 + random.nextInt(5)));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SeedosCooldown", interactionCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        interactionCooldown = tag.getInt("SeedosCooldown");
    }
}
