package com.reactivefluids;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Indigo Milk Cap (Lactarius indigo) — vivid deep blue mushroom.
 * Concentric ring pattern on cap. When broken, "bleeds" blue —
 * drops blue dye alongside itself.
 */
public class IndigoMilkCapBlock extends BushBlock {

    public static final MapCodec<IndigoMilkCapBlock> CODEC = simpleCodec(IndigoMilkCapBlock::new);
    private static final VoxelShape SHAPE = box(4, 0, 4, 12, 9, 12);

    public IndigoMilkCapBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<IndigoMilkCapBlock> codec() {
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
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new java.util.ArrayList<>(super.getDrops(state, builder));
        // Bleeds blue dye when harvested
        drops.add(new ItemStack(Items.BLUE_DYE, 1));
        return drops;
    }
}
