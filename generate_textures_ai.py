"""
Reactive Fluids — AI texture generator.
Generates 16x16 Minecraft textures via AI image generation APIs.

This is an alternative to generate_textures.py (which uses procedural Python).
The AI pipeline generates a high-res image then pixelates it to 16x16 using
nearest-neighbor downscaling.

Requirements:
    pip install openai Pillow

Providers:
    openai    — Uses gpt-image-1 via the OpenAI Python SDK.
                Set the OPENAI_API_KEY environment variable before running.

    provider2 — Stub. The intended model is unclear (see TODO below).
                Set the PROVIDER2_API_KEY environment variable when configured.

Usage examples:
    python generate_textures_ai.py --provider openai --all
    python generate_textures_ai.py --provider openai --texture amber_resin_still
    python generate_textures_ai.py --provider provider2 --all
"""

import argparse
import os
import sys
from io import BytesIO
from pathlib import Path

# ---------------------------------------------------------------------------
# Output path roots (mirrors generate_textures.py)
# ---------------------------------------------------------------------------
BLK = Path("src/main/resources/assets/reactivefluids/textures/block")
ITM = Path("src/main/resources/assets/reactivefluids/textures/item")

# ---------------------------------------------------------------------------
# Prompt templates
# ---------------------------------------------------------------------------
# Each entry maps a logical texture name to a prompt string.
# The pixelation step handles converting the AI output to 16x16 — so prompts
# ask for "16x16 pixel art" style but the actual generation is at 1024x1024.

