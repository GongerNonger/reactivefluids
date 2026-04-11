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
    // Glowing water — transient block left behind by bioluminescent entities.
    // Visually invisible (identical to water when waterlogged), emits light
    // based on GLOW property, fades back to plain water via scheduled ticks.
    // =========================================================================
    public static final DeferredBlock<GlowingWaterBlock> GLOWING_WATER =
        BLOCKS.register("glowing_water", () ->
            new GlowingWaterBlock(BlockBehaviour.Properties.of()
                .noCollission()
                .noOcclusion()
                .noLootTable()
                .replaceable()
                .strength(100f)
                .sound(SoundType.EMPTY)
                .mapColor(MapColor.COLOR_BLUE)
                .lightLevel(state -> state.getValue(GlowingWaterBlock.GLOW) * 3)));

    // =========================================================================
    // Acid — dissolves stone downward, exposes ores
    // =========================================================================
    public static final DeferredBlock<AcidBlock> ACID_BLOCK =
        BLOCKS.register("acid_block", () ->
            new AcidBlock((FlowingFluid) ModFluids.ACID_SOURCE.get(),
                fluidProps(MapColor.COLOR_LIGHT_GREEN)));

    // Liquid Nitrogen — freezes water/lava, slows entities
    // =========================================================================
    public static final DeferredBlock<LiquidNitrogenBlock> LIQUID_NITROGEN_BLOCK =
        BLOCKS.register("liquid_nitrogen_block", () ->
            new LiquidNitrogenBlock((FlowingFluid) ModFluids.LIQUID_NITROGEN_SOURCE.get(),
                fluidProps(MapColor.ICE)));

    // =========================================================================
    // Greek Fire — sticky burning fluid, water makes it spread
    // =========================================================================
    public static final DeferredBlock<GreekFireBlock> GREEK_FIRE_BLOCK =
        BLOCKS.register("greek_fire_block", () ->
            new GreekFireBlock((FlowingFluid) ModFluids.GREEK_FIRE_SOURCE.get(),
                fluidProps(MapColor.COLOR_ORANGE).lightLevel(state -> 12)));

    // =========================================================================
    // Ferrofluid — responds to redstone, solidifies into iron blocks
    // =========================================================================
    public static final DeferredBlock<FerrofluidBlock> FERROFLUID_BLOCK =
        BLOCKS.register("ferrofluid_block", () ->
            new FerrofluidBlock((FlowingFluid) ModFluids.FERROFLUID_SOURCE.get(),
                fluidProps(MapColor.COLOR_BLACK)));

    // =========================================================================
    // Superfluid (Helium-3) — zero friction, climbs walls
    // =========================================================================
    public static final DeferredBlock<SuperfluidBlock> SUPERFLUID_BLOCK =
        BLOCKS.register("superfluid_block", () ->
            new SuperfluidBlock((FlowingFluid) ModFluids.SUPERFLUID_SOURCE.get(),
                fluidProps(MapColor.ICE)));

    // =========================================================================
    // Mycelium Slurry — spreads mushrooms, reacts with bone meal
    // =========================================================================
    public static final DeferredBlock<MyceliumSlurryBlock> MYCELIUM_SLURRY_BLOCK =
        BLOCKS.register("mycelium_slurry_block", () ->
            new MyceliumSlurryBlock((FlowingFluid) ModFluids.MYCELIUM_SLURRY_SOURCE.get(),
                fluidProps(MapColor.COLOR_PURPLE)));

    // =========================================================================
    // Custom Mushroom Blocks — grown by Mycelium Slurry
    // =========================================================================
    private static BlockBehaviour.Properties mushroomProps() {
        return BlockBehaviour.Properties.of()
                .noCollission().instabreak()
                .sound(SoundType.GRASS)
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }

    public static final DeferredBlock<GhostFungusBlock> GHOST_FUNGUS =
        BLOCKS.register("ghost_fungus", () ->
            new GhostFungusBlock(mushroomProps().lightLevel(state -> 8)
                .mapColor(MapColor.TERRACOTTA_WHITE)));

    public static final DeferredBlock<IndigoMilkCapBlock> INDIGO_MILK_CAP =
        BLOCKS.register("indigo_milk_cap", () ->
            new IndigoMilkCapBlock(mushroomProps().mapColor(MapColor.COLOR_BLUE)));

    public static final DeferredBlock<BleedingToothBlock> BLEEDING_TOOTH =
        BLOCKS.register("bleeding_tooth", () ->
            new BleedingToothBlock(mushroomProps().mapColor(MapColor.TERRACOTTA_WHITE)));

    public static final DeferredBlock<AmethystDeceiverBlock> AMETHYST_DECEIVER =
        BLOCKS.register("amethyst_deceiver", () ->
            new AmethystDeceiverBlock(mushroomProps().mapColor(MapColor.COLOR_PURPLE)));

    public static final DeferredBlock<LionsManeBlock> LIONS_MANE =
        BLOCKS.register("lions_mane", () ->
            new LionsManeBlock(mushroomProps().mapColor(MapColor.SNOW)));

    public static final DeferredBlock<DevilsCigarBlock> DEVILS_CIGAR =
        BLOCKS.register("devils_cigar", () ->
            new DevilsCigarBlock(mushroomProps().mapColor(MapColor.COLOR_BROWN)));

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
    // Lava Lamps — decorative glowing blocks with rising blob particles
    // =========================================================================
    private static BlockBehaviour.Properties lavaLampProps(MapColor color) {
        return BlockBehaviour.Properties.of()
                .strength(0.5f, 0.5f).sound(SoundType.GLASS)
                .noOcclusion().isSuffocating((s,l,p) -> false).isViewBlocking((s,l,p) -> false)
                .lightLevel(state -> 12).mapColor(color);
    }

    public static final DeferredBlock<LavaLampBlock> LAVA_LAMP_RED =
        BLOCKS.register("lava_lamp_red", () -> new LavaLampBlock(lavaLampProps(MapColor.COLOR_RED), 0xFF3020));

    public static final DeferredBlock<LavaLampBlock> LAVA_LAMP_BLUE =
        BLOCKS.register("lava_lamp_blue", () -> new LavaLampBlock(lavaLampProps(MapColor.COLOR_BLUE), 0x2080FF));

    public static final DeferredBlock<LavaLampBlock> LAVA_LAMP_GREEN =
        BLOCKS.register("lava_lamp_green", () -> new LavaLampBlock(lavaLampProps(MapColor.COLOR_GREEN), 0x20FF60));

    public static final DeferredBlock<LavaLampBlock> LAVA_LAMP_PURPLE =
        BLOCKS.register("lava_lamp_purple", () -> new LavaLampBlock(lavaLampProps(MapColor.COLOR_PURPLE), 0xA030FF));
}
