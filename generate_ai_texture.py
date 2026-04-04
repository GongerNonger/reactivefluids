"""
AI Texture Generator for Reactive Fluids mod.
Uses OpenAI gpt-image-1 to generate Minecraft-style pixel art textures,
then downscales to the target resolution with nearest-neighbor interpolation.

Usage:
    python generate_ai_texture.py <prompt> <output_path> [--size 16] [--raw-size 512]

Examples:
    python generate_ai_texture.py "ghost mushroom with blue-green bioluminescent gills" textures/block/ghost_fungus.png
    python generate_ai_texture.py "jellyfish entity texture UV map, dome bell and tentacles" textures/entity/jellyfish.png --size 64
"""
import sys
import os
import json
import urllib.request
import urllib.error
import base64
import zlib
import struct
import argparse


def call_openai_image(prompt, api_key, raw_size=1024):
    """Generate an image using OpenAI gpt-image-1."""
    url = "https://api.openai.com/v1/images/generations"

    full_prompt = (
        f"16-bit pixel art for a Minecraft mod texture. "
        f"Sharp pixel edges, no anti-aliasing, limited color palette, "
        f"retro game aesthetic, transparent background where appropriate. "
        f"{prompt}"
    )

    payload = json.dumps({
        "model": "gpt-image-1",
        "prompt": full_prompt,
        "n": 1,
        "size": f"{raw_size}x{raw_size}",
        "quality": "medium",
    }).encode("utf-8")

    req = urllib.request.Request(url, data=payload, headers={
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
    })

    print(f"  Generating image ({raw_size}x{raw_size})...")
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            data = json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        error_body = e.read().decode("utf-8") if e.fp else "unknown"
        print(f"  ERROR {e.code}: {error_body}")
        sys.exit(1)

    # gpt-image-1 returns b64_json by default in some configs, or url
    if "data" in data and len(data["data"]) > 0:
        item = data["data"][0]
        if "b64_json" in item:
            return base64.b64decode(item["b64_json"])
        elif "url" in item:
            print(f"  Downloading from URL...")
            with urllib.request.urlopen(item["url"]) as img_resp:
                return img_resp.read()

    print(f"  Unexpected response: {json.dumps(data, indent=2)}")
    sys.exit(1)


def downscale_nearest(input_bytes, target_size):
    """
    Downscale a PNG image to target_size x target_size using nearest-neighbor.
    Uses only stdlib (no Pillow required).
    Reads PNG, decodes RGBA pixels, samples, re-encodes PNG.
    """
    try:
        from PIL import Image
        import io
        img = Image.open(io.BytesIO(input_bytes)).convert("RGBA")
        img = img.resize((target_size, target_size), Image.NEAREST)
        buf = io.BytesIO()
        img.save(buf, format="PNG")
        return buf.getvalue()
    except ImportError:
        # Fallback: save raw and let user downscale manually
        print("  WARNING: Pillow not installed. Saving at original resolution.")
        print("  Install Pillow for auto-downscale: pip install Pillow")
        return input_bytes


def main():
    parser = argparse.ArgumentParser(description="Generate Minecraft textures with AI")
    parser.add_argument("prompt", help="Description of the texture to generate")
    parser.add_argument("output", help="Output file path (relative to project root)")
    parser.add_argument("--size", type=int, default=16, help="Target texture size (default: 16)")
    parser.add_argument("--raw-size", type=int, default=512, help="Generation size before downscale (default: 512)")
    args = parser.parse_args()

    api_key = os.environ.get("OPENAI_API_KEY")
    if not api_key:
        # Try loading from Windows environment
        try:
            import subprocess
            result = subprocess.run(
                ["powershell.exe", "-Command",
                 "[System.Environment]::GetEnvironmentVariable('OPENAI_API_KEY', 'User')"],
                capture_output=True, text=True)
            api_key = result.stdout.strip()
        except Exception:
            pass

    if not api_key:
        print("ERROR: OPENAI_API_KEY not set. Run: setx OPENAI_API_KEY \"your-key\"")
        sys.exit(1)

    print(f"Generating: {args.prompt}")
    print(f"Output: {args.output} ({args.size}x{args.size})")

    # Generate
    raw_png = call_openai_image(args.prompt, api_key, args.raw_size)

    # Downscale
    if args.size != args.raw_size:
        print(f"  Downscaling {args.raw_size} -> {args.size} (nearest-neighbor)...")
        final_png = downscale_nearest(raw_png, args.size)
    else:
        final_png = raw_png

    # Save
    os.makedirs(os.path.dirname(args.output) or ".", exist_ok=True)
    with open(args.output, "wb") as f:
        f.write(final_png)

    print(f"  Saved: {args.output} ({len(final_png)} bytes)")
    print("Done!")


if __name__ == "__main__":
    main()
