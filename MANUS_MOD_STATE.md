# Viva Piñata Minecraft Mod — Current State

## Overview

This NeoForge 1.21.1 Minecraft mod recreates the Viva Piñata: Trouble in Paradise (VP:TIP) experience inside Minecraft. Players create gardens, attract piñata creatures, breed them, evolve them, and defend against threats.

The mod lives inside the `reactivefluids` mod as a subpackage (`com.reactivefluids.pinata`). All piñata code is self-contained in that package.

---

## What's Built (as of latest commit)

### 64 Piñata Species (of 88 VP total)

Each species has:
- Entity class extending `BasePinataEntity` (which extends `Animal`)
- Renderer class (using shared model shapes with unique textures)
- Spawn egg item
- Candy food item (dropped when piñata is broken)
- Placeholder texture (normal + sour variant)
- Lang entries
- VP-accurate stats (HP, speed, attack, armor)
- Species-specific food preferences (visit, resident, romance foods)
- AI goals (predator hunting, block attraction, item eating, conflict)

#### Complete Species List

| # | Species | Animal | HP | Speed | Key Mechanic |
|---|---------|--------|-----|-------|-------------|
| 1 | Whirlm | Worm | 8 | 0.20 | Simplest, attracted to dirt/grass |
| 2 | Sparrowmint | Sparrow | 12 | 0.30 | Hunts Whirlms, evolves→Candary |
| 3 | Fudgehog | Hedgehog | 14 | 0.22 | Attracted to tall grass, 4 armor, evolves→Parmadillo |
| 4 | Mousemallow | Mouse | 6 | 0.35 | Fastest species, prey for many |
| 5 | Syrupent | Snake | 16 | 0.28 | Multi-segment model, hunts Mousemallows |
| 6 | Taffly | Fly | 6 | 0.30 | Attracted to flowers, buzz hover, evolves→Reddhott |
| 7 | Bunnycomb | Rabbit | 10 | 0.33 | Fast breeder, flees Pretztail |
| 8 | Quackberry | Duck | 12 | 0.25 | Aquatic, evolves→Juicygoose |
| 9 | Shellybean | Snail | 12 | 0.12 | Slowest, 6 armor, slime trail |
| 10 | Newtgat | Newt | 10 | 0.25 | Semi-aquatic, conflicts Lickatoad, evolves→Salamango |
| 11 | Lickatoad | Frog | 14 | 0.25 | Hunts Tafflies, hopping movement |
| 12 | Pretztail | Fox | 18 | 0.35 | Hunts Bunnycombs + Mousemallows, evolves→Pieena |
| 13 | Buzzlegum | Bee | 12 | 0.28 | Produces honeycomb every 5min, conflicts Raisant |
| 14 | Cluckles | Chicken | 10 | 0.25 | Lays eggs, evolves→Chocstrich |
| 15 | Horstachio | Horse | 30 | 0.30 | Largest basic, evolves→Zumbug |
| 16 | Barkbark | Dog | 16 | 0.32 | Loyal, conflicts with Kittyfloss |
| 17 | Kittyfloss | Cat | 14 | 0.33 | Hunts Mousemallows, conflicts Barkbark |
| 18 | Goobaa | Sheep | 14 | 0.22 | Produces wool periodically |
| 19 | Rashberry | Pig | 16 | 0.23 | Part of Pigxie cross-breed recipe |
| 20 | Doenut | Deer | 18 | 0.30 | Shy (flees players), evolves→Moojoo |
| 21 | Squazzil | Squirrel | 10 | 0.35 | Collects saplings, high jumper |
| 22 | Sweetooth | Bear | 24 | 0.24 | Omnivore, loves honey |
| 23 | Mallowolf | Wolf | 20 | 0.33 | Howl scares Ruffians, sour variant |
| 24 | Cocoadile | Crocodile | 28 | 0.22 | Aquatic apex, 6 armor, sour variant |
| 25 | Dragonache | Dragon | 50 | 0.28 | Fire immune, 8 armor, drops diamonds, scares Professor Pester |
| 26 | Elephanilla | Elephant | 40 | 0.20 | Largest non-dragon, 6 armor |
| 27 | Chewnicorn | Unicorn | 30 | 0.30 | Magical healer concept |
| 28 | Roario | Lion | 26 | 0.30 | Conflicts with Tigermisu |
| 29 | Tigermisu | Tiger | 24 | 0.32 | Conflicts with Roario |
| 30 | Parrybo | Parrot | 10 | 0.28 | 4 color variants |
| 31 | Swanana | Swan | 16 | 0.25 | Aquatic, part of Pigxie recipe |
| 32 | Eaglair | Eagle | 20 | 0.30 | Hunts Squazzils |
| 33 | Badgesicle | Badger | 16 | 0.25 | 3 armor burrower |
| 34 | Hootyfruity | Owl | 12 | 0.22 | Nocturnal, hunts Mousemallows |
| 35 | Dragumfly | Dragonfly | 8 | 0.35 | Fast flyer, water attracted |
| 36 | Candary | Canary | 14 | 0.32 | Evolved from Sparrowmint |
| 37 | Parmadillo | Armadillo | 18 | 0.22 | Evolved from Fudgehog, 8 armor |
| 38 | Zumbug | Zebra | 28 | 0.32 | Evolved from Horstachio |
| 39 | Pieena | Hyena | 20 | 0.33 | Evolved from Pretztail, hunts Bunnycombs |
| 40 | Juicygoose | Goose | 16 | 0.27 | Evolved from Quackberry, aquatic |
| 41 | Salamango | Salamander | 14 | 0.28 | Evolved from Newtgat, fire immune |
| 42 | Reddhott | Firefly | 10 | 0.30 | Evolved from Taffly, fire immune |
| 43 | Chocstrich | Ostrich | 18 | 0.35 | Evolved from Cluckles |
| 44 | Moojoo | Highland Cow | 20 | 0.20 | Evolved from Doenut, produces milk |
| 45 | Cinnamonkey | Monkey | 14 | 0.33 | Agile tree-dweller |
| 46 | Sarsgorilla | Gorilla | 30 | 0.25 | Apex land predator, 4 armor |
| 47 | Camello | Camel | 24 | 0.25 | Desert, attracted to sand |
| 48 | Pengum | Penguin | 12 | 0.22 | Arctic, attracted to snow |
| 49 | Walrusk | Walrus | 28 | 0.18 | Arctic aquatic, 5 armor |
| 50 | Polollybear | Polar Bear | 30 | 0.26 | Arctic apex, evolved from Fizzlybear |
| 51 | Fizzlybear | Brown Bear | 22 | 0.24 | Evolves→Polollybear |
| 52 | Limeoceros | Rhino | 35 | 0.22 | 8 armor, fights Professor Pester |
| 53 | Pigxie | Pig-Fairy | 16 | 0.28 | Cross-breed of Rashberry + Swanana |
| 54 | Fourheads | Hydra | 40 | 0.20 | Four-headed, 5 armor |
| 55 | Twingersnap | Two-headed Snake | 20 | 0.30 | Hunts Mousemallows |
| 56 | Choclodocus | Dinosaur | 45 | 0.18 | 6 armor, scares Professor Pester |
| 57 | Jameleon | Chameleon | 10 | 0.25 | 6 color variants |
| 58 | Geckie | Gecko | 8 | 0.30 | Desert lizard |
| 59 | Jeli | Jellyfish | 8 | 0.15 | Aquatic, translucent |
| 60 | Custacean | Crab | 12 | 0.20 | 5 armor, beach dweller |
| 61 | Mothdrop | Moth | 6 | 0.28 | Nocturnal, attracted to light |
| 62 | Sweetle | Beetle | 8 | 0.20 | 3 armor, 4 variants |
| 63 | Raisant | Ant | 4 | 0.30 | Tiny, conflicts with Buzzlegum |
| 64 | Cherrapin | Turtle | 14 | 0.15 | Aquatic, 7 armor |

