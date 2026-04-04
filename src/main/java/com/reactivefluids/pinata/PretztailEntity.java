package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.HuntPreyGoal;
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
 * Pretztail — fox piñata, cunning predator. Eats Bunnycombs.
 *
 * VP model: Sleek fox body, large bushy pretzel-twisted tail,
 * pointy ears, long snout. Orange-red with brown pretzel-pattern markings.
 *
 * VP: Appear with 2 Bunnycomb residents. Resident: eat 1 Bunnycomb.
 * Romance: eat 2 Bunnycombs. Evolves into Pieena (feed bone).
 */
public class PretztailEntity extends BasePinataEntity {

    public PretztailEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 4;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35) // Fast hunter
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    public String getPinataSpeciesName() { return "Pretztail"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(new ItemStack(Items.CHICKEN), new ItemStack(Items.RABBIT));
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        // Must eat Bunnycombs to become resident
        return List.of(new ItemStack(Items.RABBIT));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        return List.of(new ItemStack(Items.CHICKEN));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.PRETZTAIL_CANDY.get(), 1 + random.nextInt(baseCandyCount))
        );
    }

    @Override public int getVariantCount() { return 2; }

    @Override
    protected void registerPinataGoals() {
        // Hunt Bunnycombs — fox eats rabbits
        goalSelector.addGoal(2, new HuntPreyGoal(this,
                () -> ModPinataEntities.BUNNYCOMB.get(), 1.4, 16.0));
        // Also hunts Mousemallows as secondary prey
        goalSelector.addGoal(3, new HuntPreyGoal(this,
                () -> ModPinataEntities.MOUSEMALLOW.get(), 1.3, 12.0));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, stack ->
                stack.is(Items.CHICKEN) || stack.is(Items.RABBIT), false));
    }

    @Override
    protected void tickClientParticles() {
        super.tickClientParticles();
        // Sly sneak particles when hunting
        if (getDeltaMovement().horizontalDistanceSqr() > 0.02 && random.nextInt(8) == 0) {
            level().addParticle(net.minecraft.core.particles.ParticleTypes.POOF,
                    getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        PretztailEntity baby = ModPinataEntities.PRETZTAIL.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
