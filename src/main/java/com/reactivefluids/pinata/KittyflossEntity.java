package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.HuntPreyGoal;
import com.reactivefluids.pinata.ai.SpeciesConflictGoal;
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
 * Kittyfloss — cat piñata. Agile, independent, conflicts with Barkbark.
 * VP model: Sleek cat body, pointed ears, long curling tail. Candy-floss pink coloring.
 */
public class KittyflossEntity extends BasePinataEntity {
    public KittyflossEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 4; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override public String getPinataSpeciesName() { return "Kittyfloss"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.COD), new ItemStack(Items.SALMON)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.COD)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.SALMON)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.KITTYFLOSS_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 3; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(2, new SpeciesConflictGoal(this, () -> ModPinataEntities.BARKBARK.get(), 10.0));
        goalSelector.addGoal(3, new HuntPreyGoal(this, () -> ModPinataEntities.MOUSEMALLOW.get(), 1.3, 10.0));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.COD) || s.is(Items.SALMON), false));
    }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        KittyflossEntity baby = ModPinataEntities.KITTYFLOSS.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
