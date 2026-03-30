# Reactive Fluids — Changelog

## v1.3.0 — Throwable Hardener (in progress)

Infrastructure for a throwable hardener item added in parallel with the existing fluid-reaction system.

- Added `HardenerEntity` — extends `ThrownItemEntity`, BFS flood-fills up to 1024 connected resin blocks on impact, converts regular resin to transparent epoxy and glowing resin to glowing epoxy
- Added `HardenerItem` — throwable item that spawns `HardenerEntity` on right-click, plays splash potion throw sound, consumes one item in survival
- Added `ModEntities` — registers `HardenerEntity` entity type (MISC category, 0.25×0.25 hitbox, 4-chunk tracking range)
- Added `generate_textures_ai.py` — AI-based texture generator using OpenAI API (requires `OPENAI_API_KEY`, see `AI_TEXTURE_SETUP.md`)
- **Not yet complete:** `ModEntities.ENTITY_TYPES` not yet registered in `ReactiveFluids.java`; `HardenerItem` entries missing from `ModItems`; entity renderer not yet registered in `ClientEvents`; `FluidInteractionRegistry` system still active as primary reaction pathway

---

## v1.2.0 — Visual Improvements

- Procedural fluid texture generator (`generate_textures.py`) using stdlib only (no pip dependencies)
- Still textures: 5 pure-X + 5 pure-Y sinusoidal wave layers with two drifting specular blobs and a horizontal sheen gradient — no diagonal `sin(x+y)` terms to prevent stripe artifacts
- Flow textures: pure vertical ribbon system with per-column static brightness profiles and 3 drip waves per column at incommensurable periods; movement is downward-only
- Epoxy block textures: static seamless wave pattern, gloss highlight at top-left, edge darkening, random specular pixels
- Bucket sprites: 16×16 with steel gray handle/walls, per-fluid fill with highlight/shadow gradient zones
- Alpha values: resin 80 (clearly translucent), hardener 60 (very translucent/watery), glowing resin 100 (extra presence for the glow), transparent epoxy ~90, glowing epoxy ~110
- Animation: resin uses `interpolate: false` (sharp, thick drips); hardener uses `interpolate: true` (smooth, watery)
- Translucent fluid rendering enforced via `TranslucentLiquidBlock` (extends `LiquidBlock`) to override NeoForge's default opaque fluid render pass
- Epoxy blocks set to `RenderType.translucent()` in `ClientEvents`; `noOcclusion()` + `isViewBlocking=false` for glass-like appearance
- Reaction particles changed from BUBBLE/SPLASH (which don't work outside water) to CLOUD + POOF bursts in `EpoxyBlock.onPlace()`

---

## v1.1.0 — Epoxy Variants

- Added glowing resin variants (Amber/Cobalt/Jade): created by combining resin bucket with a glow ink sac
- Glowing resin fluid blocks emit light level 8 while flowing in-world
- Glowing resin + hardener via `FluidInteractionRegistry` → glowing epoxy block (light level 12, matches sea lantern)
- Added opaque epoxy variant: crafted from epoxy + ink sac, uses `Block` with `SoundType.STONE`, strength 3.0/8.0
- Transparent/glowing epoxy uses `EpoxyBlock` with `strength(2.5f, 6.0f)`, `SoundType.GLASS`
- Glowing resin buckets added to `ModItems` and creative tab
- Lang: glowing resin fluids/blocks, glowing epoxy blocks, opaque epoxy blocks (named "Solid Epoxy")

---

## v1.0.0 — Initial Release

- Three fluid families: Amber (warm orange/gold), Cobalt (deep blue/cyan), Jade (deep green/lime)
- Each family: resin fluid (slow — tick delay 25, `slopeFindDistance` 2) + hardener fluid (fast — standard tick delay, `slopeFindDistance` 4)
- `FluidInteractionRegistry`-based bidirectional reaction: touching fluids of the same color family convert to transparent epoxy
- Transparent epoxy block: `EpoxyBlock`, `noOcclusion()`, `isViewBlocking=false`, `RenderType.translucent()`
- All fluid blocks use `TranslucentLiquidBlock` to guarantee translucent render layer
- `ModFluids`: `FluidType` with `IClientFluidTypeExtensions` for still/flow textures and tint color; `SlowSource`/`SlowFlowing` inner classes for viscous resin behavior; density 1500, viscosity 3000
- `ModBlocks`: `TranslucentLiquidBlock` for all 6 fluid in-world blocks (3 resin + 3 hardener)
- `ModItems`: `BucketItem` for all 6 fluids + `BlockItem` for transparent epoxy blocks
- `ModCreativeTab`: single "Reactive Fluids" tab with Amber Epoxy as icon
- `ClientEvents`: sets epoxy blocks and fluid blocks to translucent render layer on `FMLClientSetupEvent`
- Lang file: bucket items, fluid blocks, epoxy blocks for all 3 color families
- `ReactiveFluids` main class: registers all `DeferredRegister`s in dependency order (fluid types → fluids → blocks → items → creative tab)
