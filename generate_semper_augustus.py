"""Generate a 16x16 pixel art texture for the Semper Augustus tulip.

The Semper Augustus was the most famous tulip of Dutch Tulip Mania (1637).
White petals with vivid crimson/red flame streaks, green stem and leaves.
Uses the cross model (two diagonal planes), so the texture shows one side view.
"""
import struct
import zlib

def write_png(path, pixels, w, h):
    """Write an RGBA PNG from a flat list of (r,g,b,a) tuples."""
    def chunk(ctype, data):
        c = ctype + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xFFFFFFFF)

    raw = b''
    for y in range(h):
        raw += b'\x00'  # filter none
        for x in range(w):
            r, g, b, a = pixels[y * w + x]
            raw += struct.pack('BBBB', r, g, b, a)

    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)  # 8-bit RGBA
    return sig + chunk(b'IHDR', ihdr) + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b'')

# Color palette
T = (0, 0, 0, 0)           # transparent
STEM      = (58, 95, 35, 255)
STEM_DARK = (42, 72, 25, 255)
LEAF      = (72, 120, 42, 255)
LEAF_DARK = (52, 90, 30, 255)
WHITE     = (245, 240, 235, 255)
WHITE_SH  = (215, 210, 205, 255)  # white shadow
CREAM     = (235, 225, 210, 255)
RED       = (180, 25, 35, 255)    # crimson flame
RED_DEEP  = (140, 18, 28, 255)    # deeper crimson
RED_LIGHT = (210, 50, 55, 255)    # lighter crimson edge
PETAL_TIP = (230, 230, 220, 255)  # bright petal tip

# 16x16 grid, row 0 = top
# The tulip: stem at bottom, bulb/petals in upper half
# Semper Augustus: white petals with bold red flame streaks
grid = [
    #  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
    [T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T],  # row 0
    [T, T, T, T, T, T, T, PETAL_TIP, PETAL_TIP, T, T, T, T, T, T, T],  # row 1
    [T, T, T, T, T, T, PETAL_TIP, WHITE, WHITE, PETAL_TIP, T, T, T, T, T, T],  # row 2
    [T, T, T, T, T, WHITE, RED_LIGHT, WHITE, WHITE, RED_LIGHT, WHITE, T, T, T, T, T],  # row 3
    [T, T, T, T, T, WHITE, RED, WHITE_SH, WHITE_SH, RED, WHITE, T, T, T, T, T],  # row 4
    [T, T, T, T, T, WHITE_SH, RED, RED_DEEP, RED_DEEP, RED, WHITE_SH, T, T, T, T, T],  # row 5
    [T, T, T, T, T, CREAM, RED_DEEP, WHITE_SH, WHITE_SH, RED_DEEP, CREAM, T, T, T, T, T],  # row 6
    [T, T, T, T, T, T, WHITE_SH, RED, RED, WHITE_SH, T, T, T, T, T, T],  # row 7
    [T, T, T, T, T, T, T, STEM, STEM, T, T, T, T, T, T, T],  # row 8
    [T, T, T, T, T, T, T, STEM, STEM, T, T, T, T, T, T, T],  # row 9
    [T, T, T, T, T, T, T, STEM, STEM, LEAF, LEAF, T, T, T, T, T],  # row 10
    [T, T, T, T, T, T, T, STEM, STEM_DARK, T, LEAF, LEAF_DARK, T, T, T, T],  # row 11
    [T, T, T, T, T, T, T, STEM, STEM, T, T, T, T, T, T, T],  # row 12
    [T, T, T, T, T, LEAF_DARK, LEAF, STEM, STEM, T, T, T, T, T, T, T],  # row 13
    [T, T, T, T, LEAF_DARK, T, T, STEM_DARK, STEM, T, T, T, T, T, T, T],  # row 14
    [T, T, T, T, T, T, T, STEM_DARK, STEM_DARK, T, T, T, T, T, T, T],  # row 15
]

pixels = []
for row in grid:
    pixels.extend(row)

data = write_png(None, pixels, 16, 16)
out = 'src/main/resources/assets/reactivefluids/textures/block/semper_augustus.png'
with open(out, 'wb') as f:
    f.write(data)
print(f'Wrote {out} ({len(data)} bytes)')
