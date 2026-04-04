package com.reactivefluids;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ModDimensions {

    public static final ResourceKey<Level> MANSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "mansion"));

    public static final ResourceKey<Level> GROTTO = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "grotto"));
}
