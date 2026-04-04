package com.reactivefluids.pinata;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Ruffian — hostile humanoid NPC that invades gardens.
 *
 * VP behavior:
 * - Enters garden from edge, causes destruction
 * - Breaks fences, destroys decorations
 * - Can be bribed with chocolate coins (gold ingots as stand-in)
 * - Can be scared away by hitting with upgraded shovel
 * - Tamed Mallowolf howl scares them
 * - Professor Pester sends them
 *
 * MC implementation:
 * - Hostile mob (like Illager/Pillager style)
 * - Targets and breaks fence blocks
 * - Attacks piñatas, reducing happiness
 * - Bribed by right-clicking with gold ingot/chocolate coin
 * - Hit with shovel to scare away (flee behavior)
 */
public class RuffianEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> DATA_FLEEING =
            SynchedEntityData.defineId(RuffianEntity.class, EntityDataSerializers.BOOLEAN);

    private int destructionCooldown;
    private int lifetimeTicks;
    private static final int MAX_LIFETIME = 6000; // 5 minutes then leaves

    public RuffianEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.lifetimeTicks = MAX_LIFETIME;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.ARMOR, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLEEING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        // Target piñatas
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this,
                BasePinataEntity.class, true,
                e -> e instanceof BasePinataEntity p && p.isResident()));
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            lifetimeTicks--;
            if (lifetimeTicks <= 0 || entityData.get(DATA_FLEEING)) {
                if (lifetimeTicks <= -200) { // Give flee time
                    discard();
                    return;
                }
            }

            // Periodically break nearby fences
            if (destructionCooldown > 0) {
                destructionCooldown--;
            } else if (random.nextInt(40) == 0) {
                tryBreakNearbyFence();
            }

            // Scare particles
            if (random.nextInt(20) == 0 && level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                        getX(), getY() + getBbHeight(), getZ(),
                        1, 0.2, 0.1, 0.2, 0);
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Bribe with gold ingots (chocolate coin stand-in)
        if (stack.is(Items.GOLD_INGOT) || (stack.getItem() instanceof ChocolateCoinItem)) {
            if (!level().isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                // Ruffian takes bribe and leaves
                entityData.set(DATA_FLEEING, true);
                lifetimeTicks = -1; // Start flee timer
                playSound(SoundEvents.VILLAGER_YES, 1.0F, 0.7F);
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            getX(), getY() + 1.5, getZ(),
                            5, 0.3, 0.2, 0.3, 0.02);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);

        // Hit with shovel → flee
        if (result && source.getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            if (weapon.getItem() instanceof GardenShovelItem) {
                entityData.set(DATA_FLEEING, true);
                lifetimeTicks = -1;
                playSound(SoundEvents.VILLAGER_NO, 1.0F, 0.6F);
            }
        }

        return result;
    }

    private void tryBreakNearbyFence() {
        BlockPos pos = blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {
            BlockState state = level().getBlockState(p);
            if (state.getBlock() instanceof FenceBlock) {
                if (level() instanceof ServerLevel sl) {
                    sl.destroyBlock(p, true);
                    playSound(SoundEvents.WOOD_BREAK, 1.0F, 0.8F);
                    sl.sendParticles(ParticleTypes.SMOKE,
                            p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                            5, 0.3, 0.3, 0.3, 0.02);
                }
                destructionCooldown = 60;
                return;
            }
        }
    }

    @Override
    public boolean isPersistenceRequired() { return true; }
    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        // Drop some chocolate coins when defeated
        spawnAtLocation(new ItemStack(ModPinataItems.CHOCOLATE_COIN.get(), 3 + random.nextInt(5)));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Lifetime", lifetimeTicks);
        tag.putBoolean("Fleeing", entityData.get(DATA_FLEEING));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lifetimeTicks = tag.getInt("Lifetime");
        entityData.set(DATA_FLEEING, tag.getBoolean("Fleeing"));
    }
}
