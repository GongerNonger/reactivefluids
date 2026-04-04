"""
Generate improved 16x16 cross-model mushroom block textures.
These are side-view mushroom textures displayed on two diagonal X-shaped planes.
Stem at bottom center, cap on top, transparent background.
"""
import zlib, struct, os, math, random

random.seed(42)

# ---------------------------------------------------------------------------
# PNG writer (stdlib only)
# ---------------------------------------------------------------------------
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
    print(f"  wrote {path}")

T = 16
TRANSPARENT = (0, 0, 0, 0)
OUT = "src/main/resources/assets/reactivefluids/textures/block"

def cl(v):
    return max(0, min(255, int(round(v))))

def mix(c1, c2, t):
    """Lerp two RGBA colors."""
    t = max(0.0, min(1.0, t))
    return tuple(cl(c1[i] + (c2[i] - c1[i]) * t) for i in range(4))

def darken(c, f):
    return (cl(c[0]*f), cl(c[1]*f), cl(c[2]*f), c[3])

def lighten(c, f):
    return (cl(c[0] + (255 - c[0]) * f), cl(c[1] + (255 - c[1]) * f), cl(c[2] + (255 - c[2]) * f), c[3])

def dist(x1, y1, x2, y2):
    return math.sqrt((x1-x2)**2 + (y1-y2)**2)

def new_grid():
    return [[TRANSPARENT for _ in range(T)] for _ in range(T)]

def noise_val(x, y, seed=0):
    """Simple deterministic pseudo-noise."""
    n = x * 374761393 + y * 668265263 + seed * 1274126177
    n = (n ^ (n >> 13)) * 1274126177
    n = n ^ (n >> 16)
    return (n & 0xFFFF) / 65535.0

# ===========================================================================
# 1. Ghost Fungus — cream/white cap with blue-green gills, subtle glow
# ===========================================================================
def gen_ghost_fungus():
    grid = new_grid()
    cap_color = (220, 215, 200, 255)
    glow_color = (240, 245, 230, 255)
    gill_color = (100, 180, 170, 255)
    stem_color = (200, 195, 185, 255)

    # Stem: rows 10-14, cols 6-9
    for y in range(10, 15):
        for x in range(6, 10):
            shade = 0.9 + noise_val(x, y, 1) * 0.1
            grid[y][x] = darken(stem_color, shade)

    # Cap: rows 2-9, bell/dome shape
    cap_widths = {2: (6,10), 3: (4,12), 4: (3,13), 5: (3,13), 6: (3,13), 7: (4,12), 8: (5,11), 9: (6,10)}
    for y, (xl, xr) in cap_widths.items():
        for x in range(xl, xr):
            cx, cy = 8.0, 5.5
            d = dist(x, y, cx, cy)
            # Base cap color with slight noise
            n = noise_val(x, y, 2) * 0.08
            c = darken(cap_color, 0.92 + n)

            # Glow effect on edges (top and sides)
            edge_d = min(x - xl, xr - 1 - x, y - 2)
            if edge_d <= 1:
                c = mix(c, glow_color, 0.5)

            # Blue-green gill lines on underside (rows 7-9)
            if y >= 7:
                gill_intensity = (y - 6) / 3.0
                # Vertical gill lines every 2 pixels
                if x % 2 == 0:
                    c = mix(c, gill_color, gill_intensity * 0.6)

            grid[y][x] = c

    # Highlight on top of cap
    for x in range(6, 10):
        grid[2][x] = lighten(grid[2][x], 0.3) if grid[2][x][3] > 0 else grid[2][x]
        if grid[3][x][3] > 0:
            grid[3][x] = lighten(grid[3][x], 0.15)

    return grid

