package com.reactivefluids.pinata;

import com.reactivefluids.ModBlocks;
import com.reactivefluids.ReactiveFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Items for the Viva Piñata system — spawn eggs, candy, and special items.
 */
public class ModPinataItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ReactiveFluids.MOD_ID);

    // Shared candy food properties
    private static FoodProperties candyFood(int nutrition, float saturation) {
        return new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .fast()
                .build();
    }

    // === Spawn Eggs ===
    public static final DeferredHolder<Item, DeferredSpawnEggItem> WHIRLM_SPAWN_EGG =
            ITEMS.register("whirlm_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.WHIRLM,
                            0xDC508C, 0xF08CB4,  // Pink body / light pink accent
                            new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> SPARROWMINT_SPAWN_EGG =
            ITEMS.register("sparrowmint_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.SPARROWMINT,
                            0x7BC86C, 0xC8E6A0,  // Mint green / light green
                            new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> FUDGEHOG_SPAWN_EGG =
            ITEMS.register("fudgehog_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.FUDGEHOG,
                            0x8B5E3C, 0xD4A056,  // Chocolate brown / caramel
                            new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> MOUSEMALLOW_SPAWN_EGG =
            ITEMS.register("mousemallow_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.MOUSEMALLOW,
                            0xF5E0E8, 0xFFB6D9,  // White-pink / marshmallow pink
                            new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> SYRUPENT_SPAWN_EGG =
            ITEMS.register("syrupent_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.SYRUPENT,
                            0xD4960A, 0x8B6508,  // Golden amber / dark amber
                            new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> TAFFLY_SPAWN_EGG =
            ITEMS.register("taffly_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.TAFFLY,
                            0xC87828, 0xE8D0A0,  // Toffee brown / light toffee
                            new Item.Properties()));

    // === Candy Items ===
    public static final DeferredHolder<Item, Item> WHIRLM_CANDY =
            ITEMS.register("whirlm_candy", () ->
                    new Item(new Item.Properties().food(candyFood(3, 0.4F))));

    public static final DeferredHolder<Item, Item> SPARROWMINT_CANDY =
            ITEMS.register("sparrowmint_candy", () ->
                    new Item(new Item.Properties().food(candyFood(4, 0.5F))));

    public static final DeferredHolder<Item, Item> FUDGEHOG_CANDY =
            ITEMS.register("fudgehog_candy", () ->
                    new Item(new Item.Properties().food(candyFood(4, 0.5F))));

    public static final DeferredHolder<Item, Item> MOUSEMALLOW_CANDY =
            ITEMS.register("mousemallow_candy", () ->
                    new Item(new Item.Properties().food(candyFood(3, 0.3F))));

    public static final DeferredHolder<Item, Item> SYRUPENT_CANDY =
            ITEMS.register("syrupent_candy", () ->
                    new Item(new Item.Properties().food(candyFood(5, 0.6F))));

    public static final DeferredHolder<Item, Item> TAFFLY_CANDY =
            ITEMS.register("taffly_candy", () ->
                    new Item(new Item.Properties().food(candyFood(2, 0.3F))));

    // === Batch 3: Spawn Eggs ===
    public static final DeferredHolder<Item, DeferredSpawnEggItem> BUNNYCOMB_SPAWN_EGG =
            ITEMS.register("bunnycomb_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.BUNNYCOMB,
                            0xE8C850, 0xF0A030, new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> QUACKBERRY_SPAWN_EGG =
            ITEMS.register("quackberry_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.QUACKBERRY,
                            0x4060D0, 0x8060C0, new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> SHELLYBEAN_SPAWN_EGG =
            ITEMS.register("shellybean_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.SHELLYBEAN,
                            0x90D8A0, 0xF0C0D0, new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> NEWTGAT_SPAWN_EGG =
            ITEMS.register("newtgat_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.NEWTGAT,
                            0xC08040, 0xE0A060, new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> LICKATOAD_SPAWN_EGG =
            ITEMS.register("lickatoad_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.LICKATOAD,
                            0x40C040, 0x80E060, new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> PRETZTAIL_SPAWN_EGG =
            ITEMS.register("pretztail_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.PRETZTAIL,
                            0xD06020, 0x8B4513, new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> BUZZLEGUM_SPAWN_EGG =
            ITEMS.register("buzzlegum_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.BUZZLEGUM,
                            0xF0D040, 0x202020, new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> CLUCKLES_SPAWN_EGG =
            ITEMS.register("cluckles_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.CLUCKLES,
                            0xD0A070, 0xC03020, new Item.Properties()));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> HORSTACHIO_SPAWN_EGG =
            ITEMS.register("horstachio_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.HORSTACHIO,
                            0x80B060, 0xC8E0A0, new Item.Properties()));

    // === Batch 3: Candy ===
    public static final DeferredHolder<Item, Item> BUNNYCOMB_CANDY =
            ITEMS.register("bunnycomb_candy", () ->
                    new Item(new Item.Properties().food(candyFood(4, 0.5F))));

    public static final DeferredHolder<Item, Item> QUACKBERRY_CANDY =
            ITEMS.register("quackberry_candy", () ->
                    new Item(new Item.Properties().food(candyFood(4, 0.4F))));

    public static final DeferredHolder<Item, Item> SHELLYBEAN_CANDY =
            ITEMS.register("shellybean_candy", () ->
                    new Item(new Item.Properties().food(candyFood(3, 0.4F))));

    public static final DeferredHolder<Item, Item> NEWTGAT_CANDY =
            ITEMS.register("newtgat_candy", () ->
                    new Item(new Item.Properties().food(candyFood(3, 0.4F))));

    public static final DeferredHolder<Item, Item> LICKATOAD_CANDY =
            ITEMS.register("lickatoad_candy", () ->
                    new Item(new Item.Properties().food(candyFood(4, 0.5F))));

    public static final DeferredHolder<Item, Item> PRETZTAIL_CANDY =
            ITEMS.register("pretztail_candy", () ->
                    new Item(new Item.Properties().food(candyFood(5, 0.6F))));

    public static final DeferredHolder<Item, Item> BUZZLEGUM_CANDY =
            ITEMS.register("buzzlegum_candy", () ->
                    new Item(new Item.Properties().food(candyFood(4, 0.5F))));

    public static final DeferredHolder<Item, Item> CLUCKLES_CANDY =
            ITEMS.register("cluckles_candy", () ->
                    new Item(new Item.Properties().food(candyFood(3, 0.4F))));

    public static final DeferredHolder<Item, Item> HORSTACHIO_CANDY =
            ITEMS.register("horstachio_candy", () ->
                    new Item(new Item.Properties().food(candyFood(6, 0.7F))));

    // === Garden Items ===
    public static final DeferredHolder<Item, BlockItem> GARDEN_PLOT =
            ITEMS.register("garden_plot", () ->
                    new BlockItem(ModBlocks.GARDEN_PLOT.get(), new Item.Properties()));
}
