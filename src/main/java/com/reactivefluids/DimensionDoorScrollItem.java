package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 4th-level — teleport up to 64 blocks in look direction, passing through walls.
 * Based on D&D 5e Dimension Door.
 */
public class DimensionDoorScrollItem extends Item {

    private static final double MAX_RANGE = 64.0;

    public DimensionDoorScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);

        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();

        // Walk along the ray from far end back toward player to find safe landing
        BlockPos destination = null;
        for (int dist = (int) MAX_RANGE; dist >= 4; dist--) {
            Vec3 target = eyePos.add(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist);
            BlockPos feet = BlockPos.containing(target);
            BlockPos head = feet.above();
            BlockPos below = feet.below();

            BlockState feetState = level.getBlockState(feet);
            BlockState headState = level.getBlockState(head);
            BlockState belowState = level.getBlockState(below);

            // Need: solid ground below, air at feet and head
            if (!belowState.isAir() && belowState.blocksMotion()
                    && !feetState.blocksMotion() && !headState.blocksMotion()) {
                destination = feet;
                break;
            }
        }

        if (destination == null) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.dimension_door_fail"), true);
            return InteractionResultHolder.fail(stack);
        }

        // Departure particles
        serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1.0, player.getZ(),
                40, 0.4, 0.8, 0.4, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 1.0F);

        // Teleport
        player.teleportTo(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5);

        // Arrival particles
        serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                destination.getX() + 0.5, destination.getY() + 1.0, destination.getZ() + 0.5,
                40, 0.4, 0.8, 0.4, 0.05);
        level.playSound(null, destination, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 1.2F);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.dimension_door_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
