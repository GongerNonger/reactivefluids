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
 * Dinosaur piñata — chocolate coloring. Scares Professor Pester. Second rarest.
 */
public class ChoclodocusEntity extends BasePinataEntity {
    public ChoclodocusEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 9; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 45)
                .add(Attributes.MOVEMENT_SPEED, 0.18).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override public String getPinataSpeciesName() { return "Choclodocus"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.HAY_BLOCK), new ItemStack(Items.APPLE)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.HAY_BLOCK)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.CHOCLODOCUS_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        // Default goals only
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.HAY_BLOCK), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        ChoclodocusEntity baby = ModPinataEntities.CHOCLODOCUS.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
