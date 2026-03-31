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
    public static final DeferredHolder<FluidType, FluidType> PLANKTON_TYPE =
        FLUID_TYPES.register("plankton", () -> makeFluidType(
            0xC0081830,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/plankton_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/plankton_flow")
        ));

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
    // LIQUID NITROGEN  (pale icy blue — fast, watery)
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> LIQUID_NITROGEN_TYPE =
        FLUID_TYPES.register("liquid_nitrogen", () -> makeFluidType(
            0xA0C8EEFF,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/liquid_nitrogen_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/liquid_nitrogen_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_NITROGEN_SOURCE =
        FLUIDS.register("liquid_nitrogen", () -> new BaseFlowingFluid.Source(ModFluids.LIQUID_NITROGEN_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> LIQUID_NITROGEN_FLOWING =
        FLUIDS.register("liquid_nitrogen_flowing", () -> new BaseFlowingFluid.Flowing(ModFluids.LIQUID_NITROGEN_PROPS));

    public static final BaseFlowingFluid.Properties LIQUID_NITROGEN_PROPS =
        new BaseFlowingFluid.Properties(LIQUID_NITROGEN_TYPE, LIQUID_NITROGEN_SOURCE, LIQUID_NITROGEN_FLOWING)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.LIQUID_NITROGEN_BLOCK.get())
            .bucket(() -> ModItems.LIQUID_NITROGEN_BUCKET.get());

    // =========================================================================
    // GREEK FIRE  (blue-green — slow, viscous like oil)
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> GREEK_FIRE_TYPE =
        FLUID_TYPES.register("greek_fire", () -> makeFluidType(
            0xC0009988,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/greek_fire_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/greek_fire_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> GREEK_FIRE_SOURCE =
        FLUIDS.register("greek_fire", () -> new SlowSource(ModFluids.GREEK_FIRE_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> GREEK_FIRE_FLOWING =
        FLUIDS.register("greek_fire_flowing", () -> new SlowFlowing(ModFluids.GREEK_FIRE_PROPS));

    public static final BaseFlowingFluid.Properties GREEK_FIRE_PROPS =
        new BaseFlowingFluid.Properties(GREEK_FIRE_TYPE, GREEK_FIRE_SOURCE, GREEK_FIRE_FLOWING)
            .slopeFindDistance(3)
            .levelDecreasePerBlock(2)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.GREEK_FIRE_BLOCK.get())
            .bucket(() -> ModItems.GREEK_FIRE_BUCKET.get());

    // =========================================================================
    // FERROFLUID  (dark metallic black — medium speed)
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> FERROFLUID_TYPE =
        FLUID_TYPES.register("ferrofluid", () -> makeFluidType(
            0xD0181820,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/ferrofluid_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/ferrofluid_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> FERROFLUID_SOURCE =
        FLUIDS.register("ferrofluid", () -> new MediumSource(ModFluids.FERROFLUID_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> FERROFLUID_FLOWING =
        FLUIDS.register("ferrofluid_flowing", () -> new MediumFlowing(ModFluids.FERROFLUID_PROPS));

    public static final BaseFlowingFluid.Properties FERROFLUID_PROPS =
        new BaseFlowingFluid.Properties(FERROFLUID_TYPE, FERROFLUID_SOURCE, FERROFLUID_FLOWING)
            .slopeFindDistance(3)
            .levelDecreasePerBlock(1)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.FERROFLUID_BLOCK.get())
            .bucket(() -> ModItems.FERROFLUID_BUCKET.get());

    // =========================================================================
    // SUPERFLUID (Helium-3)  (very pale blue, almost clear — extremely fast)
    // =========================================================================
    private static class FastSource extends BaseFlowingFluid.Source {
        FastSource(BaseFlowingFluid.Properties p) { super(p); }
        @Override public int getTickDelay(LevelReader level) { return 2; }
    }
    private static class FastFlowing extends BaseFlowingFluid.Flowing {
        FastFlowing(BaseFlowingFluid.Properties p) { super(p); }
        @Override public int getTickDelay(LevelReader level) { return 2; }
    }

    public static final DeferredHolder<FluidType, FluidType> SUPERFLUID_TYPE =
        FLUID_TYPES.register("superfluid", () -> makeFluidType(
            0x60D0E8FF,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/superfluid_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/superfluid_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> SUPERFLUID_SOURCE =
        FLUIDS.register("superfluid", () -> new FastSource(ModFluids.SUPERFLUID_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> SUPERFLUID_FLOWING =
        FLUIDS.register("superfluid_flowing", () -> new FastFlowing(ModFluids.SUPERFLUID_PROPS));

    public static final BaseFlowingFluid.Properties SUPERFLUID_PROPS =
        new BaseFlowingFluid.Properties(SUPERFLUID_TYPE, SUPERFLUID_SOURCE, SUPERFLUID_FLOWING)
            .slopeFindDistance(6)
            .levelDecreasePerBlock(1)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.SUPERFLUID_BLOCK.get())
            .bucket(() -> ModItems.SUPERFLUID_BUCKET.get());

    // =========================================================================
    // MYCELIUM SLURRY  (deep purple/brown — medium speed)
    // =========================================================================
    public static final DeferredHolder<FluidType, FluidType> MYCELIUM_SLURRY_TYPE =
        FLUID_TYPES.register("mycelium_slurry", () -> makeFluidType(
            0xB0603880,
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/mycelium_slurry_still"),
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "block/mycelium_slurry_flow")
        ));

    public static final DeferredHolder<Fluid, FlowingFluid> MYCELIUM_SLURRY_SOURCE =
        FLUIDS.register("mycelium_slurry", () -> new MediumSource(ModFluids.MYCELIUM_SLURRY_PROPS));

    public static final DeferredHolder<Fluid, FlowingFluid> MYCELIUM_SLURRY_FLOWING =
        FLUIDS.register("mycelium_slurry_flowing", () -> new MediumFlowing(ModFluids.MYCELIUM_SLURRY_PROPS));

    public static final BaseFlowingFluid.Properties MYCELIUM_SLURRY_PROPS =
        new BaseFlowingFluid.Properties(MYCELIUM_SLURRY_TYPE, MYCELIUM_SLURRY_SOURCE, MYCELIUM_SLURRY_FLOWING)
            .slopeFindDistance(3)
            .levelDecreasePerBlock(1)
            .block(() -> (net.minecraft.world.level.block.LiquidBlock) ModBlocks.MYCELIUM_SLURRY_BLOCK.get())
            .bucket(() -> ModItems.MYCELIUM_SLURRY_BUCKET.get());

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
