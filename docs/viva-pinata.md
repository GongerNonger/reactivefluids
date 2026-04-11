# Viva Pinata Mod

## Overview

A large-scale recreation of Viva Pinata: Trouble in Paradise within Minecraft. Players attract, tame, breed, and care for paper-craft pinata creatures in their gardens. The mod lives on the `claude/viva-pinata-minecraft-mod-WshX2` branch.

## Current State

- **64 species** implemented with unique entities, AI behaviors, and food chain relationships
- **15 base models** shared across species (e.g., HorstachioModel serves horse-like species, SparrowmintModel serves bird species)
- **Garden system** with plot blocks, boundary tracking, and attraction-based spawning
- **Romance mechanic** with species-specific requirements
- **Sour variants** with hostile AI and taming quests
- **Economy** with chocolate coin currency and NPC shops
- **Evolution system** with 11 species transformations
- **3 NPC characters** (Seedos, Storkos, Doc Patchingo)
- **2 threat types** (Ruffian enemies, Dastardos boss)
- **Professor Pester** boss fight
- Compiles and runs, but most species have placeholder textures

## Species

### Model Groups

Each species uses one of 15 base entity models. The model determines the creature's shape and the required texture dimensions.

| Model | Texture | Shape | Species Using It |
|-------|---------|-------|-----------------|
| WhirlmModel | 64x32 | Segmented worm | Whirlm |
| SparrowmintModel | 32x32 | Round bird | Sparrowmint, Candary, Chocstrich, Eaglair, Hootyfruity, Parrybo, Pengum |
| FudgehogModel | 48x32 | Spiky hedgehog | Fudgehog, Badgesicle, Fizzlybear, Goobaa, Limoceros, Moojoo, Parmadillo, Polollybear, Rashberry, Sarsgorilla, Sweetooth, Walrusk |
| HorstachioModel | 64x32 | Large horse | Horstachio, Camello, Chewnicorn, Choclodocus, Dragonache, Elephanilla, Zumbug |
| PretztailModel | 32x32 | Fox/dog | Pretztail, Barkbark, Doenut, Kittyfloss, Mallowolf, Pieena, Roario, Tigermisu |
| BunnycombModel | 32x32 | Rabbit | Bunnycomb |
| BuzzlegumModel | 32x32 | Bumblebee | Buzzlegum |
| ClucklesModel | 32x32 | Chicken | Cluckles |
| LickatoadModel | 32x16 | Squat toad | Lickatoad |
| MousemallowModel | 32x16 | Small mouse | Mousemallow, Cinnamonkey, Pigxie, Raisant, Squazzil |
| NewtgatModel | 32x16 | Lizard | Newtgat, Geckie, Jameleon, Salamango |
| QuackberryModel | 32x32 | Duck | Quackberry, Juicygoose, Swanana |
| ShellybeanModel | 32x16 | Snail/turtle | Shellybean, Cherrapin, Custacean, Jeli, Sweetle |
| SyrupentModel | 32x32 | Snake | Syrupent, Cocoadile, Fourheads, Twingersnap |
| TafflyModel | 32x16 | Small insect | Taffly, Dragumfly, Mothdrop, Reddhott |

### Taming Lifecycle

Every pinata goes through stages:
1. **Wild** — spawns in the world naturally based on garden conditions
2. **Visitor** — enters the garden, exploring
3. **Resident** — fed its favorite food, becomes permanent
4. Residents can be **romanced** (bred) when species-specific conditions are met
5. On death, pinatas drop **candy** items

### Sour Variants

Some species have sour variants — corrupted pinatas with desaturated textures and hostile behavior. Sour pinatas can be tamed by meeting specific conditions, converting them to regular residents.

### Evolution System

11 species can evolve/transform into other species:
- Feed specific items or meet conditions to trigger transformation
- Example: feeding a Sparrowmint a specific item evolves it into a Candary

## Garden System

### GardenManager (`SavedData`)
Tracks garden boundaries, ownership, and resident pinatas per player. Persists across world saves.

### Garden Plots
`GardenPlotBlock` marks claimed garden territory. Pinatas are attracted to gardens based on:
- Block composition (grass, flowers, water, specific crops)
- Existing resident species
- Garden level/size

### Buildings
- **Pinata Houses** — species-specific housing blocks that provide shelter
- **Produce Buildings** — Honey Hive, Milking Shed, Shearing Shed, Mine

### Tools
- **Shovel** — terrain manipulation within the garden
- **Watering Can** — waters plants and garden plots
- **Surface Packets** — change terrain type (grass, sand, snow, etc.)

## AI System

Custom AI goals in `com.reactivefluids.pinata.ai`:

| Goal | Behavior |
|------|----------|
| `AttractedToBlockGoal` | Species seek specific blocks (flowers, water, crops) |
| `EatItemEntityGoal` | Pinatas eat dropped items matching their diet |
| `HuntPreyGoal` | Predator species hunt prey species (Sparrowmint hunts Whirlm, etc.) |
| `PinataRomanceGoal` | Breeding behavior when conditions are met |
| `SourBehaviorGoal` | Hostile behavior for sour variants |
| `SpeciesConflictGoal` | Species pairs that fight (wolf vs. fox, etc.) |

## NPCs

| NPC | Role |
|-----|------|
| **Seedos** | Sells seeds and garden supplies |
| **Storkos** | Delivers eggs from romance events |
| **Doc Patchingo** | Heals sick pinatas |
| **Professor Pester** | Boss enemy, attacks gardens |
| **Ruffian** | Common enemy that harasses pinatas |
| **Dastardos** | Hunts sick pinatas, must be stopped |

## Economy

- **Chocolate Coins** — currency dropped by pinatas and earned from activities
- **Accessories** — 9 equipment slots per pinata for cosmetic items
- **Produce** — pinatas produce items over time (honey from Buzzlegum, milk from Moojoo, etc.)

## Texture Status

3 out of 64 species have proper UV-mapped textures (Whirlm, Sparrowmint, Fudgehog). The remaining 61 have correctly-sized placeholder textures that fill UV regions with the species' base color but lack detail.

A Manus AI prompt has been prepared (`Downloads/manus_pinata_texture_prompt.md`) with complete UV layouts for all 15 models and color descriptions for all 64 species. Reference textures from the BedrockParadise Bedrock Edition mod provide the style guide.

## Key Files

| File | Purpose |
|------|---------|
| `BasePinataEntity.java` | Base class for all pinata entities — taming, candy, sour state |
| `PinataEntities.java` | Registry of all 64 entity types |
| `PinataItems.java` | Registry of candy, tools, accessories |
| `PinataBlocks.java` | Registry of garden blocks, houses, produce buildings |
| `GardenManager.java` | `SavedData` for garden state persistence |
| `generate_pinata_resources.py` | Generates blockstates, models, placeholder textures |
| `generate_pinata_models.py` | Paints UV-mapped entity textures |
| `VIVA_PINATA_DESIGN.md` | Original design document with implementation phases |