PROMPTS = {
    # --- Fluid stills ---
    "amber_resin_still": (
        "Minecraft pixel art 16x16 texture, viscous amber orange resin fluid, "
        "slow-moving honey-like liquid surface, warm golden-brown tones, glossy "
        "surface with subtle wave pattern, seamless tile, no background"
    ),
    "amber_hardener_still": (
        "Minecraft pixel art 16x16 texture, amber orange fluid hardener, thinner "
        "and clearer than resin, golden-amber tones with a watery clarity, liquid "
        "surface with fine ripple, seamless tile, no background"
    ),
    "cobalt_resin_still": (
        "Minecraft pixel art 16x16 texture, deep cobalt blue resin fluid, viscous "
        "slow-moving liquid surface, rich blue tones with slight purple depth, "
        "seamless tile, no background"
    ),
    "cobalt_hardener_still": (
        "Minecraft pixel art 16x16 texture, cobalt blue fluid hardener, thinner and "
        "clearer than resin, bright sapphire-blue with watery clarity, fine ripple "
        "pattern, seamless tile, no background"
    ),
    "jade_resin_still": (
        "Minecraft pixel art 16x16 texture, jade green resin fluid, viscous "
        "slow-moving liquid, deep forest green with translucent quality, seamless "
        "tile, no background"
    ),
    "jade_hardener_still": (
        "Minecraft pixel art 16x16 texture, jade green fluid hardener, thinner and "
        "clearer than resin, bright emerald-green with watery clarity, fine ripple "
        "pattern, seamless tile, no background"
    ),
    "amber_glowing_resin_still": (
        "Minecraft pixel art 16x16 texture, bioluminescent amber resin fluid, "
        "viscous glowing liquid surface, warm golden glow with light emission, "
        "seamless tile, no background"
    ),
    "cobalt_glowing_resin_still": (
        "Minecraft pixel art 16x16 texture, bioluminescent cobalt blue resin fluid, "
        "viscous glowing liquid surface, electric blue glow with light emission, "
        "seamless tile, no background"
    ),
    "jade_glowing_resin_still": (
        "Minecraft pixel art 16x16 texture, bioluminescent jade green resin fluid, "
        "viscous glowing liquid surface, vivid green glow with light emission, "
        "seamless tile, no background"
    ),

    # --- Fluid flows ---
    "amber_resin_flow": (
        "Minecraft pixel art 16x16 texture, viscous amber orange resin flowing "
        "vertically, thick honey-like drip pattern, warm golden-brown tones, "
        "seamless vertical tile, no background"
    ),
    "amber_hardener_flow": (
        "Minecraft pixel art 16x16 texture, amber orange fluid hardener flowing "
        "vertically, thin clear liquid streaks, golden-amber tones, seamless "
        "vertical tile, no background"
    ),
    "cobalt_resin_flow": (
        "Minecraft pixel art 16x16 texture, deep cobalt blue resin flowing "
        "vertically, thick viscous drip pattern, rich blue-purple tones, seamless "
        "vertical tile, no background"
    ),
    "cobalt_hardener_flow": (
        "Minecraft pixel art 16x16 texture, cobalt blue fluid hardener flowing "
        "vertically, thin clear liquid streaks, bright sapphire tones, seamless "
        "vertical tile, no background"
    ),
    "jade_resin_flow": (
        "Minecraft pixel art 16x16 texture, jade green resin flowing vertically, "
        "thick viscous drip pattern, deep forest green, seamless vertical tile, "
        "no background"
    ),
    "jade_hardener_flow": (
        "Minecraft pixel art 16x16 texture, jade green fluid hardener flowing "
        "vertically, thin clear liquid streaks, bright emerald tones, seamless "
        "vertical tile, no background"
    ),
    "amber_glowing_resin_flow": (
        "Minecraft pixel art 16x16 texture, bioluminescent amber resin flowing "
        "vertically, thick glowing drip pattern, warm golden glow, light emission, "
        "seamless vertical tile, no background"
    ),
    "cobalt_glowing_resin_flow": (
        "Minecraft pixel art 16x16 texture, bioluminescent cobalt blue resin flowing "
        "vertically, thick glowing drip pattern, electric blue glow, light emission, "
        "seamless vertical tile, no background"
    ),
    "jade_glowing_resin_flow": (
        "Minecraft pixel art 16x16 texture, bioluminescent jade green resin flowing "
        "vertically, thick glowing drip pattern, vivid green glow, light emission, "
        "seamless vertical tile, no background"
    ),

    # --- Epoxy blocks: transparent ---
    "amber_epoxy_block": (
        "Minecraft pixel art 16x16 texture, hardened transparent amber epoxy resin "
        "block, amber gemstone look, faceted glass-like surface, translucent warm "
        "gold, seamless tile, no background"
    ),
    "cobalt_epoxy_block": (
        "Minecraft pixel art 16x16 texture, hardened transparent cobalt blue epoxy "
        "resin block, sapphire gemstone look, faceted glass-like surface, translucent "
        "deep blue, seamless tile, no background"
    ),
    "jade_epoxy_block": (
        "Minecraft pixel art 16x16 texture, hardened transparent jade green epoxy "
        "resin block, jade gemstone look, faceted glass-like surface, translucent "
        "forest green, seamless tile, no background"
    ),

    # --- Epoxy blocks: glowing ---
    "amber_epoxy_glowing": (
        "Minecraft pixel art 16x16 texture, hardened amber epoxy resin block, "
        "bioluminescent glow, light emission, warm golden inner radiance, faceted "
        "glass-like surface, translucent amber, seamless tile, no background"
    ),
    "cobalt_epoxy_glowing": (
        "Minecraft pixel art 16x16 texture, hardened cobalt blue epoxy resin block, "
        "bioluminescent glow, light emission, electric blue inner radiance, faceted "
        "glass-like surface, translucent cobalt, seamless tile, no background"
    ),
    "jade_epoxy_glowing": (
        "Minecraft pixel art 16x16 texture, hardened jade green epoxy resin block, "
        "bioluminescent glow, light emission, vivid green inner radiance, faceted "
        "glass-like surface, translucent jade, seamless tile, no background"
    ),

    # --- Epoxy blocks: opaque ---
    "amber_epoxy_opaque": (
        "Minecraft pixel art 16x16 texture, opaque solid amber epoxy block, matte "
        "finish, stone-like hardness, warm amber-brown tones, seamless tile, "
        "no background"
    ),
    "cobalt_epoxy_opaque": (
        "Minecraft pixel art 16x16 texture, opaque solid cobalt blue epoxy block, "
        "matte finish, stone-like hardness, deep blue tones, seamless tile, "
        "no background"
    ),
    "jade_epoxy_opaque": (
        "Minecraft pixel art 16x16 texture, opaque solid jade green epoxy block, "
        "matte finish, stone-like hardness, deep forest green tones, seamless tile, "
        "no background"
    ),
}

