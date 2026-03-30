# AI Texture Generator — Setup Guide

Alternative to `generate_textures.py`. Generates Minecraft 16x16 textures using AI image APIs.

---

## 1. Install dependencies

```
pip install openai Pillow
```

---

## 2. Set the API key

**Windows (Command Prompt):**
```
set OPENAI_API_KEY=sk-your-key-here
```

**Windows (PowerShell):**
```
$env:OPENAI_API_KEY = "sk-your-key-here"
```

**macOS / Linux:**
```
export OPENAI_API_KEY=sk-your-key-here
```

Get your key at: https://platform.openai.com/api-keys

---

## 3. Run the script

**Generate all textures:**
```
python generate_textures_ai.py --all
```

**Generate one texture by name:**
```
python generate_textures_ai.py --texture amber_resin_still
```

**List all available texture names:**
```
python generate_textures_ai.py --list
```

---

## 4. What is "nano banana 2"?

The script includes a stub for a second AI provider (`--provider provider2`).
The name "nano banana 2" is likely a speech-to-text garble of an actual model name.

**Candidates — please confirm which one you meant:**

| Model | Provider | Notes |
|-------|----------|-------|
| **Ideogram 2** | ideogram.ai | Strong stylistic control, good at pixel art |
| **Imagen 2** | Google DeepMind / Vertex AI | High fidelity, requires a GCP project |
| **Flux** | Black Forest Labs | Open weights, very fast, runs locally or via API |

Once you confirm the model, the stub function `generate_with_provider2()` in
`generate_textures_ai.py` can be implemented. Look for the `# TODO` comment near
that function.

---

## 5. Expected output

All textures are written to the same paths used by `generate_textures.py`:

```
src/main/resources/assets/reactivefluids/textures/block/
    amber_resin_still.png
    amber_resin_flow.png
    cobalt_resin_still.png
    cobalt_resin_flow.png
    jade_resin_still.png
    jade_resin_flow.png
    amber_hardener_still.png
    amber_hardener_flow.png
    cobalt_hardener_still.png
    cobalt_hardener_flow.png
    jade_hardener_still.png
    jade_hardener_flow.png
    amber_glowing_resin_still.png
    amber_glowing_resin_flow.png
    cobalt_glowing_resin_still.png
    cobalt_glowing_resin_flow.png
    jade_glowing_resin_still.png
    jade_glowing_resin_flow.png
    amber_epoxy_block.png
    amber_epoxy_glowing.png
    amber_epoxy_opaque.png
    cobalt_epoxy_block.png
    cobalt_epoxy_glowing.png
    cobalt_epoxy_opaque.png
    jade_epoxy_block.png
    jade_epoxy_glowing.png
    jade_epoxy_opaque.png
```

Each image is 16x16 RGBA PNG, generated from a 1024x1024 AI output that is
downscaled using nearest-neighbor pixelation.

Note: `generate_textures.py` also produces `.mcmeta` animation files and bucket
item sprites. This script does not — it only covers the base block/fluid textures.
If you need animated textures you will need to write the `.mcmeta` files manually
or extend this script.
```
