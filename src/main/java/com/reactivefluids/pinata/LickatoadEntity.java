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
 * Lickatoad — frog piñata, eats Tafflies. Semi-aquatic.
 *
 * VP model: Squat wide body, long tongue, large eyes on top of head,
 * strong back legs for hopping. Lollipop-green coloring with spots.
 *
 * VP: Appear with 1 Taffly resident. Resident: eat 1 Taffly.
 * Romance: eat 2 Tafflies.
 * Evolves into Lackatoad (feed nightshade, hit with shovel).
 * Conflicts with: Newtgat.
 */
public class LickatoadEntity extends BasePinataEntity {

    private int hopCooldown;

    public LickatoadEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 4;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.JUMP_STRENGTH, 0.7);
    }

    @Override
    public String getPinataSpeciesName() { return "Lickatoad"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(new ItemStack(Items.SLIME_BALL), new ItemStack(Items.LILY_PAD));
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        // Must eat Tafflies to become resident (via HuntPreyGoal)
        return List.of(new ItemStack(Items.SLIME_BALL));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        return List.of(new ItemStack(Items.LILY_PAD));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.LICKATOAD_CANDY.get(), 1 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.SLIME_BALL, random.nextInt(2))
        );
    }

    @Override public int getVariantCount() { return 2; }

    @Override
    protected void registerPinataGoals() {
        // Hunt Tafflies — tongue-snatch (uses generic hunt goal)
        goalSelector.addGoal(2, new HuntPreyGoal(this,
                () -> ModPinataEntities.TAFFLY.get(), 1.3, 10.0));
        // Attracted to water
        goalSelector.addGoal(3, new AttractedToBlockGoal(this,
                () -> Blocks.WATER, 0.9, 16));
        goalSelector.addGoal(5, new TemptGoal(this, 1.0, stack ->
                stack.is(Items.SLIME_BALL) || stack.is(Items.LILY_PAD), false));
    }

    @Override
    public void tick() {
        super.tick();
        // Periodic hopping movement
        if (!level().isClientSide() && onGround() && hopCooldown <= 0
                && getDeltaMovement().horizontalDistanceSqr() > 0.001) {
            if (random.nextInt(10) == 0) {
                setDeltaMovement(getDeltaMovement().add(0, 0.35, 0));
                hopCooldown = 10;
            }
        }
        if (hopCooldown > 0) hopCooldown--;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        LickatoadEntity baby = ModPinataEntities.LICKATOAD.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
