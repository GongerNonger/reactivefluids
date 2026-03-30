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

    print("=== Bucket item textures ===")
    for name in BUCKET_PALETTES:
        save_png(os.path.join(ITM, f"{name}_bucket.png"), make_bucket(name))

    print("\nDone.")

if __name__ == "__main__":
    main()
