package com.reactivefluids.pinata;

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

import javax.annotation.Nullable;
import java.util.List;

/**
 * Cluckles — chicken piñata. Common, eats seeds, produces eggs.
 *
 * VP model: Round plump body, small wings, red comb on head,
 * fan-shaped tail feathers. Chocolate-chip cookie coloring
 * (light brown with darker spots).
 *
 * VP: Appear with 2 sq pinometers grass. Resident: eat 1 wheat.
 * Romance: eat 1 corn. Evolves into Chocstrich (feed cactus fruit).
 */
public class ClucklesEntity extends BasePinataEntity {

    private int eggTimer;

    public ClucklesEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.baseCandyCount = 3;
        this.eggTimer = random.nextInt(6000) + 6000;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    public String getPinataSpeciesName() { return "Cluckles"; }

    @Override
    public List<ItemStack> getVisitFoods() {
        return List.of(new ItemStack(Items.WHEAT_SEEDS), new ItemStack(Items.WHEAT));
    }

    @Override
    public List<ItemStack> getResidentFoods() {
        return List.of(new ItemStack(Items.WHEAT));
    }

    @Override
    public List<ItemStack> getRomanceFoods() {
        // Corn equivalent
        return List.of(new ItemStack(Items.WHEAT));
    }

    @Override
    public List<ItemStack> getCandyDrops() {
        return List.of(
                new ItemStack(ModPinataItems.CLUCKLES_CANDY.get(), 1 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.EGG, random.nextInt(2))
        );
    }

    @Override public int getVariantCount() { return 2; }

    @Override
    protected void registerPinataGoals() {
        goalSelector.addGoal(3, new EatItemEntityGoal(this,
                stack -> stack.is(Items.WHEAT_SEEDS) || stack.is(Items.WHEAT),
                1.0, 10.0, 6));
        goalSelector.addGoal(4, new TemptGoal(this, 1.1, stack ->
                stack.is(Items.WHEAT_SEEDS) || stack.is(Items.WHEAT), false));
    }

    @Override
    public void tick() {
        super.tick();
        // Egg laying — like vanilla chicken
        if (!level().isClientSide() && isResident() && getHappiness() > 40) {
            eggTimer--;
            if (eggTimer <= 0) {
                spawnAtLocation(Items.EGG);
                playSound(net.minecraft.sounds.SoundEvents.CHICKEN_EGG,
                        1.0F, 1.0F + random.nextFloat() * 0.2F);
                eggTimer = random.nextInt(6000) + 6000; // 5-10 min
            }
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        ClucklesEntity baby = ModPinataEntities.CLUCKLES.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
