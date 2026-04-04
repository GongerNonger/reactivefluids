"""
Reactive Fluids — texture generator.
Implements the art direction spec verbatim.

STILL: 4 sinusoidal wave layers + dual specular highlights + sheen gradient.
FLOW:  4 column groups, vertical curtain streaks, edge darkening.
Key distinction — Resin: alpha 242, interpolate=false, sharp gloss, thick streaks.
                  Hardener: alpha 210, interpolate=true, diffuse sheen, thin streaks.
"""
import zlib, struct, os, math, random, zipfile, io, tempfile

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
    'indicator':           {'outline':(60,80,60),   'primary':(180,210,180), 'highlight':(220,240,220), 'shadow':(140,170,140)},
    'crystal_solution':    {'outline':(40,60,100),  'primary':(140,200,255), 'highlight':(190,230,255), 'shadow':(90,150,210)},
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
    # Rainbow Indicator — neutral green base, tint-shifted by pH
    ("indicator",                (200,220, 200),  80, False,  3, 2, ( 0, 3,-2)),
    # Crystal Solution — clear icy blue
    ("crystal_solution",         (140,200, 255),  75, False,  3, 2, ( 0, 3, 8)),
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
    "indicator":            (200,220, 200),
    "crystal_solution":     (140,200, 255),
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
def make_crystal_skeleton_overlay():
    """
    64x64 overlay texture for the crystallized skeleton.
    Matches the skeleton model UV layout. Renders as an emissive EyesLayer
    (RenderType.eyes(), full-brightness additive blending).

    Design: sparse crystal cluster formations on specific body parts —
    shoulders, crown of head, chest, knees. Most of the texture is fully
    transparent so the base skeleton shows through. Crystal tips are pale
    blue-white; bases are deeper blue.
    """
    rng = random.Random(8001)

    # Start with a fully transparent 64x64 texture
    rows = [[(0, 0, 0, 0) for _ in range(64)] for _ in range(64)]

    # --- Crystal cluster definitions ---
    # Each cluster is (center_x, center_y, radius, intensity, num_shards)
    # intensity 0.0-1.0 controls overall brightness/alpha
    # Clusters are placed at anatomically meaningful UV positions.

    clusters = []

    # == HEAD (UV 0,0 to 32,16) ==
    # Crown — crystals growing upward from top of skull
    # Head top face is at UV (8,0)-(16,8)
    clusters.append((11, 1, 3, 1.0, 6))   # center crown
    clusters.append((14, 2, 2, 0.7, 4))   # right crown
    clusters.append((9, 3, 2, 0.6, 3))    # left crown
    # Forehead area — head front face is at UV (8,8)-(16,16)
    clusters.append((12, 9, 2, 0.5, 3))   # small forehead cluster

    # == BODY / TORSO (UV 16,16 to 40,32) ==
    # Chest — center of the front face of the torso
    # Torso front face: UV (20,20)-(28,32)
    clusters.append((24, 22, 3, 0.9, 7))  # main chest cluster
    clusters.append((21, 24, 2, 0.6, 4))  # left chest
    clusters.append((27, 23, 2, 0.5, 3))  # right chest
    # Collarbone area
    clusters.append((23, 20, 2, 0.4, 3))  # collarbone

    # == RIGHT ARM (UV 40,16 to 56,32) ==
    # Shoulder — top of arm
    # Arm top face: UV (44,16)-(48,20)
    clusters.append((46, 17, 2, 0.9, 5))  # right shoulder top
    clusters.append((44, 18, 2, 0.6, 3))  # shoulder edge
    # Upper arm front: UV (44,20)-(48,32)
    clusters.append((46, 21, 2, 0.4, 3))  # upper arm accent

    # == LEFT ARM (UV 32,48 to 48,64) ==
    # Shoulder — top of arm
    # Arm top face: UV (36,48)-(40,52)
    clusters.append((38, 49, 2, 0.9, 5))  # left shoulder top
    clusters.append((36, 50, 2, 0.6, 3))  # shoulder edge
    # Upper arm
    clusters.append((38, 53, 2, 0.4, 3))  # upper arm accent

    # == RIGHT LEG (UV 0,16 to 16,32) ==
    # Knee area — roughly mid-height of leg front face
    # Leg front face: UV (4,20)-(8,32)
    clusters.append((6, 26, 2, 0.7, 4))   # right knee
    clusters.append((5, 28, 2, 0.4, 3))   # below knee

    # == LEFT LEG (UV 16,48 to 32,64) ==
    # Knee area
    # Leg front face: UV (20,52)-(24,64)
    clusters.append((22, 58, 2, 0.7, 4))  # left knee
    clusters.append((21, 60, 2, 0.4, 3))  # below knee

    def place_crystal_shard(cx, cy, length, angle, brightness, alpha_mult):
        """Draw a single crystal shard — a short line of 1-4 pixels tapering from
        base (deeper blue, lower alpha) to tip (pale white-blue, higher alpha)."""
        for i in range(length):
            t = i / max(1, length - 1)  # 0 at base, 1 at tip
            px = int(round(cx + math.cos(angle) * i))
            py = int(round(cy + math.sin(angle) * i))
            if 0 <= px < 64 and 0 <= py < 64:
                # Color: base = deeper blue (100,160,220), tip = pale ice (200,230,255)
                r = cl(100 + 100 * t * brightness)
                g = cl(160 + 70 * t * brightness)
                b = cl(220 + 35 * t * brightness)
                # Alpha: moderate at base, brighter at tip, scaled by alpha_mult
                a = cl((60 + 120 * t) * alpha_mult * brightness)
                # Blend: keep the brighter of existing or new pixel
                er, eg, eb, ea = rows[py][px]
                if a > ea:
                    rows[py][px] = (r, g, b, a)

    def place_cluster(cx, cy, radius, intensity, num_shards):
        """Place a cluster of crystal shards radiating outward from a center point."""
        for _ in range(num_shards):
            # Offset the shard origin slightly from cluster center
            ox = cx + rng.random() * radius * 2 - radius
            oy = cy + rng.random() * radius * 2 - radius
            # Random outward angle
            angle = rng.random() * 2 * math.pi
            # Shard length: 1-4 pixels
            length = rng.randint(1, 4)
            bright = 0.5 + rng.random() * 0.5
            place_crystal_shard(ox, oy, length, angle, bright * intensity, intensity)

        # Central bright pixel — the crystal core
        if 0 <= int(cx) < 64 and 0 <= int(cy) < 64:
            core_b = intensity
            r = cl(180 * core_b)
            g = cl(220 * core_b)
            b = cl(255 * core_b)
            a = cl(180 * core_b)
            ix, iy = int(cx), int(cy)
            er, eg, eb, ea = rows[iy][ix]
            if a > ea:
                rows[iy][ix] = (r, g, b, a)

    # Place all clusters
    for cx, cy, radius, intensity, num_shards in clusters:
        place_cluster(cx, cy, radius, intensity, num_shards)

    # Add a few isolated single-pixel "sparkle" dots scattered very sparsely
    # across body-part UV regions only (not empty space)
    body_regions = [
        (0, 0, 32, 16),    # head
        (16, 16, 40, 32),  # torso
        (40, 16, 56, 32),  # right arm
        (32, 48, 48, 64),  # left arm
        (0, 16, 16, 32),   # right leg
        (16, 48, 32, 64),  # left leg
    ]
    for x0, y0, x1, y1 in body_regions:
        for _ in range(3):  # just a few sparkles per region
            sx = rng.randint(x0, x1 - 1)
            sy = rng.randint(y0, y1 - 1)
            if rows[sy][sx][3] == 0:  # only on empty pixels
                sparkle_a = rng.randint(15, 40)
                rows[sy][sx] = (160, 210, 245, sparkle_a)

    return rows

def make_crystal_block(seed=7001):
    """Semi-transparent crystal block texture — faceted icy blue appearance."""
    rng = random.Random(seed)
    base = (120, 180, 240)
    alpha = 160
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            # Create faceted crystal pattern with sharp brightness transitions
            noise = rng.random() * 0.3
            # Diagonal facets
            facet1 = ((x + y) % 5) / 5.0
            facet2 = ((x * 2 + y) % 7) / 7.0
            bright = 0.6 + facet1 * 0.25 + facet2 * 0.15 + noise
            # Edge highlight
            if x == 0 or y == 0:
                bright += 0.15
            # Inner glow spots
            cx, cy = x - 8, y - 8
            dist = math.sqrt(cx*cx + cy*cy)
            if dist < 3:
                bright += 0.1 * (1 - dist/3)
            bright = max(0.4, min(1.2, bright))
            r = cl(base[0] * bright)
            g = cl(base[1] * bright)
            b = cl(base[2] * bright)
            # Occasional bright specular highlights
            if rng.random() < 0.06:
                r = min(255, r + 60)
                g = min(255, g + 50)
                b = min(255, b + 40)
            row.append((r, g, b, alpha))
        rows.append(row)
    return rows

BLK = os.path.join("src","main","resources","assets","reactivefluids","textures","block")
ITM = os.path.join("src","main","resources","assets","reactivefluids","textures","item")

