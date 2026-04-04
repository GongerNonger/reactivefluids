package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.AttractedToBlockGoal;
import com.reactivefluids.pinata.ai.EatItemEntityGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Bunnycomb — rabbit piñata. Common, breeds fast, eaten by Pretztail.
 *
 * VP model: Round fluffy body, long upright ears, cotton-puff tail,
 * honeycomb-pattern paper texture in warm yellow/orange.
 *
 * VP: Appear with 3 daisies. Resident: eat 1 carrot. Romance: eat 1 daisy.
 * MC: Attracted to flowers/carrots. Romance food: dandelion.
 */
public class BunnycombEntity extends BasePinataEntity {

    public BunnycombEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.JUMP_STRENGTH, 0.6);
    }

    @Override
    public String getPinataSpeciesName() { return "Bunnycomb"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(new ItemStack(Items.CARROT), new ItemStack(Items.DANDELION));
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        return List.of(new ItemStack(Items.CARROT));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        return List.of(new ItemStack(Items.DANDELION));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.BUNNYCOMB_CANDY.get(), 1 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.CARROT, random.nextInt(2))
        );
    }

    @Override public int getVariantCount() { return 3; }

    @Override
    protected void registerPinataGoals() {
        // Flee from Pretztail (fox eats rabbits)
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this,
                PretztailEntity.class, 12.0F, 1.5, 1.8));
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.DANDELION, 1.1, 16));
        goalSelector.addGoal(4, new EatItemEntityGoal(this,
                stack -> stack.is(Items.CARROT) || stack.is(Items.DANDELION),
                1.2, 10.0, 8));
        goalSelector.addGoal(5, new TemptGoal(this, 1.2, stack ->
                stack.is(Items.CARROT) || stack.is(Items.DANDELION), false));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        BunnycombEntity baby = ModPinataEntities.BUNNYCOMB.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
