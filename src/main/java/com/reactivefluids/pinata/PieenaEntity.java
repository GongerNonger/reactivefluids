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
 * Hyena piñata — evolved from Pretztail. Pie-crust coloring. Laughing hunter.
 */
public class PieenaEntity extends BasePinataEntity {
    public PieenaEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 5; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.MOVEMENT_SPEED, 0.33).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 1.0);
    }

    @Override public String getPinataSpeciesName() { return "Pieena"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.BONE), new ItemStack(Items.ROTTEN_FLESH)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.BONE)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.COOKED_BEEF)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.PIEENA_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.HuntPreyGoal(this, () -> ModPinataEntities.BUNNYCOMB.get(), 1.4, 14.0));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.BONE), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        PieenaEntity baby = ModPinataEntities.PIEENA.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
