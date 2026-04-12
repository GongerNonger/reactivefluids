# Reactive Fluids — 1.21.4 Migration Handoff

This document is intended to be read by a future Claude Code session picking up the NeoForge 1.21.1 → 1.21.4 migration that was started on 2026-04-11 and paused mid-way. Read this whole file before touching any code.

## Current state

- **Branch:** `migration/1.21.4`
- **Last commit:** `0218619` — "WIP: partial 1.21.4 migration — scrolls, custom Entity, jellyfish, simple renderers"
- **Fallback tag:** `ship/celestial-1.21.1` points at the last known-working 1.21.1 commit with the Celestial skybox placeholder shipped. `git reset --hard ship/celestial-1.21.1` restores the working 1.21.1 state.
- **Error count on last compile:** 95 errors (down from 255 at the start of the migration)

## Target versions

| Component | Old | New |
|---|---|---|
| `minecraft_version` | 1.21.1 | **1.21.4** |
| `neo_version` | 21.1.172 | **21.4.157** |
| `minecraft_version_range` | `[1.21,1.22)` | `[1.21.4,1.22)` |
| `neo_version_range` | `[21.1,)` | `[21.4,)` |
| GeckoLib dep | `geckolib-neoforge-1.21.1:4.7.5.1` | **`geckolib-neoforge-1.21.4:4.8.5`** |
| GeckoLib `neoforge.mods.toml` range | `[4.7,)` | `[4.8,)` |
| `pack.mcmeta` format | 34 | 34 (no change needed) |
| `data` run type in build.gradle | present | **removed** (unused, replaced in 1.21.4 by clientData/serverData) |

All version bumps and the build.gradle data run removal are already committed in `0218619`.

## What's already done in commit 0218619

### InteractionResultHolder migration (14 files, completed)
1.21.2+ replaced `InteractionResultHolder<ItemStack>` with `InteractionResult`. Migration done via Python script. Pattern:
- `InteractionResultHolder<ItemStack> use(...)` → `InteractionResult use(...)`
- `InteractionResultHolder.success(stack)` → `InteractionResult.SUCCESS`
- `InteractionResultHolder.fail(stack)` → `InteractionResult.FAIL`
- `InteractionResultHolder.consume(stack)` → `InteractionResult.CONSUME`
- `InteractionResultHolder.sidedSuccess(stack, isClient)` → `InteractionResult.SUCCESS`
- Old import removed, new import `net.minecraft.world.InteractionResult` added
- Also fixed two non-scroll occurrences in `DevilsCigarBlock` and `PhantomSteedEntity`

Files touched: MagicMissileScrollItem, MagnificentMansionScrollItem, BonesOfTheEarthScrollItem, FogCloudScrollItem, ReverseGravityScrollItem, DisintegrateScrollItem, GongersGrottoScrollItem, HardenerItem, RaiseDeadScrollItem, ControlWaterScrollItem, ReagentItem, DimensionDoorScrollItem, DancingLightsScrollItem, ConjureAnimalsScrollItem, DevilsCigarBlock, PhantomSteedEntity.

### hurtServer() abstract method (5 files, completed)
1.21.2+ made `Entity.hurtServer(ServerLevel, DamageSource, float)` abstract. Custom Entity subclasses must implement it. All our custom non-damaging entities return `false`.

Files: MagicMissileEntity, MeteorEntity, DisintegrateBeamEntity, FogCloudEntity, DancingLightEntity.

### EntitySpawnReason rename + ConversionParams (2 files, completed)
`MobSpawnType` was renamed to `EntitySpawnReason` and moved in the package tree. `Mob.convertTo` now takes a `ConversionParams` argument built via `ConversionParams.single(mob, keepEquip, preserveLoot)`.

Files: CrystalSolutionBlock (convertTo migration), MoonlightJellyfishEntity (finalizeSpawn signature).

### Simple renderers migrated to EntityRenderState (5 files, completed)
The new `EntityRenderer<T extends Entity, S extends EntityRenderState>` pattern. Every custom renderer now requires:
1. A companion `S extends EntityRenderState` class (or reuse `EntityRenderState` itself for no-ops)
2. Override `createRenderState()` returning a new instance
3. Override `extractRenderState(T entity, S state, float partialTick)` to copy entity fields into the state
4. The `render()` method now takes `(S state, PoseStack, MultiBufferSource, int)` — no entity parameter

