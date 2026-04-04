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
 * Armadillo piñata — evolved from Fudgehog. Heavy armor (8). Parma ham coloring.
 */
public class ParmadilloEntity extends BasePinataEntity {
    public ParmadilloEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 5; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 18)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, 8.0);
    }

    @Override public String getPinataSpeciesName() { return "Parmadillo"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.COCOA_BEANS), new ItemStack(Items.SWEET_BERRIES)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.COCOA_BEANS)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.MELON_SLICE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.PARMADILLO_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        // Default goals only
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.COCOA_BEANS), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        ParmadilloEntity baby = ModPinataEntities.PARMADILLO.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
