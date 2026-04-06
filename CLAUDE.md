# Reactive Fluids — Claude Code Context

## Mod Overview

Reactive Fluids is a NeoForge 1.21.1 Minecraft mod that adds colored reactive fluid systems. The core mechanic: pour **resin** (slow, viscous) and **hardener** (fast-flowing) of the same color family into the same space and they react to form a solid **epoxy** block. Three color families exist — Amber (warm orange/gold), Cobalt (deep blue/cyan), and Jade (deep green/lime). Each family has regular resin, glowing resin (resin + glow ink sac), and hardener variants. Epoxy comes in three forms: transparent (glass-like), glowing (light-emitting), and opaque (solid stone-like).

## Architecture

- **Java package:** `com.reactivefluids`
- **Mod ID:** `reactivefluids`
- **NeoForge version:** 21.1.172, Minecraft 1.21.1, Java 21
- **Build:** Gradle 8.8, plugin `net.neoforged.gradle.userdev 7.0.145`
- **Build command:**
  ```
  ./gradlew build
  ```
  Gradle's toolchain auto-detects Java 21 on the local machine (`auto-detect=true`, `auto-download=true` in `gradle.properties`). No `JAVA_HOME` needed.

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
| `ModParticles.java` | Registers 5 custom `SimpleParticleType`s: `disintegrate`, `dancing_light`, `spectral`, `fog_cloud`, `necrotic`. |
| `ModDimensions.java` | Declares `ResourceKey<Level>` for `mansion` and `grotto` pocket dimensions. |
| `AcidBlock.java` | `TranslucentLiquidBlock` subclass. Source blocks drill downward dissolving stone/dirt, stopping at ores and immune blocks. |
| `FoamBlock.java` | `Block` subclass. Grows upward (max 20) and outward via scheduled ticks, simulating elephant's toothpaste eruption. |
| `PlanktonBlock.java` | `TranslucentLiquidBlock` subclass. `GLOW` block state (0–4); lit by entities passing through, cascades to neighbors, fades over time. |
| `CrystalSolutionBlock.java` | `TranslucentLiquidBlock` subclass. Grows `CrystalBlock` on random ticks; converts skeletons to `CrystallizedSkeleton` after 30 s. |
| `IndicatorBlock.java` | `TranslucentLiquidBlock` subclass. `PH` block state (0–6); shifted by `ReagentEntity` BFS flood-fill. |
| `ReagentItem.java` | Throwable `ACID_REAGENT` / `BASE_REAGENT`. Spawns `ReagentEntity`; implements `ProjectileItem` for dispenser support. |
| `ReagentEntity.java` | `ThrowableItemProjectile`. BFS flood-fills up to 512 indicator blocks, shifting pH ±1 based on thrown item type. |
| `CrystalBlock.java` | Solid crystal deposit block grown by `CrystalSolutionBlock`. |
| `CrystallizedSkeleton.java` | Hostile mob — skeleton converted by crystal solution. Has `CrystalOverlayLayer` render layer and `CrystalLaserGoal`. |
| `ArcaneBarrierBlock.java` | Translucent shimmering dome block placed by Tiny Hut scroll. |
| `ReturnPortalBlock.java` | Exit portal block placed inside pocket dimensions (Mansion, Grotto) to return players to overworld. |
| `GaldersTower.java` | Helper class that builds the 7×11 two-story Galder's Tower structure with randomly-themed rooms. |

## Texture Generation

- **Still textures:** 16×16 PNG, 32 frames stacked vertically (16×512 total). 5 pure-X + 5 pure-Y sinusoidal wave layers + dual specular blobs + horizontal sheen gradient. No diagonal `sin(x+y)` terms.
- **Flow textures:** 16×16 PNG, 32 frames (16×512). Pure vertical ribbon system — per-column static brightness profile, 3 drip waves per column, downward scroll only.
- **Epoxy block textures:** Static 16×16 PNG with seamless wave pattern, gloss highlight, edge darkening.
- **Bucket sprites:** 16×16 PNG with steel handle/walls + per-fluid fill color with highlight/shadow zones.
- **Alpha:** Resin 80, hardener 60, glowing resin 100, transparent epoxy ~90, glowing epoxy ~110.
- **Animation:** Resin uses `interpolate: false` (sharp/thick). Hardener uses `interpolate: true` (smooth/watery).

Run after any color or art change:
```
python generate_textures.py
```

## Build & Deploy

```
./gradlew build
```

To deploy to a local CurseForge test instance, copy `build/libs/reactivefluids-1.0.0.jar` to your instance's `mods/` folder.

## Special Reactive Fluids

Beyond the color-family resin/epoxy system, the mod includes five standalone reactive fluid systems. Each has its own fluid type, source/flowing registrations in `ModFluids`, and a custom block class.

### Elephant's Toothpaste
- **Fluids:** `HYDROGEN_PEROXIDE` (pale blue) + `POTASSIUM_IODIDE` (tan/brown)
- **Reaction:** `FluidInteractionRegistry` triggers when they meet — produces `FOAM_BLOCK`
- **`FoamBlock`** — grows upward via scheduled ticks (max height 20). Blooms outward as it rises (cone/mushroom shape). Only the reaction-placed base block initiates growth; player-placed or side-bloomed foam does not cascade. Emits CLOUD particles during eruption.

