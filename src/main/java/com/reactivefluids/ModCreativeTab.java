package com.reactivefluids;

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
                    // Liquid Nitrogen
                    output.accept(ModItems.LIQUID_NITROGEN_BUCKET.get());
                    // Greek Fire
                    output.accept(ModItems.GREEK_FIRE_BUCKET.get());
                    // Ferrofluid
                    output.accept(ModItems.FERROFLUID_BUCKET.get());
                    // Superfluid
                    output.accept(ModItems.SUPERFLUID_BUCKET.get());
                    // Mycelium Slurry
                    output.accept(ModItems.MYCELIUM_SLURRY_BUCKET.get());
                    // Custom Mushrooms
                    output.accept(ModItems.GHOST_FUNGUS_ITEM.get());
                    output.accept(ModItems.INDIGO_MILK_CAP_ITEM.get());
                    output.accept(ModItems.BLEEDING_TOOTH_ITEM.get());
                    output.accept(ModItems.AMETHYST_DECEIVER_ITEM.get());
                    output.accept(ModItems.LIONS_MANE_ITEM.get());
                    output.accept(ModItems.DEVILS_CIGAR_ITEM.get());
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
                    output.accept(ModItems.MAGIC_MISSILE_SCROLL.get());    // 1st level
                    // Undead crafting blocks
                    output.accept(ModItems.ROTTEN_FLESH_BLOCK_ITEM.get());
                    // Mobs
                    output.accept(ModItems.MOONLIGHT_JELLYFISH_SPAWN_EGG.get());
                    // Lava Lamps
                    output.accept(ModItems.LAVA_LAMP_RED_ITEM.get());
                    output.accept(ModItems.LAVA_LAMP_BLUE_ITEM.get());
                    output.accept(ModItems.LAVA_LAMP_GREEN_ITEM.get());
                    output.accept(ModItems.LAVA_LAMP_PURPLE_ITEM.get());
                })
                .build()
        );
}
