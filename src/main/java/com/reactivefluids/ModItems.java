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
}
