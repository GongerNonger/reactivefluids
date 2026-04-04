package com.reactivefluids.pinata;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Dragonache — dragon piñata. The rarest and most powerful piñata.
 * VP model: Full dragon body, wings, horns, long neck and tail, fire breath.
 * Must hatch from a special egg found by Diggerling in the Mine.
 * Scares Professor Pester away when present in garden.
 *
 * MC: Very rare, extremely high stats, fire particles, large model.
 */
public class DragonacheEntity extends BasePinataEntity {
    public DragonacheEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 10; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 8.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override public String getPinataSpeciesName() { return "Dragonache"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.MAGMA_CREAM)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.BLAZE_ROD)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.DRAGON_BREATH)); }
    @Override public List<ItemStack> getCandyDrops() {
        return List.of(new ItemStack(ModPinataItems.DRAGONACHE_CANDY.get(), 3 + random.nextInt(baseCandyCount)),
                new ItemStack(Items.DIAMOND, 1 + random.nextInt(3)),
                new ItemStack(Items.BLAZE_ROD, 1 + random.nextInt(2)));
    }
    @Override public int getVariantCount() { return 3; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(5, new TemptGoal(this, 1.0, s -> s.is(Items.BLAZE_ROD) || s.is(Items.MAGMA_CREAM), false));
    }

    @Override public boolean fireImmune() { return true; }

    @Override protected void tickClientParticles() {
        super.tickClientParticles();
        // Fire breath particles
        if (random.nextInt(5) == 0) {
            double dx = -Math.sin(Math.toRadians(getYRot())) * 0.5;
            double dz = Math.cos(Math.toRadians(getYRot())) * 0.5;
            level().addParticle(ParticleTypes.FLAME,
                    getX() + dx, getY() + getBbHeight() * 0.8, getZ() + dz,
                    dx * 0.05, 0.02, dz * 0.05);
        }
        // Wing sparkle
        if (random.nextInt(8) == 0) {
            level().addParticle(ParticleTypes.END_ROD,
                    getX() + (random.nextDouble() - 0.5) * getBbWidth() * 2,
                    getY() + getBbHeight() * 0.5,
                    getZ() + (random.nextDouble() - 0.5) * getBbWidth() * 2,
                    0, 0.02, 0);
        }
    }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        DragonacheEntity baby = ModPinataEntities.DRAGONACHE.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
