"""
Generate all missing blockstate, model, and texture resources for the Viva Pinata mod content.
"""
import os, json, struct, zlib, math, random

MOD_ID = "reactivefluids"
BASE = "src/main/resources/assets/reactivefluids"

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

def save_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
    print(f"  {path}")

def cl(v): return max(0, min(255, int(round(v))))

# ---- Block definitions ----
BLOCKS = [
    "garden_plot",
    "whirlm_house", "sparrowmint_house", "fudgehog_house",
    "bunnycomb_house", "horstachio_house", "buzzlegum_house",
]

# Block colors for textures
BLOCK_COLORS = {
    "garden_plot":       (120, 85, 50),    # Rich brown soil
    "whirlm_house":      (180, 120, 80),   # Warm wood brown
    "sparrowmint_house": (100, 160, 90),   # Mint green
    "fudgehog_house":    (140, 90, 60),    # Fudge brown
    "bunnycomb_house":   (220, 200, 120),  # Honey yellow
    "horstachio_house":  (160, 140, 110),  # Pistachio tan
    "buzzlegum_house":   (220, 180, 60),   # Bee yellow
}

# ---- Spawn egg items (use vanilla spawn egg model) ----
SPAWN_EGGS = [
    "whirlm_spawn_egg", "sparrowmint_spawn_egg", "fudgehog_spawn_egg",
    "mousemallow_spawn_egg", "syrupent_spawn_egg", "taffly_spawn_egg",
    "bunnycomb_spawn_egg", "quackberry_spawn_egg", "shellybean_spawn_egg",
    "newtgat_spawn_egg", "lickatoad_spawn_egg", "pretztail_spawn_egg",
    "buzzlegum_spawn_egg", "cluckles_spawn_egg", "horstachio_spawn_egg",
    "barkbark_spawn_egg", "kittyfloss_spawn_egg", "goobaa_spawn_egg",
    "rashberry_spawn_egg", "doenut_spawn_egg",
    "squazzil_spawn_egg", "sweetooth_spawn_egg", "mallowolf_spawn_egg",
    "cocoadile_spawn_egg", "dragonache_spawn_egg",
    "elephanilla_spawn_egg", "chewnicorn_spawn_egg", "camello_spawn_egg",
    "fourheads_spawn_egg", "choclodocus_spawn_egg", "geckie_spawn_egg",
    "jameleon_spawn_egg", "salamango_spawn_egg", "twingersnap_spawn_egg",
    "dragumfly_spawn_egg",
    "badgesicle_spawn_egg", "candary_spawn_egg", "cherrapin_spawn_egg",
    "chocstrich_spawn_egg", "cinnamonkey_spawn_egg", "custacean_spawn_egg",
    "eaglair_spawn_egg", "fizzlybear_spawn_egg", "hootyfruity_spawn_egg",
    "jeli_spawn_egg", "juicygoose_spawn_egg", "limoceros_spawn_egg",
    "moojoo_spawn_egg", "mothdrop_spawn_egg", "parrybo_spawn_egg",
    "parmadillo_spawn_egg", "pengum_spawn_egg", "pieena_spawn_egg",
    "pigxie_spawn_egg", "polollybear_spawn_egg", "raisant_spawn_egg",
    "reddhott_spawn_egg", "roario_spawn_egg", "sarsgorilla_spawn_egg",
    "swanana_spawn_egg", "sweetle_spawn_egg", "tigermisu_spawn_egg",
    "walrusk_spawn_egg", "zumbug_spawn_egg",
]

