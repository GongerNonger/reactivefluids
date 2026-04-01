package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import java.util.Set;

/**
 * 6th-level — Move Earth. Raises or lowers a 7x7 area of soft terrain by 3 blocks.
 * Sneak to lower, normal use to raise.
 */
public class MoveEarthScrollItem extends Item {

    private static final int AREA_SIZE = 7;
    private static final int SHIFT_AMOUNT = 3;

    private static final Set<Block> SOFT_BLOCKS = Set.of(
            Blocks.DIRT,
            Blocks.GRASS_BLOCK,
            Blocks.COARSE_DIRT,
            Blocks.PODZOL,
            Blocks.MYCELIUM,
            Blocks.SAND,
            Blocks.RED_SAND,
            Blocks.GRAVEL,
            Blocks.CLAY,
            Blocks.MUD,
            Blocks.SNOW_BLOCK,
            Blocks.MOSS_BLOCK,
            Blocks.FARMLAND,
            Blocks.DIRT_PATH,
            Blocks.SOUL_SAND,
            Blocks.SOUL_SOIL
    );

    public MoveEarthScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos clickedPos = context.getClickedPos();
        boolean lower = player.isShiftKeyDown();

        int halfSize = AREA_SIZE / 2;
        int affected = 0;

        for (int dx = -halfSize; dx <= halfSize; dx++) {
            for (int dz = -halfSize; dz <= halfSize; dz++) {
                int x = clickedPos.getX() + dx;
                int z = clickedPos.getZ() + dz;

                if (lower) {
                    affected += lowerColumn(level, serverLevel, x, clickedPos.getY(), z);
                } else {
                    affected += raiseColumn(level, serverLevel, x, clickedPos.getY(), z);
                }
            }
        }

        if (affected == 0) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.move_earth_fail"), true);
            return InteractionResult.FAIL;
        }

        level.playSound(null, clickedPos, SoundEvents.ROOTED_DIRT_BREAK,
                SoundSource.BLOCKS, 2.0F, 0.6F);

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    /**
     * Raise: move soft blocks up by SHIFT_AMOUNT (process from top to bottom to avoid overwriting).
     * Fill the gap below with dirt.
     */
    private int raiseColumn(Level level, ServerLevel serverLevel, int x, int baseY, int z) {
        // Scan column to find soft blocks (search a reasonable range around baseY)
        int scanMin = baseY - 10;
        int scanMax = baseY + 10;
        int affected = 0;

        // Process from top to bottom to avoid overwriting
        for (int y = scanMax; y >= scanMin; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (isSoft(state)) {
                BlockPos newPos = new BlockPos(x, y + SHIFT_AMOUNT, z);
                // Only move if target is air or replaceable
                if (level.getBlockState(newPos).isAir() || level.getBlockState(newPos).canBeReplaced()) {
                    // Send block break particles at old position
                    serverLevel.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, state),
                            x + 0.5, y + 0.5, z + 0.5,
                            5, 0.3, 0.3, 0.3, 0.05);

                    level.setBlock(newPos, state, Block.UPDATE_ALL);
                    // Fill original position with dirt
                    level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                    affected++;
                }
            }
        }

        return affected;
    }

    /**
     * Lower: move soft blocks down by SHIFT_AMOUNT (process from bottom to top).
     * Clear the top positions to air.
     */
    private int lowerColumn(Level level, ServerLevel serverLevel, int x, int baseY, int z) {
        int scanMin = baseY - 10;
        int scanMax = baseY + 10;
        int affected = 0;

        // Process from bottom to top to avoid overwriting
        for (int y = scanMin; y <= scanMax; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (isSoft(state)) {
                BlockPos newPos = new BlockPos(x, y - SHIFT_AMOUNT, z);
                // Send block break particles at old position
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        x + 0.5, y + 0.5, z + 0.5,
                        5, 0.3, 0.3, 0.3, 0.05);

                level.setBlock(newPos, state, Block.UPDATE_ALL);
                // Clear the old position to air
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                affected++;
            }
        }

        return affected;
    }

    private static boolean isSoft(BlockState state) {
        return SOFT_BLOCKS.contains(state.getBlock());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.move_earth_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