# Map each texture name to its output path
OUTPUT_PATHS = {
    # Fluid stills
    "amber_resin_still":         BLK / "amber_resin_still.png",
    "amber_hardener_still":      BLK / "amber_hardener_still.png",
    "cobalt_resin_still":        BLK / "cobalt_resin_still.png",
    "cobalt_hardener_still":     BLK / "cobalt_hardener_still.png",
    "jade_resin_still":          BLK / "jade_resin_still.png",
    "jade_hardener_still":       BLK / "jade_hardener_still.png",
    "amber_glowing_resin_still": BLK / "amber_glowing_resin_still.png",
    "cobalt_glowing_resin_still":BLK / "cobalt_glowing_resin_still.png",
    "jade_glowing_resin_still":  BLK / "jade_glowing_resin_still.png",
    # Fluid flows
    "amber_resin_flow":          BLK / "amber_resin_flow.png",
    "amber_hardener_flow":       BLK / "amber_hardener_flow.png",
    "cobalt_resin_flow":         BLK / "cobalt_resin_flow.png",
    "cobalt_hardener_flow":      BLK / "cobalt_hardener_flow.png",
    "jade_resin_flow":           BLK / "jade_resin_flow.png",
    "jade_hardener_flow":        BLK / "jade_hardener_flow.png",
    "amber_glowing_resin_flow":  BLK / "amber_glowing_resin_flow.png",
    "cobalt_glowing_resin_flow": BLK / "cobalt_glowing_resin_flow.png",
    "jade_glowing_resin_flow":   BLK / "jade_glowing_resin_flow.png",
    # Epoxy blocks
    "amber_epoxy_block":         BLK / "amber_epoxy_block.png",
    "cobalt_epoxy_block":        BLK / "cobalt_epoxy_block.png",
    "jade_epoxy_block":          BLK / "jade_epoxy_block.png",
    "amber_epoxy_glowing":       BLK / "amber_epoxy_glowing.png",
    "cobalt_epoxy_glowing":      BLK / "cobalt_epoxy_glowing.png",
    "jade_epoxy_glowing":        BLK / "jade_epoxy_glowing.png",
    "amber_epoxy_opaque":        BLK / "amber_epoxy_opaque.png",
    "cobalt_epoxy_opaque":       BLK / "cobalt_epoxy_opaque.png",
    "jade_epoxy_opaque":         BLK / "jade_epoxy_opaque.png",
}

# ---------------------------------------------------------------------------
# Pixelation helper
# ---------------------------------------------------------------------------

def pixelate(img_bytes, target_size=16):
    """
    Downscale raw PNG/image bytes to target_size x target_size using
    nearest-neighbor interpolation, preserving the pixel-art aesthetic.

    Args:
        img_bytes: Raw bytes of the source image (e.g. 1024x1024 PNG from API).
        target_size: Output dimension in pixels (default 16).

    Returns:
        A PIL Image object at target_size x target_size in RGBA mode.
    """
    try:
        from PIL import Image
    except ImportError:
        print("ERROR: Pillow is not installed. Install Pillow: pip install Pillow")
        sys.exit(1)

    img = Image.open(BytesIO(img_bytes)).convert("RGBA")
    img = img.resize((target_size, target_size), Image.NEAREST)
    return img


def save_image(pil_image, output_path):
    """Save a PIL Image to disk, creating parent directories as needed."""
    output_path = Path(output_path)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    pil_image.save(output_path, format="PNG")
    print(f"  Saved: {output_path}")


# ---------------------------------------------------------------------------
# Provider 1: OpenAI gpt-image-1
# ---------------------------------------------------------------------------

def generate_with_openai(prompt, output_path):
    """
    Generate a texture using OpenAI's gpt-image-1 model.

    Workflow:
      1. Call the Images API at 1024x1024.
      2. Retrieve the raw PNG bytes from the response.
      3. Pixelate to 16x16 via nearest-neighbor downscaling.
      4. Save the result to output_path.

    Requires:
      - OPENAI_API_KEY environment variable set.
      - openai package installed (pip install openai).
    """
    api_key = os.environ.get("OPENAI_API_KEY")
    if not api_key:
        print(
            "ERROR: Set OPENAI_API_KEY environment variable to use OpenAI provider.\n"
            "       export OPENAI_API_KEY=sk-..."
        )
        sys.exit(1)

    try:
        from openai import OpenAI
    except ImportError:
        print("ERROR: openai package is not installed. Install it: pip install openai")
        sys.exit(1)

    client = OpenAI(api_key=api_key)

    print(f"  Generating via OpenAI gpt-image-1: {Path(output_path).name}")
    print(f"  Prompt: {prompt[:80]}{'...' if len(prompt) > 80 else ''}")

    try:
        response = client.images.generate(
            model="gpt-image-1",
            prompt=prompt,
            size="1024x1024",
            output_format="png",
            n=1,
        )
    except Exception as exc:
        print(f"  ERROR: OpenAI API call failed — {exc}")
        return

    # gpt-image-1 returns base64-encoded image data in response.data[0].b64_json
    import base64
    image_data = response.data[0]

    if hasattr(image_data, "b64_json") and image_data.b64_json:
        img_bytes = base64.b64decode(image_data.b64_json)
    elif hasattr(image_data, "url") and image_data.url:
        # Fallback: download from URL if b64_json is not present
        import urllib.request
        with urllib.request.urlopen(image_data.url) as resp:
            img_bytes = resp.read()
    else:
        print("  ERROR: Could not retrieve image data from OpenAI response.")
        return

    pil_image = pixelate(img_bytes, target_size=16)
    save_image(pil_image, output_path)


