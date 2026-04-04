# Viva Piñata: Minecraft Mod — Design Document

## Overview

This mod brings the Viva Piñata: Trouble in Paradise (TIP) experience to Minecraft.
Players attract, tame, breed, and care for piñata creatures in their gardens. The core
loop: modify your garden (plants, terrain, decorations) → attract wild piñatas → feed
them to become residents → romance them to breed → collect candy and unlock rarer species.

---

## Implementation Phases

### Phase 1 — Core Piñatas (CURRENT)
Build the entity framework and first 10-15 starter species. Focus on the basic
lifecycle: wild → visitor → resident, feeding, candy drops on death.

**Starter species (in order of implementation):**
1. **Whirlm** (worm) — DONE ✓
2. **Sparrowmint** (sparrow) — eats Whirlms
3. **Fudgehog** (hedgehog) — attracted to long grass
4. **Mousemallow** (mouse) — attracted to turnip heads
5. **Taffly** (fly) — attracted to fruit/flowers
6. **Syrupent** (snake) — attracted to long grass, eats Mousemallows
7. **Bunnycomb** (rabbit) — attracted to carrots/daisies
8. **Quackberry** (duck) — needs water
9. **Shellybean** (snail) — eats flowers
10. **Newtgat** (newt) — needs water + land
11. **Lickatoad** (frog) — eats Tafflies
12. **Pretztail** (fox) — eats Bunnycombs
13. **Buzzlegum** (bee) — attracted to flowers, produces honey
14. **Cluckles** (chicken) — attracted to seeds
15. **Horstachio** (horse) — large, attracted to apples

### Phase 2 — Garden & Romance
- Garden boundary system (claimed area tracking)
- Gardener level/rank XP
- Romance mechanic (simplified: meet requirements → hearts → egg → baby)
- Piñata houses (block entities)
- Storkos NPC (egg delivery)
- Plants & seeds system (custom growable crops)

### Phase 3 — Conflict & Threats
- Species conflict pairs and fight AI
- Sour piñatas (8 species with taming quests)
- Ruffians (hostile NPCs)
- Dastardos (sick piñata hunter)
- Sickness mechanic
- Tower of Sour multiblock

