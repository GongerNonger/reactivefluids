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
}
