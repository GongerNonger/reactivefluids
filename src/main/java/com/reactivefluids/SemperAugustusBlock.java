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
 * Semper Augustus — the legendary tulip of Dutch Tulip Mania (1637).
 * White petals streaked with vivid crimson flames, once worth more
 * than a canal house in Amsterdam.
 */
public class SemperAugustusBlock extends BushBlock {

    public static final MapCodec<SemperAugustusBlock> CODEC = simpleCodec(SemperAugustusBlock::new);
    private static final VoxelShape SHAPE = box(5, 0, 5, 11, 12, 11);

    public SemperAugustusBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<SemperAugustusBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Rare crimson petal drift particle
        if (random.nextInt(8) == 0) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double y = pos.getY() + 0.6 + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
            level.addParticle(
                    new DustParticleOptions(new Vector3f(0.8f, 0.1f, 0.15f), 0.5f),
                    x, y, z, 0, -0.02, 0);
        }
    }
}
