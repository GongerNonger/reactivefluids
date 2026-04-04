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
 * Gorilla piñata — sarsaparilla-brown. Powerful, territorial. Apex land predator.
 */
public class SarsgorillaEntity extends BasePinataEntity {
    public SarsgorillaEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 7; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override public String getPinataSpeciesName() { return "Sarsgorilla"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.APPLE), new ItemStack(Items.MELON_SLICE)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.MELON_SLICE)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.GOLDEN_APPLE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.SARSGORILLA_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        // Default goals only
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.APPLE), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        SarsgorillaEntity baby = ModPinataEntities.SARSGORILLA.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
