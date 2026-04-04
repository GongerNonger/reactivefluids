"""
Generate jellyfish entity textures (stdlib only — zlib, struct, os).
Produces:
  1. textures/entity/moonlight_jellyfish.png        (64x64 base)
  2. textures/entity/moonlight_jellyfish_glow.png    (64x64 glow overlay)
  3. textures/item/moonlight_jellyfish_spawn_egg.png (16x16 spawn egg)
"""
import zlib, struct, os, math, random

# ── PNG writer (same as generate_textures.py) ────────────────────────────
def make_png(width, height, rows):
    def chunk(tag, data):
        crc = zlib.crc32(tag + data) & 0xFFFFFFFF
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', crc)
    raw = b''.join(
        b'\x00' + b''.join(bytes([r & 255, g & 255, b & 255, a & 255])
                           for r, g, b, a in row)
        for row in rows
    )
    sig  = b'\x89PNG\r\n\x1a\n'
    ihdr = chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
    idat = chunk(b'IDAT', zlib.compress(raw, 9))
    iend = chunk(b'IEND', b'')
    return sig + ihdr + idat + iend

def save_png(path, rows):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(make_png(len(rows[0]), len(rows), rows))
    print(f"  wrote {path}")

def cl(v):
    return max(0, min(255, int(round(v))))

# ── Deterministic seed ───────────────────────────────────────────────────
random.seed(42)

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                    "src", "main", "resources", "assets", "reactivefluids")

# =========================================================================
# Helper: draw a cube unwrap onto a pixel grid
# =========================================================================
# Minecraft cube UV unwrap layout (standard):
#
#     [  top  ]                    row 0..dz-1
#     [ front ][ right ][ back ][ left ]   row dz..dz+dy-1
#     [bottom ]                    row dz+dy..2*dz+dy-1   (some packs)
#
# More precisely for a cube of size (dx, dy, dz):
#   The UV area is (2*dz + 2*dx) wide  x  (dz + dy) tall.
#   Layout (col, row offsets within that area):
#     Top:    (dz,    0)         size dx x dz
#     Bottom: (dz+dx, 0)        size dx x dz
#     Front:  (dz,    dz)       size dx x dy
#     Right:  (dz+dx, dz)       size dz x dy
#     Back:   (dz+dx+dz, dz)    size dx x dy
#     Left:   (0,     dz)       size dz x dy

def _face_rect(tex_u, tex_v, dx, dy, dz, face):
    """Return (col, row, w, h) of *face* within the unwrap anchored at (tex_u, tex_v)."""
    if face == 'top':
        return (tex_u + dz, tex_v, dx, dz)
    elif face == 'bottom':
        return (tex_u + dz + dx, tex_v, dx, dz)
    elif face == 'front':
        return (tex_u + dz, tex_v + dz, dx, dy)
    elif face == 'right':
        return (tex_u + dz + dx, tex_v + dz, dz, dy)
    elif face == 'back':
        return (tex_u + dz + dx + dz, tex_v + dz, dx, dy)
    elif face == 'left':
        return (tex_u, tex_v + dz, dz, dy)


def fill_rect(pixels, col, row, w, h, color_fn):
    """Fill rectangle; color_fn(local_x, local_y, w, h) -> (r,g,b,a)."""
    for ly in range(h):
        for lx in range(w):
            py = row + ly
            px = col + lx
            if 0 <= py < len(pixels) and 0 <= px < len(pixels[0]):
                pixels[py][px] = color_fn(lx, ly, w, h)


def paint_cube(pixels, tex_u, tex_v, dx, dy, dz, color_fn):
    """Paint all six faces of a cube unwrap."""
    for face in ('top', 'bottom', 'front', 'right', 'back', 'left'):
        c, r, w, h = _face_rect(tex_u, tex_v, dx, dy, dz, face)
        fill_rect(pixels, c, r, w, h, color_fn)


