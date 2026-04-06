package com.reactivefluids;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(ReactiveFluids.MOD_ID);

    // =========================================================================
    // Buckets
    // =========================================================================
    public static final DeferredItem<BucketItem> AMBER_RESIN_BUCKET =
        ITEMS.register("amber_resin_bucket", () ->
            new BucketItem(ModFluids.AMBER_RESIN_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final DeferredItem<HardenerItem> AMBER_HARDENER =
        ITEMS.register("amber_hardener", () -> new HardenerItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<BucketItem> COBALT_RESIN_BUCKET =
        ITEMS.register("cobalt_resin_bucket", () ->
            new BucketItem(ModFluids.COBALT_RESIN_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final DeferredItem<HardenerItem> COBALT_HARDENER =
        ITEMS.register("cobalt_hardener", () -> new HardenerItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<BucketItem> JADE_RESIN_BUCKET =
        ITEMS.register("jade_resin_bucket", () ->
            new BucketItem(ModFluids.JADE_RESIN_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final DeferredItem<HardenerItem> JADE_HARDENER =
        ITEMS.register("jade_hardener", () -> new HardenerItem(new Item.Properties().stacksTo(16)));

    // =========================================================================
    // Glowing resin buckets (resin + glow ink sac)
    // =========================================================================
    public static final DeferredItem<BucketItem> AMBER_GLOWING_RESIN_BUCKET =
        ITEMS.register("amber_glowing_resin_bucket", () ->
            new BucketItem(ModFluids.AMBER_GLOWING_RESIN_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final DeferredItem<BucketItem> COBALT_GLOWING_RESIN_BUCKET =
        ITEMS.register("cobalt_glowing_resin_bucket", () ->
            new BucketItem(ModFluids.COBALT_GLOWING_RESIN_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final DeferredItem<BucketItem> JADE_GLOWING_RESIN_BUCKET =
        ITEMS.register("jade_glowing_resin_bucket", () ->
            new BucketItem(ModFluids.JADE_GLOWING_RESIN_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    // =========================================================================
    // Transparent epoxy block items
    // =========================================================================
    public static final DeferredItem<BlockItem> AMBER_EPOXY_BLOCK_ITEM =
        ITEMS.register("amber_epoxy_block", () ->
            new BlockItem(ModBlocks.AMBER_EPOXY_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> COBALT_EPOXY_BLOCK_ITEM =
        ITEMS.register("cobalt_epoxy_block", () ->
            new BlockItem(ModBlocks.COBALT_EPOXY_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> JADE_EPOXY_BLOCK_ITEM =
        ITEMS.register("jade_epoxy_block", () ->
            new BlockItem(ModBlocks.JADE_EPOXY_BLOCK.get(), new Item.Properties()));

    // =========================================================================
    // Glowing epoxy block items
    // =========================================================================
    public static final DeferredItem<BlockItem> AMBER_EPOXY_GLOWING_ITEM =
        ITEMS.register("amber_epoxy_glowing", () ->
            new BlockItem(ModBlocks.AMBER_EPOXY_GLOWING.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> COBALT_EPOXY_GLOWING_ITEM =
        ITEMS.register("cobalt_epoxy_glowing", () ->
            new BlockItem(ModBlocks.COBALT_EPOXY_GLOWING.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> JADE_EPOXY_GLOWING_ITEM =
        ITEMS.register("jade_epoxy_glowing", () ->
            new BlockItem(ModBlocks.JADE_EPOXY_GLOWING.get(), new Item.Properties()));

    // =========================================================================
    // Opaque epoxy block items
    // =========================================================================
    public static final DeferredItem<BlockItem> AMBER_EPOXY_OPAQUE_ITEM =
        ITEMS.register("amber_epoxy_opaque", () ->
            new BlockItem(ModBlocks.AMBER_EPOXY_OPAQUE.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> COBALT_EPOXY_OPAQUE_ITEM =
        ITEMS.register("cobalt_epoxy_opaque", () ->
            new BlockItem(ModBlocks.COBALT_EPOXY_OPAQUE.get(), new Item.Properties()));

    public static final DeferredItem<BlockItem> JADE_EPOXY_OPAQUE_ITEM =
        ITEMS.register("jade_epoxy_opaque", () ->
            new BlockItem(ModBlocks.JADE_EPOXY_OPAQUE.get(), new Item.Properties()));

    // =========================================================================
    // Elephant's Toothpaste
    // =========================================================================
    public static final DeferredItem<BucketItem> HYDROGEN_PEROXIDE_BUCKET =
        ITEMS.register("hydrogen_peroxide_bucket", () ->
            new BucketItem(ModFluids.HYDROGEN_PEROXIDE_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final DeferredItem<BucketItem> POTASSIUM_IODIDE_BUCKET =
        ITEMS.register("potassium_iodide_bucket", () ->
            new BucketItem(ModFluids.POTASSIUM_IODIDE_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final DeferredItem<BlockItem> FOAM_BLOCK_ITEM =
        ITEMS.register("foam_block", () ->
            new BlockItem(ModBlocks.FOAM_BLOCK.get(), new Item.Properties()));

    // =========================================================================
    // Bioluminescent Plankton
    // =========================================================================
    public static final DeferredItem<BucketItem> PLANKTON_BUCKET =
        ITEMS.register("plankton_bucket", () ->
            new BucketItem(ModFluids.PLANKTON_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    // =========================================================================
    // Acid
    // =========================================================================
    public static final DeferredItem<BucketItem> ACID_BUCKET =
        ITEMS.register("acid_bucket", () ->
            new BucketItem(ModFluids.ACID_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    // =========================================================================
    // Crystal Solution
    // =========================================================================
    public static final DeferredItem<BucketItem> CRYSTAL_SOLUTION_BUCKET =
        ITEMS.register("crystal_solution_bucket", () ->
            new BucketItem(ModFluids.CRYSTAL_SOLUTION_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final DeferredItem<BlockItem> CRYSTAL_BLOCK_ITEM =
        ITEMS.register("crystal_block", () ->
            new BlockItem(ModBlocks.CRYSTAL_BLOCK.get(), new Item.Properties()));

    // =========================================================================
    // Rainbow Indicator
    // =========================================================================
    public static final DeferredItem<BucketItem> INDICATOR_BUCKET =
        ITEMS.register("indicator_bucket", () ->
            new BucketItem(ModFluids.INDICATOR_SOURCE.get(),
                new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)));

    public static final DeferredItem<ReagentItem> ACID_REAGENT =
        ITEMS.register("acid_reagent", () -> new ReagentItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<ReagentItem> BASE_REAGENT =
        ITEMS.register("base_reagent", () -> new ReagentItem(new Item.Properties().stacksTo(16)));

    // =========================================================================
    // Rotten Flesh Block item
    // =========================================================================
    public static final DeferredItem<BlockItem> ROTTEN_FLESH_BLOCK_ITEM =
        ITEMS.register("rotten_flesh_block", () ->
            new BlockItem(ModBlocks.ROTTEN_FLESH_BLOCK.get(), new Item.Properties()));

    // =========================================================================
    // Spell Scrolls
    // =========================================================================
    public static final DeferredItem<TowerScrollItem> TOWER_SCROLL =
        ITEMS.register("tower_scroll", () -> new TowerScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<SteedScrollItem> STEED_SCROLL =
        ITEMS.register("steed_scroll", () -> new SteedScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<DancingLightsScrollItem> DANCING_LIGHTS_SCROLL =
        ITEMS.register("dancing_lights_scroll", () -> new DancingLightsScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<DisintegrateScrollItem> DISINTEGRATE_SCROLL =
        ITEMS.register("disintegrate_scroll", () -> new DisintegrateScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<MoldEarthScrollItem> MOLD_EARTH_SCROLL =
        ITEMS.register("mold_earth_scroll", () -> new MoldEarthScrollItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<WallOfStoneScrollItem> WALL_OF_STONE_SCROLL =
        ITEMS.register("wall_of_stone_scroll", () -> new WallOfStoneScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<PasswallScrollItem> PASSWALL_SCROLL =
        ITEMS.register("passwall_scroll", () -> new PasswallScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<DimensionDoorScrollItem> DIMENSION_DOOR_SCROLL =
        ITEMS.register("dimension_door_scroll", () -> new DimensionDoorScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<ConjureAnimalsScrollItem> CONJURE_ANIMALS_SCROLL =
        ITEMS.register("conjure_animals_scroll", () -> new ConjureAnimalsScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<ReverseGravityScrollItem> REVERSE_GRAVITY_SCROLL =
        ITEMS.register("reverse_gravity_scroll", () -> new ReverseGravityScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<PlantGrowthScrollItem> PLANT_GROWTH_SCROLL =
        ITEMS.register("plant_growth_scroll", () -> new PlantGrowthScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<FogCloudScrollItem> FOG_CLOUD_SCROLL =
        ITEMS.register("fog_cloud_scroll", () -> new FogCloudScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<EruptingEarthScrollItem> ERUPTING_EARTH_SCROLL =
        ITEMS.register("erupting_earth_scroll", () -> new EruptingEarthScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<TinyHutScrollItem> TINY_HUT_SCROLL =
        ITEMS.register("tiny_hut_scroll", () -> new TinyHutScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<BonesOfTheEarthScrollItem> BONES_OF_THE_EARTH_SCROLL =
        ITEMS.register("bones_of_the_earth_scroll", () -> new BonesOfTheEarthScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<MoveEarthScrollItem> MOVE_EARTH_SCROLL =
        ITEMS.register("move_earth_scroll", () -> new MoveEarthScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<ArcaneGateScrollItem> ARCANE_GATE_SCROLL =
        ITEMS.register("arcane_gate_scroll", () -> new ArcaneGateScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<MeteorSwarmScrollItem> METEOR_SWARM_SCROLL =
        ITEMS.register("meteor_swarm_scroll", () -> new MeteorSwarmScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<ControlWaterScrollItem> CONTROL_WATER_SCROLL =
        ITEMS.register("control_water_scroll", () -> new ControlWaterScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<MagnificentMansionScrollItem> MAGNIFICENT_MANSION_SCROLL =
        ITEMS.register("magnificent_mansion_scroll", () -> new MagnificentMansionScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<GongersGrottoScrollItem> GONGERS_GROTTO_SCROLL =
        ITEMS.register("gongers_grotto_scroll", () -> new GongersGrottoScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<RaiseDeadScrollItem> RAISE_DEAD_SCROLL =
        ITEMS.register("raise_dead_scroll", () -> new RaiseDeadScrollItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<FletchersFireballScrollItem> FLETCHERS_FIREBALL_SCROLL =
        ITEMS.register("fletchers_fireball_scroll", () -> new FletchersFireballScrollItem(new Item.Properties().stacksTo(1)));
}
