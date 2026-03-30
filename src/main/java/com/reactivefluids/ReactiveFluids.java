package com.reactivefluids;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ReactiveFluids.MOD_ID)
public class ReactiveFluids {

    public static final String MOD_ID = "reactivefluids";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ReactiveFluids(IEventBus modEventBus, ModContainer modContainer) {
        // Register fluid types first so fluids can reference them
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        // Blocks after fluids so LiquidBlock lambdas can call .get() safely
        ModBlocks.BLOCKS.register(modEventBus);
        // Items after blocks so BucketItem and BlockItem lambdas resolve correctly
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);
    }
}
