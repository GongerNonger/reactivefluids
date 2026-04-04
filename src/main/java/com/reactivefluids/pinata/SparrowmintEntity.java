package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.HuntPreyGoal;
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
 * Sparrowmint — a small sparrow piñata, the first predator in the food chain.
 *
 * VP reference model: Round bird body with paper-fold wings, small beak,
 * bright yellow-green body with mint/teal accents. Stubby legs.
 *
 * VP facts:
 * - Appear: 1 Whirlm resident in garden
 * - Visit: 1 Whirlm resident
 * - Resident: Has eaten 1 Whirlm
 * - Romance: Has eaten 2 Whirlms, have a Buttercup in garden
 * - Evolves into: Candary (feed a dandelion)
 * - Predator of: Whirlm
 *
 * MC mapping:
 * - Attracted by Whirlm presence
 * - Hunts and eats Whirlms (HuntPreyGoal)
 * - Romance food: Buttercup → Yellow Flower / Dandelion
 */
public class SparrowmintEntity extends BasePinataEntity {

    public SparrowmintEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)  // Quick little bird
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    public String getPinataSpeciesName() { return "Sparrowmint"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        // Seeds attract sparrows
        return List.of(
                new ItemStack(Items.WHEAT_SEEDS),
                new ItemStack(Items.BEETROOT_SEEDS)
        );
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        // Must eat a Whirlm to become resident (handled by HuntPreyGoal)
        // But also accepts seeds as a backup
        return List.of(new ItemStack(Items.WHEAT_SEEDS));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        // Buttercup equivalent — dandelion in MC
        return List.of(new ItemStack(Items.DANDELION));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.SPARROWMINT_CANDY.get(), 1 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.FEATHER, random.nextInt(2))
        );
    }

    @Override
    public int getVariantCount() { return 2; } // Normal + bright variant

    @Override
    protected void registerPinataGoals() {
        // Hunt Whirlms — the core food chain mechanic
        goalSelector.addGoal(3, new HuntPreyGoal(this,
                () -> ModPinataEntities.WHIRLM.get(), 1.3, 12.0));

        // Tempted by seeds
        goalSelector.addGoal(4, new TemptGoal(this, 1.1, stack ->
                stack.is(Items.WHEAT_SEEDS) || stack.is(Items.BEETROOT_SEEDS), false));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        SparrowmintEntity baby = ModPinataEntities.SPARROWMINT.get().create(level);
        if (baby != null) {
            baby.setLifecycle(LIFECYCLE_RESIDENT);
            baby.setHappiness(75);
        }
        return baby;
    }

    @Override
    protected void tickClientParticles() {
        super.tickClientParticles();
        // Occasional feather particles when moving fast
        if (getDeltaMovement().horizontalDistanceSqr() > 0.01 && random.nextInt(8) == 0) {
            level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    getX(), getY() + 0.3, getZ(), 0, 0.01, 0);
        }
    }
}
