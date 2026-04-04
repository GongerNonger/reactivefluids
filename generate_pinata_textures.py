"""
Generate placeholder textures for Viva Piñata creatures.
Uses only Python stdlib (PIL not required).

Whirlm reference (VP in-game model):
- Pink/magenta body with paper-fold stripe pattern
- Segmented worm shape with lighter belly
- Small black dot eyes on the head segment
- Paper piñata texture with crepe-paper frills
"""
import struct
import zlib
import os

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
TEXTURE_DIR = os.path.join(SCRIPT_DIR, "src", "main", "resources", "assets",
                           "reactivefluids", "textures", "entity", "pinata")
ITEM_TEXTURE_DIR = os.path.join(SCRIPT_DIR, "src", "main", "resources", "assets",
                                "reactivefluids", "textures", "item")

def write_png(filepath, width, height, pixels):
    """Write a PNG file from RGBA pixel data. pixels = list of (r,g,b,a) tuples."""
    def chunk(chunk_type, data):
        c = chunk_type + data
        crc = struct.pack('>I', zlib.crc32(c) & 0xFFFFFFFF)
        return struct.pack('>I', len(data)) + c + crc

    signature = b'\x89PNG\r\n\x1a\n'
    ihdr = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)  # 8-bit RGBA
    raw = b''
    for y in range(height):
        raw += b'\x00'  # filter byte
        for x in range(width):
            r, g, b, a = pixels[y * width + x]
            raw += struct.pack('BBBB', r, g, b, a)
    compressed = zlib.compress(raw)

    with open(filepath, 'wb') as f:
        f.write(signature)
        f.write(chunk(b'IHDR', ihdr))
        f.write(chunk(b'IDAT', compressed))
        f.write(chunk(b'IEND', b''))

def lerp_color(c1, c2, t):
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(4))

def generate_whirlm_texture():
    """
    64x32 texture map for the Whirlm model.
    Based on VP in-game model: bright pink/magenta body, paper-fold stripes,
    lighter pink belly, small dark eyes.

    Layout (matching WhirlmModel UV):
      (0,0)  - Head: 5x5x4 cube -> UV area 18x9 starting at (0,0)
      (0,10) - Body1: 4x4x4 cube -> UV area 16x8 starting at (0,10)
      (0,19) - Body2: 4x4x4 cube -> UV area 16x8 starting at (0,19)
      (18,0) - Tail: 3x3x4 cube -> UV area 14x7 starting at (18,0)
    """
    W, H = 64, 32
    pixels = [(200, 150, 180, 255)] * (W * H)  # Base: light pinkish paper

    # Color palette — VP Whirlm colors
    PINK_MAIN = (220, 80, 140, 255)      # Main body pink
    PINK_LIGHT = (240, 140, 180, 255)    # Light pink (belly/highlights)
    PINK_DARK = (170, 50, 100, 255)      # Dark pink (shadows/stripes)
    PINK_STRIPE = (250, 160, 200, 255)   # Paper stripe accent
    WHITE = (255, 245, 250, 255)         # Near-white for eye area
    BLACK = (20, 15, 15, 255)            # Eyes
    MAGENTA = (200, 60, 120, 255)        # Deeper accent

    def set_pixel(x, y, color):
        if 0 <= x < W and 0 <= y < H:
            pixels[y * W + x] = color

    def fill_rect(x1, y1, w, h, color):
        for dy in range(h):
            for dx in range(w):
                set_pixel(x1 + dx, y1 + dy, color)

    # === HEAD (0,0) 18x9 area ===
    fill_rect(0, 0, 18, 9, PINK_MAIN)
    # Paper-fold stripes on head (horizontal)
    for x in range(18):
        set_pixel(x, 2, PINK_STRIPE)
        set_pixel(x, 5, PINK_STRIPE)
    # Eyes — two dark dots on the front face area (approx x=5-8, y=1-3)
    set_pixel(6, 3, BLACK)
    set_pixel(7, 3, BLACK)
    set_pixel(10, 3, BLACK)
    set_pixel(11, 3, BLACK)
    # Eye whites
    set_pixel(6, 2, WHITE)
    set_pixel(7, 2, WHITE)
    set_pixel(10, 2, WHITE)
    set_pixel(11, 2, WHITE)
    # Lighter underside
    fill_rect(0, 7, 18, 2, PINK_LIGHT)
    # Shadow at top
    fill_rect(0, 0, 18, 1, PINK_DARK)

    # === BODY1 (0,10) 16x8 area ===
    fill_rect(0, 10, 16, 8, PINK_MAIN)
    # Paper-fold stripes
    for x in range(16):
        set_pixel(x, 12, PINK_STRIPE)
        set_pixel(x, 15, PINK_STRIPE)
    # Lighter belly
    fill_rect(0, 16, 16, 2, PINK_LIGHT)
    # Shadow
    fill_rect(0, 10, 16, 1, PINK_DARK)
    # Crepe paper frills — alternating pixels on edges
    for y in range(10, 18):
        if y % 2 == 0:
            set_pixel(0, y, MAGENTA)
            set_pixel(15, y, MAGENTA)

    # === BODY2 (0,19) 16x8 area ===
    fill_rect(0, 19, 16, 8, PINK_MAIN)
    # Paper-fold stripes
    for x in range(16):
        set_pixel(x, 21, PINK_STRIPE)
        set_pixel(x, 24, PINK_STRIPE)
    # Lighter belly
    fill_rect(0, 25, 16, 2, PINK_LIGHT)
    # Shadow
    fill_rect(0, 19, 16, 1, PINK_DARK)
    # Frills
    for y in range(19, 27):
        if y % 2 == 0:
            set_pixel(0, y, MAGENTA)
            set_pixel(15, y, MAGENTA)

    # === TAIL (18,0) 14x7 area ===
    fill_rect(18, 0, 14, 7, PINK_MAIN)
    # Tapered look — darker at the tip
    fill_rect(28, 0, 4, 7, PINK_DARK)
    # Stripe
    for x in range(18, 32):
        set_pixel(x, 3, PINK_STRIPE)
    # Light belly
    fill_rect(18, 5, 14, 2, PINK_LIGHT)

    write_png(os.path.join(TEXTURE_DIR, "whirlm.png"), W, H, pixels)
    print("  Generated whirlm.png")

