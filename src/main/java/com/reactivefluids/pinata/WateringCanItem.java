package com.reactivefluids.pinata;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Watering Can — waters plants and piñatas.
 *
 * Right-click on blocks:
 * - Crops/saplings → advances growth stage (like bone meal)
 * - Farmland → hydrates it
 * - Fire → extinguishes
 *
 * Right-click on piñatas:
 * - Calms angry piñatas (restores happiness)
 * - Extinguishes burning piñatas (Taffly→Reddhott evolution step)
 * - General happiness boost for water-loving species
 *
 * In VP, the watering can has a water capacity that must be refilled.
 * Simplified here: infinite use, just a versatile tool.
 */
public class WateringCanItem extends Item {

    public WateringCanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (level.isClientSide()) return InteractionResult.sidedSuccess(true);

        ServerLevel serverLevel = (ServerLevel) level;

        // Water crops — advance growth (like bone meal)
        if (state.getBlock() instanceof CropBlock crop) {
            if (!crop.isMaxAge(state)) {
                int age = crop.getAge(state);
                level.setBlock(pos, crop.getStateForAge(Math.min(age + 1, crop.getMaxAge())), 3);
                spawnWaterParticles(serverLevel, pos);
                playWaterSound(level, pos);
                return InteractionResult.SUCCESS;
            }
        }

        // Water saplings — attempt growth tick
        if (state.getBlock() instanceof SaplingBlock sapling) {
            sapling.advanceTree(serverLevel, pos, state, serverLevel.random);
            spawnWaterParticles(serverLevel, pos);
            playWaterSound(level, pos);
            return InteractionResult.SUCCESS;
        }

        // Hydrate farmland
        if (state.is(Blocks.FARMLAND)) {
            level.setBlock(pos, Blocks.FARMLAND.defaultBlockState()
                    .setValue(FarmBlock.MOISTURE, 7), 3);
            spawnWaterParticles(serverLevel, pos);
            playWaterSound(level, pos);
            return InteractionResult.SUCCESS;
        }

        // Extinguish fire
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS, 0.8F, 1.2F);
            spawnWaterParticles(serverLevel, pos);
            return InteractionResult.SUCCESS;
        }

        // Water area — splash effect that affects nearby piñatas
        waterNearbyPinatas(serverLevel, pos);
        spawnWaterParticles(serverLevel, pos);
        playWaterSound(level, pos);

        return InteractionResult.SUCCESS;
    }

    private void waterNearbyPinatas(ServerLevel level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(3);
        List<BasePinataEntity> pinatas = level.getEntitiesOfClass(BasePinataEntity.class, area);
        for (BasePinataEntity pinata : pinatas) {
            // Calm angry piñatas
            pinata.addHappiness(3);

            // Extinguish burning piñatas (evolution trigger for Taffly→Reddhott)
            if (pinata.isOnFire()) {
                pinata.clearFire();
            }

            // Water particles on the piñata
            level.sendParticles(ParticleTypes.SPLASH,
                    pinata.getX(), pinata.getY() + pinata.getBbHeight(),
                    pinata.getZ(), 5, 0.2, 0.1, 0.2, 0.05);
        }
    }

    private void spawnWaterParticles(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.SPLASH,
                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                10, 0.4, 0.1, 0.4, 0.05);
        level.sendParticles(ParticleTypes.FALLING_WATER,
                pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                5, 0.3, 0.0, 0.3, 0);
    }

    private void playWaterSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY,
                SoundSource.PLAYERS, 0.8F, 1.0F + level.getRandom().nextFloat() * 0.2F);
    }
}
