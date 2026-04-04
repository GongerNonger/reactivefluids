package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.AttractedToBlockGoal;
import com.reactivefluids.pinata.ai.EatItemEntityGoal;
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
 * Doenut — deer piñata. Graceful, shy, attracted to trees.
 * VP model: Elegant deer body, small antlers, large eyes, slender legs. Doughnut-brown with sugar-dust white spots.
 * Evolves into Moojoo (feed fir tree seed / spruce sapling).
 */
public class DoenutEntity extends BasePinataEntity {
    public DoenutEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 5; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.JUMP_STRENGTH, 0.5);
    }

    @Override public String getPinataSpeciesName() { return "Doenut"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.APPLE), new ItemStack(Items.OAK_SAPLING)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.APPLE)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.GOLDEN_APPLE)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.DOENUT_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, net.minecraft.world.entity.player.Player.class, 8.0F, 1.3, 1.6));
        goalSelector.addGoal(3, new AttractedToBlockGoal(this, () -> Blocks.OAK_LOG, 0.9, 16));
        goalSelector.addGoal(4, new EatItemEntityGoal(this, s -> s.is(Items.APPLE) || s.is(Items.OAK_SAPLING), 1.0, 10.0, 8));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.APPLE) || s.is(Items.GOLDEN_APPLE), false));
    }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        DoenutEntity baby = ModPinataEntities.DOENUT.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
