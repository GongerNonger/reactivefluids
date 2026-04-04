"""
Generate proper UV-mapped entity textures for the first 3 piñata species:
Whirlm, Sparrowmint, Fudgehog.

Each texture is painted to match the model's texOffs UV layout with proper
coloring, eyes, markings, and piñata paper-strip details.
"""
import os, zlib, struct, math, random

def make_png(width, height, rows):
    def chunk(tag, data):
        crc = zlib.crc32(tag + data) & 0xFFFFFFFF
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', crc)
    raw = b''.join(b'\x00' + b''.join(bytes([r&255,g&255,b&255,a&255]) for r,g,b,a in row) for row in rows)
    sig  = b'\x89PNG\r\n\x1a\n'
    ihdr = chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
    idat = chunk(b'IDAT', zlib.compress(raw, 9))
    iend = chunk(b'IEND', b'')
    return sig + ihdr + idat + iend

def save_png(path, rows):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(make_png(len(rows[0]), len(rows), rows))
    print(f"  {path}")

def cl(v): return max(0, min(255, int(round(v))))

BASE = "src/main/resources/assets/reactivefluids/textures/entity/pinata"


def fill_rect(rows, x0, y0, w, h, color):
    """Fill a rectangle on the texture."""
    for y in range(y0, min(y0 + h, len(rows))):
        for x in range(x0, min(x0 + w, len(rows[0]))):
            rows[y][x] = color

def set_pixel(rows, x, y, color):
    if 0 <= y < len(rows) and 0 <= x < len(rows[0]):
        rows[y][x] = color

def noise_fill(rows, x0, y0, w, h, base_color, noise_amt=12, seed=42):
    """Fill with base color + per-pixel noise."""
    rng = random.Random(seed)
    r, g, b = base_color
    for y in range(y0, min(y0 + h, len(rows))):
        for x in range(x0, min(x0 + w, len(rows[0]))):
            n = rng.randint(-noise_amt, noise_amt)
            rows[y][x] = (cl(r+n), cl(g+n), cl(b+n), 255)