### Phase 4 — Economy & Production
- Chocolate coin currency
- NPC shops (Costolot's, Paper Pets, Willy Builder, Bart)
- Accessories (9 slots per piñata)
- Produce buildings (Honey Hive, Milking Shed, Shearing Shed, Mine)
- Item transformation (Bart's Workshop)
- Tool upgrades (shovel tiers, watering can)

### Phase 5 — Advanced
- Evolution/transformation system (11 known evolutions)
- Professor Pester boss
- Desert/Arctic biome trapping
- Full 88-species roster
- Wildcard variants & twins
- Trick Stick

---

## Complete Species List (88 Standard Species)

| # | Species | Based On | Size | Category |
|---|---------|----------|------|----------|
| 1 | Whirlm | Worm | S | Herbivore |
| 2 | Sparrowmint | Sparrow | S | Predator |
| 3 | Fudgehog | Hedgehog | S | Herbivore |
| 4 | Mousemallow | Mouse | S | Herbivore |
| 5 | Taffly | Fly | S | Herbivore |
| 6 | Syrupent | Snake | M | Predator |
| 7 | Bunnycomb | Rabbit | S | Herbivore |
| 8 | Quackberry | Duck | S | Aquatic |
| 9 | Shellybean | Snail | S | Herbivore |
| 10 | Newtgat | Newt | S | Aquatic |
| 11 | Lickatoad | Frog | S | Predator |
| 12 | Pretztail | Fox | M | Predator |
| 13 | Buzzlegum | Bee | S | Herbivore |
| 14 | Cluckles | Chicken | S | Herbivore |
| 15 | Horstachio | Horse | L | Herbivore |
| 16 | Arocknid | Spider | M | Predator |
| 17 | Badgesicle | Badger | M | Predator |
| 18 | Barkbark | Dog | M | Predator |
| 19 | Bispotti | Dalmatian | M | Predator |
| 20 | Bonboon | Baboon | M | Predator |
| 21 | Buzzenge | Wasp | S | Predator |
| 22 | Camello | Camel | L | Herbivore |
| 23 | Candary | Canary | S | Herbivore |
| 24 | Cherrapin | Turtle | M | Aquatic |
| 25 | Chewnicorn | Unicorn | L | Herbivore |
| 26 | Chippopotamus | Hippo | L | Aquatic |
| 27 | Choclodocus | Dinosaur | L | Herbivore |
| 28 | Chocstrich | Ostrich | L | Herbivore |
| 29 | Cinnamonkey | Monkey | M | Herbivore |
| 30 | Cocoadile | Crocodile | L | Predator |
| 31 | Crowla | Crow | S | Predator |
| 32 | Custacean | Crab | S | Aquatic |
| 33 | Doenut | Deer | M | Herbivore |
| 34 | Dragonache | Dragon | L | Legendary |
| 35 | Dragumfly | Dragonfly | S | Predator |
| 36 | Eaglair | Eagle | M | Predator |
| 37 | Elephanilla | Elephant | L | Herbivore |
| 38 | Fizzlybear | Brown Bear | L | Predator |
| 39 | Flapyak | Yak | L | Herbivore |
| 40 | Fourheads | Hydra | L | Legendary |
| 41 | Galagoogoo | Bushbaby | S | Herbivore |
| 42 | Geckie | Gecko | S | Predator |
| 43 | Goobaa | Sheep | M | Herbivore |
| 44 | Hoghurt | Warthog | M | Herbivore |
| 45 | Hootyfruity | Owl | M | Predator |
| 46 | Jameleon | Chameleon | S | Predator |
| 47 | Jeli | Jellyfish | S | Aquatic |
| 48 | Juicygoose | Goose | M | Aquatic |
| 49 | Kittyfloss | Cat | M | Predator |
| 50 | Lackatoad | Poison Frog | S | Predator |
| 51 | Lemmoning | Lemming | S | Herbivore |
| 52 | Limeoceros | Rhino | L | Herbivore |
| 53 | Macaraccoon | Raccoon | M | Predator |
| 54 | Mallowolf | Wolf | M | Predator |
| 55 | Moojoo | Highland Cow | L | Herbivore |
| 56 | Moozipan | Cow | L | Herbivore |
| 57 | Mothdrop | Moth | S | Herbivore |
| 58 | Parmadillo | Armadillo | M | Herbivore |
| 59 | Parrybo | Parrot | M | Herbivore |
| 60 | Peckanmix | Toucan | M | Herbivore |
| 61 | Pengum | Penguin | S | Aquatic |
| 62 | Pieena | Hyena | M | Predator |
| 63 | Pigxie | Pig-Fairy | M | Special |
| 64 | Polollybear | Polar Bear | L | Predator |
| 65 | Ponocky | Pony/Zebra | M | Herbivore |
| 66 | Profitamole | Mole | S | Herbivore |
| 67 | Pudgeon | Pigeon | S | Herbivore |
| 68 | Raisant | Ant | S | Herbivore |
| 69 | Rashberry | Pig | M | Herbivore |
| 70 | Reddhott | Firefly | S | Special |
| 71 | Roario | Lion | L | Predator |
| 72 | Robean | Robin | S | Herbivore |
| 73 | S'morepion | Scorpion | M | Predator |
| 74 | Salamango | Salamander | S | Predator |
| 75 | Sarsgorilla | Gorilla | L | Predator |
| 76 | Smelba | Koala/Skunk | M | Herbivore |
| 77 | Squazzil | Squirrel | S | Herbivore |
| 78 | Swanana | Swan | M | Aquatic |
| 79 | Sweetle | Beetle | S | Herbivore |
| 80 | Sweetooth | Bear | L | Herbivore |
| 81 | Tartridge | Partridge | S | Herbivore |
| 82 | Tigermisu | Tiger | L | Predator |
| 83 | Twingersnap | Two-headed Snake | M | Predator |
| 84 | Vulchurro | Vulture | M | Predator |
| 85 | Walrusk | Walrus | L | Aquatic |
| 86 | White Flutterscotch | Butterfly | S | Herbivore |
| 87 | Zumbug | Zebra | L | Herbivore |
| 88 | Red Flutterscotch | Butterfly (Red) | S | Herbivore |

---

## Evolution / Transformation Chart

| Base Species | Evolves Into | How |
|---|---|---|
| Sparrowmint | Candary | Feed a dandelion |
| Horstachio | Zumbug | Feed a daisy + a blackberry |
| Pretztail | Pieena | Feed a bone |
| Quackberry | Juicygoose | Feed a gooseberry |
| Taffly | Reddhott | Catch fire, then extinguish with water |
| Fudgehog | Parmadillo | Feed a coconut |
| Doenut | Moojoo | Feed a fir tree seed |
| Fizzlybear | Polollybear | Feed a blue gem |
| Newtgat | Salamango | Feed a chili |
| Lickatoad | Lackatoad | Feed nightshade berry, then hit with shovel |
| Cluckles | Chocstrich | Feed cactus fruit |

---

## Conflict Pairs (Species That Fight)

| Species A | Species B |
|---|---|
| Arocknid | Reddhott |
| Arocknid | S'morepion |
| Barkbark | Kittyfloss |
| Barkbark | Roario |
| Barkbark | Tigermisu |
| Bonboon | Cinnamonkey |
| Buzzlegum | Raisant |
| Chocstrich | Pieena |
| Chewnicorn | Ponocky |
| Chewnicorn | Zumbug |
| Dragumfly | Reddhott |
| Horstachio | Ponocky |
| Horstachio | Zumbug |
| Juicygoose | Quackberry |
| Juicygoose | Swanana |
| Lackatoad | Salamango |
| Lickatoad | Newtgat |
| Pigxie | Rashberry |
| Pigxie | Swanana |
| Quackberry | Swanana |
| Roario | Tigermisu |

---

## Sour Piñatas (8 Hostile Species)

| Sour Species | Harmful Behavior | Taming Method | Tamed Benefit |
|---|---|---|---|
| Sour Bonboon | Starts fights | Lose fight to Syrupent/Twingersnap | Stops conflicts |
| Sour Cocoadile | Attacks helpers | Feed Sweetooth + Swanana, 160 water | Scares sours, fertilizes |
| Sour Crowla | Eats sick piñatas | Build birdbath, feed medicine | Distracts Dastardos |
| Sour Macaraccoon | Steals eggs | 5 Master Romancer awards, feed Cluckle | Brings items |
| Sour Mallowolf | Prevents visitors | Feed a Pigxie | Scares ruffians |
| Sour Profitamole | Attacks flowers | 2 mushrooms, feed Red Flutterscotch | Drops rare candy |
| Sour Shellybean | Eats seeds | Feed apple seed | Eats weed seeds |
| Sour Sherbat | Drains piñatas | Feed Jack-o'-Lantern | Distracts Dastardos |

---

## Garden Mechanics → Minecraft Mapping

### Garden Boundary
- **VP:** Fixed rectangular area, grows with rank
- **MC:** Place a "Garden Plot" block to claim a chunk-sized area. Upgrade to expand.
  Alternatively, fence-based: any enclosed fence area counts as a garden.

### Surface Types
| VP Surface | MC Block | How to Create |
|---|---|---|
| Grass | Grass Block | Default / surface packet item |
| Long Grass | Tall Grass | Surface packet or bone meal |
| Soil/Dirt | Dirt / Farmland | Shovel right-click on grass |
| Sand | Sand | Surface packet (desert piñata req) |
| Snow | Snow Layer | Surface packet (arctic piñata req) |
| Water/Pond | Water source | Dig with shovel tool |

### Gardener Level
- XP-like system: earn points for first-time romances, taming sours, growing plants
- Each level unlocks: new species attractions, tool upgrades, shop items, garden size
- Stored as player capability data
- Max level: ~50 (simplified from VP's 196)

### Candiosity (Happiness)
- 0-100 integer on each piñata entity
- Increases: feeding favorite foods (+5-15), accessories (+10 each), romance (+15), house (+20)
- Decreases: fights (-10), sour attacks (-15), neglect over time (-1/day)
- At 0: piñata becomes sick (Dastardos timer starts)
- At 100: bonus candy drops, required for some produce

---

## Plants System

### Plant List (47 plants, phased implementation)

**Phase 1 plants (use vanilla blocks where possible):**
- Daisy → Oxeye Daisy
- Dandelion → Dandelion
- Tulip → Tulip
- Sunflower → Sunflower
- Poppy → Poppy
- Carrot → Carrot crop
- Pumpkin → Pumpkin
- Apple Tree → Oak tree
- Corn → Wheat (reskin later)
- Turnip → Beetroot (reskin later)

**Phase 2 custom plants:**
- Buttercup, Bluebell, Orchid, Rose, Snapdragon, Snowdrop, Tiger Lily, Bird of Paradise
- Blackberry Bush, Blueberry Bush, Gooseberry Bush, Holly Bush
- Banana Tree, Fir Tree, Gem Tree, Hazel Tree, Monkeynut Tree, Orange Tree, Palm Tree
- Chili, Garlic, Pea, Radish, Yam
- Nightshade Bush, Poison Ivy (dangerous plants)
- Bullrush, Cactus, Toadstool, Venus Piñata Trap, Watercress

### Growth Mechanic
- Plant seeds on appropriate surface → 3-4 growth stages → mature plant
- Fertilizer items (3 colors) accelerate growth
- Watering can use advances growth stage
- Mature plants produce harvestable items periodically

---

## Tools → Minecraft Items

| VP Tool | MC Item | Behavior |
|---|---|---|
| Shovel | Custom Shovel | Right-click: dig terrain. Left-click entity: whack. Tiered upgrades. |
| Watering Can | Custom Item | Right-click plant: grow. Right-click entity: calm/extinguish. Refills at water. |
| Surface Packets (x4) | Custom Items | Right-click terrain to convert. Infinite durability. |
| Seed Pouch | Bundle-like Item | Container that holds only seeds. |
| Trick Stick | Custom Item | Right-click piñata to teach/perform tricks. |

---

## NPCs

| VP NPC | MC Implementation | Function |
|---|---|---|
| Leafos | Custom Villager | Tutorial tips, advice chat |
| Seedos | Wandering NPC | Gives free random seeds |
| Storkos | Flying Entity | Delivers eggs after romance |
| Doc Patchingo | NPC / Item | Cures sick piñatas |
| Willy Builder | NPC Shop | Sells houses and produce buildings |
| Costolot | NPC Shop | Sells seeds, tools, general items |
| Miss Petula | NPC Shop | Sells accessories |
| Bart | Workbench Block | Item transformation (honey→medicine, etc.) |
| Gretchen Fetchem | NPC Shop | Re-summons previously owned species |
| Langston | NPC Shop | Sells traps for desert/arctic |

---

## Produce System

| Produce | Source Piñata | Building | Trigger |
|---|---|---|---|
| Honey | Buzzlegum | Honey Hive | Feed daisy, direct to hive |
| Milk | Moozipan / Flapyak | Milking Shed | Feed sunflower, direct to shed |
| Wool | Goobaa | Shearing Shed | Direct to shed |
| Gems | Diggerling (helper) | Mine | Periodic generation |

---

## Accessories (9 Slots Per Piñata)

Slots: Head, Eyes, Ears, Nose, Mouth, Neck, Arms, Body, Feet

Each accessory provides:
- +candiosity bonus
- Some are romance requirements for specific species
- Special effects: auto-produce (Keeper Hat), auto-heal (Halo of Hardness)
- Render as additional model layers

---

## Technical Architecture

### Entity Hierarchy
```
Animal (vanilla)
  └── BasePinataEntity (shared lifecycle, happiness, candy drops)
      ├── WhirlmEntity
      ├── SparrowmintEntity
      ├── FudgehogEntity
      └── ... (88 species)
```

### Registration Pattern
- `ModPinataEntities` — DeferredRegister for all piñata EntityTypes
- `ModPinataItems` — spawn eggs, candy items, tools, accessories
- Each entity has a static `createAttributes()` method
- Attributes registered in bulk via `EntityAttributeCreationEvent`

### Rendering
- Custom Java models per species (WhirlmModel, etc.)
- Consider GeckoLib migration for complex models with keyframe animation
- Sour variants use alternate textures
- Accessories render as overlay layers

### AI Goals (shared base + per-species)
```
Priority 0: FloatGoal
Priority 1: PanicGoal (flee when hurt)
Priority 2: AvoidEntityGoal (flee predators)
Priority 3: PinataRomanceGoal (custom)
Priority 4: TemptGoal (attracted to favorite food in player's hand)
Priority 5: EatFavoriteItemGoal (custom — eat items on ground)
Priority 6: AttractedToBlockGoal (custom — path toward required blocks)
Priority 7: WaterAvoidingRandomStrollGoal
Priority 8: LookAtPlayerGoal
Priority 9: RandomLookAroundGoal
```

### Data Storage
- Entity NBT: happiness, lifecycle, variant, accessories, romance cooldown
- Player capability: gardener level, discovered species, romance achievements
- World saved data: garden boundaries, Tower of Sour state

---

## Model Reference Notes

All piñata models should reference the in-game VP models. Key characteristics:
- **Paper/cardboard construction** — blocky but with rounded implied shapes
- **Visible crepe-paper frills** along segment edges
- **Bold, saturated colors** with paper-fold stripe patterns
- **Simple dot or bead eyes** — usually black with white highlight
- **Each species has a distinct silhouette** even at distance

### Whirlm Model Reference
- Pink/magenta segmented worm (3-4 segments)
- Each segment is a rounded cuboid with paper frills
- Small beady eyes on front-most segment
- Wiggly undulating movement animation
- About 1-1.5 blocks long, 0.3 blocks tall
