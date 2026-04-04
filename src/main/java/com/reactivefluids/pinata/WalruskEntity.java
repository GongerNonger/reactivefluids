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
 * Walrus piñata — rusk-cracker tusks. Arctic aquatic heavyweight.
 */
public class WalruskEntity extends BasePinataEntity {
    public WalruskEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 6; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 28)
                .add(Attributes.MOVEMENT_SPEED, 0.18).add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 5.0);
    }

    @Override public String getPinataSpeciesName() { return "Walrusk"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.COD), new ItemStack(Items.SALMON)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.COD)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.TROPICAL_FISH)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.WALRUSK_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SNOW_BLOCK, 0.8, 16));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.COD), false));
    }


    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        WalruskEntity baby = ModPinataEntities.WALRUSK.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
