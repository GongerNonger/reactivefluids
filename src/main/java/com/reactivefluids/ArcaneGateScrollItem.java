package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 6th-level — two-use item that creates linked portals.
 * First use places entry marker, second use places exit and activates both.
 * Entities stepping into one portal are teleported to the other.
 * Based on D&D 5e Arcane Gate.
 */
public class ArcaneGateScrollItem extends Item {

    // No distance limit — portals can link any two points in the same dimension
    public static final int DURATION_TICKS = 2400; // 2 minutes

    /** Stores the first portal position per player UUID. */
    private static final Map<UUID, BlockPos> PENDING_GATES = new HashMap<>();

    public ArcaneGateScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos clickedPos = context.getClickedPos().above(); // place portal above the clicked block
        UUID playerId = player.getUUID();

        if (!PENDING_GATES.containsKey(playerId)) {
            // First use — place entry marker
            PENDING_GATES.put(playerId, clickedPos.immutable());

            level.playSound(null, clickedPos, SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS, 1.0F, 1.2F);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    clickedPos.getX() + 0.5, clickedPos.getY() + 0.5, clickedPos.getZ() + 0.5,
                    30, 0.3, 0.5, 0.3, 0.05);

            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.arcane_gate_entry"), true);

            return InteractionResult.SUCCESS;
        } else {
            // Second use — place exit and activate both portals
            BlockPos entryPos = PENDING_GATES.remove(playerId);
            BlockPos exitPos = clickedPos.immutable();

            // Place 2-tall purple stained glass at both positions
            placePortalBlocks(serverLevel, entryPos);
            placePortalBlocks(serverLevel, exitPos);

            // Register with SavedData for tick-based teleportation and expiry
            ArcaneGateData.get(serverLevel).addGate(
                    entryPos, exitPos, level.getGameTime() + DURATION_TICKS);

            // Effects at both portals
            for (BlockPos pos : new BlockPos[]{entryPos, exitPos}) {
                level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS, 1.5F, 0.8F);
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        50, 0.3, 0.8, 0.3, 0.1);
            }

            // Consume scroll
            if (!player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.CONSUME;
        }
    }

    private static void placePortalBlocks(ServerLevel level, BlockPos base) {
        level.setBlock(base, Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(base.above(), Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), Block.UPDATE_ALL);
    }

    /** Called from ArcaneGateData to clear pending entries if needed. */
    public static void clearPending(UUID playerId) {
        PENDING_GATES.remove(playerId);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.arcane_gate_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