# =========================================================================
# 1. Base jellyfish texture  (64x64)
# =========================================================================
def gen_base():
    W, H = 64, 64
    pixels = [[(0, 0, 0, 0) for _ in range(W)] for _ in range(H)]

    # Bell dome: 10x6x10 cube at texOffs(0,0)
    def bell_color(lx, ly, w, h):
        # Subtle edge darkening
        cx, cy = w / 2, h / 2
        dx_n = (lx - cx) / max(cx, 1)
        dy_n = (ly - cy) / max(cy, 1)
        edge = math.sqrt(dx_n * dx_n + dy_n * dy_n)
        edge = min(edge, 1.0)
        # Base: pale blue-white
        r = cl(190 - 30 * edge + 4 * math.sin(lx * 0.9 + ly * 0.7))
        g = cl(218 - 18 * edge + 3 * math.sin(lx * 1.1 + ly * 0.5))
        b = cl(242 - 12 * edge + 2 * math.sin(lx * 0.7 + ly * 1.3))
        a = cl(195 - 30 * edge)  # semi-transparent
        return (r, g, b, a)

    paint_cube(pixels, 0, 0, 10, 6, 10, bell_color)

    # Inner bell: 8x4x8 cube at texOffs(0,16)
    def inner_color(lx, ly, w, h):
        cx, cy = w / 2, h / 2
        dx_n = (lx - cx) / max(cx, 1)
        dy_n = (ly - cy) / max(cy, 1)
        edge = math.sqrt(dx_n * dx_n + dy_n * dy_n)
        edge = min(edge, 1.0)
        r = cl(155 - 25 * edge + 3 * math.sin(lx * 1.2))
        g = cl(195 - 15 * edge + 3 * math.cos(ly * 0.8))
        b = cl(235 - 15 * edge)
        a = cl(180 - 20 * edge)
        return (r, g, b, a)

    paint_cube(pixels, 0, 16, 8, 4, 8, inner_color)

    # Four tentacles: 1x8x1 cubes at texOffs 0,28 / 4,28 / 8,28 / 12,28
    for i, tu in enumerate([0, 4, 8, 12]):
        def tent_color(lx, ly, w, h, _i=i):
            # Gradient from blue-white top to transparent tip
            t = ly / max(h - 1, 1)  # 0=top, 1=bottom
            r = cl(180 - 40 * t + random.randint(-3, 3))
            g = cl(210 - 30 * t + random.randint(-3, 3))
            b = cl(240 - 20 * t + random.randint(-2, 2))
            a = cl(170 - 130 * t)  # fade to near-transparent
            return (r, g, b, a)
        paint_cube(pixels, tu, 28, 1, 8, 1, tent_color)

    save_png(os.path.join(BASE, "textures", "entity", "moonlight_jellyfish.png"), pixels)


# =========================================================================
# 2. Glow overlay  (64x64)
# =========================================================================
def gen_glow():
    W, H = 64, 64
    pixels = [[(0, 0, 0, 0) for _ in range(W)] for _ in range(H)]

    # Bell dome glow: cyan-white spots / vein pattern
    def bell_glow(lx, ly, w, h):
        # Create vein-like glow using overlapping sine waves
        v1 = math.sin(lx * 1.3 + ly * 0.4) * 0.5 + 0.5
        v2 = math.sin(lx * 0.5 + ly * 1.7 + 1.0) * 0.5 + 0.5
        v3 = math.sin(lx * 2.1 - ly * 0.9 + 2.3) * 0.5 + 0.5
        intensity = (v1 * v2 + v3 * 0.3) / 1.3
        # Spots: local bright areas
        spot = 0.0
        spots = [(3, 2), (7, 4), (5, 8), (2, 6), (9, 3)]
        for sx, sy in spots:
            d = math.sqrt((lx - sx) ** 2 + (ly - sy) ** 2)
            spot = max(spot, max(0, 1.0 - d / 3.0))
        intensity = min(1.0, intensity * 0.6 + spot * 0.6)
        if intensity < 0.15:
            return (0, 0, 0, 0)
        r = cl(150 + 70 * intensity)
        g = cl(230 + 25 * intensity)
        b = 255
        a = cl(180 + 40 * intensity)
        return (r, g, b, a)

    paint_cube(pixels, 0, 0, 10, 6, 10, bell_glow)

    # Inner bell glow: brighter concentrated
    def inner_glow(lx, ly, w, h):
        cx, cy = w / 2, h / 2
        d = math.sqrt((lx - cx) ** 2 + (ly - cy) ** 2)
        maxd = math.sqrt(cx ** 2 + cy ** 2)
        t = 1.0 - min(d / max(maxd, 1), 1.0)
        if t < 0.1:
            return (0, 0, 0, 0)
        r = cl(100 + 80 * t)
        g = cl(200 + 55 * t)
        b = 255
        a = cl(160 + 40 * t)
        return (r, g, b, a)

    paint_cube(pixels, 0, 16, 8, 4, 8, inner_glow)

    # Tentacle base glow (fade out toward tips)
    for i, tu in enumerate([0, 4, 8, 12]):
        def tent_glow(lx, ly, w, h, _i=i):
            t = ly / max(h - 1, 1)  # 0=top (base), 1=bottom (tip)
            intensity = max(0, 1.0 - t * 1.5)  # fade out quickly
            if intensity < 0.05:
                return (0, 0, 0, 0)
            r = cl(120 + 60 * intensity)
            g = cl(210 + 30 * intensity)
            b = 255
            a = cl(80 + 70 * intensity)
            return (r, g, b, a)
        paint_cube(pixels, tu, 28, 1, 8, 1, tent_glow)

    save_png(os.path.join(BASE, "textures", "entity", "moonlight_jellyfish_glow.png"), pixels)


