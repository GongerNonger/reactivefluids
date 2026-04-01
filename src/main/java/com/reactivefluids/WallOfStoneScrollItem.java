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

import java.util.List;

/**
 * 5th-level — conjure a permanent 10-wide, 5-tall stone brick wall
 * perpendicular to the caster's facing direction.
 * Based on D&D 5e Wall of Stone.
 */
public class WallOfStoneScrollItem extends Item {

    private static final int WALL_WIDTH = 10;
    private static final int WALL_HEIGHT = 5;

    public WallOfStoneScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos base = context.getClickedPos().above();

        // Wall extends perpendicular to player facing
        Direction facing = player.getDirection();
        Direction wallDir = facing.getClockWise(); // perpendicular

        // Center the wall
        int halfWidth = WALL_WIDTH / 2;
        BlockPos startPos = base.relative(wallDir, -halfWidth);

        int placed = 0;
        for (int w = 0; w < WALL_WIDTH; w++) {
            for (int h = 0; h < WALL_HEIGHT; h++) {
                BlockPos pos = startPos.relative(wallDir, w).above(h);
                BlockState current = level.getBlockState(pos);
                if (current.isAir() || current.canBeReplaced()) {
                    level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), Block.UPDATE_ALL);
                    placed++;

                    // Particle on each block
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            3, 0.3, 0.3, 0.3, 0.02);
                }
            }
        }

        if (placed == 0) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.wall_blocked"), true);
            return InteractionResult.FAIL;
        }

        level.playSound(null, base, SoundEvents.STONE_PLACE,
                SoundSource.BLOCKS, 2.0F, 0.6F);
        level.playSound(null, base, SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.0F, 0.8F);

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.wall_of_stone_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
