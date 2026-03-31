package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Lion's Mane (Hericium erinaceus) — pure white cascading tendrils.
 * Looks like a frozen waterfall or pom-pom. Reduces fall damage
 * like a hay bale when landed on.
 */
public class LionsManeBlock extends BushBlock {

    private static final VoxelShape SHAPE = box(3, 0, 3, 13, 11, 13);

    public LionsManeBlock(Properties properties) {
        super(properties);
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
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        // Reduce fall damage by 80% — soft fungal cushion
        entity.causeFallDamage(fallDistance, 0.2f, level.damageSources().fall());
    }
}
