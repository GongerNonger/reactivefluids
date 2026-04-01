package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
 * Cantrip — instantly excavate a 3x3x3 cube of soft earth blocks.
 * Based on D&D 5e Mold Earth.
 */
public class MoldEarthScrollItem extends Item {

    private static final Set<Block> SOFT_BLOCKS = Set.of(
            Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT,
            Blocks.DIRT_PATH, Blocks.FARMLAND, Blocks.PODZOL, Blocks.MYCELIUM,
            Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.CLAY,
            Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS,
            Blocks.SOUL_SAND, Blocks.SOUL_SOIL,
            Blocks.SNOW_BLOCK, Blocks.MOSS_BLOCK
    );

    public MoldEarthScrollItem(Properties properties) {
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
        int excavated = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (SOFT_BLOCKS.contains(state.getBlock())) {
                        // Drop items and remove block
                        Block.dropResources(state, level, pos);
                        serverLevel.sendParticles(
                                new BlockParticleOption(ParticleTypes.BLOCK, state),
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                8, 0.3, 0.3, 0.3, 0.05);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        excavated++;
                    }
                }
            }
        }

        if (excavated == 0) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.mold_earth_fail"), true);
            return InteractionResult.FAIL;
        }

        level.playSound(null, center, SoundEvents.ROOTED_DIRT_BREAK,
                SoundSource.BLOCKS, 1.5F, 0.8F);

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.mold_earth_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