def generate_whirlm_sour_texture():
    """Sour Whirlm — dark purple/brown with cracks, hostile-looking."""
    W, H = 64, 32
    pixels = [(80, 50, 70, 255)] * (W * H)

    SOUR_MAIN = (100, 40, 80, 255)
    SOUR_DARK = (60, 20, 50, 255)
    SOUR_LIGHT = (130, 70, 100, 255)
    SOUR_CRACK = (40, 15, 30, 255)
    RED_EYE = (220, 30, 30, 255)
    BLACK = (15, 10, 10, 255)

    def set_pixel(x, y, color):
        if 0 <= x < W and 0 <= y < H:
            pixels[y * W + x] = color

    def fill_rect(x1, y1, w, h, color):
        for dy in range(h):
            for dx in range(w):
                set_pixel(x1 + dx, y1 + dy, color)

    # Head
    fill_rect(0, 0, 18, 9, SOUR_MAIN)
    fill_rect(0, 0, 18, 1, SOUR_DARK)
    fill_rect(0, 7, 18, 2, SOUR_LIGHT)
    # Red angry eyes
    set_pixel(6, 2, RED_EYE)
    set_pixel(7, 2, RED_EYE)
    set_pixel(7, 3, BLACK)
    set_pixel(10, 2, RED_EYE)
    set_pixel(11, 2, RED_EYE)
    set_pixel(10, 3, BLACK)
    # Cracks
    for x in [3, 8, 14]:
        set_pixel(x, 4, SOUR_CRACK)
        set_pixel(x+1, 5, SOUR_CRACK)

    # Body segments
    for base_y in [10, 19]:
        fill_rect(0, base_y, 16, 8, SOUR_MAIN)
        fill_rect(0, base_y, 16, 1, SOUR_DARK)
        fill_rect(0, base_y + 6, 16, 2, SOUR_LIGHT)
        for x in [2, 7, 12]:
            set_pixel(x, base_y + 3, SOUR_CRACK)
            set_pixel(x + 1, base_y + 4, SOUR_CRACK)

    # Tail
    fill_rect(18, 0, 14, 7, SOUR_MAIN)
    fill_rect(28, 0, 4, 7, SOUR_DARK)
    fill_rect(18, 5, 14, 2, SOUR_LIGHT)

    write_png(os.path.join(TEXTURE_DIR, "whirlm_sour.png"), W, H, pixels)
    print("  Generated whirlm_sour.png")

