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

    public static final DeferredHolder<Item, BlockItem> HONEY_HIVE =
            ITEMS.register("honey_hive", () ->
                    new BlockItem(ModBlocks.HONEY_HIVE.get(), new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> MILKING_SHED =
            ITEMS.register("milking_shed", () ->
                    new BlockItem(ModBlocks.MILKING_SHED.get(), new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> SHEARING_SHED =
            ITEMS.register("shearing_shed", () ->
                    new BlockItem(ModBlocks.SHEARING_SHED.get(), new Item.Properties()));

    // === Piñata Houses ===
    public static final DeferredHolder<Item, BlockItem> WHIRLM_HOUSE =
            ITEMS.register("whirlm_house", () -> new BlockItem(ModBlocks.WHIRLM_HOUSE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> SPARROWMINT_HOUSE =
            ITEMS.register("sparrowmint_house", () -> new BlockItem(ModBlocks.SPARROWMINT_HOUSE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> FUDGEHOG_HOUSE =
            ITEMS.register("fudgehog_house", () -> new BlockItem(ModBlocks.FUDGEHOG_HOUSE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> BUNNYCOMB_HOUSE =
            ITEMS.register("bunnycomb_house", () -> new BlockItem(ModBlocks.BUNNYCOMB_HOUSE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> HORSTACHIO_HOUSE =
            ITEMS.register("horstachio_house", () -> new BlockItem(ModBlocks.HORSTACHIO_HOUSE.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> BUZZLEGUM_HOUSE =
            ITEMS.register("buzzlegum_house", () -> new BlockItem(ModBlocks.BUZZLEGUM_HOUSE.get(), new Item.Properties()));

    // === NPC & Threat Spawn Eggs ===
    public static final DeferredHolder<Item, DeferredSpawnEggItem> SEEDOS_SPAWN_EGG =
            ITEMS.register("seedos_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.SEEDOS,
                            0x4CAF50, 0x8BC34A, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> RUFFIAN_SPAWN_EGG =
            ITEMS.register("ruffian_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.RUFFIAN,
                            0x505050, 0x8B0000, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> DASTARDOS_SPAWN_EGG =
            ITEMS.register("dastardos_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.DASTARDOS,
                            0x200020, 0x800080, new Item.Properties()));

    // === Tools ===
    public static final DeferredHolder<Item, GardenShovelItem> GARDEN_SHOVEL =
            ITEMS.register("garden_shovel", () ->
                    new GardenShovelItem(new Item.Properties().durability(500).stacksTo(1)));

    public static final DeferredHolder<Item, WateringCanItem> WATERING_CAN =
            ITEMS.register("watering_can", () ->
                    new WateringCanItem(new Item.Properties().stacksTo(1)));

    // === Batch 5: Spawn Eggs + Candy ===
    public static final DeferredHolder<Item, DeferredSpawnEggItem> SQUAZZIL_SPAWN_EGG =
            ITEMS.register("squazzil_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.SQUAZZIL, 0x8B6914, 0xD4A06A, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> SWEETOOTH_SPAWN_EGG =
            ITEMS.register("sweetooth_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.SWEETOOTH, 0xC88040, 0xE0C090, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> MALLOWOLF_SPAWN_EGG =
            ITEMS.register("mallowolf_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.MALLOWOLF, 0xE8E8F0, 0x8090B0, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> COCOADILE_SPAWN_EGG =
            ITEMS.register("cocoadile_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.COCOADILE, 0x5C3A1E, 0x8B5E3C, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> DRAGONACHE_SPAWN_EGG =
            ITEMS.register("dragonache_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.DRAGONACHE, 0xC040FF, 0xFFD700, new Item.Properties()));

    public static final DeferredHolder<Item, Item> SQUAZZIL_CANDY =
            ITEMS.register("squazzil_candy", () -> new Item(new Item.Properties().food(candyFood(3, 0.4F))));
    public static final DeferredHolder<Item, Item> SWEETOOTH_CANDY =
            ITEMS.register("sweetooth_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.6F))));
    public static final DeferredHolder<Item, Item> MALLOWOLF_CANDY =
            ITEMS.register("mallowolf_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.6F))));
    public static final DeferredHolder<Item, Item> COCOADILE_CANDY =
            ITEMS.register("cocoadile_candy", () -> new Item(new Item.Properties().food(candyFood(6, 0.7F))));
    public static final DeferredHolder<Item, Item> DRAGONACHE_CANDY =
            ITEMS.register("dragonache_candy", () -> new Item(new Item.Properties().food(candyFood(8, 1.0F))));

    // === Batch 4: Spawn Eggs ===
    public static final DeferredHolder<Item, DeferredSpawnEggItem> BARKBARK_SPAWN_EGG =
            ITEMS.register("barkbark_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.BARKBARK, 0x8B5A2B, 0xD2A06D, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> KITTYFLOSS_SPAWN_EGG =
            ITEMS.register("kittyfloss_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.KITTYFLOSS, 0xFFB6C1, 0xFF69B4, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> GOOBAA_SPAWN_EGG =
            ITEMS.register("goobaa_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.GOOBAA, 0xF0F0F0, 0xE0D0C0, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> RASHBERRY_SPAWN_EGG =
            ITEMS.register("rashberry_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.RASHBERRY, 0xE87090, 0xC04060, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> DOENUT_SPAWN_EGG =
            ITEMS.register("doenut_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.DOENUT, 0xC09060, 0xF0E0D0, new Item.Properties()));

    // === Batch 4: Candy ===
    public static final DeferredHolder<Item, Item> BARKBARK_CANDY =
            ITEMS.register("barkbark_candy", () -> new Item(new Item.Properties().food(candyFood(4, 0.5F))));
    public static final DeferredHolder<Item, Item> KITTYFLOSS_CANDY =
            ITEMS.register("kittyfloss_candy", () -> new Item(new Item.Properties().food(candyFood(4, 0.5F))));
    public static final DeferredHolder<Item, Item> GOOBAA_CANDY =
            ITEMS.register("goobaa_candy", () -> new Item(new Item.Properties().food(candyFood(4, 0.5F))));
    public static final DeferredHolder<Item, Item> RASHBERRY_CANDY =
            ITEMS.register("rashberry_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.6F))));
    public static final DeferredHolder<Item, Item> DOENUT_CANDY =
            ITEMS.register("doenut_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.6F))));

    // === Currency ===
    public static final DeferredHolder<Item, ChocolateCoinItem> CHOCOLATE_COIN =
            ITEMS.register("chocolate_coin", () ->
                    new ChocolateCoinItem(new Item.Properties().stacksTo(64)));

    // === Accessories (a selection — more can be added) ===
    public static final DeferredHolder<Item, AccessoryItem> TOP_HAT =
            ITEMS.register("top_hat", () ->
                    new AccessoryItem(new Item.Properties().stacksTo(1), "top_hat"));
    public static final DeferredHolder<Item, AccessoryItem> CROWN =
            ITEMS.register("crown", () ->
                    new AccessoryItem(new Item.Properties().stacksTo(1), "crown"));
    public static final DeferredHolder<Item, AccessoryItem> HALO_OF_HARDNESS =
            ITEMS.register("halo_of_hardness", () ->
                    new AccessoryItem(new Item.Properties().stacksTo(1), "halo_of_hardness"));
    public static final DeferredHolder<Item, AccessoryItem> KEEPER_HAT =
            ITEMS.register("keeper_hat", () ->
                    new AccessoryItem(new Item.Properties().stacksTo(1), "keeper_hat"));
    public static final DeferredHolder<Item, AccessoryItem> RUNNING_SHOES =
            ITEMS.register("running_shoes", () ->
                    new AccessoryItem(new Item.Properties().stacksTo(1), "running_shoes"));
    public static final DeferredHolder<Item, AccessoryItem> BOW_TIE =
            ITEMS.register("bow_tie", () ->
                    new AccessoryItem(new Item.Properties().stacksTo(1), "bow_tie"));

    // === Surface Packets (infinite use terrain painters) ===
    public static final DeferredHolder<Item, SurfacePacketItem> GRASS_PACKET =
            ITEMS.register("grass_packet", () ->
                    new SurfacePacketItem(new Item.Properties(),
                            SurfacePacketItem.SurfaceType.GRASS));

    public static final DeferredHolder<Item, SurfacePacketItem> LONG_GRASS_PACKET =
            ITEMS.register("long_grass_packet", () ->
                    new SurfacePacketItem(new Item.Properties(),
                            SurfacePacketItem.SurfaceType.LONG_GRASS));

    public static final DeferredHolder<Item, SurfacePacketItem> SAND_PACKET =
            ITEMS.register("sand_packet", () ->
                    new SurfacePacketItem(new Item.Properties(),
                            SurfacePacketItem.SurfaceType.SAND));

    public static final DeferredHolder<Item, SurfacePacketItem> SNOW_PACKET =
            ITEMS.register("snow_packet", () ->
                    new SurfacePacketItem(new Item.Properties(),
                            SurfacePacketItem.SurfaceType.SNOW));
}
