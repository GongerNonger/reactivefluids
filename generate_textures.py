"""
Reactive Fluids — texture generator.
Implements the art direction spec verbatim.

STILL: 4 sinusoidal wave layers + dual specular highlights + sheen gradient.
FLOW:  4 column groups, vertical curtain streaks, edge darkening.
Key distinction — Resin: alpha 242, interpolate=false, sharp gloss, thick streaks.
                  Hardener: alpha 210, interpolate=true, diffuse sheen, thin streaks.
"""
import zlib, struct, os, math, random

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
    print(f"  {path}")

def save_mcmeta(png_path, frametime, interpolate):
    val = 'true' if interpolate else 'false'
    with open(png_path + '.mcmeta', 'w') as f:
        f.write('{{"animation":{{"frametime":{},"interpolate":{}}}}}'.format(frametime, val))

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
T  = 16
NF = 32

def cl(v): return max(0, min(255, int(round(v))))
def lerp(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return tuple(cl(c1[i] + (c2[i]-c1[i])*t) for i in range(4))
def dk(c, f): return (cl(c[0]*f), cl(c[1]*f), cl(c[2]*f), c[3])
def lt(c, f): return (cl(c[0]*f), cl(c[1]*f), cl(c[2]*f), c[3])

# ---------------------------------------------------------------------------
# STILL texture — per art direction spec
# ---------------------------------------------------------------------------
def make_still(base_rgb, alpha, is_resin, tint_rgb=(0,0,0)):
    """
    Still surface texture — thick glossy pool.

    KEY FIX: all wave terms are PURE X or PURE Y, never combined (sin(x+y) creates
    45-degree diagonal stripes). Interference between independent X and Y waves at
    incommensurable frequencies produces a natural, non-directional swell pattern.

    Uses many waves at irrational-ish frequency ratios so no obvious grid repeats.
    """
    bR, bG, bB = base_rgb

    # Pure-X waves (only depend on px)
    X_WAVES = [
        # (spatial_k, time_omega, amplitude, phase)
        (0.38, 0.13, 16, 0.00),
        (0.61, 0.09, 11, 1.21),
        (1.00, 0.17,  7, 2.44),
        (1.53, 0.07,  5, 0.88),
        (2.24, 0.11,  3, 3.67),
    ]
    # Pure-Y waves (only depend on py)
    Y_WAVES = [
        (0.42, 0.11, 15, 1.57),
        (0.73, 0.08, 10, 0.34),
        (1.17, 0.15,  6, 2.09),
        (1.89, 0.06,  4, 4.18),
        (2.71, 0.12,  3, 1.05),
    ]

    rows = []
    for frame in range(NF):
        t = frame

        # Specular blobs drift independently in X and Y (Lissajous paths)
        hx  = 3.5 + 3.2 * math.sin(0.19*t)
        hy  = 3.0 + 2.1 * math.sin(0.13*t + 0.8)
        hx2 = 11.5 + 2.0 * math.sin(0.16*t + 3.1)
        hy2 = 10.5 + 2.8 * math.sin(0.10*t + 1.6)

        for py in range(T):
            row = []

            # Pre-compute Y-wave contribution for this row
            wy = sum(a * math.sin(k*py + om*t + ph) for k,om,a,ph in Y_WAVES)

            for px in range(T):
                # Pure-X contribution
                wx = sum(a * math.sin(k*px + om*t + ph) for k,om,a,ph in X_WAVES)

                # Sum and clamp — never combined x+y in a single term
                wave = max(-42.0, min(42.0, wx + wy))

                # Specular
                d1 = (px-hx)**2 + (py-hy)**2
                d2 = (px-hx2)**2 + (py-hy2)**2
                spec_soft = 38 * math.exp(-d2 / 5.0)
                spec = (68 * math.exp(-d1 / 2.2) + spec_soft) if is_resin else spec_soft * 0.75

                # Sheen: pure horizontal gradient (left bright, right dim) — no diagonal
                sheen_base = 10 * (1.0 - px / (T - 1))
                sheen = sheen_base if is_resin else sheen_base * 0.55

                R = cl(bR + wave + spec + sheen + tint_rgb[0])
                G = cl(bG + wave + spec + sheen + tint_rgb[1])
                B = cl(bB + wave + spec + sheen + tint_rgb[2])
                row.append((R, G, B, alpha))
            rows.append(row)
    return rows

# ---------------------------------------------------------------------------
# FLOW texture — rich vertical ribbon system
#
# Real resin on a vertical surface forms thick, slightly translucent ribbons
# with these properties:
#   - Each ribbon column has a unique width (1-3px), brightness, and scroll speed
#   - Ribbons have internal depth: brighter center, darker edges
#   - Multiple overlapping "drip frequency" waves per column
#   - Overall pattern scrolls DOWNWARD (subtract time from y coordinate)
#   - Occasional thick slow blobs mixed with thin fast runlets
# ---------------------------------------------------------------------------
def make_flow(base_rgb, alpha, is_resin):
    """
    Flow texture — vertical honey/resin curtain.

    Critical design rules:
    - Movement is ONLY in the -Y direction (subtract time × speed from py)
    - Wave functions are ONLY f(py') — no px term inside the sin, so there is
      zero horizontal directionality. Horizontal variation comes from per-column
      static brightness offsets only.
    - Each column has 3 drip waves at incommensurable periods so no obvious repeat.
    - Column brightness profiles are pre-baked noise (constant over time) to give
      the thick-curtain look without diagonal stripes.
    """
    bR, bG, bB = base_rgb

    # Seed from color so each fluid looks distinct but is deterministic
    rng = random.Random((bR * 1000 + bG * 100 + bB + (1 if is_resin else 0)) & 0xFFFFFF)

    spd_mul = 1.0 if is_resin else 1.6

    # Per-column static profile (computed once, no time dependence)
    # b_off: static brightness bias for this column (-25..+25)
    # speeds/amplitudes/periods/phases: 3 drip waves, all purely vertical
    col_profile = []
    for _ in range(T):
        b_off = rng.uniform(-20, 20)
        waves = []
        for w in range(3):
            spd  = rng.uniform(0.22, 0.60) * spd_mul
            amp  = rng.uniform(18, 38) if is_resin else rng.uniform(12, 26)
            # Incommensurable periods (no common factor) → pattern never locks into grid
            period = rng.choice([6.3, 8.1, 10.7, 13.3, 5.4, 7.8, 11.2])
            ph   = rng.uniform(0, math.tau)
            waves.append((spd, amp, period, ph))
        col_profile.append((b_off, waves))

    # Pre-bake horizontal brightness envelope from static noise (no time, no y)
    # Gives columns their distinct visual weight without creating diagonal bias
    H_WAVES = [
        (0.37, 14, 0.00),
        (0.89, 10, 1.23),
        (1.57,  7, 2.89),
        (2.41,  5, 0.71),
    ]
    h_brightness = []
    for px in range(T):
        hb = sum(a * math.sin(k * px + ph) for k,a,ph in H_WAVES)
        # Edge darkening
        edge = 0
        if px <= 1:    edge = -18 + px * 6
        elif px >= 14: edge = -18 + (T-1-px) * 6
        h_brightness.append(hb + edge)

    rows = []
    for frame in range(NF):
        t = frame
        for py in range(T):
            row = []
            for px in range(T):
                b_off, waves = col_profile[px]

                # All drip waves: ONLY function of scrolled_y, never px
                drip = 0.0
                for spd, amp, period, ph in waves:
                    scrolled_y = (py - spd * t) % T   # downward motion
                    drip += amp * math.sin(math.tau * scrolled_y / period + ph)

                brightness = b_off + drip + h_brightness[px]

                R = cl(bR + brightness)
                G = cl(bG + brightness)
                B = cl(bB + brightness)
                row.append((R, G, B, alpha))
            rows.append(row)
    return rows

# ---------------------------------------------------------------------------
# EPOXY block textures (static, seamless)
# ---------------------------------------------------------------------------
def make_waves(seed, n=24):
    rng = random.Random(seed)
    return [(rng.randint(1,4), rng.randint(1,4),
             rng.uniform(0.05,0.15), rng.uniform(0, math.tau)) for _ in range(n)]

def seamless(px, py, waves):
    w = math.tau / T
    return sum(a * math.sin(w*kx*px + w*ky*py + ph) for kx,ky,a,ph in waves)

def make_epoxy_transparent(rgba, seed, alpha=90):
    base = (rgba[0], rgba[1], rgba[2], alpha)
    dark   = dk(base, 0.50)
    mid    = base
    bright = (min(255,rgba[0]+70), min(255,rgba[1]+70), min(255,rgba[2]+55), min(200,alpha+40))
    waves  = make_waves(seed)
    pixels = []
    for py in range(T):
        row = []
        for px in range(T):
            v = seamless(px, py, waves)
            n = max(0.0, min(1.0, (v+1.0)/2.0))
            c = lerp(dark, mid, n*0.7+0.2)
            gx, gy = px/(T-1), py/(T-1)
            gloss = max(0.0, 0.55 - (gx*1.4 + gy*1.4))
            if gloss > 0: c = lerp(c, bright, gloss*0.85)
            if px==0 or py==0 or px==T-1 or py==T-1: c = dk(c, 0.68)
            row.append(c)
        pixels.append(row)
    rng = random.Random(seed+300)
    for _ in range(8):
        sx, sy = rng.randint(2,T-3), rng.randint(2,T-3)
        pixels[sy][sx] = (min(255,bright[0]), min(255,bright[1]), min(255,bright[2]), min(180,alpha+50))
    return pixels

def make_epoxy_glowing(rgba, seed):
    avg = sum(rgba[:3])/3.0
    def boost(c): return cl(avg + (c-avg)*1.6)
    g = (boost(rgba[0]), boost(rgba[1]), boost(rgba[2]), 110)
    pixels = make_epoxy_transparent(g, seed+100, alpha=110)
    cx, cy = T/2.0, T/2.0
    mx = math.sqrt(cx**2+cy**2)
    gb = (min(255,g[0]+60), min(255,g[1]+60), min(255,g[2]+50), 150)
    for py in range(T):
        for px in range(T):
            dist = math.sqrt((px-cx)**2+(py-cy)**2)
            t = max(0.0, 1.0-dist/mx)**1.6
            if t > 0.04:
                pixels[py][px] = lerp(pixels[py][px], gb, t*0.55)
    return pixels

def make_epoxy_opaque(rgba, seed):
    base = (rgba[0], rgba[1], rgba[2], 255)
    dark   = dk(base, 0.45)
    mid    = base
    bright = lt(base, 1.28)
    waves  = make_waves(seed+400, n=26)
    pixels = []
    for py in range(T):
        row = []
        for px in range(T):
            v = seamless(px, py, waves)
            n = max(0.0, min(1.0, (v+1.0)/2.0))
            c = lerp(dark, mid, n*0.72+0.22)
            gx, gy = px/(T-1), py/(T-1)
            gloss = max(0.0, 0.32-(gx*1.1+gy*1.1))
            if gloss > 0: c = lerp(c, bright, gloss*0.55)
            if px==0 or py==0 or px==T-1 or py==T-1: c = dk(c, 0.62)
            row.append(c)
        pixels.append(row)
    return pixels

# ---------------------------------------------------------------------------
# BUCKET sprite — art team spec implementation
# Proper per-fluid outline, highlight, primary fill, shadow gradient
# ---------------------------------------------------------------------------
BUCKET = [
    [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
    [0,0,0,0,1,1,0,0,0,0,1,1,0,0,0,0],
    [0,0,0,1,0,0,1,0,0,1,0,0,1,0,0,0],
    [0,0,0,1,0,0,0,1,1,0,0,0,1,0,0,0],
    [0,0,1,1,1,1,1,1,1,1,1,1,1,1,0,0],
    [0,0,1,2,2,2,2,2,2,2,2,2,2,1,0,0],  # row 5  — highlight
    [0,0,1,2,2,2,2,2,2,2,2,2,2,1,0,0],  # row 6  — highlight
    [0,0,1,2,2,2,2,2,2,2,2,2,2,1,0,0],  # row 7  — primary
    [0,0,1,2,2,2,2,2,2,2,2,2,2,1,0,0],  # row 8  — primary
    [0,0,1,2,2,2,2,2,2,2,2,2,2,1,0,0],  # row 9  — primary
    [0,0,1,2,2,2,2,2,2,2,2,2,2,1,0,0],  # row 10 — shadow
    [0,0,1,2,2,2,2,2,2,2,2,2,2,1,0,0],  # row 11 — shadow
    [0,0,0,1,2,2,2,2,2,2,2,2,1,0,0,0],
    [0,0,0,0,1,2,2,2,2,2,2,1,0,0,0,0],
    [0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0],
    [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
]

# Art-team specified colors per bucket
BUCKET_PALETTES = {
    'amber_resin':         {'outline':(61,40,23),    'primary':(212,165,71),  'highlight':(232,197,113), 'shadow':(184,136,74)},
    'amber_hardener':      {'outline':(61,40,23),    'primary':(244,216,155), 'highlight':(255,250,205), 'shadow':(212,165,71)},
    'amber_glowing_resin': {'outline':(61,40,23),    'primary':(230,160,40),  'highlight':(255,210,100), 'shadow':(190,120,10)},
    'cobalt_resin':        {'outline':(26,26,77),    'primary':(59,95,168),   'highlight':(107,143,212), 'shadow':(30,58,122)},
    'cobalt_hardener':     {'outline':(26,26,77),    'primary':(107,143,212), 'highlight':(164,195,255), 'shadow':(59,95,168)},
    'cobalt_glowing_resin':{'outline':(26,26,77),    'primary':(40,100,220),  'highlight':(120,180,255), 'shadow':(15,50,170)},
    'jade_resin':          {'outline':(45,74,58),    'primary':(82,165,114),  'highlight':(130,212,168), 'shadow':(58,122,90)},
    'jade_hardener':       {'outline':(45,74,58),    'primary':(130,212,168), 'highlight':(184,232,212), 'shadow':(82,165,114)},
    'jade_glowing_resin':  {'outline':(45,74,58),    'primary':(30,175,80),   'highlight':(100,240,150), 'shadow':(10,120,50)},
    'hydrogen_peroxide':   {'outline':(120,140,160), 'primary':(200,225,255), 'highlight':(230,245,255), 'shadow':(160,190,220)},
    'potassium_iodide':    {'outline':(80,55,20),    'primary':(170,115,35),  'highlight':(210,160,70),  'shadow':(120,80,20)},
    'acid':                {'outline':(20,80,10),    'primary':(50,220,20),   'highlight':(120,255,80),  'shadow':(30,150,10)},
    'plankton':            {'outline':(4,8,20),     'primary':(8,18,38),     'highlight':(30,180,200), 'shadow':(3,10,25)},
    'liquid_nitrogen':     {'outline':(80,110,140), 'primary':(170,215,255), 'highlight':(210,240,255), 'shadow':(130,180,230)},
    'greek_fire':          {'outline':(5,60,55),    'primary':(15,150,130),  'highlight':(40,200,170),  'shadow':(8,100,90)},
    'ferrofluid':          {'outline':(10,10,15),   'primary':(25,25,35),    'highlight':(55,55,70),    'shadow':(15,15,22)},
    'superfluid':          {'outline':(100,130,155),'primary':(200,230,255), 'highlight':(230,248,255), 'shadow':(170,205,240)},
    'mycelium_slurry':     {'outline':(40,22,55),   'primary':(95,55,125),   'highlight':(140,90,170),  'shadow':(65,35,90)},
}

def make_bucket(name):
    """
    Vanilla-style 16x16 bucket sprite using the BUCKET shape template.
    Proper two-legged handle arch + tapered body matching vanilla proportions.
    Per-fluid outline/highlight/primary/shadow colors from BUCKET_PALETTES.
    """
    pal  = BUCKET_PALETTES[name]
    OUT  = pal['outline']   + (255,)
    FH   = pal['highlight'] + (255,)
    F    = pal['primary']   + (255,)
    FS   = pal['shadow']    + (255,)
    X    = (0, 0, 0, 0)

    rows = []
    for row_tmpl in BUCKET:
        fluid_xs = [i for i, v in enumerate(row_tmpl) if v == 2]
        fmin  = min(fluid_xs) if fluid_xs else 0
        fspan = max(1, (max(fluid_xs) if fluid_xs else 0) - fmin)
        prow  = []
        for cx, v in enumerate(row_tmpl):
            if v == 0:
                prow.append(X)
            elif v == 1:
                prow.append(OUT)
            else:  # v == 2 — fluid fill with left→right highlight→shadow gradient
                t = (cx - fmin) / fspan
                if t < 0.25:
                    prow.append(FH)
                elif t > 0.75:
                    prow.append(FS)
                else:
                    prow.append(F)
        rows.append(prow)
    return rows

# ---------------------------------------------------------------------------
# Fluid definitions — exact base colors from art direction spec
# (name, base_rgb, alpha, is_resin, still_ft, flow_ft, tint_rgb)
# ---------------------------------------------------------------------------
FLUIDS = [
    # name                          base_rgb          alpha  resin  still_ft flow_ft  tint
    # Resins: alpha 80 (clearly transparent — see the reaction beneath)
    # Glowing resins: alpha 100 (extra presence for the glow)
    # NOTE: hardener fluids removed — hardener is now a throwable item (HardenerItem)
    ("amber_resin",              (162, 88,  12),  80, True,   4, 3, ( 8, 0, 0)),
    ("cobalt_resin",             ( 28, 52, 168),  80, True,   4, 3, ( 0, 0,10)),
    ("jade_resin",               ( 18,105,  48),  80, True,   4, 3, ( 0, 5,-3)),
    # Glowing resins
    ("amber_glowing_resin",      (220,130,  10), 100, True,   4, 3, (12, 2, 0)),
    ("cobalt_glowing_resin",     ( 20, 80, 220), 100, True,   4, 3, ( 0, 2,15)),
    ("jade_glowing_resin",       ( 10,160,  55), 100, True,   4, 3, ( 0, 8,-2)),
    # Elephant's Toothpaste fluids
    ("hydrogen_peroxide",        (160,200, 255),  70, False,  3, 2, ( 0, 0, 8)),
    ("potassium_iodide",         (170,115,  35),  65, True,   4, 3, ( 5, 0,-3)),
    # Acid
    ("acid",                     ( 50,220,  20),  75, False,  3, 2, ( 0, 8,-5)),
    # Liquid Nitrogen — pale icy blue, fast, watery
    ("liquid_nitrogen",          (160,210, 255),  65, False,  3, 2, ( 0, 5,10)),
    # Greek Fire — blue-green, slow oily
    ("greek_fire",               ( 10,140, 120),  85, True,   4, 3, ( 0,10, 5)),
    # Ferrofluid — dark metallic black
    ("ferrofluid",               ( 20, 20, 30),   90, True,   4, 3, ( 2, 0, 5)),
    # Superfluid — very pale blue, almost clear
    ("superfluid",               (190,225, 255),  45, False,  2, 1, ( 0, 3, 8)),
    # Mycelium Slurry — deep purple/brown fungal
    ("mycelium_slurry",          ( 90, 50,120),   80, True,   4, 3, ( 5,-3, 8)),
]

EPOXY = [
    ("amber", (162, 88, 12, 255), 2001),
    ("cobalt", ( 28, 52,168, 255), 2002),
    ("jade",  ( 18,105, 48, 255), 2003),
]

BUCKET_COLORS = {
    "amber_resin":          (162, 88,  12),
    "amber_hardener":       (210,168,  62),
    "amber_glowing_resin":  (220,130,  10),
    "cobalt_resin":         ( 28, 52, 168),
    "cobalt_hardener":      ( 42,128, 225),
    "cobalt_glowing_resin": ( 20, 80, 220),
    "jade_resin":           ( 18,105,  48),
    "jade_hardener":        ( 78,185,  72),
    "jade_glowing_resin":   ( 10,160,  55),
    "hydrogen_peroxide":    (200,225, 255),
    "potassium_iodide":     (170,115,  35),
    "acid":                 ( 50,220,  20),
    "plankton":             (  8, 18,  38),
    "liquid_nitrogen":      (170,215, 255),
    "greek_fire":           ( 15,150, 130),
    "ferrofluid":           ( 25, 25,  35),
    "superfluid":           (200,230, 255),
    "mycelium_slurry":      ( 95, 55, 125),
}

def make_foam(seed=5001):
    """White/cream bubbly foam texture — 16x16 static."""
    rng = random.Random(seed)
    base = (245, 242, 235, 255)
    pixels = []
    for py in range(T):
        row = []
        for px in range(T):
            # Gentle noise for bubbly look
            noise = rng.randint(-12, 12)
            r = cl(base[0] + noise)
            g = cl(base[1] + noise - 2)
            b = cl(base[2] + noise - 5)
            # Edge darkening
            if px == 0 or py == 0 or px == T-1 or py == T-1:
                r, g, b = cl(r*0.82), cl(g*0.82), cl(b*0.82)
            row.append((r, g, b, 255))
        pixels.append(row)
    # Scatter bubble highlights (bright spots)
    for _ in range(18):
        bx, by = rng.randint(1, T-2), rng.randint(1, T-2)
        pixels[by][bx] = (255, 255, 252, 255)
    # Scatter bubble shadows (dark spots)
    for _ in range(10):
        bx, by = rng.randint(1, T-2), rng.randint(1, T-2)
        pixels[by][bx] = (215, 210, 200, 255)
    return pixels

# ---------------------------------------------------------------------------
# BIOLUMINESCENT PLANKTON — special animated texture
# Deep dark ocean with individual plankton organisms that glow and fade
# asynchronously. Each plankton pixel has its own lifecycle:
#   - dormant (near invisible) → igniting → peak glow → fading → dormant
# Multiple glow colors: electric cyan, seafoam green, pale blue, warm teal
# ---------------------------------------------------------------------------
def make_plankton_still():
    """
    Still plankton texture — 16x16, 64 frames (longer cycle for rich animation).
    Dark indigo ocean base with ~30 plankton organisms scattered across the
    surface, each with independent glow phase, color, and lifetime.
    """
    NUM_FRAMES = 64  # longer cycle for more variation
    rng = random.Random(7777)

    # Deep ocean base palette — very dark, shifts subtly per frame
    DEEP    = ( 5, 12, 28)   # darkest ocean
    MID     = ( 8, 18, 38)   # mid-deep
    SHIMMER = (12, 24, 48)   # subtle movement highlight

    # Plankton glow palette — multiple ethereal colors
    GLOW_COLORS = [
        ( 20, 220, 255),  # electric cyan
        ( 40, 255, 210),  # seafoam green
        ( 80, 200, 255),  # pale sky blue
        ( 10, 180, 200),  # deep teal
        ( 60, 255, 180),  # mint
        (100, 240, 255),  # ice blue
        (  0, 160, 220),  # ocean cyan
        ( 30, 255, 140),  # bright sea green
    ]

    # Generate plankton organisms with individual properties
    class Plankton:
        def __init__(self, rng):
            self.x = rng.randint(0, T-1)
            self.y = rng.randint(0, T-1)
            self.color = rng.choice(GLOW_COLORS)
            self.phase = rng.uniform(0, math.tau)     # start time offset
            self.speed = rng.uniform(0.06, 0.18)       # glow cycle speed
            self.max_brightness = rng.uniform(0.5, 1.0)  # peak intensity
            self.duty = rng.uniform(0.25, 0.55)        # fraction of cycle spent glowing
            self.size = rng.choice([1, 1, 1, 2])        # 1=single pixel, 2=2x1 or 1x2

    # Create 30-40 plankton organisms
    plankton = [Plankton(rng) for _ in range(35)]

    # Gentle water movement waves for the base
    BASE_WAVES_X = [(0.35, 0.04, 3.5, rng.uniform(0, math.tau)) for _ in range(3)]
    BASE_WAVES_Y = [(0.45, 0.03, 3.0, rng.uniform(0, math.tau)) for _ in range(3)]

    alpha = 180  # fairly opaque — deep ocean water

    rows = []
    for frame in range(NUM_FRAMES):
        t = frame

        for py in range(T):
            row = []
            for px in range(T):
                # Base ocean color with subtle wave modulation
                wave_x = sum(a * math.sin(k * px + o * t + ph)
                             for k, o, a, ph in BASE_WAVES_X)
                wave_y = sum(a * math.sin(k * py + o * t + ph)
                             for k, o, a, ph in BASE_WAVES_Y)
                wave = (wave_x + wave_y) / 2.0

                # Blend between deep and mid based on wave
                wn = max(0.0, min(1.0, (wave + 6.0) / 12.0))
                bR = cl(DEEP[0] + (MID[0] - DEEP[0]) * wn)
                bG = cl(DEEP[1] + (MID[1] - DEEP[1]) * wn)
                bB = cl(DEEP[2] + (MID[2] - DEEP[2]) * wn)

                # Subtle shimmer on wave peaks
                if wn > 0.7:
                    sm = (wn - 0.7) / 0.3
                    bR = cl(bR + (SHIMMER[0] - bR) * sm * 0.4)
                    bG = cl(bG + (SHIMMER[1] - bG) * sm * 0.4)
                    bB = cl(bB + (SHIMMER[2] - bB) * sm * 0.4)

                # Edge darkening for seamless tiling depth
                ex = min(px, T-1-px) / (T/2.0)
                ey = min(py, T-1-py) / (T/2.0)
                edge = min(1.0, min(ex, ey) * 3.0)
                bR = cl(bR * (0.7 + 0.3 * edge))
                bG = cl(bG * (0.7 + 0.3 * edge))
                bB = cl(bB * (0.7 + 0.3 * edge))

                row.append((bR, bG, bB, alpha))
            rows.append(row)

        # Overlay plankton glows for this frame
        frame_base = frame * T  # row offset in the stacked image
        for p in plankton:
            # Glow envelope: smooth pulse using sin, with configurable duty cycle
            cycle = math.sin(p.phase + p.speed * t)
            # Map [-1,1] → glow intensity, only positive part glows
            glow = max(0.0, (cycle - (1.0 - 2.0 * p.duty)) / (2.0 * p.duty))
            glow = glow ** 0.6  # soften the curve for natural fade-in/out
            glow *= p.max_brightness

            if glow < 0.02:
                continue

            # Core pixel
            fy = frame_base + p.y
            base_pixel = rows[fy][p.x]
            gR = cl(base_pixel[0] + p.color[0] * glow)
            gG = cl(base_pixel[1] + p.color[1] * glow)
            gB = cl(base_pixel[2] + p.color[2] * glow)
            # Brighter alpha when glowing
            gA = cl(alpha + 40 * glow)
            rows[fy][p.x] = (gR, gG, gB, gA)

            # Soft halo on adjacent pixels (dimmer)
            halo = glow * 0.3
            if halo > 0.03:
                for dx, dy in [(-1,0),(1,0),(0,-1),(0,1)]:
                    hx, hy = p.x + dx, p.y + dy
                    if 0 <= hx < T and 0 <= hy < T:
                        hp = rows[frame_base + hy][hx]
                        hR = cl(hp[0] + p.color[0] * halo)
                        hG = cl(hp[1] + p.color[1] * halo)
                        hB = cl(hp[2] + p.color[2] * halo)
                        hA = cl(hp[3] + 20 * halo)
                        rows[frame_base + hy][hx] = (hR, hG, hB, hA)

            # Extended size for larger organisms
            if p.size == 2:
                # Add a second pixel in a random adjacent direction
                sdx, sdy = rng.choice([(1,0),(0,1)])
                sx, sy = p.x + sdx, p.y + sdy
                if 0 <= sx < T and 0 <= sy < T:
                    sp = rows[frame_base + sy][sx]
                    sR = cl(sp[0] + p.color[0] * glow * 0.7)
                    sG = cl(sp[1] + p.color[1] * glow * 0.7)
                    sB = cl(sp[2] + p.color[2] * glow * 0.7)
                    sA = cl(sp[3] + 30 * glow)
                    rows[frame_base + sy][sx] = (sR, sG, sB, sA)

    return rows

def make_plankton_flow():
    """
    Flow plankton texture — similar aesthetic but with vertical drift.
    Individual plankton trail downward as the fluid moves.
    """
    NUM_FRAMES = 64
    rng = random.Random(8888)

    DEEP = ( 5, 12, 28)
    MID  = ( 8, 18, 38)

    GLOW_COLORS = [
        ( 20, 220, 255), ( 40, 255, 210), ( 80, 200, 255),
        ( 10, 180, 200), ( 60, 255, 180), (100, 240, 255),
    ]

    class FlowPlankton:
        def __init__(self, rng):
            self.x = rng.randint(0, T-1)
            self.base_y = rng.uniform(0, T)
            self.color = rng.choice(GLOW_COLORS)
            self.phase = rng.uniform(0, math.tau)
            self.speed = rng.uniform(0.08, 0.2)
            self.max_brightness = rng.uniform(0.4, 0.9)
            self.drift = rng.uniform(0.2, 0.5)  # downward speed

    plankton = [FlowPlankton(rng) for _ in range(28)]
    alpha = 170

    rows = []
    for frame in range(NUM_FRAMES):
        t = frame
        for py in range(T):
            row = []
            for px in range(T):
                # Vertical ribbon base (like flow textures)
                col_bright = math.sin(px * 0.9 + 1.2) * 3.0
                drift_off = math.sin(py * 0.4 - t * 0.15) * 2.0
                val = col_bright + drift_off
                wn = max(0.0, min(1.0, (val + 5.0) / 10.0))
                bR = cl(DEEP[0] + (MID[0] - DEEP[0]) * wn)
                bG = cl(DEEP[1] + (MID[1] - DEEP[1]) * wn)
                bB = cl(DEEP[2] + (MID[2] - DEEP[2]) * wn)
                row.append((bR, bG, bB, alpha))
            rows.append(row)

        frame_base = frame * T
        for p in plankton:
            # Plankton drifts downward each frame
            current_y = (p.base_y + t * p.drift) % T
            iy = int(current_y)

            cycle = math.sin(p.phase + p.speed * t)
            glow = max(0.0, cycle)
            glow = glow ** 0.7 * p.max_brightness
            if glow < 0.03:
                continue

            fy = frame_base + iy
            bp = rows[fy][p.x]
            gR = cl(bp[0] + p.color[0] * glow)
            gG = cl(bp[1] + p.color[1] * glow)
            gB = cl(bp[2] + p.color[2] * glow)
            gA = cl(alpha + 35 * glow)
            rows[fy][p.x] = (gR, gG, gB, gA)

            # Small trailing glow below (flow trail)
            for trail in range(1, 3):
                ty = (iy + trail) % T
                tp = rows[frame_base + ty][p.x]
                tfade = glow * (0.4 / trail)
                tR = cl(tp[0] + p.color[0] * tfade)
                tG = cl(tp[1] + p.color[1] * tfade)
                tB = cl(tp[2] + p.color[2] * tfade)
                rows[frame_base + ty][p.x] = (tR, tG, tB, cl(alpha + 15 * tfade))

    return rows

# ---------------------------------------------------------------------------
# MUSHROOM block textures — static 16x16 for custom mushroom varieties
# ---------------------------------------------------------------------------
def make_mushroom(base_rgb, pattern_fn, seed):
    """Generic mushroom texture with a pattern overlay function."""
    rng = random.Random(seed)
    bR, bG, bB = base_rgb
    pixels = []
    for py in range(T):
        row = []
        for px in range(T):
            # Base color with slight noise
            noise = rng.randint(-8, 8)
            r, g, b = cl(bR + noise), cl(bG + noise), cl(bB + noise)
            # Apply pattern overlay
            r, g, b = pattern_fn(px, py, r, g, b, rng)
            # Edge darkening
            if px == 0 or py == 0 or px == T-1 or py == T-1:
                r, g, b = cl(r * 0.7), cl(g * 0.7), cl(b * 0.7)
            row.append((r, g, b, 255))
        pixels.append(row)
    return pixels

def make_ghost_fungus():
    """Cream/white with blue-green bioluminescent gill lines."""
    def pattern(px, py, r, g, b, rng):
        # Gill lines on lower half
        if py > 8 and px % 3 == 1:
            glow = 0.5 + 0.3 * math.sin(py * 0.8)
            r = cl(r * 0.6 + 30 * glow)
            g = cl(g * 0.6 + 180 * glow)
            b = cl(b * 0.6 + 160 * glow)
        return r, g, b
    return make_mushroom((220, 215, 200), pattern, 6001)

def make_indigo_milk_cap():
    """Deep blue with concentric ring pattern."""
    def pattern(px, py, r, g, b, rng):
        cx, cy = 7.5, 7.5
        dist = math.sqrt((px - cx)**2 + (py - cy)**2)
        ring = math.sin(dist * 1.8) * 0.4
        r = cl(r + ring * -20)
        g = cl(g + ring * -10)
        b = cl(b + ring * 30)
        return r, g, b
    return make_mushroom((30, 50, 160), pattern, 6002)

def make_bleeding_tooth():
    """White base with scattered red blood droplets."""
    rng_drops = random.Random(6003)
    drops = [(rng_drops.randint(2, 13), rng_drops.randint(2, 13)) for _ in range(12)]
    def pattern(px, py, r, g, b, rng):
        for dx, dy in drops:
            dist = abs(px - dx) + abs(py - dy)
            if dist == 0:
                return 180, 10, 10
            elif dist == 1:
                return cl(r * 0.7 + 60), cl(g * 0.5), cl(b * 0.5)
        return r, g, b
    return make_mushroom((230, 225, 218), pattern, 6003)

def make_amethyst_deceiver():
    """Deep violet-purple with gradient from center."""
    def pattern(px, py, r, g, b, rng):
        cx, cy = 7.5, 7.5
        dist = math.sqrt((px - cx)**2 + (py - cy)**2) / 10.0
        # Lighter in center
        bright = max(0, 1.0 - dist) * 0.3
        r = cl(r + bright * 40)
        g = cl(g + bright * 15)
        b = cl(b + bright * 50)
        return r, g, b
    return make_mushroom((120, 45, 170), pattern, 6004)

def make_lions_mane():
    """White cascading tendrils — vertical streaks."""
    def pattern(px, py, r, g, b, rng):
        # Vertical tendril streaks
        streak = math.sin(px * 2.3 + py * 0.3) * 15
        drip = math.sin(py * 1.5 + px * 0.2) * 10
        val = streak + drip
        r = cl(r + val)
        g = cl(g + val)
        b = cl(b + val - 3)
        return r, g, b
    return make_mushroom((240, 238, 232), pattern, 6005)

def make_devils_cigar_closed():
    """Dark brown cigar shape — closed state."""
    def pattern(px, py, r, g, b, rng):
        # Vertical wood-grain texture
        grain = math.sin(py * 1.2 + px * 0.15) * 12
        r = cl(r + grain)
        g = cl(g + grain * 0.7)
        b = cl(b + grain * 0.4)
        return r, g, b
    return make_mushroom((75, 50, 30), pattern, 6006)

def make_devils_cigar_open():
    """Star-burst pattern — open state."""
    def pattern(px, py, r, g, b, rng):
        cx, cy = 7.5, 7.5
        angle = math.atan2(py - cy, px - cx)
        dist = math.sqrt((px - cx)**2 + (py - cy)**2)
        # Star rays (6 points)
        ray = abs(math.sin(angle * 3)) * max(0, 1.0 - dist / 8.0)
        if ray > 0.3:
            # Pale tan interior
            r = cl(180 + ray * 40)
            g = cl(160 + ray * 30)
            b = cl(120 + ray * 20)
        return r, g, b
    return make_mushroom((75, 50, 30), pattern, 6007)

MUSHROOM_TEXTURES = {
    "ghost_fungus":         make_ghost_fungus,
    "indigo_milk_cap":      make_indigo_milk_cap,
    "bleeding_tooth":       make_bleeding_tooth,
    "amethyst_deceiver":    make_amethyst_deceiver,
    "lions_mane":           make_lions_mane,
    "devils_cigar":         make_devils_cigar_closed,
    "devils_cigar_open":    make_devils_cigar_open,
}

BLK = os.path.join("src","main","resources","assets","reactivefluids","textures","block")
ITM = os.path.join("src","main","resources","assets","reactivefluids","textures","item")

def main():
    print("=== Fluid textures ===")
    for name, base_rgb, alpha, is_resin, sft, fft, tint in FLUIDS:
        sp = os.path.join(BLK, f"{name}_still.png")
        fp = os.path.join(BLK, f"{name}_flow.png")
        save_png(sp, make_still(base_rgb, alpha, is_resin, tint))
        # Resin: no interpolation (too viscous); hardener: interpolate
        save_mcmeta(sp, frametime=sft, interpolate=not is_resin)
        save_png(fp, make_flow(base_rgb, alpha, is_resin))
        save_mcmeta(fp, frametime=fft, interpolate=not is_resin)

    print("=== Epoxy block textures ===")
    for color, rgba, seed in EPOXY:
        save_png(os.path.join(BLK, f"{color}_epoxy_block.png"),   make_epoxy_transparent(rgba, seed))
        save_png(os.path.join(BLK, f"{color}_epoxy_opaque.png"),  make_epoxy_opaque(rgba, seed))
        save_png(os.path.join(BLK, f"{color}_epoxy_glowing.png"), make_epoxy_glowing(rgba, seed))

    print("=== Bioluminescent Plankton textures ===")
    sp = os.path.join(BLK, "plankton_still.png")
    fp = os.path.join(BLK, "plankton_flow.png")
    save_png(sp, make_plankton_still())
    save_mcmeta(sp, frametime=3, interpolate=True)
    save_png(fp, make_plankton_flow())
    save_mcmeta(fp, frametime=2, interpolate=True)

    print("=== Foam block texture ===")
    save_png(os.path.join(BLK, "foam_block.png"), make_foam())

    print("=== Mushroom block textures ===")
    for name, gen_fn in MUSHROOM_TEXTURES.items():
        save_png(os.path.join(BLK, f"{name}.png"), gen_fn())

    print("=== Bucket item textures ===")
    for name in BUCKET_PALETTES:
        save_png(os.path.join(ITM, f"{name}_bucket.png"), make_bucket(name))

    print("\nDone.")

if __name__ == "__main__":
    main()
