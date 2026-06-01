package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Tropical Slime — a passive/neutral slime variant inspired by Minecraft Earth.
 * Bounces and splits like a vanilla slime but does not attack players.
 * Drops slimeballs and tropical fish.
 */
public class TropicalSlimeEntity extends Slime {

    public TropicalSlimeEntity(EntityType<? extends TropicalSlimeEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    // ---- Passive behavior: never deal damage to players ----

    @Override
    public void playerTouch(Player player) {
        // Do nothing — tropical slimes don't hurt players
    }

    @Override
    protected void dealDamage(LivingEntity target) {
        // No-op: tropical slimes are passive
    }

    @Override
    protected boolean isDealsDamage() {
        return false;
    }

    // ---- Prevent hostile despawn in peaceful ----

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    // ---- Splitting: produce tropical slimes, not vanilla slimes ----

    @Override
    public void remove(RemovalReason reason) {
        int size = this.getSize();
        if (!this.level().isClientSide && size > 1 && this.isDeadOrDying()) {
            int count = 2 + this.random.nextInt(3);
            for (int i = 0; i < count; i++) {
                float offsetX = ((float) (i % 2) - 0.5F) * (float) size / 4.0F;
                float offsetZ = ((float) (i / 2) - 0.5F) * (float) size / 4.0F;
                TropicalSlimeEntity baby = ModEntities.TROPICAL_SLIME.get().create(this.level());
                if (baby != null) {
                    if (this.isPersistenceRequired()) {
                        baby.setPersistenceRequired();
                    }
                    baby.setSize(size / 2, true);
                    baby.moveTo(this.getX() + (double) offsetX, this.getY() + 0.5, this.getZ() + (double) offsetZ, this.random.nextFloat() * 360.0F, 0.0F);
                    this.level().addFreshEntity(baby);
                }
            }
        }
        // Call Entity.remove directly, skipping Slime's default splitting
        // We replicate the needed super behavior by calling the grandparent
        super.remove(reason);
    }

    // ---- Drops: slimeballs + tropical fish ----

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int size = this.getSize();
        // Small slimes drop items
        if (size == 1) {
            int slimeballCount = this.random.nextInt(2) + 1;
            for (int i = 0; i < slimeballCount; i++) {
                this.spawnAtLocation(new ItemStack(Items.SLIME_BALL));
            }
            if (this.random.nextFloat() < 0.5F) {
                this.spawnAtLocation(new ItemStack(Items.TROPICAL_FISH));
            }
        }
    }

    // ---- Sounds ----

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.isTiny() ? SoundEvents.SLIME_HURT_SMALL : SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isTiny() ? SoundEvents.SLIME_DEATH_SMALL : SoundEvents.SLIME_DEATH;
    }

    @Override
    protected SoundEvent getSquishSound() {
        return this.isTiny() ? SoundEvents.SLIME_SQUISH_SMALL : SoundEvents.SLIME_SQUISH;
    }

    @Override
    protected SoundEvent getJumpSound() {
        return this.isTiny() ? SoundEvents.SLIME_JUMP_SMALL : SoundEvents.SLIME_JUMP;
    }
}
