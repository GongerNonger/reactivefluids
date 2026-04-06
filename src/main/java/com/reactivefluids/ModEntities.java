package com.reactivefluids;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ReactiveFluids.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<HardenerEntity>> HARDENER =
            ENTITY_TYPES.register("hardener", () ->
                    EntityType.Builder.<HardenerEntity>of(HardenerEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("hardener"));

    public static final DeferredHolder<EntityType<?>, EntityType<ReagentEntity>> REAGENT =
            ENTITY_TYPES.register("reagent", () ->
                    EntityType.Builder.<ReagentEntity>of(ReagentEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("reagent"));

    public static final DeferredHolder<EntityType<?>, EntityType<CrystallizedSkeleton>> CRYSTALLIZED_SKELETON =
            ENTITY_TYPES.register("crystallized_skeleton", () ->
                    EntityType.Builder.<CrystallizedSkeleton>of(CrystallizedSkeleton::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.99F)
                            .clientTrackingRange(8)
                            .build("crystallized_skeleton"));

    public static final DeferredHolder<EntityType<?>, EntityType<PhantomSteedEntity>> PHANTOM_STEED =
            ENTITY_TYPES.register("phantom_steed", () ->
                    EntityType.Builder.<PhantomSteedEntity>of(PhantomSteedEntity::new, MobCategory.CREATURE)
                            .sized(1.3964844F, 1.6F)
                            .eyeHeight(1.52F)
                            .passengerAttachments(1.44375F)
                            .clientTrackingRange(10)
                            .build("phantom_steed"));

    public static final DeferredHolder<EntityType<?>, EntityType<DancingLightEntity>> DANCING_LIGHT =
            ENTITY_TYPES.register("dancing_light", () ->
                    EntityType.Builder.<DancingLightEntity>of(DancingLightEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build("dancing_light"));

    public static final DeferredHolder<EntityType<?>, EntityType<DisintegrateBeamEntity>> DISINTEGRATE_BEAM =
            ENTITY_TYPES.register("disintegrate_beam", () ->
                    EntityType.Builder.<DisintegrateBeamEntity>of(DisintegrateBeamEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build("disintegrate_beam"));

    public static final DeferredHolder<EntityType<?>, EntityType<SpectralWolfEntity>> SPECTRAL_WOLF =
            ENTITY_TYPES.register("spectral_wolf", () ->
                    EntityType.Builder.<SpectralWolfEntity>of(SpectralWolfEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.85F)
                            .clientTrackingRange(10)
                            .build("spectral_wolf"));

    public static final DeferredHolder<EntityType<?>, EntityType<SpectralFoxEntity>> SPECTRAL_FOX =
            ENTITY_TYPES.register("spectral_fox", () ->
                    EntityType.Builder.<SpectralFoxEntity>of(SpectralFoxEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.7F)
                            .clientTrackingRange(10)
                            .build("spectral_fox"));

    public static final DeferredHolder<EntityType<?>, EntityType<SpectralAxolotlEntity>> SPECTRAL_AXOLOTL =
            ENTITY_TYPES.register("spectral_axolotl", () ->
                    EntityType.Builder.<SpectralAxolotlEntity>of(SpectralAxolotlEntity::new, MobCategory.CREATURE)
                            .sized(0.75F, 0.42F)
                            .clientTrackingRange(10)
                            .build("spectral_axolotl"));

    public static final DeferredHolder<EntityType<?>, EntityType<FogCloudEntity>> FOG_CLOUD =
            ENTITY_TYPES.register("fog_cloud", () ->
                    EntityType.Builder.<FogCloudEntity>of(FogCloudEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .build("fog_cloud"));

    public static final DeferredHolder<EntityType<?>, EntityType<MeteorEntity>> METEOR =
            ENTITY_TYPES.register("meteor", () ->
                    EntityType.Builder.<MeteorEntity>of(MeteorEntity::new, MobCategory.MISC)
                            .sized(0.8F, 0.8F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build("meteor"));

    public static final DeferredHolder<EntityType<?>, EntityType<RaisedZombieEntity>> RAISED_ZOMBIE =
            ENTITY_TYPES.register("raised_zombie", () ->
                    EntityType.Builder.<RaisedZombieEntity>of(RaisedZombieEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build("raised_zombie"));

    public static final DeferredHolder<EntityType<?>, EntityType<RaisedSkeletonEntity>> RAISED_SKELETON =
            ENTITY_TYPES.register("raised_skeleton", () ->
                    EntityType.Builder.<RaisedSkeletonEntity>of(RaisedSkeletonEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.99F)
                            .clientTrackingRange(8)
                            .build("raised_skeleton"));

    public static final DeferredHolder<EntityType<?>, EntityType<FireballEntity>> FIREBALL =
            ENTITY_TYPES.register("fletchers_fireball", () ->
                    EntityType.Builder.<FireballEntity>of(FireballEntity::new, MobCategory.MISC)
                            .sized(0.3F, 0.3F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build("fletchers_fireball"));
}
