package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/**
 * Lava Lamp — decorative light-emitting block with rising blob particles.
 * Emits colored dust particles that float upward inside the lamp shape,
 * simulating the wax blobs in a real lava lamp.
 */
public class LavaLampBlock extends Block {

    // Slightly narrower than a full block — lamp shape
    private static final VoxelShape SHAPE = box(3, 0, 3, 13, 16, 13);

    private final int particleColor; // RGB packed

    public LavaLampBlock(Properties properties, int particleColor) {
        super(properties);
        this.particleColor = particleColor;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Rising blob particles — 2-3 per tick
        for (int i = 0; i < 2 + random.nextInt(2); i++) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 0.1 + random.nextDouble() * 0.8;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;

            // Slight horizontal wobble, steady upward drift
            double vx = (random.nextDouble() - 0.5) * 0.01;
            double vy = 0.02 + random.nextDouble() * 0.02;
            double vz = (random.nextDouble() - 0.5) * 0.01;

            float r = ((particleColor >> 16) & 0xFF) / 255.0F;
            float g = ((particleColor >> 8) & 0xFF) / 255.0F;
            float b = (particleColor & 0xFF) / 255.0F;

            // Vary the color slightly per particle for organic feel
            float variance = 0.9F + random.nextFloat() * 0.2F;
            float size = 0.4F + random.nextFloat() * 0.6F;

            level.addParticle(
                    new DustParticleOptions(new Vector3f(
                            Mth.clamp(r * variance, 0, 1),
                            Mth.clamp(g * variance, 0, 1),
                            Mth.clamp(b * variance, 0, 1)), size),
                    x, y, z, vx, vy, vz);
        }
    }
}
