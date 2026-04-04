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
 * Ant piñata — raisin-colored. Tiny. Conflicts with Buzzlegum (ant vs bee).
 */
public class RaisantEntity extends BasePinataEntity {
    public RaisantEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 2; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 4)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 1.0);
    }

    @Override public String getPinataSpeciesName() { return "Raisant"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.SUGAR), new ItemStack(Items.COOKIE)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.SUGAR)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.CAKE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.RAISANT_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(2, new com.reactivefluids.pinata.ai.SpeciesConflictGoal(this, () -> ModPinataEntities.BUZZLEGUM.get(), 8.0));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.SUGAR), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        RaisantEntity baby = ModPinataEntities.RAISANT.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
