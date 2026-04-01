package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Consumable spell scroll that conjures Galder's Tower —
 * a temporary two-story stone tower with furnished rooms.
 */
public class TowerScrollItem extends Item {

    public TowerScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // Only place on top of a block
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        Direction doorFacing = player.getDirection().getOpposite();

        // Center the tower so the door wall aligns with the clicked block
        BlockPos center = context.getClickedPos().relative(doorFacing.getOpposite(), 3);

        if (!GaldersTower.canPlace(serverLevel, center, doorFacing)) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.tower_blocked"), true);
            return InteractionResult.FAIL;
        }

        // Build with random room types
        RandomSource random = level.getRandom();
        GaldersTower.RoomType room1 = GaldersTower.RoomType.random(random);
        GaldersTower.RoomType room2 = GaldersTower.RoomType.random(random);
        List<BlockPos> blocks = GaldersTower.build(serverLevel, center, doorFacing, room1, room2);

        // Track for expiration
        long expiryTick = level.getGameTime() + GaldersTower.DURATION_TICKS;
        ConjuredTowerData.get(serverLevel).addTower(blocks, expiryTick);

        // Consume scroll
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.tower_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
