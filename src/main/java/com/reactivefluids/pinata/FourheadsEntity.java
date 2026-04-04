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
 * Hydra piñata — four-headed serpent. One of the rarest species.
 */
public class FourheadsEntity extends BasePinataEntity {
    public FourheadsEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 8; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 5.0);
    }

    @Override public String getPinataSpeciesName() { return "Fourheads"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.COOKED_BEEF), new ItemStack(Items.COOKED_PORKCHOP)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.COOKED_BEEF)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.GOLDEN_APPLE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.FOURHEADS_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 1; }

    @Override protected void registerPinataGoals() {
        // Default goals only
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.COOKED_BEEF), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        FourheadsEntity baby = ModPinataEntities.FOURHEADS.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
