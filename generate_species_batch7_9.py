#!/usr/bin/env python3
"""
Batch generate piñata species for batches 7-9.
Includes evolution targets, exotic/arctic/desert species, and unique creatures.
"""
import os, sys

# Reuse the template system
PINATA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)),
    "src", "main", "java", "com", "reactivefluids", "pinata")

SPECIES = [
    # === Batch 7: Evolution targets + monkeys/gorillas ===
    ("Candary", "canary", 14, 0.32, 2.0, 0.0,
     "Items.DANDELION, Items.WHEAT_SEEDS", "Items.DANDELION", "Items.SUNFLOWER",
     4, 3, "SparrowmintModel", 0.3, 0.45, 0.5,
     "", "", "Canary piñata — evolved from Sparrowmint. Bright canary-yellow. Sings."),

    ("Parmadillo", "armadillo", 18, 0.22, 2.0, 8.0,
     "Items.COCOA_BEANS, Items.SWEET_BERRIES", "Items.COCOA_BEANS", "Items.MELON_SLICE",
     5, 2, "FudgehogModel", 0.35, 0.6, 0.4,
     "", "", "Armadillo piñata — evolved from Fudgehog. Heavy armor (8). Parma ham coloring."),

    ("Zumbug", "zebra", 28, 0.32, 3.0, 2.0,
     "Items.APPLE, Items.CARROT", "Items.APPLE", "Items.GOLDEN_APPLE",
     6, 2, "HorstachioModel", 0.6, 1.3, 1.5,
     "", "", "Zebra piñata — evolved from Horstachio. Humbug-striped (black+white candy)."),

    ("Pieena", "hyena", 20, 0.33, 5.0, 1.0,
     "Items.BONE, Items.ROTTEN_FLESH", "Items.BONE", "Items.COOKED_BEEF",
     5, 2, "PretztailModel", 0.45, 0.7, 0.7,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.HuntPreyGoal(this, () -> ModPinataEntities.BUNNYCOMB.get(), 1.4, 14.0));',
     "", "Hyena piñata — evolved from Pretztail. Pie-crust coloring. Laughing hunter."),

    ("Juicygoose", "goose", 16, 0.27, 2.0, 0.0,
     "Items.SWEET_BERRIES, Items.BREAD", "Items.SWEET_BERRIES", "Items.GOLDEN_APPLE",
     5, 2, "QuackberryModel", 0.4, 0.65, 0.75,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.WATER, 1.0, 20));',
     "    @Override public boolean canBreatheUnderwater() { return true; }",
     "Goose piñata — evolved from Quackberry. Juicy fruit coloring. Aquatic."),

    ("Salamango", "salamander", 14, 0.28, 3.0, 1.0,
     "Items.BLAZE_POWDER, Items.MAGMA_CREAM", "Items.BLAZE_POWDER", "Items.FIRE_CHARGE",
     4, 2, "NewtgatModel", 0.3, 0.5, 0.3,
     "", "    @Override public boolean fireImmune() { return true; }",
     "Salamander piñata — evolved from Newtgat. Mango-colored. Fire immune."),

    ("Reddhott", "firefly", 10, 0.3, 1.0, 0.0,
     "Items.BLAZE_POWDER, Items.GLOWSTONE_DUST", "Items.BLAZE_POWDER", "Items.FIRE_CHARGE",
     3, 2, "TafflyModel", 0.2, 0.3, 0.3,
     "", "    @Override public boolean fireImmune() { return true; }",
     "Firefly piñata — evolved from Taffly. Red-hot coloring. Glows. Fire immune."),

    ("Chocstrich", "ostrich", 18, 0.35, 2.0, 0.0,
     "Items.CACTUS, Items.WHEAT", "Items.CACTUS", "Items.MELON_SLICE",
     5, 2, "SparrowmintModel", 0.45, 0.6, 1.0,
     "", "", "Ostrich piñata — evolved from Cluckles. Chocolate coloring. Fast runner."),

    ("Moojoo", "highland_cow", 20, 0.2, 1.0, 2.0,
     "Items.WHEAT, Items.SPRUCE_SAPLING", "Items.SPRUCE_SAPLING", "Items.HAY_BLOCK",
     5, 2, "FudgehogModel", 0.5, 0.8, 0.7,
     "", "", "Highland cow piñata — evolved from Doenut. Moo-juice coloring. Produces milk."),

    ("Cinnamonkey", "monkey", 14, 0.33, 3.0, 0.0,
     "Items.APPLE, Items.COCOA_BEANS", "Items.APPLE", "Items.COOKIE",
     4, 2, "MousemallowModel", 0.35, 0.5, 0.6,
     '        goalSelector.addGoal(2, new com.reactivefluids.pinata.ai.SpeciesConflictGoal(this, () -> ModPinataEntities.CINNAMONKEY.get(), 8.0));',
     "", "Monkey piñata — cinnamon-bun coloring. Agile tree-dweller. Conflicts with Bonboon."),

    # === Batch 8: Exotic + Arctic + Desert ===
    ("Sarsgorilla", "gorilla", 30, 0.25, 7.0, 4.0,
     "Items.APPLE, Items.MELON_SLICE", "Items.MELON_SLICE", "Items.GOLDEN_APPLE",
     7, 2, "FudgehogModel", 0.6, 1.0, 1.0,
     "", "", "Gorilla piñata — sarsaparilla-brown. Powerful, territorial. Apex land predator."),

    ("Camello", "camel", 24, 0.25, 1.0, 2.0,
     "Items.CACTUS, Items.DEAD_BUSH", "Items.CACTUS", "Items.DRIED_KELP",
     5, 2, "HorstachioModel", 0.6, 1.2, 1.5,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SAND, 0.9, 20));',
     "", "Camel piñata — caramel-colored desert dweller. Attracted to sand."),

    ("Pengum", "penguin", 12, 0.22, 1.0, 1.0,
     "Items.COD, Items.SALMON", "Items.COD", "Items.COOKED_COD",
     4, 2, "SparrowmintModel", 0.3, 0.45, 0.6,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SNOW_BLOCK, 0.9, 16));',
     "    @Override public boolean canBreatheUnderwater() { return true; }",
     "Penguin piñata — bubblegum-blue arctic species. Loves snow and fish."),

    ("Walrusk", "walrus", 28, 0.18, 3.0, 5.0,
     "Items.COD, Items.SALMON", "Items.COD", "Items.TROPICAL_FISH",
     6, 2, "FudgehogModel", 0.5, 0.9, 0.7,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SNOW_BLOCK, 0.8, 16));',
     "    @Override public boolean canBreatheUnderwater() { return true; }",
     "Walrus piñata — rusk-cracker tusks. Arctic aquatic heavyweight."),

    ("Polollybear", "polar_bear", 30, 0.26, 6.0, 4.0,
     "Items.COD, Items.SALMON", "Items.SALMON", "Items.PUFFERFISH",
     7, 2, "FudgehogModel", 0.6, 1.0, 0.9,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SNOW_BLOCK, 0.9, 20));',
     "", "Polar bear piñata — lollipop-white arctic apex. Evolved from Fizzlybear."),

    ("Fizzlybear", "brown_bear", 22, 0.24, 5.0, 2.0,
     "Items.HONEYCOMB, Items.SWEET_BERRIES", "Items.HONEYCOMB", "Items.HONEY_BOTTLE",
     6, 2, "FudgehogModel", 0.55, 0.9, 0.85,
     "", "", "Brown bear piñata — fizzy-drink coloring. Evolves into Polollybear (feed lapis)."),

    ("Limeoceros", "rhino", 35, 0.22, 6.0, 8.0,
     "Items.APPLE, Items.HAY_BLOCK", "Items.HAY_BLOCK", "Items.GOLDEN_APPLE",
     7, 2, "FudgehogModel", 0.65, 1.2, 0.9,
     "", "", "Rhino piñata — lime-green horn. Tamed Limeoceros attacks Professor Pester."),

    # === Batch 9: Unique/special species ===
    ("Pigxie", "pig_fairy", 16, 0.28, 2.0, 0.0,
     "Items.GOLDEN_CARROT, Items.COOKIE", "Items.GOLDEN_CARROT", "Items.CAKE",
     5, 2, "MousemallowModel", 0.3, 0.5, 0.5,
     "", "", "Pig-fairy hybrid piñata — cross-breed of Rashberry + Swanana. Can fly."),

    ("Fourheads", "hydra", 40, 0.2, 8.0, 5.0,
     "Items.COOKED_BEEF, Items.COOKED_PORKCHOP", "Items.COOKED_BEEF", "Items.GOLDEN_APPLE",
     8, 1, "SyrupentModel", 0.6, 1.0, 1.0,
     "", "", "Hydra piñata — four-headed serpent. One of the rarest species."),

    ("Twingersnap", "two_headed_snake", 20, 0.3, 4.0, 2.0,
     "Items.APPLE, Items.HONEY_BOTTLE", "Items.APPLE", "Items.HONEY_BOTTLE",
     5, 2, "SyrupentModel", 0.4, 0.6, 0.5,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.HuntPreyGoal(this, () -> ModPinataEntities.MOUSEMALLOW.get(), 1.4, 14.0));',
     "", "Two-headed snake piñata — ginger-snap coloring. Evolved from Syrupent line."),

    ("Choclodocus", "dinosaur", 45, 0.18, 5.0, 6.0,
     "Items.HAY_BLOCK, Items.APPLE", "Items.HAY_BLOCK", "Items.ENCHANTED_GOLDEN_APPLE",
     9, 2, "HorstachioModel", 0.7, 1.8, 2.0,
     "", "", "Dinosaur piñata — chocolate coloring. Scares Professor Pester. Second rarest."),

    ("Jameleon", "chameleon", 10, 0.25, 1.0, 0.0,
     "Items.SPIDER_EYE, Items.GLOW_BERRIES", "Items.SPIDER_EYE", "Items.FERMENTED_SPIDER_EYE",
     3, 6, "NewtgatModel", 0.25, 0.5, 0.3,
     "", "", "Chameleon piñata — jam-colored. 6 variants (changes color). Camouflage ability."),

    ("Geckie", "gecko", 8, 0.3, 1.0, 0.0,
     "Items.SPIDER_EYE, Items.GLOW_BERRIES", "Items.GLOW_BERRIES", "Items.GLOWSTONE_DUST",
     3, 3, "NewtgatModel", 0.2, 0.4, 0.25,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SAND, 0.9, 16));',
     "", "Gecko piñata — desert lizard. Gummy-candy coloring."),

    ("Jeli", "jellyfish", 8, 0.15, 1.0, 0.0,
     "Items.GLOW_INK_SAC, Items.SLIME_BALL", "Items.GLOW_INK_SAC", "Items.GLOWSTONE_DUST",
     3, 3, "ShellybeanModel", 0.3, 0.5, 0.6,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.WATER, 0.8, 20));',
     "    @Override public boolean canBreatheUnderwater() { return true; }",
     "Jellyfish piñata — jelly-translucent. Aquatic. Glows."),

    ("Custacean", "crab", 12, 0.2, 3.0, 5.0,
     "Items.COD, Items.KELP", "Items.COD", "Items.COOKED_COD",
     4, 2, "ShellybeanModel", 0.3, 0.5, 0.35,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SAND, 0.9, 16));',
     "    @Override public boolean canBreatheUnderwater() { return true; }",
     "Crab piñata — custard-cream claws. Scuttles sideways. Beach/sand dweller."),

    ("Mothdrop", "moth", 6, 0.28, 0.0, 0.0,
     "Items.GLOWSTONE_DUST, Items.TORCH", "Items.GLOWSTONE_DUST", "Items.GLOW_INK_SAC",
     2, 3, "TafflyModel", 0.2, 0.35, 0.3,
     "", "", "Moth piñata — gumdrop coloring. Nocturnal. Attracted to light sources."),

    ("Sweetle", "beetle", 8, 0.2, 1.0, 3.0,
     "Items.APPLE, Items.SWEET_BERRIES", "Items.APPLE", "Items.HONEYCOMB",
     3, 4, "ShellybeanModel", 0.2, 0.4, 0.3,
     "", "", "Beetle piñata — sweet/candy-shell coloring. Many color variants."),

    ("Raisant", "ant", 4, 0.3, 1.0, 1.0,
     "Items.SUGAR, Items.COOKIE", "Items.SUGAR", "Items.CAKE",
     2, 2, "MousemallowModel", 0.15, 0.25, 0.2,
     '        goalSelector.addGoal(2, new com.reactivefluids.pinata.ai.SpeciesConflictGoal(this, () -> ModPinataEntities.BUZZLEGUM.get(), 8.0));',
     "", "Ant piñata — raisin-colored. Tiny. Conflicts with Buzzlegum (ant vs bee)."),

    ("Cherrapin", "turtle", 14, 0.15, 1.0, 7.0,
     "Items.SEAGRASS, Items.KELP", "Items.SEAGRASS", "Items.TURTLE_EGG",
     4, 2, "ShellybeanModel", 0.35, 0.6, 0.4,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.WATER, 0.8, 16));',
     "    @Override public boolean canBreatheUnderwater() { return true; }",
     "Turtle piñata — cherry-flavored shell. Aquatic. Very high armor."),
]

