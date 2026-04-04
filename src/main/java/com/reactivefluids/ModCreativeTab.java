package com.reactivefluids;

import com.reactivefluids.pinata.ModPinataItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ReactiveFluids.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> REACTIVE_FLUIDS_TAB =
        CREATIVE_MODE_TABS.register("reactive_fluids_tab", () ->
            CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.reactivefluids.reactive_fluids"))
                .icon(() -> ModItems.AMBER_EPOXY_BLOCK_ITEM.get().getDefaultInstance())
                .displayItems((params, output) -> {
                    // Buckets
                    output.accept(ModItems.AMBER_RESIN_BUCKET.get());
                    output.accept(ModItems.AMBER_GLOWING_RESIN_BUCKET.get());
                    output.accept(ModItems.AMBER_HARDENER.get());
                    output.accept(ModItems.COBALT_RESIN_BUCKET.get());
                    output.accept(ModItems.COBALT_GLOWING_RESIN_BUCKET.get());
                    output.accept(ModItems.COBALT_HARDENER.get());
                    output.accept(ModItems.JADE_RESIN_BUCKET.get());
                    output.accept(ModItems.JADE_GLOWING_RESIN_BUCKET.get());
                    output.accept(ModItems.JADE_HARDENER.get());
                    // Transparent epoxy (reaction product)
                    output.accept(ModItems.AMBER_EPOXY_BLOCK_ITEM.get());
                    output.accept(ModItems.COBALT_EPOXY_BLOCK_ITEM.get());
                    output.accept(ModItems.JADE_EPOXY_BLOCK_ITEM.get());
                    // Glowing epoxy (epoxy + glow ink sac)
                    output.accept(ModItems.AMBER_EPOXY_GLOWING_ITEM.get());
                    output.accept(ModItems.COBALT_EPOXY_GLOWING_ITEM.get());
                    output.accept(ModItems.JADE_EPOXY_GLOWING_ITEM.get());
                    // Opaque epoxy (epoxy + squid ink sac)
                    output.accept(ModItems.AMBER_EPOXY_OPAQUE_ITEM.get());
                    output.accept(ModItems.COBALT_EPOXY_OPAQUE_ITEM.get());
                    output.accept(ModItems.JADE_EPOXY_OPAQUE_ITEM.get());
                    // Elephant's Toothpaste
                    output.accept(ModItems.HYDROGEN_PEROXIDE_BUCKET.get());
                    output.accept(ModItems.POTASSIUM_IODIDE_BUCKET.get());
                    output.accept(ModItems.FOAM_BLOCK_ITEM.get());
                    // Bioluminescent Plankton
                    output.accept(ModItems.PLANKTON_BUCKET.get());
                    // Acid
                    output.accept(ModItems.ACID_BUCKET.get());
                    // Crystal Solution
                    output.accept(ModItems.CRYSTAL_SOLUTION_BUCKET.get());
                    output.accept(ModItems.CRYSTAL_BLOCK_ITEM.get());
                    // Rainbow Indicator
                    output.accept(ModItems.INDICATOR_BUCKET.get());
                    output.accept(ModItems.ACID_REAGENT.get());
                    output.accept(ModItems.BASE_REAGENT.get());
                    // Spell Scrolls
                    output.accept(ModItems.MOLD_EARTH_SCROLL.get());
                    output.accept(ModItems.DANCING_LIGHTS_SCROLL.get());
                    output.accept(ModItems.CONJURE_ANIMALS_SCROLL.get());
                    output.accept(ModItems.TOWER_SCROLL.get());
                    output.accept(ModItems.STEED_SCROLL.get());
                    output.accept(ModItems.DIMENSION_DOOR_SCROLL.get());
                    output.accept(ModItems.WALL_OF_STONE_SCROLL.get());
                    output.accept(ModItems.PASSWALL_SCROLL.get());
                    output.accept(ModItems.DISINTEGRATE_SCROLL.get());
                    output.accept(ModItems.REVERSE_GRAVITY_SCROLL.get());
                    // New spell scrolls — ordered by spell level
                    output.accept(ModItems.FOG_CLOUD_SCROLL.get());       // 1st level
                    output.accept(ModItems.PLANT_GROWTH_SCROLL.get());    // 3rd level
                    output.accept(ModItems.ERUPTING_EARTH_SCROLL.get());  // 3rd level
                    output.accept(ModItems.TINY_HUT_SCROLL.get());        // 3rd level
                    output.accept(ModItems.CONTROL_WATER_SCROLL.get());   // 4th level
                    output.accept(ModItems.MOVE_EARTH_SCROLL.get());      // 6th level
                    output.accept(ModItems.BONES_OF_THE_EARTH_SCROLL.get()); // 6th level
                    output.accept(ModItems.ARCANE_GATE_SCROLL.get());     // 6th level
                    output.accept(ModItems.METEOR_SWARM_SCROLL.get());    // 9th level
                    output.accept(ModItems.MAGNIFICENT_MANSION_SCROLL.get()); // 7th level
                    output.accept(ModItems.GONGERS_GROTTO_SCROLL.get());      // 7th level
                    output.accept(ModItems.RAISE_DEAD_SCROLL.get());         // 3rd level
                    // Undead crafting blocks
                    output.accept(ModItems.ROTTEN_FLESH_BLOCK_ITEM.get());
                    // --- Viva Piñata: Spawn Eggs ---
                    output.accept(ModPinataItems.WHIRLM_SPAWN_EGG.get());
                    output.accept(ModPinataItems.SPARROWMINT_SPAWN_EGG.get());
                    output.accept(ModPinataItems.FUDGEHOG_SPAWN_EGG.get());
                    output.accept(ModPinataItems.MOUSEMALLOW_SPAWN_EGG.get());
                    output.accept(ModPinataItems.SYRUPENT_SPAWN_EGG.get());
                    output.accept(ModPinataItems.TAFFLY_SPAWN_EGG.get());
                    output.accept(ModPinataItems.BUNNYCOMB_SPAWN_EGG.get());
                    output.accept(ModPinataItems.QUACKBERRY_SPAWN_EGG.get());
                    output.accept(ModPinataItems.SHELLYBEAN_SPAWN_EGG.get());
                    output.accept(ModPinataItems.NEWTGAT_SPAWN_EGG.get());
                    output.accept(ModPinataItems.LICKATOAD_SPAWN_EGG.get());
                    output.accept(ModPinataItems.PRETZTAIL_SPAWN_EGG.get());
                    output.accept(ModPinataItems.BUZZLEGUM_SPAWN_EGG.get());
                    output.accept(ModPinataItems.CLUCKLES_SPAWN_EGG.get());
                    output.accept(ModPinataItems.HORSTACHIO_SPAWN_EGG.get());
                    // --- Viva Piñata: Candy ---
                    output.accept(ModPinataItems.WHIRLM_CANDY.get());
                    output.accept(ModPinataItems.SPARROWMINT_CANDY.get());
                    output.accept(ModPinataItems.FUDGEHOG_CANDY.get());
                    output.accept(ModPinataItems.MOUSEMALLOW_CANDY.get());
                    output.accept(ModPinataItems.SYRUPENT_CANDY.get());
                    output.accept(ModPinataItems.TAFFLY_CANDY.get());
                    output.accept(ModPinataItems.BUNNYCOMB_CANDY.get());
                    output.accept(ModPinataItems.QUACKBERRY_CANDY.get());
                    output.accept(ModPinataItems.SHELLYBEAN_CANDY.get());
                    output.accept(ModPinataItems.NEWTGAT_CANDY.get());
                    output.accept(ModPinataItems.LICKATOAD_CANDY.get());
                    output.accept(ModPinataItems.PRETZTAIL_CANDY.get());
                    output.accept(ModPinataItems.BUZZLEGUM_CANDY.get());
                    output.accept(ModPinataItems.CLUCKLES_CANDY.get());
                    output.accept(ModPinataItems.HORSTACHIO_CANDY.get());
                })
                .build()
        );
}