# ===========================================================================
# 2. Indigo Milk Cap — deep blue cap with concentric lighter rings
# ===========================================================================
def gen_indigo_milk_cap():
    grid = new_grid()
    cap_dark = (30, 50, 160, 255)
    cap_light = (70, 100, 200, 255)
    ring_color = (90, 120, 220, 255)
    stem_color = (100, 130, 190, 255)

    # Stem: rows 10-14, cols 6-9
    for y in range(10, 15):
        for x in range(6, 10):
            shade = 0.85 + noise_val(x, y, 3) * 0.15
            c = darken(stem_color, shade)
            # Slight gradient darker at edges
            if x == 6 or x == 9:
                c = darken(c, 0.85)
            grid[y][x] = c

    # Classic toadstool cap: rows 1-9, wider dome
    cap_widths = {1: (6,10), 2: (4,12), 3: (3,13), 4: (2,14), 5: (2,14),
                  6: (2,14), 7: (3,13), 8: (4,12), 9: (5,11)}
    cx, cy = 8.0, 5.0
    for y, (xl, xr) in cap_widths.items():
        for x in range(xl, xr):
            d = dist(x, y, cx, cy)
            # Base color - darker towards edges
            edge_factor = d / 7.0
            c = mix(cap_light, cap_dark, edge_factor)

            # Concentric rings
            ring_d = d % 2.5
            if ring_d < 0.7:
                c = mix(c, ring_color, 0.5)

            # Noise variation
            n = noise_val(x, y, 4) * 0.1
            c = darken(c, 0.9 + n)

            grid[y][x] = c

    # Cap rim highlight
    for x in range(4, 12):
        if grid[2][x][3] > 0:
            grid[2][x] = lighten(grid[2][x], 0.2)

    return grid

# ===========================================================================
# 3. Bleeding Tooth — white blobby mass with red droplets, no stem
# ===========================================================================
def gen_bleeding_tooth():
    grid = new_grid()
    base_color = (230, 225, 218, 255)
    drop_color = (180, 10, 10, 255)
    drop_dark = (140, 5, 5, 255)

    # Irregular blobby shape filling most of the 16x16
    # Define an organic blob using distance from multiple centers
    centers = [(7, 7), (9, 5), (5, 8), (10, 9), (6, 5)]
    radii = [5.5, 4.0, 4.0, 3.5, 3.5]

    for y in range(T):
        for x in range(T):
            inside = False
            for (ccx, ccy), r in zip(centers, radii):
                d = dist(x, y, ccx, ccy)
                # Add noise to radius for organic shape
                nr = r + (noise_val(x, y, 5) - 0.5) * 1.5
                if d < nr:
                    inside = True
                    break

            if inside:
                # Base white/cream with bumpy texture
                n = noise_val(x, y, 6)
                shade = 0.88 + n * 0.12
                c = darken(base_color, shade)

                # Surface bumps — lighter highlights
                if n > 0.7:
                    c = lighten(c, 0.15)
                elif n < 0.2:
                    c = darken(c, 0.9)

                grid[y][x] = c

    # Red droplets at specific positions
    droplets = [(5, 4), (8, 3), (10, 6), (4, 7), (7, 8), (11, 5),
                (6, 10), (9, 9), (3, 6), (8, 6), (12, 8)]
    for dx, dy in droplets:
        if 0 <= dy < T and 0 <= dx < T and grid[dy][dx][3] > 0:
            grid[dy][dx] = drop_color
            # Add darker center to some drops for depth
            if noise_val(dx, dy, 7) > 0.4:
                grid[dy][dx] = drop_dark
            # Drip trail below some droplets
            if dy + 1 < T and grid[dy+1][dx][3] > 0 and noise_val(dx, dy, 8) > 0.5:
                grid[dy+1][dx] = mix(drop_color, base_color, 0.4)

    return grid

