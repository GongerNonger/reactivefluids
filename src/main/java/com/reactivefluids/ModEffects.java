package com.reactivefluids;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
        DeferredRegister.create(Registries.MOB_EFFECT, ReactiveFluids.MOD_ID);

    public static final DeferredHolder<MobEffect, TulipManiaMobEffect> TULIP_MANIA =
        MOB_EFFECTS.register("tulip_mania", TulipManiaMobEffect::new);
}
