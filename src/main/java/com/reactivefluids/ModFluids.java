package com.reactivefluids;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Consumer;

public class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ReactiveFluids.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(Registries.FLUID, ReactiveFluids.MOD_ID);

    // =========================================================================
    // Slow fluid helpers — resin flows like lava (tick delay 25, spreads less)
    // Hardener flows like water (tick delay 5, spreads further)
    // =========================================================================
    private static class SlowSource extends BaseFlowingFluid.Source {
        SlowSource(BaseFlowingFluid.Properties p) { super(p); }
        @Override public int getTickDelay(LevelReader level) { return 25; }
    }
    private static class SlowFlowing extends BaseFlowingFluid.Flowing {
        SlowFlowing(BaseFlowingFluid.Properties p) { super(p); }
        @Override public int getTickDelay(LevelReader level) { return 25; }
    }

    // =========================================================================
    // AMBER RESIN  (warm orange — slow)
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> AMBER_RESIN_TYPE =
        FLUID_TYPES.register("amber_resin", () -> makeFluidType(
            0xBEDC7814,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/amber_resin_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/amber_resin_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> AMBER_RESIN_SOURCE =
        FLUIDS.register("amber_resin", () -> new SlowSource(ModFluids.AMBER_RESIN_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> AMBER_RESIN_FLOWING =
        FLUIDS.register("amber_resin_flowing", () -> new SlowFlowing(ModFluids.AMBER_RESIN_PROPS));

    public static final BaseFlowingFluid.Properties AMBER_RESIN_PROPS =
        new BaseFlowingFluid.Properties(AMBER_RESIN_TYPE, AMBER_RESIN_SOURCE, AMBER_RESIN_FLOWING)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.AMBER_RESIN_BLOCK.get())
            .bucket(() -> ModItems.AMBER_RESIN_BUCKET.get());

    // =========================================================================
    // COBALT RESIN  (deep blue — slow)
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> COBALT_RESIN_TYPE =
        FLUID_TYPES.register("cobalt_resin", () -> makeFluidType(
            0xBE1E32C8,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/cobalt_resin_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/cobalt_resin_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> COBALT_RESIN_SOURCE =
        FLUIDS.register("cobalt_resin", () -> new SlowSource(ModFluids.COBALT_RESIN_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> COBALT_RESIN_FLOWING =
        FLUIDS.register("cobalt_resin_flowing", () -> new SlowFlowing(ModFluids.COBALT_RESIN_PROPS));

    public static final BaseFlowingFluid.Properties COBALT_RESIN_PROPS =
        new BaseFlowingFluid.Properties(COBALT_RESIN_TYPE, COBALT_RESIN_SOURCE, COBALT_RESIN_FLOWING)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.COBALT_RESIN_BLOCK.get())
            .bucket(() -> ModItems.COBALT_RESIN_BUCKET.get());

    // =========================================================================
    // JADE RESIN  (deep green — slow)
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> JADE_RESIN_TYPE =
        FLUID_TYPES.register("jade_resin", () -> makeFluidType(
            0xBE008C46,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/jade_resin_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/jade_resin_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> JADE_RESIN_SOURCE =
        FLUIDS.register("jade_resin", () -> new SlowSource(ModFluids.JADE_RESIN_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> JADE_RESIN_FLOWING =
        FLUIDS.register("jade_resin_flowing", () -> new SlowFlowing(ModFluids.JADE_RESIN_PROPS));

    public static final BaseFlowingFluid.Properties JADE_RESIN_PROPS =
        new BaseFlowingFluid.Properties(JADE_RESIN_TYPE, JADE_RESIN_SOURCE, JADE_RESIN_FLOWING)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.JADE_RESIN_BLOCK.get())
            .bucket(() -> ModItems.JADE_RESIN_BUCKET.get());

    // =========================================================================
    // AMBER GLOWING RESIN  (resin + glow ink sac — slow, luminous)
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> AMBER_GLOWING_RESIN_TYPE =
        FLUID_TYPES.register("amber_glowing_resin", () -> makeFluidType(
            0xCEFF9C00,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/amber_glowing_resin_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/amber_glowing_resin_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> AMBER_GLOWING_RESIN_SOURCE =
        FLUIDS.register("amber_glowing_resin", () -> new SlowSource(ModFluids.AMBER_GLOWING_RESIN_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> AMBER_GLOWING_RESIN_FLOWING =
        FLUIDS.register("amber_glowing_resin_flowing", () -> new SlowFlowing(ModFluids.AMBER_GLOWING_RESIN_PROPS));

    public static final BaseFlowingFluid.Properties AMBER_GLOWING_RESIN_PROPS =
        new BaseFlowingFluid.Properties(AMBER_GLOWING_RESIN_TYPE, AMBER_GLOWING_RESIN_SOURCE, AMBER_GLOWING_RESIN_FLOWING)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.AMBER_GLOWING_RESIN_BLOCK.get())
            .bucket(() -> ModItems.AMBER_GLOWING_RESIN_BUCKET.get());

    // =========================================================================
    // COBALT GLOWING RESIN
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> COBALT_GLOWING_RESIN_TYPE =
        FLUID_TYPES.register("cobalt_glowing_resin", () -> makeFluidType(
            0xCE00D4FF,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/cobalt_glowing_resin_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/cobalt_glowing_resin_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> COBALT_GLOWING_RESIN_SOURCE =
        FLUIDS.register("cobalt_glowing_resin", () -> new SlowSource(ModFluids.COBALT_GLOWING_RESIN_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> COBALT_GLOWING_RESIN_FLOWING =
        FLUIDS.register("cobalt_glowing_resin_flowing", () -> new SlowFlowing(ModFluids.COBALT_GLOWING_RESIN_PROPS));

    public static final BaseFlowingFluid.Properties COBALT_GLOWING_RESIN_PROPS =
        new BaseFlowingFluid.Properties(COBALT_GLOWING_RESIN_TYPE, COBALT_GLOWING_RESIN_SOURCE, COBALT_GLOWING_RESIN_FLOWING)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.COBALT_GLOWING_RESIN_BLOCK.get())
            .bucket(() -> ModItems.COBALT_GLOWING_RESIN_BUCKET.get());

    // =========================================================================
    // JADE GLOWING RESIN
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> JADE_GLOWING_RESIN_TYPE =
        FLUID_TYPES.register("jade_glowing_resin", () -> makeFluidType(
            0xCE00FF88,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/jade_glowing_resin_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/jade_glowing_resin_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> JADE_GLOWING_RESIN_SOURCE =
        FLUIDS.register("jade_glowing_resin", () -> new SlowSource(ModFluids.JADE_GLOWING_RESIN_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> JADE_GLOWING_RESIN_FLOWING =
        FLUIDS.register("jade_glowing_resin_flowing", () -> new SlowFlowing(ModFluids.JADE_GLOWING_RESIN_PROPS));

    public static final BaseFlowingFluid.Properties JADE_GLOWING_RESIN_PROPS =
        new BaseFlowingFluid.Properties(JADE_GLOWING_RESIN_TYPE, JADE_GLOWING_RESIN_SOURCE, JADE_GLOWING_RESIN_FLOWING)
            .slopeFindDistance(2)
            .levelDecreasePerBlock(2)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.JADE_GLOWING_RESIN_BLOCK.get())
            .bucket(() -> ModItems.JADE_GLOWING_RESIN_BUCKET.get());

    // =========================================================================
    // Shared helper
    // =========================================================================
    private static FluidType makeFluidType(int tintColor, ResourceLocation still, ResourceLocation flow) {
        return new FluidType(FluidType.Properties.create()
            .density(1500)
            .viscosity(3000)
        ) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override public ResourceLocation getStillTexture()   { return still; }
                    @Override public ResourceLocation getFlowingTexture() { return flow;  }
                    @Override public int getTintColor()                   { return tintColor; }
                });
            }
        };
    }
}
