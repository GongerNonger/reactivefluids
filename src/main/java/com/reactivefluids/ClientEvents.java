package com.reactivefluids;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = ReactiveFluids.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HARDENER.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.REAGENT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.CRYSTALLIZED_SKELETON.get(), CrystallizedSkeletonRenderer::new);
        event.registerEntityRenderer(ModEntities.PHANTOM_STEED.get(), PhantomSteedRenderer::new);
        event.registerEntityRenderer(ModEntities.DANCING_LIGHT.get(), DancingLightRenderer::new);
        event.registerEntityRenderer(ModEntities.DISINTEGRATE_BEAM.get(), DisintegrateBeamRenderer::new);
        event.registerEntityRenderer(ModEntities.SPECTRAL_WOLF.get(), SpectralWolfRenderer::new);
        event.registerEntityRenderer(ModEntities.SPECTRAL_FOX.get(), SpectralFoxRenderer::new);
        event.registerEntityRenderer(ModEntities.SPECTRAL_AXOLOTL.get(), SpectralAxolotlRenderer::new);
        event.registerEntityRenderer(ModEntities.FOG_CLOUD.get(), FogCloudRenderer::new);
        event.registerEntityRenderer(ModEntities.METEOR.get(), MeteorRenderer::new);
        event.registerEntityRenderer(ModEntities.RAISED_ZOMBIE.get(), RaisedZombieRenderer::new);
        event.registerEntityRenderer(ModEntities.RAISED_SKELETON.get(), RaisedSkeletonRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.DISINTEGRATE.get(), DisintegrateParticle.Provider::new);
        event.registerSpriteSet(ModParticles.DANCING_LIGHT.get(), DancingLightParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SPECTRAL.get(), SpectralParticle.Provider::new);
        event.registerSpriteSet(ModParticles.FOG_CLOUD.get(), FogCloudParticle.Provider::new);
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

            // Crystal block — translucent
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CRYSTAL_BLOCK.get(), RenderType.translucent());

            // Arcane Barrier — translucent (Tiny Hut dome)
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ARCANE_BARRIER.get(), RenderType.translucent());

            // Return Portal — translucent (pocket dimension exit)
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RETURN_PORTAL.get(), RenderType.translucent());
        });
    }

}
