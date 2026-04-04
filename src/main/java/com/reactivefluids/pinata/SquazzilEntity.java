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
 * Squazzil — squirrel piñata. Quick, collects nuts, attracted to trees.
 * VP model: Fluffy body, huge bushy tail, big cheeks. Hazelnut-brown coloring.
 * Loves acorns (oak saplings in MC).
 */
public class SquazzilEntity extends BasePinataEntity {
    public SquazzilEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 3; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.JUMP_STRENGTH, 0.6);
    }

    @Override public String getPinataSpeciesName() { return "Squazzil"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.OAK_SAPLING), new ItemStack(Items.BIRCH_SAPLING)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.OAK_SAPLING)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.ACACIA_SAPLING)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.SQUAZZIL_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new AttractedToBlockGoal(this, () -> Blocks.OAK_LOG, 1.1, 16));
        goalSelector.addGoal(4, new EatItemEntityGoal(this, s -> s.is(Items.OAK_SAPLING) || s.is(Items.BIRCH_SAPLING), 1.2, 10.0, 6));
        goalSelector.addGoal(5, new TemptGoal(this, 1.2, s -> s.is(Items.OAK_SAPLING), false));
    }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        SquazzilEntity baby = ModPinataEntities.SQUAZZIL.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