# ---- Candy items ----
CANDY_ITEMS = [
    "whirlm_candy", "sparrowmint_candy", "fudgehog_candy",
    "mousemallow_candy", "syrupent_candy", "taffly_candy",
    "bunnycomb_candy", "quackberry_candy", "shellybean_candy",
    "newtgat_candy", "lickatoad_candy", "pretztail_candy",
    "buzzlegum_candy", "cluckles_candy", "horstachio_candy",
    "barkbark_candy", "kittyfloss_candy", "goobaa_candy",
    "rashberry_candy", "doenut_candy",
    "squazzil_candy", "sweetooth_candy", "mallowolf_candy",
    "cocoadile_candy", "dragonache_candy",
    "elephanilla_candy", "chewnicorn_candy", "camello_candy",
    "fourheads_candy", "choclodocus_candy", "geckie_candy",
    "jameleon_candy", "salamango_candy", "twingersnap_candy",
    "dragumfly_candy",
    "badgesicle_candy", "candary_candy", "cherrapin_candy",
    "chocstrich_candy", "cinnamonkey_candy", "custacean_candy",
    "eaglair_candy", "fizzlybear_candy", "hootyfruity_candy",
    "jeli_candy", "juicygoose_candy", "limoceros_candy",
    "moojoo_candy", "mothdrop_candy", "parrybo_candy",
    "parmadillo_candy", "pengum_candy", "pieena_candy",
    "pigxie_candy", "polollybear_candy", "raisant_candy",
    "reddhott_candy", "roario_candy", "sarsgorilla_candy",
    "swanana_candy", "sweetle_candy", "tigermisu_candy",
    "walrusk_candy", "zumbug_candy",
]

# ---- Other items ----
OTHER_ITEMS = [
    "garden_shovel", "watering_can", "chocolate_coin",
    "top_hat", "crown", "halo_of_hardness", "keeper_hat",
    "running_shoes", "bow_tie",
    "grass_packet", "long_grass_packet", "sand_packet", "snow_packet",
    "honey_hive", "milking_shed", "shearing_shed",
]

# Candy colors (roughly matching piñata species)
CANDY_COLORS = {
    "whirlm": (200, 140, 180),
    "sparrowmint": (120, 200, 140),
    "fudgehog": (160, 100, 60),
    "mousemallow": (240, 220, 200),
    "syrupent": (180, 140, 60),
    "taffly": (200, 200, 80),
    "bunnycomb": (255, 200, 100),
    "quackberry": (100, 160, 220),
    "shellybean": (180, 220, 160),
    "newtgat": (180, 130, 80),
    "lickatoad": (100, 180, 100),
    "pretztail": (200, 160, 100),
    "buzzlegum": (240, 200, 60),
    "cluckles": (220, 180, 140),
    "horstachio": (160, 200, 120),
    "barkbark": (160, 120, 80),
    "kittyfloss": (220, 180, 220),
    "goobaa": (240, 240, 220),
    "rashberry": (200, 60, 80),
    "doenut": (180, 140, 100),
    "squazzil": (200, 140, 80),
    "sweetooth": (160, 100, 60),
    "mallowolf": (180, 180, 200),
    "cocoadile": (100, 140, 60),
    "dragonache": (180, 100, 200),
    "elephanilla": (200, 200, 180),
    "chewnicorn": (220, 180, 240),
    "camello": (200, 180, 140),
    "fourheads": (100, 160, 100),
    "choclodocus": (140, 100, 60),
    "geckie": (120, 200, 120),
    "jameleon": (100, 200, 180),
    "salamango": (240, 120, 60),
    "twingersnap": (120, 160, 60),
    "dragumfly": (160, 200, 240),
    "badgesicle": (180, 180, 180),
    "candary": (255, 240, 100),
    "cherrapin": (200, 100, 100),
    "chocstrich": (140, 100, 60),
    "cinnamonkey": (180, 120, 60),
    "custacean": (220, 140, 100),
    "eaglair": (180, 160, 140),
    "fizzlybear": (200, 180, 140),
    "hootyfruity": (160, 180, 120),
    "jeli": (180, 200, 240),
    "juicygoose": (160, 200, 100),
    "limoceros": (140, 200, 80),
    "moojoo": (240, 220, 200),
    "mothdrop": (200, 180, 220),
    "parrybo": (100, 200, 200),
    "parmadillo": (200, 180, 140),
    "pengum": (80, 80, 100),
    "pieena": (200, 160, 120),
    "pigxie": (240, 180, 200),
    "polollybear": (220, 220, 240),
    "raisant": (60, 60, 40),
    "reddhott": (220, 60, 40),
    "roario": (220, 180, 80),
    "sarsgorilla": (120, 100, 80),
    "swanana": (240, 240, 220),
    "sweetle": (120, 200, 160),
    "tigermisu": (220, 160, 60),
    "walrusk": (140, 140, 160),
    "zumbug": (60, 60, 80),
}

