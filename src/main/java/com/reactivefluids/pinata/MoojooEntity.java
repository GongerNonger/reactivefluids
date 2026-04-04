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
 * Highland cow piñata — evolved from Doenut. Moo-juice coloring. Produces milk.
 */
public class MoojooEntity extends BasePinataEntity {
    public MoojooEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 5; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override public String getPinataSpeciesName() { return "Moojoo"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.WHEAT), new ItemStack(Items.SPRUCE_SAPLING)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.SPRUCE_SAPLING)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.HAY_BLOCK)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.MOOJOO_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        // Default goals only
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.WHEAT), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        MoojooEntity baby = ModPinataEntities.MOOJOO.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
