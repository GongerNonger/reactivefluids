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
 * Owl piñata — fruit-salad coloring. Nocturnal hunter of Mousemallows.
 */
public class HootyfruityEntity extends BasePinataEntity {
    public HootyfruityEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 4; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 12)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override public String getPinataSpeciesName() { return "Hootyfruity"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.ROTTEN_FLESH)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.SPIDER_EYE)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.FERMENTED_SPIDER_EYE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.HOOTYFRUITY_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.HuntPreyGoal(this, () -> ModPinataEntities.MOUSEMALLOW.get(), 1.2, 12.0));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.SPIDER_EYE), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        HootyfruityEntity baby = ModPinataEntities.HOOTYFRUITY.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
