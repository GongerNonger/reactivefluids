# Reactive Fluids — Claude Code Context

## Mod Overview

Reactive Fluids is a NeoForge 1.21.1 Minecraft mod that adds colored reactive fluid systems. The core mechanic: pour **resin** (slow, viscous) and **hardener** (fast-flowing) of the same color family into the same space and they react to form a solid **epoxy** block. Three color families exist — Amber (warm orange/gold), Cobalt (deep blue/cyan), and Jade (deep green/lime). Each family has regular resin, glowing resin (resin + glow ink sac), and hardener variants. Epoxy comes in three forms: transparent (glass-like), glowing (light-emitting), and opaque (solid stone-like).

## Architecture

- **Java package:** `com.reactivefluids`
- **Mod ID:** `reactivefluids`
- **NeoForge version:** 21.1.172, Minecraft 1.21.1, Java 21
- **Build:** Gradle 8.8, plugin `net.neoforged.gradle.userdev 7.0.145`
- **IMPORTANT build command** — always requires explicit JAVA_HOME:
  ```
  JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.10.7-hotspot" ./gradlew build
  ```

## Fluid System

Three color families, each with the same structure:

| Family | Resin color (ARGB) | Hardener color (ARGB) | Glowing resin color (ARGB) |
|--------|--------------------|----------------------|---------------------------|
| Amber  | `0xBEDC7814` | `0x96FFD232` | `0xCEFF9C00` |
| Cobalt | `0xBE1E32C8` | `0x9600B4E6` | `0xCE00D4FF` |
| Jade   | `0xBE008C46` | `0x9650DC64` | `0xCE00FF88` |

**Resin** — slow fluid (tick delay 25, `slopeFindDistance` 2, `levelDecreasePerBlock` 2). Alpha 80 in textures. Uses `SlowSource`/`SlowFlowing` inner classes. Viscosity 3000, density 1500.

**Hardener** — fast fluid (standard tick delay 5, `slopeFindDistance` 4, `levelDecreasePerBlock` 1). Alpha 60 in textures. Standard `BaseFlowingFluid.Source/Flowing`.

**Glowing resin** — same flow properties as regular resin. Alpha 100. Fluid block emits light level 8 while flowing. Produces glowing epoxy when reacted.

All fluid blocks use `TranslucentLiquidBlock` (extends `LiquidBlock`) to guarantee the translucent render layer is applied so texture alpha is respected. `TranslucentLiquidBlock.registerRenderLayers()` is called from `ClientEvents`.

## Reaction Mechanic — Throwable Hardener

The **only** reaction system is the throwable hardener. `FluidInteractionRegistry` and all hardener fluids have been removed entirely.

`HardenerItem` extends `Item` — right-click throws a `HardenerEntity` projectile. `HardenerEntity` extends `ThrowableItemProjectile` and on impact performs a **BFS flood-fill** converting up to 1024 connected same-color resin blocks instantly to epoxy. Three throwable items: `AMBER_HARDENER`, `COBALT_HARDENER`, `JADE_HARDENER` (stack to 16). The entity reads `getItem().getItem()` to determine color mapping; no separate entity type per color is needed.

## Block Types

All epoxy blocks use `EpoxyBlock` (extends `Block`) which emits **CLOUD + POOF particles** server-side when placed by a fluid reaction (detected by checking `oldState.getFluidState().isEmpty()`).

| Type | Strength | Sound | Light | Rendering | Notes |
|------|----------|-------|-------|-----------|-------|
| Transparent epoxy | 2.5/6.0 | GLASS | 0 | translucent | noOcclusion, isViewBlocking=false |
| Glowing epoxy | 2.5/6.0 | GLASS | 12 | translucent | noOcclusion, isViewBlocking=false |
| Opaque epoxy | 3.0/8.0 | STONE | 0 | solid | Standard `Block`, no special render |
| Glowing resin block | 100f (unbreakable) | — | 8 | translucent | `TranslucentLiquidBlock`, fluid |

Opaque epoxy is named "Solid Epoxy" in lang (e.g., "Amber Solid Epoxy").

## Key Files

