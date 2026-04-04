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
    public static final DeferredHolder<Item, DeferredSpawnEggItem> STORKOS_SPAWN_EGG =
            ITEMS.register("storkos_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.STORKOS, 0xFFFFFF, 0xFF6060, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> DOC_PATCHINGO_SPAWN_EGG =
            ITEMS.register("doc_patchingo_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.DOC_PATCHINGO, 0xFFFFFF, 0x40C040, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> PROFESSOR_PESTER_SPAWN_EGG =
            ITEMS.register("professor_pester_spawn_egg", () ->
                    new DeferredSpawnEggItem(ModPinataEntities.PROFESSOR_PESTER, 0x400040, 0xFF0000, new Item.Properties()));
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

    // === Batches 7-9: Spawn Eggs + Candy ===
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CANDARY_SPAWN_EGG = ITEMS.register("candary_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.CANDARY, 0xFFFF40, 0xFFD700, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CANDARY_CANDY = ITEMS.register("candary_candy", () -> new Item(new Item.Properties().food(candyFood(4, 0.4F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> PARMADILLO_SPAWN_EGG = ITEMS.register("parmadillo_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.PARMADILLO, 0xC89060, 0x808080, new Item.Properties()));
    public static final DeferredHolder<Item, Item> PARMADILLO_CANDY = ITEMS.register("parmadillo_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.5F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> ZUMBUG_SPAWN_EGG = ITEMS.register("zumbug_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.ZUMBUG, 0x202020, 0xF0F0F0, new Item.Properties()));
    public static final DeferredHolder<Item, Item> ZUMBUG_CANDY = ITEMS.register("zumbug_candy", () -> new Item(new Item.Properties().food(candyFood(6, 0.6F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> PIEENA_SPAWN_EGG = ITEMS.register("pieena_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.PIEENA, 0xC8A060, 0x606060, new Item.Properties()));
    public static final DeferredHolder<Item, Item> PIEENA_CANDY = ITEMS.register("pieena_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.5F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> JUICYGOOSE_SPAWN_EGG = ITEMS.register("juicygoose_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.JUICYGOOSE, 0x80C040, 0xF0F0F0, new Item.Properties()));
    public static final DeferredHolder<Item, Item> JUICYGOOSE_CANDY = ITEMS.register("juicygoose_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.5F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> SALAMANGO_SPAWN_EGG = ITEMS.register("salamango_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.SALAMANGO, 0xFF6020, 0xFFD040, new Item.Properties()));
    public static final DeferredHolder<Item, Item> SALAMANGO_CANDY = ITEMS.register("salamango_candy", () -> new Item(new Item.Properties().food(candyFood(4, 0.4F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> REDDHOTT_SPAWN_EGG = ITEMS.register("reddhott_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.REDDHOTT, 0xFF2020, 0xFF8040, new Item.Properties()));
    public static final DeferredHolder<Item, Item> REDDHOTT_CANDY = ITEMS.register("reddhott_candy", () -> new Item(new Item.Properties().food(candyFood(3, 0.3F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CHOCSTRICH_SPAWN_EGG = ITEMS.register("chocstrich_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.CHOCSTRICH, 0x5C3A1E, 0xF0E0D0, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CHOCSTRICH_CANDY = ITEMS.register("chocstrich_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.5F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> MOOJOO_SPAWN_EGG = ITEMS.register("moojoo_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.MOOJOO, 0x8B5E3C, 0xFFFFE0, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MOOJOO_CANDY = ITEMS.register("moojoo_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.5F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CINNAMONKEY_SPAWN_EGG = ITEMS.register("cinnamonkey_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.CINNAMONKEY, 0xC08040, 0xE0C0A0, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CINNAMONKEY_CANDY = ITEMS.register("cinnamonkey_candy", () -> new Item(new Item.Properties().food(candyFood(4, 0.4F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> SARSGORILLA_SPAWN_EGG = ITEMS.register("sarsgorilla_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.SARSGORILLA, 0x404040, 0x808080, new Item.Properties()));
    public static final DeferredHolder<Item, Item> SARSGORILLA_CANDY = ITEMS.register("sarsgorilla_candy", () -> new Item(new Item.Properties().food(candyFood(7, 0.7F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CAMELLO_SPAWN_EGG = ITEMS.register("camello_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.CAMELLO, 0xD4A060, 0xF0E0C0, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CAMELLO_CANDY = ITEMS.register("camello_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.5F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> PENGUM_SPAWN_EGG = ITEMS.register("pengum_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.PENGUM, 0x202040, 0xF0F0FF, new Item.Properties()));
    public static final DeferredHolder<Item, Item> PENGUM_CANDY = ITEMS.register("pengum_candy", () -> new Item(new Item.Properties().food(candyFood(4, 0.4F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> WALRUSK_SPAWN_EGG = ITEMS.register("walrusk_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.WALRUSK, 0x8B7D6B, 0xF0E0D0, new Item.Properties()));
    public static final DeferredHolder<Item, Item> WALRUSK_CANDY = ITEMS.register("walrusk_candy", () -> new Item(new Item.Properties().food(candyFood(6, 0.6F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> POLOLLYBEAR_SPAWN_EGG = ITEMS.register("polollybear_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.POLOLLYBEAR, 0xF0F0FF, 0xA0D0FF, new Item.Properties()));
    public static final DeferredHolder<Item, Item> POLOLLYBEAR_CANDY = ITEMS.register("polollybear_candy", () -> new Item(new Item.Properties().food(candyFood(7, 0.7F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> FIZZLYBEAR_SPAWN_EGG = ITEMS.register("fizzlybear_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.FIZZLYBEAR, 0x8B5E3C, 0xD4A06D, new Item.Properties()));
    public static final DeferredHolder<Item, Item> FIZZLYBEAR_CANDY = ITEMS.register("fizzlybear_candy", () -> new Item(new Item.Properties().food(candyFood(6, 0.6F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> LIMEOCEROS_SPAWN_EGG = ITEMS.register("limeoceros_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.LIMEOCEROS, 0x80C040, 0x404040, new Item.Properties()));
    public static final DeferredHolder<Item, Item> LIMEOCEROS_CANDY = ITEMS.register("limeoceros_candy", () -> new Item(new Item.Properties().food(candyFood(7, 0.7F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> PIGXIE_SPAWN_EGG = ITEMS.register("pigxie_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.PIGXIE, 0xFFB6C1, 0xE0A0FF, new Item.Properties()));
    public static final DeferredHolder<Item, Item> PIGXIE_CANDY = ITEMS.register("pigxie_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.5F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> FOURHEADS_SPAWN_EGG = ITEMS.register("fourheads_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.FOURHEADS, 0x404020, 0x80A040, new Item.Properties()));
    public static final DeferredHolder<Item, Item> FOURHEADS_CANDY = ITEMS.register("fourheads_candy", () -> new Item(new Item.Properties().food(candyFood(8, 0.8F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> TWINGERSNAP_SPAWN_EGG = ITEMS.register("twingersnap_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.TWINGERSNAP, 0xC08830, 0xE0C080, new Item.Properties()));
    public static final DeferredHolder<Item, Item> TWINGERSNAP_CANDY = ITEMS.register("twingersnap_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.5F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CHOCLODOCUS_SPAWN_EGG = ITEMS.register("choclodocus_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.CHOCLODOCUS, 0x5C3A1E, 0xD4A06D, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CHOCLODOCUS_CANDY = ITEMS.register("choclodocus_candy", () -> new Item(new Item.Properties().food(candyFood(9, 0.9F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> JAMELEON_SPAWN_EGG = ITEMS.register("jameleon_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.JAMELEON, 0x40C040, 0xC04040, new Item.Properties()));
    public static final DeferredHolder<Item, Item> JAMELEON_CANDY = ITEMS.register("jameleon_candy", () -> new Item(new Item.Properties().food(candyFood(3, 0.3F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> GECKIE_SPAWN_EGG = ITEMS.register("geckie_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.GECKIE, 0x80FF80, 0xFFFF40, new Item.Properties()));
    public static final DeferredHolder<Item, Item> GECKIE_CANDY = ITEMS.register("geckie_candy", () -> new Item(new Item.Properties().food(candyFood(3, 0.3F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> JELI_SPAWN_EGG = ITEMS.register("jeli_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.JELI, 0xA0D0FF, 0xF0E0FF, new Item.Properties()));
    public static final DeferredHolder<Item, Item> JELI_CANDY = ITEMS.register("jeli_candy", () -> new Item(new Item.Properties().food(candyFood(3, 0.3F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CUSTACEAN_SPAWN_EGG = ITEMS.register("custacean_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.CUSTACEAN, 0xD06030, 0xFFE0A0, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CUSTACEAN_CANDY = ITEMS.register("custacean_candy", () -> new Item(new Item.Properties().food(candyFood(4, 0.4F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> MOTHDROP_SPAWN_EGG = ITEMS.register("mothdrop_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.MOTHDROP, 0xA0A080, 0xE0D0C0, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MOTHDROP_CANDY = ITEMS.register("mothdrop_candy", () -> new Item(new Item.Properties().food(candyFood(2, 0.2F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> SWEETLE_SPAWN_EGG = ITEMS.register("sweetle_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.SWEETLE, 0x40A040, 0xFFD040, new Item.Properties()));
    public static final DeferredHolder<Item, Item> SWEETLE_CANDY = ITEMS.register("sweetle_candy", () -> new Item(new Item.Properties().food(candyFood(3, 0.3F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> RAISANT_SPAWN_EGG = ITEMS.register("raisant_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.RAISANT, 0x402020, 0x604040, new Item.Properties()));
    public static final DeferredHolder<Item, Item> RAISANT_CANDY = ITEMS.register("raisant_candy", () -> new Item(new Item.Properties().food(candyFood(2, 0.2F))));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CHERRAPIN_SPAWN_EGG = ITEMS.register("cherrapin_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.CHERRAPIN, 0xC02040, 0x408040, new Item.Properties()));
    public static final DeferredHolder<Item, Item> CHERRAPIN_CANDY = ITEMS.register("cherrapin_candy", () -> new Item(new Item.Properties().food(candyFood(4, 0.4F))));

    // === Batch 6: Spawn Eggs + Candy ===
    public static final DeferredHolder<Item, DeferredSpawnEggItem> ELEPHANILLA_SPAWN_EGG =
            ITEMS.register("elephanilla_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.ELEPHANILLA, 0x909090, 0xF0E0C0, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CHEWNICORN_SPAWN_EGG =
            ITEMS.register("chewnicorn_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.CHEWNICORN, 0xFFB6C1, 0xFFFFE0, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> ROARIO_SPAWN_EGG =
            ITEMS.register("roario_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.ROARIO, 0xD4A030, 0x8B4513, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> TIGERMISU_SPAWN_EGG =
            ITEMS.register("tigermisu_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.TIGERMISU, 0xE88020, 0x202020, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> PARRYBO_SPAWN_EGG =
            ITEMS.register("parrybo_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.PARRYBO, 0xFF4040, 0x40FF40, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> SWANANA_SPAWN_EGG =
            ITEMS.register("swanana_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.SWANANA, 0xFFFF80, 0xFFFFFF, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> EAGLAIR_SPAWN_EGG =
            ITEMS.register("eaglair_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.EAGLAIR, 0x8B5E3C, 0xFFFFFF, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> BADGESICLE_SPAWN_EGG =
            ITEMS.register("badgesicle_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.BADGESICLE, 0x404040, 0xE0E0E0, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> HOOTYFRUITY_SPAWN_EGG =
            ITEMS.register("hootyfruity_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.HOOTYFRUITY, 0x6B3FA0, 0xFFA040, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> DRAGUMFLY_SPAWN_EGG =
            ITEMS.register("dragumfly_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.DRAGUMFLY, 0x60D0FF, 0xFF60A0, new Item.Properties()));

    public static final DeferredHolder<Item, Item> ELEPHANILLA_CANDY = ITEMS.register("elephanilla_candy", () -> new Item(new Item.Properties().food(candyFood(7, 0.8F))));
    public static final DeferredHolder<Item, Item> CHEWNICORN_CANDY = ITEMS.register("chewnicorn_candy", () -> new Item(new Item.Properties().food(candyFood(6, 0.7F))));
    public static final DeferredHolder<Item, Item> ROARIO_CANDY = ITEMS.register("roario_candy", () -> new Item(new Item.Properties().food(candyFood(6, 0.7F))));
    public static final DeferredHolder<Item, Item> TIGERMISU_CANDY = ITEMS.register("tigermisu_candy", () -> new Item(new Item.Properties().food(candyFood(6, 0.7F))));
    public static final DeferredHolder<Item, Item> PARRYBO_CANDY = ITEMS.register("parrybo_candy", () -> new Item(new Item.Properties().food(candyFood(3, 0.4F))));
    public static final DeferredHolder<Item, Item> SWANANA_CANDY = ITEMS.register("swanana_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.6F))));
    public static final DeferredHolder<Item, Item> EAGLAIR_CANDY = ITEMS.register("eaglair_candy", () -> new Item(new Item.Properties().food(candyFood(5, 0.6F))));
    public static final DeferredHolder<Item, Item> BADGESICLE_CANDY = ITEMS.register("badgesicle_candy", () -> new Item(new Item.Properties().food(candyFood(4, 0.5F))));
    public static final DeferredHolder<Item, Item> HOOTYFRUITY_CANDY = ITEMS.register("hootyfruity_candy", () -> new Item(new Item.Properties().food(candyFood(4, 0.5F))));
    public static final DeferredHolder<Item, Item> DRAGUMFLY_CANDY = ITEMS.register("dragumfly_candy", () -> new Item(new Item.Properties().food(candyFood(3, 0.3F))));

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
