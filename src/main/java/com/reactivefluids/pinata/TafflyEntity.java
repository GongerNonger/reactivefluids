package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.AttractedToBlockGoal;
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
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Taffly — a small fly/insect piñata attracted to fruit and flowers.
 *
 * VP reference model: Round body with translucent paper wings,
 * toffee-brown/amber coloring. Large compound eyes.
 * Hovers/buzzes around erratically. Very small.
 *
 * VP facts:
 * - Appear: Have a garden with a daisy
 * - Visit: Have a daisy in garden
 * - Resident: Have 2 daisies
 * - Romance: Have eaten a piece of fruit, have a Taffly house
 * - Eaten by: Lickatoad, Arocknid
 * - Evolves into: Reddhott (catch fire, then extinguish)
 *
 * MC mapping:
 * - Attracted to flowers (daisies, dandelions)
 * - Eats fruit items (apples, berries)
 * - Very small, buzzing movement
 * - Prey for Lickatoad
 */
public class TafflyEntity extends BasePinataEntity {

    private float buzzOffset; // Random offset for buzzing animation

    public TafflyEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 2;
        this.buzzOffset = random.nextFloat() * 6.28F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)    // Fragile
                .add(Attributes.MOVEMENT_SPEED, 0.3)  // Quick buzzer
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    public String getPinataSpeciesName() { return "Taffly"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(
                new ItemStack(Items.OXEYE_DAISY),
                new ItemStack(Items.DANDELION),
                new ItemStack(Items.APPLE)
        );
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        return List.of(
                new ItemStack(Items.OXEYE_DAISY),
                new ItemStack(Items.DANDELION)
        );
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        // Fruit
        return List.of(
                new ItemStack(Items.APPLE),
                new ItemStack(Items.SWEET_BERRIES)
        );
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.TAFFLY_CANDY.get(), 1 + random.nextInt(baseCandyCount))
        );
    }

    @Override
    public int getVariantCount() { return 2; }

    @Override
    protected void registerPinataGoals() {
        // Attracted to flowers
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.OXEYE_DAISY, 1.1, 20));
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.DANDELION, 1.1, 20));

        // Eat fruit off the ground
        goalSelector.addGoal(4, new EatItemEntityGoal(this,
                stack -> stack.is(Items.APPLE) || stack.is(Items.SWEET_BERRIES)
                        || stack.is(Items.GLOW_BERRIES),
                1.1, 10.0, 5));

        // Tempted by flowers/fruit in player hand
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, stack ->
                stack.is(Items.OXEYE_DAISY) || stack.is(Items.DANDELION)
                || stack.is(Items.APPLE), false));
    }

    @Override
    public void tick() {
        super.tick();

        // Buzzing hover effect — slight vertical oscillation
        if (!level().isClientSide() && !onGround() && getDeltaMovement().y < 0) {
            // Slow falling when airborne (buzz hover)
            setDeltaMovement(getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        TafflyEntity baby = ModPinataEntities.TAFFLY.get().create(level);
        if (baby != null) {
            baby.setLifecycle(LIFECYCLE_RESIDENT);
            baby.setHappiness(75);
        }
        return baby;
    }

    @Override
    protected void tickClientParticles() {
        super.tickClientParticles();
        // Buzzing wing sparkle particles
        if (random.nextInt(6) == 0) {
            double px = getX() + (random.nextDouble() - 0.5) * 0.3;
            double py = getY() + 0.3 + random.nextDouble() * 0.2;
            double pz = getZ() + (random.nextDouble() - 0.5) * 0.3;
            level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    px, py, pz, 0, -0.01, 0);
        }
    }

}
