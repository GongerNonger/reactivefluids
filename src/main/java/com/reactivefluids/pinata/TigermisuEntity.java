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
 * Tiger piñata — tiramisu-striped. Conflicts with Roario.
 */
public class TigermisuEntity extends BasePinataEntity {
    public TigermisuEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 6; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 24)
                .add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override public String getPinataSpeciesName() { return "Tigermisu"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.COOKED_PORKCHOP), new ItemStack(Items.COD)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.COOKED_PORKCHOP)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.COOKED_BEEF)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.TIGERMISU_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 3; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(2, new com.reactivefluids.pinata.ai.SpeciesConflictGoal(this, () -> ModPinataEntities.ROARIO.get(), 10.0));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.COOKED_PORKCHOP), false));
    }



    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        TigermisuEntity baby = ModPinataEntities.TIGERMISU.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
