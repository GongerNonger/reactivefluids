package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.AttractedToBlockGoal;
import com.reactivefluids.pinata.ai.HuntPreyGoal;
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
 * Syrupent — a snake piñata, medium predator.
 *
 * VP reference model: Long serpentine body made of connected segments,
 * golden-amber/syrup coloring with darker diamond patterns.
 * Slithers along the ground. Head raised slightly with paper forked tongue.
 *
 * VP facts:
 * - Appear: Have 4 sq pinometers of long grass
 * - Visit: Have 6 sq pinometers of long grass
 * - Resident: Has eaten 2 Mousemallows
 * - Romance: Has eaten 3 Mousemallows, have a Syrupent house
 * - Predator of: Mousemallow
 * - Attracted to: long grass
 *
 * MC mapping:
 * - Attracted to tall grass
 * - Hunts Mousemallows
 * - Slithering movement model (multi-segment)
 * - Romance food: apple (syrup connection)
 */
public class SyrupentEntity extends BasePinataEntity {

    public SyrupentEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 4;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    public String getPinataSpeciesName() { return "Syrupent"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(
                new ItemStack(Items.APPLE),
                new ItemStack(Items.HONEY_BOTTLE)
        );
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        // Must eat Mousemallows to become resident (via HuntPreyGoal)
        // Apple as backup
        return List.of(new ItemStack(Items.APPLE));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        return List.of(new ItemStack(Items.HONEY_BOTTLE));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.SYRUPENT_CANDY.get(), 1 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.HONEY_BOTTLE, random.nextInt(2))
        );
    }

    @Override
    public int getVariantCount() { return 2; }

    @Override
    protected void registerPinataGoals() {
        // Hunt Mousemallows — primary food source
        goalSelector.addGoal(2, new HuntPreyGoal(this,
                () -> ModPinataEntities.MOUSEMALLOW.get(), 1.3, 14.0));

        // Attracted to tall grass (habitat)
        goalSelector.addGoal(4, new AttractedToBlockGoal(this,
                () -> Blocks.TALL_GRASS, 0.9, 16));

        // Tempted by honey/apples
        goalSelector.addGoal(5, new TemptGoal(this, 1.0, stack ->
                stack.is(Items.APPLE) || stack.is(Items.HONEY_BOTTLE), false));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        SyrupentEntity baby = ModPinataEntities.SYRUPENT.get().create(level);
        if (baby != null) {
            baby.setLifecycle(LIFECYCLE_RESIDENT);
            baby.setHappiness(75);
        }
        return baby;
    }

    @Override
    protected void tickClientParticles() {
        super.tickClientParticles();
        // Slither trail — subtle ground particles
        if (getDeltaMovement().horizontalDistanceSqr() > 0.001 && random.nextInt(6) == 0) {
            level().addParticle(new net.minecraft.core.particles.BlockParticleOption(
                            net.minecraft.core.particles.ParticleTypes.FALLING_DUST,
                            net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState()),
                    getX(), getY(), getZ(), 0, 0, 0);
        }
    }
}