def generate_whirlm_candy_texture():
    """16x16 item texture for Whirlm Candy — pink wrapped candy."""
    W, H = 16, 16
    pixels = [(0, 0, 0, 0)] * (W * H)  # Transparent base

    PINK = (220, 80, 140, 255)
    PINK_L = (245, 150, 190, 255)
    PINK_D = (170, 50, 100, 255)
    WHITE = (255, 240, 245, 255)
    WRAPPER = (250, 200, 220, 255)

    def set_pixel(x, y, color):
        if 0 <= x < W and 0 <= y < H:
            pixels[y * W + x] = color

    def fill_rect(x1, y1, w, h, color):
        for dy in range(h):
            for dx in range(w):
                set_pixel(x1 + dx, y1 + dy, color)

    # Candy body — oval in center
    fill_rect(5, 5, 6, 6, PINK)
    fill_rect(6, 4, 4, 8, PINK)
    fill_rect(4, 6, 8, 4, PINK)
    # Highlight
    set_pixel(6, 5, PINK_L)
    set_pixel(7, 5, PINK_L)
    set_pixel(6, 6, PINK_L)
    # Shadow
    fill_rect(5, 10, 6, 1, PINK_D)
    fill_rect(6, 11, 4, 1, PINK_D)
    # Wrapper twist left
    fill_rect(2, 7, 2, 2, WRAPPER)
    set_pixel(1, 6, WRAPPER)
    set_pixel(1, 9, WRAPPER)
    # Wrapper twist right
    fill_rect(12, 7, 2, 2, WRAPPER)
    set_pixel(14, 6, WRAPPER)
    set_pixel(14, 9, WRAPPER)
    # Stripe on candy
    for y in range(5, 11):
        set_pixel(8, y, WHITE)

    write_png(os.path.join(ITEM_TEXTURE_DIR, "whirlm_candy.png"), W, H, pixels)
    print("  Generated whirlm_candy.png")

