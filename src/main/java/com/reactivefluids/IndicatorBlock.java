package com.reactivefluids;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;

/**
 * Rainbow pH indicator fluid — starts neutral (green, pH 3) and shifts
 * towards red (acidic, pH 0) or violet (basic, pH 6) when hit by
 * throwable acid/base reagents.
 */
public class IndicatorBlock extends TranslucentLiquidBlock {

    /** pH level: 0=red (acid), 1=orange, 2=yellow, 3=green (neutral), 4=blue, 5=indigo, 6=violet (base) */
    public static final int MAX_PH = 6;
    public static final int NEUTRAL_PH = 3;
    public static final IntegerProperty PH = IntegerProperty.create("ph", 0, MAX_PH);

    public IndicatorBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
        registerDefaultState(stateDefinition.any().setValue(PH, NEUTRAL_PH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PH);
    }
}
