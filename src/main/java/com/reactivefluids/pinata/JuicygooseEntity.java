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
 * Goose piñata — evolved from Quackberry. Juicy fruit coloring. Aquatic.
 */
public class JuicygooseEntity extends BasePinataEntity {
    public JuicygooseEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 5; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 16)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override public String getPinataSpeciesName() { return "Juicygoose"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.SWEET_BERRIES), new ItemStack(Items.BREAD)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.SWEET_BERRIES)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.GOLDEN_APPLE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.JUICYGOOSE_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.WATER, 1.0, 20));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.SWEET_BERRIES), false));
    }


    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        JuicygooseEntity baby = ModPinataEntities.JUICYGOOSE.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