# ---------------------------------------------------------------------------
# Provider 2: Unknown ("nano banana 2")
# ---------------------------------------------------------------------------

def generate_with_provider2(prompt, output_path):
    # TODO: Clarify which model "nano banana 2" refers to (Ideogram 2? Imagen 2? Flux?)
    #
    # Candidates:
    #   - Ideogram 2   (ideogram.ai) — strong text rendering, good pixel art style
    #   - Imagen 2     (Google DeepMind / Vertex AI) — high fidelity, requires GCP
    #   - Flux          (Black Forest Labs) — open weights, fast inference
    #
    # Once identified, implement the API call here following the same pattern
    # as generate_with_openai(): call API, get bytes, call pixelate(), call save_image().
    raise NotImplementedError(
        "Provider 2 not yet configured — see TODO comment in generate_with_provider2()"
    )


# ---------------------------------------------------------------------------
# Dispatch
# ---------------------------------------------------------------------------

PROVIDER_FUNCTIONS = {
    "openai":    generate_with_openai,
    "provider2": generate_with_provider2,
}


def generate_texture(name, provider):
    """Generate a single named texture using the given provider."""
    if name not in PROMPTS:
        print(f"ERROR: Unknown texture name '{name}'.")
        print(f"Valid names: {', '.join(sorted(PROMPTS.keys()))}")
        sys.exit(1)

    prompt = PROMPTS[name]
    output_path = OUTPUT_PATHS[name]
    generate_fn = PROVIDER_FUNCTIONS[provider]

    try:
        generate_fn(prompt, output_path)
    except NotImplementedError as exc:
        print(f"ERROR: {exc}")
        sys.exit(1)


def generate_all(provider):
    """Generate every texture in PROMPTS using the given provider."""
    total = len(PROMPTS)
    errors = []

    for i, name in enumerate(PROMPTS, start=1):
        print(f"\n[{i}/{total}] {name}")
        prompt = PROMPTS[name]
        output_path = OUTPUT_PATHS[name]
        generate_fn = PROVIDER_FUNCTIONS[provider]
        try:
            generate_fn(prompt, output_path)
        except NotImplementedError as exc:
            print(f"  SKIP: {exc}")
            errors.append(name)
        except Exception as exc:
            print(f"  ERROR: {exc}")
            errors.append(name)

    print(f"\nDone. {total - len(errors)}/{total} textures generated.")
    if errors:
        print(f"Failed/skipped: {', '.join(errors)}")


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(
        description="Generate Reactive Fluids textures via AI image generation APIs."
    )
    parser.add_argument(
        "--provider",
        choices=["openai", "provider2"],
        default="openai",
        help="Which AI provider to use (default: openai).",
    )
    parser.add_argument(
        "--texture",
        metavar="NAME",
        help=(
            "Generate a single named texture. "
            f"Valid names: {', '.join(sorted(PROMPTS.keys()))}"
        ),
    )
    parser.add_argument(
        "--all",
        action="store_true",
        help="Generate all textures.",
    )
    parser.add_argument(
        "--list",
        action="store_true",
        help="List all available texture names and exit.",
    )
    args = parser.parse_args()

    if args.list:
        print("Available texture names:")
        for name in sorted(PROMPTS.keys()):
            print(f"  {name}")
        return

    if not args.texture and not args.all:
        parser.print_help()
        print(
            "\nERROR: Specify --texture NAME or --all to generate textures."
        )
        sys.exit(1)

    if args.texture and args.all:
        print("ERROR: Use either --texture NAME or --all, not both.")
        sys.exit(1)

    if args.all:
        generate_all(args.provider)
    else:
        generate_texture(args.texture, args.provider)


if __name__ == "__main__":
    main()