### NPCs (6)

| NPC | Role | Key Behavior |
|-----|------|-------------|
| Seedos | Seed giver | Right-click for 1-3 random seeds, VP dialogue, 60s cooldown |
| Storkos | Egg delivery | Cosmetic bird that flies in after romance |
| Doc Patchingo | Doctor | Heals sick piñatas for payment, auto-heals critical cases |
| Ruffian | Hostile invader | Breaks fences, attacks piñatas, bribable, shovel scares |
| Dastardos | Sick piñata reaper | Spawns when piñata sick 60s+, kills on contact, 100HP |
| Professor Pester | Main boss | 200HP, targets most valuable piñata, bribable with gold |

### Core Systems

#### Garden System
- **GardenPlotBlock**: place to create a garden center
- **GardenManager**: SavedData tracking all gardens, scans block counts
- **GardenTickHandler**: every 10 seconds per garden:
  - Scans blocks (grass, water, flowers, long grass, sand, snow)
  - Spawns attracted piñatas based on VP conditions
  - Spawns Ruffians (2% chance, garden level 2+)
  - Spawns Dastardos when piñata sick 60s+
  - Applies house happiness bonuses

#### Piñata Lifecycle
1. **Wild** → feeds on visit food → **Visitor** → feeds on resident food → **Resident**
2. Residents persist, have happiness system, can romance
3. Sour piñatas can be tamed by feeding resident food

