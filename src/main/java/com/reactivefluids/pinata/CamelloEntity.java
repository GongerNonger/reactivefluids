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
 * Camel piñata — caramel-colored desert dweller. Attracted to sand.
 */
public class CamelloEntity extends BasePinataEntity {
    public CamelloEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 5; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 24)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override public String getPinataSpeciesName() { return "Camello"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.CACTUS), new ItemStack(Items.DEAD_BUSH)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.CACTUS)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.DRIED_KELP)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.CAMELLO_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SAND, 0.9, 20));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.CACTUS), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        CamelloEntity baby = ModPinataEntities.CAMELLO.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
