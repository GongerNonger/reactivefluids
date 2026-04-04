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
 * Quackberry — duck piñata, needs water.
 *
 * VP model: Flat-billed duck body, wide flat feet, stubby wings,
 * blueberry-blue coloring with purple paper-fold accents.
 *
 * VP: Appear with 4 sq pinometers water. Resident: eat 1 bluebell seed.
 * Romance: has eaten 2 bluebell seeds + has Quackberry house.
 * Evolves into Juicygoose (feed gooseberry).
 * Conflicts with: Swanana.
 */
public class QuackberryEntity extends BasePinataEntity {

    public QuackberryEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    public String getPinataSpeciesName() { return "Quackberry"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(new ItemStack(Items.BLUE_ORCHID), new ItemStack(Items.WHEAT_SEEDS));
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        return List.of(new ItemStack(Items.BLUE_ORCHID));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        return List.of(new ItemStack(Items.SWEET_BERRIES));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.QUACKBERRY_CANDY.get(), 1 + random.nextInt(baseCandyCount))
        );
    }

    @Override public int getVariantCount() { return 2; }

    @Override
    protected void registerPinataGoals() {
        // Attracted to water (duck habitat)
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.WATER, 1.0, 20));
        goalSelector.addGoal(4, new EatItemEntityGoal(this,
                stack -> stack.is(Items.BLUE_ORCHID) || stack.is(Items.WHEAT_SEEDS),
                1.0, 10.0, 6));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, stack ->
                stack.is(Items.SWEET_BERRIES) || stack.is(Items.BLUE_ORCHID), false));
    }

    @Override
    public boolean canBreatheUnderwater() { return true; }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        QuackberryEntity baby = ModPinataEntities.QUACKBERRY.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
