package com.reactivefluids;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Devil's Cigar (Chorioactis geaster) — two-state reactive mushroom.
 * Starts as a closed dark-brown cigar shape. When right-clicked or
 * powered by redstone, bursts open into a star shape and releases
 * a cloud of spore particles. One of the rarest fungi on Earth.
 */
public class DevilsCigarBlock extends BushBlock {

    public static final MapCodec<DevilsCigarBlock> CODEC = simpleCodec(DevilsCigarBlock::new);
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    private static final VoxelShape CLOSED_SHAPE = box(5, 0, 5, 11, 12, 11);
    private static final VoxelShape OPEN_SHAPE = box(2, 0, 2, 14, 8, 14);

    public DevilsCigarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OPEN, false));
    }

    @Override
    protected MapCodec<DevilsCigarBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(OPEN) ? OPEN_SHAPE : CLOSED_SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getBlock() instanceof net.minecraft.world.level.block.MyceliumBlock
                || state.getBlock() instanceof net.minecraft.world.level.block.NyliumBlock
                || super.mayPlaceOn(state, level, pos);
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (!state.getValue(OPEN)) {
            burstOpen(state, level, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                BlockPos neighborPos, boolean movedByPiston) {
        if (!state.getValue(OPEN) && level.hasNeighborSignal(pos)) {
            burstOpen(state, level, pos);
        }
    }

    private void burstOpen(BlockState state, Level level, BlockPos pos) {
        level.setBlock(pos, state.setValue(OPEN, true), 3);

        if (level instanceof ServerLevel serverLevel) {
            // Spore cloud explosion
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, cx, cy + 0.3, cz,
                    15, 0.4, 0.3, 0.4, 0.03);
            serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, cx, cy + 0.5, cz,
                    25, 0.8, 0.5, 0.8, 0.08);
            serverLevel.sendParticles(ParticleTypes.MYCELIUM, cx, cy, cz,
                    20, 0.6, 0.3, 0.6, 0.05);
        }
        level.playSound(null, pos, SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.BLOCKS, 0.8f, 0.6f);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(OPEN) && random.nextInt(4) == 0) {
            // Open state: occasional lingering spores
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.3 + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
            level.addParticle(ParticleTypes.MYCELIUM, x, y, z,
                    (random.nextDouble() - 0.5) * 0.01, 0.015, (random.nextDouble() - 0.5) * 0.01);
        }
    }
}