| File | Purpose |
|------|---------|
| `ReactiveFluids.java` | Main mod class. Registers all `DeferredRegister`s in constructor: fluid types, fluids, blocks, items, entities, creative tab. No `commonSetup` / `FluidInteractionRegistry`. |
| `ModFluids.java` | All `FluidType` and `Fluid` registrations. Contains `SlowSource`/`SlowFlowing` inner classes. `makeFluidType()` helper wires `IClientFluidTypeExtensions`. |
| `ModBlocks.java` | All block registrations: `TranslucentLiquidBlock` for fluids, `EpoxyBlock` for transparent/glowing epoxy, `Block` for opaque epoxy. |
| `ModItems.java` | `BucketItem` for all fluids, `BlockItem` for all epoxy blocks. `HardenerItem` entries (`AMBER_HARDENER`, `COBALT_HARDENER`, `JADE_HARDENER`) — `stacksTo(16)`. |
| `ModCreativeTab.java` | Single creative tab "Reactive Fluids" — buckets (resin, glowing resin, hardener items), then transparent/glowing/opaque epoxy per color. |
| `ModEntities.java` | Registers `HardenerEntity` entity type. Wired into `ReactiveFluids.java` constructor via `ModEntities.ENTITY_TYPES.register(modEventBus)`. |
| `ClientEvents.java` | `@EventBusSubscriber` on MOD bus, CLIENT dist. Registers `ThrownItemRenderer` for `HardenerEntity`. Calls `TranslucentLiquidBlock.registerRenderLayers()` and sets epoxy blocks to `RenderType.translucent()`. |
| `EpoxyBlock.java` | `Block` subclass that emits CLOUD+POOF particles server-side when placed on top of a fluid block. |
| `HardenerEntity.java` | `ThrowableItemProjectile` subclass. `onHit` tries 7 candidate positions (hit pos + 6 neighbors). `tryFlood()` does BFS up to 1024 blocks, converts resin→epoxy and glowingResin→glowingEpoxy. |
| `HardenerItem.java` | `Item` subclass. `use()` plays splash potion throw sound, spawns `HardenerEntity` via `shootFromRotation`. Consumes 1 item in survival. |
| `TranslucentLiquidBlock.java` | `LiquidBlock` subclass with `@OnlyIn(CLIENT)` static `registerRenderLayers()` that sets all fluid blocks to `RenderType.translucent()`. |
| `generate_textures.py` | Procedural texture generator (stdlib only, no pip deps). Run with `python generate_textures.py`. |
| `generate_textures_ai.py` | AI-based texture generator. Requires `OPENAI_API_KEY`. See `AI_TEXTURE_SETUP.md`. |

## Texture Generation

- **Still textures:** 16×16 PNG, 32 frames stacked vertically (16×512 total). 5 pure-X + 5 pure-Y sinusoidal wave layers + dual specular blobs + horizontal sheen gradient. No diagonal `sin(x+y)` terms.
- **Flow textures:** 16×16 PNG, 32 frames (16×512). Pure vertical ribbon system — per-column static brightness profile, 3 drip waves per column, downward scroll only.
- **Epoxy block textures:** Static 16×16 PNG with seamless wave pattern, gloss highlight, edge darkening.
- **Bucket sprites:** 16×16 PNG with steel handle/walls + per-fluid fill color with highlight/shadow zones.
- **Alpha:** Resin 80, hardener 60, glowing resin 100, transparent epoxy ~90, glowing epoxy ~110.
- **Animation:** Resin uses `interpolate: false` (sharp/thick). Hardener uses `interpolate: true` (smooth/watery).

Run after any color or art change:
```
cd /c/Users/Administrator/Documents/reactivefluids
python generate_textures.py
```

## Build & Deploy

```
cd /c/Users/Administrator/Documents/reactivefluids
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.10.7-hotspot" ./gradlew build
cp build/libs/reactivefluids-1.0.0.jar "/c/Users/Administrator/curseforge/minecraft/Instances/Reactive Fluids Dev/mods/"
```

Test instance: "Reactive Fluids Dev" in CurseForge.

## Known Issues / Design Decisions

- **NeoForge fluid renderer ignores tint alpha** — transparency comes only from texture alpha values (80/60/100), not the ARGB tint color's alpha byte.
- **TranslucentLiquidBlock** is required because NeoForge's default fluid pass can override the `ClientEvents` render layer assignment; setting it at the block level guarantees translucency.
- **BUBBLE/SPLASH particles don't work outside water** — `EpoxyBlock` uses CLOUD + POOF instead.
- **Hardener is a throwable item, not a fluid** — `ModFluids` has no hardener fluids; `ModBlocks` has no hardener liquid blocks. The only reaction pathway is `HardenerEntity` BFS flood-fill.

## Adding a New Fluid Color Family

1. Add fluid type + source/flowing fluid + `BaseFlowingFluid.Properties` entries to `ModFluids` (resin and glowing resin variants only — no hardener fluid).
2. Add `TranslucentLiquidBlock` registrations to `ModBlocks` (resin block, glowing resin block with `lightLevel(state -> 8)`).
3. Add `EpoxyBlock` registrations to `ModBlocks` (transparent, glowing, opaque variants).
4. Add bucket `BucketItem` entries, `HardenerItem` entry, and epoxy `BlockItem` entries to `ModItems`.
5. Add all new items to `ModCreativeTab`.
6. Add BFS cases to `HardenerEntity.tryFlood()` (new `else if` branch for the new color).
7. Add lang entries to `en_us.json` (bucket items, fluid blocks, epoxy blocks, hardener item).
8. Add to `FLUIDS`, `EPOXY`, `BUCKET_PALETTES`, and `BUCKET_COLORS` in `generate_textures.py`.
9. Run `python generate_textures.py`.
