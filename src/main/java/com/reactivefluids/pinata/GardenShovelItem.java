package com.reactivefluids.pinata;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Garden Shovel — the primary garden tool from Viva Piñata.
 *
 * Right-click on blocks:
 * - Grass → converts to dirt (dig up turf)
 * - Dirt → converts to water source (dig pond)
 * - Dirt/grass → converts to farmland (till)
 *
 * Left-click (attack) on piñatas:
 * - "Whack" — can trigger evolution steps (Lickatoad→Lackatoad),
 *   discipline piñatas, or break open piñatas for candy.
 *
 * In VP, the shovel is the most important tool, used for terraforming
 * the entire garden.
 */
public class GardenShovelItem extends Item {

    public GardenShovelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (level.isClientSide()) return InteractionResult.sidedSuccess(true);

        // Grass → Dirt (strip turf)
        if (state.is(Blocks.GRASS_BLOCK)) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            playDigSound(level, pos);
            spawnDigParticles((ServerLevel) level, pos);
            if (player != null) context.getItemInHand().hurtAndBreak(1, player,
                    LivingEntity.getSlotForHand(context.getHand()));
            return InteractionResult.SUCCESS;
        }

        // Dirt → Water (dig pond)
        if (state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)) {
            // Check if sneak-clicking for pond
            if (player != null && player.isShiftKeyDown()) {
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                playDigSound(level, pos);
                spawnDigParticles((ServerLevel) level, pos);
                context.getItemInHand().hurtAndBreak(2, player,
                        LivingEntity.getSlotForHand(context.getHand()));
                return InteractionResult.SUCCESS;
            }
            // Normal click: dirt → farmland
            level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 3);
            playDigSound(level, pos);
            context.getItemInHand().hurtAndBreak(1, player,
                    LivingEntity.getSlotForHand(context.getHand()));
            return InteractionResult.SUCCESS;
        }

        // Sand → Red Sand (variant cycling for garden decoration)
        if (state.is(Blocks.SAND)) {
            level.setBlock(pos, Blocks.RED_SAND.defaultBlockState(), 3);
            playDigSound(level, pos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Whacking piñatas — used for discipline and evolution triggers
        if (target instanceof BasePinataEntity pinata) {
            pinata.addHappiness(-5); // Discipline
            // Evolution check for shovel-triggered evolutions (e.g., Lickatoad→Lackatoad)
        }
        stack.hurtAndBreak(1, attacker, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
        return true;
    }

    private void playDigSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.SHOVEL_FLATTEN,
                SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void spawnDigParticles(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.CLOUD,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                5, 0.3, 0.1, 0.3, 0.02);
    }
}
