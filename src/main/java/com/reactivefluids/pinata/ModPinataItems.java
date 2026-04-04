package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.core.registries.Registries;
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

    // === Spawn Eggs ===
    public static final DeferredHolder<Item, DeferredSpawnEggItem> WHIRLM_SPAWN_EGG =
            ITEMS.register("whirlm_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.WHIRLM,
                            0xE8A04C,  // Primary color: warm orange-brown (piñata paper)
                            0xFF69B4,  // Secondary color: hot pink (piñata accent)
                            new Item.Properties()));

    // === Candy Items (dropped when piñatas are broken) ===
    public static final DeferredHolder<Item, Item> WHIRLM_CANDY =
            ITEMS.register("whirlm_candy", () ->
                    new Item(new Item.Properties()
                            .food(new net.minecraft.world.food.FoodProperties.Builder()
                                    .nutrition(3)
                                    .saturationModifier(0.4F)
                                    .fast()
                                    .build())));

    // Future candy items for each piñata species...
    // SPARROWMINT_CANDY, FUDGEHOG_CANDY, etc.
}
