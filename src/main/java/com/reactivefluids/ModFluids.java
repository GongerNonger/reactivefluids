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
    // HYDROGEN PEROXIDE  (clear/pale blue — fast, watery)
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> HYDROGEN_PEROXIDE_TYPE =
        FLUID_TYPES.register("hydrogen_peroxide", () -> makeFluidType(
            0xA0B0D8FF,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/hydrogen_peroxide_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/hydrogen_peroxide_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> HYDROGEN_PEROXIDE_SOURCE =
        FLUIDS.register("hydrogen_peroxide", () -> new BaseFlowingFluid.Source(ModFluids.HYDROGEN_PEROXIDE_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> HYDROGEN_PEROXIDE_FLOWING =
        FLUIDS.register("hydrogen_peroxide_flowing", () -> new BaseFlowingFluid.Flowing(ModFluids.HYDROGEN_PEROXIDE_PROPS));

    public static final BaseFlowingFluid.Properties HYDROGEN_PEROXIDE_PROPS =
        new BaseFlowingFluid.Properties(HYDROGEN_PEROXIDE_TYPE, HYDROGEN_PEROXIDE_SOURCE, HYDROGEN_PEROXIDE_FLOWING)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.HYDROGEN_PEROXIDE_BLOCK.get())
            .bucket(() -> ModItems.HYDROGEN_PEROXIDE_BUCKET.get());

    // =========================================================================
    // POTASSIUM IODIDE  (amber/brown — medium speed)
    // =========================================================================
    private static class MediumSource extends BaseFlowingFluid.Source {
        MediumSource(BaseFlowingFluid.Properties p) { super(p); }
        @Override public int getTickDelay(LevelReader level) { return 15; }
    }
    private static class MediumFlowing extends BaseFlowingFluid.Flowing {
        MediumFlowing(BaseFlowingFluid.Properties p) { super(p); }
        @Override public int getTickDelay(LevelReader level) { return 15; }
    }

    public static final DeferredHolder<FluidType, FluidType> POTASSIUM_IODIDE_TYPE =
        FLUID_TYPES.register("potassium_iodide", () -> makeFluidType(
            0x90B47828,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/potassium_iodide_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/potassium_iodide_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> POTASSIUM_IODIDE_SOURCE =
        FLUIDS.register("potassium_iodide", () -> new MediumSource(ModFluids.POTASSIUM_IODIDE_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> POTASSIUM_IODIDE_FLOWING =
        FLUIDS.register("potassium_iodide_flowing", () -> new MediumFlowing(ModFluids.POTASSIUM_IODIDE_PROPS));

    public static final BaseFlowingFluid.Properties POTASSIUM_IODIDE_PROPS =
        new BaseFlowingFluid.Properties(POTASSIUM_IODIDE_TYPE, POTASSIUM_IODIDE_SOURCE, POTASSIUM_IODIDE_FLOWING)
            .slopeFindDistance(3)
            .levelDecreasePerBlock(2)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.POTASSIUM_IODIDE_BLOCK.get())
            .bucket(() -> ModItems.POTASSIUM_IODIDE_BUCKET.get());

    // =========================================================================
    // BIOLUMINESCENT PLANKTON  (deep ocean — slow, dark until disturbed)
    // =========================================================================
    // Plankton tint endpoints — ARGB
    private static final int PLANKTON_DARK_A = 0xC0, PLANKTON_DARK_R = 0x08, PLANKTON_DARK_G = 0x18, PLANKTON_DARK_B = 0x30;
    private static final int PLANKTON_LIT_A  = 0xC0, PLANKTON_LIT_R  = 0x20, PLANKTON_LIT_G  = 0xDD, PLANKTON_LIT_B  = 0xFF;

    /** Linearly interpolate between dark indigo and bright cyan based on glow level (0–4). */
    private static int planktonTint(int glow) {
        if (glow <= 0) return (PLANKTON_DARK_A << 24) | (PLANKTON_DARK_R << 16) | (PLANKTON_DARK_G << 8) | PLANKTON_DARK_B;
        if (glow >= PlanktonBlock.MAX_GLOW) return (PLANKTON_LIT_A << 24) | (PLANKTON_LIT_R << 16) | (PLANKTON_LIT_G << 8) | PLANKTON_LIT_B;
        float t = (float) glow / PlanktonBlock.MAX_GLOW;
        int a = (int) (PLANKTON_DARK_A + (PLANKTON_LIT_A - PLANKTON_DARK_A) * t);
        int r = (int) (PLANKTON_DARK_R + (PLANKTON_LIT_R - PLANKTON_DARK_R) * t);
        int g = (int) (PLANKTON_DARK_G + (PLANKTON_LIT_G - PLANKTON_DARK_G) * t);
        int b = (int) (PLANKTON_DARK_B + (PLANKTON_LIT_B - PLANKTON_DARK_B) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static final DeferredHolder<FluidType, FluidType> PLANKTON_TYPE =
        FLUID_TYPES.register("plankton", () -> {
            ResourceLocation still = ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/plankton_still");
            ResourceLocation flow  = ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/plankton_flow");
            return new FluidType(FluidType.Properties.create()
                .density(1500)
                .viscosity(3000)
            ) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override public ResourceLocation getStillTexture()   { return still; }
                        @Override public ResourceLocation getFlowingTexture() { return flow;  }
                        @Override public int getTintColor()                   { return planktonTint(0); }

                        @Override
                        public int getTintColor(net.neoforged.neoforge.fluids.FluidStack stack) {
                            return planktonTint(0);
                        }

                        @Override
                        public int getTintColor(net.minecraft.world.level.material.FluidState state,
                                                net.minecraft.world.level.BlockAndTintGetter getter,
                                                net.minecraft.core.BlockPos pos) {
                            if (pos != null && getter != null) {
                                net.minecraft.world.level.block.state.BlockState blockState = getter.getBlockState(pos);
                                if (blockState.getBlock() instanceof PlanktonBlock) {
                                    return planktonTint(blockState.getValue(PlanktonBlock.GLOW));
                                }
                            }
                            return planktonTint(0);
                        }

                        @Override
                        public org.joml.Vector3f modifyFogColor(net.minecraft.client.Camera camera, float partialTick,
                                net.minecraft.client.multiplayer.ClientLevel level, int renderDistance,
                                float darkenWorldAmount, org.joml.Vector3f fluidFogColor) {
                        int c = planktonTint(0);
                            float r = ((c >> 16) & 0xFF) / 255.0f;
                            float g = ((c >> 8) & 0xFF) / 255.0f;
                            float b = (c & 0xFF) / 255.0f;
                            return new org.joml.Vector3f(r, g, b);
                        }

                        @Override
                        public void modifyFogRender(net.minecraft.client.Camera camera,
                                net.minecraft.client.renderer.FogRenderer.FogMode mode, float renderDistance,
                                float partialTick, float nearDistance, float farDistance,
                                com.mojang.blaze3d.shaders.FogShape shape) {
                            com.mojang.blaze3d.systems.RenderSystem.setShaderFogStart(-2.0f);
                            com.mojang.blaze3d.systems.RenderSystem.setShaderFogEnd(6.0f);
                            com.mojang.blaze3d.systems.RenderSystem.setShaderFogShape(com.mojang.blaze3d.shaders.FogShape.SPHERE);
                        }
                    });
                }
            };
        });

    public static final DeferredHolder<Fluid, FlowingFluid> PLANKTON_SOURCE =
        FLUIDS.register("plankton", () -> new SlowSource(ModFluids.PLANKTON_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> PLANKTON_FLOWING =
        FLUIDS.register("plankton_flowing", () -> new SlowFlowing(ModFluids.PLANKTON_PROPS));

    public static final BaseFlowingFluid.Properties PLANKTON_PROPS =
        new BaseFlowingFluid.Properties(PLANKTON_TYPE, PLANKTON_SOURCE, PLANKTON_FLOWING)
            .slopeFindDistance(3)
            .levelDecreasePerBlock(1)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.PLANKTON_BLOCK.get())
            .bucket(() -> ModItems.PLANKTON_BUCKET.get());

    // =========================================================================
    // ACID  (bright green, corrosive — medium speed)
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> ACID_TYPE =
        FLUID_TYPES.register("acid", () -> makeFluidType(
            0xC044FF22,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/acid_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/acid_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> ACID_SOURCE =
        FLUIDS.register("acid", () -> new BaseFlowingFluid.Source(ModFluids.ACID_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> ACID_FLOWING =
        FLUIDS.register("acid_flowing", () -> new BaseFlowingFluid.Flowing(ModFluids.ACID_PROPS));

    public static final BaseFlowingFluid.Properties ACID_PROPS =
        new BaseFlowingFluid.Properties(ACID_TYPE, ACID_SOURCE, ACID_FLOWING)
            .slopeFindDistance(3)
            .levelDecreasePerBlock(1)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.ACID_BLOCK.get())
            .bucket(() -> ModItems.ACID_BUCKET.get());

    // =========================================================================
    // CRYSTAL SOLUTION  (clear blue, grows crystals, crystallizes skeletons)
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> CRYSTAL_SOLUTION_TYPE =
        FLUID_TYPES.register("crystal_solution", () -> makeFluidType(
            0xC088CCFF,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/crystal_solution_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/crystal_solution_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> CRYSTAL_SOLUTION_SOURCE =
        FLUIDS.register("crystal_solution", () -> new SlowSource(ModFluids.CRYSTAL_SOLUTION_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> CRYSTAL_SOLUTION_FLOWING =
        FLUIDS.register("crystal_solution_flowing", () -> new SlowFlowing(ModFluids.CRYSTAL_SOLUTION_PROPS));

    public static final BaseFlowingFluid.Properties CRYSTAL_SOLUTION_PROPS =
        new BaseFlowingFluid.Properties(CRYSTAL_SOLUTION_TYPE, CRYSTAL_SOLUTION_SOURCE, CRYSTAL_SOLUTION_FLOWING)
            .slopeFindDistance(3)
            .levelDecreasePerBlock(1)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.CRYSTAL_SOLUTION_BLOCK.get())
            .bucket(() -> ModItems.CRYSTAL_SOLUTION_BUCKET.get());

    // =========================================================================
    // RAINBOW INDICATOR  (neutral green, shifts red↔violet with acid/base)
    // =========================================================================

    /** Rainbow pH tint colors: 0=red, 1=orange, 2=yellow, 3=green, 4=blue, 5=indigo, 6=violet */
    private static final int[] INDICATOR_TINTS = {
        0xC0FF2020,  // pH 0 — red (most acidic)
        0xC0FF8820,  // pH 1 — orange
        0xC0FFDD20,  // pH 2 — yellow
        0xC020DD40,  // pH 3 — green (neutral)
        0xC02080FF,  // pH 4 — blue
        0xC04030CC,  // pH 5 — indigo
        0xC09020DD,  // pH 6 — violet (most basic)
    };

    private static int indicatorTint(int ph) {
        return INDICATOR_TINTS[Math.max(0, Math.min(IndicatorBlock.MAX_PH, ph))];
    }

    public static final DeferredHolder<FluidType, FluidType> INDICATOR_TYPE =
        FLUID_TYPES.register("indicator", () -> {
            ResourceLocation still = ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/indicator_still");
            ResourceLocation flow  = ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/indicator_flow");
            return new FluidType(FluidType.Properties.create()
                .density(1000)
                .viscosity(1000)
            ) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override public ResourceLocation getStillTexture()   { return still; }
                        @Override public ResourceLocation getFlowingTexture() { return flow;  }
                        @Override public int getTintColor()                   { return indicatorTint(IndicatorBlock.NEUTRAL_PH); }

                        @Override
                        public int getTintColor(net.neoforged.neoforge.fluids.FluidStack stack) {
                            return indicatorTint(IndicatorBlock.NEUTRAL_PH);
                        }

                        @Override
                        public int getTintColor(net.minecraft.world.level.material.FluidState state,
                                                net.minecraft.world.level.BlockAndTintGetter getter,
                                                net.minecraft.core.BlockPos pos) {
                            if (pos != null && getter != null) {
                                net.minecraft.world.level.block.state.BlockState blockState = getter.getBlockState(pos);
                                if (blockState.getBlock() instanceof IndicatorBlock) {
                                    return indicatorTint(blockState.getValue(IndicatorBlock.PH));
                                }
                            }
                            return indicatorTint(IndicatorBlock.NEUTRAL_PH);
                        }

                        @Override
                        public org.joml.Vector3f modifyFogColor(net.minecraft.client.Camera camera, float partialTick,
                                net.minecraft.client.multiplayer.ClientLevel level, int renderDistance,
                                float darkenWorldAmount, org.joml.Vector3f fluidFogColor) {
                        int c = indicatorTint(IndicatorBlock.NEUTRAL_PH);
                            float r = ((c >> 16) & 0xFF) / 255.0f;
                            float g = ((c >> 8) & 0xFF) / 255.0f;
                            float b = (c & 0xFF) / 255.0f;
                            return new org.joml.Vector3f(r, g, b);
                        }

                        @Override
                        public void modifyFogRender(net.minecraft.client.Camera camera,
                                net.minecraft.client.renderer.FogRenderer.FogMode mode, float renderDistance,
                                float partialTick, float nearDistance, float farDistance,
                                com.mojang.blaze3d.shaders.FogShape shape) {
                            com.mojang.blaze3d.systems.RenderSystem.setShaderFogStart(-2.0f);
                            com.mojang.blaze3d.systems.RenderSystem.setShaderFogEnd(6.0f);
                            com.mojang.blaze3d.systems.RenderSystem.setShaderFogShape(com.mojang.blaze3d.shaders.FogShape.SPHERE);
                        }
                    });
                }
            };
        });

    public static final DeferredHolder<Fluid, FlowingFluid> INDICATOR_SOURCE =
        FLUIDS.register("indicator", () -> new BaseFlowingFluid.Source(ModFluids.INDICATOR_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> INDICATOR_FLOWING =
        FLUIDS.register("indicator_flowing", () -> new BaseFlowingFluid.Flowing(ModFluids.INDICATOR_PROPS));

    public static final BaseFlowingFluid.Properties INDICATOR_PROPS =
        new BaseFlowingFluid.Properties(INDICATOR_TYPE, INDICATOR_SOURCE, INDICATOR_FLOWING)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.INDICATOR_BLOCK.get())
            .bucket(() -> ModItems.INDICATOR_BUCKET.get());

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

                    @Override
                    public org.joml.Vector3f modifyFogColor(net.minecraft.client.Camera camera, float partialTick,
                            net.minecraft.client.multiplayer.ClientLevel level, int renderDistance,
                            float darkenWorldAmount, org.joml.Vector3f fluidFogColor) {
                        float r = ((tintColor >> 16) & 0xFF) / 255.0f;
                        float g = ((tintColor >> 8) & 0xFF) / 255.0f;
                        float b = (tintColor & 0xFF) / 255.0f;
                        return new org.joml.Vector3f(r, g, b);
                    }

                    @Override
                    public void modifyFogRender(net.minecraft.client.Camera camera,
                            net.minecraft.client.renderer.FogRenderer.FogMode mode, float renderDistance,
                            float partialTick, float nearDistance, float farDistance,
                            com.mojang.blaze3d.shaders.FogShape shape) {
                        com.mojang.blaze3d.systems.RenderSystem.setShaderFogStart(-2.0f);
                        com.mojang.blaze3d.systems.RenderSystem.setShaderFogEnd(6.0f);
                        com.mojang.blaze3d.systems.RenderSystem.setShaderFogShape(com.mojang.blaze3d.shaders.FogShape.SPHERE);
                    }
                });
            }
        };
    }
}
