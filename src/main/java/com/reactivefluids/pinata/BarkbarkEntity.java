package com.reactivefluids.pinata;

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
 * Barkbark — dog piñata. Loyal, follows player, conflicts with Kittyfloss.
 * VP model: Blocky dog body, floppy ears, wagging tail. Bark-brown coloring.
 */
public class BarkbarkEntity extends BasePinataEntity {
    public BarkbarkEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 4; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override public String getPinataSpeciesName() { return "Barkbark"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.BONE), new ItemStack(Items.COOKED_BEEF)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.BONE)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.COOKED_BEEF)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.BARKBARK_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 3; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(2, new SpeciesConflictGoal(this, () -> ModPinataEntities.KITTYFLOSS.get(), 10.0));
        goalSelector.addGoal(4, new FollowMobGoal(this, 1.0, 5.0F, 10.0F));
        goalSelector.addGoal(5, new TemptGoal(this, 1.2, s -> s.is(Items.BONE) || s.is(Items.COOKED_BEEF), false));
    }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        BarkbarkEntity baby = ModPinataEntities.BARKBARK.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
