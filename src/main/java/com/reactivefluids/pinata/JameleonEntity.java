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
 * Chameleon piñata — jam-colored. 6 variants (changes color). Camouflage ability.
 */
public class JameleonEntity extends BasePinataEntity {
    public JameleonEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 3; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override public String getPinataSpeciesName() { return "Jameleon"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.GLOW_BERRIES)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.SPIDER_EYE)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.FERMENTED_SPIDER_EYE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.JAMELEON_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 6; }

    @Override protected void registerPinataGoals() {
        // Default goals only
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.SPIDER_EYE), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        JameleonEntity baby = ModPinataEntities.JAMELEON.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
