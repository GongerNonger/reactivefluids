package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.AttractedToBlockGoal;
import com.reactivefluids.pinata.ai.HuntPreyGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Cocoadile — crocodile piñata. Large aquatic predator, has sour variant.
 * VP model: Long flat body, powerful jaws, armored back, short legs, long tail. Cocoa-brown coloring.
 * Sour Cocoadile attacks helpers; tamed by feeding Sweetooth + Swanana + water.
 */
public class CocoadileEntity extends BasePinataEntity {
    public CocoadileEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 6; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 28.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override public String getPinataSpeciesName() { return "Cocoadile"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.COD), new ItemStack(Items.TROPICAL_FISH)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.COD)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.PUFFERFISH)); }
    @Override public List<ItemStack> getCandyDrops() {
        return List.of(new ItemStack(ModPinataItems.COCOADILE_CANDY.get(), 2 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.COCOA_BEANS, 1 + random.nextInt(3)));
    }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new AttractedToBlockGoal(this, () -> Blocks.WATER, 0.9, 20));
        goalSelector.addGoal(5, new TemptGoal(this, 1.0, s -> s.is(Items.COD) || s.is(Items.TROPICAL_FISH), false));
    }

    @Override public boolean canBreatheUnderwater() { return true; }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        CocoadileEntity baby = ModPinataEntities.COCOADILE.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
