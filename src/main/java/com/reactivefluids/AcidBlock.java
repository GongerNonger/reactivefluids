package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.Set;

/**
 * Acid fluid block — only SOURCE blocks dissolve the block directly below.
 * Drills a 1x1 shaft straight down through stone, leaving ores exposed.
 * Stops at bedrock, obsidian, and ore blocks.
 */
public class AcidBlock extends TranslucentLiquidBlock {

    private static final int DISSOLVE_TICKS = 8;

    /** Blocks that acid will NOT dissolve — they survive. */
    private static final Set<Block> IMMUNE = Set.of(
        Blocks.BEDROCK,
        Blocks.OBSIDIAN,
        Blocks.CRYING_OBSIDIAN,
        Blocks.REINFORCED_DEEPSLATE,
        Blocks.END_PORTAL_FRAME,
        Blocks.BARRIER,
        Blocks.COMMAND_BLOCK
    );

    public AcidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        // Only source blocks (level 0 = full source) dissolve downward
        if (state.getFluidState().getAmount() < 8) return;

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        Block belowBlock = belowState.getBlock();

        // Don't dissolve immune blocks
        if (IMMUNE.contains(belowBlock)) return;

        // Don't dissolve ores — leave them exposed
        if (isOre(belowState)) return;

        // Don't dissolve fluids or air
        if (belowState.isAir() || !belowState.getFluidState().isEmpty()) return;

        // Only dissolve "natural" blocks — stone-like, dirt-like, sand-like
        if (!isDissolvable(belowState)) return;

        // Dissolve the block below — replace with air so acid flows down
        level.destroyBlock(below, false);

        // Hissing/bubbling particles
        double cx = below.getX() + 0.5;
        double cy = below.getY() + 0.5;
        double cz = below.getZ() + 0.5;
        level.sendParticles(ParticleTypes.SMOKE, cx, cy + 0.2, cz, 8, 0.3, 0.15, 0.3, 0.02);
        level.sendParticles(ParticleTypes.CLOUD, cx, cy + 0.5, cz, 4, 0.2, 0.1, 0.2, 0.03);

        // Reschedule so the acid keeps drilling
        level.scheduleTick(pos, this, DISSOLVE_TICKS);
    }

    @Override
    public void onPlace(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && state.getFluidState().getAmount() >= 8) {
            level.scheduleTick(pos, this, DISSOLVE_TICKS);
        }
    }

    private boolean isOre(BlockState state) {
        // Tag check covers all vanilla and modded ores
        if (state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES) || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES)) {
            return true;
        }
        // Catch ancient debris and other special ores
        Block b = state.getBlock();
        return b == Blocks.ANCIENT_DEBRIS || b == Blocks.NETHER_QUARTZ_ORE
                || b == Blocks.NETHER_GOLD_ORE;
    }

    private boolean isDissolvable(BlockState state) {
        // Stone family
        if (state.is(BlockTags.BASE_STONE_OVERWORLD)) return true;
        if (state.is(BlockTags.DIRT)) return true;
        if (state.is(BlockTags.SAND)) return true;

        Block b = state.getBlock();
        return b == Blocks.GRAVEL || b == Blocks.CLAY
                || b == Blocks.SANDSTONE || b == Blocks.RED_SANDSTONE
                || b == Blocks.COBBLESTONE || b == Blocks.MOSSY_COBBLESTONE
                || b == Blocks.COBBLED_DEEPSLATE
                || b == Blocks.SMOOTH_BASALT || b == Blocks.BASALT
                || b == Blocks.NETHERRACK || b == Blocks.SOUL_SAND || b == Blocks.SOUL_SOIL
                || b == Blocks.END_STONE
                || b == Blocks.TERRACOTTA || b == Blocks.PACKED_MUD || b == Blocks.MUD;
    }
}
