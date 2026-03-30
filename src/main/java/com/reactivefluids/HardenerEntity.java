package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class HardenerEntity extends ThrowableItemProjectile {

    private static final int MAX_FLOOD = 1024;

    public HardenerEntity(EntityType<? extends HardenerEntity> type, Level level) {
        super(type, level);
    }

    public HardenerEntity(EntityType<? extends HardenerEntity> type, LivingEntity thrower, Level level) {
        super(type, thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.AMBER_HARDENER.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            BlockPos center = result instanceof BlockHitResult bhr
                    ? bhr.getBlockPos()
                    : BlockPos.containing(getX(), getY(), getZ());
            // Try the hit pos and all 6 neighbours — resin might be adjacent to the surface hit
            for (BlockPos candidate : new BlockPos[]{center,
                    center.above(), center.below(),
                    center.north(), center.south(),
                    center.east(), center.west()}) {
                if (tryFlood(candidate)) break;
            }
            discard();
        }
    }

    private boolean tryFlood(BlockPos start) {
        Block startBlock = level().getBlockState(start).getBlock();

        Block resinBlock;
        Block glowingResinBlock;
        BlockState epoxyState;
        BlockState glowingEpoxyState;
        Item thrownItem = getItem().getItem();

        if (thrownItem == ModItems.AMBER_HARDENER.get()) {
            resinBlock        = ModBlocks.AMBER_RESIN_BLOCK.get();
            glowingResinBlock = ModBlocks.AMBER_GLOWING_RESIN_BLOCK.get();
            epoxyState        = ModBlocks.AMBER_EPOXY_BLOCK.get().defaultBlockState();
            glowingEpoxyState = ModBlocks.AMBER_EPOXY_GLOWING.get().defaultBlockState();
        } else if (thrownItem == ModItems.COBALT_HARDENER.get()) {
            resinBlock        = ModBlocks.COBALT_RESIN_BLOCK.get();
            glowingResinBlock = ModBlocks.COBALT_GLOWING_RESIN_BLOCK.get();
            epoxyState        = ModBlocks.COBALT_EPOXY_BLOCK.get().defaultBlockState();
            glowingEpoxyState = ModBlocks.COBALT_EPOXY_GLOWING.get().defaultBlockState();
        } else if (thrownItem == ModItems.JADE_HARDENER.get()) {
            resinBlock        = ModBlocks.JADE_RESIN_BLOCK.get();
            glowingResinBlock = ModBlocks.JADE_GLOWING_RESIN_BLOCK.get();
            epoxyState        = ModBlocks.JADE_EPOXY_BLOCK.get().defaultBlockState();
            glowingEpoxyState = ModBlocks.JADE_EPOXY_GLOWING.get().defaultBlockState();
        } else {
            return false;
        }

        if (startBlock != resinBlock && startBlock != glowingResinBlock) return false;

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && visited.size() <= MAX_FLOOD) {
            BlockPos current = queue.poll();
            Block currentBlock = level().getBlockState(current).getBlock();
            if (currentBlock == resinBlock) {
                level().setBlock(current, epoxyState, 3);
            } else if (currentBlock == glowingResinBlock) {
                level().setBlock(current, glowingEpoxyState, 3);
            } else {
                continue;
            }
            for (BlockPos nb : new BlockPos[]{
                    current.above(), current.below(),
                    current.north(), current.south(),
                    current.east(), current.west()}) {
                if (!visited.contains(nb)) {
                    Block nbBlock = level().getBlockState(nb).getBlock();
                    if (nbBlock == resinBlock || nbBlock == glowingResinBlock) {
                        visited.add(nb);
                        queue.add(nb);
                    }
                }
            }
        }
        return true;
    }
}
