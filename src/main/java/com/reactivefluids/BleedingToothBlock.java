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
 * Bleeding Tooth Fungus (Hydnellum peckii) — white velvety surface
 * oozing thick blood-red droplets. Emits dripping red particles constantly.
 */
public class BleedingToothBlock extends BushBlock {

    public static final MapCodec<BleedingToothBlock> CODEC = simpleCodec(BleedingToothBlock::new);
    private static final VoxelShape SHAPE = box(3, 0, 3, 13, 8, 13);

    public BleedingToothBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BleedingToothBlock> codec() {
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
        // Constant dripping red "blood" particles
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 0.2 + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
            // Dark blood red
            level.addParticle(
                    new DustParticleOptions(new Vector3f(0.7f, 0.05f, 0.05f), 0.9f),
                    x, y, z, 0, -0.04, 0);
        }
        // Occasional larger drip falling down
        if (random.nextInt(8) == 0) {
            double x = pos.getX() + 0.4 + random.nextDouble() * 0.2;
            double y = pos.getY() + 0.1;
            double z = pos.getZ() + 0.4 + random.nextDouble() * 0.2;
            level.addParticle(
                    new DustParticleOptions(new Vector3f(0.85f, 0.0f, 0.0f), 1.2f),
                    x, y, z, 0, -0.06, 0);
        }
    }
}