#### Romance
- Feed romance food to a resident → sets "romancing" state
- Two romancing same-species residents trigger PinataRomanceGoal
- 5-second dance with hearts + note particles
- Baby spawned with celebration fireworks
- 5-minute cooldown per parent

#### Evolution (9 active paths)
- Sparrowmint→Candary (dandelion), Fudgehog→Parmadillo (cocoa beans)
- Horstachio→Zumbug (sweet berries), Pretztail→Pieena (bone)
- Quackberry→Juicygoose (sweet berries), Newtgat→Salamango (blaze powder)
- Cluckles→Chocstrich (cactus), Doenut→Moojoo (spruce sapling)
- Fizzlybear→Polollybear (lapis lazuli)

#### Food Chain (predator→prey)
- Sparrowmint→Whirlm, Syrupent→Mousemallow, Pretztail→Bunnycomb
- Lickatoad→Taffly, Kittyfloss→Mousemallow, Eaglair→Squazzil
- Hootyfruity→Mousemallow, Mallowolf→Bunnycomb, Pieena→Bunnycomb
- Twingersnap→Mousemallow

#### Species Conflicts
- Barkbark vs Kittyfloss, Newtgat vs Lickatoad
- Roario vs Tigermisu, Raisant vs Buzzlegum

#### Sickness
- Happiness ≤ 10 = sick (particles, Dastardos timer)
- Halo of Hardness accessory auto-heals
- Doc Patchingo NPC heals for payment

#### Economy
- **Chocolate coins**: universal currency
- **19 accessories** across 9 body slots (Head, Eyes, Ears, Nose, Mouth, Neck, Arms, Body, Feet)
- Special effects: Auto-Produce, Auto-Heal, Speed Boost, Value Boost

### Tools
| Tool | Function |
|------|----------|
| Garden Shovel | Grass→dirt, dirt→farmland, shift+dirt→water, whack piñatas |
| Watering Can | Grow crops, hydrate farmland, calm piñatas, extinguish fire |
| Grass Packet | Convert terrain to grass (infinite use) |
| Long Grass Packet | Convert to grass + place short grass |
| Sand Packet | Convert terrain to sand |
| Snow Packet | Convert terrain to snow |

