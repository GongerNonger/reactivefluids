package com.reactivefluids.pinata;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Canary piñata — evolved from Sparrowmint. Bright canary-yellow. Sings.
 */
public class CandaryEntity extends BasePinataEntity {
    public CandaryEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 4; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 14)
                .add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override public String getPinataSpeciesName() { return "Candary"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.DANDELION), new ItemStack(Items.WHEAT_SEEDS)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.DANDELION)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.SUNFLOWER)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.CANDARY_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 3; }

    @Override protected void registerPinataGoals() {
        // Default goals only
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.DANDELION), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        CandaryEntity baby = ModPinataEntities.CANDARY.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
