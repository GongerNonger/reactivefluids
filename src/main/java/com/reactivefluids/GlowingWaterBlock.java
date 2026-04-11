package com.reactivefluids;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowingWaterBlock extends Block {

    public static final int MAX_GLOW = 4;
    public static final IntegerProperty GLOW = IntegerProperty.create("glow", 0, MAX_GLOW);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final int FADE_INTERVAL_TICKS = 12;

    public GlowingWaterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(GLOW, MAX_GLOW)
                .setValue(WATERLOGGED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GLOW, WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int glow = state.getValue(GLOW);
        if (glow <= 1) {
            boolean waterlogged = state.getValue(WATERLOGGED);
            BlockState replacement = waterlogged
                    ? Blocks.WATER.defaultBlockState()
                    : Blocks.AIR.defaultBlockState();
            level.setBlock(pos, replacement, 3);
        } else {
            level.setBlock(pos, state.setValue(GLOW, glow - 1), 3);
            level.scheduleTick(pos, this, FADE_INTERVAL_TICKS);
        }
    }

    public static void illuminate(ServerLevel level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        GlowingWaterBlock glowingWater = ModBlocks.GLOWING_WATER.get();

        if (current.is(glowingWater)) {
            int existingGlow = current.getValue(GLOW);
            if (existingGlow < MAX_GLOW) {
                level.setBlock(pos, current.setValue(GLOW, MAX_GLOW), 3);
            }
            level.scheduleTick(pos, glowingWater, FADE_INTERVAL_TICKS);
            return;
        }

        boolean isAir = current.isAir();
        boolean isWater = current.getFluidState().getType() == Fluids.WATER;
        if (!isAir && !isWater) return;

        BlockState newState = glowingWater.defaultBlockState()
                .setValue(GLOW, MAX_GLOW)
                .setValue(WATERLOGGED, isWater);
        level.setBlock(pos, newState, 3);
        level.scheduleTick(pos, glowingWater, FADE_INTERVAL_TICKS);
    }
}
