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
 * Monkey piñata — cinnamon-bun coloring. Agile tree-dweller. Conflicts with Bonboon.
 */
public class CinnamonkeyEntity extends BasePinataEntity {
    public CinnamonkeyEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 4; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 14)
                .add(Attributes.MOVEMENT_SPEED, 0.33).add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override public String getPinataSpeciesName() { return "Cinnamonkey"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.APPLE), new ItemStack(Items.COCOA_BEANS)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.APPLE)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.COOKIE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.CINNAMONKEY_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(2, new com.reactivefluids.pinata.ai.SpeciesConflictGoal(this, () -> ModPinataEntities.CINNAMONKEY.get(), 8.0));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.APPLE), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        CinnamonkeyEntity baby = ModPinataEntities.CINNAMONKEY.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
