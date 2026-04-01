package com.reactivefluids;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ReactiveFluids.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DISINTEGRATE =
            PARTICLE_TYPES.register("disintegrate", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> DANCING_LIGHT =
            PARTICLE_TYPES.register("dancing_light", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPECTRAL =
            PARTICLE_TYPES.register("spectral", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FOG_CLOUD =
            PARTICLE_TYPES.register("fog_cloud", () -> new SimpleParticleType(false));
}
