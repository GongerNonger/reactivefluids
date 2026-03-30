package com.reactivefluids;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * A LiquidBlock that registers itself as translucent so the fluid
 * textures' alpha channel is respected during chunk mesh compilation.
 * The standard approach (ItemBlockRenderTypes.setRenderLayer in ClientEvents)
 * can be overridden by NeoForge's default opaque fluid pass; overriding here
 * at the block level guarantees the render layer is always translucent.
 */
public class TranslucentLiquidBlock extends LiquidBlock {

    public TranslucentLiquidBlock(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderLayers() {
        // Fluids — the fluid renderer looks up render type by Fluid, NOT by Block.
        // Both source and flowing variants must be registered.
        ItemBlockRenderTypes.setRenderLayer(ModFluids.AMBER_RESIN_SOURCE.get(),          RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.AMBER_RESIN_FLOWING.get(),         RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.COBALT_RESIN_SOURCE.get(),         RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.COBALT_RESIN_FLOWING.get(),        RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.JADE_RESIN_SOURCE.get(),           RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.JADE_RESIN_FLOWING.get(),          RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.AMBER_GLOWING_RESIN_SOURCE.get(),  RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.AMBER_GLOWING_RESIN_FLOWING.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.COBALT_GLOWING_RESIN_SOURCE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.COBALT_GLOWING_RESIN_FLOWING.get(),RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.JADE_GLOWING_RESIN_SOURCE.get(),   RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.JADE_GLOWING_RESIN_FLOWING.get(),  RenderType.translucent());
        // Elephant's Toothpaste fluids
        ItemBlockRenderTypes.setRenderLayer(ModFluids.HYDROGEN_PEROXIDE_SOURCE.get(),    RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.HYDROGEN_PEROXIDE_FLOWING.get(),   RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.POTASSIUM_IODIDE_SOURCE.get(),     RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.POTASSIUM_IODIDE_FLOWING.get(),    RenderType.translucent());
        // Bioluminescent Plankton
        ItemBlockRenderTypes.setRenderLayer(ModFluids.PLANKTON_SOURCE.get(),             RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.PLANKTON_FLOWING.get(),            RenderType.translucent());
        // Acid
        ItemBlockRenderTypes.setRenderLayer(ModFluids.ACID_SOURCE.get(),                 RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.ACID_FLOWING.get(),                RenderType.translucent());
    }
}