ITEM_COLORS = {
    "garden_shovel": (140, 140, 140),
    "watering_can": (100, 140, 200),
    "chocolate_coin": (200, 170, 60),
    "top_hat": (40, 40, 40),
    "crown": (220, 200, 60),
    "halo_of_hardness": (240, 240, 200),
    "keeper_hat": (60, 120, 60),
    "running_shoes": (200, 80, 60),
    "bow_tie": (200, 60, 60),
    "grass_packet": (80, 160, 60),
    "long_grass_packet": (60, 140, 40),
    "sand_packet": (220, 200, 160),
    "snow_packet": (230, 240, 250),
    "honey_hive": (220, 180, 60),
    "milking_shed": (200, 200, 200),
    "shearing_shed": (180, 160, 140),
}


def make_block_texture(color, name):
    """16x16 block texture with some noise and detail."""
    rng = random.Random(hash(name) & 0xFFFFFFFF)
    r, g, b = color
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            n = rng.randint(-15, 15)
            # Add a border/edge effect
            edge = 0
            if x == 0 or x == 15 or y == 0 or y == 15:
                edge = -20
            elif x == 1 or x == 14 or y == 1 or y == 14:
                edge = -8
            pr = cl(r + n + edge)
            pg = cl(g + n + edge)
            pb = cl(b + n + edge)
            row.append((pr, pg, pb, 255))
        rows.append(row)
    return rows


def make_candy_texture(color, name):
    """16x16 candy item texture — wrapped candy shape."""
    rng = random.Random(hash(name) & 0xFFFFFFFF)
    r, g, b = color
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            # Candy body: oval in center
            cx, cy = 7.5, 7.5
            dx = (x - cx) / 5.0
            dy = (y - cy) / 3.5
            d = dx*dx + dy*dy

            # Wrapper twist ends
            in_twist = False
            if (3 <= y <= 12):
                if (x <= 2 and 6 <= y <= 9) or (x >= 13 and 6 <= y <= 9):
                    in_twist = True

            if d < 1.0:
                n = rng.randint(-10, 10)
                # Highlight
                hi = max(0, 1.0 - d) * 30 if y < 8 else 0
                pr = cl(r + n + hi)
                pg = cl(g + n + hi)
                pb = cl(b + n + hi)
                row.append((pr, pg, pb, 255))
            elif in_twist:
                n = rng.randint(-8, 8)
                # Wrapper: lighter version of color
                pr = cl(min(255, r + 40) + n)
                pg = cl(min(255, g + 40) + n)
                pb = cl(min(255, b + 40) + n)
                row.append((pr, pg, pb, 255))
            else:
                row.append((0, 0, 0, 0))
        rows.append(row)
    return rows


def make_item_texture(color, name):
    """16x16 generic item texture — simple shape with color."""
    rng = random.Random(hash(name) & 0xFFFFFFFF)
    r, g, b = color
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            cx, cy = 7.5, 7.5
            dx = (x - cx) / 6.0
            dy = (y - cy) / 6.0
            d = dx*dx + dy*dy
            if d < 1.0:
                n = rng.randint(-12, 12)
                hi = max(0, 0.5 - d) * 40
                pr = cl(r + n + hi)
                pg = cl(g + n + hi)
                pb = cl(b + n + hi)
                row.append((pr, pg, pb, 255))
            else:
                row.append((0, 0, 0, 0))
        rows.append(row)
    return rows