def generate_entity_texture(name, w, h, main_color, light_color, dark_color,
                            accent_color, eye_color=(20,15,15,255), has_stripes=True):
    """Generic piñata entity texture generator.
    Creates a paper-piñata look with stripes, highlights, shadows, and eyes."""
    pixels = [main_color] * (w * h)

    def set_pixel(x, y, color):
        if 0 <= x < w and 0 <= y < h:
            pixels[y * w + x] = color

    def fill_rect(x1, y1, rw, rh, color):
        for dy in range(rh):
            for dx in range(rw):
                set_pixel(x1 + dx, y1 + dy, color)

    # Paper-fold horizontal stripes
    if has_stripes:
        for y in range(0, h, 3):
            for x in range(w):
                set_pixel(x, y, accent_color)

    # Top shadow
    for x in range(w):
        set_pixel(x, 0, dark_color)
        if h > 1:
            set_pixel(x, 1, dark_color)

    # Bottom highlight
    for x in range(w):
        set_pixel(x, h - 1, light_color)
        if h > 1:
            set_pixel(x, h - 2, light_color)

    # Eye area (top-left quadrant of texture, front face area)
    eye_y = min(h // 4, 4)
    eye_x1 = w // 4
    eye_x2 = w // 4 + 3
    set_pixel(eye_x1, eye_y, eye_color)
    set_pixel(eye_x1 + 1, eye_y, eye_color)
    set_pixel(eye_x2, eye_y, eye_color)
    set_pixel(eye_x2 + 1, eye_y, eye_color)
    # Eye highlights
    set_pixel(eye_x1, eye_y - 1, (255, 255, 255, 255))
    set_pixel(eye_x2, eye_y - 1, (255, 255, 255, 255))

    # Side shading
    for y in range(h):
        set_pixel(0, y, dark_color)
        set_pixel(w - 1, y, dark_color)

    write_png(os.path.join(TEXTURE_DIR, f"{name}.png"), w, h, pixels)
    print(f"  Generated {name}.png")

def generate_sour_texture(name, w, h, main_color, dark_color):
    """Generate a sour variant — dark, cracked, hostile."""
    # Darken the main color
    sour_main = tuple(max(0, c - 80) for c in main_color[:3]) + (255,)
    sour_dark = tuple(max(0, c - 100) for c in dark_color[:3]) + (255,)
    sour_light = tuple(min(255, c + 20) for c in sour_main[:3]) + (255,)
    crack = tuple(max(0, c - 120) for c in main_color[:3]) + (255,)

    pixels = [sour_main] * (w * h)

    def set_pixel(x, y, color):
        if 0 <= x < w and 0 <= y < h:
            pixels[y * w + x] = color

    # Cracks
    import random
    rng = random.Random(hash(name))
    for _ in range(w * h // 20):
        cx = rng.randint(0, w - 1)
        cy = rng.randint(0, h - 1)
        set_pixel(cx, cy, crack)
        if cx + 1 < w:
            set_pixel(cx + 1, cy, crack)

    # Dark top/bottom
    for x in range(w):
        set_pixel(x, 0, sour_dark)
        set_pixel(x, h - 1, sour_dark)

    # Red angry eyes
    eye_y = min(h // 4, 4)
    eye_x1 = w // 4
    eye_x2 = w // 4 + 3
    red_eye = (220, 30, 30, 255)
    set_pixel(eye_x1, eye_y, red_eye)
    set_pixel(eye_x1 + 1, eye_y, red_eye)
    set_pixel(eye_x2, eye_y, red_eye)
    set_pixel(eye_x2 + 1, eye_y, red_eye)

    write_png(os.path.join(TEXTURE_DIR, f"{name}_sour.png"), w, h, pixels)
    print(f"  Generated {name}_sour.png")

def generate_candy_texture(name, main_color, light_color, dark_color, stripe_color):
    """Generate a 16x16 wrapped candy item texture."""
    W, H = 16, 16
    pixels = [(0, 0, 0, 0)] * (W * H)

    def set_pixel(x, y, color):
        if 0 <= x < W and 0 <= y < H:
            pixels[y * W + x] = color

    def fill_rect(x1, y1, w, h, color):
        for dy in range(h):
            for dx in range(w):
                set_pixel(x1 + dx, y1 + dy, color)

    # Candy body
    fill_rect(5, 5, 6, 6, main_color)
    fill_rect(6, 4, 4, 8, main_color)
    fill_rect(4, 6, 8, 4, main_color)
    # Highlight
    set_pixel(6, 5, light_color)
    set_pixel(7, 5, light_color)
    set_pixel(6, 6, light_color)
    # Shadow
    fill_rect(5, 10, 6, 1, dark_color)
    fill_rect(6, 11, 4, 1, dark_color)
    # Wrapper twist left
    wrapper = tuple(min(255, c + 40) for c in main_color[:3]) + (255,)
    fill_rect(2, 7, 2, 2, wrapper)
    set_pixel(1, 6, wrapper)
    set_pixel(1, 9, wrapper)
    # Wrapper twist right
    fill_rect(12, 7, 2, 2, wrapper)
    set_pixel(14, 6, wrapper)
    set_pixel(14, 9, wrapper)
    # Stripe
    for y in range(5, 11):
        set_pixel(8, y, stripe_color)

    write_png(os.path.join(ITEM_TEXTURE_DIR, f"{name}_candy.png"), W, H, pixels)
    print(f"  Generated {name}_candy.png")

# === Species color definitions ===
SPECIES = {
    "sparrowmint": {
        "w": 32, "h": 32,
        "main": (123, 200, 108, 255),   # Mint green
        "light": (180, 230, 160, 255),  # Light green
        "dark": (70, 140, 60, 255),     # Dark green
        "accent": (200, 230, 160, 255), # Yellow-green stripe
    },
    "fudgehog": {
        "w": 48, "h": 32,
        "main": (139, 94, 60, 255),     # Chocolate brown
        "light": (200, 160, 100, 255),  # Caramel
        "dark": (90, 55, 30, 255),      # Dark chocolate
        "accent": (212, 160, 86, 255),  # Fudge highlight
    },
    "mousemallow": {
        "w": 32, "h": 16,
        "main": (245, 224, 232, 255),   # White-pink marshmallow
        "light": (255, 240, 248, 255),  # Near white
        "dark": (210, 180, 195, 255),   # Dusty pink
        "accent": (255, 182, 217, 255), # Pink stripe
    },
    "syrupent": {
        "w": 32, "h": 32,
        "main": (212, 150, 10, 255),    # Golden amber
        "light": (240, 200, 80, 255),   # Light gold
        "dark": (139, 101, 8, 255),     # Dark amber
        "accent": (180, 130, 40, 255),  # Diamond pattern brown
    },
    "taffly": {
        "w": 32, "h": 16,
        "main": (200, 120, 40, 255),    # Toffee brown
        "light": (232, 208, 160, 255),  # Light toffee
        "dark": (140, 80, 20, 255),     # Dark toffee
        "accent": (220, 180, 100, 255), # Amber stripe
    },
    "bunnycomb": {
        "w": 32, "h": 32,
        "main": (232, 200, 80, 255),    # Warm honeycomb yellow
        "light": (250, 230, 140, 255),  # Light yellow
        "dark": (180, 140, 40, 255),    # Dark honey
        "accent": (240, 160, 48, 255),  # Orange accent
    },
    "quackberry": {
        "w": 32, "h": 32,
        "main": (64, 96, 208, 255),     # Blueberry blue
        "light": (120, 150, 230, 255),  # Light blue
        "dark": (40, 60, 150, 255),     # Dark blue
        "accent": (128, 96, 192, 255),  # Purple accent
    },
    "shellybean": {
        "w": 32, "h": 16,
        "main": (144, 216, 160, 255),   # Pastel green
        "light": (200, 240, 210, 255),  # Light mint
        "dark": (90, 160, 110, 255),    # Darker green
        "accent": (240, 192, 208, 255), # Pastel pink (jellybean)
    },
    "newtgat": {
        "w": 32, "h": 16,
        "main": (192, 128, 64, 255),    # Nougat brown-orange
        "light": (224, 176, 96, 255),   # Light nougat
        "dark": (140, 90, 40, 255),     # Dark nougat
        "accent": (200, 100, 40, 255),  # Orange spot
    },
    "lickatoad": {
        "w": 32, "h": 16,
        "main": (64, 192, 64, 255),     # Lollipop green
        "light": (128, 224, 96, 255),   # Light green
        "dark": (32, 128, 32, 255),     # Dark green
        "accent": (200, 230, 80, 255),  # Yellow-green spot
    },
    "pretztail": {
        "w": 32, "h": 32,
        "main": (208, 96, 32, 255),     # Pretzel orange-red
        "light": (240, 160, 80, 255),   # Light orange
        "dark": (139, 69, 19, 255),     # Saddle brown
        "accent": (180, 120, 60, 255),  # Pretzel brown stripe
    },
    "buzzlegum": {
        "w": 32, "h": 32,
        "main": (240, 208, 64, 255),    # Bubblegum yellow
        "light": (255, 240, 120, 255),  # Light yellow
        "dark": (32, 32, 32, 255),      # Black stripes
        "accent": (255, 180, 200, 255), # Bubblegum pink
    },
    "cluckles": {
        "w": 32, "h": 32,
        "main": (208, 160, 112, 255),   # Cookie brown
        "light": (232, 200, 160, 255),  # Light cookie
        "dark": (140, 100, 60, 255),    # Dark choc chip
        "accent": (192, 48, 32, 255),   # Red comb
    },
    "horstachio": {
        "w": 64, "h": 32,
        "main": (128, 176, 96, 255),    # Pistachio green
        "light": (200, 224, 160, 255),  # Light pistachio
        "dark": (80, 120, 56, 255),     # Dark green
        "accent": (216, 200, 160, 255), # Cream/tan accent
    },
}

if __name__ == "__main__":
    os.makedirs(TEXTURE_DIR, exist_ok=True)
    os.makedirs(ITEM_TEXTURE_DIR, exist_ok=True)

    print("Generating Viva Piñata textures...")

    # Whirlm (custom textures)
    generate_whirlm_texture()
    generate_whirlm_sour_texture()
    generate_whirlm_candy_texture()

    # All other species (generic system)
    for name, colors in SPECIES.items():
        generate_entity_texture(name, colors["w"], colors["h"],
                                colors["main"], colors["light"],
                                colors["dark"], colors["accent"])
        generate_sour_texture(name, colors["w"], colors["h"],
                              colors["main"], colors["dark"])
        generate_candy_texture(name, colors["main"], colors["light"],
                               colors["dark"], (255, 255, 255, 200))

    print("Done!")
