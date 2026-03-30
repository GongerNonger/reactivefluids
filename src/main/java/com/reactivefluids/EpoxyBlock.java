package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class EpoxyBlock extends Block {

    public EpoxyBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // Only emit particles when placed by a fluid reaction (old block was a fluid source/flow)
        if (!level.isClientSide() && !oldState.getFluidState().isEmpty()
                && level instanceof ServerLevel serverLevel) {
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;
            // White foam/cloud burst — visible in all environments
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    cx, cy + 0.2, cz, 20, 0.4, 0.2, 0.4, 0.04);
            // Rapid dissolving pops for a chemical-reaction feel
            serverLevel.sendParticles(ParticleTypes.POOF,
                    cx, cy + 0.5, cz, 12, 0.3, 0.15, 0.3, 0.08);
        }
    }
}
