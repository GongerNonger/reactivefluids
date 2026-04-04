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
 * Goobaa — sheep piñata. Produces wool at Shearing Shed.
 * VP model: Fluffy round body covered in wool, small face peeking out, stubby legs. Goo-white with pastel tints.
 */
public class GoobaaEntity extends BasePinataEntity {
    private int woolTimer;

    public GoobaaEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 4; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override public String getPinataSpeciesName() { return "Goobaa"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.WHEAT), new ItemStack(Items.FERN)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.WHEAT)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.FERN)); }
    @Override public List<ItemStack> getCandyDrops() {
        return List.of(new ItemStack(ModPinataItems.GOOBAA_CANDY.get(), 1 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.WHITE_WOOL, 1 + random.nextInt(2)));
    }
    @Override public int getVariantCount() { return 3; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(3, new AttractedToBlockGoal(this, () -> Blocks.SHORT_GRASS, 0.9, 16));
        goalSelector.addGoal(4, new EatItemEntityGoal(this, s -> s.is(Items.WHEAT) || s.is(Items.FERN), 1.0, 10.0, 6));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.WHEAT), false));
    }

    @Override public void tick() {
        super.tick();
        if (!level().isClientSide() && isResident() && getHappiness() > 50) {
            woolTimer++;
            if (woolTimer >= 8000) { spawnAtLocation(Items.WHITE_WOOL); woolTimer = 0; }
        }
    }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        GoobaaEntity baby = ModPinataEntities.GOOBAA.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
