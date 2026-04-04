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
 * Salamander piñata — evolved from Newtgat. Mango-colored. Fire immune.
 */
public class SalamangoEntity extends BasePinataEntity {
    public SalamangoEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 4; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 14)
                .add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 1.0);
    }

    @Override public String getPinataSpeciesName() { return "Salamango"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.BLAZE_POWDER), new ItemStack(Items.MAGMA_CREAM)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.BLAZE_POWDER)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.FIRE_CHARGE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.SALAMANGO_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        // Default goals only
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.BLAZE_POWDER), false));
    }

    @Override public boolean fireImmune() { return true; }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        SalamangoEntity baby = ModPinataEntities.SALAMANGO.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