Files:
- `DancingLightRenderer`, `DisintegrateBeamRenderer`, `FogCloudRenderer` — no-op, use bare `EntityRenderState`
- `MagicMissileRenderer` — uses bare `EntityRenderState`, render logic doesn't read entity fields
- `MeteorRenderer` — has a custom `MeteorRenderer.State` inner class with a `tickCount` field

## What's left (95 errors clustered in these categories)

### Category 1: MobRenderer subclasses with anonymous RenderLayer inner classes (~40 errors)

Five renderers extend `MobRenderer<Entity, Model<Entity>>` and add anonymous `RenderLayer<Entity, Model>` inner classes in their constructors. In 1.21.4:
- Vanilla models lost their entity generic parameter: `WolfModel<SpectralWolfEntity>` → `WolfModel`
- Renderers need a third generic parameter: `MobRenderer<Entity, RenderState, Model>`
- `RenderLayer` inner classes need the same three-param generic: `RenderLayer<Entity, RenderState, Model>`
- The inner class `render()` method signature changed to take the `RenderState` instead of the entity plus a bunch of float parameters
- Entity field reads inside inner classes (hurtTime, collar color, held item, tail angle) must move to the outer `extractRenderState()` and be stored on the state object

**Affected files with specific complexity notes:**

- **`SpectralWolfRenderer`** — 2 inner RenderLayer classes (body + collar). Body reads entity.hurtTime, entity.deathTime. Collar reads entity.isTame(), entity.getCollarColor().getTextureDiffuseColor(). Uses a custom `RenderLayer` subclass rather than vanilla ones. Also overrides `getBob()` to return `entity.getTailAngle()`. The target vanilla type is `WolfRenderState`, and `WolfRenderer extends AgeableMobRenderer<Wolf, WolfRenderState, WolfModel>`. Study the vanilla `WolfRenderer` at `build/neoForm/neoFormJoined1.21.4-20241203.161809/steps/unzipSources/unpacked/net/minecraft/client/renderer/entity/WolfRenderer.java` to see the exact pattern.

- **`SpectralFoxRenderer`** — 2 inner RenderLayer classes (body + held item in mouth). The held-item layer is the most complex — it reads entity.getItemBySlot, entity.isSleeping(), entity.isBaby(), entity.getHeadRollAngle(partialTick). All of these fields must move to the render state. Look at vanilla `ItemInHandLayer` / `FoxHeldItemLayer` in 1.21.4 for the pattern.

- **`SpectralAxolotlRenderer`** — 1 inner RenderLayer (body). Simplest of the three spectral renderers. Just needs to read tex variant + hurt state from render state.

- **`PhantomSteedRenderer`** — 1 inner RenderLayer (body). Also overrides `scale()` and `getWhiteOverlayProgress()` — both of those method signatures may have changed to take RenderState instead of entity. Reads `entity.getTicksRemaining()`.

- **`RaisedZombieRenderer`** — extends `AbstractZombieRenderer<RaisedZombieEntity, ZombieModel<RaisedZombieEntity>>`. New signature is `AbstractZombieRenderer<Zombie, ZombieRenderState, ZombieModel<ZombieRenderState>>`. Question: can you still extend this with a custom entity type, or is it locked to vanilla Zombie? If it won't accept our RaisedZombieEntity, we need to subclass `HumanoidMobRenderer` directly instead and lose the vanilla zombie layers.

- **`RaisedSkeletonRenderer`** — extends `HumanoidMobRenderer<RaisedSkeletonEntity, SkeletonModel<RaisedSkeletonEntity>>`. Has a `HumanoidArmorLayer`. Similar question about whether extending vanilla renderers with custom entity types still works in 1.21.4.

**General advice for this category**: read the vanilla 1.21.4 equivalent renderer before migrating each one. The source is in `build/neoForm/neoFormJoined1.21.4-20241203.161809/steps/unzipSources/unpacked/net/minecraft/client/renderer/entity/`. Match the generic parameters exactly.

### Category 2: CrystallizedSkeletonRenderer + CrystalOverlayLayer (~10 errors)

`CrystallizedSkeletonRenderer extends SkeletonRenderer<CrystallizedSkeleton>` — but vanilla `SkeletonRenderer` is now `extends AbstractSkeletonRenderer<Skeleton, SkeletonRenderState>`. So either:
- Change to `extends AbstractSkeletonRenderer<CrystallizedSkeleton, SkeletonRenderState>` (if generic still accepts custom types)
- Or extend `HumanoidMobRenderer` directly and reimplement the skeleton visual logic

