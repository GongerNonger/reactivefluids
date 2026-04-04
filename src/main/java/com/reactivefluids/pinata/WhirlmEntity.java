package com.reactivefluids.pinata;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Whirlm — a small worm-like piñata, the simplest and first creature.
 *
 * Viva Piñata facts:
 * - Appear requirement: Have 1 square of dirt/grass in garden
 * - Visit requirement: Have 2 squares of dirt/grass
 * - Resident requirement: Have 4 squares of dirt/grass
 * - Romance requirement: Feed a daisy
 * - Eating a turnip seed evolves it into a Sparrowmint food chain target
 * - Can be eaten by: Sparrowmint, Hedgehog (Flapyak?)
 * - Produces: used as food by many other piñatas
 *
 * Minecraft mapping:
 * - Appear: 1+ dirt/grass blocks nearby
 * - Visit: 4+ dirt/grass blocks nearby
 * - Resident: 8+ dirt/grass blocks nearby (scaled for MC world)
 * - Romance food: Dandelion (MC equivalent of daisy)
 * - Eats: seeds, wheat seeds
 */
public class WhirlmEntity extends BasePinataEntity {

    public WhirlmEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 2; // Small piñata = less candy
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)    // Fragile little worm
                .add(Attributes.MOVEMENT_SPEED, 0.2) // Slow mover
                .add(Attributes.ATTACK_DAMAGE, 0.0); // Harmless
    }

    @Override
    public String getPinataSpeciesName() {
        return "Whirlm";
    }

    @Override
    public List<ItemStack> getVisitFoods() {
        // Any seed attracts a Whirlm to visit
        return List.of(
                new ItemStack(Items.WHEAT_SEEDS),
                new ItemStack(Items.BEETROOT_SEEDS),
                new ItemStack(Items.MELON_SEEDS),
                new ItemStack(Items.PUMPKIN_SEEDS)
        );
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        // Feed it more seeds to make it stay
        return List.of(
                new ItemStack(Items.WHEAT_SEEDS),
                new ItemStack(Items.BEETROOT_SEEDS)
        );
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        // Dandelion = MC equivalent of VP's daisy
        return List.of(new ItemStack(Items.DANDELION));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        // Candy drops when broken
        return List.of(
                new ItemStack(ModPinataItems.WHIRLM_CANDY.get(), 1 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.SUGAR, random.nextInt(2))
        );
    }

    @Override
    public int getVariantCount() {
        return 3; // Normal, pink variant, golden variant
    }

    @Override
    protected void registerPinataGoals() {
        // Whirlms like to dig — attracted to dirt/farmland
        goalSelector.addGoal(2, new TemptGoal(this, 1.1, stack ->
                stack.is(Items.WHEAT_SEEDS) || stack.is(Items.BEETROOT_SEEDS) ||
                stack.is(Items.MELON_SEEDS) || stack.is(Items.PUMPKIN_SEEDS) ||
                stack.is(Items.DANDELION), false));

        // Flee from chickens (Sparrowmint equivalent — they eat Whirlms)
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this,
                net.minecraft.world.entity.animal.Chicken.class, 8.0F, 1.2, 1.4));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        WhirlmEntity baby = ModPinataEntities.WHIRLM.get().create(level);
        if (baby != null) {
            baby.setLifecycle(LIFECYCLE_RESIDENT); // Babies born in garden are residents
            baby.setHappiness(75); // Happy baby
        }
        return baby;
    }

    @Override
    protected void tickClientParticles() {
        super.tickClientParticles();

        // Whirlms leave small dirt particles as they move
        if (getDeltaMovement().horizontalDistanceSqr() > 0.001 && random.nextInt(5) == 0) {
            level().addParticle(net.minecraft.core.particles.ParticleTypes.FALLING_DUST,
                    getX(), getY(), getZ(),
                    0, 0, 0);
        }
    }

    @Override
    public float getEyeHeight(net.minecraft.world.entity.Pose pose, net.minecraft.world.entity.EntityDimensions dimensions) {
        return 0.2F; // Low to the ground
    }
}
