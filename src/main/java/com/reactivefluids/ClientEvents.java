package com.reactivefluids;

import com.reactivefluids.pinata.*;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
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

        // Piñata renderers — GeckoLib for species with Bedrock reference models
        event.registerEntityRenderer(ModPinataEntities.WHIRLM.get(), PinataGeoRenderer.provider("whirlm"));
        event.registerEntityRenderer(ModPinataEntities.SPARROWMINT.get(), PinataGeoRenderer.provider("sparrowmint"));
        event.registerEntityRenderer(ModPinataEntities.FUDGEHOG.get(), PinataGeoRenderer.providerAnimated("fudgehog"));
        event.registerEntityRenderer(ModPinataEntities.MOUSEMALLOW.get(), PinataGeoRenderer.provider("mousemallow"));
        event.registerEntityRenderer(ModPinataEntities.SYRUPENT.get(), SyrupentRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.TAFFLY.get(), TafflyRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.BUNNYCOMB.get(), PinataGeoRenderer.provider("bunnycomb"));
        event.registerEntityRenderer(ModPinataEntities.QUACKBERRY.get(), QuackberryRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.SHELLYBEAN.get(), ShellybeanRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.NEWTGAT.get(), NewtgatRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.LICKATOAD.get(), LickatoadRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.PRETZTAIL.get(), PretztailRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.BUZZLEGUM.get(), BuzzlegumRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.CLUCKLES.get(), ClucklesRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.HORSTACHIO.get(), HorstachioRenderer::new);
        // Batch 4 piñatas (reuse existing model shapes with different textures)
        event.registerEntityRenderer(ModPinataEntities.BARKBARK.get(), BarkbarkRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.KITTYFLOSS.get(), KittyflossRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.GOOBAA.get(), GoobaaRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.RASHBERRY.get(), RashberryRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.DOENUT.get(), DoenutRenderer::new);
        // Batches 7-9
        event.registerEntityRenderer(ModPinataEntities.CANDARY.get(), CandaryRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.PARMADILLO.get(), PinataGeoRenderer.provider("parmadillo"));
        event.registerEntityRenderer(ModPinataEntities.ZUMBUG.get(), ZumbugRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.PIEENA.get(), PieenaRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.JUICYGOOSE.get(), JuicygooseRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.SALAMANGO.get(), SalamangoRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.REDDHOTT.get(), ReddhottRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.CHOCSTRICH.get(), ChocstrichRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.MOOJOO.get(), MoojooRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.CINNAMONKEY.get(), CinnamonkeyRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.SARSGORILLA.get(), SarsgorillaRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.CAMELLO.get(), CamelloRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.PENGUM.get(), PinataGeoRenderer.provider("pengum"));
        event.registerEntityRenderer(ModPinataEntities.WALRUSK.get(), WalruskRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.POLOLLYBEAR.get(), PolollybearRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.FIZZLYBEAR.get(), FizzlybearRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.LIMEOCEROS.get(), LimeocerosRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.PIGXIE.get(), PigxieRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.FOURHEADS.get(), FourheadsRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.TWINGERSNAP.get(), TwingersnapRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.CHOCLODOCUS.get(), ChoclodocusRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.JAMELEON.get(), JameleonRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.GECKIE.get(), GeckieRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.JELI.get(), JeliRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.CUSTACEAN.get(), PinataGeoRenderer.provider("custacean"));
        event.registerEntityRenderer(ModPinataEntities.MOTHDROP.get(), MothdropRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.SWEETLE.get(), SweetleRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.RAISANT.get(), RaisantRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.CHERRAPIN.get(), CherrapinRenderer::new);
        // Batch 6
        event.registerEntityRenderer(ModPinataEntities.ELEPHANILLA.get(), ElephanillaRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.CHEWNICORN.get(), ChewnicornRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.ROARIO.get(), RoarioRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.TIGERMISU.get(), TigermisuRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.PARRYBO.get(), ParryboRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.SWANANA.get(), SwananaRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.EAGLAIR.get(), EaglairRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.BADGESICLE.get(), BadgesicleRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.HOOTYFRUITY.get(), HootyfruityRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.DRAGUMFLY.get(), DragumflyRenderer::new);
        // Batch 5
        event.registerEntityRenderer(ModPinataEntities.SQUAZZIL.get(), SquazzilRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.SWEETOOTH.get(), SweetoothRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.MALLOWOLF.get(), MallowolfRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.COCOADILE.get(), CocoadileRenderer::new);
        event.registerEntityRenderer(ModPinataEntities.DRAGONACHE.get(), DragonacheRenderer::new);
        // Storkos, Doc Patchingo, Professor Pester — use villager models
        event.registerEntityRenderer(ModPinataEntities.STORKOS.get(),
                ctx -> new net.minecraft.client.renderer.entity.MobRenderer<StorkosEntity, VillagerModel<StorkosEntity>>(
                        ctx, new VillagerModel<>(ctx.bakeLayer(ModelLayers.VILLAGER)), 0.4F) {
                    @Override public net.minecraft.resources.ResourceLocation getTextureLocation(StorkosEntity e) {
                        return net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
                    }
                });
        event.registerEntityRenderer(ModPinataEntities.DOC_PATCHINGO.get(),
                ctx -> new net.minecraft.client.renderer.entity.MobRenderer<DocPatchingoEntity, VillagerModel<DocPatchingoEntity>>(
                        ctx, new VillagerModel<>(ctx.bakeLayer(ModelLayers.VILLAGER)), 0.5F) {
                    @Override public net.minecraft.resources.ResourceLocation getTextureLocation(DocPatchingoEntity e) {
                        return net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
                    }
                });
        event.registerEntityRenderer(ModPinataEntities.PROFESSOR_PESTER.get(),
                ctx -> new net.minecraft.client.renderer.entity.MobRenderer<ProfessorPesterEntity, VillagerModel<ProfessorPesterEntity>>(
                        ctx, new VillagerModel<>(ctx.bakeLayer(ModelLayers.VILLAGER)), 0.6F) {
                    @Override public net.minecraft.resources.ResourceLocation getTextureLocation(ProfessorPesterEntity e) {
                        return net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/entity/illager/evoker.png");
                    }
                });
        // NPCs and threats — use villager model as placeholder
        event.registerEntityRenderer(ModPinataEntities.RUFFIAN.get(),
                ctx -> new net.minecraft.client.renderer.entity.MobRenderer<RuffianEntity, VillagerModel<RuffianEntity>>(
                        ctx, new VillagerModel<>(ctx.bakeLayer(ModelLayers.VILLAGER)), 0.5F) {
                    @Override
                    public net.minecraft.resources.ResourceLocation getTextureLocation(RuffianEntity entity) {
                        return net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/entity/illager/pillager.png");
                    }
                });
        event.registerEntityRenderer(ModPinataEntities.DASTARDOS.get(),
                ctx -> new net.minecraft.client.renderer.entity.MobRenderer<DastardosEntity, VillagerModel<DastardosEntity>>(
                        ctx, new VillagerModel<>(ctx.bakeLayer(ModelLayers.VILLAGER)), 0.5F) {
                    @Override
                    public net.minecraft.resources.ResourceLocation getTextureLocation(DastardosEntity entity) {
                        return net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/entity/illager/evoker.png");
                    }
                });
        event.registerEntityRenderer(ModPinataEntities.SEEDOS.get(),
                ctx -> new net.minecraft.client.renderer.entity.MobRenderer<SeedosEntity, VillagerModel<SeedosEntity>>(
                        ctx, new VillagerModel<>(ctx.bakeLayer(ModelLayers.VILLAGER)), 0.5F) {
                    @Override
                    public net.minecraft.resources.ResourceLocation getTextureLocation(SeedosEntity entity) {
                        return net.minecraft.resources.ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
                    }
                });
    }

    @SubscribeEvent
    public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WhirlmModel.LAYER, WhirlmModel::createBodyLayer);
        event.registerLayerDefinition(SparrowmintModel.LAYER, SparrowmintModel::createBodyLayer);
        event.registerLayerDefinition(FudgehogModel.LAYER, FudgehogModel::createBodyLayer);
        event.registerLayerDefinition(MousemallowModel.LAYER, MousemallowModel::createBodyLayer);
        event.registerLayerDefinition(SyrupentModel.LAYER, SyrupentModel::createBodyLayer);
        event.registerLayerDefinition(TafflyModel.LAYER, TafflyModel::createBodyLayer);
        event.registerLayerDefinition(BunnycombModel.LAYER, BunnycombModel::createBodyLayer);
        event.registerLayerDefinition(QuackberryModel.LAYER, QuackberryModel::createBodyLayer);
        event.registerLayerDefinition(ShellybeanModel.LAYER, ShellybeanModel::createBodyLayer);
        event.registerLayerDefinition(NewtgatModel.LAYER, NewtgatModel::createBodyLayer);
        event.registerLayerDefinition(LickatoadModel.LAYER, LickatoadModel::createBodyLayer);
        event.registerLayerDefinition(PretztailModel.LAYER, PretztailModel::createBodyLayer);
        event.registerLayerDefinition(BuzzlegumModel.LAYER, BuzzlegumModel::createBodyLayer);
        event.registerLayerDefinition(ClucklesModel.LAYER, ClucklesModel::createBodyLayer);
        event.registerLayerDefinition(HorstachioModel.LAYER, HorstachioModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.DISINTEGRATE.get(), DisintegrateParticle.Provider::new);
        event.registerSpriteSet(ModParticles.DANCING_LIGHT.get(), DancingLightParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SPECTRAL.get(), SpectralParticle.Provider::new);
        event.registerSpriteSet(ModParticles.FOG_CLOUD.get(), FogCloudParticle.Provider::new);
        event.registerSpriteSet(ModParticles.NECROTIC.get(), NecroticParticle.Provider::new);
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
