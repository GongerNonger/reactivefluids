package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.EatItemEntityGoal;
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
 * Rashberry — pig piñata. Common, loves mud, part of Pigxie cross-breed.
 * VP model: Round pig body, curly tail, snout, floppy ears. Raspberry-pink coloring.
 * Special: Swanana + Rashberry in Mystery House = Pigxie
 */
public class RashberryEntity extends BasePinataEntity {
    public RashberryEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 4; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override public String getPinataSpeciesName() { return "Rashberry"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.CARROT), new ItemStack(Items.POTATO)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.CARROT)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.POTATO)); }
    @Override public List<ItemStack> getCandyDrops() {
        return List.of(new ItemStack(ModPinataItems.RASHBERRY_CANDY.get(), 1 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.PORKCHOP, random.nextInt(2)));
    }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new EatItemEntityGoal(this, s -> s.is(Items.CARROT) || s.is(Items.POTATO) || s.is(Items.BEETROOT), 1.0, 10.0, 6));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.CARROT) || s.is(Items.POTATO), false));
    }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        RashberryEntity baby = ModPinataEntities.RASHBERRY.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