def stripe_fill(rows, x0, y0, w, h, color1, color2, stripe_width=2, vertical=True, seed=42):
    """Fill with alternating stripes + noise for piñata paper look."""
    rng = random.Random(seed)
    for y in range(y0, min(y0 + h, len(rows))):
        for x in range(x0, min(x0 + w, len(rows[0]))):
            coord = x if vertical else y
            is_stripe = ((coord - x0) // stripe_width) % 2 == 0
            c = color1 if is_stripe else color2
            n = rng.randint(-6, 6)
            rows[y][x] = (cl(c[0]+n), cl(c[1]+n), cl(c[2]+n), 255)


# =============================================================================
# WHIRLM — 64x32 texture
# Model UV layout:
#   head:  texOffs(0,0)  box 5x5x4  → UV uses 0..13 x 0..14 area
#   body1: texOffs(0,10) box 4x4x4  → UV uses 0..12 x 10..22 area
#   body2: texOffs(0,19) box 4x4x4  → UV uses 0..12 x 19..31 area
#   tail:  texOffs(18,0) box 3x3x4  → UV uses 18..28 x 0..10 area
#
# Whirlm colors: pink/magenta body, lighter belly, dark stripe between segments
# =============================================================================
def make_whirlm_texture():
    W, H = 64, 32
    rows = [[(0,0,0,0) for _ in range(W)] for _ in range(H)]

    PINK_LIGHT = (220, 140, 180)    # Top/sides highlight
    PINK_MID   = (200, 100, 150)    # Main body
    PINK_DARK  = (160, 70, 120)     # Belly/underside
    PINK_STRIPE = (140, 50, 100)    # Segment dividers
    WHITE      = (240, 230, 235)    # Eye whites
    BLACK      = (30, 20, 25)       # Pupils

    # Head (texOffs 0,0 — 5w 5h 4d)
    # UV net: top(4x5 at 4,0), front(5x5 at 4,4), sides, etc.
    # For a box WxHxD, UV layout:
    #   Row 0: [D-wide gap] [D x W top] [D x W bottom]
    #   Row D: [D x H left] [W x H front] [D x H right] [W x H back]
    noise_fill(rows, 0, 0, 18, 14, PINK_MID, 10, 100)
    # Head front face — add eyes (approx at texOffs 4+4=8 area)
    # Front face starts at x=4, y=4, size 5x5
    set_pixel(rows, 5, 6, WHITE + (255,))   # Left eye white
    set_pixel(rows, 6, 6, WHITE + (255,))
    set_pixel(rows, 6, 6, BLACK + (255,))   # Left pupil
    set_pixel(rows, 8, 6, WHITE + (255,))   # Right eye white
    set_pixel(rows, 7, 6, WHITE + (255,))
    set_pixel(rows, 7, 6, BLACK + (255,))   # Right pupil
    # Lighter top of head
    noise_fill(rows, 4, 0, 5, 4, PINK_LIGHT, 8, 101)
    # Darker bottom
    noise_fill(rows, 13, 0, 5, 4, PINK_DARK, 8, 102)

    # Body segment 1 (texOffs 0,10 — 4w 4h 4d)
    noise_fill(rows, 0, 10, 16, 12, PINK_MID, 10, 200)
    # Top lighter
    noise_fill(rows, 4, 10, 4, 4, PINK_LIGHT, 8, 201)
    # Bottom darker
    noise_fill(rows, 12, 10, 4, 4, PINK_DARK, 8, 202)
    # Stripe at edges (segment divider)
    noise_fill(rows, 4, 14, 4, 1, PINK_STRIPE, 5, 203)
    noise_fill(rows, 4, 17, 4, 1, PINK_STRIPE, 5, 204)

    # Body segment 2 (texOffs 0,19 — 4w 4h 4d)
    noise_fill(rows, 0, 19, 16, 12, PINK_MID, 10, 300)
    noise_fill(rows, 4, 19, 4, 4, PINK_LIGHT, 8, 301)
    noise_fill(rows, 12, 19, 4, 4, PINK_DARK, 8, 302)
    noise_fill(rows, 4, 23, 4, 1, PINK_STRIPE, 5, 303)
    noise_fill(rows, 4, 26, 4, 1, PINK_STRIPE, 5, 304)

    # Tail (texOffs 18,0 — 3w 3h 4d)
    noise_fill(rows, 18, 0, 14, 10, PINK_DARK, 10, 400)
    noise_fill(rows, 21, 0, 3, 3, PINK_LIGHT, 8, 401)
    # Tail tip slightly darker/redder
    noise_fill(rows, 21, 3, 3, 3, (180, 80, 130), 8, 402)

    # Add paper-strip texture lines throughout
    rng = random.Random(500)
    for y in range(H):
        for x in range(W):
            if rows[y][x][3] > 0 and rng.random() < 0.08:
                r, g, b, a = rows[y][x]
                # Subtle lighter paper fold line
                rows[y][x] = (cl(r+15), cl(g+15), cl(b+15), 255)

    return rows


# =============================================================================
# SPARROWMINT — 32x32 texture
# Model UV layout:
#   body:      texOffs(0,0)  box 6x6x7
#   head:      texOffs(0,14) box 4x4x3 + beak texOffs(26,0) 1x1x2
#   wing_left: texOffs(0,22) box 1x4x5
#   wing_right:texOffs(0,22) box 1x4x5 (shared UV)
#   leg_left:  texOffs(26,4) box 1x2x1
#   leg_right: texOffs(26,4) box 1x2x1 (shared UV)
#   tail:      texOffs(13,14) box 4x2x3
#
# Sparrowmint colors: mint green body, lighter belly, darker wing tips,
# yellow/orange beak, white eye spots with black pupils
# =============================================================================
def make_sparrowmint_texture():
    W, H = 32, 32
    rows = [[(0,0,0,0) for _ in range(W)] for _ in range(H)]

    MINT_LIGHT  = (140, 210, 160)   # Top/highlight
    MINT_MID    = (100, 180, 120)   # Main body
    MINT_DARK   = (60, 140, 80)     # Underside/shadows
    WING_GREEN  = (70, 150, 90)     # Wing base
    WING_TIP    = (40, 100, 60)     # Wing tips darker
    BEAK_YELLOW = (220, 180, 60)    # Beak
    LEG_ORANGE  = (200, 140, 60)    # Legs
    WHITE       = (240, 240, 240)   # Eye whites
    BLACK       = (20, 20, 20)      # Pupils
    TAIL_GREEN  = (80, 160, 100)    # Tail feathers

    # Body (texOffs 0,0 — 6w 6h 7d)
    # UV top row: gap(7) then top(6x7) then bottom(6x7)
    # UV body row: left(7x6) front(6x6) right(7x6) back(6x6)
    noise_fill(rows, 0, 0, 26, 13, MINT_MID, 10, 100)
    # Top face lighter
    noise_fill(rows, 7, 0, 6, 7, MINT_LIGHT, 8, 101)
    # Bottom face darker
    noise_fill(rows, 13, 0, 6, 7, MINT_DARK, 8, 102)
    # Front face — slight belly highlight
    noise_fill(rows, 7, 7, 6, 6, MINT_MID, 8, 103)
    # Lighter center belly stripe on front
    noise_fill(rows, 9, 9, 2, 3, MINT_LIGHT, 5, 104)

    # Head (texOffs 0,14 — 4w 4h 3d)
    noise_fill(rows, 0, 14, 14, 11, MINT_MID, 10, 200)
    # Top lighter
    noise_fill(rows, 3, 14, 4, 3, MINT_LIGHT, 8, 201)
    # Front face at (3, 17) size 4x4
    noise_fill(rows, 3, 17, 4, 4, MINT_LIGHT, 6, 202)
    # Eyes on front face
    set_pixel(rows, 4, 18, WHITE + (255,))
    set_pixel(rows, 6, 18, WHITE + (255,))
    set_pixel(rows, 4, 18, BLACK + (255,))   # Left pupil (overwrite for small eye)
    set_pixel(rows, 6, 18, BLACK + (255,))   # Right pupil
    # Actually make eyes 2px — white with black pupil
    set_pixel(rows, 4, 18, WHITE + (255,))
    set_pixel(rows, 4, 19, BLACK + (255,))
    set_pixel(rows, 6, 18, WHITE + (255,))
    set_pixel(rows, 6, 19, BLACK + (255,))

    # Beak (texOffs 26,0 — 1w 1h 2d)
    noise_fill(rows, 26, 0, 6, 4, BEAK_YELLOW, 8, 210)

    # Wings (texOffs 0,22 — 1w 4h 5d)
    noise_fill(rows, 0, 22, 12, 10, WING_GREEN, 10, 300)
    # Wing tips darker (bottom part of wing UV)
    noise_fill(rows, 5, 27, 1, 4, WING_TIP, 8, 301)
    noise_fill(rows, 6, 27, 1, 4, WING_TIP, 8, 302)

    # Legs (texOffs 26,4 — 1w 2h 1d)
    noise_fill(rows, 26, 4, 4, 4, LEG_ORANGE, 8, 400)

    # Tail (texOffs 13,14 — 4w 2h 3d)
    noise_fill(rows, 13, 14, 14, 8, TAIL_GREEN, 10, 500)
    # Lighter feather centers
    noise_fill(rows, 16, 17, 4, 2, MINT_LIGHT, 6, 501)
    # Darker tail tips
    noise_fill(rows, 16, 19, 4, 1, WING_TIP, 6, 502)

    # Paper texture overlay
    rng = random.Random(600)
    for y in range(H):
        for x in range(W):
            if rows[y][x][3] > 0 and rng.random() < 0.07:
                r, g, b, a = rows[y][x]
                rows[y][x] = (cl(r+12), cl(g+12), cl(b+12), 255)

    return rows


# =============================================================================
# FUDGEHOG — 48x32 texture
# Model UV layout:
#   body:     texOffs(0,0)  box 7x6x8
#   spines:   texOffs(0,15) box 8x4x7 (inflated 0.3)
#   head:     texOffs(22,0) box 3x3x3 + nose texOffs(30,0) 1x1x1
#   legs:     texOffs(22,7) box 1x3x1 (x4, shared UV)
#
# Fudgehog colors: dark chocolate brown spines, lighter tan/cream face and belly,
# caramel/fudge body, pink nose, beady black eyes
# =============================================================================
def make_fudgehog_texture():
    W, H = 48, 32
    rows = [[(0,0,0,0) for _ in range(W)] for _ in range(H)]

    CHOC_DARK   = (80, 50, 30)      # Dark chocolate spines
    CHOC_MID    = (120, 75, 45)     # Medium chocolate body
    CHOC_LIGHT  = (160, 110, 70)    # Lighter chocolate
    CARAMEL     = (190, 140, 80)    # Caramel highlights
    CREAM       = (220, 200, 170)   # Belly/face cream
    FACE_TAN    = (200, 170, 130)   # Face
    PINK_NOSE   = (220, 140, 140)   # Nose
    BLACK       = (20, 15, 10)      # Eyes/pupils
    WHITE       = (240, 235, 225)   # Eye highlights
    LEG_BROWN   = (100, 65, 40)     # Leg color

    # Body (texOffs 0,0 — 7w 6h 8d)
    # Top row: gap(8) top(7x8) bottom(7x8)
    # Body row: left(8x6) front(7x6) right(8x6) back(7x6)
    noise_fill(rows, 0, 0, 30, 14, CHOC_MID, 10, 100)
    # Top face
    noise_fill(rows, 8, 0, 7, 8, CHOC_LIGHT, 8, 101)
    # Bottom face — cream belly!
    noise_fill(rows, 15, 0, 7, 8, CREAM, 8, 102)
    # Front face
    noise_fill(rows, 8, 8, 7, 6, CARAMEL, 8, 103)
    # Back face
    noise_fill(rows, 23, 8, 7, 6, CHOC_MID, 8, 104)
    # Sides
    noise_fill(rows, 0, 8, 8, 6, CHOC_MID, 8, 105)
    noise_fill(rows, 15, 8, 8, 6, CHOC_MID, 8, 106)

    # Spines (texOffs 0,15 — 8w 4h 7d)
    # Inflated overlay — dark spiny chocolate
    noise_fill(rows, 0, 15, 30, 16, CHOC_DARK, 12, 200)
    # Top of spines — vary between dark and medium for texture
    stripe_fill(rows, 7, 15, 8, 7, CHOC_DARK, (60, 35, 20), 1, True, 201)
    # Spines sides — add pointed texture look with alternating dark/lighter
    stripe_fill(rows, 0, 22, 7, 4, CHOC_DARK, CHOC_MID, 2, True, 202)
    stripe_fill(rows, 15, 22, 7, 4, CHOC_DARK, CHOC_MID, 2, True, 203)
    # Front spines — slightly lighter to show separation from face
    noise_fill(rows, 7, 22, 8, 4, (90, 55, 35), 8, 204)

    # Head (texOffs 22,0 — 3w 3h 3d)
    noise_fill(rows, 22, 0, 12, 9, FACE_TAN, 8, 300)
    # Front face of head at (25, 3) size 3x3
    noise_fill(rows, 25, 3, 3, 3, CREAM, 6, 301)
    # Eyes on face
    set_pixel(rows, 25, 4, BLACK + (255,))   # Left eye
    set_pixel(rows, 27, 4, BLACK + (255,))   # Right eye
    # Eye highlights (tiny white dots)
    set_pixel(rows, 25, 3, WHITE + (255,))
    set_pixel(rows, 27, 3, WHITE + (255,))
    # Mouth/smile line
    set_pixel(rows, 26, 5, (150, 120, 90, 255))

    # Nose (texOffs 30,0 — 1w 1h 1d)
    noise_fill(rows, 30, 0, 4, 3, PINK_NOSE, 8, 310)

    # Legs (texOffs 22,7 — 1w 3h 1d)
    noise_fill(rows, 22, 7, 4, 5, LEG_BROWN, 8, 400)

    # Paper-strip texture overlay
    rng = random.Random(700)
    for y in range(H):
        for x in range(W):
            if rows[y][x][3] > 0:
                # Horizontal paper fold lines
                if y % 4 == 0 and rng.random() < 0.3:
                    r, g, b, a = rows[y][x]
                    rows[y][x] = (cl(r-8), cl(g-8), cl(b-8), 255)
                # Random paper texture
                elif rng.random() < 0.06:
                    r, g, b, a = rows[y][x]
                    rows[y][x] = (cl(r+10), cl(g+10), cl(b+10), 255)

    return rows


# =============================================================================
# Also generate sour variants — darker/more muted versions
# =============================================================================
def make_sour_variant(rows, tint=(60, 40, 80), blend=0.35, seed=999):
    """Create a sour variant by blending toward a dark purple/grey tint."""
    rng = random.Random(seed)
    H = len(rows)
    W = len(rows[0])
    sour = [[rows[y][x] for x in range(W)] for y in range(H)]
    for y in range(H):
        for x in range(W):
            r, g, b, a = rows[y][x]
            if a > 0:
                # Desaturate + tint toward sour color
                lum = 0.299 * r + 0.587 * g + 0.114 * b
                nr = r + 0.4 * (lum - r)  # Desaturate
                ng = g + 0.4 * (lum - g)
                nb = b + 0.4 * (lum - b)
                # Blend toward sour tint
                nr = nr * (1-blend) + tint[0] * blend
                ng = ng * (1-blend) + tint[1] * blend
                nb = nb * (1-blend) + tint[2] * blend
                # Darken slightly
                nr *= 0.85; ng *= 0.85; nb *= 0.85
                n = rng.randint(-5, 5)
                sour[y][x] = (cl(nr+n), cl(ng+n), cl(nb+n), 255)
    return sour


if __name__ == "__main__":
    print("=== Whirlm texture ===")
    whirlm = make_whirlm_texture()
    save_png(f"{BASE}/whirlm.png", whirlm)
    save_png(f"{BASE}/whirlm_sour.png", make_sour_variant(whirlm, seed=1001))

    print("=== Sparrowmint texture ===")
    sparrowmint = make_sparrowmint_texture()
    save_png(f"{BASE}/sparrowmint.png", sparrowmint)
    save_png(f"{BASE}/sparrowmint_sour.png", make_sour_variant(sparrowmint, seed=1002))

    print("=== Fudgehog texture ===")
    fudgehog = make_fudgehog_texture()
    save_png(f"{BASE}/fudgehog.png", fudgehog)
    save_png(f"{BASE}/fudgehog_sour.png", make_sour_variant(fudgehog, seed=1003))

    print("\nDone! Generated 6 textures (3 normal + 3 sour variants)")
