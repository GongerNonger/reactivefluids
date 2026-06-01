package com.reactivefluids;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {

    public static final DeferredRegister<Potion> POTIONS =
        DeferredRegister.create(Registries.POTION, ReactiveFluids.MOD_ID);

    // Base potion: 3 minutes of Tulip Mania
    public static final DeferredHolder<Potion, Potion> TULIP_MANIA =
        POTIONS.register("tulip_mania", () ->
            new Potion(new MobEffectInstance(ModEffects.TULIP_MANIA, 3600))); // 3 min

    // Long potion: 8 minutes (redstone-extended)
    public static final DeferredHolder<Potion, Potion> LONG_TULIP_MANIA =
        POTIONS.register("long_tulip_mania", () ->
            new Potion(new MobEffectInstance(ModEffects.TULIP_MANIA, 9600))); // 8 min

    // Strong potion: 1:30 but amplifier 1 (glowstone-enhanced)
    public static final DeferredHolder<Potion, Potion> STRONG_TULIP_MANIA =
        POTIONS.register("strong_tulip_mania", () ->
            new Potion(new MobEffectInstance(ModEffects.TULIP_MANIA, 1800, 1))); // 1:30, level II
}
