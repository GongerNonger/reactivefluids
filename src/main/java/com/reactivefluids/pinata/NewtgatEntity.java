package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.AttractedToBlockGoal;
import com.reactivefluids.pinata.ai.EatItemEntityGoal;
import com.reactivefluids.pinata.ai.SpeciesConflictGoal;
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
 * Newtgat — newt piñata, semi-aquatic (needs water + land).
 *
 * VP model: Lizard-like body with flat paddle tail, four splayed legs,
 * nougat-brown/orange coloring with darker spots.
 *
 * VP: Needs water + land. Resident: eat 1 bluebell seed.
 * Evolves into Salamango (feed chili).
 * Conflicts with: Lickatoad.
 */
public class NewtgatEntity extends BasePinataEntity {

    public NewtgatEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    public String getPinataSpeciesName() { return "Newtgat"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(new ItemStack(Items.BLUE_ORCHID), new ItemStack(Items.TROPICAL_FISH));
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        return List.of(new ItemStack(Items.BLUE_ORCHID));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        return List.of(new ItemStack(Items.TROPICAL_FISH));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.NEWTGAT_CANDY.get(), 1 + random.nextInt(baseCandyCount))
        );
    }

    @Override public int getVariantCount() { return 2; }

    @Override
    protected void registerPinataGoals() {
        // Conflicts with Lickatoad — they fight when near
        goalSelector.addGoal(2, new SpeciesConflictGoal(this,
                () -> ModPinataEntities.LICKATOAD.get(), 8.0));
        // Attracted to water (semi-aquatic)
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.WATER, 0.9, 16));
        goalSelector.addGoal(4, new EatItemEntityGoal(this,
                stack -> stack.is(Items.TROPICAL_FISH) || stack.is(Items.BLUE_ORCHID),
                1.0, 10.0, 6));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, stack ->
                stack.is(Items.TROPICAL_FISH), false));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        NewtgatEntity baby = ModPinataEntities.NEWTGAT.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