# ---------------------------------------------------------------------------
# Reagent vial sprite — small throwable bottle, 16x16
# ---------------------------------------------------------------------------
# Vial shape template (0=transparent, 1=outline, 2=glass, 3=liquid, 4=cork)
VIAL = [
    [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
    [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
    [0,0,0,0,0,0,0,4,4,0,0,0,0,0,0,0],
    [0,0,0,0,0,0,0,4,4,0,0,0,0,0,0,0],
    [0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0],
    [0,0,0,0,0,1,2,2,2,2,1,0,0,0,0,0],
    [0,0,0,0,1,2,3,3,3,3,2,1,0,0,0,0],
    [0,0,0,0,1,2,3,3,3,3,2,1,0,0,0,0],
    [0,0,0,0,1,2,3,3,3,3,2,1,0,0,0,0],
    [0,0,0,0,1,2,3,3,3,3,2,1,0,0,0,0],
    [0,0,0,0,1,2,3,3,3,3,2,1,0,0,0,0],
    [0,0,0,0,1,2,3,3,3,3,2,1,0,0,0,0],
    [0,0,0,0,1,2,3,3,3,3,2,1,0,0,0,0],
    [0,0,0,0,0,1,2,3,3,2,1,0,0,0,0,0],
    [0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,0],
    [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],
]

def make_reagent(liquid_rgb, glass_rgb=(200,220,230)):
    """16x16 vial sprite with colored liquid."""
    X = (0,0,0,0)
    OUT = (40,40,40,255)
    CORK = (160,120,70,255)
    GLASS = glass_rgb + (160,)
    GLASS_HI = tuple(min(255, c+40) for c in glass_rgb) + (180,)
    LIQ = liquid_rgb + (255,)
    LIQ_HI = tuple(min(255, c+50) for c in liquid_rgb) + (255,)
    LIQ_SH = tuple(max(0, c-40) for c in liquid_rgb) + (255,)

    rows = []
    for y, row_tmpl in enumerate(VIAL):
        prow = []
        for x, v in enumerate(row_tmpl):
            if v == 0: prow.append(X)
            elif v == 1: prow.append(OUT)
            elif v == 4: prow.append(CORK)
            elif v == 2:
                # Glass — highlight on left edge
                prow.append(GLASS_HI if x <= 5 else GLASS)
            else:
                # Liquid with highlight gradient
                if x <= 6: prow.append(LIQ_HI)
                elif x >= 9: prow.append(LIQ_SH)
                else: prow.append(LIQ)
        rows.append(prow)
    return rows

def _scroll_base():
    """Shared scroll body: 16x16 rolled parchment with cylindrical shading,
    wooden dowel handles, and subtle paper texture noise.
    Returns a 16x16 grid of (r,g,b,a) pixels."""
    BG = (0, 0, 0, 0)
    # Parchment palette — warm aged paper
    P_HI   = (240, 220, 185, 255)   # highlight (leftmost visible column)
    P_MID  = (218, 195, 155, 255)   # mid tone
    P_DK   = (185, 160, 125, 255)   # shadow (right side)
    P_DEEP = (155, 130, 100, 255)   # deep shadow at border
    # Roll edges — darker, compressed paper
    ROLL_T = (160, 135, 105, 255)   # top/bottom roll highlight
    ROLL_S = (130, 108, 82, 255)    # roll shadow
    # Wooden dowel handles
    WOOD_HI = (155, 125, 85, 255)
    WOOD    = (125, 100, 70, 255)
    WOOD_DK = (95, 75, 55, 255)
    KNOB    = (140, 112, 78, 255)

    # Parchment column brightness: cylindrical falloff across columns 4-11
    # Brightest at col 5-6, darkening toward edges
    col_lut = {4: P_DEEP, 5: P_HI, 6: P_HI, 7: P_MID, 8: P_MID,
               9: P_DK, 10: P_DK, 11: P_DEEP}

    # Deterministic pseudo-noise for paper texture
    rng = random.Random(7777)
    noise = [[rng.randint(-8, 8) for _ in range(16)] for _ in range(16)]

    pixels = [[BG]*16 for _ in range(16)]

    for y in range(16):
        for x in range(16):
            # --- Knob caps at top and bottom of dowels ---
            if (y == 0 or y == 15) and x in (3, 12):
                pixels[y][x] = KNOB
                continue

            # --- Wooden dowel handles: columns 3 and 12, rows 1-14 ---
            if x == 3 and 1 <= y <= 14:
                pixels[y][x] = WOOD_DK   # left dowel (shadow side)
                continue
            if x == 12 and 1 <= y <= 14:
                pixels[y][x] = WOOD_HI   # right dowel (lit side)
                continue

            # --- Scroll body: columns 4-11, rows 1-14 ---
            if not (4 <= x <= 11 and 1 <= y <= 14):
                continue

            # Top roll (rows 1-2): convex cylinder bulge
            if y == 1:
                pixels[y][x] = ROLL_S if x in (4, 11) else ROLL_T
                continue
            if y == 2:
                pixels[y][x] = P_DEEP if x in (4, 11) else P_DK
                continue

            # Bottom roll (rows 13-14): convex cylinder bulge
            if y == 14:
                pixels[y][x] = ROLL_S if x in (4, 11) else ROLL_T
                continue
            if y == 13:
                pixels[y][x] = P_DEEP if x in (4, 11) else P_DK
                continue

            # Main parchment area (rows 3-12)
            base = col_lut.get(x, P_MID)
            n = noise[y][x]
            pixels[y][x] = (cl(base[0]+n), cl(base[1]+n), cl(base[2]+n-2), 255)

    return pixels


def _apply_ribbon(pixels, ribbon_color, ribbon_hi, ribbon_dk):
    """Draw a ribbon/seal band across the lower scroll (rows 11-13)."""
    for x in range(5, 11):
        pixels[11][x] = ribbon_dk if x in (5, 10) else ribbon_color
    for x in range(6, 10):
        pixels[12][x] = ribbon_color
    # Wax seal nub / highlight center
    pixels[12][7] = ribbon_hi
    pixels[12][8] = ribbon_hi
    # Dangling ribbon tails
    pixels[13][6] = ribbon_dk
    pixels[13][9] = ribbon_dk


def make_tower_scroll():
    """16x16 tower scroll item sprite — parchment with a mystical tower rune."""
    pixels = _scroll_base()

    # Rune colors — arcane purple
    RUNE     = (85, 45, 160, 255)
    RUNE_MID = (110, 70, 195, 255)
    RUNE_GL  = (155, 115, 245, 255)
    RUNE_BR  = (190, 160, 255, 255)   # bright tip/glow

    # Ribbon — crimson red
    RIBBON   = (155, 30, 30, 255)
    RIBN_HI  = (200, 65, 55, 255)
    RIBN_DK  = (110, 20, 20, 255)

    # Tower rune: a pointed wizard tower with crenellations and arcane window
    # Row 3: spire tip (bright glow)
    pixels[3][7] = RUNE_BR
    pixels[3][8] = RUNE_BR
    # Row 4: spire body
    pixels[4][7] = RUNE_GL
    pixels[4][8] = RUNE_GL
    # Row 5: roof / crenellations
    pixels[5][6] = RUNE_MID
    pixels[5][7] = RUNE
    pixels[5][8] = RUNE
    pixels[5][9] = RUNE_MID
    # Row 6: upper wall
    pixels[6][7] = RUNE
    pixels[6][8] = RUNE
    # Row 7: arcane window (glowing)
    pixels[7][7] = RUNE_GL
    pixels[7][8] = RUNE_GL
    # Row 8: mid wall
    pixels[8][7] = RUNE
    pixels[8][8] = RUNE
    # Row 9: lower wall with side buttresses
    pixels[9][6] = RUNE_MID
    pixels[9][7] = RUNE
    pixels[9][8] = RUNE
    pixels[9][9] = RUNE_MID
    # Row 10: base / foundation (wide)
    pixels[10][5] = RUNE_MID
    pixels[10][6] = RUNE
    pixels[10][7] = RUNE
    pixels[10][8] = RUNE
    pixels[10][9] = RUNE
    pixels[10][10] = RUNE_MID

    # Arcane sparkles flanking the tower
    pixels[4][6] = RUNE_BR
    pixels[6][9] = RUNE_GL
    pixels[8][6] = RUNE_GL

    _apply_ribbon(pixels, RIBBON, RIBN_HI, RIBN_DK)

    return pixels


def make_steed_scroll():
    """16x16 steed scroll item sprite — parchment with horse silhouette rune."""
    pixels = _scroll_base()

    # Rune colors — teal/cyan
    RUNE     = (40, 120, 165, 255)
    RUNE_MID = (65, 150, 195, 255)
    RUNE_GL  = (100, 185, 235, 255)
    RUNE_BR  = (140, 210, 250, 255)

    # Ribbon — forest green
    RIBBON   = (35, 115, 55, 255)
    RIBN_HI  = (65, 160, 85, 255)
    RIBN_DK  = (22, 80, 35, 255)

    # Horse silhouette: right-facing, with head up, arched neck, body, 4 legs
    # Row 3: ear tips
    pixels[3][9] = RUNE_BR
    # Row 4: head (muzzle forward)
    pixels[4][8] = RUNE_GL
    pixels[4][9] = RUNE_MID
    pixels[4][10] = RUNE     # muzzle
    # Row 5: neck arch
    pixels[5][7] = RUNE_GL
    pixels[5][8] = RUNE
    pixels[5][9] = RUNE
    # Row 6: upper body + neck
    pixels[6][6] = RUNE_MID
    pixels[6][7] = RUNE
    pixels[6][8] = RUNE
    pixels[6][9] = RUNE
    # Row 7: body (widest)
    pixels[7][5] = RUNE_MID
    pixels[7][6] = RUNE
    pixels[7][7] = RUNE
    pixels[7][8] = RUNE
    pixels[7][9] = RUNE
    # Row 8: belly + tail start
    pixels[8][5] = RUNE_GL   # tail
    pixels[8][6] = RUNE
    pixels[8][7] = RUNE
    pixels[8][8] = RUNE
    pixels[8][9] = RUNE
    # Row 9: upper legs (4 legs visible) + tail
    pixels[9][5] = RUNE_MID  # tail flowing
    pixels[9][6] = RUNE      # back leg
    pixels[9][7] = RUNE      # back leg
    pixels[9][9] = RUNE      # front leg
    # Row 10: lower legs
    pixels[10][6] = RUNE     # back hoof
    pixels[10][9] = RUNE     # front leg
    pixels[10][10] = RUNE_MID # front hoof forward (gallop)
    # Row 11: hooves (glow accents)
    pixels[10][7] = RUNE_GL  # back hoof glow

    _apply_ribbon(pixels, RIBBON, RIBN_HI, RIBN_DK)

    return pixels


def make_dancing_lights_scroll():
    """16x16 dancing lights scroll item sprite — parchment with warm glowing orbs."""
    pixels = _scroll_base()

    # Orb colors — warm golden-orange radiance
    ORB_CORE  = (255, 240, 140, 255)  # bright core
    ORB_MID   = (245, 200, 60, 255)   # mid glow
    ORB_OUTER = (220, 160, 40, 255)   # outer glow
    ORB_HALO  = (200, 145, 35, 120)   # faint warm halo (semi-transparent blend)

    # Ribbon — burnished gold
    RIBBON   = (185, 150, 40, 255)
    RIBN_HI  = (225, 195, 75, 255)
    RIBN_DK  = (140, 110, 25, 255)

    # Helper: draw a 3x3 glowing orb centered at (cx, cy) with halo
    def _orb(cx, cy):
        # Core pixel
        pixels[cy][cx] = ORB_CORE
        # Cardinal neighbors — mid glow
        for dx, dy in [(-1,0),(1,0),(0,-1),(0,1)]:
            nx, ny = cx+dx, cy+dy
            if 4 <= nx <= 11 and 3 <= ny <= 10:
                pixels[ny][nx] = ORB_MID
        # Diagonal neighbors — outer glow
        for dx, dy in [(-1,-1),(1,-1),(-1,1),(1,1)]:
            nx, ny = cx+dx, cy+dy
            if 4 <= nx <= 11 and 3 <= ny <= 10:
                # Blend halo onto existing parchment
                bg = pixels[ny][nx]
                t = 0.35
                pixels[ny][nx] = (cl(bg[0]*(1-t)+ORB_OUTER[0]*t),
                                  cl(bg[1]*(1-t)+ORB_OUTER[1]*t),
                                  cl(bg[2]*(1-t)+ORB_OUTER[2]*t), 255)
        # Extended halo — 2px out on cardinals
        for dx, dy in [(-2,0),(2,0),(0,-2),(0,2)]:
            nx, ny = cx+dx, cy+dy
            if 4 <= nx <= 11 and 3 <= ny <= 10:
                bg = pixels[ny][nx]
                t = 0.18
                pixels[ny][nx] = (cl(bg[0]*(1-t)+ORB_OUTER[0]*t),
                                  cl(bg[1]*(1-t)+ORB_OUTER[1]*t),
                                  cl(bg[2]*(1-t)+ORB_OUTER[2]*t), 255)

    # Four orbs in a loose diamond — positioned within scroll body
    _orb(6, 5)    # upper-left
    _orb(9, 5)    # upper-right
    _orb(6, 9)    # lower-left
    _orb(9, 9)    # lower-right

    # Connecting spark trail between orbs (center sparkle)
    pixels[7][7] = ORB_MID
    pixels[7][8] = ORB_MID

    _apply_ribbon(pixels, RIBBON, RIBN_HI, RIBN_DK)

    return pixels


def make_disintegrate_scroll():
    """16x16 disintegrate scroll item sprite — parchment with menacing green ray rune.
    6th-level spell — the most dangerous scroll. The rune shows a searing green beam
    with crackling energy and a disintegration burst at the impact point."""
    pixels = _scroll_base()

    # Ray colors — toxic/necrotic green, very saturated
    RAY_CORE  = (120, 255, 120, 255)  # searing bright core
    RAY_MID   = (50, 210, 50, 255)    # main beam
    RAY_OUTER = (30, 160, 30, 255)    # outer edge
    RAY_DK    = (15, 100, 15, 255)    # dark crackling energy
    BURST     = (180, 255, 140, 255)  # impact flash
    VOID      = (20, 30, 20, 255)     # void/absence at target

    # Ribbon — charcoal/obsidian (ominous)
    RIBBON   = (45, 40, 50, 255)
    RIBN_HI  = (75, 70, 85, 255)
    RIBN_DK  = (25, 22, 30, 255)

    # Diagonal green ray from lower-left to upper-right with 2px width
    # The beam runs from approximately (5,10) to (10,4)
    # Main beam spine
    beam = [(5,10), (6,9), (6,8), (7,8), (7,7), (8,7), (8,6), (9,5), (9,4), (10,4)]
    for bx, by in beam:
        if 4 <= bx <= 11 and 3 <= by <= 10:
            pixels[by][bx] = RAY_MID

    # Bright core (center pixels of beam)
    core = [(7,8), (7,7), (8,7), (8,6)]
    for bx, by in core:
        pixels[by][bx] = RAY_CORE

    # Beam width: adjacent pixels get outer glow
    glow_pixels = [(5,9), (6,7), (7,6), (8,5), (9,6), (10,5),
                   (5,11), (6,10), (7,9), (8,8), (9,7), (10,3)]
    for bx, by in glow_pixels:
        if 4 <= bx <= 11 and 3 <= by <= 10:
            bg = pixels[by][bx]
            t = 0.45
            pixels[by][bx] = (cl(bg[0]*(1-t)+RAY_OUTER[0]*t),
                              cl(bg[1]*(1-t)+RAY_OUTER[1]*t),
                              cl(bg[2]*(1-t)+RAY_OUTER[2]*t), 255)

    # Impact burst at upper-right end — bright flash
    pixels[3][10] = BURST
    pixels[4][10] = RAY_CORE
    pixels[3][9] = RAY_CORE
    pixels[4][9] = BURST
    pixels[5][10] = RAY_OUTER

    # Disintegration void/scatter at impact
    pixels[3][8] = RAY_DK
    pixels[5][9] = RAY_DK

    # Crackling energy sparks along beam
    pixels[6][5] = RAY_DK
    pixels[9][8] = RAY_DK
    pixels[10][6] = RAY_OUTER

    # Origin point — finger/hand casting (lower-left)
    pixels[10][5] = RAY_OUTER
    pixels[10][4] = RAY_DK

    # Tint the parchment near the beam with a faint sickly green wash
    wash_coords = [(6,6), (7,5), (8,9), (9,9), (5,8), (10,6)]
    for wx, wy in wash_coords:
        if 4 <= wx <= 11 and 3 <= wy <= 10:
            bg = pixels[wy][wx]
            t = 0.12
            pixels[wy][wx] = (cl(bg[0]*(1-t)+30*t),
                              cl(bg[1]*(1-t)+180*t),
                              cl(bg[2]*(1-t)+30*t), 255)

    _apply_ribbon(pixels, RIBBON, RIBN_HI, RIBN_DK)

    return pixels

def make_mold_earth_scroll():
    """Mold Earth scroll — brown earth rune, crumbling cube."""
    pixels = _scroll_base()
    RUNE = (120, 80, 40, 255); RUNE_GL = (170, 120, 60, 255)
    for y in range(16):
        for x in range(16):
            if y == 5 and 6 <= x <= 9:       pixels[y][x] = RUNE
            elif y == 9 and 6 <= x <= 9:     pixels[y][x] = RUNE
            elif 5 <= y <= 9 and x == 6:     pixels[y][x] = RUNE
            elif 5 <= y <= 9 and x == 9:     pixels[y][x] = RUNE
            elif y == 7 and x == 7:          pixels[y][x] = RUNE_GL
            elif y == 7 and x == 8:          pixels[y][x] = RUNE_GL
            elif y == 10 and x == 7:         pixels[y][x] = RUNE
            elif y == 11 and x == 8:         pixels[y][x] = RUNE
            elif y == 11 and x == 6:         pixels[y][x] = RUNE
    _apply_ribbon(pixels, (120, 80, 40, 255), (160, 110, 60, 255), (90, 60, 30, 255))
    return pixels

def make_wall_of_stone_scroll():
    """Wall of Stone scroll — stone brick pattern rune."""
    pixels = _scroll_base()
    RUNE = (130, 130, 140, 255); RUNE_GL = (180, 180, 195, 255)
    for y in range(16):
        for x in range(16):
            if y == 4 and 5 <= x <= 10:      pixels[y][x] = RUNE_GL
            elif y == 5 and 5 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 6 and (x == 5 or x == 8 or x == 10): pixels[y][x] = RUNE
            elif y == 7 and 5 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 8 and (x == 5 or x == 7 or x == 10): pixels[y][x] = RUNE
            elif y == 9 and 5 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 10 and (x == 5 or x == 9 or x == 10): pixels[y][x] = RUNE
            elif y == 11 and 5 <= x <= 10:   pixels[y][x] = RUNE_GL
    _apply_ribbon(pixels, (130, 130, 140, 255), (170, 170, 180, 255), (100, 100, 110, 255))
    return pixels

def make_passwall_scroll():
    """Passwall scroll — archway/tunnel rune."""
    pixels = _scroll_base()
    RUNE = (100, 50, 150, 255); RUNE_GL = (160, 100, 230, 255)
    for y in range(16):
        for x in range(16):
            if y == 4 and 7 <= x <= 8:       pixels[y][x] = RUNE_GL
            elif y == 5 and (x == 6 or x == 9): pixels[y][x] = RUNE
            elif y == 5 and 7 <= x <= 8:     pixels[y][x] = RUNE_GL
            elif 6 <= y <= 10 and x == 6:    pixels[y][x] = RUNE
            elif 6 <= y <= 10 and x == 9:    pixels[y][x] = RUNE
            elif y == 11 and 6 <= x <= 9:    pixels[y][x] = RUNE
            elif y == 7 and x == 8:          pixels[y][x] = RUNE_GL
            elif y == 9 and x == 7:          pixels[y][x] = RUNE_GL
    _apply_ribbon(pixels, (100, 50, 150, 255), (140, 80, 200, 255), (70, 30, 110, 255))
    return pixels

def make_dimension_door_scroll():
    """Dimension Door scroll — connected portal circles rune."""
    pixels = _scroll_base()
    RUNE = (80, 40, 160, 255); RUNE_GL = (150, 100, 255, 255)
    for y in range(16):
        for x in range(16):
            if y == 5 and 6 <= x <= 7:       pixels[y][x] = RUNE
            elif y == 6 and (x == 5 or x == 8): pixels[y][x] = RUNE
            elif y == 7 and (x == 5 or x == 8): pixels[y][x] = RUNE
            elif y == 8 and 6 <= x <= 7:     pixels[y][x] = RUNE
            elif y == 7 and 9 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 8 and (x == 8 or x == 11): pixels[y][x] = RUNE
            elif y == 9 and (x == 8 or x == 11): pixels[y][x] = RUNE
            elif y == 10 and 9 <= x <= 10:   pixels[y][x] = RUNE
            elif y == 6 and x == 7:          pixels[y][x] = RUNE_GL
            elif y == 9 and x == 10:         pixels[y][x] = RUNE_GL
            elif y == 8 and x == 8:          pixels[y][x] = RUNE_GL
    _apply_ribbon(pixels, (80, 40, 160, 255), (130, 80, 220, 255), (50, 20, 120, 255))
    return pixels

def make_conjure_animals_scroll():
    """Conjure Animals scroll — wolf head rune."""
    pixels = _scroll_base()
    RUNE = (60, 140, 180, 255); RUNE_GL = (100, 200, 240, 255)
    for y in range(16):
        for x in range(16):
            if y == 4 and (x == 6 or x == 9): pixels[y][x] = RUNE_GL
            elif y == 5 and (x == 6 or x == 9): pixels[y][x] = RUNE
            elif y == 5 and 7 <= x <= 8:     pixels[y][x] = RUNE
            elif y == 6 and 6 <= x <= 9:     pixels[y][x] = RUNE
            elif y == 7 and 6 <= x <= 9:     pixels[y][x] = RUNE
            elif y == 7 and x == 7:          pixels[y][x] = RUNE_GL
            elif y == 8 and 7 <= x <= 8:     pixels[y][x] = RUNE
            elif y == 9 and x == 8:          pixels[y][x] = RUNE_GL
            elif y == 10 and (x == 6 or x == 10): pixels[y][x] = RUNE
            elif y == 11 and (x == 5 or x == 10): pixels[y][x] = RUNE
    _apply_ribbon(pixels, (60, 140, 180, 255), (90, 180, 220, 255), (40, 100, 140, 255))
    return pixels

def make_reverse_gravity_scroll():
    """Reverse Gravity scroll — upward arrow rune."""
    pixels = _scroll_base()
    RUNE = (150, 50, 200, 255); RUNE_GL = (210, 120, 255, 255)
    for y in range(16):
        for x in range(16):
            if y == 4 and x == 8:            pixels[y][x] = RUNE_GL
            elif y == 5 and 7 <= x <= 9:     pixels[y][x] = RUNE_GL
            elif y == 6 and 6 <= x <= 10:    pixels[y][x] = RUNE
            elif 7 <= y <= 10 and 7 <= x <= 9: pixels[y][x] = RUNE
            elif y == 11 and 7 <= x <= 9:    pixels[y][x] = RUNE
            elif y == 6 and x == 5:          pixels[y][x] = RUNE
            elif y == 5 and x == 10:         pixels[y][x] = RUNE
            elif y == 8 and x == 5:          pixels[y][x] = RUNE
    _apply_ribbon(pixels, (150, 50, 200, 255), (200, 90, 255, 255), (100, 30, 150, 255))
    return pixels

def make_plant_growth_scroll():
    """Plant Growth scroll — green leaf/sprout rune."""
    pixels = _scroll_base()
    RUNE = (30, 140, 50, 255); RUNE_GL = (80, 220, 80, 255)
    for y in range(16):
        for x in range(16):
            if y == 4 and x == 8:            pixels[y][x] = RUNE_GL
            elif y == 5 and 7 <= x <= 9:     pixels[y][x] = RUNE_GL
            elif y == 6 and 6 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 7 and 6 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 8 and 7 <= x <= 9:     pixels[y][x] = RUNE
            elif y == 9 and x == 8:          pixels[y][x] = RUNE
            elif y == 10 and x == 8:         pixels[y][x] = RUNE
            elif y == 11 and 7 <= x <= 9:    pixels[y][x] = RUNE
            elif y == 6 and x == 8:          pixels[y][x] = RUNE_GL
    _apply_ribbon(pixels, (30, 140, 50, 255), (60, 180, 70, 255), (20, 100, 35, 255))
    return pixels

def make_fog_cloud_scroll():
    """Fog Cloud scroll — swirling cloud rune."""
    pixels = _scroll_base()
    RUNE = (180, 180, 190, 255); RUNE_GL = (220, 220, 235, 255)
    for y in range(16):
        for x in range(16):
            if y == 5 and 6 <= x <= 9:       pixels[y][x] = RUNE_GL
            elif y == 6 and 5 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 7 and 5 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 8 and 6 <= x <= 9:     pixels[y][x] = RUNE
            elif y == 9 and 7 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 10 and 6 <= x <= 9:    pixels[y][x] = RUNE
            elif y == 7 and x == 7:          pixels[y][x] = RUNE_GL
            elif y == 9 and x == 9:          pixels[y][x] = RUNE_GL
    _apply_ribbon(pixels, (180, 180, 190, 255), (210, 210, 225, 255), (140, 140, 155, 255))
    return pixels

def make_erupting_earth_scroll():
    """Erupting Earth scroll — cracked earth/explosion rune."""
    pixels = _scroll_base()
    RUNE = (160, 100, 40, 255); RUNE_GL = (220, 150, 60, 255)
    for y in range(16):
        for x in range(16):
            if y == 4 and x == 8:            pixels[y][x] = RUNE_GL
            elif y == 5 and 7 <= x <= 9:     pixels[y][x] = RUNE_GL
            elif y == 6 and 6 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 7 and 5 <= x <= 11:    pixels[y][x] = RUNE
            elif y == 8 and 6 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 9 and 7 <= x <= 9:     pixels[y][x] = RUNE
            elif y == 10 and (x == 6 or x == 10): pixels[y][x] = RUNE
            elif y == 11 and (x == 5 or x == 11): pixels[y][x] = RUNE
            elif y == 7 and x == 8:          pixels[y][x] = RUNE_GL
    _apply_ribbon(pixels, (160, 100, 40, 255), (200, 130, 50, 255), (120, 70, 30, 255))
    return pixels

def make_tiny_hut_scroll():
    """Tiny Hut scroll — dome/hemisphere rune."""
    pixels = _scroll_base()
    RUNE = (100, 180, 220, 255); RUNE_GL = (150, 220, 255, 255)
    for y in range(16):
        for x in range(16):
            if y == 4 and 7 <= x <= 8:       pixels[y][x] = RUNE_GL
            elif y == 5 and 6 <= x <= 9:     pixels[y][x] = RUNE
            elif y == 6 and 5 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 7 and 5 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 8 and 5 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 9 and 5 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 6 and x == 8:          pixels[y][x] = RUNE_GL
            elif y == 8 and x == 7:          pixels[y][x] = RUNE_GL
    _apply_ribbon(pixels, (100, 180, 220, 255), (130, 210, 245, 255), (70, 140, 180, 255))
    return pixels

def make_bones_of_the_earth_scroll():
    """Bones of the Earth scroll — rising stone pillars rune."""
    pixels = _scroll_base()
    RUNE = (140, 130, 120, 255); RUNE_GL = (200, 190, 180, 255)
    for y in range(16):
        for x in range(16):
            if 4 <= y <= 11 and x == 6:      pixels[y][x] = RUNE
            elif 5 <= y <= 11 and x == 8:    pixels[y][x] = RUNE
            elif 6 <= y <= 11 and x == 10:   pixels[y][x] = RUNE
            elif y == 4 and x == 6:          pixels[y][x] = RUNE_GL
            elif y == 5 and x == 8:          pixels[y][x] = RUNE_GL
            elif y == 6 and x == 10:         pixels[y][x] = RUNE_GL
            elif y == 11 and (x == 5 or x == 7 or x == 9 or x == 11): pixels[y][x] = RUNE
    _apply_ribbon(pixels, (140, 130, 120, 255), (180, 170, 160, 255), (100, 90, 80, 255))
    return pixels

def make_move_earth_scroll():
    """Move Earth scroll — up/down arrows on terrain rune."""
    pixels = _scroll_base()
    RUNE = (120, 90, 50, 255); RUNE_GL = (170, 130, 70, 255)
    for y in range(16):
        for x in range(16):
            if y == 4 and x == 6:            pixels[y][x] = RUNE_GL
            elif y == 5 and 5 <= x <= 7:     pixels[y][x] = RUNE
            elif 6 <= y <= 8 and x == 6:     pixels[y][x] = RUNE
            elif y == 9 and x == 10:         pixels[y][x] = RUNE_GL
            elif 7 <= y <= 9 and x == 10:    pixels[y][x] = RUNE
            elif y == 10 and 9 <= x <= 11:   pixels[y][x] = RUNE
            elif y == 11 and 5 <= x <= 11:   pixels[y][x] = RUNE
            elif y == 6 and x == 6:          pixels[y][x] = RUNE_GL
    _apply_ribbon(pixels, (120, 90, 50, 255), (160, 120, 70, 255), (80, 60, 35, 255))
    return pixels

def make_arcane_gate_scroll():
    """Arcane Gate scroll — two linked ovals rune."""
    pixels = _scroll_base()
    RUNE = (120, 40, 180, 255); RUNE_GL = (180, 100, 255, 255)
    for y in range(16):
        for x in range(16):
            if y == 4 and 5 <= x <= 7:       pixels[y][x] = RUNE
            elif y == 5 and (x == 5 or x == 7): pixels[y][x] = RUNE
            elif y == 6 and (x == 5 or x == 7): pixels[y][x] = RUNE
            elif y == 7 and 5 <= x <= 7:     pixels[y][x] = RUNE
            elif y == 7 and 9 <= x <= 11:    pixels[y][x] = RUNE
            elif y == 8 and (x == 9 or x == 11): pixels[y][x] = RUNE
            elif y == 9 and (x == 9 or x == 11): pixels[y][x] = RUNE
            elif y == 10 and 9 <= x <= 11:   pixels[y][x] = RUNE
            elif y == 5 and x == 6:          pixels[y][x] = RUNE_GL
            elif y == 9 and x == 10:         pixels[y][x] = RUNE_GL
            elif y == 7 and x == 8:          pixels[y][x] = RUNE_GL
    _apply_ribbon(pixels, (120, 40, 180, 255), (160, 80, 230, 255), (80, 20, 130, 255))
    return pixels

def make_meteor_swarm_scroll():
    """Meteor Swarm scroll — falling fireballs rune."""
    pixels = _scroll_base()
    RUNE = (200, 60, 20, 255); RUNE_GL = (255, 140, 40, 255)
    for y in range(16):
        for x in range(16):
            if y == 4 and x == 6:            pixels[y][x] = RUNE_GL
            elif y == 5 and 5 <= x <= 7:     pixels[y][x] = RUNE
            elif y == 5 and x == 10:         pixels[y][x] = RUNE_GL
            elif y == 6 and 9 <= x <= 11:    pixels[y][x] = RUNE
            elif y == 7 and x == 7:          pixels[y][x] = RUNE_GL
            elif y == 8 and 6 <= x <= 8:     pixels[y][x] = RUNE
            elif y == 9 and x == 9:          pixels[y][x] = RUNE_GL
            elif y == 10 and 8 <= x <= 10:   pixels[y][x] = RUNE
            elif y == 11 and 5 <= x <= 11:   pixels[y][x] = RUNE
            elif y == 6 and x == 6:          pixels[y][x] = RUNE_GL
    _apply_ribbon(pixels, (200, 60, 20, 255), (240, 100, 30, 255), (150, 40, 15, 255))
    return pixels

def make_control_water_scroll():
    """Control Water scroll — parted waves rune."""
    pixels = _scroll_base()
    RUNE = (30, 100, 200, 255); RUNE_GL = (80, 160, 255, 255)
    for y in range(16):
        for x in range(16):
            if y == 5 and (x == 5 or x == 6):    pixels[y][x] = RUNE
            elif y == 5 and (x == 10 or x == 11): pixels[y][x] = RUNE
            elif y == 6 and x == 5:          pixels[y][x] = RUNE
            elif y == 6 and x == 11:         pixels[y][x] = RUNE
            elif y == 7 and x == 5:          pixels[y][x] = RUNE
            elif y == 7 and x == 11:         pixels[y][x] = RUNE
            elif y == 8 and (x == 5 or x == 6):   pixels[y][x] = RUNE
            elif y == 8 and (x == 10 or x == 11): pixels[y][x] = RUNE
            elif y == 9 and 6 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 7 and x == 8:          pixels[y][x] = RUNE_GL
            elif y == 6 and x == 8:          pixels[y][x] = RUNE_GL
    _apply_ribbon(pixels, (30, 100, 200, 255), (60, 140, 240, 255), (20, 70, 150, 255))
    return pixels

def make_magnificent_mansion_scroll():
    """Magnificent Mansion scroll — grand doorway/mansion rune."""
    pixels = _scroll_base()
    RUNE = (180, 140, 50, 255); RUNE_GL = (240, 200, 80, 255)
    for y in range(16):
        for x in range(16):
            # Mansion silhouette with peaked roof
            if y == 3 and x == 8:            pixels[y][x] = RUNE_GL
            elif y == 4 and 7 <= x <= 9:     pixels[y][x] = RUNE
            elif y == 5 and 6 <= x <= 10:    pixels[y][x] = RUNE
            elif y == 6 and 5 <= x <= 11:    pixels[y][x] = RUNE
            elif 7 <= y <= 10 and x == 5:    pixels[y][x] = RUNE
            elif 7 <= y <= 10 and x == 11:   pixels[y][x] = RUNE
            elif y == 11 and 5 <= x <= 11:   pixels[y][x] = RUNE
            elif 8 <= y <= 10 and x == 8:    pixels[y][x] = RUNE_GL  # door
            elif y == 7 and (x == 7 or x == 9): pixels[y][x] = RUNE_GL  # windows
    _apply_ribbon(pixels, (180, 140, 50, 255), (220, 180, 70, 255), (140, 100, 35, 255))
    return pixels

def make_gongers_grotto_scroll():
    """Gonger's Glorious Grotto scroll — iron gate over cavern rune."""
    pixels = _scroll_base()
    RUNE = (60, 60, 80, 255); RUNE_GL = (120, 100, 160, 255)
    for y in range(16):
        for x in range(16):
            # Iron gate bars
            if y == 4 and 6 <= x <= 10:      pixels[y][x] = RUNE
            elif y == 4 and x == 8:          pixels[y][x] = RUNE_GL
            elif 5 <= y <= 8 and (x == 6 or x == 8 or x == 10): pixels[y][x] = RUNE
            elif y == 9 and 6 <= x <= 10:    pixels[y][x] = RUNE
            # Cavern below
            elif y == 10 and 5 <= x <= 11:   pixels[y][x] = RUNE_GL
            elif y == 11 and 4 <= x <= 12:   pixels[y][x] = RUNE
            elif y == 11 and (x == 6 or x == 10): pixels[y][x] = RUNE_GL  # glow spots
    _apply_ribbon(pixels, (60, 60, 80, 255), (90, 80, 120, 255), (40, 40, 60, 255))
    return pixels


def make_raise_dead_scroll():
    """16x16 raise dead scroll item sprite — parchment with a rising skeletal hand rune."""
    pixels = _scroll_base()

    # Rune colors — necromantic dark green / sickly yellow-green
    RUNE     = (30, 75, 30, 255)     # dark green bone
    RUNE_MID = (55, 110, 40, 255)    # mid sickly green
    RUNE_GL  = (120, 170, 50, 255)   # yellow-green glow
    RUNE_BR  = (160, 200, 60, 255)   # bright sickly highlight

    # Ribbon — deathly black-green
    RIBBON   = (20, 30, 18, 255)
    RIBN_HI  = (40, 55, 30, 255)
    RIBN_DK  = (10, 15, 8, 255)

    # Rising skeletal hand clawing upward from below
    # Row 3: middle finger tip (highest point, bright)
    pixels[3][8] = RUNE_BR
    # Row 4: middle finger upper, index finger tip
    pixels[4][7] = RUNE_BR
    pixels[4][8] = RUNE_GL
    # Row 5: ring finger tip, middle finger, index finger
    pixels[5][6] = RUNE_GL
    pixels[5][7] = RUNE_MID
    pixels[5][8] = RUNE
    # Row 6: pinky tip, ring, middle, index continuing
    pixels[6][5] = RUNE_GL
    pixels[6][6] = RUNE_MID
    pixels[6][7] = RUNE
    pixels[6][8] = RUNE
    pixels[6][9] = RUNE_GL   # thumb start
    # Row 7: fingers converge into palm
    pixels[7][6] = RUNE
    pixels[7][7] = RUNE
    pixels[7][8] = RUNE
    pixels[7][9] = RUNE_MID
    # Row 8: palm / wrist
    pixels[8][6] = RUNE_MID
    pixels[8][7] = RUNE
    pixels[8][8] = RUNE
    pixels[8][9] = RUNE_MID
    # Row 9: wrist bones narrowing
    pixels[9][7] = RUNE
    pixels[9][8] = RUNE
    # Row 10: forearm bone emerging from ground
    pixels[10][7] = RUNE_MID
    pixels[10][8] = RUNE_MID
    # Ground line — cracked earth the hand bursts from
    pixels[10][5] = RUNE_GL
    pixels[10][6] = RUNE_GL
    pixels[10][9] = RUNE_GL
    pixels[10][10] = RUNE_GL

    # Necromantic energy wisps flanking the hand
    pixels[4][10] = RUNE_BR
    pixels[6][4] = RUNE_BR
    pixels[8][10] = RUNE_GL
    pixels[5][10] = RUNE_MID

    _apply_ribbon(pixels, RIBBON, RIBN_HI, RIBN_DK)

    return pixels


def make_spectral_wolf_texture():
    """64x64 ghostly wolf entity texture — semi-transparent blue-white."""
    rows = []
    for y in range(64):
        row = []
        for x in range(64):
            r = 150 + ((x * 5 + y * 3) % 30)
            g = 200 + ((x * 3 + y * 5) % 25)
            b = 245
            a = 110
            row.append((min(r, 255), min(g, 255), min(b, 255), a))
        rows.append(row)
    return rows

def make_disintegrate_beam():
    """16x16 beam cross-section texture — white center fading to transparent edges.
    The renderer tints this green with vertex colors."""
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            # Distance from center line (horizontal midpoint)
            cy = abs(y - 7.5) / 7.5
            cx = x / 15.0  # along beam length
            d = cy  # fade based on distance from center
            if d < 0.15:
                row.append((255, 255, 255, 240))  # bright white core
            elif d < 0.35:
                row.append((230, 255, 230, 200))  # near-white green tinge
            elif d < 0.6:
                a = int(180 * (1.0 - (d - 0.35) / 0.25))
                row.append((200, 255, 200, max(a, 0)))
            elif d < 0.85:
                a = int(100 * (1.0 - (d - 0.6) / 0.25))
                row.append((150, 220, 150, max(a, 0)))
            else:
                row.append((0, 0, 0, 0))
        rows.append(row)
    return rows

def read_png(path):
    """Read a PNG file using only stdlib (struct, zlib).
    Returns a list of rows, each row a list of (r,g,b,a) tuples.
    Supports 8-bit RGB (colortype 2) and RGBA (colortype 6).
    Supports filter types 0-4 (None, Sub, Up, Average, Paeth)."""
    with open(path, 'rb') as f:
        data = f.read()

    # Verify PNG signature
    assert data[:8] == b'\x89PNG\r\n\x1a\n', "Not a valid PNG file"

    # Parse chunks
    pos = 8
    width = height = bit_depth = color_type = 0
    idat_chunks = []
    palette = []  # for color type 3 (indexed)
    trns = None   # tRNS chunk data for palette transparency

    while pos < len(data):
        length = struct.unpack('>I', data[pos:pos+4])[0]
        chunk_type = data[pos+4:pos+8]
        chunk_data = data[pos+8:pos+8+length]
        # skip CRC (4 bytes after chunk data)
        pos += 12 + length

        if chunk_type == b'IHDR':
            width, height, bit_depth, color_type = struct.unpack('>IIBB', chunk_data[:10])
            assert bit_depth in (1, 2, 4, 8), f"Only 1/2/4/8-bit depth supported, got {bit_depth}"
            assert color_type in (2, 3, 6), f"Only RGB(2), indexed(3), and RGBA(6) supported, got {color_type}"
        elif chunk_type == b'PLTE':
            # Palette: sequence of (R,G,B) entries
            for i in range(0, len(chunk_data), 3):
                palette.append((chunk_data[i], chunk_data[i+1], chunk_data[i+2]))
        elif chunk_type == b'tRNS':
            trns = chunk_data
        elif chunk_type == b'IDAT':
            idat_chunks.append(chunk_data)
        elif chunk_type == b'IEND':
            break

    # Decompress all IDAT data
    raw = zlib.decompress(b''.join(idat_chunks))

    # Bytes per pixel (for filtering purposes, minimum 1)
    if color_type == 3:
        bpp_filter = max(1, bit_depth // 8)  # for sub-byte depths, filter uses bpp=1
    elif color_type == 2:
        bpp_filter = 3
    else:
        bpp_filter = 4
    # Stride = bytes per scanline (for sub-byte indexed, ceil(width*bits/8))
    if color_type == 3 and bit_depth < 8:
        stride = (width * bit_depth + 7) // 8
    else:
        stride = width * bpp_filter
    bpp = bpp_filter  # used by unfilter below

    def paeth_predictor(a, b, c):
        p = a + b - c
        pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
        if pa <= pb and pa <= pc:
            return a
        elif pb <= pc:
            return b
        return c

    # Unfilter scanlines
    rows = []
    prev_row_bytes = bytes(stride)  # previous row starts as all zeros
    offset = 0

    for y in range(height):
        filter_type = raw[offset]
        offset += 1
        scanline = bytearray(raw[offset:offset + stride])
        offset += stride

        if filter_type == 0:  # None
            pass
        elif filter_type == 1:  # Sub
            for i in range(stride):
                a = scanline[i - bpp] if i >= bpp else 0
                scanline[i] = (scanline[i] + a) & 0xFF
        elif filter_type == 2:  # Up
            for i in range(stride):
                scanline[i] = (scanline[i] + prev_row_bytes[i]) & 0xFF
        elif filter_type == 3:  # Average
            for i in range(stride):
                a = scanline[i - bpp] if i >= bpp else 0
                b = prev_row_bytes[i]
                scanline[i] = (scanline[i] + ((a + b) >> 1)) & 0xFF
        elif filter_type == 4:  # Paeth
            for i in range(stride):
                a = scanline[i - bpp] if i >= bpp else 0
                b = prev_row_bytes[i]
                c = prev_row_bytes[i - bpp] if i >= bpp else 0
                scanline[i] = (scanline[i] + paeth_predictor(a, b, c)) & 0xFF

        prev_row_bytes = bytes(scanline)

        # Convert scanline bytes to pixel tuples
        row = []
        if color_type == 3 and bit_depth < 8:
            # Sub-byte indexed: extract pixel indices from packed bytes
            pixels_per_byte = 8 // bit_depth
            mask = (1 << bit_depth) - 1
            for x in range(width):
                byte_idx = (x * bit_depth) // 8
                bit_offset = 8 - bit_depth - ((x * bit_depth) % 8)
                idx = (scanline[byte_idx] >> bit_offset) & mask
                r, g, b = palette[idx]
                a = trns[idx] if (trns is not None and idx < len(trns)) else 255
                row.append((r, g, b, a))
        else:
            for x in range(width):
                if color_type == 3:  # 8-bit Indexed
                    p = x
                    idx = scanline[p]
                    r, g, b = palette[idx]
                    a = trns[idx] if (trns is not None and idx < len(trns)) else 255
                    row.append((r, g, b, a))
                elif color_type == 2:  # RGB
                    p = x * 3
                    row.append((scanline[p], scanline[p+1], scanline[p+2], 255))
                else:  # RGBA
                    p = x * 4
                    row.append((scanline[p], scanline[p+1], scanline[p+2], scanline[p+3]))
        rows.append(row)

    return rows

def make_phantom_steed_texture():
    """64x64 ghostly horse entity texture based on vanilla horse_white.png.

    Reads the vanilla horse texture, computes luminance, and applies a rich
    spectral color palette with region-specific effects:
    - Icy blue-white body with depth gradient (lighter highlights, deeper
      blue-purple shadows)
    - Bright cyan/white glowing eyes
    - Soul-fire wisps on mane and tail
    - Ethereal glowing hooves fading to bright cyan
    - Arcane energy vein patterns across the body
    - Edge-glow on UV region boundaries suggesting inner light

    All output pixels are fully opaque (alpha=255) — translucency comes from
    the render shader, not texture alpha.
    """
    vanilla_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'vanilla_horse_white.png')
    vanilla = read_png(vanilla_path)
    H = len(vanilla)
    W = len(vanilla[0])

    # --- Color palette ---
    # Base ghostly tint: highlights (bright icy white-blue) to shadows (deep blue-purple)
    hi_color  = (210, 235, 255)  # bright icy white-blue for highlights
    mid_color = (120, 160, 220)  # mid ethereal blue
    lo_color  = (60, 50, 140)    # deep blue-purple for shadows

    # Special colors
    eye_color     = (220, 255, 255)  # bright white-cyan for eyes
    eye_core      = (255, 255, 255)  # pure white eye center
    vein_color    = (140, 230, 255)  # bright cyan for energy veins
    hoof_glow     = (180, 245, 255)  # bright cyan-white for hooves
    mane_wisp     = (100, 220, 255)  # soul fire cyan for mane/tail wisps
    mane_bright   = (180, 250, 255)  # bright wisp tips

    # --- Define UV region bounding boxes ---
    # Each entry: (tex_u, tex_v, box_w, box_h, box_d, label)
    # The UV footprint for a box of size (w, h, d) starting at texOffs(u, v):
    #   top face:    (u+d,       v,        w, d)
    #   bottom face: (u+d+w,     v,        w, d)
    #   front face:  (u+d,       v+d,      w, h)
    #   back face:   (u+d+w+d,   v+d,      w, h)
    #   left face:   (u,         v+d,      d, h)
    #   right face:  (u+d+w,     v+d,      d, h)

    def uv_rects(u, v, bw, bh, bd):
        """Return list of (x, y, w, h) pixel rectangles for a box's UV faces."""
        return [
            (u + bd,         v,          bw, bd),   # top
            (u + bd + bw,    v,          bw, bd),   # bottom
            (u + bd,         v + bd,     bw, bh),   # front
            (u + bd + bw + bd, v + bd,   bw, bh),   # back
            (u,              v + bd,     bd, bh),   # left
            (u + bd + bw,    v + bd,     bd, bh),   # right
        ]

    # Build pixel-level region map (64x64) — each pixel tagged with its region
    # Regions: 'body', 'head', 'mouth', 'headcluster', 'mane', 'tail', 'leg', 'ear', None
    region_map = [[None]*W for _ in range(H)]
    edge_map = [[False]*W for _ in range(H)]

    region_defs = [
        # (texOffs_u, texOffs_v, box_w, box_h, box_d, label)
        (0,  32, 10, 10, 22, 'body'),
        (0,  13,  6,  5,  7, 'head'),
        (0,  25,  4,  5,  5, 'mouth'),
        (0,  35,  4, 12,  7, 'headcluster'),
        (56, 36,  2, 16,  2, 'mane'),
        (42, 36,  3, 14,  4, 'tail'),
        (48, 21,  4, 11,  4, 'leg'),
        (19, 16,  2,  3,  1, 'ear'),
    ]

    for u, v, bw, bh, bd, label in region_defs:
        rects = uv_rects(u, v, bw, bh, bd)
        for rx, ry, rw, rh in rects:
            for py in range(ry, min(ry + rh, H)):
                for px in range(rx, min(rx + rw, W)):
                    region_map[py][px] = label

    # Build edge map — pixels at the border of each UV rect
    for u, v, bw, bh, bd, label in region_defs:
        rects = uv_rects(u, v, bw, bh, bd)
        for rx, ry, rw, rh in rects:
            for py in range(ry, min(ry + rh, H)):
                for px in range(rx, min(rx + rw, W)):
                    if py == ry or py == ry + rh - 1 or px == rx or px == rx + rw - 1:
                        edge_map[py][px] = True

    # Precompute hoof rows for legs — bottom 3 rows of front face of leg UV
    # Leg at texOffs(48,21), box 4x11x4
    # Front face: (48+4, 21+4, 4, 11) = (52, 25, 4, 11)
    # Hoof = bottom 3 rows of each face
    leg_rects = uv_rects(48, 21, 4, 11, 4)
    hoof_pixels = set()
    for rx, ry, rw, rh in leg_rects:
        for py in range(max(ry, ry + rh - 3), ry + rh):
            for px in range(rx, min(rx + rw, W)):
                if py < H:
                    hoof_pixels.add((px, py))

    # Hoof gradient: how close to the very bottom (0.0 = top of hoof zone, 1.0 = bottom)
    hoof_gradient = {}
    for rx, ry, rw, rh in leg_rects:
        hoof_start = max(ry, ry + rh - 3)
        for py in range(hoof_start, min(ry + rh, H)):
            t = (py - hoof_start) / 2.0  # 0..1 over 3 rows
            for px in range(rx, min(rx + rw, W)):
                hoof_gradient[(px, py)] = t

    # Mane/tail pixel sets for wisp effects
    mane_rects = uv_rects(56, 36, 2, 16, 2)
    mane_pixels = set()
    for rx, ry, rw, rh in mane_rects:
        for py in range(ry, min(ry + rh, H)):
            for px in range(rx, min(rx + rw, W)):
                mane_pixels.add((px, py))

    tail_rects = uv_rects(42, 36, 3, 14, 4)
    tail_pixels = set()
    for rx, ry, rw, rh in tail_rects:
        for py in range(ry, min(ry + rh, H)):
            for px in range(rx, min(rx + rw, W)):
                tail_pixels.add((px, py))

    # Body pixel set for energy veins
    body_rects = uv_rects(0, 32, 10, 10, 22)
    body_pixels = set()
    for rx, ry, rw, rh in body_rects:
        for py in range(ry, min(ry + rh, H)):
            for px in range(rx, min(rx + rw, W)):
                body_pixels.add((px, py))

    # Seeded RNG for deterministic noise
    rng = random.Random(42)
    noise = [[rng.random() for _ in range(W)] for _ in range(H)]

    rows = []
    for y in range(H):
        row = []
        for x in range(W):
            sr, sg, sb, sa = vanilla[y][x]

            if sa == 0:
                row.append((0, 0, 0, 0))
                continue

            # Luminance from vanilla pixel
            lum = (sr * 0.299 + sg * 0.587 + sb * 0.114) / 255.0

            # --- Base spectral tint with depth gradient ---
            # Two-stop gradient: lo_color -> mid_color -> hi_color
            if lum < 0.5:
                t = lum * 2.0  # 0..1 over dark half
                base_r = lo_color[0] + (mid_color[0] - lo_color[0]) * t
                base_g = lo_color[1] + (mid_color[1] - lo_color[1]) * t
                base_b = lo_color[2] + (mid_color[2] - lo_color[2]) * t
            else:
                t = (lum - 0.5) * 2.0  # 0..1 over bright half
                base_r = mid_color[0] + (hi_color[0] - mid_color[0]) * t
                base_g = mid_color[1] + (hi_color[1] - mid_color[1]) * t
                base_b = mid_color[2] + (hi_color[2] - mid_color[2]) * t

            # Add subtle noise for texture variation
            n = noise[y][x]
            base_r += (n - 0.5) * 16
            base_g += (n - 0.5) * 12
            base_b += (n - 0.5) * 10

            nr, ng, nb = base_r, base_g, base_b
            region = region_map[y][x]

            # --- Glowing eyes ---
            # Head front face at texOffs(0,13), box 6x5x7
            # Front face: (0+7, 13+7, 6, 5) = (7, 20, 6, 5)
            # Eye pixels roughly x=10-13, y=14-16 on the head texture
            if 10 <= x <= 13 and 14 <= y <= 16:
                # Core of eye = pure white, edges = bright cyan
                cx, cy = 11.5, 15.0
                d = math.sqrt((x - cx)**2 + (y - cy)**2)
                if d < 1.0:
                    nr, ng, nb = eye_core
                elif d < 2.0:
                    t2 = d - 1.0
                    nr = eye_core[0] + (eye_color[0] - eye_core[0]) * t2
                    ng = eye_core[1] + (eye_color[1] - eye_core[1]) * t2
                    nb = eye_core[2] + (eye_color[2] - eye_core[2]) * t2
                else:
                    nr, ng, nb = eye_color

            # --- Ethereal hooves ---
            elif (x, y) in hoof_pixels:
                t_hoof = hoof_gradient.get((x, y), 0.5)
                # Blend from base toward bright hoof glow
                blend = 0.5 + 0.5 * t_hoof  # stronger glow at bottom
                nr = nr + (hoof_glow[0] - nr) * blend
                ng = ng + (hoof_glow[1] - ng) * blend
                nb = nb + (hoof_glow[2] - nb) * blend
                # Add sparkle at very bottom
                if t_hoof > 0.8:
                    sparkle = (noise[y][x] > 0.5)
                    if sparkle:
                        nr = min(255, nr + 40)
                        ng = min(255, ng + 30)
                        nb = min(255, nb + 20)

            # --- Mane wisps (soul fire effect) ---
            elif (x, y) in mane_pixels:
                # Wispy pattern based on noise + position
                wisp_val = math.sin(y * 1.3 + x * 0.7) * 0.5 + 0.5
                wisp_val = wisp_val * 0.6 + noise[y][x] * 0.4
                if wisp_val > 0.6:
                    t_wisp = (wisp_val - 0.6) / 0.4  # 0..1
                    nr = nr + (mane_bright[0] - nr) * t_wisp * 0.8
                    ng = ng + (mane_bright[1] - ng) * t_wisp * 0.8
                    nb = nb + (mane_bright[2] - nb) * t_wisp * 0.8
                elif wisp_val > 0.3:
                    t_wisp = (wisp_val - 0.3) / 0.3
                    nr = nr + (mane_wisp[0] - nr) * t_wisp * 0.5
                    ng = ng + (mane_wisp[1] - ng) * t_wisp * 0.5
                    nb = nb + (mane_wisp[2] - nb) * t_wisp * 0.5

            # --- Tail wisps (soul fire effect) ---
            elif (x, y) in tail_pixels:
                wisp_val = math.sin(y * 1.1 + x * 0.9 + 2.0) * 0.5 + 0.5
                wisp_val = wisp_val * 0.55 + noise[y][x] * 0.45
                if wisp_val > 0.55:
                    t_wisp = (wisp_val - 0.55) / 0.45
                    nr = nr + (mane_bright[0] - nr) * t_wisp * 0.75
                    ng = ng + (mane_bright[1] - ng) * t_wisp * 0.75
                    nb = nb + (mane_bright[2] - nb) * t_wisp * 0.75
                elif wisp_val > 0.25:
                    t_wisp = (wisp_val - 0.25) / 0.3
                    nr = nr + (mane_wisp[0] - nr) * t_wisp * 0.45
                    ng = ng + (mane_wisp[1] - ng) * t_wisp * 0.45
                    nb = nb + (mane_wisp[2] - nb) * t_wisp * 0.45

            # --- Body energy veins / arcane rune patterns ---
            if (x, y) in body_pixels and not (10 <= x <= 13 and 14 <= y <= 16):
                # Sine-based vein network
                v1 = math.sin(x * 1.8 + y * 0.4) * math.cos(y * 1.2 - x * 0.6)
                v2 = math.sin((x + y) * 0.9 + 1.5) * math.sin((x - y) * 0.7)
                vein_intensity = max(v1, v2)
                # Thin vein lines where intensity is near peak
                if vein_intensity > 0.85:
                    t_vein = (vein_intensity - 0.85) / 0.15
                    nr = nr + (vein_color[0] - nr) * t_vein * 0.7
                    ng = ng + (vein_color[1] - ng) * t_vein * 0.7
                    nb = nb + (vein_color[2] - nb) * t_vein * 0.7
                # Subtle wider glow around veins
                elif vein_intensity > 0.7:
                    t_vein = (vein_intensity - 0.7) / 0.15
                    nr = nr + (vein_color[0] - nr) * t_vein * 0.2
                    ng = ng + (vein_color[1] - ng) * t_vein * 0.2
                    nb = nb + (vein_color[2] - nb) * t_vein * 0.2

            # --- Edge glow (inner light at UV boundaries) ---
            if edge_map[y][x] and sa > 0:
                nr = min(255, nr + 22)
                ng = min(255, ng + 28)
                nb = min(255, nb + 18)

            # Clamp and force fully opaque
            row.append((cl(nr), cl(ng), cl(nb), 255))
        rows.append(row)
    return rows

def make_raised_skeleton_texture():
    """64x32 raised skeleton entity texture based on vanilla skeleton.png.

    Subtle modifications — slightly yellowed bones, green glowing eye sockets,
    and a few faint dark marks. Should still look like a skeleton, just aged
    and necromantically reanimated.
    """
    jar_path = os.path.join(
        os.path.expanduser("~"),
        ".gradle", "caches", "minecraft", "versions", "1.21.1", "client.jar"
    )
    skeleton_entry = "assets/minecraft/textures/entity/skeleton/skeleton.png"
    tmp = tempfile.NamedTemporaryFile(suffix=".png", delete=False)
    try:
        with zipfile.ZipFile(jar_path, 'r') as zf:
            tmp.write(zf.read(skeleton_entry))
            tmp.close()
        vanilla = read_png(tmp.name)
    finally:
        os.unlink(tmp.name)

    H = len(vanilla)
    W = len(vanilla[0])
    rng = random.Random(0xB00E0042)

    rows = []
    for y in range(H):
        row = []
        for x in range(W):
            r, g, b, a = vanilla[y][x]

            # Preserve transparency
            if a == 0:
                row.append((0, 0, 0, 0))
                continue

            lum = 0.299 * r + 0.587 * g + 0.114 * b

            # Subtle warm/yellow shift on bones — blend 25% toward aged bone color
            # Aged bone target varies by luminance
            bone_r = 190 + (lum / 255.0) * 50  # 190-240
            bone_g = 175 + (lum / 255.0) * 45  # 175-220
            bone_b = 130 + (lum / 255.0) * 30  # 130-160
            t = 0.25  # only 25% blend — keep it subtle
            nr = r + t * (bone_r - r)
            ng = g + t * (bone_g - g)
            nb = b + t * (bone_b - b)

            # Slight overall darken
            nr *= 0.92
            ng *= 0.92
            nb *= 0.90

            # Sparse dark marks (~5% of visible pixels)
            if rng.random() < 0.05 and lum > 60:
                nr *= 0.7
                ng *= 0.7
                nb *= 0.65

            # Tiny noise
            n = rng.uniform(-3, 3)
            nr += n; ng += n; nb += n

            row.append((cl(nr), cl(ng), cl(nb), a))
        rows.append(row)
    return rows

def make_spectral_particle():
    """8x8 particle — wispy ethereal spirit mote for Phantom Steed / Spectral Wolf.
    Icy blue-white center fading to pale blue then transparent.
    Irregular flame-like wisp shape, NOT circular."""
    W, H = 8, 8
    cx, cy = 3.5, 3.0  # slightly above center for upward wisp feel

    rows = []
    for y in range(H):
        row = []
        for x in range(W):
            dx = x - cx
            dy = y - cy

            # Wisp shape: narrow at top, wider in middle, tapered at bottom
            # Use an asymmetric distance field to create a flame/wisp silhouette
            # Compress horizontal distance more at top and bottom
            y_norm = (y - cy) / 3.5  # -1 at top to +1 at bottom roughly

            # Wisp width envelope: widest slightly above center, tapers both ways
            width_factor = 1.0 - 0.35 * y_norm * y_norm  # wider in middle
            if y_norm < -0.3:
                # Narrow quickly at top (wisp tip)
                width_factor *= max(0.2, 1.0 + y_norm * 1.2)
            if y_norm > 0.6:
                # Taper at bottom too
                width_factor *= max(0.3, 1.0 - (y_norm - 0.6) * 1.5)

            # Adjusted distance accounting for wisp shape
            adj_dx = dx / max(0.3, width_factor)
            d = math.sqrt(adj_dx * adj_dx + dy * dy * 0.7)

            # Add some irregularity via angle-dependent wobble
            angle = math.atan2(dy, dx)
            wobble = 0.3 * math.sin(angle * 3.0 + 1.2) + 0.2 * math.sin(angle * 5.0 + 0.7)
            d += wobble

            if d < 1.0:
                # Bright core — blue-white
                t = d / 1.0
                r = cl(200 + (1 - t) * 55)
                g = cl(220 + (1 - t) * 35)
                b = 255
                a = cl(200 - t * 30)
            elif d < 2.0:
                # Inner glow — icy blue
                t = (d - 1.0) / 1.0
                r = cl(200 - t * 60)
                g = cl(220 - t * 40)
                b = cl(255 - t * 15)
                a = cl(170 - t * 50)
            elif d < 3.0:
                # Outer wisp — pale blue, fading
                t = (d - 2.0) / 1.0
                r = cl(140 - t * 80)
                g = cl(180 - t * 100)
                b = cl(240 - t * 80)
                a = cl(120 - t * 80)
            elif d < 3.8:
                # Faint halo — barely visible
                t = (d - 3.0) / 0.8
                r = cl(60 - t * 50)
                g = cl(80 - t * 60)
                b = cl(160 - t * 100)
                a = cl(40 - t * 40)
            else:
                r, g, b, a = 0, 0, 0, 0

            row.append((r, g, b, a))
        rows.append(row)
    return rows


def make_necrotic_particle_frames():
    """Generate 11 animation frames for the necrotic particle (16x16 each).
    Animation: teal-green wispy energy coalesces into a skull shape that
    opens its mouth before dissipating into nothing.

    Frame progression:
      0-2: Amorphous teal-green wisp forming/coalescing
      3-5: Wisp solidifies into a recognizable skull face (eye sockets, nose, teeth)
      6-8: Skull opens its mouth wider each frame
      9-10: Skull breaks apart and dissipates

    Hard-edged pixel art at 16x16, matching vanilla soul particle style.
    Returns list of 11 row-lists."""
    W, H = 16, 16
    frames = []

    # Color palette — teal-green necromantic energy
    # Bright core/highlight
    C_BRIGHT = (100, 255, 220, 255)
    # Main body color
    C_MAIN   = (40, 200, 160, 255)
    # Darker shade for edges/depth
    C_DARK   = (20, 130, 100, 255)
    # Very dark for eye sockets / deep features
    C_DEEP   = (10, 60, 50, 255)
    # Faint glow / wispy edges
    C_GLOW   = (60, 180, 140, 180)
    # Transparent
    T = (0, 0, 0, 0)

    # Helper to define frames as 16x16 grids using character maps
    # . = transparent, 1 = C_GLOW, 2 = C_DARK, 3 = C_MAIN, 4 = C_BRIGHT, 0 = C_DEEP
    palette = {'.': T, '1': C_GLOW, '2': C_DARK, '3': C_MAIN, '4': C_BRIGHT, '0': C_DEEP}

    frame_maps = [
        # Frame 0: Faint wisps forming at bottom — very sparse
        [
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            "......1.1.......",
            ".....1..........",
            "......11.1......",
            ".....1..21......",
            "......1.1.......",
            "................",
            "................",
        ],
        # Frame 1: Wisps gathering, denser blob forming
        [
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            "................",
            ".....1..1.......",
            "....1.21.1......",
            "....12321.......",
            ".....2332.......",
            "....13321.......",
            ".....1221.......",
            "......11........",
            "................",
            "................",
        ],
        # Frame 2: Dense cloud, starting to hint at round shape
        [
            "................",
            "................",
            "................",
            "................",
            "................",
            ".....1221.......",
            "....123321......",
            "...12334321.....",
            "...23344321.....",
            "...23343321.....",
            "....233321......",
            ".....2321.......",
            "......11........",
            "................",
            "................",
            "................",
        ],
        # Frame 3: Skull shape emerging — cranium round, eye sockets appear
        [
            "................",
            "................",
            "................",
            ".....1221.......",
            "....233332......",
            "...23444321.....",
            "...34444431.....",
            "...30344031.....",
            "...33400431.....",
            "....304031......",
            "....233321......",
            ".....2332.......",
            "......22........",
            "................",
            "................",
            "................",
        ],
        # Frame 4: Clear skull — eyes, nose hole, teeth visible, mouth closed
        [
            "................",
            "................",
            "......122.......",
            ".....233321.....",
            "....2344432.....",
            "...234444321....",
            "...340440431....",
            "...340440431....",
            "....30030031....",
            "....33403321....",
            "....34343431....",
            ".....303031.....",
            ".....23332......",
            "......222.......",
            "................",
            "................",
        ],
        # Frame 5: Skull fully formed — mouth starting to open slightly
        [
            "................",
            "................",
            ".....1232.......",
            "....2344321.....",
            "...234444321....",
            "...344444431....",
            "...340440431....",
            "...340440431....",
            "....30030031....",
            "....34343431....",
            "....30303031....",
            "................",
            ".....23332......",
            "......222.......",
            "................",
            "................",
        ],
        # Frame 6: Mouth opening wider — jaw separating from upper skull
        [
            "................",
            ".....1221.......",
            "....234432......",
            "...2344443......",
            "...344444431....",
            "...340440431....",
            "...340440431....",
            "....30030031....",
            "....34343431....",
            "................",
            "................",
            ".....30303......",
            "....2343431.....",
            ".....23332......",
            "......12........",
            "................",
        ],
        # Frame 7: Mouth wide open — jaw dropping, skull starting to crack
        [
            "................",
            "....12321.......",
            "...23444321.....",
            "...344444431....",
            "...340440431....",
            "...340440431....",
            "....3003003.....",
            "....3434343.....",
            "................",
            "................",
            "................",
            "....303.303.....",
            "....234.432.....",
            ".....23.32......",
            "......1.1.......",
            "................",
        ],
        # Frame 8: Skull breaking apart — fragments separating, wisps escaping
        [
            "......12........",
            "...1.3443.1.....",
            "...23444321.....",
            "...3404.0431....",
            "....340.043.....",
            "....300.003.....",
            ".....3...3......",
            "................",
            "................",
            "................",
            "....1.....1.....",
            "....23...32.....",
            ".....2...2......",
            "......1.1.......",
            "................",
            "................",
        ],
        # Frame 9: Mostly dissipated — scattered fragments
        [
            ".......1........",
            "....1.23........",
            ".....343.1......",
            "....130..3......",
            ".....3...1......",
            "......1.........",
            "................",
            "................",
            "................",
            "................",
            ".....1...1......",
            "......2.2.......",
            "......1.1.......",
            "................",
            "................",
            "................",
        ],
        # Frame 10: Almost gone — tiny wisps
        [
            "................",
            "................",
            "......1.........",
            ".....12.........",
            "......3.........",
            "......1.........",
            "................",
            "................",
            "................",
            "................",
            "................",
            "......1.........",
            "................",
            "................",
            "................",
            "................",
        ],
    ]

    for fi, fmap in enumerate(frame_maps):
        rows = []
        for y in range(H):
            row = []
            line = fmap[y] if y < len(fmap) else '.' * W
            for x in range(W):
                ch = line[x] if x < len(line) else '.'
                row.append(palette.get(ch, T))
            rows.append(row)
        frames.append(rows)
    return frames


def make_fog_cloud_particle():
    """8x8 particle — soft white cloud puff for Fog Cloud spell.
    Large, billowy, mostly opaque white with soft edges."""
    W, H = 8, 8
    cx, cy = 3.5, 3.5
    rows = []
    for y in range(H):
        row = []
        for x in range(W):
            dx = x - cx
            dy = y - cy
            d = math.sqrt(dx * dx + dy * dy)
            if d < 1.5:
                row.append((240, 240, 245, 220))
            elif d < 2.5:
                t = (d - 1.5) / 1.0
                a = cl(220 - t * 40)
                row.append((235, 235, 240, a))
            elif d < 3.5:
                t = (d - 2.5) / 1.0
                a = cl(180 - t * 80)
                row.append((225, 225, 235, a))
            elif d < 4.2:
                t = (d - 3.5) / 0.7
                a = cl(100 - t * 100)
                row.append((215, 215, 225, max(a, 0)))
            else:
                row.append((0, 0, 0, 0))
        rows.append(row)
    return rows

def make_raised_zombie_texture():
    """64x64 raised zombie entity texture based on vanilla zombie.png.

    Subtle modifications to the vanilla zombie — should still look like a zombie,
    just slightly more grey/pallid with faint green glowing eyes and a few dark
    vein-like marks. The vanilla texture does most of the heavy lifting.
    """
    jar_path = os.path.join(
        os.path.expanduser("~"),
        ".gradle", "caches", "minecraft", "versions", "1.21.1", "client.jar"
    )
    tex_entry = "assets/minecraft/textures/entity/zombie/zombie.png"
    tmp_path = None
    try:
        with zipfile.ZipFile(jar_path, 'r') as jar:
            tex_data = jar.read(tex_entry)
        tmp_fd, tmp_path = tempfile.mkstemp(suffix='.png')
        os.close(tmp_fd)
        with open(tmp_path, 'wb') as f:
            f.write(tex_data)
        vanilla = read_png(tmp_path)
    finally:
        if tmp_path and os.path.exists(tmp_path):
            os.remove(tmp_path)

    H = len(vanilla)
    W = len(vanilla[0])
    rng = random.Random(0xDEAD0042)

    # Sparse vein seeds — only 6 short paths for subtle effect
    vein_pixels = set()
    for _ in range(6):
        sx, sy = rng.randint(0, W-1), rng.randint(0, H-1)
        cx, cy = sx, sy
        for __ in range(rng.randint(2, 4)):
            vein_pixels.add((cx, cy))
            cx = max(0, min(W-1, cx + rng.choice([-1, 0, 1])))
            cy = max(0, min(H-1, cy + rng.choice([-1, 0, 1])))

    rows = []
    for y in range(H):
        row = []
        for x in range(W):
            r, g, b, a = vanilla[y][x]
            if a == 0:
                row.append((0, 0, 0, 0))
                continue

            # Subtle desaturation — 30% toward grey (keep most of original color)
            lum = 0.299 * r + 0.587 * g + 0.114 * b
            nr = r + 0.3 * (lum - r)
            ng = g + 0.3 * (lum - g)
            nb = b + 0.3 * (lum - b)

            # Slight darken + cool shift (more grey-green, less warm)
            nr = nr * 0.85
            ng = ng * 0.90
            nb = nb * 0.82

            # Faint dark vein marks (blend 40% toward dark purple)
            if (x, y) in vein_pixels:
                nr = nr * 0.6 + 30 * 0.4
                ng = ng * 0.6 + 15 * 0.4
                nb = nb * 0.6 + 40 * 0.4

            # Tiny per-pixel noise
            n = rng.uniform(-4, 4)
            nr += n; ng += n; nb += n

            row.append((cl(nr), cl(ng), cl(nb), 255))
        rows.append(row)
    return rows

def make_meteor_texture():
    """16x16 meteor entity texture — rocky meteorite with heated edges.
    Rendered as billboard quad with entityTranslucentEmissive; vertex color tint
    is orange-yellow (1.0, 0.7, 0.2) so texture colors are multiplied by that.
    The texture depicts a rough ROCK seen head-on: dark grey core with mineral
    flecks, brighter heated rim, irregular rocky edges.
    Uses only alpha=255 (opaque) or alpha=0 (transparent) — any intermediate
    alpha causes checkerboard artifacts with entityTranslucentEmissive."""
    rng = random.Random(42)

    # --- Generate multiple octaves of noise for rocky surface detail ---
    def make_noise():
        return [[rng.uniform(-1.0, 1.0) for _ in range(16)] for _ in range(16)]

    noise1 = make_noise()  # large-scale surface variation
    noise2 = make_noise()  # fine grain / mineral texture
    noise3 = make_noise()  # edge irregularity

    # Smooth noise by averaging with neighbors (one pass)
    def smooth(grid):
        out = [[0.0] * 16 for _ in range(16)]
        for sy in range(16):
            for sx in range(16):
                total = grid[sy][sx] * 2.0
                count = 2.0
                for dsy in (-1, 0, 1):
                    for dsx in (-1, 0, 1):
                        if dsy == 0 and dsx == 0:
                            continue
                        sy2 = max(0, min(15, sy + dsy))
                        sx2 = max(0, min(15, sx + dsx))
                        total += grid[sy2][sx2]
                        count += 1.0
                out[sy][sx] = total / count
        return out

    sn1 = smooth(noise1)  # smooth large-scale
    sn3 = smooth(noise3)  # smooth edge noise

    # --- Pre-place mineral flecks (bright spots in the rock) ---
    fleck_set = set()
    for _ in range(8):
        fx = rng.randint(4, 11)
        fy = rng.randint(4, 11)
        fleck_set.add((fx, fy))

    # --- Pre-place hot spots near the leading edge (bottom of texture) ---
    hotspot_set = set()
    for _ in range(4):
        hx = rng.randint(5, 10)
        hy = rng.randint(10, 13)
        hotspot_set.add((hx, hy))

    rows = []
    # Center slightly off for natural asymmetry; leading edge is bottom
    cx, cy = 7.3, 7.5
    # Base radius of the rock (will be distorted by noise)
    base_radius = 5.8

    for y in range(16):
        row = []
        for x in range(16):
            dx = x - cx
            dy = y - cy
            d = math.sqrt(dx * dx + dy * dy)
            angle = math.atan2(dy, dx)

            # Irregular rocky boundary: distort radius with noise
            edge_noise = sn3[y][x] * 1.4 + math.sin(angle * 5.0) * 0.5
            # Slightly larger toward the bottom (trailing ablation shape)
            ablation_stretch = -0.4 * (dy / 8.0)  # negative dy = top, shrink slightly
            effective_radius = base_radius + edge_noise + ablation_stretch

            if d >= effective_radius:
                # Outside the rock — fully transparent
                row.append((0, 0, 0, 0))
                continue

            # --- Determine how deep inside the rock this pixel is ---
            # 0.0 = at the very edge, 1.0 = at the center
            depth = max(0.0, min(1.0, 1.0 - d / effective_radius))

            # === Base rock color: dark grey with slight brown tint ===
            # Remember: vertex tint (1.0, 0.7, 0.2) multiplies these values.
            # We want the final appearance to be dark brownish-grey rock.
            # A medium grey (120,120,120) * (1.0,0.7,0.2) = (120,84,24) — dark brown. Good.
            # A lighter grey (180,180,180) * tint = (180,126,36) — warm brown. Good for heated edges.

            # Large-scale surface variation
            surface_var = sn1[y][x] * 18
            # Fine grain noise (unsmoothed for gritty texture)
            grain = noise2[y][x] * 12

            # Core rock: darker in the middle, lighter toward edges (heating)
            if depth > 0.55:
                # Deep interior — dark rocky grey
                base_v = 95 + surface_var + grain
                r = cl(base_v + 5)   # very slight warm bias
                g = cl(base_v - 2)
                b = cl(base_v - 8)   # slightly less blue for warmth
            elif depth > 0.3:
                # Mid region — transitioning to heated
                t = (0.55 - depth) / 0.25  # 0 at deep side, 1 at edge side
                base_v = 95 + t * 55 + surface_var + grain
                r = cl(base_v + 10 + t * 20)
                g = cl(base_v - 2 + t * 5)
                b = cl(base_v - 8 - t * 10)
            else:
                # Outer heated rim — brighter, will glow orange through tint
                t = (0.3 - depth) / 0.3  # 0 at mid, 1 at very edge
                base_v = 150 + t * 70 + surface_var * 0.6 + grain * 0.5
                r = cl(base_v + 25 + t * 30)
                g = cl(base_v + 5)
                b = cl(base_v - 20 - t * 30)

            # === Mineral flecks: bright spots that will glow through the tint ===
            if (x, y) in fleck_set and depth > 0.35:
                fleck_bright = rng.uniform(30, 60)
                r = cl(r + fleck_bright)
                g = cl(g + fleck_bright * 0.8)
                b = cl(b + fleck_bright * 0.4)

            # === Hot spots near leading edge (bottom): white-hot ===
            if (x, y) in hotspot_set and depth > 0.15:
                r = cl(240 + grain)
                g = cl(235 + grain)
                b = cl(210 + grain)

            # === Dark cracks/veins: occasional dark lines in the rock ===
            # Use a simple threshold on fine noise to create crack-like features
            crack_val = noise2[y][x] + sn1[y][x] * 0.3
            if crack_val < -0.75 and depth > 0.4:
                r = cl(r * 0.55)
                g = cl(g * 0.5)
                b = cl(b * 0.45)

            row.append((cl(r), cl(g), cl(b), 255))
        rows.append(row)
    return rows

def make_arcane_barrier_texture():
    """16x16 animated arcane barrier — 8 frames, subtle shimmer.
    Translucent blue-white with faint rune-like pattern."""
    NF_BARRIER = 8
    all_rows = []
    for frame in range(NF_BARRIER):
        phase = frame / NF_BARRIER * math.pi * 2
        for y in range(16):
            row = []
            for x in range(16):
                # Base translucent blue
                base_a = 60
                # Shimmer wave across the block
                shimmer = math.sin(x * 0.8 + y * 0.5 + phase) * 0.5 + 0.5
                # Rune-like grid pattern
                grid = 0
                if x % 4 == 0 or y % 4 == 0:
                    grid = 15
                # Diamond highlight
                cx_d = abs(x - 7.5)
                cy_d = abs(y - 7.5)
                diamond = max(0, 1.0 - (cx_d + cy_d) / 10.0)
                r = cl(140 + shimmer * 40 + diamond * 30)
                g = cl(180 + shimmer * 30 + diamond * 25)
                b = cl(230 + shimmer * 25)
                a = cl(base_a + shimmer * 30 + grid + diamond * 20)
                row.append((min(r, 255), min(g, 255), min(b, 255), min(a, 150)))
            all_rows.append(row)
    return all_rows

def make_return_portal_texture():
    """16x16 animated return portal — 8 frames, swirling purple-indigo shimmer."""
    NF = 8
    all_rows = []
    for frame in range(NF):
        phase = frame / NF * math.pi * 2
        for y in range(16):
            row = []
            for x in range(16):
                # Radial distance from center
                cx_d = x - 7.5
                cy_d = y - 7.5
                dist = math.sqrt(cx_d * cx_d + cy_d * cy_d) / 10.0
                angle = math.atan2(cy_d, cx_d)
                # Swirling pattern
                swirl = math.sin(angle * 3 + dist * 5 + phase) * 0.5 + 0.5
                spiral = math.sin(angle * 2 - dist * 8 + phase * 1.5) * 0.3 + 0.5
                # Center glow
                glow = max(0, 1.0 - dist * 1.2)
                r = cl(80 + swirl * 60 + glow * 50)
                g = cl(40 + spiral * 30 + glow * 30)
                b = cl(160 + swirl * 50 + spiral * 40 + glow * 40)
                a = cl(80 + glow * 60 + swirl * 30)
                row.append((min(r, 255), min(g, 255), min(b, 255), min(a, 200)))
            all_rows.append(row)
    return all_rows

def make_disintegrate_particle():
    """8x8 particle — angular green force shard / ember for Disintegrate spell."""
    W, H = 8, 8
    cx, cy = 3.5, 3.5  # center

    # Define an angular shard shape using a rotated diamond with asymmetric arms
    # The shape mask gives a value 0.0-1.0 indicating how "inside" each pixel is
    rows = []
    for y in range(H):
        row = []
        for x in range(W):
            dx = x - cx
            dy = y - cy

            # Rotated diamond distance (angular/crystalline feel)
            # Use a mix of L1 (diamond) and rotated L1 for a shard shape
            angle = 0.45  # slight rotation so it's not axis-aligned
            rx = dx * math.cos(angle) - dy * math.sin(angle)
            ry = dx * math.sin(angle) + dy * math.cos(angle)

            # Diamond (L1) distance in rotated space — gives angular shape
            d_diamond = abs(rx) + abs(ry)

            # Elongate slightly along one axis for shard feel
            d_shard = abs(rx * 0.8) + abs(ry * 1.2)

            # Use the minimum of the two for a more crystalline outline
            d = min(d_diamond, d_shard)

            # Normalize: core is at d=0, outer edge ~3.5
            if d < 0.8:
                # Bright core — searing green-white
                r, g, b = 180, 255, 180
                a = 255
            elif d < 1.6:
                # Inner glow — vivid green
                t = (d - 0.8) / 0.8
                r = cl(180 - t * 130)
                g = cl(255 - t * 30)
                b = cl(180 - t * 150)
                a = cl(255 - t * 30)
            elif d < 2.6:
                # Outer glow — darker sickly green, fading alpha
                t = (d - 1.6) / 1.0
                r = cl(50 - t * 35)
                g = cl(225 - t * 100)
                b = cl(30 - t * 15)
                a = cl(225 - t * 140)
            elif d < 3.5:
                # Faint halo — very dark green, mostly transparent
                t = (d - 2.6) / 0.9
                r = cl(15 - t * 10)
                g = cl(125 - t * 100)
                b = cl(15 - t * 10)
                a = cl(85 - t * 85)
            else:
                r, g, b, a = 0, 0, 0, 0

            row.append((r, g, b, a))
        rows.append(row)
    return rows


def make_dancing_light_particle():
    """8x8 particle — warm golden fairy light orb for Dancing Lights spell."""
    W, H = 8, 8
    cx, cy = 3.5, 3.5  # center

    rows = []
    for y in range(H):
        row = []
        for x in range(W):
            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx * dx + dy * dy)

            # Slight warmth variation — shift hue slightly based on angle
            angle = math.atan2(dy, dx)
            warm_shift = math.sin(angle * 2.0 + 0.7) * 0.12  # subtle variation

            if dist < 1.0:
                # White-yellow hot core
                r, g, b = 255, 252, 220
                a = 255
            elif dist < 2.0:
                # Bright golden yellow
                t = (dist - 1.0) / 1.0
                r = cl(255 - t * 15)
                g = cl(252 - t * 60 - warm_shift * 30)
                b = cl(220 - t * 140)
                a = cl(255 - t * 25)
            elif dist < 3.0:
                # Warm amber/orange
                t = (dist - 2.0) / 1.0
                r = cl(240 - t * 50 + warm_shift * 20)
                g = cl(192 - t * 80 - warm_shift * 15)
                b = cl(80 - t * 50)
                a = cl(230 - t * 100)
            elif dist < 4.0:
                # Fading warm orange to transparent
                t = (dist - 3.0) / 1.0
                r = cl(190 - t * 120 + warm_shift * 15)
                g = cl(112 - t * 80)
                b = cl(30 - t * 25)
                a = cl(130 - t * 130)
            else:
                r, g, b, a = 0, 0, 0, 0

            row.append((r, g, b, a))
        rows.append(row)
    return rows


def make_rotten_flesh_block_texture():
    """16x16 rotten flesh block — styled after vanilla rotten flesh item colors.
    Pinkish-red compressed meat look with subtle green rot patches and fiber texture.
    References the vanilla rotten_flesh item palette: salmon-pink base, darker red-brown
    shadows, olive-green rot spots."""
    W, H = 16, 16
    rng = random.Random(0xF1E5)

    # Palette — matches vanilla rotten flesh item colors closely
    FLESH_LIGHT = (190, 110, 95)   # lighter flesh (salmon-pink)
    FLESH_MID   = (165, 85, 70)    # mid flesh (the dominant color)
    FLESH_DARK  = (130, 60, 50)    # shadow flesh
    ROT_OLIVE   = (110, 105, 60)   # olive-green rot
    ROT_DARK    = (85, 80, 45)     # darker rot

    # Fill with randomized base flesh tones
    pixels = [[None for _ in range(W)] for _ in range(H)]
    for y in range(H):
        for x in range(W):
            v = rng.random()
            if v < 0.35:
                pixels[y][x] = FLESH_LIGHT
            elif v < 0.75:
                pixels[y][x] = FLESH_MID
            else:
                pixels[y][x] = FLESH_DARK

    # Horizontal fiber lines — subtle darker streaks
    for fy in (1, 4, 7, 10, 13):
        for x in range(W):
            if rng.random() < 0.8:
                r, g, b = pixels[fy][x]
                pixels[fy][x] = (max(0, r - 20), max(0, g - 15), max(0, b - 12))

    # Rot patches — small olive-green blobs
    rot_spots = [
        [(3,2),(4,2),(3,3),(4,3),(5,3)],                    # top-left
        [(10,5),(11,5),(10,6),(11,6),(12,6)],                # center-right
        [(6,11),(7,11),(5,12),(6,12),(7,12),(6,13)],         # bottom-center
        [(13,9),(14,9),(13,10)],                             # right edge
    ]
    for patch in rot_spots:
        for px, py in patch:
            pixels[py][px] = ROT_OLIVE if rng.random() < 0.6 else ROT_DARK

    # Edge shading — single-pixel border darken
    for y in range(H):
        for x in range(W):
            d = min(x, y, W-1-x, H-1-y)
            if d == 0:
                r, g, b = pixels[y][x]
                pixels[y][x] = (max(0, r - 25), max(0, g - 18), max(0, b - 15))

    # Per-pixel noise for organic variation
    rows = []
    for y in range(H):
        row = []
        for x in range(W):
            r, g, b = pixels[y][x]
            n = rng.randint(-6, 6)
            row.append((cl(r + n), cl(g + n), cl(b + n), 255))
        rows.append(row)
    return rows


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

    print("=== Crystallized skeleton overlay ===")
    save_png(os.path.join("src","main","resources","assets","reactivefluids","textures","entity",
             "crystallized_skeleton_overlay.png"), make_crystal_skeleton_overlay())

    print("=== Crystal block texture ===")
    save_png(os.path.join(BLK, "crystal_block.png"), make_crystal_block())

    print("=== Reagent item textures ===")
    save_png(os.path.join(ITM, "acid_reagent.png"),  make_reagent((220, 40, 30)))   # red acid
    save_png(os.path.join(ITM, "base_reagent.png"),  make_reagent((60, 40, 200)))   # blue/purple base

    print("=== Tower scroll texture ===")
    save_png(os.path.join(ITM, "tower_scroll.png"), make_tower_scroll())

    print("=== Steed scroll texture ===")
    save_png(os.path.join(ITM, "steed_scroll.png"), make_steed_scroll())

    print("=== Dancing lights scroll texture ===")
    save_png(os.path.join(ITM, "dancing_lights_scroll.png"), make_dancing_lights_scroll())

    print("=== Disintegrate scroll texture ===")
    save_png(os.path.join(ITM, "disintegrate_scroll.png"), make_disintegrate_scroll())

    print("=== New spell scroll textures ===")
    save_png(os.path.join(ITM, "mold_earth_scroll.png"), make_mold_earth_scroll())
    save_png(os.path.join(ITM, "wall_of_stone_scroll.png"), make_wall_of_stone_scroll())
    save_png(os.path.join(ITM, "passwall_scroll.png"), make_passwall_scroll())
    save_png(os.path.join(ITM, "dimension_door_scroll.png"), make_dimension_door_scroll())
    save_png(os.path.join(ITM, "conjure_animals_scroll.png"), make_conjure_animals_scroll())
    save_png(os.path.join(ITM, "reverse_gravity_scroll.png"), make_reverse_gravity_scroll())

    print("=== New batch spell scroll textures ===")
    save_png(os.path.join(ITM, "plant_growth_scroll.png"), make_plant_growth_scroll())
    save_png(os.path.join(ITM, "fog_cloud_scroll.png"), make_fog_cloud_scroll())
    save_png(os.path.join(ITM, "erupting_earth_scroll.png"), make_erupting_earth_scroll())
    save_png(os.path.join(ITM, "tiny_hut_scroll.png"), make_tiny_hut_scroll())
    save_png(os.path.join(ITM, "bones_of_the_earth_scroll.png"), make_bones_of_the_earth_scroll())
    save_png(os.path.join(ITM, "move_earth_scroll.png"), make_move_earth_scroll())
    save_png(os.path.join(ITM, "arcane_gate_scroll.png"), make_arcane_gate_scroll())
    save_png(os.path.join(ITM, "meteor_swarm_scroll.png"), make_meteor_swarm_scroll())
    save_png(os.path.join(ITM, "control_water_scroll.png"), make_control_water_scroll())
    save_png(os.path.join(ITM, "magnificent_mansion_scroll.png"), make_magnificent_mansion_scroll())
    save_png(os.path.join(ITM, "gongers_grotto_scroll.png"), make_gongers_grotto_scroll())
    save_png(os.path.join(ITM, "raise_dead_scroll.png"), make_raise_dead_scroll())

    print("=== Spectral wolf entity texture ===")
    save_png(os.path.join("src","main","resources","assets","reactivefluids","textures","entity",
             "spectral_wolf.png"), make_spectral_wolf_texture())

    print("=== Phantom steed entity texture ===")
    save_png(os.path.join("src","main","resources","assets","reactivefluids","textures","entity",
             "phantom_steed.png"), make_phantom_steed_texture())

    PRT = os.path.join("src","main","resources","assets","reactivefluids","textures","particle")
    print("=== Particle textures ===")
    save_png(os.path.join(PRT, "disintegrate_particle.png"), make_disintegrate_particle())
    save_png(os.path.join(PRT, "dancing_light_particle.png"), make_dancing_light_particle())
    save_png(os.path.join(PRT, "spectral_particle.png"), make_spectral_particle())
    save_png(os.path.join(PRT, "fog_cloud_particle.png"), make_fog_cloud_particle())
    necrotic_frames = make_necrotic_particle_frames()
    for i, frame_rows in enumerate(necrotic_frames):
        save_png(os.path.join(PRT, f"necrotic_particle_{i}.png"), frame_rows)

    print("=== Disintegrate beam texture ===")
    ENT = os.path.join("src","main","resources","assets","reactivefluids","textures","entity")
    save_png(os.path.join(ENT, "disintegrate_beam.png"), make_disintegrate_beam())

    print("=== Meteor entity texture ===")
    save_png(os.path.join(ENT, "meteor.png"), make_meteor_texture())

    print("=== Arcane barrier block texture (animated) ===")
    BLK_DIR = os.path.join("src","main","resources","assets","reactivefluids","textures","block")
    barrier_path = os.path.join(BLK_DIR, "arcane_barrier.png")
    save_png(barrier_path, make_arcane_barrier_texture())
    save_mcmeta(barrier_path, 4, True)

    print("=== Return portal block texture (animated) ===")
    portal_path = os.path.join(BLK_DIR, "return_portal.png")
    save_png(portal_path, make_return_portal_texture())
    save_mcmeta(portal_path, 4, True)

    print("=== Rotten flesh block texture ===")
    save_png(os.path.join(BLK_DIR, "rotten_flesh_block.png"), make_rotten_flesh_block_texture())

    print("=== Raised zombie entity texture ===")
    save_png(os.path.join(ENT, "raised_zombie.png"), make_raised_zombie_texture())

    print("=== Raised skeleton entity texture ===")
    save_png(os.path.join(ENT, "raised_skeleton.png"), make_raised_skeleton_texture())

    print("\nDone.")

if __name__ == "__main__":
    main()
