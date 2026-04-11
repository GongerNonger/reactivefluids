# D&D Spell Scrolls

## Overview

A collection of single-use spell scroll items inspired by Dungeons & Dragons 5th Edition. Each scroll is consumed on use and produces a dramatic world effect with custom entities, particles, and structures.

## Spell List

### Combat & Destruction

| Scroll | Effect | Key Classes |
|--------|--------|-------------|
| **Disintegrate** | Fires a beam entity that vaporizes blocks and entities in a line | `DisintegrateScrollItem`, `DisintegrateBeamEntity`, `DisintegrateBeamRenderer`, `DisintegrateParticle` |
| **Meteor Swarm** | Spawns meteor entities that rain from the sky with explosions | `MeteorSwarmScrollItem`, `MeteorEntity`, `MeteorRenderer` |
| **Erupting Earth** | Sends earth erupting upward in an area | `EruptingEarthScrollItem` |

### Necromancy

| Scroll | Effect | Key Classes |
|--------|--------|-------------|
| **Raise Dead** | Reanimates nearby skeletons/zombies as loyal undead with custom textures | Uses `CrystallizedSkeleton`, custom overlays |
| **Conjure Animals** | Summons spectral wolves | `ConjureAnimalsScrollItem` |
| **Phantom Steed** | Summons a rideable translucent horse with custom model | `PhantomSteedEntity`, `PhantomSteedModel`, `PhantomSteedRenderer` |

The necromancy effects use a custom **Necrotic Particle** — an 11-frame animated sprite sheet showing a skull that forms from teal-green wisps, opens its mouth, then dissipates. Built using character-map grids in `generate_textures.py` with `SpriteSet` animation cycling.

### Utility & Terrain

| Scroll | Effect | Key Classes |
|--------|--------|-------------|
| **Dancing Lights** | Creates floating light orb entities that follow the player | `DancingLightsScrollItem`, `DancingLightEntity`, `DancingLightParticle` |
| **Fog Cloud** | Area-of-effect fog entity that obscures vision | `FogCloudScrollItem`, `FogCloudEntity`, `FogCloudParticle` |
| **Control Water** | Manipulates water in an area (part/redirect/raise/lower) | `ControlWaterScrollItem`, `ControlWaterData` |
| **Plant Growth** | Accelerates crop and plant growth in an area | `PlantGrowthScrollItem` |
| **Mold Earth** | Reshapes a small area of earth/dirt/stone | `MoldEarthScrollItem` |
| **Move Earth** | Larger-scale terrain relocation | `MoveEarthScrollItem` |
| **Bones of the Earth** | Raises stone pillars from the ground | `BonesOfTheEarthScrollItem` |
| **Passwall** | Creates a temporary passage through solid walls | `PasswallScrollItem`, `PasswallData` |

### Structure Spawning

| Scroll | Effect | Key Classes |
|--------|--------|-------------|
| **Galder's Tower** | Spawns a multi-story wizard tower | `GaldersTower` (structure generation) |
| **Magnificent Mansion** | Creates a large mansion structure | `MagnificentMansionScrollItem`, `MansionData` |
| **Gong's Grotto** | Spawns a cave/grotto structure | `GongersGrottoScrollItem`, `GrottoData` |
| **Dimension Door** | Teleports the player to a target location | `DimensionDoorScrollItem` |
| **Arcane Gate** | Creates a linked portal pair | `ArcaneGateScrollItem`, `ArcaneGateData` |

## Technical Implementation

Each scroll follows a common pattern:
1. Extends `Item` with a custom `use()` method
2. Consumes the scroll item on use (survival mode)
3. Plays a sound effect and spawns particles
4. Creates either a persistent entity, modifies terrain, or spawns a structure
5. Persistent effects use `SavedData` subclasses (e.g., `PasswallData`, `ArcaneGateData`) to survive world saves

Custom entities are registered in `ModEntities` and rendered via `ClientEvents`. Particles use NeoForge's `ParticleType` registry with custom `ParticleProvider` implementations.
