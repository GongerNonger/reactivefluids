package com.reactivefluids;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(ReactiveFluids.MOD_ID);

    private static BlockBehaviour.Properties fluidProps(MapColor color) {
        return BlockBehaviour.Properties.of().noCollission().strength(100f).noLootTable()
            .liquid().replaceable().mapColor(color);
    }

    // =========================================================================
    // Fluid in-world blocks — TranslucentLiquidBlock guarantees translucent
    // render layer so the texture alpha channel is respected.
    // =========================================================================
    public static final DeferredBlock<TranslucentLiquidBlock> AMBER_RESIN_BLOCK =
        BLOCKS.register("amber_resin_block", () ->
            new TranslucentLiquidBlock((FlowingFluid) ModFluids.AMBER_RESIN_SOURCE.get(),
                fluidProps(MapColor.COLOR_ORANGE)));

    public static final DeferredBlock<TranslucentLiquidBlock> COBALT_RESIN_BLOCK =
        BLOCKS.register("cobalt_resin_block", () ->
            new TranslucentLiquidBlock((FlowingFluid) ModFluids.COBALT_RESIN_SOURCE.get(),
                fluidProps(MapColor.COLOR_BLUE)));

    public static final DeferredBlock<TranslucentLiquidBlock> JADE_RESIN_BLOCK =
        BLOCKS.register("jade_resin_block", () ->
            new TranslucentLiquidBlock((FlowingFluid) ModFluids.JADE_RESIN_SOURCE.get(),
                fluidProps(MapColor.COLOR_GREEN)));

    // =========================================================================
    // Glowing resin liquid blocks (emits light level 8 while flowing)
    // =========================================================================
    public static final DeferredBlock<TranslucentLiquidBlock> AMBER_GLOWING_RESIN_BLOCK =
        BLOCKS.register("amber_glowing_resin_block", () ->
            new TranslucentLiquidBlock((FlowingFluid) ModFluids.AMBER_GLOWING_RESIN_SOURCE.get(),
                fluidProps(MapColor.COLOR_ORANGE).lightLevel(state -> 8)));

    public static final DeferredBlock<TranslucentLiquidBlock> COBALT_GLOWING_RESIN_BLOCK =
        BLOCKS.register("cobalt_glowing_resin_block", () ->
            new TranslucentLiquidBlock((FlowingFluid) ModFluids.COBALT_GLOWING_RESIN_SOURCE.get(),
                fluidProps(MapColor.COLOR_BLUE).lightLevel(state -> 8)));

    public static final DeferredBlock<TranslucentLiquidBlock> JADE_GLOWING_RESIN_BLOCK =
        BLOCKS.register("jade_glowing_resin_block", () ->
            new TranslucentLiquidBlock((FlowingFluid) ModFluids.JADE_GLOWING_RESIN_SOURCE.get(),
                fluidProps(MapColor.COLOR_GREEN).lightLevel(state -> 8)));

    // =========================================================================
    // Transparent epoxy (forms when fluids meet — glass-like, see-through)
    // noOcclusion() tells the engine not to cull neighbouring faces.
    // The render layer (translucent) is set in ClientEvents.
    // EpoxyBlock emits bubble particles when placed by a fluid reaction.
    // =========================================================================
    public static final DeferredBlock<EpoxyBlock> AMBER_EPOXY_BLOCK =
        BLOCKS.register("amber_epoxy_block", () ->
            new EpoxyBlock(BlockBehaviour.Properties.of()
                .strength(2.5f, 6.0f).sound(SoundType.GLASS)
                .noOcclusion().isSuffocating((s,l,p) -> false).isViewBlocking((s,l,p) -> false)
                .mapColor(MapColor.COLOR_ORANGE)));

    public static final DeferredBlock<EpoxyBlock> COBALT_EPOXY_BLOCK =
        BLOCKS.register("cobalt_epoxy_block", () ->
            new EpoxyBlock(BlockBehaviour.Properties.of()
                .strength(2.5f, 6.0f).sound(SoundType.GLASS)
                .noOcclusion().isSuffocating((s,l,p) -> false).isViewBlocking((s,l,p) -> false)
                .mapColor(MapColor.COLOR_BLUE)));

    public static final DeferredBlock<EpoxyBlock> JADE_EPOXY_BLOCK =
        BLOCKS.register("jade_epoxy_block", () ->
            new EpoxyBlock(BlockBehaviour.Properties.of()
                .strength(2.5f, 6.0f).sound(SoundType.GLASS)
                .noOcclusion().isSuffocating((s,l,p) -> false).isViewBlocking((s,l,p) -> false)
                .mapColor(MapColor.COLOR_GREEN)));

    // =========================================================================
    // Glowing epoxy — transparent + light emission (crafted with glow ink sac)
    // lightLevel 12 matches sea lantern
    // =========================================================================
    public static final DeferredBlock<EpoxyBlock> AMBER_EPOXY_GLOWING =
        BLOCKS.register("amber_epoxy_glowing", () ->
            new EpoxyBlock(BlockBehaviour.Properties.of()
                .strength(2.5f, 6.0f).sound(SoundType.GLASS)
                .noOcclusion().isSuffocating((s,l,p) -> false).isViewBlocking((s,l,p) -> false)
                .lightLevel(state -> 12)
                .mapColor(MapColor.COLOR_ORANGE)));

    public static final DeferredBlock<EpoxyBlock> COBALT_EPOXY_GLOWING =
        BLOCKS.register("cobalt_epoxy_glowing", () ->
            new EpoxyBlock(BlockBehaviour.Properties.of()
                .strength(2.5f, 6.0f).sound(SoundType.GLASS)
                .noOcclusion().isSuffocating((s,l,p) -> false).isViewBlocking((s,l,p) -> false)
                .lightLevel(state -> 12)
                .mapColor(MapColor.COLOR_BLUE)));

    public static final DeferredBlock<EpoxyBlock> JADE_EPOXY_GLOWING =
        BLOCKS.register("jade_epoxy_glowing", () ->
            new EpoxyBlock(BlockBehaviour.Properties.of()
                .strength(2.5f, 6.0f).sound(SoundType.GLASS)
                .noOcclusion().isSuffocating((s,l,p) -> false).isViewBlocking((s,l,p) -> false)
                .lightLevel(state -> 12)
                .mapColor(MapColor.COLOR_GREEN)));

    // =========================================================================
    // Opaque epoxy — solid, no transparency (crafted with squid ink sac)
    // =========================================================================
    public static final DeferredBlock<Block> AMBER_EPOXY_OPAQUE =
        BLOCKS.register("amber_epoxy_opaque", () ->
            new Block(BlockBehaviour.Properties.of()
                .strength(3.0f, 8.0f).sound(SoundType.STONE)
                .mapColor(MapColor.COLOR_ORANGE)));

    public static final DeferredBlock<Block> COBALT_EPOXY_OPAQUE =
        BLOCKS.register("cobalt_epoxy_opaque", () ->
            new Block(BlockBehaviour.Properties.of()
                .strength(3.0f, 8.0f).sound(SoundType.STONE)
                .mapColor(MapColor.COLOR_BLUE)));

    public static final DeferredBlock<Block> JADE_EPOXY_OPAQUE =
        BLOCKS.register("jade_epoxy_opaque", () ->
            new Block(BlockBehaviour.Properties.of()
                .strength(3.0f, 8.0f).sound(SoundType.STONE)
                .mapColor(MapColor.COLOR_GREEN)));
}
