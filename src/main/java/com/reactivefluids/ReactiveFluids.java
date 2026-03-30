package com.reactivefluids;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import org.slf4j.Logger;

@Mod(ReactiveFluids.MOD_ID)
public class ReactiveFluids {

    public static final String MOD_ID = "reactivefluids";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ReactiveFluids(IEventBus modEventBus, ModContainer modContainer) {
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Elephant's Toothpaste: hydrogen peroxide + potassium iodide → foam
            // Constructor: InteractionInformation(FluidType otherFluid, BlockState result)
            FluidInteractionRegistry.addInteraction(
                ModFluids.HYDROGEN_PEROXIDE_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation(
                    ModFluids.POTASSIUM_IODIDE_TYPE.get(),
                    ModBlocks.FOAM_BLOCK.get().defaultBlockState()
                )
            );
            FluidInteractionRegistry.addInteraction(
                ModFluids.POTASSIUM_IODIDE_TYPE.get(),
                new FluidInteractionRegistry.InteractionInformation(
                    ModFluids.HYDROGEN_PEROXIDE_TYPE.get(),
                    ModBlocks.FOAM_BLOCK.get().defaultBlockState()
                )
            );
        });
    }
}
