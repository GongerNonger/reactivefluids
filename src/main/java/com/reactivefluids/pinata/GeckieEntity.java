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
 * Gecko piñata — desert lizard. Gummy-candy coloring.
 */
public class GeckieEntity extends BasePinataEntity {
    public GeckieEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 3; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 8)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override public String getPinataSpeciesName() { return "Geckie"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.GLOW_BERRIES)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.GLOW_BERRIES)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.GLOWSTONE_DUST)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.GECKIE_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 3; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SAND, 0.9, 16));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.SPIDER_EYE), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        GeckieEntity baby = ModPinataEntities.GECKIE.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