# =========================================================================
# 3. Spawn egg  (16x16)
# =========================================================================
def gen_spawn_egg():
    W, H = 16, 16
    pixels = [[(0, 0, 0, 0) for _ in range(W)] for _ in range(H)]

    # Vanilla spawn egg shape mask (1 = inside egg, row by row)
    # Classic MC spawn egg: roughly oval centered, ~10 wide x 13 tall
    egg_rows = [
        #  0123456789ABCDEF
        "................",  # 0
        "................",  # 1
        "......XXXX......",  # 2
        ".....XXXXXX.....",  # 3
        "....XXXXXXXX....",  # 4
        "....XXXXXXXX....",  # 5
        "...XXXXXXXXXX...",  # 6
        "...XXXXXXXXXX...",  # 7
        "...XXXXXXXXXX...",  # 8
        "...XXXXXXXXXX...",  # 9
        "....XXXXXXXX....",  # 10
        "....XXXXXXXX....",  # 11
        ".....XXXXXX.....",  # 12
        "......XXXX......",  # 13
        "................",  # 14
        "................",  # 15
    ]

    def in_egg(x, y):
        if 0 <= y < 16 and 0 <= x < 16:
            return egg_rows[y][x] == 'X'
        return False

    def is_edge(x, y):
        if not in_egg(x, y):
            return False
        for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            if not in_egg(x + dx, y + dy):
                return True
        return False

    # Base color: dark ocean blue
    base = (20, 40, 80, 255)
    # Outline: very dark
    outline = (8, 15, 35, 255)
    # Highlight color (specular)
    highlight = (60, 90, 140, 255)

    # Spot positions (glowing cyan spots)
    spots = [(8, 5), (6, 8), (10, 9), (7, 11)]
    spot_color = (100, 220, 255, 255)
    spot_highlight = (160, 240, 255, 255)

    for y in range(H):
        for x in range(W):
            if not in_egg(x, y):
                continue
            if is_edge(x, y):
                pixels[y][x] = outline
                continue

            # Default: base fill
            c = base

            # Specular highlight (upper-left area of egg)
            hx, hy = 6, 4
            hd = math.sqrt((x - hx) ** 2 + (y - hy) ** 2)
            if hd < 2.5:
                t = 1.0 - hd / 2.5
                c = (cl(base[0] + (highlight[0] - base[0]) * t),
                     cl(base[1] + (highlight[1] - base[1]) * t),
                     cl(base[2] + (highlight[2] - base[2]) * t),
                     255)

            # Slight vertical shading (darker at bottom)
            shade = 1.0 - 0.15 * ((y - 2) / 12)
            c = (cl(c[0] * shade), cl(c[1] * shade), cl(c[2] * shade), 255)

            # Spots
            for si, (sx, sy) in enumerate(spots):
                sd = math.sqrt((x - sx) ** 2 + (y - sy) ** 2)
                if sd < 1.2:
                    t = 1.0 - sd / 1.2
                    sc = spot_highlight if sd < 0.5 else spot_color
                    c = (cl(c[0] + (sc[0] - c[0]) * t),
                         cl(c[1] + (sc[1] - c[1]) * t),
                         cl(c[2] + (sc[2] - c[2]) * t),
                         255)

            pixels[y][x] = c

    save_png(os.path.join(BASE, "textures", "item", "moonlight_jellyfish_spawn_egg.png"), pixels)


# =========================================================================
# Main
# =========================================================================
if __name__ == '__main__':
    print("Generating jellyfish textures...")
    gen_base()
    gen_glow()
    gen_spawn_egg()
    print("Done.")
