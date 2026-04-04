package com.reactivefluids;

import com.mojang.logging.LogUtils;
import com.reactivefluids.pinata.*;
import com.reactivefluids.pinata.ModPinataEntities;
import com.reactivefluids.pinata.ModPinataItems;
import com.reactivefluids.pinata.garden.GardenTickHandler;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
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
        ModParticles.PARTICLE_TYPES.register(modEventBus);
        ModCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);

        // Viva Piñata registrations
        ModPinataEntities.ENTITY_TYPES.register(modEventBus);
        ModPinataItems.ITEMS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerAttributes);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Elephant's Toothpaste: hydrogen peroxide + potassium iodide → foam
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

            // Dispenser behaviors — throwable hardeners (ProjectileItem interface)
            DispenserBlock.registerBehavior(ModItems.AMBER_HARDENER.get(),
                new ProjectileDispenseBehavior(ModItems.AMBER_HARDENER.get()));
            DispenserBlock.registerBehavior(ModItems.COBALT_HARDENER.get(),
                new ProjectileDispenseBehavior(ModItems.COBALT_HARDENER.get()));
            DispenserBlock.registerBehavior(ModItems.JADE_HARDENER.get(),
                new ProjectileDispenseBehavior(ModItems.JADE_HARDENER.get()));

            // Dispenser behaviors — throwable reagents
            DispenserBlock.registerBehavior(ModItems.ACID_REAGENT.get(),
                new ProjectileDispenseBehavior(ModItems.ACID_REAGENT.get()));
            DispenserBlock.registerBehavior(ModItems.BASE_REAGENT.get(),
                new ProjectileDispenseBehavior(ModItems.BASE_REAGENT.get()));
        });
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CRYSTALLIZED_SKELETON.get(),
                CrystallizedSkeleton.createAttributes().build());
        event.put(ModEntities.PHANTOM_STEED.get(),
                PhantomSteedEntity.createAttributes().build());
        event.put(ModEntities.SPECTRAL_WOLF.get(),
                SpectralWolfEntity.createAttributes().build());
        event.put(ModEntities.SPECTRAL_FOX.get(),
                SpectralFoxEntity.createAttributes().build());
        event.put(ModEntities.SPECTRAL_AXOLOTL.get(),
                SpectralAxolotlEntity.createAttributes().build());
        event.put(ModEntities.RAISED_ZOMBIE.get(),
                RaisedZombieEntity.createAttributes().build());
        event.put(ModEntities.RAISED_SKELETON.get(),
                RaisedSkeletonEntity.createAttributes().build());

        // Piñata entities
        event.put(ModPinataEntities.WHIRLM.get(),
                WhirlmEntity.createAttributes().build());
        event.put(ModPinataEntities.SPARROWMINT.get(),
                SparrowmintEntity.createAttributes().build());
        event.put(ModPinataEntities.FUDGEHOG.get(),
                FudgehogEntity.createAttributes().build());
        event.put(ModPinataEntities.MOUSEMALLOW.get(),
                MousemallowEntity.createAttributes().build());
        event.put(ModPinataEntities.SYRUPENT.get(),
                SyrupentEntity.createAttributes().build());
        event.put(ModPinataEntities.TAFFLY.get(),
                TafflyEntity.createAttributes().build());
        event.put(ModPinataEntities.BUNNYCOMB.get(),
                BunnycombEntity.createAttributes().build());
        event.put(ModPinataEntities.QUACKBERRY.get(),
                QuackberryEntity.createAttributes().build());
        event.put(ModPinataEntities.SHELLYBEAN.get(),
                ShellybeanEntity.createAttributes().build());
        event.put(ModPinataEntities.NEWTGAT.get(),
                NewtgatEntity.createAttributes().build());
        event.put(ModPinataEntities.LICKATOAD.get(),
                LickatoadEntity.createAttributes().build());
        event.put(ModPinataEntities.PRETZTAIL.get(),
                PretztailEntity.createAttributes().build());
        event.put(ModPinataEntities.BUZZLEGUM.get(),
                BuzzlegumEntity.createAttributes().build());
        event.put(ModPinataEntities.CLUCKLES.get(),
                ClucklesEntity.createAttributes().build());
        event.put(ModPinataEntities.HORSTACHIO.get(),
                HorstachioEntity.createAttributes().build());
        event.put(ModPinataEntities.BARKBARK.get(),
                BarkbarkEntity.createAttributes().build());
        event.put(ModPinataEntities.KITTYFLOSS.get(),
                KittyflossEntity.createAttributes().build());
        event.put(ModPinataEntities.GOOBAA.get(),
                GoobaaEntity.createAttributes().build());
        event.put(ModPinataEntities.RASHBERRY.get(),
                RashberryEntity.createAttributes().build());
        event.put(ModPinataEntities.DOENUT.get(),
                DoenutEntity.createAttributes().build());
        event.put(ModPinataEntities.SEEDOS.get(),
                SeedosEntity.createAttributes().build());
        // Batch 6
        event.put(ModPinataEntities.ELEPHANILLA.get(), ElephanillaEntity.createAttributes().build());
        event.put(ModPinataEntities.CHEWNICORN.get(), ChewnicornEntity.createAttributes().build());
        event.put(ModPinataEntities.ROARIO.get(), RoarioEntity.createAttributes().build());
        event.put(ModPinataEntities.TIGERMISU.get(), TigermisuEntity.createAttributes().build());
        event.put(ModPinataEntities.PARRYBO.get(), ParryboEntity.createAttributes().build());
        event.put(ModPinataEntities.SWANANA.get(), SwananaEntity.createAttributes().build());
        event.put(ModPinataEntities.EAGLAIR.get(), EaglairEntity.createAttributes().build());
        event.put(ModPinataEntities.BADGESICLE.get(), BadgesicleEntity.createAttributes().build());
        event.put(ModPinataEntities.HOOTYFRUITY.get(), HootyfruityEntity.createAttributes().build());
        event.put(ModPinataEntities.DRAGUMFLY.get(), DragumflyEntity.createAttributes().build());
        event.put(ModPinataEntities.SQUAZZIL.get(),
                SquazzilEntity.createAttributes().build());
        event.put(ModPinataEntities.SWEETOOTH.get(),
                SweetoothEntity.createAttributes().build());
        event.put(ModPinataEntities.MALLOWOLF.get(),
                MallowolfEntity.createAttributes().build());
        event.put(ModPinataEntities.COCOADILE.get(),
                CocoadileEntity.createAttributes().build());
        event.put(ModPinataEntities.DRAGONACHE.get(),
                DragonacheEntity.createAttributes().build());
        event.put(ModPinataEntities.RUFFIAN.get(),
                RuffianEntity.createAttributes().build());
        event.put(ModPinataEntities.DASTARDOS.get(),
                DastardosEntity.createAttributes().build());
    }

    private void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            // Arcane Gate needs per-tick checking for entity teleportation
            ArcaneGateData.get(level).tick(level);

            // These only need periodic checking for expiration
            if (event.getServer().getTickCount() % 200 == 0) {
                ConjuredTowerData.get(level).tick(level);
                PasswallData.get(level).tick(level);
                TinyHutData.get(level).tick(level);
                ControlWaterData.get(level).tick(level);
                // Piñata garden system — scan gardens and spawn attracted piñatas
                GardenTickHandler.tickGardens(level);
            }
        }

        // Pocket dimension expiry (checked on overworld, manages both mansion & grotto)
        if (event.getServer().getTickCount() % 200 == 0) {
            PocketDimensionData.get(event.getServer()).tick(event.getServer());
        }
    }
}
