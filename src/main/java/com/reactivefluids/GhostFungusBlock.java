package com.reactivefluids;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/**
 * Ghost Fungus (Omphalotus nidiformis) — bioluminescent mushroom.
 * Cream-colored cap that emits an eerie blue-green glow in darkness.
 * Light level 8 — dimmer than glowstone but atmospheric.
 */
public class GhostFungusBlock extends BushBlock {

    public static final MapCodec<GhostFungusBlock> CODEC = simpleCodec(GhostFungusBlock::new);
    private static final VoxelShape SHAPE = box(4, 0, 4, 12, 10, 12);

    public GhostFungusBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<GhostFungusBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getBlock() instanceof net.minecraft.world.level.block.MyceliumBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.NyliumBlock
                || super.mayPlaceOn(state, level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 0.5 + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
            // Blue-green bioluminescent glow particles
            level.addParticle(
                    new DustParticleOptions(new Vector3f(0.1f, 0.85f, 0.7f), 0.6f),
                    x, y, z, 0, 0.015, 0);
        }
    }
}
