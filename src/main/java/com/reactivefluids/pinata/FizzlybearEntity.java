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
 * Brown bear piñata — fizzy-drink coloring. Evolves into Polollybear (feed lapis).
 */
public class FizzlybearEntity extends BasePinataEntity {
    public FizzlybearEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 6; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 22)
                .add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override public String getPinataSpeciesName() { return "Fizzlybear"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.HONEYCOMB), new ItemStack(Items.SWEET_BERRIES)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.HONEYCOMB)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.HONEY_BOTTLE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.FIZZLYBEAR_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        // Default goals only
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.HONEYCOMB), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        FizzlybearEntity baby = ModPinataEntities.FIZZLYBEAR.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
