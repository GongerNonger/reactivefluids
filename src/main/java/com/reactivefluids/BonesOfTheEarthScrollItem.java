package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * 6th-level — Bones of the Earth. Erupts a stone column under the caster,
 * launching them skyward and damaging nearby enemies.
 */
public class BonesOfTheEarthScrollItem extends Item {

    private static final int PILLAR_HEIGHT = 14;
    private static final float LAUNCH_VELOCITY = 1.6F;
    private static final float DAMAGE = 12.0F;
    private static final float DAMAGE_RADIUS = 4.0F;

    private static final BlockState[] PILLAR_BLOCKS = {
            Blocks.STONE.defaultBlockState(),
            Blocks.ANDESITE.defaultBlockState(),
            Blocks.DIORITE.defaultBlockState(),
            Blocks.DEEPSLATE.defaultBlockState(),
            Blocks.COBBLESTONE.defaultBlockState(),
    };

    public BonesOfTheEarthScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos playerPos = player.blockPosition();

        // Find ground directly under the player
        BlockPos basePos = findGround(level, playerPos);
        if (basePos == null) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.bones_blocked"), true);
            return InteractionResult.FAIL;
        }

        // Build a 3x3 stone column from the ground up
        for (int h = 0; h < PILLAR_HEIGHT; h++) {
            for (int px = -1; px <= 1; px++) {
                for (int pz = -1; pz <= 1; pz++) {
                    BlockPos pos = basePos.offset(px, h, pz);
                    BlockState existing = level.getBlockState(pos);
                    if (existing.isAir() || existing.canBeReplaced()) {
                        BlockState stone = PILLAR_BLOCKS[level.random.nextInt(PILLAR_BLOCKS.length)];
                        level.setBlock(pos, stone, Block.UPDATE_ALL);
                    }
                }
            }
        }

        // Teleport caster to top of pillar and launch upward
        double topY = basePos.getY() + PILLAR_HEIGHT;
        player.teleportTo(basePos.getX() + 0.5, topY, basePos.getZ() + 0.5);
        player.setDeltaMovement(player.getDeltaMovement().add(0, LAUNCH_VELOCITY, 0));
        player.hurtMarked = true;
        player.fallDistance = 0; // reset so the pillar ride doesn't count

        // Damage nearby enemies (not the caster)
        AABB damageArea = new AABB(
                playerPos.getX() - DAMAGE_RADIUS, playerPos.getY() - 1, playerPos.getZ() - DAMAGE_RADIUS,
                playerPos.getX() + DAMAGE_RADIUS + 1, topY + 2, playerPos.getZ() + DAMAGE_RADIUS + 1);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, damageArea,
                e -> e != player && e.isAlive());
        for (LivingEntity entity : nearby) {
            entity.hurt(level.damageSources().magic(), DAMAGE);
            // Knock them away from the pillar
            double dx = entity.getX() - (basePos.getX() + 0.5);
            double dz = entity.getZ() - (basePos.getZ() + 0.5);
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.01) {
                entity.setDeltaMovement(
                        dx / dist * 0.8,
                        0.6,
                        dz / dist * 0.8);
                entity.hurtMarked = true;
            }
        }

        // Particles — dust burst at base and eruption along the column
        serverLevel.sendParticles(ParticleTypes.CLOUD,
                basePos.getX() + 0.5, basePos.getY() + 0.5, basePos.getZ() + 0.5,
                40, 2.0, 0.5, 2.0, 0.1);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                basePos.getX() + 0.5, basePos.getY() + PILLAR_HEIGHT / 2.0, basePos.getZ() + 0.5,
                8, 0.8, PILLAR_HEIGHT / 3.0, 0.8, 0.0);

        // Sounds
        level.playSound(null, playerPos, SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.BLOCKS, 1.5F, 0.5F);
        level.playSound(null, playerPos, SoundEvents.STONE_PLACE,
                SoundSource.BLOCKS, 2.0F, 0.4F);
        level.playSound(null, playerPos, SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.0F, 0.8F);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    /**
     * Find the topmost solid block at the player's xz, searching downward from their feet.
     */
    private BlockPos findGround(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        for (int y = pos.getY(); y > pos.getY() - 20; y--) {
            mutable.setY(y);
            if (!level.getBlockState(mutable).isAir()) {
                mutable.setY(y + 1);
                return mutable.immutable();
            }
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.bones_of_the_earth_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
