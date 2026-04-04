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
 * Fudgehog — a hedgehog piñata, loves long grass.
 *
 * VP reference model: Round spiky body (like a ball with paper spines),
 * chocolate-brown base with caramel/fudge-colored spines. Small face
 * peeking out front. Waddles when walking.
 *
 * VP facts:
 * - Appear: Have 10 sq pinometers of long grass
 * - Visit: Have 12 sq pinometers of long grass
 * - Resident: Have 14 sq pinometers of long grass
 * - Romance: Have a Fudgehog house, feed a thistle seed
 * - Evolves into: Parmadillo (feed a coconut)
 * - Eats: Thistle seeds
 *
 * MC mapping:
 * - Attracted to tall grass blocks
 * - Resident requirement: enough tall grass nearby
 * - Romance food: fern (thistle equivalent)
 * - Evolves into Parmadillo when fed cocoa beans (coconut stand-in)
 */
public class FudgehogEntity extends BasePinataEntity {

    public FudgehogEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22) // Slow waddle
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 4.0); // Spiny defense
    }

    @Override
    public String getPinataSpeciesName() { return "Fudgehog"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(
                new ItemStack(Items.FERN),
                new ItemStack(Items.SWEET_BERRIES)
        );
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        return List.of(new ItemStack(Items.FERN));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        // Thistle seed equivalent
        return List.of(new ItemStack(Items.SWEET_BERRIES));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.FUDGEHOG_CANDY.get(), 1 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.COCOA_BEANS, random.nextInt(2))
        );
    }

    @Override
    public int getVariantCount() { return 2; }

    @Override
    protected void registerPinataGoals() {
        // Attracted to tall grass
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.SHORT_GRASS, 1.0, 16));
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.TALL_GRASS, 1.0, 16));

        // Eat ferns/berries off the ground
        goalSelector.addGoal(4, new EatItemEntityGoal(this,
                stack -> stack.is(Items.FERN) || stack.is(Items.SWEET_BERRIES),
                1.0, 10.0, 8));

        // Tempted by sweet berries in player hand
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, stack ->
                stack.is(Items.SWEET_BERRIES) || stack.is(Items.FERN), false));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        FudgehogEntity baby = ModPinataEntities.FUDGEHOG.get().create(level);
        if (baby != null) {
            baby.setLifecycle(LIFECYCLE_RESIDENT);
            baby.setHappiness(75);
        }
        return baby;
    }

    @Override
    protected void tickClientParticles() {
        super.tickClientParticles();
        // Rustle particles when walking through grass
        if (getDeltaMovement().horizontalDistanceSqr() > 0.001 && random.nextInt(10) == 0) {
            if (level().getBlockState(blockPosition()).is(Blocks.SHORT_GRASS) ||
                level().getBlockState(blockPosition()).is(Blocks.TALL_GRASS)) {
                level().addParticle(net.minecraft.core.particles.ParticleTypes.COMPOSTER,
                        getX(), getY() + 0.2, getZ(), 0, 0.02, 0);
            }
        }
    }
}
