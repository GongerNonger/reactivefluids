package com.reactivefluids.pinata;

import com.reactivefluids.pinata.ai.HuntPreyGoal;
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
 * Mallowolf — wolf piñata. Pack predator, has sour variant.
 * VP model: Lean wolf body, sharp ears, bushy tail, fangs. Marshmallow-white with gray/blue accents.
 * Sour Mallowolf prevents visitors; tamed by feeding Pigxie.
 * Tamed Mallowolf howl scares Ruffians.
 */
public class MallowolfEntity extends BasePinataEntity {
    private int howlCooldown;

    public MallowolfEntity(EntityType<? extends Animal> type, Level level) { super(type, level); baseCandyCount = 5; }

    public static AttributeSupplier.Builder createAttributes() {
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33).add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override public String getPinataSpeciesName() { return "Mallowolf"; }
    @Override public List<ItemStack> getVisitFoods() { return List.of(new ItemStack(Items.BONE), new ItemStack(Items.MUTTON)); }
    @Override public List<ItemStack> getResidentFoods() { return List.of(new ItemStack(Items.MUTTON)); }
    @Override public List<ItemStack> getRomanceFoods() { return List.of(new ItemStack(Items.COOKED_MUTTON)); }
    @Override public List<ItemStack> getCandyDrops() { return List.of(new ItemStack(ModPinataItems.MALLOWOLF_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }
    @Override public int getVariantCount() { return 2; }

    @Override protected void registerPinataGoals() {
        goalSelector.addGoal(2, new HuntPreyGoal(this, () -> ModPinataEntities.BUNNYCOMB.get(), 1.4, 16.0));
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is(Items.BONE) || s.is(Items.MUTTON), false));
    }

    @Override public void tick() {
        super.tick();
        if (!level().isClientSide() && isResident() && howlCooldown <= 0 && random.nextInt(2000) == 0) {
            // Howl — scares nearby Ruffians
            playSound(net.minecraft.sounds.SoundEvents.WOLF_HOWL, 1.5F, 0.7F);
            howlCooldown = 1200;
            // Scare nearby ruffians by dealing minor damage (triggers their flee)
            var ruffians = level().getEntitiesOfClass(RuffianEntity.class, getBoundingBox().inflate(20));
            for (RuffianEntity r : ruffians) {
                r.hurt(damageSources().mobAttack(this), 0.1F);
            }
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.NOTE, getX(), getY() + getBbHeight() + 0.5, getZ(), 5, 0.3, 0.2, 0.3, 0);
            }
        }
        if (howlCooldown > 0) howlCooldown--;
    }

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        MallowolfEntity baby = ModPinataEntities.MALLOWOLF.get().create(level);
        if (baby != null) { baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }
        return baby;
    }
}
