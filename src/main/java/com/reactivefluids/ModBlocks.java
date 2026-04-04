package com.reactivefluids;

import com.reactivefluids.pinata.garden.GardenPlotBlock;
import com.reactivefluids.pinata.garden.ProduceBuildingBlock;
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

    // =========================================================================
    // Elephant's Toothpaste — fluid blocks + foam product
    // =========================================================================
    public static final DeferredBlock<TranslucentLiquidBlock> HYDROGEN_PEROXIDE_BLOCK =
        BLOCKS.register("hydrogen_peroxide_block", () ->
            new TranslucentLiquidBlock((FlowingFluid) ModFluids.HYDROGEN_PEROXIDE_SOURCE.get(),
                fluidProps(MapColor.ICE)));

    public static final DeferredBlock<TranslucentLiquidBlock> POTASSIUM_IODIDE_BLOCK =
        BLOCKS.register("potassium_iodide_block", () ->
            new TranslucentLiquidBlock((FlowingFluid) ModFluids.POTASSIUM_IODIDE_SOURCE.get(),
                fluidProps(MapColor.COLOR_BROWN)));

    public static final DeferredBlock<FoamBlock> FOAM_BLOCK =
        BLOCKS.register("foam_block", () ->
            new FoamBlock(BlockBehaviour.Properties.of()
                .strength(0.3f, 0.3f).sound(SoundType.WOOL)
                .mapColor(MapColor.SNOW)));

    // =========================================================================
    // Bioluminescent plankton — glows when entities move through it
    // =========================================================================
    public static final DeferredBlock<PlanktonBlock> PLANKTON_BLOCK =
        BLOCKS.register("plankton_block", () ->
            new PlanktonBlock((FlowingFluid) ModFluids.PLANKTON_SOURCE.get(),
                fluidProps(MapColor.COLOR_BLUE).lightLevel(state ->
                    state.getValue(PlanktonBlock.GLOW) * 3)));

    // =========================================================================
    // Acid — dissolves stone downward, exposes ores
    // =========================================================================
    public static final DeferredBlock<AcidBlock> ACID_BLOCK =
        BLOCKS.register("acid_block", () ->
            new AcidBlock((FlowingFluid) ModFluids.ACID_SOURCE.get(),
                fluidProps(MapColor.COLOR_LIGHT_GREEN)));

    // =========================================================================
    // Crystal solution — grows crystals, crystallizes skeletons
    // =========================================================================
    public static final DeferredBlock<CrystalSolutionBlock> CRYSTAL_SOLUTION_BLOCK =
        BLOCKS.register("crystal_solution_block", () ->
            new CrystalSolutionBlock((FlowingFluid) ModFluids.CRYSTAL_SOLUTION_SOURCE.get(),
                BlockBehaviour.Properties.of()
                    .mapColor(MapColor.ICE)
                    .replaceable()
                    .noCollission()
                    .strength(100.0f)
                    .noLootTable()
                    .liquid()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .sound(SoundType.EMPTY)
                    .randomTicks()));

    public static final DeferredBlock<CrystalBlock> CRYSTAL_BLOCK =
        BLOCKS.register("crystal_block", () -> new CrystalBlock());

    // =========================================================================
    // Arcane Barrier — translucent shimmering dome block for Tiny Hut
    // =========================================================================
    public static final DeferredBlock<ArcaneBarrierBlock> ARCANE_BARRIER =
        BLOCKS.register("arcane_barrier", ArcaneBarrierBlock::new);

    // =========================================================================
    // Return Portal — pocket dimension exit block
    // =========================================================================
    public static final DeferredBlock<ReturnPortalBlock> RETURN_PORTAL =
        BLOCKS.register("return_portal", ReturnPortalBlock::new);

    // =========================================================================
    // Rotten Flesh Block — crafted from 9 rotten flesh, used for Raise Dead
    // =========================================================================
    public static final DeferredBlock<Block> ROTTEN_FLESH_BLOCK =
        BLOCKS.register("rotten_flesh_block", () ->
            new Block(BlockBehaviour.Properties.of()
                .strength(0.5f, 0.5f).sound(SoundType.SLIME_BLOCK)
                .mapColor(MapColor.COLOR_BROWN)));

    // =========================================================================
    // Rainbow indicator — pH-reactive fluid
    // =========================================================================
    public static final DeferredBlock<IndicatorBlock> INDICATOR_BLOCK =
        BLOCKS.register("indicator_block", () ->
            new IndicatorBlock((FlowingFluid) ModFluids.INDICATOR_SOURCE.get(),
                fluidProps(MapColor.COLOR_GREEN)));

    // =========================================================================
    // Garden Plot — Viva Piñata garden center marker
    // =========================================================================
    public static final DeferredBlock<GardenPlotBlock> GARDEN_PLOT =
        BLOCKS.register("garden_plot", () ->
            new GardenPlotBlock(BlockBehaviour.Properties.of()
                .strength(1.0f, 3.0f).sound(SoundType.WOOD)
                .mapColor(MapColor.COLOR_GREEN)));

    // === Produce Buildings ===
    public static final DeferredBlock<ProduceBuildingBlock> HONEY_HIVE =
        BLOCKS.register("honey_hive", () ->
            new ProduceBuildingBlock(BlockBehaviour.Properties.of()
                .strength(2.0f, 4.0f).sound(SoundType.WOOD)
                .mapColor(MapColor.COLOR_YELLOW),
                ProduceBuildingBlock.ProduceType.HONEY_HIVE));

    public static final DeferredBlock<ProduceBuildingBlock> MILKING_SHED =
        BLOCKS.register("milking_shed", () ->
            new ProduceBuildingBlock(BlockBehaviour.Properties.of()
                .strength(2.0f, 4.0f).sound(SoundType.WOOD)
                .mapColor(MapColor.TERRACOTTA_WHITE),
                ProduceBuildingBlock.ProduceType.MILKING_SHED));

    public static final DeferredBlock<ProduceBuildingBlock> SHEARING_SHED =
        BLOCKS.register("shearing_shed", () ->
            new ProduceBuildingBlock(BlockBehaviour.Properties.of()
                .strength(2.0f, 4.0f).sound(SoundType.WOOD)
                .mapColor(MapColor.TERRACOTTA_BROWN),
                ProduceBuildingBlock.ProduceType.SHEARING_SHED));
}
