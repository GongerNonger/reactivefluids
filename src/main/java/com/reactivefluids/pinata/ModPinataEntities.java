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

    // Future piñata registrations will go here:
    // SPARROWMINT, FUDGEHOG, PRETZTAIL, BUNNYCOMB, TAFFLY, MOUSEMALLOW,
    // SYRUPENT, SHELLYBEAN, QUACKBERRY, NEWTGAT, BADGESICLE, EAGLAIR,
    // HORSTACHIO, ELEPHANILLA, DRAGONACHE, CHOCLODOCUS, etc.
}