# ===========================================================================
# 4. Amethyst Deceiver — vivid violet-purple small toadstool
# ===========================================================================
def gen_amethyst_deceiver():
    grid = new_grid()
    cap_outer = (120, 45, 170, 255)
    cap_center = (170, 100, 210, 255)
    stem_color = (150, 110, 180, 255)

    # Thin stem: rows 9-14, cols 7-8
    for y in range(9, 15):
        for x in range(7, 9):
            shade = 0.85 + noise_val(x, y, 10) * 0.15
            c = darken(stem_color, shade)
            if x == 7:
                c = darken(c, 0.9)
            grid[y][x] = c

    # Small toadstool cap: rows 2-8
    cap_widths = {2: (6,10), 3: (4,12), 4: (3,13), 5: (3,13),
                  6: (4,12), 7: (5,11), 8: (6,10)}
    cx, cy = 7.5, 5.0
    for y, (xl, xr) in cap_widths.items():
        for x in range(xl, xr):
            d = dist(x, y, cx, cy)
            # Lighter center gradient
            center_t = 1.0 - min(d / 5.5, 1.0)
            c = mix(cap_outer, cap_center, center_t * center_t)

            # Noise
            n = noise_val(x, y, 11) * 0.1
            c = darken(c, 0.9 + n)

            # Subtle radial lines
            angle = math.atan2(y - cy, x - cx)
            line_v = (math.sin(angle * 8) + 1) * 0.5
            if line_v > 0.7:
                c = lighten(c, 0.08)

            grid[y][x] = c

    # Top highlight
    for x in range(6, 10):
        if grid[2][x][3] > 0:
            grid[2][x] = lighten(grid[2][x], 0.25)

    # Underside slightly darker
    for x in range(5, 11):
        if grid[8][x][3] > 0:
            grid[8][x] = darken(grid[8][x], 0.8)

    return grid

# ===========================================================================
# 5. Lion's Mane — white cascading icicle tendrils, pom-pom shape
# ===========================================================================
def gen_lions_mane():
    grid = new_grid()
    base_white = (240, 238, 232, 255)
    shadow = (210, 205, 198, 255)
    highlight = (250, 248, 245, 255)

    # Lion's mane: cascading vertical tendrils from a central mass
    # Upper mass: rows 1-5, cols 3-12
    # Tendrils hang down: rows 6-14 with varying lengths per column

    # Tendril definitions: (col, start_row, end_row)
    tendrils = [
        (3, 3, 8), (4, 2, 10), (5, 1, 12), (6, 1, 13),
        (7, 1, 14), (8, 1, 14), (9, 1, 13), (10, 1, 12),
        (11, 2, 10), (12, 3, 8),
    ]

    for col, start, end in tendrils:
        for y in range(start, end + 1):
            # Base color with vertical streak variation
            streak = noise_val(col, 0, 12) * 0.15
            n = noise_val(col, y, 13) * 0.1

            # Darker towards bottom (hanging effect)
            depth_factor = (y - start) / max(1, end - start)
            c = mix(highlight, shadow, depth_factor * 0.6)
            c = darken(c, 0.9 + streak + n)

            # Vertical streak pattern — alternating light/dark columns
            if col % 2 == 0:
                c = lighten(c, 0.05)
            else:
                c = darken(c, 0.95)

            # Icicle tips are slightly pointed/darker
            if y == end:
                c = darken(c, 0.85)

            # Top mass is brighter
            if y <= 3:
                c = lighten(c, 0.15)

            grid[y][col] = c

    # Add some horizontal texture in the upper mass for fullness
    for y in range(1, 5):
        for x in range(4, 12):
            if grid[y][x][3] > 0:
                n = noise_val(x, y, 14)
                if n > 0.6:
                    grid[y][x] = lighten(grid[y][x], 0.1)

    return grid

