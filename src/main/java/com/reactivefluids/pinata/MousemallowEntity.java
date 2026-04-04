package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.EatItemEntityGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Mousemallow — a tiny mouse piñata, very common herbivore.
 *
 * VP reference model: Small round body with large round ears,
 * pink/white marshmallow coloring. Long thin tail.
 * Twitchy movements, very quick.
 *
 * VP facts:
 * - Appear: Have a turnip seed in garden
 * - Visit: Have a turnip seed
 * - Resident: Eat a turnip seed
 * - Romance: Have a Mousemallow house, eat a piece of cheese
 * - Eaten by: Syrupent, Pretztail, Barkbark
 * - Very common, breeds quickly
 *
 * MC mapping:
 * - Attracted to beetroot seeds (turnip stand-in)
 * - Very small and fast
 * - Primary prey for Syrupent
 * - Romance food: cheese → milk bucket (stand-in)
 */
public class MousemallowEntity extends BasePinataEntity {

    public MousemallowEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)    // Very fragile
                .add(Attributes.MOVEMENT_SPEED, 0.35) // Fast!
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    public String getPinataSpeciesName() { return "Mousemallow"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(
                new ItemStack(Items.BEETROOT_SEEDS),
                new ItemStack(Items.WHEAT_SEEDS)
        );
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        return List.of(new ItemStack(Items.BEETROOT_SEEDS));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        // Cheese → milk bucket stand-in
        return List.of(new ItemStack(Items.MILK_BUCKET));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.MOUSEMALLOW_CANDY.get(), 1 + random.nextInt(baseCandyCount))
        );
    }

    @Override
    public int getVariantCount() { return 2; }

    @Override
    protected void registerPinataGoals() {
        // Flee from Syrupent (their predator)
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this,
                SyrupentEntity.class, 10.0F, 1.4, 1.6));

        // Also flee from Pretztail
        // goalSelector.addGoal(1, new AvoidEntityGoal<>(this,
        //         PretztailEntity.class, 10.0F, 1.4, 1.6));

        // Eat seeds off the ground
        goalSelector.addGoal(3, new EatItemEntityGoal(this,
                stack -> stack.is(Items.BEETROOT_SEEDS) || stack.is(Items.WHEAT_SEEDS),
                1.2, 8.0, 5));

        // Tempted by seeds
        goalSelector.addGoal(4, new TemptGoal(this, 1.2, stack ->
                stack.is(Items.BEETROOT_SEEDS) || stack.is(Items.WHEAT_SEEDS), false));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        MousemallowEntity baby = ModPinataEntities.MOUSEMALLOW.get().create(level);
        if (baby != null) {
            baby.setLifecycle(LIFECYCLE_RESIDENT);
            baby.setHappiness(75);
        }
        return baby;
    }

}
