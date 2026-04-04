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
 * Horstachio — horse piñata. Large, majestic, valuable.
 *
 * VP model: Full horse body with paper-pistachio mane and tail,
 * strong legs, proud posture. Pistachio-green with cream/tan accents.
 * One of the larger piñatas.
 *
 * VP: Appear with 500 sq pinometers. Resident: eat 1 apple + 1 carrot + 1 buttercup.
 * Romance: eat 2 apples + 2 carrots + 2 buttercups.
 * Evolves into Zumbug (feed daisy + blackberry).
 * Conflicts with: Ponocky, Zumbug.
 */
public class HorstachioEntity extends BasePinataEntity {

    public HorstachioEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 6; // Large piñata = more candy
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)     // Tough
                .add(Attributes.MOVEMENT_SPEED, 0.3)   // Fast gallop
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.JUMP_STRENGTH, 0.6);
    }

    @Override
    public String getPinataSpeciesName() { return "Horstachio"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(new ItemStack(Items.APPLE), new ItemStack(Items.CARROT),
                new ItemStack(Items.HAY_BLOCK));
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        return List.of(new ItemStack(Items.APPLE), new ItemStack(Items.CARROT));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        return List.of(new ItemStack(Items.GOLDEN_APPLE));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.HORSTACHIO_CANDY.get(), 2 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.APPLE, 1 + random.nextInt(2))
        );
    }

    @Override public int getVariantCount() { return 3; }

    @Override
    protected void registerPinataGoals() {
        // Attracted to hay bales (stable-like)
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.HAY_BLOCK, 1.0, 20));
        goalSelector.addGoal(4, new EatItemEntityGoal(this,
                stack -> stack.is(Items.APPLE) || stack.is(Items.CARROT)
                        || stack.is(Items.HAY_BLOCK),
                1.1, 12.0, 10));
        goalSelector.addGoal(5, new TemptGoal(this, 1.2, stack ->
                stack.is(Items.APPLE) || stack.is(Items.GOLDEN_APPLE)
                || stack.is(Items.CARROT), false));
    }

    @Override
    protected void tickClientParticles() {
        super.tickClientParticles();
        // Galloping dust
        if (getDeltaMovement().horizontalDistanceSqr() > 0.02 && random.nextInt(3) == 0) {
            level().addParticle(net.minecraft.core.particles.ParticleTypes.POOF,
                    getX() + (random.nextDouble() - 0.5) * 0.5,
                    getY(),
                    getZ() + (random.nextDouble() - 0.5) * 0.5,
                    0, 0.02, 0);
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        HorstachioEntity baby = ModPinataEntities.HORSTACHIO.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
