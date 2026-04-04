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
 * Two-headed snake piñata — ginger-snap coloring. Evolved from Syrupent line.
 */
public class TwingersnapEntity extends BasePinataEntity {
    public TwingersnapEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 5; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override public String getPinataSpeciesName() { return "Twingersnap"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.APPLE), new ItemStack(Items.HONEY_BOTTLE)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.APPLE)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.HONEY_BOTTLE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.TWINGERSNAP_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.HuntPreyGoal(this, () -> ModPinataEntities.MOUSEMALLOW.get(), 1.4, 14.0));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.APPLE), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        TwingersnapEntity baby = ModPinataEntities.TWINGERSNAP.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
