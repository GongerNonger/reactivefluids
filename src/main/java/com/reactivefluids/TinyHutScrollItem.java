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

import java.util.ArrayList;
import java.util.List;

/**
 * 3rd-level — Leomund's Tiny Hut. Conjures a dome of light blue stained glass
 * centered on the caster with radius 5. Lasts 10 minutes then vanishes.
 */
public class TinyHutScrollItem extends Item {

    private static final double RADIUS = 5.0;
    private static final double INNER_RADIUS = 4.5;
    private static final double OUTER_RADIUS = 5.5;
    private static final int DURATION_TICKS = 12000; // 10 minutes

    public TinyHutScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ServerLevel serverLevel = (ServerLevel) level;

        // Center dome on player position
        BlockPos center = player.blockPosition();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        List<BlockPos> placedBlocks = new ArrayList<>();
        int range = (int) Math.ceil(OUTER_RADIUS);

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = 0; dy <= range; dy++) { // Floor is left open (don't place below caster y)
                for (int dz = -range; dz <= range; dz++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist >= INNER_RADIUS && dist <= OUTER_RADIUS) {
                        BlockPos pos = new BlockPos(centerX + dx, centerY + dy, centerZ + dz);
                        if (level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, ModBlocks.ARCANE_BARRIER.get().defaultBlockState(), Block.UPDATE_ALL);
                            placedBlocks.add(pos.immutable());
                        }
                    }
                }
            }
        }

        // Place a lantern at center
        BlockPos lanternPos = center;
        if (level.getBlockState(lanternPos).isAir()) {
            level.setBlock(lanternPos, Blocks.LANTERN.defaultBlockState(), Block.UPDATE_ALL);
            placedBlocks.add(lanternPos.immutable());
        }

        if (placedBlocks.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.reactivefluids.tiny_hut_blocked"), true);
            return InteractionResult.FAIL;
        }

        // Register for expiration
        TinyHutData.get(serverLevel).addHut(placedBlocks, center,
                level.getGameTime() + DURATION_TICKS);

        // Effects
        level.playSound(null, center, SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, center, SoundEvents.GLASS_PLACE,
                SoundSource.BLOCKS, 1.5F, 0.8F);

        // Particles around the dome
        serverLevel.sendParticles(ParticleTypes.END_ROD,
                centerX + 0.5, centerY + 3.0, centerZ + 0.5,
                40, 3.0, 2.0, 3.0, 0.02);

        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltipComponents, TooltipFlag flag) {
        tooltipComponents.add(Component.translatable("item.reactivefluids.tiny_hut_scroll.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
