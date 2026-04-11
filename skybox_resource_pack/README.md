# Moonlight Night Sky — Celestial resource pack

A custom constellation skybox for the overworld night sky. Intended to ship alongside the Reactive Fluids mod as an optional companion resource pack.

## What it does

Replaces the vanilla procedural night sky with a hand-painted 6-face cubemap featuring real constellations (Ursa Major, Ursa Minor, Draco, Orion), the Milky Way, and named stars. Sun, moon, and vanilla-style procedural filler stars all render on top via Celestial sky objects.

## Requirements

- Minecraft 1.21.1 NeoForge 21.1.x (ships with Reactive Fluids)
- [Celestial Unofficial 2.0 Fix](https://modrinth.com/mod/celestial-unofficial-2.0-fix) — the community-patched build of Celestial 2.0 for NeoForge 1.21

## Installation

1. Install Reactive Fluids and GeckoLib in the normal way.
2. Add `celestial_1.21_neoforge-2.0.jar` (the Unofficial 2.0 Fix build) to your instance's `mods/` folder.
3. Copy `MoonlightNightSky_Celestial.zip` into the instance's `resourcepacks/` folder.
4. Launch the game, open Options → Resource Packs, and enable "Moonlight Night Sky".

## Known limitations (Celestial 2.0, NeoForge 1.21.1)

- **The cubemap is static** — Celestial's `skybox` type does not rotate with the day/night cycle. The wiki documents this as intentional. The sky *feels* alive because the 400 vanilla-style procedural stars from `stars.json` do rotate over the top of the cubemap, but the constellations themselves are fixed.
- **Milky Way is painted per-face** — the three Milky Way faces (west, south-adjacent-east, east) don't perfectly line up at the cube edges, so you may see the band as three discrete diagonals rather than one continuous arc. Fixing this requires repainting the cubemap with edge-continuity awareness.
- **Seams between faces** — mitigated with 1-pixel edge padding on each cell, but some residual visibility may remain depending on resource-pack sampling.

All of these limitations are solved by migrating to Nuit on NeoForge 1.21.4+, which supports proper rotating cubemap skyboxes (`rotationSpeedY: 1` gives exactly one full wheel per Minecraft day). That migration is planned as a separate project. Until then, this pack is the ship-ready placeholder.

## Files

| File | Purpose |
|---|---|
| `MoonlightNightSky_Celestial.zip` | The finished resource pack — drop into instance `resourcepacks/` |
| `source/` | Unzipped contents of the pack, for inspection and manual edits |
| `source/pack.mcmeta` | pack_format + description |
| `source/assets/celestial/sky/dimensions.json` | Which dimensions the sky definitions apply to (overworld only) |
| `source/assets/celestial/sky/overworld/sky.json` | Top-level sky config, object order, environment settings |
| `source/assets/celestial/sky/overworld/objects/moonlight_skybox.json` | The custom constellation cubemap |
| `source/assets/celestial/sky/overworld/objects/stars.json` | 400 procedural vanilla-style stars that rotate over the cubemap |
| `source/assets/celestial/sky/overworld/objects/sun.json` | Vanilla sun, rotates on its day arc |
| `source/assets/celestial/sky/overworld/objects/moon.json` | Vanilla moon with phases |
| `source/assets/celestial/sky/overworld/objects/twilight.json` | Inert placeholder (prevents undeclared-variable parse errors) |
| `source/assets/reactivefluids/textures/sky/night_skybox.png` | 384×256 cubemap texture, 3×2 face grid, 128×128 per face |
| `source/assets/reactivefluids/textures/sky/night_skybox.png.mcmeta` | blur off, clamp on — partial seam mitigation |

## Celestial cubemap face mapping (empirically derived, v7 layout)

The 3×2 texture atlas maps to cube faces in a non-obvious way. This is the v7 layout derived from the first successful in-game test. A later attempt to re-derive it under rotation produced a broken v8 that put the empty face on the north side, so v7 is the canonical working mapping.

| Grid cell | Face |
|---|---|
| (0,0) top-left | east (rendered 180° rotated) |
| (1,0) top-middle | west |
| (2,0) top-right | down |
| (0,1) bottom-left | south |
| (1,1) bottom-middle | up |
| (2,1) bottom-right | north |

The east face is rendered with an internal 180° rotation by Celestial, so content for that face must be pre-rotated 180° in the atlas to appear right-side-up.
