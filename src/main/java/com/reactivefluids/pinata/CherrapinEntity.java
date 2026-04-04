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
 * Turtle piñata — cherry-flavored shell. Aquatic. Very high armor.
 */
public class CherrapinEntity extends BasePinataEntity {
    public CherrapinEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 4; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 14)
                .add(Attributes.MOVEMENT_SPEED, 0.15).add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 7.0);
    }

    @Override public String getPinataSpeciesName() { return "Cherrapin"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.SEAGRASS), new ItemStack(Items.KELP)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.SEAGRASS)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.TURTLE_EGG)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.CHERRAPIN_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.WATER, 0.8, 16));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.SEAGRASS), false));
    }

    @Override public boolean canBreatheUnderwater() { return true; }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        CherrapinEntity baby = ModPinataEntities.CHERRAPIN.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
