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

    // === Bunnycomb (rabbit) — small, fast breeder ===
    public static final DeferredHolder<EntityType<?>, EntityType<BunnycombEntity>> BUNNYCOMB =
            ENTITY_TYPES.register("bunnycomb", () ->
                    EntityType.Builder.<BunnycombEntity>of(BunnycombEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.5F).clientTrackingRange(8).build("bunnycomb"));

    // === Quackberry (duck) — aquatic ===
    public static final DeferredHolder<EntityType<?>, EntityType<QuackberryEntity>> QUACKBERRY =
            ENTITY_TYPES.register("quackberry", () ->
                    EntityType.Builder.<QuackberryEntity>of(QuackberryEntity::new, MobCategory.CREATURE)
                            .sized(0.55F, 0.5F).clientTrackingRange(8).build("quackberry"));

    // === Shellybean (snail) — very slow, armored ===
    public static final DeferredHolder<EntityType<?>, EntityType<ShellybeanEntity>> SHELLYBEAN =
            ENTITY_TYPES.register("shellybean", () ->
                    EntityType.Builder.<ShellybeanEntity>of(ShellybeanEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.4F).clientTrackingRange(8).build("shellybean"));

    // === Newtgat (newt) — semi-aquatic ===
    public static final DeferredHolder<EntityType<?>, EntityType<NewtgatEntity>> NEWTGAT =
            ENTITY_TYPES.register("newtgat", () ->
                    EntityType.Builder.<NewtgatEntity>of(NewtgatEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.3F).clientTrackingRange(8).build("newtgat"));

    // === Lickatoad (frog) — eats Tafflies ===
    public static final DeferredHolder<EntityType<?>, EntityType<LickatoadEntity>> LICKATOAD =
            ENTITY_TYPES.register("lickatoad", () ->
                    EntityType.Builder.<LickatoadEntity>of(LickatoadEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.45F).clientTrackingRange(8).build("lickatoad"));

    // === Pretztail (fox) — eats Bunnycombs ===
    public static final DeferredHolder<EntityType<?>, EntityType<PretztailEntity>> PRETZTAIL =
            ENTITY_TYPES.register("pretztail", () ->
                    EntityType.Builder.<PretztailEntity>of(PretztailEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.7F).clientTrackingRange(10).build("pretztail"));

    // === Buzzlegum (bee) — produces honey ===
    public static final DeferredHolder<EntityType<?>, EntityType<BuzzlegumEntity>> BUZZLEGUM =
            ENTITY_TYPES.register("buzzlegum", () ->
                    EntityType.Builder.<BuzzlegumEntity>of(BuzzlegumEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.5F).clientTrackingRange(8).build("buzzlegum"));

    // === Cluckles (chicken) — lays eggs ===
    public static final DeferredHolder<EntityType<?>, EntityType<ClucklesEntity>> CLUCKLES =
            ENTITY_TYPES.register("cluckles", () ->
                    EntityType.Builder.<ClucklesEntity>of(ClucklesEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.6F).clientTrackingRange(8).build("cluckles"));

    // === Horstachio (horse) — large, majestic ===
    public static final DeferredHolder<EntityType<?>, EntityType<HorstachioEntity>> HORSTACHIO =
            ENTITY_TYPES.register("horstachio", () ->
                    EntityType.Builder.<HorstachioEntity>of(HorstachioEntity::new, MobCategory.CREATURE)
                            .sized(1.4F, 1.6F).clientTrackingRange(10).build("horstachio"));

    // === Seedos NPC — seed-giving gardener ===
    public static final DeferredHolder<EntityType<?>, EntityType<SeedosEntity>> SEEDOS =
            ENTITY_TYPES.register("seedos", () ->
                    EntityType.Builder.<SeedosEntity>of(SeedosEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F).clientTrackingRange(10).build("seedos"));
}