# Same templates as batch generator
ENTITY_TEMPLATE = '''package com.reactivefluids.pinata;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

/**
 * {description}
 */
public class {name}Entity extends BasePinataEntity {{
    public {name}Entity(EntityType<? extends Animal> type, Level level) {{ super(type, level); baseCandyCount = {candy_n}; }}

    public static AttributeSupplier.Builder createAttributes() {{
        return createBasePinataAttributes().add(Attributes.MAX_HEALTH, {hp})
                .add(Attributes.MOVEMENT_SPEED, {speed}).add(Attributes.ATTACK_DAMAGE, {attack})
                .add(Attributes.ARMOR, {armor});
    }}

    @Override public String getPinataSpeciesName() {{ return "{name}"; }}
    @Override public List<ItemStack> getVisitFoods() {{ return List.of({visit_foods}); }}
    @Override public List<ItemStack> getResidentFoods() {{ return List.of({resident_foods}); }}
    @Override public List<ItemStack> getRomanceFoods() {{ return List.of({romance_foods}); }}
    @Override public List<ItemStack> getCandyDrops() {{ return List.of(new ItemStack(ModPinataItems.{NAME}_CANDY.get(), 1 + random.nextInt(baseCandyCount))); }}
    @Override public int getVariantCount() {{ return {variants}; }}

    @Override protected void registerPinataGoals() {{
{extra_goals}
        goalSelector.addGoal(5, new TemptGoal(this, 1.1, s -> s.is({visit_item_1}), false));
    }}

{extra_traits}

    @Nullable @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {{
        {name}Entity baby = ModPinataEntities.{NAME}.get().create(level);
        if (baby != null) {{ baby.setLifecycle(LIFECYCLE_RESIDENT); baby.setHappiness(75); }}
        return baby;
    }}
}}
'''

