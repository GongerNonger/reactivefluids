package com.reactivefluids;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;

/**
 * 3rd-level — causes rapid plant growth in a wide area.
 * Saplings advance to trees, crops grow to max age, bonemealable blocks grow.
 * Based on D&D 5e Plant Growth.
 */
public class PlantGrowthScrollItem extends Item {

    private static final int RADIUS = 12;

    public PlantGrowthScrollItem(Properties properties) {
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
        RandomSource random = serverLevel.getRandom();
        int grown = 0;

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -4; dy <= 8; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (dx * dx + dz * dz > RADIUS * RADIUS) continue;

                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    Block block = state.getBlock();

                    boolean didGrow = false;

                    if (block instanceof SaplingBlock sapling) {
                        // Attempt to grow the sapling into a tree
                        sapling.advanceTree(serverLevel, pos, state, random);
                        if (level.getBlockState(pos) != state) {
                            didGrow = true;
                        }
                    } else if (block instanceof CropBlock crop) {
                        int maxAge = crop.getMaxAge();
                        BlockState maxState = crop.getStateForAge(maxAge);
                        if (state != maxState) {
                            level.setBlock(pos, maxState, Block.UPDATE_ALL);
                            didGrow = true;
                        }
                    } else if (block instanceof CactusBlock || block instanceof SugarCaneBlock) {
                        // Grow cactus/sugar cane to max height (3 blocks)
                        int stackHeight = 1;
                        BlockPos below = pos.below();
                        while (level.getBlockState(below).is(block) && stackHeight < 3) {
                            stackHeight++;
                            below = below.below();
                        }
                        for (int h = stackHeight; h < 3; h++) {
                            BlockPos above = pos.above(h - stackHeight + 1);
                            if (level.getBlockState(above).isAir()) {
                                level.setBlock(above, block.defaultBlockState(), Block.UPDATE_ALL);
                                didGrow = true;
                            } else {
                                break;
                            }
                        }
                    } else if (state.is(Blocks.LILY_PAD)) {
                        // Spread lily pads to adjacent water surfaces
                        for (int i = 0; i < 3; i++) {
                            int ox = random.nextInt(3) - 1;
                            int oz = random.nextInt(3) - 1;
                            BlockPos adj = pos.offset(ox, 0, oz);
                            if (level.getBlockState(adj).isAir()
                                    && level.getBlockState(adj.below()).getFluidState().isSource()) {
                                level.setBlock(adj, Blocks.LILY_PAD.defaultBlockState(), Block.UPDATE_ALL);
                                didGrow = true;
                            }
                        }
                    } else if (block instanceof BonemealableBlock bonemealable) {
                        // General bonemealable blocks (grass, flowers, etc.)
                        if (bonemealable.isValidBonemealTarget(level, pos, state)) {
                            for (int i = 0; i < 5; i++) {
                                if (bonemealable.isValidBonemealTarget(level, pos, state)) {
                                    bonemealable.performBonemeal(serverLevel, random, pos, state);
                                    state = level.getBlockState(pos);
                                }
                            }
                            didGrow = true;
                        }
                    }

                    if (didGrow) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                8, 0.4, 0.4, 0.4, 0.02);
                        grown++;
                    }
                }
            }
        }

        if (grown == 0) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.plant_growth_fail"), true);
            return InteractionResult.FAIL;
        }

        level.playSound(null, center, SoundEvents.CHORUS_FLOWER_GROW,
                SoundSource.BLOCKS, 2.0F, 1.0F);

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.plant_growth_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
