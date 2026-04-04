package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * 5th-level — create a temporary 2x3 tunnel up to 15 blocks deep through
 * stone, deepslate, or wood. Blocks regenerate after 60 seconds.
 * Based on D&D 5e Passwall.
 */
public class PasswallScrollItem extends Item {

    private static final int TUNNEL_DEPTH = 15;
    private static final int TUNNEL_WIDTH = 2;
    private static final int TUNNEL_HEIGHT = 3;
    public static final int RESTORE_TICKS = 1200; // 60 seconds

    public PasswallScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ServerLevel serverLevel = (ServerLevel) level;

        // Tunnel goes into the wall — opposite of the clicked face
        Direction tunnelDir = context.getClickedFace().getOpposite();
        BlockPos start = context.getClickedPos();

        // Determine "right" and "up" directions for the tunnel cross-section
        Direction right;
        Direction up = Direction.UP;
        if (tunnelDir.getAxis() == Direction.Axis.Y) {
            // Tunneling up or down — use player facing for orientation
            up = player.getDirection();
            right = up.getClockWise();
        } else {
            right = tunnelDir.getClockWise();
        }

        // Carve the tunnel, storing original blocks
        List<BlockPos> positions = new ArrayList<>();
        List<BlockState> originalStates = new ArrayList<>();
        int carved = 0;

        for (int depth = 0; depth < TUNNEL_DEPTH; depth++) {
            boolean rowHadBlock = false;
            for (int w = 0; w < TUNNEL_WIDTH; w++) {
                for (int h = 0; h < TUNNEL_HEIGHT; h++) {
                    BlockPos pos = start
                            .relative(tunnelDir, depth)
                            .relative(right, w)
                            .relative(up, h);
                    BlockState state = level.getBlockState(pos);

                    if (isPassable(state)) {
                        positions.add(pos.immutable());
                        originalStates.add(state);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        carved++;
                        rowHadBlock = true;

                        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                3, 0.3, 0.3, 0.3, 0.1);
                    }
                }
            }
            // Stop if we've broken through to open air
            if (!rowHadBlock && depth > 0) break;
        }

        if (carved == 0) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.passwall_fail"), true);
            return InteractionResult.FAIL;
        }

        // Register for restoration
        PasswallData.get(serverLevel).addPasswall(positions, originalStates,
                level.getGameTime() + RESTORE_TICKS);

        // Effects
        level.playSound(null, start, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
                SoundSource.BLOCKS, 1.0F, 1.5F);
        serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                start.getX() + 0.5, start.getY() + 1.0, start.getZ() + 0.5,
                30, 0.5, 1.0, 0.5, 0.1);

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    private static boolean isPassable(BlockState state) {
        if (state.isAir()) return false;
        float hardness = state.getDestroySpeed(null, BlockPos.ZERO);
        if (hardness < 0) return false;    // bedrock, barriers
        if (hardness > 50.0F) return false; // obsidian
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.passwall_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
