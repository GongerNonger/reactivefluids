# Project History & Development Efforts

## Timeline

### March 30, 2026 — Reactive Fluids v1.0
The project began as a fluid chemistry mod. The first commit established three color families (Amber, Cobalt, Jade) of reactive fluids — slow-flowing resin and fast-flowing hardener that combine to form solid epoxy blocks.

Key technical work:
- Custom `FluidType` registrations with `IClientFluidTypeExtensions` for tinted rendering
- `SlowSource`/`SlowFlowing` inner classes for viscous resin behavior (tick delay 25, viscosity 3000)
- `TranslucentLiquidBlock` to force translucent render layer — NeoForge's default fluid pass overrides `ClientEvents` render layer assignments, so the block-level override is necessary
- `FluidInteractionRegistry` for automatic same-color resin+hardener reactions

Same day — rapid iteration through v1.1 (glowing resin, opaque epoxy variants) and v1.2 (procedural texture generation). The `generate_textures.py` script was built using only Python stdlib to create animated 16x512 fluid textures with sinusoidal wave layers, specular blobs, and drip effects.

### March 30, 2026 — Experimental Fluids
Additional reactive fluid systems were added:
- **Elephant's Toothpaste** — hydrogen peroxide fluid + catalyst block triggers expanding foam eruption (capped at 20 blocks to prevent runaway growth)
- **Acid** — dissolves stone blocks downward, exposing ores beneath
- **Bioluminescent Plankton** — glows reactively, emits bright blue particle eruptions when lit on fire
- **Crystal Solution** — grows crystal block structures

### March 30, 2026 — v1.3 Throwable Hardener
The fluid interaction system was replaced entirely with a throwable hardener item system. `HardenerEntity` extends `ThrowableItemProjectile` and performs BFS flood-fill on impact — up to 1024 connected same-color resin blocks are converted to epoxy instantly with cloud/poof particle effects. This was more satisfying gameplay than passive fluid touching.

All hardener fluids and `FluidInteractionRegistry` calls were removed. Hardener became a throwable item (stacks to 16) rather than a placeable fluid.

### March 31 — April 3, 2026 — Testing Branch Experiments
A `testing-new-fluids` branch explored new content:
- **Custom Mushroom Blocks** — 6 fantasy mushroom variants with unique models
- **Moonlight Jellyfish** — bioluminescent swimming mob, eventually migrated to GeckoLib for proper animated tentacles. This required extensive iteration on orientation, hitbox alignment, and squid-style swimming AI. The jellyfish model was built in Blockbench by Manus and converted to GeckoLib format.
- **Lava Lamp Blocks** — decorative animated blocks

### April 1-2, 2026 — D&D Spell Scrolls
A major feature addition on master: consumable spell scroll items inspired by D&D 5e. Each scroll is a one-time-use item that creates dramatic world effects:
- **Raise Dead** / **Animate Dead** — reanimate skeletons/zombies with custom textures and crystal overlays
- **Phantom Steed** — summon a rideable translucent horse entity with custom model
- **Disintegrate** — beam entity that vaporizes blocks in a line
- **Meteor Swarm** — spawns meteor entities that rain from the sky with explosion effects
- **Dancing Lights** — floating light entities that follow the player
- **Fog Cloud** — area-of-effect fog entity
- **Control Water**, **Plant Growth**, **Mold Earth**, **Move Earth**, **Erupting Earth** — terrain manipulation scrolls
- **Bones of the Earth**, **Passwall**, **Dimension Door** — structural manipulation
- **Galder's Tower**, **Magnificent Mansion**, **Gong's Grotto** — structure-spawning scrolls with persistent data

Custom particle systems were created for necromancy effects, including an 11-frame animated skull particle that forms from wisps, opens its jaw, and dissipates.

### April 4, 2026 — Viva Pinata Mod
The most ambitious effort: a full recreation of Viva Pinata: Trouble in Paradise. Built on a new branch (`claude/viva-pinata-minecraft-mod-WshX2`) branching from master.

Development was rapid — the entire framework was built in a single day:
1. Base entity framework with `BasePinataEntity` (taming levels, candy drops, sour variants)
2. First 6 species with food chain AI (Whirlm, Sparrowmint, Fudgehog, Mousemallow, Taffly, Syrupent)
3. Garden system with `GardenManager`, plot blocks, and attraction spawning
4. Romance mechanic with species-specific requirements
5. Sour pinata variants with hostile AI and taming quests
6. Species conflicts and predator/prey relationships
7. Garden tools (shovel, watering can, surface packets)
8. Economy system with chocolate coins, accessories, produce buildings
9. NPC characters (Seedos seed vendor, Storkos delivery, Doc Patchingo healer)
10. Threats (Ruffian enemies, Dastardos sick-pinata hunter)
11. Evolution/transformation system (11 species evolutions)
12. Professor Pester boss fight
13. Expansion to 64 total species across 9 implementation batches

15 base models cover all 64 species through model sharing (e.g., HorstachioModel serves Horstachio, Camello, Chewnicorn, Zumbug, etc.).

Significant debugging was required:
- 25+ NeoForge 1.21.1 API compatibility fixes (final methods, changed signatures)
- Entity rendering ClassCastException from shared model generics — all 15 models changed to use `BasePinataEntity` type parameter
- Missing resource files — `generate_pinata_resources.py` was created to generate 200+ JSON/PNG files
- Texture UV alignment — texture dimensions had to match each model's `LayerDefinition.create(mesh, W, H)` call

### April 4, 2026 — Texture Research & Reference Gathering
Research effort to find existing Viva Pinata 3D models for artistic reference:
- **BedrockParadise** GitHub repo provided `.geo.json` Bedrock entity models for 8 species with painted textures
- **Manus AI** was used to browse Sketchfab, GitHub, and modding communities to collect available models
- `vp_reference_models.zip` was assembled with reference textures for Whirlm, Sparrowmint, Mousemallow, Bunnycomb, Bispotti, Custacean, Parmadillo, Pengum
- A comprehensive Manus prompt file was prepared for generating all 64 species textures
- VP:TiP ISO and reTiP recompile were examined but use proprietary Rare engine formats (`.pkg` packages, hashed data blobs) that prevent easy model extraction

## Development Approach

This project is developed collaboratively between a human developer and Claude Code (AI assistant). The workflow:
1. High-level feature requests and creative direction come from the developer
2. Claude Code implements the Java mod code, generates textures, debugs build issues
3. Manus AI is used for tasks requiring web browsing (research, asset collection)
4. Testing happens in CurseForge-managed Minecraft instances
5. `CLAUDE.md` maintains context across sessions for the AI assistant

Python scripts handle procedural asset generation — fluid textures, particle animations, entity textures, and resource file scaffolding are all generated programmatically rather than hand-drawn.
