package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Mycelium Slurry — a fungal fluid that spreads nether-like growth.
 * - Converts dirt/grass to mycelium on contact
 * - Grows red/brown mushrooms on nearby mycelium blocks
 * - When bone meal is applied nearby (bone block adjacent), causes explosive
 *   mushroom growth — giant mushrooms and nylium spread
 * - Source blocks slowly spread nether vines on adjacent blocks
 */
public class MyceliumSlurryBlock extends TranslucentLiquidBlock {

    private static final int GROW_TICKS = 20;
    private static final int SPREAD_RADIUS = 2;

    public MyceliumSlurryBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide()) {
            spreadMycelium((ServerLevel) level, pos);
            level.scheduleTick(pos, this, GROW_TICKS);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        spreadMycelium(level, pos);
        growMushrooms(level, pos, random);
        checkBoneBlockReaction(level, pos, random);

        level.scheduleTick(pos, this, GROW_TICKS);
    }

    /**
     * Convert dirt, grass, and podzol to mycelium within a radius.
     */
    private void spreadMycelium(ServerLevel level, BlockPos center) {
        for (int dx = -SPREAD_RADIUS; dx <= SPREAD_RADIUS; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -SPREAD_RADIUS; dz <= SPREAD_RADIUS; dz++) {
                    BlockPos target = center.offset(dx, dy, dz);
                    BlockState targetState = level.getBlockState(target);
                    Block targetBlock = targetState.getBlock();

                    if (targetBlock == Blocks.GRASS_BLOCK || targetBlock == Blocks.DIRT
                            || targetBlock == Blocks.PODZOL || targetBlock == Blocks.COARSE_DIRT) {
                        level.setBlock(target, Blocks.MYCELIUM.defaultBlockState(), 3);

                        // Spore particles
                        double cx = target.getX() + 0.5;
                        double cy = target.getY() + 1.1;
                        double cz = target.getZ() + 0.5;
                        level.sendParticles(ParticleTypes.MYCELIUM, cx, cy, cz, 5, 0.3, 0.1, 0.3, 0.01);
                    }
                }
            }
        }
    }

    /** All mushroom types that can be grown, weighted by rarity. */
    private Block getRandomMushroom(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 15) return Blocks.RED_MUSHROOM;          // 15% — vanilla
        if (roll < 30) return Blocks.BROWN_MUSHROOM;         // 15% — vanilla
        if (roll < 45) return ModBlocks.GHOST_FUNGUS.get();  // 15% — bioluminescent
        if (roll < 58) return ModBlocks.INDIGO_MILK_CAP.get(); // 13% — blue
        if (roll < 70) return ModBlocks.AMETHYST_DECEIVER.get(); // 12% — purple
        if (roll < 80) return ModBlocks.BLEEDING_TOOTH.get(); // 10% — bloody
        if (roll < 90) return ModBlocks.LIONS_MANE.get();    // 10% — white tendrils
        return ModBlocks.DEVILS_CIGAR.get();                  // 10% — reactive star
    }

    /**
     * Occasionally spawn mushrooms on nearby mycelium blocks.
     * Grows a mix of vanilla and custom exotic mushroom varieties.
     */
    private void growMushrooms(ServerLevel level, BlockPos center, RandomSource random) {
        if (random.nextInt(4) != 0) return; // 25% chance per tick cycle

        for (int dx = -SPREAD_RADIUS; dx <= SPREAD_RADIUS; dx++) {
            for (int dz = -SPREAD_RADIUS; dz <= SPREAD_RADIUS; dz++) {
                BlockPos ground = center.offset(dx, -1, dz);
                BlockPos mushroomPos = ground.above();

                if (level.getBlockState(ground).getBlock() == Blocks.MYCELIUM
                        && level.getBlockState(mushroomPos).isAir()) {
                    if (random.nextInt(8) == 0) {
                        Block mushroom = getRandomMushroom(random);
                        level.setBlock(mushroomPos, mushroom.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    /**
     * If a bone block is adjacent, trigger explosive fungal growth:
     * - Convert nearby blocks to warped/crimson nylium
     * - Consume the bone block
     */
    private void checkBoneBlockReaction(ServerLevel level, BlockPos center, RandomSource random) {
        for (BlockPos neighbor : new BlockPos[]{
                center.above(), center.below(),
                center.north(), center.south(),
                center.east(), center.west()}) {

            if (level.getBlockState(neighbor).getBlock() == Blocks.BONE_BLOCK) {
                // Consume bone block
                level.destroyBlock(neighbor, false);

                // Explosive fungal growth — convert nearby surface to nylium
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        if (dx * dx + dz * dz > 9) continue;
                        BlockPos ground = center.offset(dx, -1, dz);
                        BlockState groundState = level.getBlockState(ground);

                        if (groundState.getBlock() == Blocks.MYCELIUM
                                || groundState.is(BlockTags.DIRT)
                                || groundState.getBlock() == Blocks.GRASS_BLOCK) {
                            Block nylium = random.nextBoolean()
                                    ? Blocks.WARPED_NYLIUM : Blocks.CRIMSON_NYLIUM;
                            level.setBlock(ground, nylium.defaultBlockState(), 3);
                        }

                        // Place nether vegetation on top
                        BlockPos aboveGround = ground.above();
                        if (level.getBlockState(aboveGround).isAir() && random.nextInt(3) == 0) {
                            Block vegetation;
                            int choice = random.nextInt(4);
                            switch (choice) {
                                case 0 -> vegetation = Blocks.WARPED_FUNGUS;
                                case 1 -> vegetation = Blocks.CRIMSON_FUNGUS;
                                case 2 -> vegetation = Blocks.WARPED_ROOTS;
                                default -> vegetation = Blocks.CRIMSON_ROOTS;
                            }
                            level.setBlock(aboveGround, vegetation.defaultBlockState(), 3);
                        }
                    }
                }

                // Big particle explosion
                double cx = center.getX() + 0.5;
                double cy = center.getY() + 0.5;
                double cz = center.getZ() + 0.5;
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, cx, cy + 1.0, cz, 40, 2.0, 1.0, 2.0, 0.1);
                level.sendParticles(ParticleTypes.MYCELIUM, cx, cy + 0.5, cz, 30, 1.5, 0.5, 1.5, 0.05);

                break; // Only react once per tick
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        // Constant spore particles
        if (random.nextInt(2) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.8 + random.nextDouble() * 0.4;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.MYCELIUM, x, y, z,
                    (random.nextDouble() - 0.5) * 0.01, 0.02, (random.nextDouble() - 0.5) * 0.01);
        }

        // Occasional spore blossom particles
        if (random.nextInt(6) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.5 + random.nextDouble() * 0.5;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, x, y, z, 0, 0.01, 0);
        }
    }
}