### Bioluminescent Plankton
- **Fluid:** `PLANKTON` (deep ocean blue)
- **`PlanktonBlock`** — has `GLOW` integer block state (0–4). When a `LivingEntity` moves through it, `entityInside` calls `illuminate()` which BFS-cascades glow to neighboring plankton blocks within radius 3, with brightness falling off by distance. Glow fades stepwise over ~3 seconds via scheduled ticks. Light level = `GLOW * 3` (0–12).
- Emits a bright blue particle eruption when lit (registered separately).

### Acid
- **Fluid:** `ACID` (light green)
- **`AcidBlock`** — only source blocks (amount ≥ 8) act. On `tick`, dissolves the block directly below if it is dissolvable (stone family, dirt, sand, gravel, etc.) and is not immune (bedrock, obsidian, reinforced deepslate, etc.) and is not an ore. Ores are deliberately left exposed. Drills a 1×1 shaft straight down; emits SMOKE + CLOUD particles on each dissolve. Rescheduled every 8 ticks to continue drilling.

### Crystal Solution
- **Fluid:** `CRYSTAL_SOLUTION` (pale icy blue)
- **`CrystalSolutionBlock`** — source blocks grow `CrystalBlock` instances on adjacent surfaces via `randomTick` (1-in-15 chance). Requires at least one solid or existing crystal face adjacent to the target. Also: skeletons standing in the fluid accumulate `crystal_soak_time` ticks in persistent data; at 600 ticks (30 s) they convert to a `CrystallizedSkeleton` via `convertTo`.
- **`CrystalBlock`** — the grown crystal deposit block.
- **`CrystallizedSkeleton`** — hostile mob variant of skeleton with crystal overlay render layer (`CrystalOverlayLayer`). Has a `CrystalLaserGoal` for ranged crystal attacks.

### Rainbow Indicator
- **Fluid:** `INDICATOR` (starts green/neutral)
- **`IndicatorBlock`** — has `PH` integer block state (0–6). 0 = red (acid), 3 = green (neutral), 6 = violet (base). Does not shift on its own.
- **`ReagentItem`** — throwable item (like hardener). Two variants: `ACID_REAGENT` (shifts pH −1) and `BASE_REAGENT` (shifts pH +1). Spawns `ReagentEntity` on right-click.
- **`ReagentEntity`** — `ThrowableItemProjectile`. On impact, BFS flood-fills up to 512 connected indicator blocks and shifts each block's PH by ±1 depending on the thrown item. Supports dispenser fire via `ProjectileItem`.

## Spell Scroll System

A WIP D&D 5e–inspired system of 22 consumable spell scrolls. All scrolls are items that trigger their effect on right-click (`use()`). Most stack to 1 (except `MOLD_EARTH_SCROLL` which stacks to 16). All registered in `ModItems`.

### Scroll Items

| Item ID | Class | D&D Level | Effect |
|---------|-------|-----------|--------|
| `mold_earth_scroll` | `MoldEarthScrollItem` | Cantrip | Excavates a 3×3×3 cube of soft earth |
| `fog_cloud_scroll` | `FogCloudScrollItem` | 1st | Spawns a `FogCloudEntity` that blinds nearby hostile mobs |
| `plant_growth_scroll` | `PlantGrowthScrollItem` | 3rd | Accelerates vegetation growth in wide radius |
| `erupting_earth_scroll` | `EruptingEarthScrollItem` | 3rd | Violently erupts blocks upward in a localized explosion |
| `tiny_hut_scroll` | `TinyHutScrollItem` | 3rd | Builds an `ArcaneBarrierBlock` dome shelter lasting 10 min |
| `raise_dead_scroll` | `RaiseDeadScrollItem` | 3rd | Cast on rotten flesh block or bone block — raises `RaisedZombieEntity` or `RaisedSkeletonEntity` |
| `control_water_scroll` | `ControlWaterScrollItem` | 4th | Parts water blocks temporarily (60 s); tracked by `ControlWaterData` |
| `conjure_animals_scroll` | `ConjureAnimalsScrollItem` | 4th | Summons spectral wolves, foxes, and axolotls for 2 min |
| `wall_of_stone_scroll` | `WallOfStoneScrollItem` | 5th | Places a permanent stone brick wall |
| `passwall_scroll` | `PasswallScrollItem` | 5th | Creates a temporary tunnel through solid walls; tracked by `PasswallData` |
| `dimension_door_scroll` | `DimensionDoorScrollItem` | 4th | Teleports player up to 64 blocks in look direction with safety checks |
| `dancing_lights_scroll` | `DancingLightsScrollItem` | Cantrip | Spawns 4 orbiting `DancingLightEntity` instances for 1 min |
| `disintegrate_scroll` | `DisintegrateScrollItem` | 6th | Fires a `DisintegrateBeamEntity` green ray that destroys the target block/entity |
| `reverse_gravity_scroll` | `ReverseGravityScrollItem` | 7th | Launches all nearby creatures skyward |
| `steed_scroll` | `SteedScrollItem` | 2nd | Conjures a rideable `PhantomSteedEntity` for 10 min |
| `move_earth_scroll` | `MoveEarthScrollItem` | 6th | Raises or lowers soft terrain (sneak to lower) |
| `bones_of_the_earth_scroll` | `BonesOfTheEarthScrollItem` | 6th | Erupts stone pillars from the ground |
| `arcane_gate_scroll` | `ArcaneGateScrollItem` | 6th | Two-use linked portal pair; state tracked by `ArcaneGateData` |
| `tower_scroll` | `TowerScrollItem` | 4th | Builds Galder's Tower (7×11 two-story structure via `GaldersTower`); tracked by `ConjuredTowerData` |
| `magnificent_mansion_scroll` | `MagnificentMansionScrollItem` | 7th | Opens portal to `ModDimensions.MANSION` pocket dimension; tracked by `MansionData` + `PocketDimensionData` |
| `gongers_grotto_scroll` | `GongersGrottoScrollItem` | 7th | Opens portal to `ModDimensions.GROTTO` pocket dimension; tracked by `GrottoData` + `PocketDimensionData` |
| `meteor_swarm_scroll` | `MeteorSwarmScrollItem` | 9th | Spawns 4 `MeteorEntity` projectiles from above |

