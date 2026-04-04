package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity type registrations for all piñata creatures.
 * Kept separate from the main ModEntities to organize the VP content.
 */
public class ModPinataEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ReactiveFluids.MOD_ID);

    // === Whirlm (worm) — small, ground-level ===
    public static final DeferredHolder<EntityType<?>, EntityType<WhirlmEntity>> WHIRLM =
            ENTITY_TYPES.register("whirlm", () ->
                    EntityType.Builder.<WhirlmEntity>of(WhirlmEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.3F)  // Small worm-like creature
                            .clientTrackingRange(8)
                            .build("whirlm"));

    // === Sparrowmint (sparrow) — small bird, eats Whirlms ===
    public static final DeferredHolder<EntityType<?>, EntityType<SparrowmintEntity>> SPARROWMINT =
            ENTITY_TYPES.register("sparrowmint", () ->
                    EntityType.Builder.<SparrowmintEntity>of(SparrowmintEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .build("sparrowmint"));

    // === Fudgehog (hedgehog) — small, spiky, loves long grass ===
    public static final DeferredHolder<EntityType<?>, EntityType<FudgehogEntity>> FUDGEHOG =
            ENTITY_TYPES.register("fudgehog", () ->
                    EntityType.Builder.<FudgehogEntity>of(FudgehogEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.45F)
                            .clientTrackingRange(8)
                            .build("fudgehog"));

    // === Mousemallow (mouse) — tiny, fast herbivore ===
    public static final DeferredHolder<EntityType<?>, EntityType<MousemallowEntity>> MOUSEMALLOW =
            ENTITY_TYPES.register("mousemallow", () ->
                    EntityType.Builder.<MousemallowEntity>of(MousemallowEntity::new, MobCategory.CREATURE)
                            .sized(0.35F, 0.25F)  // Very small
                            .clientTrackingRange(8)
                            .build("mousemallow"));

    // === Syrupent (snake) — medium predator, eats Mousemallows ===
    public static final DeferredHolder<EntityType<?>, EntityType<SyrupentEntity>> SYRUPENT =
            ENTITY_TYPES.register("syrupent", () ->
                    EntityType.Builder.<SyrupentEntity>of(SyrupentEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.4F)
                            .clientTrackingRange(8)
                            .build("syrupent"));

    // === Taffly (fly) — tiny, attracted to flowers ===
    public static final DeferredHolder<EntityType<?>, EntityType<TafflyEntity>> TAFFLY =
            ENTITY_TYPES.register("taffly", () ->
                    EntityType.Builder.<TafflyEntity>of(TafflyEntity::new, MobCategory.CREATURE)
                            .sized(0.3F, 0.3F)  // Tiny buzzer
                            .clientTrackingRange(8)
                            .build("taffly"));

    // Future: PRETZTAIL, BUNNYCOMB, QUACKBERRY, SHELLYBEAN, NEWTGAT,
    // LICKATOAD, BUZZLEGUM, CLUCKLES, HORSTACHIO, etc.
}