RENDERER_TEMPLATE = '''package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings({{"unchecked", "rawtypes"}})
public class {name}Renderer extends MobRenderer {{
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ReactiveFluids.MOD_ID, "textures/entity/pinata/{lower}.png");

    public {name}Renderer(EntityRendererProvider.Context context) {{
        super(context, new {model}(context.bakeLayer({model}.LAYER)), {shadow}F);
    }}

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.world.entity.Entity entity) {{
        return TEXTURE;
    }}
}}
'''

def make_food_refs(food_str):
    parts = [f.strip() for f in food_str.split(",")]
    return ", ".join(f"new ItemStack({p})" for p in parts)

def main():
    for spec in SPECIES:
        (name, animal, hp, speed, attack, armor,
         visit_foods, resident_foods, romance_foods,
         candy_n, variants, model, shadow, sizeW, sizeH,
         extra_goals, extra_traits, description) = spec

        NAME = name.upper()
        lower = name.lower()
        visit_item_1 = visit_foods.split(",")[0].strip()

        entity_code = ENTITY_TEMPLATE.format(
            name=name, NAME=NAME, lower=lower, description=description,
            hp=hp, speed=speed, attack=attack, armor=armor,
            visit_foods=make_food_refs(visit_foods),
            resident_foods=make_food_refs(resident_foods),
            romance_foods=make_food_refs(romance_foods),
            candy_n=candy_n, variants=variants,
            visit_item_1=visit_item_1,
            extra_goals=extra_goals if extra_goals else "        // Default goals only",
            extra_traits=extra_traits if extra_traits else "",
        )

        renderer_code = RENDERER_TEMPLATE.format(
            name=name, lower=lower, model=model, shadow=shadow
        )

        with open(os.path.join(PINATA_DIR, f"{name}Entity.java"), 'w') as f:
            f.write(entity_code)
        with open(os.path.join(PINATA_DIR, f"{name}Renderer.java"), 'w') as f:
            f.write(renderer_code)
        print(f"  Generated {name}Entity.java + {name}Renderer.java")

    # Also output registration snippets
    print("\n=== Entity Registration Snippet ===")
    for spec in SPECIES:
        name = spec[0]; NAME = name.upper(); lower = name.lower()
        sizeW = spec[13]; sizeH = spec[14]
        print(f'    public static final DeferredHolder<EntityType<?>, EntityType<{name}Entity>> {NAME} =')
        print(f'            ENTITY_TYPES.register("{lower}", () -> EntityType.Builder.<{name}Entity>of({name}Entity::new, MobCategory.CREATURE)')
        print(f'                    .sized({sizeW}F, {sizeH}F).clientTrackingRange(10).build("{lower}"));')

    print("\n=== Attribute Registration Snippet ===")
    for spec in SPECIES:
        name = spec[0]; NAME = name.upper()
        print(f'        event.put(ModPinataEntities.{NAME}.get(), {name}Entity.createAttributes().build());')

    print("\n=== Item Registration Snippet (spawn eggs + candy) ===")
    for spec in SPECIES:
        name = spec[0]; NAME = name.upper(); lower = name.lower()
        print(f'    public static final DeferredHolder<Item, DeferredSpawnEggItem> {NAME}_SPAWN_EGG =')
        print(f'            ITEMS.register("{lower}_spawn_egg", () -> new DeferredSpawnEggItem(ModPinataEntities.{NAME}, 0x808080, 0xC0C0C0, new Item.Properties()));')
        candy_n = spec[9]
        print(f'    public static final DeferredHolder<Item, Item> {NAME}_CANDY =')
        print(f'            ITEMS.register("{lower}_candy", () -> new Item(new Item.Properties().food(candyFood({candy_n}, {candy_n/10:.1f}F))));')

    print(f"\nGenerated {len(SPECIES)} species")

if __name__ == "__main__":
    main()
