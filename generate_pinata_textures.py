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

if __name__ == "__main__":
    os.makedirs(TEXTURE_DIR, exist_ok=True)
    os.makedirs(ITEM_TEXTURE_DIR, exist_ok=True)

    print("Generating Viva Piñata textures...")
    generate_whirlm_texture()
    generate_whirlm_sour_texture()
    generate_whirlm_candy_texture()
    print("Done!")
