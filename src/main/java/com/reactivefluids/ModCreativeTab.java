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
                })
                .build()
        );
}
