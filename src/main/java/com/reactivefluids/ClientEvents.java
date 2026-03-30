package com.reactivefluids;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ReactiveFluids.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HARDENER.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Fluid blocks — registered via TranslucentLiquidBlock helper
            TranslucentLiquidBlock.registerRenderLayers();

            // Epoxy blocks — translucent (glass-like)
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.AMBER_EPOXY_BLOCK.get(),  RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.COBALT_EPOXY_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.JADE_EPOXY_BLOCK.get(),   RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.AMBER_EPOXY_GLOWING.get(),  RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.COBALT_EPOXY_GLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.JADE_EPOXY_GLOWING.get(),   RenderType.translucent());
        });
    }

}
