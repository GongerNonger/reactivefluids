package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity type registrations for all piñata creatures.
 * Kept separate from the main ModEntities to organize the VP content.
 */
public class ModPinataEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ReactiveFluids.MOD_ID);

    // === Whirlm (worm) — small, ground-level ===
    public static final DeferredHolder<EntityType<?>, EntityType<WhirlmEntity>> WHIRLM =
            ENTITY_TYPES.register("whirlm", () ->
                    EntityType.Builder.<WhirlmEntity>of(WhirlmEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.3F)  // Small worm-like creature
                            .clientTrackingRange(8)
                            .build("whirlm"));

    // === Sparrowmint (sparrow) — small bird, eats Whirlms ===
    public static final DeferredHolder<EntityType<?>, EntityType<SparrowmintEntity>> SPARROWMINT =
            ENTITY_TYPES.register("sparrowmint", () ->
                    EntityType.Builder.<SparrowmintEntity>of(SparrowmintEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .build("sparrowmint"));

    // === Fudgehog (hedgehog) — small, spiky, loves long grass ===
    public static final DeferredHolder<EntityType<?>, EntityType<FudgehogEntity>> FUDGEHOG =
            ENTITY_TYPES.register("fudgehog", () ->
                    EntityType.Builder.<FudgehogEntity>of(FudgehogEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.45F)
                            .clientTrackingRange(8)
                            .build("fudgehog"));

    // === Mousemallow (mouse) — tiny, fast herbivore ===
    public static final DeferredHolder<EntityType<?>, EntityType<MousemallowEntity>> MOUSEMALLOW =
            ENTITY_TYPES.register("mousemallow", () ->
                    EntityType.Builder.<MousemallowEntity>of(MousemallowEntity::new, MobCategory.CREATURE)
                            .sized(0.35F, 0.25F)  // Very small
                            .clientTrackingRange(8)
                            .build("mousemallow"));

    // === Syrupent (snake) — medium predator, eats Mousemallows ===
    public static final DeferredHolder<EntityType<?>, EntityType<SyrupentEntity>> SYRUPENT =
            ENTITY_TYPES.register("syrupent", () ->
                    EntityType.Builder.<SyrupentEntity>of(SyrupentEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.4F)
                            .clientTrackingRange(8)
                            .build("syrupent"));

    // === Taffly (fly) — tiny, attracted to flowers ===
    public static final DeferredHolder<EntityType<?>, EntityType<TafflyEntity>> TAFFLY =
            ENTITY_TYPES.register("taffly", () ->
                    EntityType.Builder.<TafflyEntity>of(TafflyEntity::new, MobCategory.CREATURE)
                            .sized(0.3F, 0.3F)  // Tiny buzzer
                            .clientTrackingRange(8)
                            .build("taffly"));

    // === Bunnycomb (rabbit) — small, fast breeder ===
    public static final DeferredHolder<EntityType<?>, EntityType<BunnycombEntity>> BUNNYCOMB =
            ENTITY_TYPES.register("bunnycomb", () ->
                    EntityType.Builder.<BunnycombEntity>of(BunnycombEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.5F).clientTrackingRange(8).build("bunnycomb"));

    // === Quackberry (duck) — aquatic ===
    public static final DeferredHolder<EntityType<?>, EntityType<QuackberryEntity>> QUACKBERRY =
            ENTITY_TYPES.register("quackberry", () ->
                    EntityType.Builder.<QuackberryEntity>of(QuackberryEntity::new, MobCategory.CREATURE)
                            .sized(0.55F, 0.5F).clientTrackingRange(8).build("quackberry"));

    // === Shellybean (snail) — very slow, armored ===
    public static final DeferredHolder<EntityType<?>, EntityType<ShellybeanEntity>> SHELLYBEAN =
            ENTITY_TYPES.register("shellybean", () ->
                    EntityType.Builder.<ShellybeanEntity>of(ShellybeanEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.4F).clientTrackingRange(8).build("shellybean"));

    // === Newtgat (newt) — semi-aquatic ===
    public static final DeferredHolder<EntityType<?>, EntityType<NewtgatEntity>> NEWTGAT =
            ENTITY_TYPES.register("newtgat", () ->
                    EntityType.Builder.<NewtgatEntity>of(NewtgatEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.3F).clientTrackingRange(8).build("newtgat"));

    // === Lickatoad (frog) — eats Tafflies ===
    public static final DeferredHolder<EntityType<?>, EntityType<LickatoadEntity>> LICKATOAD =
            ENTITY_TYPES.register("lickatoad", () ->
                    EntityType.Builder.<LickatoadEntity>of(LickatoadEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.45F).clientTrackingRange(8).build("lickatoad"));

    // === Pretztail (fox) — eats Bunnycombs ===
    public static final DeferredHolder<EntityType<?>, EntityType<PretztailEntity>> PRETZTAIL =
            ENTITY_TYPES.register("pretztail", () ->
                    EntityType.Builder.<PretztailEntity>of(PretztailEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.7F).clientTrackingRange(10).build("pretztail"));

    // === Buzzlegum (bee) — produces honey ===
    public static final DeferredHolder<EntityType<?>, EntityType<BuzzlegumEntity>> BUZZLEGUM =
            ENTITY_TYPES.register("buzzlegum", () ->
                    EntityType.Builder.<BuzzlegumEntity>of(BuzzlegumEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.5F).clientTrackingRange(8).build("buzzlegum"));

    // === Cluckles (chicken) — lays eggs ===
    public static final DeferredHolder<EntityType<?>, EntityType<ClucklesEntity>> CLUCKLES =
            ENTITY_TYPES.register("cluckles", () ->
                    EntityType.Builder.<ClucklesEntity>of(ClucklesEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.6F).clientTrackingRange(8).build("cluckles"));

    // === Horstachio (horse) — large, majestic ===
    public static final DeferredHolder<EntityType<?>, EntityType<HorstachioEntity>> HORSTACHIO =
            ENTITY_TYPES.register("horstachio", () ->
                    EntityType.Builder.<HorstachioEntity>of(HorstachioEntity::new, MobCategory.CREATURE)
                            .sized(1.4F, 1.6F).clientTrackingRange(10).build("horstachio"));

    // === Barkbark (dog) ===
    public static final DeferredHolder<EntityType<?>, EntityType<BarkbarkEntity>> BARKBARK =
            ENTITY_TYPES.register("barkbark", () ->
                    EntityType.Builder.<BarkbarkEntity>of(BarkbarkEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.65F).clientTrackingRange(10).build("barkbark"));

    // === Kittyfloss (cat) ===
    public static final DeferredHolder<EntityType<?>, EntityType<KittyflossEntity>> KITTYFLOSS =
            ENTITY_TYPES.register("kittyfloss", () ->
                    EntityType.Builder.<KittyflossEntity>of(KittyflossEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.55F).clientTrackingRange(10).build("kittyfloss"));

    // === Goobaa (sheep) ===
    public static final DeferredHolder<EntityType<?>, EntityType<GoobaaEntity>> GOOBAA =
            ENTITY_TYPES.register("goobaa", () ->
                    EntityType.Builder.<GoobaaEntity>of(GoobaaEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 0.6F).clientTrackingRange(8).build("goobaa"));

    // === Rashberry (pig) ===
    public static final DeferredHolder<EntityType<?>, EntityType<RashberryEntity>> RASHBERRY =
            ENTITY_TYPES.register("rashberry", () ->
                    EntityType.Builder.<RashberryEntity>of(RashberryEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 0.6F).clientTrackingRange(8).build("rashberry"));

    // === Doenut (deer) ===
    public static final DeferredHolder<EntityType<?>, EntityType<DoenutEntity>> DOENUT =
            ENTITY_TYPES.register("doenut", () ->
                    EntityType.Builder.<DoenutEntity>of(DoenutEntity::new, MobCategory.CREATURE)
                            .sized(0.8F, 1.0F).clientTrackingRange(10).build("doenut"));

    // === Batches 7-9: 29 species ===
    public static final DeferredHolder<EntityType<?>, EntityType<CandaryEntity>> CANDARY =
            ENTITY_TYPES.register("candary", () -> EntityType.Builder.<CandaryEntity>of(CandaryEntity::new, MobCategory.CREATURE)
                    .sized(0.45F, 0.5F).clientTrackingRange(10).build("candary"));
    public static final DeferredHolder<EntityType<?>, EntityType<ParmadilloEntity>> PARMADILLO =
            ENTITY_TYPES.register("parmadillo", () -> EntityType.Builder.<ParmadilloEntity>of(ParmadilloEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.4F).clientTrackingRange(10).build("parmadillo"));
    public static final DeferredHolder<EntityType<?>, EntityType<ZumbugEntity>> ZUMBUG =
            ENTITY_TYPES.register("zumbug", () -> EntityType.Builder.<ZumbugEntity>of(ZumbugEntity::new, MobCategory.CREATURE)
                    .sized(1.3F, 1.5F).clientTrackingRange(10).build("zumbug"));
    public static final DeferredHolder<EntityType<?>, EntityType<PieenaEntity>> PIEENA =
            ENTITY_TYPES.register("pieena", () -> EntityType.Builder.<PieenaEntity>of(PieenaEntity::new, MobCategory.CREATURE)
                    .sized(0.7F, 0.7F).clientTrackingRange(10).build("pieena"));
    public static final DeferredHolder<EntityType<?>, EntityType<JuicygooseEntity>> JUICYGOOSE =
            ENTITY_TYPES.register("juicygoose", () -> EntityType.Builder.<JuicygooseEntity>of(JuicygooseEntity::new, MobCategory.CREATURE)
                    .sized(0.65F, 0.75F).clientTrackingRange(10).build("juicygoose"));
    public static final DeferredHolder<EntityType<?>, EntityType<SalamangoEntity>> SALAMANGO =
            ENTITY_TYPES.register("salamango", () -> EntityType.Builder.<SalamangoEntity>of(SalamangoEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.3F).clientTrackingRange(10).build("salamango"));
    public static final DeferredHolder<EntityType<?>, EntityType<ReddhottEntity>> REDDHOTT =
            ENTITY_TYPES.register("reddhott", () -> EntityType.Builder.<ReddhottEntity>of(ReddhottEntity::new, MobCategory.CREATURE)
                    .sized(0.3F, 0.3F).clientTrackingRange(10).build("reddhott"));
    public static final DeferredHolder<EntityType<?>, EntityType<ChocstrichEntity>> CHOCSTRICH =
            ENTITY_TYPES.register("chocstrich", () -> EntityType.Builder.<ChocstrichEntity>of(ChocstrichEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.0F).clientTrackingRange(10).build("chocstrich"));
    public static final DeferredHolder<EntityType<?>, EntityType<MoojooEntity>> MOOJOO =
            ENTITY_TYPES.register("moojoo", () -> EntityType.Builder.<MoojooEntity>of(MoojooEntity::new, MobCategory.CREATURE)
                    .sized(0.8F, 0.7F).clientTrackingRange(10).build("moojoo"));
    public static final DeferredHolder<EntityType<?>, EntityType<CinnamonkeyEntity>> CINNAMONKEY =
            ENTITY_TYPES.register("cinnamonkey", () -> EntityType.Builder.<CinnamonkeyEntity>of(CinnamonkeyEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.6F).clientTrackingRange(10).build("cinnamonkey"));
    public static final DeferredHolder<EntityType<?>, EntityType<SarsgorillaEntity>> SARSGORILLA =
            ENTITY_TYPES.register("sarsgorilla", () -> EntityType.Builder.<SarsgorillaEntity>of(SarsgorillaEntity::new, MobCategory.CREATURE)
                    .sized(1.0F, 1.0F).clientTrackingRange(10).build("sarsgorilla"));
    public static final DeferredHolder<EntityType<?>, EntityType<CamelloEntity>> CAMELLO =
            ENTITY_TYPES.register("camello", () -> EntityType.Builder.<CamelloEntity>of(CamelloEntity::new, MobCategory.CREATURE)
                    .sized(1.2F, 1.5F).clientTrackingRange(10).build("camello"));
    public static final DeferredHolder<EntityType<?>, EntityType<PengumEntity>> PENGUM =
            ENTITY_TYPES.register("pengum", () -> EntityType.Builder.<PengumEntity>of(PengumEntity::new, MobCategory.CREATURE)
                    .sized(0.45F, 0.6F).clientTrackingRange(10).build("pengum"));
    public static final DeferredHolder<EntityType<?>, EntityType<WalruskEntity>> WALRUSK =
            ENTITY_TYPES.register("walrusk", () -> EntityType.Builder.<WalruskEntity>of(WalruskEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 0.7F).clientTrackingRange(10).build("walrusk"));
    public static final DeferredHolder<EntityType<?>, EntityType<PolollybearEntity>> POLOLLYBEAR =
            ENTITY_TYPES.register("polollybear", () -> EntityType.Builder.<PolollybearEntity>of(PolollybearEntity::new, MobCategory.CREATURE)
                    .sized(1.0F, 0.9F).clientTrackingRange(10).build("polollybear"));
    public static final DeferredHolder<EntityType<?>, EntityType<FizzlybearEntity>> FIZZLYBEAR =
            ENTITY_TYPES.register("fizzlybear", () -> EntityType.Builder.<FizzlybearEntity>of(FizzlybearEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 0.85F).clientTrackingRange(10).build("fizzlybear"));
    public static final DeferredHolder<EntityType<?>, EntityType<LimeocerosEntity>> LIMEOCEROS =
            ENTITY_TYPES.register("limeoceros", () -> EntityType.Builder.<LimeocerosEntity>of(LimeocerosEntity::new, MobCategory.CREATURE)
                    .sized(1.2F, 0.9F).clientTrackingRange(10).build("limeoceros"));
    public static final DeferredHolder<EntityType<?>, EntityType<PigxieEntity>> PIGXIE =
            ENTITY_TYPES.register("pigxie", () -> EntityType.Builder.<PigxieEntity>of(PigxieEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.5F).clientTrackingRange(10).build("pigxie"));
    public static final DeferredHolder<EntityType<?>, EntityType<FourheadsEntity>> FOURHEADS =
            ENTITY_TYPES.register("fourheads", () -> EntityType.Builder.<FourheadsEntity>of(FourheadsEntity::new, MobCategory.CREATURE)
                    .sized(1.0F, 1.0F).clientTrackingRange(10).build("fourheads"));
    public static final DeferredHolder<EntityType<?>, EntityType<TwingersnapEntity>> TWINGERSNAP =
            ENTITY_TYPES.register("twingersnap", () -> EntityType.Builder.<TwingersnapEntity>of(TwingersnapEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.5F).clientTrackingRange(10).build("twingersnap"));
    public static final DeferredHolder<EntityType<?>, EntityType<ChoclodocusEntity>> CHOCLODOCUS =
            ENTITY_TYPES.register("choclodocus", () -> EntityType.Builder.<ChoclodocusEntity>of(ChoclodocusEntity::new, MobCategory.CREATURE)
                    .sized(1.8F, 2.0F).clientTrackingRange(10).build("choclodocus"));
    public static final DeferredHolder<EntityType<?>, EntityType<JameleonEntity>> JAMELEON =
            ENTITY_TYPES.register("jameleon", () -> EntityType.Builder.<JameleonEntity>of(JameleonEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.3F).clientTrackingRange(10).build("jameleon"));
    public static final DeferredHolder<EntityType<?>, EntityType<GeckieEntity>> GECKIE =
            ENTITY_TYPES.register("geckie", () -> EntityType.Builder.<GeckieEntity>of(GeckieEntity::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.25F).clientTrackingRange(10).build("geckie"));
    public static final DeferredHolder<EntityType<?>, EntityType<JeliEntity>> JELI =
            ENTITY_TYPES.register("jeli", () -> EntityType.Builder.<JeliEntity>of(JeliEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.6F).clientTrackingRange(10).build("jeli"));
    public static final DeferredHolder<EntityType<?>, EntityType<CustaceanEntity>> CUSTACEAN =
            ENTITY_TYPES.register("custacean", () -> EntityType.Builder.<CustaceanEntity>of(CustaceanEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.35F).clientTrackingRange(10).build("custacean"));
    public static final DeferredHolder<EntityType<?>, EntityType<MothdropEntity>> MOTHDROP =
            ENTITY_TYPES.register("mothdrop", () -> EntityType.Builder.<MothdropEntity>of(MothdropEntity::new, MobCategory.CREATURE)
                    .sized(0.35F, 0.3F).clientTrackingRange(10).build("mothdrop"));
    public static final DeferredHolder<EntityType<?>, EntityType<SweetleEntity>> SWEETLE =
            ENTITY_TYPES.register("sweetle", () -> EntityType.Builder.<SweetleEntity>of(SweetleEntity::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.3F).clientTrackingRange(10).build("sweetle"));
    public static final DeferredHolder<EntityType<?>, EntityType<RaisantEntity>> RAISANT =
            ENTITY_TYPES.register("raisant", () -> EntityType.Builder.<RaisantEntity>of(RaisantEntity::new, MobCategory.CREATURE)
                    .sized(0.25F, 0.2F).clientTrackingRange(10).build("raisant"));
    public static final DeferredHolder<EntityType<?>, EntityType<CherrapinEntity>> CHERRAPIN =
            ENTITY_TYPES.register("cherrapin", () -> EntityType.Builder.<CherrapinEntity>of(CherrapinEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.4F).clientTrackingRange(10).build("cherrapin"));

    // === NPCs ===
    public static final DeferredHolder<EntityType<?>, EntityType<SeedosEntity>> SEEDOS =
            ENTITY_TYPES.register("seedos", () ->
                    EntityType.Builder.<SeedosEntity>of(SeedosEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F).clientTrackingRange(10).build("seedos"));

    // === Batch 6 ===
    public static final DeferredHolder<EntityType<?>, EntityType<ElephanillaEntity>> ELEPHANILLA =
            ENTITY_TYPES.register("elephanilla", () -> EntityType.Builder.<ElephanillaEntity>of(ElephanillaEntity::new, MobCategory.CREATURE)
                    .sized(1.6F, 1.8F).clientTrackingRange(10).build("elephanilla"));
    public static final DeferredHolder<EntityType<?>, EntityType<ChewnicornEntity>> CHEWNICORN =
            ENTITY_TYPES.register("chewnicorn", () -> EntityType.Builder.<ChewnicornEntity>of(ChewnicornEntity::new, MobCategory.CREATURE)
                    .sized(1.3F, 1.6F).clientTrackingRange(10).build("chewnicorn"));
    public static final DeferredHolder<EntityType<?>, EntityType<RoarioEntity>> ROARIO =
            ENTITY_TYPES.register("roario", () -> EntityType.Builder.<RoarioEntity>of(RoarioEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 0.9F).clientTrackingRange(10).build("roario"));
    public static final DeferredHolder<EntityType<?>, EntityType<TigermisuEntity>> TIGERMISU =
            ENTITY_TYPES.register("tigermisu", () -> EntityType.Builder.<TigermisuEntity>of(TigermisuEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 0.9F).clientTrackingRange(10).build("tigermisu"));
    public static final DeferredHolder<EntityType<?>, EntityType<ParryboEntity>> PARRYBO =
            ENTITY_TYPES.register("parrybo", () -> EntityType.Builder.<ParryboEntity>of(ParryboEntity::new, MobCategory.CREATURE)
                    .sized(0.45F, 0.6F).clientTrackingRange(8).build("parrybo"));
    public static final DeferredHolder<EntityType<?>, EntityType<SwananaEntity>> SWANANA =
            ENTITY_TYPES.register("swanana", () -> EntityType.Builder.<SwananaEntity>of(SwananaEntity::new, MobCategory.CREATURE)
                    .sized(0.7F, 0.8F).clientTrackingRange(8).build("swanana"));
    public static final DeferredHolder<EntityType<?>, EntityType<EaglairEntity>> EAGLAIR =
            ENTITY_TYPES.register("eaglair", () -> EntityType.Builder.<EaglairEntity>of(EaglairEntity::new, MobCategory.CREATURE)
                    .sized(0.7F, 0.7F).clientTrackingRange(10).build("eaglair"));
    public static final DeferredHolder<EntityType<?>, EntityType<BadgesicleEntity>> BADGESICLE =
            ENTITY_TYPES.register("badgesicle", () -> EntityType.Builder.<BadgesicleEntity>of(BadgesicleEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.5F).clientTrackingRange(8).build("badgesicle"));
    public static final DeferredHolder<EntityType<?>, EntityType<HootyfruityEntity>> HOOTYFRUITY =
            ENTITY_TYPES.register("hootyfruity", () -> EntityType.Builder.<HootyfruityEntity>of(HootyfruityEntity::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.6F).clientTrackingRange(8).build("hootyfruity"));
    public static final DeferredHolder<EntityType<?>, EntityType<DragumflyEntity>> DRAGUMFLY =
            ENTITY_TYPES.register("dragumfly", () -> EntityType.Builder.<DragumflyEntity>of(DragumflyEntity::new, MobCategory.CREATURE)
                    .sized(0.35F, 0.35F).clientTrackingRange(8).build("dragumfly"));

    // === Batch 5 ===
    public static final DeferredHolder<EntityType<?>, EntityType<SquazzilEntity>> SQUAZZIL =
            ENTITY_TYPES.register("squazzil", () -> EntityType.Builder.<SquazzilEntity>of(SquazzilEntity::new, MobCategory.CREATURE)
                    .sized(0.4F, 0.45F).clientTrackingRange(8).build("squazzil"));
    public static final DeferredHolder<EntityType<?>, EntityType<SweetoothEntity>> SWEETOOTH =
            ENTITY_TYPES.register("sweetooth", () -> EntityType.Builder.<SweetoothEntity>of(SweetoothEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 0.9F).clientTrackingRange(10).build("sweetooth"));
    public static final DeferredHolder<EntityType<?>, EntityType<MallowolfEntity>> MALLOWOLF =
            ENTITY_TYPES.register("mallowolf", () -> EntityType.Builder.<MallowolfEntity>of(MallowolfEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.8F).clientTrackingRange(10).build("mallowolf"));
    public static final DeferredHolder<EntityType<?>, EntityType<CocoadileEntity>> COCOADILE =
            ENTITY_TYPES.register("cocoadile", () -> EntityType.Builder.<CocoadileEntity>of(CocoadileEntity::new, MobCategory.CREATURE)
                    .sized(1.2F, 0.5F).clientTrackingRange(10).build("cocoadile"));
    public static final DeferredHolder<EntityType<?>, EntityType<DragonacheEntity>> DRAGONACHE =
            ENTITY_TYPES.register("dragonache", () -> EntityType.Builder.<DragonacheEntity>of(DragonacheEntity::new, MobCategory.CREATURE)
                    .sized(1.6F, 1.8F).clientTrackingRange(16).fireImmune().build("dragonache"));

    // === NPCs ===
    public static final DeferredHolder<EntityType<?>, EntityType<StorkosEntity>> STORKOS =
            ENTITY_TYPES.register("storkos", () -> EntityType.Builder.<StorkosEntity>of(StorkosEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.2F).clientTrackingRange(16).build("storkos"));
    public static final DeferredHolder<EntityType<?>, EntityType<DocPatchingoEntity>> DOC_PATCHINGO =
            ENTITY_TYPES.register("doc_patchingo", () -> EntityType.Builder.<DocPatchingoEntity>of(DocPatchingoEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F).clientTrackingRange(10).build("doc_patchingo"));

    // === Boss ===
    public static final DeferredHolder<EntityType<?>, EntityType<ProfessorPesterEntity>> PROFESSOR_PESTER =
            ENTITY_TYPES.register("professor_pester", () -> EntityType.Builder.<ProfessorPesterEntity>of(ProfessorPesterEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.0F).clientTrackingRange(16).build("professor_pester"));

    // === Threats ===
    public static final DeferredHolder<EntityType<?>, EntityType<RuffianEntity>> RUFFIAN =
            ENTITY_TYPES.register("ruffian", () ->
                    EntityType.Builder.<RuffianEntity>of(RuffianEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.9F).clientTrackingRange(10).build("ruffian"));

    public static final DeferredHolder<EntityType<?>, EntityType<DastardosEntity>> DASTARDOS =
            ENTITY_TYPES.register("dastardos", () ->
                    EntityType.Builder.<DastardosEntity>of(DastardosEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.9F).clientTrackingRange(16).build("dastardos"));
}
