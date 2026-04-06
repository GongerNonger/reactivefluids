# Reactive Fluids

A NeoForge 1.21.1 Minecraft mod that adds reactive fluid chemistry and a D&D-inspired spell scroll system.

---

## Reactive Fluid Systems

### Resin + Hardener → Epoxy

The core mechanic. Pour **resin** into the world, then throw a **hardener** at it to trigger a reaction. The hardener is a throwable item (like a splash potion) — on impact it BFS flood-fills up to 1024 connected resin blocks, converting them instantly to solid **epoxy**.

Three color families:

| Family | Resin | Glowing Resin | Epoxy Products |
|--------|-------|---------------|----------------|
| **Amber** | Orange/gold, slow-flowing | Same + emits light 8 | Transparent / Glowing (light 12) / Solid |
| **Cobalt** | Deep blue/cyan, slow-flowing | Same + emits light 8 | Transparent / Glowing (light 12) / Solid |
| **Jade** | Deep green/lime, slow-flowing | Same + emits light 8 | Transparent / Glowing (light 12) / Solid |

- **Transparent epoxy** — glass-like, see-through
- **Glowing epoxy** — translucent + emits light level 12
- **Solid epoxy** — stone-like, opaque, strongest variant

When epoxy forms from a reaction, a burst of CLOUD and POOF particles fires from the block.

---

### Elephant's Toothpaste

Pour **hydrogen peroxide** and **potassium iodide** into the same space. They react to produce a **foam block** that erupts violently upward, growing up to 20 blocks tall and blooming outward into a cone/mushroom shape as it rises.

---

### Bioluminescent Plankton

A dark, inert fluid that **glows when living things move through it**. Light cascades outward to neighboring plankton blocks (up to 3 blocks radius), then fades back to dark over a few seconds. Light level ranges 0–12 depending on proximity to the disturbance.

---

### Acid

A corrosive fluid that **drills straight down** through stone, dirt, sand, gravel, and most natural blocks. It stops at ores (leaving them exposed), bedrock, obsidian, and reinforced deepslate. Emits smoke and cloud particles as it dissolves.

---

### Crystal Solution

A fluid that slowly **grows crystal formations** on adjacent solid surfaces. If a skeleton stands in it long enough (30 seconds), it converts into a **Crystallized Skeleton** — a hostile mob variant that fires crystal lasers.

---

### Rainbow pH Indicator

A fluid that **changes color based on pH** — green (neutral) by default, shifting through yellow/orange/red toward acid, or blue/indigo/violet toward base. Throw an **Acid Reagent** at it to shift acidic, or a **Base Reagent** to shift basic. The shift flood-fills through connected indicator fluid.

| pH | Color |
|----|-------|
| 0 | Red (most acidic) |
| 1 | Orange |
| 2 | Yellow |
| 3 | Green (neutral) |
| 4 | Blue |
| 5 | Indigo |
| 6 | Violet (most basic) |

---

## Spell Scrolls

A WIP system of 22 consumable spell scrolls inspired by D&D 5e. Right-click to cast. Most are single-use.

### Terrain & Construction

| Scroll | Effect |
|--------|--------|
| **Mold Earth** | Excavates a 3×3×3 cube of soft earth (stacks to 16) |
| **Erupting Earth** | Violently erupts blocks upward in a small explosion |
| **Bones of the Earth** | Erupts stone pillars from the ground |
| **Move Earth** | Raises or lowers soft terrain (sneak to lower) |
| **Wall of Stone** | Places a permanent stone brick wall |
| **Passwall** | Creates a temporary tunnel through solid walls |
| **Tiny Hut** | Conjures a glass dome shelter lasting 10 minutes |
| **Galder's Tower** | Builds a furnished two-story stone tower lasting 1 day |

### Summoning

| Scroll | Effect |
|--------|--------|
| **Conjure Animals** | Summons spectral wolves, foxes, and axolotls to fight for you (2 min) |
| **Find Steed** | Conjures a rideable phantom horse (10 min) |
| **Raise Dead** | Cast on a Rotten Flesh Block or Bone Block to raise a zombie or skeleton that fights for you |
| **Dancing Lights** | Conjures 4 orbiting magical lights that follow you (1 min) |

### Teleportation & Dimensions

| Scroll | Effect |
|--------|--------|
| **Dimension Door** | Teleports you up to 64 blocks in the direction you're looking |
| **Arcane Gate** | Two-use — first cast places an entry portal, second cast places the exit |
| **Magnificent Mansion** | Opens a portal to a pocket dimension furnished mansion (1 day) |
| **Gonger's Grotto** | Opens a portal to a magical island grotto pocket dimension (1 day) |

### Combat & Utility

| Scroll | Effect |
|--------|--------|
| **Disintegrate** | Fires a green ray that destroys the targeted block or entity |
| **Meteor Swarm** | Rains 4 devastating meteors from the sky |
| **Reverse Gravity** | Launches all nearby creatures skyward |
| **Fog Cloud** | Creates a blinding fog that inflicts Blindness on hostile mobs |
| **Control Water** | Parts a body of water for 60 seconds |
| **Plant Growth** | Causes rapid growth in vegetation across a wide area |

---

## Building the Mod

Requires Java 21. Gradle's toolchain will auto-detect it on your machine — no `JAVA_HOME` needed:

```bash
./gradlew build
```

Output: `build/libs/reactivefluids-1.0.0.jar`

### Regenerating Textures

After changing fluid colors or adding new fluids, regenerate all textures with:

```bash
python generate_textures.py
```

No pip dependencies required — uses Python stdlib only.

---

## Technical Notes

- **NeoForge 21.1.172**, Minecraft 1.21.1, Java 21
- **Mod ID:** `reactivefluids`
- All fluid blocks use a custom `TranslucentLiquidBlock` to guarantee translucent rendering — NeoForge's default fluid pass can override render layer assignments set at event time.
- Fluid transparency comes from **texture alpha values**, not the ARGB tint color.
- The hardener is a **throwable item**, not a fluid — there are no hardener fluid blocks.
- Spell scrolls use Minecraft's `SavedData` system for persistent timed effects (portals, structures, summoned creatures).
