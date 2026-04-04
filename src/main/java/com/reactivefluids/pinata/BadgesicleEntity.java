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
 * Badger piñata — ice-lolly striped. Tough burrower.
 */
public class BadgesicleEntity extends BasePinataEntity {
    public BadgesicleEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 4; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 16)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 3.0);
    }

    @Override public String getPinataSpeciesName() { return "Badgesicle"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.SWEET_BERRIES), new ItemStack(Items.APPLE)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.SWEET_BERRIES)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.GLOW_BERRIES)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.BADGESICLE_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SHORT_GRASS, 0.9, 16));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.SWEET_BERRIES), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        BadgesicleEntity baby = ModPinataEntities.BADGESICLE.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