The renderer has a 130-line `renderLaserBeam()` helper that reads `entity.getLaserTarget()`, `entity.getLaserScale(partialTick)`, `entity.tickCount`, `entity.getEyeHeight()`, plus interpolated positions using `entity.xOld, entity.getX()`, etc. All of that needs to move to the render state.

`CrystalOverlayLayer` (~200 lines) has its own `render()` method signature mismatch error. The old `render(PoseStack, MultiBufferSource, int, Entity, float, float, float, float, float, float)` signature is gone; the new one takes RenderState. Entity reads include `entity.getChargeProgress()` (or similar). Needs full rewrite.

### Category 3: ClientEvents renderer registration (12 errors)

`ClientEvents.registerEntityRenderers()` uses `event.registerEntityRenderer(EntityType, Renderer::new)` 12 times. Each call now fails because the renderer constructors take different signatures after migration. Most of these will auto-resolve once Categories 1 and 2 compile — just one cascading symptom. The error message is "method registerEntityRenderer in class RegisterRenderers cannot be applied to given types."

### Category 4: ModFluids.initializeClient deprecation (3 errors)

`initializeClient(Consumer<IClientFluidTypeExtensions>)` was marked "for removal" in 1.21.1 and is gone in 1.21.4. Three occurrences at `ModFluids.java:259, 519, 590`. The new API is almost certainly `registerExtensions(RegisterClientExtensionsEvent)` fired on the mod event bus, or direct `IClientFluidTypeExtensions` registration via a dedicated event. Look for `RegisterClientExtensionsEvent` in the NeoForge sources.

### Category 5: Miscellaneous smaller errors (~30 errors)

From the error tally:
- **16 "String cannot be converted to ResourceKey<EntityType<?>>"** — some EntityType API started taking a `ResourceKey<EntityType<?>>` where it used to take a string. Likely in `EntityType.Builder.build("id")` calls or similar.
- **7 "Vector3f cannot be converted to int"** — some method that used to return an `int` color now returns `Vector3f`. Possibly `DyeColor.getTextureDiffuseColor()` or similar. Need to convert with `((int)(v.x*255)<<16) | ((int)(v.y*255)<<8) | ((int)(v.z*255))`.
- **2 "no suitable method found for teleportTo(ServerLevel, double, double, double, int, int)"** — `teleportTo` added or removed parameters. Check the new signature and adjust.
- **2 "no suitable constructor found for ThrowableItemProjectile(EntityType<CAP#1>, LivingEntity, Level)"** — ThrowableItemProjectile constructor changed. Likely needs an ItemStack parameter now.
- **2 "isSolidRender in class BlockStateBase cannot be applied"** — method signature changed; probably now requires a BlockPos or similar.
- **1 "addCooldown(MagicMissileScrollItem, int)"** — Player.getCooldowns().addCooldown signature changed, probably now takes ItemStack instead of Item.
- **1 "BlockPos cannot be converted to Orientation"** — some block update method added an Orientation parameter.
- **1 "PhantomSteedEntity cannot override hurt(DamageSource, float) in Entity"** — `Entity.hurt` is now final, override `hurtServer` instead.
- **1 "NearestAttackableTargetGoal cannot infer type arguments"** — unrelated type inference issue, just needs explicit type.
- **1 "LunarJellyfishModel does not override getTextureResource(LunarJellyfishEntity, GeoRenderer<LunarJellyfishEntity>)"** — GeckoLib 4.8 changed the `getTextureResource` signature to take an additional `GeoRenderer` parameter. Easy fix: add the parameter to the method in both jellyfish models. Same fix goes for `MoonlightJellyfishModel`.

## Recommended execution order for the next session

