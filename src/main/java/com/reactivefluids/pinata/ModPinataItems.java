package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
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
}
