package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

/**
 * 3rd-level — erupts the earth in a violent 5x5 explosion.
 * Launches blocks upward as FallingBlockEntities and damages nearby entities.
 * Based on D&D 5e Erupting Earth.
 */
public class EruptingEarthScrollItem extends Item {

    private static final Set<Block> ERUPTABLE_BLOCKS = Set.of(
            Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.COBBLESTONE,
            Blocks.DEEPSLATE, Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL,
            Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.TUFF
    );

    private static final float DAMAGE = 8.0F;

    public EruptingEarthScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos center = context.getClickedPos();

        int erupted = 0;

        // Erupt blocks in a 5x5 area (2-block radius from center)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos pos = center.offset(dx, 0, dz);
                BlockState state = level.getBlockState(pos);

                if (ERUPTABLE_BLOCKS.contains(state.getBlock())) {
                    // Remove the block
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

                    // Spawn a FallingBlockEntity launched upward
                    FallingBlockEntity fallingBlock = FallingBlockEntity.fall(
                            serverLevel, pos, state);
                    double vx = (serverLevel.getRandom().nextDouble() - 0.5) * 0.6;
                    double vy = 0.6 + serverLevel.getRandom().nextDouble() * 0.5;
                    double vz = (serverLevel.getRandom().nextDouble() - 0.5) * 0.6;
                    fallingBlock.setDeltaMovement(vx, vy, vz);
                    fallingBlock.hurtMarked = true;

                    // Explosion particles per block
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            2, 0.3, 0.3, 0.3, 0.0);

                    erupted++;
                }
            }
        }

        // Damage and launch all living entities in the 5x5 area
        AABB damageArea = new AABB(
                center.getX() - 2.5, center.getY() - 1, center.getZ() - 2.5,
                center.getX() + 3.5, center.getY() + 3, center.getZ() + 3.5);

        for (Entity entity : level.getEntities(null, damageArea)) {
            if (entity instanceof LivingEntity living) {
                living.hurt(level.damageSources().magic(), DAMAGE);
                Vec3 motion = living.getDeltaMovement();
                living.setDeltaMovement(motion.x, 0.8, motion.z);
                living.hurtMarked = true;
            }
        }

        if (erupted == 0) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.erupting_earth_fail"), true);
            return InteractionResult.FAIL;
        }

        // Central explosion particles
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
                3, 1.0, 0.5, 1.0, 0.0);

        level.playSound(null, center, SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.BLOCKS, 2.0F, 0.8F);

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.erupting_earth_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
