package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Throwable reagent projectile — shifts pH of indicator fluid blocks.
 * Acid reagent lowers pH (towards red), base reagent raises pH (towards violet).
 * Performs a BFS flood-fill up to MAX_FLOOD indicator blocks from impact point.
 */
public class ReagentEntity extends ThrowableItemProjectile {

    private static final int MAX_FLOOD = 512;

    public ReagentEntity(EntityType<? extends ReagentEntity> type, Level level) {
        super(type, level);
    }

    public ReagentEntity(EntityType<? extends ReagentEntity> type, LivingEntity thrower, Level level) {
        super(type, thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ACID_REAGENT.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            BlockPos center = result instanceof BlockHitResult bhr
                    ? bhr.getBlockPos()
                    : BlockPos.containing(getX(), getY(), getZ());
            for (BlockPos candidate : new BlockPos[]{center,
                    center.above(), center.below(),
                    center.north(), center.south(),
                    center.east(), center.west()}) {
                if (tryShiftPH(candidate)) break;
            }
            discard();
        }
    }

    private boolean tryShiftPH(BlockPos start) {
        BlockState startState = level().getBlockState(start);
        if (!(startState.getBlock() instanceof IndicatorBlock)) return false;

        Item thrownItem = getItem().getItem();
        int delta;
        if (thrownItem == ModItems.ACID_REAGENT.get()) {
            delta = -1; // lower pH = more acidic = towards red
        } else if (thrownItem == ModItems.BASE_REAGENT.get()) {
            delta = +1; // higher pH = more basic = towards violet
        } else {
            return false;
        }

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && visited.size() <= MAX_FLOOD) {
            BlockPos current = queue.poll();
            BlockState currentState = level().getBlockState(current);
            if (!(currentState.getBlock() instanceof IndicatorBlock)) continue;

            int currentPH = currentState.getValue(IndicatorBlock.PH);
            int newPH = Math.max(0, Math.min(IndicatorBlock.MAX_PH, currentPH + delta));
            if (newPH != currentPH) {
                level().setBlock(current, currentState.setValue(IndicatorBlock.PH, newPH), 3);
            }

            for (BlockPos nb : new BlockPos[]{
                    current.above(), current.below(),
                    current.north(), current.south(),
                    current.east(), current.west()}) {
                if (!visited.contains(nb)) {
                    BlockState nbState = level().getBlockState(nb);
                    if (nbState.getBlock() instanceof IndicatorBlock) {
                        visited.add(nb);
                        queue.add(nb);
                    }
                }
            }
        }
        return true;
    }
}