### Supporting Infrastructure

**Persistence (SavedData):**

| Class | Tracks |
|-------|--------|
| `ArcaneGateData` | Active portal pairs, entity teleportation between portals, expiry |
| `TinyHutData` | Dome structure positions and expiry |
| `PasswallData` | Tunnel block positions and expiry |
| `ControlWaterData` | Parted water block sets and expiry |
| `ConjuredTowerData` | Tower footprint positions and expiry |
| `MansionData` | Magnificent Mansion portal instances and expiry |
| `GrottoData` | Gonger's Grotto portal instances and expiry |
| `PocketDimensionData` | Player return positions for pocket dimensions |

**Special Blocks:**

| Block | Class | Notes |
|-------|-------|-------|
| `arcane_barrier` | `ArcaneBarrierBlock` | Translucent shimmering dome block used by Tiny Hut |
| `return_portal` | `ReturnPortalBlock` | Exit block placed inside pocket dimensions |
| `rotten_flesh_block` | `Block` | Crafted from 9 rotten flesh; used as catalyst for Raise Dead |

**Custom Entities (spell-spawned):**

| Entity | Class | Spawned By |
|--------|-------|------------|
| `phantom_steed` | `PhantomSteedEntity` | Steed scroll |
| `dancing_light` | `DancingLightEntity` | Dancing Lights scroll |
| `disintegrate_beam` | `DisintegrateBeamEntity` | Disintegrate scroll |
| `spectral_wolf` | `SpectralWolfEntity` | Conjure Animals scroll |
| `spectral_fox` | `SpectralFoxEntity` | Conjure Animals scroll |
| `spectral_axolotl` | `SpectralAxolotlEntity` | Conjure Animals scroll |
| `fog_cloud` | `FogCloudEntity` | Fog Cloud scroll |
| `meteor` | `MeteorEntity` | Meteor Swarm scroll |
| `raised_zombie` | `RaisedZombieEntity` | Raise Dead scroll |
| `raised_skeleton` | `RaisedSkeletonEntity` | Raise Dead scroll |
| `crystallized_skeleton` | `CrystallizedSkeleton` | Crystal Solution fluid (mob conversion) |

**Custom Particles (`ModParticles`):**

| ID | Class | Used By |
|----|-------|---------|
| `disintegrate` | `DisintegrateParticle` | Disintegrate beam |
| `dancing_light` | `DancingLightParticle` | Dancing Lights entities |
| `spectral` | `SpectralParticle` | Spectral creature summons |
| `fog_cloud` | `FogCloudParticle` | Fog Cloud entity |
| `necrotic` | `NecroticParticle` | Raise Dead / raised undead |

**Dimensions (`ModDimensions`):**

| Key | Used By |
|-----|---------|
| `reactivefluids:mansion` | Magnificent Mansion scroll |
| `reactivefluids:grotto` | Gonger's Grotto scroll |

### Adding a New Spell Scroll

1. Create `YourSpellScrollItem.java` extending `Item`. Implement the effect in `use()`.
2. Register it in `ModItems`: `ITEMS.register("your_spell_scroll", () -> new YourSpellScrollItem(...))`.
3. Add to `ModCreativeTab.displayItems`.
4. If the spell spawns a custom entity: add entity type to `ModEntities`, add renderer registration in `ClientEvents`.
5. If the spell needs timed persistence: create a `SavedData` subclass following the pattern of `TinyHutData` or `PasswallData`.
6. If the spell builds a structure: follow `GaldersTower` — build via `level.setBlock` calls from a helper class.
7. Add lang entry to `en_us.json`: `"item.reactivefluids.your_spell_scroll": "Your Spell Scroll"`.
8. Add a scroll texture PNG to `assets/reactivefluids/textures/item/` and model JSON to `assets/reactivefluids/models/item/`.

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
