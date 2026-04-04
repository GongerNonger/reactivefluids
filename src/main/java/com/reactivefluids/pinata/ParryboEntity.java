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
 * Parrot piñata — colorful, mimics sounds. Many color variants.
 */
public class ParryboEntity extends BasePinataEntity {
    public ParryboEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 3; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override public String getPinataSpeciesName() { return "Parrybo"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.MELON_SEEDS), new ItemStack(Items.WHEAT_SEEDS)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.MELON_SEEDS)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.PUMPKIN_SEEDS)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.PARRYBO_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 4; }

    @Override protected void registerPinataGoals() {
        // Default goals only
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.MELON_SEEDS), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        ParryboEntity baby = ModPinataEntities.PARRYBO.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
