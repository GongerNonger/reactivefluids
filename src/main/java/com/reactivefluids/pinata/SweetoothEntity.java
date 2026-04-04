package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.EatItemEntityGoal;
import com.reactivefluids.pinata.ai.HuntPreyGoal;
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
 * Sweetooth — bear piñata. Large, omnivore, loves honey.
 * VP model: Stocky bear body, round ears, big paws. Sweet-tooth candy coloring (caramel/toffee).
 * Needs Buzzlegum honey + a Sweetooth house to romance.
 */
public class SweetoothEntity extends BasePinataEntity {
    public SweetoothEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 5; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override public String getPinataSpeciesName() { return "Sweetooth"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.HONEYCOMB), new ItemStack(Items.HONEY_BOTTLE)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.HONEYCOMB)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.HONEY_BOTTLE)); }
    @Override public List<ItemStack> getCandyDrops() {
        return List.of(new ItemStack(ModPinataItems.SWEETOOTH_CANDY.get(), 2 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.HONEYCOMB, 1 + random.nextInt(2)));
    }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new EatItemEntityGoal(this, s -> s.is(Items.HONEYCOMB) || s.is(Items.HONEY_BOTTLE) || s.is(Items.SWEET_BERRIES), 1.0, 12.0, 10));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.HONEYCOMB) || s.is(Items.HONEY_BOTTLE), false));
    }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        SweetoothEntity baby = ModPinataEntities.SWEETOOTH.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
