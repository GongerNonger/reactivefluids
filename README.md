# Reactive Fluids

A NeoForge 1.21.1 Minecraft mod that started as a colored reactive fluid system and has grown into a multi-feature creative mod with chemistry-inspired fluids, D&D spell scrolls, and an ambitious Viva Pinata recreation — all in one package.

## What's in the Mod

### Reactive Fluid System (master branch)
The original core of the mod. Three color families of fluids — **Amber**, **Cobalt**, and **Jade** — each with resin (slow, viscous) and hardener (fast-flowing) variants. Throw hardener at resin of the same color and it reacts, converting up to 1024 connected blocks into solid **epoxy** via BFS flood-fill. Epoxy comes in transparent, glowing, and solid opaque variants.

Additional fluid experiments include:
- **Elephant's Toothpaste** — hydrogen peroxide + catalyst = expanding foam eruption
- **Acid** — dissolves stone downward, exposing ores
- **Bioluminescent Plankton** — glows when disturbed, particle eruptions when ignited
- **Crystal Solution** — grows crystal structures

### D&D Spell Scrolls (master branch)
A set of consumable magic scrolls inspired by D&D spells, each with custom entities, particles, and world-altering effects:
- **Raise Dead** — reanimates skeletons and zombies as loyal undead
- **Phantom Steed** — summons a rideable spectral horse
- **Disintegrate** — fires a beam that vaporizes blocks and entities
- **Meteor Swarm** — rains fireballs from the sky
- **Dancing Lights**, **Fog Cloud**, **Control Water**, **Plant Growth**, **Mold Earth**, and more
- **Necrotic Particle** — custom 11-frame skull animation for necromancy effects

### Viva Pinata Mod (viva-pinata branch)
A large-scale recreation of Viva Pinata: Trouble in Paradise as a Minecraft mod. Currently in active development with 64 species implemented, garden mechanics, romance/breeding, economy, threats, and evolution systems. See [docs/viva-pinata.md](docs/viva-pinata.md) for full details.

### Experimental Features (testing-new-fluids branch)
- **Moonlight Jellyfish** — bioluminescent swimming mob with GeckoLib animation
- **Lava Lamp Blocks** — decorative animated blocks
- **Custom Mushroom Blocks** — 6 fantasy mushroom variants

## Project Structure

```
reactivefluids/
  src/main/java/com/reactivefluids/
    pinata/                    # Viva Pinata entities, models, AI, garden system
      ai/                     # Custom AI goals (hunting, romance, conflicts)
      garden/                  # Garden management, plots, houses, produce buildings
      *.java                   # 64 entity classes, 15 models, 64 renderers, NPCs
    *.java                     # Reactive fluids, spell scrolls, core mod classes
  src/main/resources/
    assets/reactivefluids/     # Textures, models, blockstates, lang, particles
    data/reactivefluids/       # Loot tables, recipes, tags
  generate_textures.py         # Procedural fluid/particle texture generator
  generate_pinata_resources.py # Generates all pinata blockstates, models, textures
  generate_pinata_models.py    # UV-mapped entity texture painter
  CLAUDE.md                    # AI assistant context file
  VIVA_PINATA_DESIGN.md        # Viva Pinata design document
  docs/                        # Detailed documentation
```

## Branches

| Branch | Purpose | Status |
|--------|---------|--------|
| `master` | Reactive fluids + spell scrolls | Stable, builds and runs |
| `claude/viva-pinata-minecraft-mod-WshX2` | Viva Pinata mod | In development, compiles, placeholder textures |
| `testing-new-fluids` | Jellyfish, lava lamps, mushrooms | Experimental |

## Build & Run

Requires Java 21 (Eclipse Adoptium recommended).

```bash
cd /path/to/reactivefluids
JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.10.7-hotspot" ./gradlew build
```

The built jar lands in `build/libs/reactivefluids-1.0.0.jar`.

### Deploy to CurseForge instances

```bash
# Master branch (reactive fluids + spells)
cp build/libs/reactivefluids-1.0.0.jar "/path/to/curseforge/Instances/Reactive Fluids Dev/mods/"

# Viva Pinata branch
cp build/libs/reactivefluids-1.0.0.jar "/path/to/curseforge/Instances/viva pinata test/mods/"
```

### Texture Generation

Fluid and particle textures are procedurally generated:
```bash
python generate_textures.py          # Fluids, epoxy, buckets, particles
python generate_pinata_resources.py  # Pinata blockstates, models, placeholder textures
python generate_pinata_models.py     # UV-mapped entity textures (Whirlm, Sparrowmint, Fudgehog)
```

## Tech Stack

- **Minecraft** 1.21.1
- **NeoForge** 21.1.172
- **Java** 21
- **Gradle** 8.8 with `net.neoforged.gradle.userdev` 7.0.145
- **GeckoLib** (testing branch only, for jellyfish animation)
- **Python 3** (texture generation scripts, stdlib only)

## Documentation

- [Project History & Efforts](docs/project-history.md) — Timeline of all development efforts
- [Reactive Fluids System](docs/reactive-fluids.md) — Fluid mechanics, reactions, textures
- [Spell Scrolls](docs/spell-scrolls.md) — D&D magic system
- [Viva Pinata Mod](docs/viva-pinata.md) — Species, garden, AI, economy, roadmap
- [Texture Pipeline](docs/texture-pipeline.md) — How textures are generated and structured
- [Long-Term Vision](docs/vision.md) — Where this project is headed

## License

Private project. Not currently published.
