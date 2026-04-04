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
 * Swan piñata — elegant aquatic. Banana-yellow coloring. Part of Pigxie recipe.
 */
public class SwananaEntity extends BasePinataEntity {
    public SwananaEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 5; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 16)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override public String getPinataSpeciesName() { return "Swanana"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.WHEAT_SEEDS), new ItemStack(Items.BREAD)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.BREAD)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.CAKE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.SWANANA_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.WATER, 1.0, 20));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.WHEAT_SEEDS), false));
    }

    @Override public boolean canBreatheUnderwater() { return true; }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        SwananaEntity baby = ModPinataEntities.SWANANA.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
