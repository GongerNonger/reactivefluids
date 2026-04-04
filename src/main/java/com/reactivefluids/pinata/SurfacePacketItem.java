package com.reactivefluids.pinata;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Surface Packet — converts terrain to a specific surface type.
 * Infinite durability (like VP). Four types: Grass, Long Grass, Sand, Snow.
 *
 * Right-click on dirt/grass/sand/snow to convert it.
 * Cannot convert water, bedrock, or non-terrain blocks.
 */
public class SurfacePacketItem extends Item {

    public enum SurfaceType {
        GRASS(() -> Blocks.GRASS_BLOCK),
        LONG_GRASS(() -> Blocks.SHORT_GRASS),  // Places grass on top
        SAND(() -> Blocks.SAND),
        SNOW(() -> Blocks.SNOW_BLOCK);

        public final Supplier<Block> block;
        SurfaceType(Supplier<Block> block) { this.block = block; }
    }

    private final SurfaceType surfaceType;

    public SurfacePacketItem(Properties properties, SurfaceType surfaceType) {
        super(properties);
        this.surfaceType = surfaceType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (level.isClientSide()) return InteractionResult.sidedSuccess(true);

        // Only convert terrain-like blocks
        if (!isConvertible(state)) return InteractionResult.PASS;

        ServerLevel serverLevel = (ServerLevel) level;

        if (surfaceType == SurfaceType.LONG_GRASS) {
            // Long grass: convert ground to grass, place tall grass on top
            if (!state.is(Blocks.GRASS_BLOCK)) {
                level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            }
            BlockPos above = pos.above();
            if (level.getBlockState(above).isAir()) {
                level.setBlock(above, Blocks.SHORT_GRASS.defaultBlockState(), 3);
            }
        } else {
            level.setBlock(pos, surfaceType.block.get().defaultBlockState(), 3);
        }

        // Particles and sound
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                5, 0.3, 0.2, 0.3, 0.02);
        level.playSound(null, pos, SoundEvents.GRASS_PLACE,
                SoundSource.BLOCKS, 0.8F, 1.0F);

        // No durability loss (infinite use like VP)
        return InteractionResult.SUCCESS;
    }

    private boolean isConvertible(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
               state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) ||
               state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.COARSE_DIRT) ||
               state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM) ||
               state.is(Blocks.CLAY) || state.is(Blocks.GRAVEL) ||
               state.is(Blocks.FARMLAND) || state.is(Blocks.DIRT_PATH);
    }
}
