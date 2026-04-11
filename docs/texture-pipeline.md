# Texture Pipeline

## Overview

All textures in this mod are procedurally generated using Python scripts. No hand-drawn textures exist — everything from fluid animations to entity skins to bucket sprites is created programmatically.

## Scripts

### generate_textures.py (master branch)

The original texture generator. Handles all reactive fluid and spell scroll textures. Uses only Python stdlib (no pip dependencies).

**Outputs:**
- Fluid still textures (16x512 — 32 frames of 16x16 stacked vertically)
- Fluid flow textures (16x512 — 32 frames of 16x16)
- Epoxy block textures (16x16 static)
- Bucket item sprites (16x16 static)
- Necrotic particle animation (16x176 — 11 frames of 16x16)

**Fluid Still Texture Algorithm:**
1. 5 pure-X + 5 pure-Y sinusoidal wave layers (no diagonal `sin(x+y)` to prevent stripe artifacts)
2. Dual drifting specular blob highlights
3. Horizontal sheen gradient
4. Per-fluid alpha values: resin 80, hardener 60, glowing resin 100

**Fluid Flow Texture Algorithm:**
Pure vertical ribbon system — each column has a static brightness profile with 3 drip waves at incommensurable periods. Movement is downward-only for a falling-liquid appearance.

**Necrotic Particle:**
11-frame animation defined as character-map grids (each frame is a 16x16 grid of characters mapping to a teal-green color palette). Animation sequence: wisp forming, skull with eye sockets, mouth opens, jaw separates, breaks apart, dissipates. Uses `setSpriteFromAge(sprites)` for frame cycling.

**Animation metadata:**
- Resin: `interpolate: false` — sharp, thick drip transitions
- Hardener: `interpolate: true` — smooth, watery blending

### generate_pinata_resources.py (pinata branch)

Generates all missing resource files for the Viva Pinata content.

**Outputs:**
- Blockstate JSON files for all pinata blocks
- Block model JSON files
- Item model JSON files
- Placeholder entity textures at correct dimensions per model

Contains two critical lookup tables:
- `SPECIES_MODEL` — maps all 64 species to their model class
- `MODEL_TEX_SIZES` — maps model classes to required texture dimensions (e.g., HorstachioModel = 64x32)

### generate_pinata_models.py (pinata branch)

Paints proper UV-mapped entity textures. Currently handles 3 species:
- **Whirlm** (64x32) — pink/magenta with lighter belly segments, eyes on head front face
- **Sparrowmint** (32x32) — mint green, white belly, orange beak/legs
- **Fudgehog** (48x32) — chocolate brown body, caramel spines, pink nose

Each texture is painted by calculating the exact pixel position of each cube face using the model's `texOffs(U, V)` coordinates and box dimensions, then filling those regions with appropriate colors.

Also generates sour variants (desaturated with dark purple tint).

### generate_textures_ai.py

AI-powered texture generator using the OpenAI API. Requires `OPENAI_API_KEY` environment variable. See `AI_TEXTURE_SETUP.md` for configuration. Used experimentally — the procedural approach in `generate_textures.py` is the primary pipeline.

## UV Mapping Reference

Minecraft Java Edition entity textures use a box-unwrap UV system. For a cube of size `W x H x D` at `texOffs(U, V)`:

```
Row 1 (y = V, height = D):
  [gap: D wide] [TOP: W wide] [gap: D wide] [BOTTOM: W wide]

Row 2 (y = V + D, height = H):
  [LEFT: D wide] [FRONT: W wide] [RIGHT: D wide] [BACK: W wide]
```

Texture dimensions must exactly match the model's `LayerDefinition.create(mesh, W, H)` — wrong sizes cause stretching or misalignment.

## External Texture References

For the Viva Pinata effort, reference textures were collected from:
- **BedrockParadise** GitHub repo — 8 species with Bedrock `.geo.json` models and painted textures
- These serve as the style guide: flat color regions, minimal noise, paper-craft aesthetic
- Reference textures are stored in `Downloads/vp_reference_models/` (not in the repo)

## Running the Pipeline

```bash
# From the project root
python generate_textures.py           # Regenerate all fluid/particle textures
python generate_pinata_resources.py   # Regenerate pinata resource scaffolding
python generate_pinata_models.py      # Regenerate UV-mapped entity textures
```

Always regenerate textures after changing any color values, model dimensions, or adding new content.