### Buildings
| Building | Function |
|----------|----------|
| Garden Plot | Creates garden, right-click for stats + boundary particles |
| Honey Hive | Buzzlegum produces honeycomb |
| Milking Shed | (Future) Moozipan produces milk |
| Shearing Shed | (Future) Goobaa produces wool |
| Piñata Houses (×6) | Passive +2 happiness per 10s to matching species |

### AI Goals (shared, reusable)
| Goal | Purpose |
|------|---------|
| HuntPreyGoal | Predator pathfinds to and eats prey species |
| AttractedToBlockGoal | Periodic scan + pathfind toward specific blocks |
| EatItemEntityGoal | Find and consume food items on ground |
| PinataRomanceGoal | Dance + breed mechanic |
| SourBehaviorGoal | Hostile sour piñata actions (destroy flowers, attack, start fights) |
| SpeciesConflictGoal | Two enemy species auto-fight when near |

---

## File Structure

```
src/main/java/com/reactivefluids/pinata/
├── BasePinataEntity.java          — Base class for all piñatas (lifecycle, happiness, accessories, romance, sickness)
├── PinataEvolution.java           — Evolution/transformation rule registry
├── PinataAccessory.java           — Accessory system (19 items, 9 slots, special effects)
├── AccessoryItem.java             — Item that equips onto piñatas
├── ChocolateCoinItem.java         — Currency item
├── GardenShovelItem.java          — Terrain editing tool
├── WateringCanItem.java           — Plant growth + piñata calming tool
├── SurfacePacketItem.java         — Terrain painting (4 variants)
├── ModPinataEntities.java         — All 70 entity type registrations
├── ModPinataItems.java            — All 157 item registrations
├── [Species]Entity.java           — 64 species entity classes
├── [Species]Renderer.java         — 64 species renderers
├── [Species]Model.java            — 9 unique model classes (shared across species)
├── SeedosEntity.java              — Seed-giving NPC
├── StorkosEntity.java             — Egg delivery NPC
├── DocPatchingoEntity.java        — Piñata doctor NPC
├── RuffianEntity.java             — Hostile invader NPC
├── DastardosEntity.java           — Sick piñata reaper
├── ProfessorPesterEntity.java     — Main antagonist boss
├── ai/
│   ├── HuntPreyGoal.java
│   ├── AttractedToBlockGoal.java
│   ├── EatItemEntityGoal.java
│   ├── PinataRomanceGoal.java
│   ├── SourBehaviorGoal.java
│   └── SpeciesConflictGoal.java
└── garden/
    ├── GardenManager.java         — SavedData, block scanning, piñata census
    ├── GardenPlotBlock.java       — Garden center marker
    ├── GardenTickHandler.java     — Attraction spawning, threat spawning, house bonuses
    ├── ProduceBuildingBlock.java   — Honey Hive / Milking Shed / Shearing Shed
    └── PinataHouseBlock.java      — Per-species happiness boost houses
```

---

## What's NOT Built Yet

### Missing Species (24 of 88)
Bonboon, Bispotti, Buzzenge, Chippopotamus, Crowla, Galagoogoo, Hoghurt, Lackatoad, Lemmoning, Moozipan, Peckanmix, Ponocky, Profitamole, Pudgeon, Robean, S'morepion, Sherbat, Smelba, Tartridge, Vulchurro, Arocknid, Flutterscotch (White/Red), Flapyak

### Missing Features
- Custom per-species models (currently 9 shared shapes)
- GeckoLib integration for proper animations
- Piñata journal/encyclopedia GUI
- Trick Stick item
- Desert/Arctic trapping system
- More piñata houses (only 6 of 64 species)
- Costolot's Store / Paper Pets NPC shops
- Willy Builder NPC
- Bart's item transformation workshop
- Captain's Cutlass item
- Tower of Sour multiblock
- Garden level-up rewards/milestones
- Biome modifier spawning (natural world spawns)
- Data pack support for species config
