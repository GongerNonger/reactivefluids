package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.AttractedToBlockGoal;
import com.reactivefluids.pinata.ai.EatItemEntityGoal;
import net.minecraft.core.particles.ParticleTypes;
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
 * Buzzlegum — bee piñata, produces honey. Attracted to flowers.
 *
 * VP model: Rotund striped body (yellow/black), small paper wings buzzing,
 * large friendly eyes, tiny stinger at back. Bubblegum-pink/yellow coloring.
 *
 * VP: Appear with 2 daisies + 2 buttercups. Resident: eat 1 daisy.
 * Romance: eat 1 daisy, have a Buzzlegum house.
 * Produces: Honey (at Honey Hive). Conflicts with: Raisant (ant).
 */
public class BuzzlegumEntity extends BasePinataEntity {

    private int honeyTimer;

    public BuzzlegumEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 4;
        this.honeyTimer = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    public String getPinataSpeciesName() { return "Buzzlegum"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(new ItemStack(Items.OXEYE_DAISY), new ItemStack(Items.DANDELION),
                new ItemStack(Items.SUNFLOWER));
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        return List.of(new ItemStack(Items.OXEYE_DAISY));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        return List.of(new ItemStack(Items.SUNFLOWER));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.BUZZLEGUM_CANDY.get(), 1 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.HONEYCOMB, random.nextInt(2))
        );
    }

    @Override public int getVariantCount() { return 2; }

    @Override
    protected void registerPinataGoals() {
        // Attracted to flowers (pollination behavior)
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.OXEYE_DAISY, 1.1, 20));
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.DANDELION, 1.1, 20));
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.SUNFLOWER, 1.1, 20));
        goalSelector.addGoal(4, new EatItemEntityGoal(this,
                stack -> stack.is(Items.OXEYE_DAISY) || stack.is(Items.DANDELION)
                        || stack.is(Items.SUNFLOWER),
                1.1, 10.0, 8));
        goalSelector.addGoal(5, new TemptGoal(this, 1.2, stack ->
                stack.is(Items.OXEYE_DAISY) || stack.is(Items.SUNFLOWER), false));
    }

    @Override
    public void tick() {
        super.tick();
        // Honey production timer — drops honey periodically when happy resident
        if (!level().isClientSide() && isResident() && getHappiness() > 60) {
            honeyTimer++;
            if (honeyTimer >= 6000) { // Every 5 minutes
                spawnAtLocation(new ItemStack(Items.HONEYCOMB, 1));
                honeyTimer = 0;
            }
        }
    }

    @Override
    protected void tickClientParticles() {
        super.tickClientParticles();
        // Pollen/honey particles
        if (random.nextInt(8) == 0) {
            double px = getX() + (random.nextDouble() - 0.5) * 0.4;
            double py = getY() + 0.4 + random.nextDouble() * 0.2;
            double pz = getZ() + (random.nextDouble() - 0.5) * 0.4;
            level().addParticle(ParticleTypes.FALLING_HONEY, px, py, pz, 0, 0, 0);
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        BuzzlegumEntity baby = ModPinataEntities.BUZZLEGUM.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
