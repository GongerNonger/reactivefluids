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
 * Polar bear piñata — lollipop-white arctic apex. Evolved from Fizzlybear.
 */
public class PolollybearEntity extends BasePinataEntity {
    public PolollybearEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 7; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override public String getPinataSpeciesName() { return "Polollybear"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.COD), new ItemStack(Items.SALMON)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.SALMON)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.PUFFERFISH)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.POLOLLYBEAR_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SNOW_BLOCK, 0.9, 20));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.COD), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        PolollybearEntity baby = ModPinataEntities.POLOLLYBEAR.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
