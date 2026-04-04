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
 * Shellybean — snail piñata, slow, eats flowers. Has sour variant.
 *
 * VP model: Spiral shell on back, soft body with eye stalks,
 * jelly-bean coloring (pastel greens/pinks). Very slow movement.
 *
 * VP: Appear with 1 daisy. Resident: eat 1 daisy. Romance: eat 1 thistle head.
 * Sour Shellybean eats flower seeds; tamed by feeding apple seed.
 */
public class ShellybeanEntity extends BasePinataEntity {

    public ShellybeanEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.12) // Very slow!
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.ARMOR, 6.0); // Shell protection
    }

    @Override
    public String getPinataSpeciesName() { return "Shellybean"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(new ItemStack(Items.OXEYE_DAISY), new ItemStack(Items.DANDELION));
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        return List.of(new ItemStack(Items.OXEYE_DAISY));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        return List.of(new ItemStack(Items.FERN));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.SHELLYBEAN_CANDY.get(), 1 + random.nextInt(baseCandyCount))
        );
    }

    @Override public int getVariantCount() { return 3; }

    @Override
    protected void registerPinataGoals() {
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.OXEYE_DAISY, 0.8, 12));
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.DANDELION, 0.8, 12));
        goalSelector.addGoal(4, new EatItemEntityGoal(this,
                stack -> stack.is(Items.OXEYE_DAISY) || stack.is(Items.DANDELION)
                        || stack.is(Items.POPPY),
                0.8, 8.0, 8));
        goalSelector.addGoal(5, new TemptGoal(this, 0.9, stack ->
                stack.is(Items.OXEYE_DAISY) || stack.is(Items.FERN), false));
    }

    @Override
    protected void tickClientParticles() {
        super.tickClientParticles();
        // Slime trail
        if (getDeltaMovement().horizontalDistanceSqr() > 0.0001 && random.nextInt(4) == 0) {
            level().addParticle(net.minecraft.core.particles.ParticleTypes.ITEM_SLIME,
                    getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        ShellybeanEntity baby = ModPinataEntities.SHELLYBEAN.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