1. **Start with the easiest category** — GeckoLib `getTextureResource` signature fix in both jellyfish models. That's the smallest pull-in and helps confirm the build environment is working.
2. **Then fix ModFluids.initializeClient** — 3 errors, single file, probably 15 minutes once you find the right API.
3. **Then batch-fix the miscellaneous small errors** — each is isolated, no cross-file dependencies.
4. **Then tackle CrystalOverlayLayer** as a standalone RenderLayer migration (study vanilla layers first).
5. **Then migrate the MobRenderer subclasses one at a time**, starting with the simplest (SpectralAxolotl) and ending with the most complex (SpectralFox with held item).
6. **CrystallizedSkeletonRenderer last** — it depends on CrystalOverlayLayer already being correct.
7. **After each category, run** `./gradlew compileJava 2>&1 > /tmp/build_errors.log; grep -c error /tmp/build_errors.log` and confirm the count is going down. Commit after each category that compiles cleanly.
8. **Once build is clean**, deploy to a new CurseForge instance on 1.21.4 with NeoForge 21.4.157. Install GeckoLib 4.8.5 for 1.21.4 from Modrinth (URL in the `Downloads` helper below).

## Useful references on disk

- **Vanilla 1.21.4 Java sources** (decompiled, for API reference):
  `build/neoForm/neoFormJoined1.21.4-20241203.161809/steps/unzipSources/unpacked/net/minecraft/`
  Read these to see how vanilla renderers migrated their own render state code — our spectral/raised mobs should mirror their vanilla counterparts.
- **GeckoLib 4.8.5 jar** already in gradle cache:
  `~/.gradle/caches/modules-2/files-2.1/software.bernie.geckolib/geckolib-neoforge-1.21.4/4.8.5/54a80363a5fea8911c6d57563bb2c355c8b57262/geckolib-neoforge-1.21.4-4.8.5.jar`
  You can `unzip -l` it to inspect what classes it ships, or extract specific `.class` files for reference if sources aren't available.
- **NeoForge 21.4.157 sources** (for NeoForge-added classes only, not vanilla):
  `~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/21.4.157/ecfd4156c1e50460ec2bcea66aa17a01ef14c74b/neoforge-21.4.157-sources.jar`
- **Celestial placeholder skybox pack** — still on disk at `skybox_resource_pack/`, shipped in `testing-new-fluids` branch. This is the final art target once we're on 1.21.4 and can swap to Nuit for a rotating skybox.

## Downloads required to test the finished migration

1. **NeoForge 21.4.157** — create a new CurseForge instance with this exact version
2. **GeckoLib 4.8.5 for NeoForge 1.21.4**:
   https://cdn.modrinth.com/data/8BmcQJ2H/versions/eQtABRub/geckolib-neoforge-1.21.4-4.8.5.jar
3. **Nuit (optional, recommended once compiling)** — pick the latest 1.21.4 NeoForge build from https://modrinth.com/mod/nuit/versions?g=1.21.4&l=neoforge — this is the whole reason we're migrating, so don't forget to also ship a Nuit-format version of the night sky pack that actually rotates with the day/night cycle.

## What not to do

- **Don't merge `migration/1.21.4` back to `testing-new-fluids` until the build is fully clean and tested in-game.** The `testing-new-fluids` branch is the ship-ready 1.21.1 version and needs to remain working.
- **Don't touch the `ship/celestial-1.21.1` tag.** It's the rewind point.
- **Don't delete the spectral/raised undead renderers** even if the migration is tempting. They're referenced by real spell scrolls (RaiseDeadScroll, ConjureAnimalsScroll, etc.) that users will miss. Migrate them properly.
- **Don't try to upgrade to 1.21.8 or 1.21.11 in this session.** The target is 1.21.4 specifically because that's the lowest version with a Nuit NeoForge build. Jumping further is a separate project.

## Success criteria

- `./gradlew clean build` returns BUILD SUCCESSFUL with 0 errors
- Deploy to a 1.21.4 CurseForge instance with NeoForge 21.4.157 + GeckoLib 4.8.5
- Launch the game, enter a world, confirm:
  - All entities spawn (moonlight jelly, lunar jelly, spectral wolf/fox/axolotl, phantom steed, raised zombie/skeleton, crystallized skeleton, meteor, magic missile, dancing lights, fog cloud)
  - All scrolls work (raise dead, conjure animals, disintegrate, meteor swarm, magic missile, dancing lights, fog cloud, control water, move earth, bones of the earth, magnificent mansion, gongers grotto, phantom steed, reverse gravity, dimension door)
  - Epoxy reactions still work (throw amber hardener at amber resin, observe flood fill)
  - Custom fluids render translucently (plankton, acid, ferrofluid, liquid nitrogen, etc.)
  - No missing-texture/model warnings in the launcher log
- Once all of the above is confirmed, merge `migration/1.21.4` into `testing-new-fluids`, tag the merge commit as `ship/nuit-1.21.4`, and push

Good luck.