# ===========================================================================
# 6. Devil's Cigar (closed) — dark brown tall cigar shape, wood grain
# ===========================================================================
def gen_devils_cigar():
    grid = new_grid()
    base_brown = (75, 50, 30, 255)
    dark_brown = (55, 35, 20, 255)
    light_brown = (95, 65, 40, 255)

    # Tall cigar/capsule shape: rows 1-14, cols 5-10
    # Rounded top and bottom
    cigar_widths = {
        1: (7, 9), 2: (6, 10), 3: (5, 11), 4: (5, 11),
        5: (5, 11), 6: (5, 11), 7: (5, 11), 8: (5, 11),
        9: (5, 11), 10: (5, 11), 11: (5, 11), 12: (5, 11),
        13: (6, 10), 14: (7, 9),
    }

    for y, (xl, xr) in cigar_widths.items():
        for x in range(xl, xr):
            # Vertical wood-grain texture
            grain = math.sin(y * 1.3 + noise_val(x, 0, 15) * 3) * 0.5 + 0.5
            c = mix(dark_brown, light_brown, grain)

            # Cylindrical shading — darker at edges
            mid = (xl + xr) / 2.0
            edge_d = abs(x - mid) / max(1, (xr - xl) / 2.0)
            c = darken(c, 1.0 - edge_d * 0.25)

            # Fine noise
            n = noise_val(x, y, 16) * 0.1
            c = darken(c, 0.9 + n)

            # Slight vertical line texture
            if (x + y) % 3 == 0:
                c = darken(c, 0.92)

            grid[y][x] = c

    # Top tip highlight
    if grid[1][7][3] > 0:
        grid[1][7] = lighten(grid[1][7], 0.15)
    if grid[1][8][3] > 0:
        grid[1][8] = lighten(grid[1][8], 0.15)

    return grid

# ===========================================================================
# 7. Devil's Cigar (open) — star shape split open, pale interior
# ===========================================================================
def gen_devils_cigar_open():
    grid = new_grid()
    outer_brown = (75, 50, 30, 255)
    inner_tan = (180, 160, 120, 255)
    dark_edge = (55, 35, 20, 255)

    cx, cy = 7.5, 7.5

    # Star-shaped rays — 4 to 5 rays splitting outward from center
    num_rays = 5
    ray_angles = [i * 2 * math.pi / num_rays - math.pi/2 for i in range(num_rays)]

    for y in range(T):
        for x in range(T):
            dx = x - cx
            dy = y - cy
            d = math.sqrt(dx*dx + dy*dy)

            if d > 7.5:
                continue

            angle = math.atan2(dy, dx)

            # Check if pixel is on a ray
            on_ray = False
            ray_interior = False
            for ra in ray_angles:
                # Angular distance to ray centerline
                ang_diff = abs(angle - ra)
                if ang_diff > math.pi:
                    ang_diff = 2 * math.pi - ang_diff

                # Ray width narrows with distance (wedge shape from center)
                ray_width = 0.45 - d * 0.02
                if ang_diff < ray_width and d > 1.0:
                    on_ray = True
                    # Inner part of ray vs outer
                    if ang_diff < ray_width * 0.6:
                        ray_interior = True
                    break

            # Central hub
            if d <= 2.5:
                n = noise_val(x, y, 20) * 0.1
                c = darken(inner_tan, 0.85 + n)
                grid[y][x] = c
            elif on_ray:
                if ray_interior:
                    # Pale tan interior visible where split open
                    n = noise_val(x, y, 21) * 0.1
                    c = darken(inner_tan, 0.9 + n)
                else:
                    # Dark brown outer surface of the ray
                    n = noise_val(x, y, 22) * 0.1
                    c = darken(outer_brown, 0.9 + n)
                grid[y][x] = c

    # Darken the tips of rays
    for y in range(T):
        for x in range(T):
            if grid[y][x][3] > 0:
                d = dist(x, y, cx, cy)
                if d > 5.5:
                    grid[y][x] = darken(grid[y][x], 0.8)

    return grid


# ===========================================================================
# Main
# ===========================================================================
def main():
    print("Generating mushroom textures...")

    textures = {
        "ghost_fungus.png": gen_ghost_fungus(),
        "indigo_milk_cap.png": gen_indigo_milk_cap(),
        "bleeding_tooth.png": gen_bleeding_tooth(),
        "amethyst_deceiver.png": gen_amethyst_deceiver(),
        "lions_mane.png": gen_lions_mane(),
        "devils_cigar.png": gen_devils_cigar(),
        "devils_cigar_open.png": gen_devils_cigar_open(),
    }

    for name, grid in textures.items():
        path = os.path.join(OUT, name)
        save_png(path, grid)

    print("Done! All 7 mushroom textures generated.")

if __name__ == "__main__":
    main()
