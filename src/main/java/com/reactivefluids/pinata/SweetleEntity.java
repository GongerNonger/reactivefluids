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
 * Beetle piñata — sweet/candy-shell coloring. Many color variants.
 */
public class SweetleEntity extends BasePinataEntity {
    public SweetleEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 3; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 8)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 3.0);
    }

    @Override public String getPinataSpeciesName() { return "Sweetle"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.APPLE), new ItemStack(Items.SWEET_BERRIES)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.APPLE)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.HONEYCOMB)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.SWEETLE_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 4; }

    @Override protected void registerPinataGoals() {
        // Default goals only
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.APPLE), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        SweetleEntity baby = ModPinataEntities.SWEETLE.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