def generate_all():
    # ---- Blockstates ----
    print("=== Blockstates ===")
    for block in BLOCKS:
        save_json(f"{BASE}/blockstates/{block}.json", {
            "variants": {
                "": {"model": f"{MOD_ID}:block/{block}"}
            }
        })

    # ---- Block models ----
    print("=== Block models ===")
    for block in BLOCKS:
        save_json(f"{BASE}/models/block/{block}.json", {
            "parent": "minecraft:block/cube_all",
            "textures": {
                "all": f"{MOD_ID}:block/{block}"
            }
        })

    # ---- Block item models ----
    print("=== Block item models ===")
    for block in BLOCKS:
        save_json(f"{BASE}/models/item/{block}.json", {
            "parent": f"{MOD_ID}:block/{block}"
        })

    # ---- Block textures ----
    print("=== Block textures ===")
    for block in BLOCKS:
        color = BLOCK_COLORS.get(block, (128, 128, 128))
        rows = make_block_texture(color, block)
        save_png(f"{BASE}/textures/block/{block}.png", rows)

    # ---- Spawn egg item models ----
    print("=== Spawn egg models ===")
    for egg in SPAWN_EGGS:
        save_json(f"{BASE}/models/item/{egg}.json", {
            "parent": "minecraft:item/template_spawn_egg"
        })

    # ---- Candy item models + textures ----
    print("=== Candy items ===")
    for candy in CANDY_ITEMS:
        save_json(f"{BASE}/models/item/{candy}.json", {
            "parent": "minecraft:item/generated",
            "textures": {
                "layer0": f"{MOD_ID}:item/{candy}"
            }
        })
        # Generate texture if it doesn't exist
        tex_path = f"{BASE}/textures/item/{candy}.png"
        if not os.path.exists(tex_path):
            species = candy.replace("_candy", "")
            color = CANDY_COLORS.get(species, (180, 140, 100))
            rows = make_candy_texture(color, candy)
            save_png(tex_path, rows)

    # ---- Other item models + textures ----
    print("=== Other items ===")
    for item in OTHER_ITEMS:
        # BlockItems (houses, hive, sheds) that are also in BLOCKS are already handled
        if item in BLOCKS:
            continue
        save_json(f"{BASE}/models/item/{item}.json", {
            "parent": "minecraft:item/generated",
            "textures": {
                "layer0": f"{MOD_ID}:item/{item}"
            }
        })
        tex_path = f"{BASE}/textures/item/{item}.png"
        if not os.path.exists(tex_path):
            color = ITEM_COLORS.get(item, (160, 160, 160))
            rows = make_item_texture(color, item)
            save_png(tex_path, rows)

    # ---- Produce building block items (honey_hive, milking_shed, shearing_shed) ----
    # These are registered as BlockItems but we need blockstates too
    PRODUCE_BLOCKS = ["honey_hive", "milking_shed", "shearing_shed"]
    print("=== Produce building blocks ===")
    for block in PRODUCE_BLOCKS:
        # Check if registered as a block
        bs_path = f"{BASE}/blockstates/{block}.json"
        if not os.path.exists(bs_path):
            save_json(bs_path, {
                "variants": {"": {"model": f"{MOD_ID}:block/{block}"}}
            })
            save_json(f"{BASE}/models/block/{block}.json", {
                "parent": "minecraft:block/cube_all",
                "textures": {"all": f"{MOD_ID}:block/{block}"}
            })
            save_json(f"{BASE}/models/item/{block}.json", {
                "parent": f"{MOD_ID}:block/{block}"
            })
            color = ITEM_COLORS.get(block, (160, 160, 160))
            rows = make_block_texture(color, block)
            save_png(f"{BASE}/textures/block/{block}.png", rows)

    # ---- Entity textures (placeholder colored rectangles for pinata species) ----
    # Texture size must match the model's LayerDefinition.create(mesh, W, H)
    # Species → model mapping determines required texture size
    MODEL_TEX_SIZES = {
        'WhirlmModel': (64, 32),
        'SparrowmintModel': (32, 32),
        'FudgehogModel': (48, 32),
        'HorstachioModel': (64, 32),
        'PretztailModel': (32, 32),
        'NewtgatModel': (32, 16),
        'MousemallowModel': (32, 16),
        'BunnycombModel': (32, 32),
        'BuzzlegumModel': (32, 32),
        'ClucklesModel': (32, 32),
        'LickatoadModel': (32, 16),
        'QuackberryModel': (32, 32),
        'ShellybeanModel': (32, 16),
        'SyrupentModel': (32, 32),
        'TafflyModel': (32, 16),
    }
    SPECIES_MODEL = {
        'whirlm': 'WhirlmModel',
        'sparrowmint': 'SparrowmintModel', 'candary': 'SparrowmintModel',
        'chocstrich': 'SparrowmintModel', 'eaglair': 'SparrowmintModel',
        'hootyfruity': 'SparrowmintModel', 'parrybo': 'SparrowmintModel',
        'pengum': 'SparrowmintModel',
        'fudgehog': 'FudgehogModel', 'badgesicle': 'FudgehogModel',
        'fizzlybear': 'FudgehogModel', 'goobaa': 'FudgehogModel',
        'limeoceros': 'FudgehogModel', 'moojoo': 'FudgehogModel',
        'parmadillo': 'FudgehogModel', 'polollybear': 'FudgehogModel',
        'rashberry': 'FudgehogModel', 'sarsgorilla': 'FudgehogModel',
        'sweetooth': 'FudgehogModel', 'walrusk': 'FudgehogModel',
        'horstachio': 'HorstachioModel', 'camello': 'HorstachioModel',
        'chewnicorn': 'HorstachioModel', 'choclodocus': 'HorstachioModel',
        'dragonache': 'HorstachioModel', 'elephanilla': 'HorstachioModel',
        'zumbug': 'HorstachioModel',
        'pretztail': 'PretztailModel', 'barkbark': 'PretztailModel',
        'doenut': 'PretztailModel', 'kittyfloss': 'PretztailModel',
        'mallowolf': 'PretztailModel', 'pieena': 'PretztailModel',
        'roario': 'PretztailModel', 'tigermisu': 'PretztailModel',
        'newtgat': 'NewtgatModel', 'geckie': 'NewtgatModel',
        'jameleon': 'NewtgatModel', 'salamango': 'NewtgatModel',
        'mousemallow': 'MousemallowModel', 'cinnamonkey': 'MousemallowModel',
        'pigxie': 'MousemallowModel', 'raisant': 'MousemallowModel',
        'squazzil': 'MousemallowModel',
        'bunnycomb': 'BunnycombModel',
        'buzzlegum': 'BuzzlegumModel',
        'cluckles': 'ClucklesModel',
        'lickatoad': 'LickatoadModel',
        'quackberry': 'QuackberryModel', 'juicygoose': 'QuackberryModel',
        'swanana': 'QuackberryModel',
        'shellybean': 'ShellybeanModel', 'cherrapin': 'ShellybeanModel',
        'custacean': 'ShellybeanModel', 'jeli': 'ShellybeanModel',
        'sweetle': 'ShellybeanModel',
        'syrupent': 'SyrupentModel', 'cocoadile': 'SyrupentModel',
        'fourheads': 'SyrupentModel', 'twingersnap': 'SyrupentModel',
        'taffly': 'TafflyModel', 'dragumfly': 'TafflyModel',
        'mothdrop': 'TafflyModel', 'reddhott': 'TafflyModel',
        'limoceros': 'FudgehogModel',
    }
    print("=== Entity textures ===")
    entity_dir = f"{BASE}/textures/entity/pinata"
    os.makedirs(entity_dir, exist_ok=True)
    species_list = list(CANDY_COLORS.keys())
    for species in species_list:
        tex_path = f"{entity_dir}/{species}.png"
        if not os.path.exists(tex_path):
            color = CANDY_COLORS.get(species, (160, 160, 160))
            model = SPECIES_MODEL.get(species, 'FudgehogModel')
            tw, th = MODEL_TEX_SIZES.get(model, (32, 16))
            rng = random.Random(hash(species) & 0xFFFFFFFF)
            r, g, b = color
            rows = []
            for y in range(th):
                row = []
                for x in range(tw):
                    n = rng.randint(-10, 10)
                    row.append((cl(r + n), cl(g + n), cl(b + n), 255))
                rows.append(row)
            save_png(tex_path, rows)

    # Also need NPC textures
    for npc in ["professor_pester", "storkos", "doc_patchingo", "seedos", "dastardos", "ruffian"]:
        tex_path = f"{entity_dir}/{npc}.png"
        if not os.path.exists(tex_path):
            rng = random.Random(hash(npc) & 0xFFFFFFFF)
            rows = []
            for y in range(32):
                row = []
                for x in range(64):
                    row.append((cl(100 + rng.randint(-20, 20)),
                                cl(80 + rng.randint(-20, 20)),
                                cl(120 + rng.randint(-20, 20)), 255))
                rows.append(row)
            save_png(tex_path, rows)

    print("\nDone!")


if __name__ == "__main__":
    generate_all()
