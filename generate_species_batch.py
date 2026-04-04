#!/usr/bin/env python3
"""
Batch generate piñata entity + renderer Java files from species definitions.
Run: python3 generate_species_batch.py
Outputs to src/main/java/com/reactivefluids/pinata/
"""
import os

PINATA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)),
    "src", "main", "java", "com", "reactivefluids", "pinata")

# Each species: (name, animal, hp, speed, attack, armor, visitFoods, residentFoods,
#   romanceFoods, candyNutrition, variants, sharedModel, shadowRadius, sizeW, sizeH,
#   extraGoals, extraTraits, description)

SPECIES = [
    # Batch 6 — large/medium species
    ("Elephanilla", "elephant", 40, 0.2, 4.0, 6.0,
     "Items.HAY_BLOCK, Items.APPLE", "Items.HAY_BLOCK", "Items.GOLDEN_APPLE",
     7, 2, "HorstachioModel", 0.7, 1.6, 1.8,
     "", "// Largest non-dragon piñata", "Elephant piñata — enormous, gentle giant. Loves hay."),

    ("Chewnicorn", "unicorn", 30, 0.3, 3.0, 4.0,
     "Items.GOLDEN_CARROT, Items.APPLE", "Items.GOLDEN_CARROT", "Items.ENCHANTED_GOLDEN_APPLE",
     6, 3, "HorstachioModel", 0.6, 1.3, 1.6,
     "", "// Healing aura — nearby piñatas slowly gain happiness",
     "Unicorn piñata — magical, heals nearby piñatas. Chewing gum coloring."),

    ("Roario", "lion", 26, 0.3, 6.0, 3.0,
     "Items.COOKED_BEEF, Items.BONE", "Items.COOKED_BEEF", "Items.COOKED_PORKCHOP",
     6, 2, "PretztailModel", 0.5, 0.9, 0.9,
     '        goalSelector.addGoal(2, new com.reactivefluids.pinata.ai.SpeciesConflictGoal(this, () -> ModPinataEntities.TIGERMISU.get(), 10.0));',
     "", "Lion piñata — king of the garden. Conflicts with Tigermisu."),

    ("Tigermisu", "tiger", 24, 0.32, 5.0, 2.0,
     "Items.COOKED_PORKCHOP, Items.COD", "Items.COOKED_PORKCHOP", "Items.COOKED_BEEF",
     6, 3, "PretztailModel", 0.5, 0.9, 0.9,
     '        goalSelector.addGoal(2, new com.reactivefluids.pinata.ai.SpeciesConflictGoal(this, () -> ModPinataEntities.ROARIO.get(), 10.0));',
     "", "Tiger piñata — tiramisu-striped. Conflicts with Roario."),

    ("Parrybo", "parrot", 10, 0.28, 1.0, 0.0,
     "Items.MELON_SEEDS, Items.WHEAT_SEEDS", "Items.MELON_SEEDS", "Items.PUMPKIN_SEEDS",
     3, 4, "SparrowmintModel", 0.3, 0.45, 0.6,
     "", "", "Parrot piñata — colorful, mimics sounds. Many color variants."),

    ("Swanana", "swan", 16, 0.25, 2.0, 0.0,
     "Items.WHEAT_SEEDS, Items.BREAD", "Items.BREAD", "Items.CAKE",
     5, 2, "QuackberryModel", 0.4, 0.7, 0.8,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.WATER, 1.0, 20));',
     "    @Override public boolean canBreatheUnderwater() { return true; }",
     "Swan piñata — elegant aquatic. Banana-yellow coloring. Part of Pigxie recipe."),

    ("Eaglair", "eagle", 20, 0.3, 4.0, 1.0,
     "Items.RABBIT, Items.CHICKEN", "Items.RABBIT", "Items.COOKED_RABBIT",
     5, 2, "SparrowmintModel", 0.4, 0.7, 0.7,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.HuntPreyGoal(this, () -> ModPinataEntities.SQUAZZIL.get(), 1.4, 16.0));',
     "", "Eagle piñata — majestic predator. Eclair-pastry coloring. Hunts Squazzils."),

    ("Badgesicle", "badger", 16, 0.25, 3.0, 3.0,
     "Items.SWEET_BERRIES, Items.APPLE", "Items.SWEET_BERRIES", "Items.GLOW_BERRIES",
     4, 2, "FudgehogModel", 0.35, 0.6, 0.5,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.SHORT_GRASS, 0.9, 16));',
     "", "Badger piñata — ice-lolly striped. Tough burrower."),

    ("Hootyfruity", "owl", 12, 0.22, 2.0, 0.0,
     "Items.SPIDER_EYE, Items.ROTTEN_FLESH", "Items.SPIDER_EYE", "Items.FERMENTED_SPIDER_EYE",
     4, 2, "SparrowmintModel", 0.3, 0.5, 0.6,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.HuntPreyGoal(this, () -> ModPinataEntities.MOUSEMALLOW.get(), 1.2, 12.0));',
     "", "Owl piñata — fruit-salad coloring. Nocturnal hunter of Mousemallows."),

    ("Dragumfly", "dragonfly", 8, 0.35, 1.0, 0.0,
     "Items.GLOW_BERRIES, Items.OXEYE_DAISY", "Items.GLOW_BERRIES", "Items.GLOWSTONE_DUST",
     3, 3, "TafflyModel", 0.2, 0.35, 0.35,
     '        goalSelector.addGoal(3, new com.reactivefluids.pinata.ai.AttractedToBlockGoal(this, () -> net.minecraft.world.level.block.Blocks.WATER, 1.1, 20));',
     "", "Dragonfly piñata — bubblegum-colored. Fast flyer attracted to water."),
]

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
    """Convert 'Items.HAY_BLOCK, Items.APPLE' to 'new ItemStack(Items.HAY_BLOCK), new ItemStack(Items.APPLE)'"""
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

        # First visit food item for tempt goal
        visit_item_1 = visit_foods.split(",")[0].strip()

        entity_code = ENTITY_TEMPLATE.format(
            name=name, NAME=NAME, lower=lower,
            description=description,
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

        entity_path = os.path.join(PINATA_DIR, f"{name}Entity.java")
        renderer_path = os.path.join(PINATA_DIR, f"{name}Renderer.java")

        with open(entity_path, 'w') as f:
            f.write(entity_code)
        with open(renderer_path, 'w') as f:
            f.write(renderer_code)

        print(f"  Generated {name}Entity.java + {name}Renderer.java")

    print(f"\nGenerated {len(SPECIES)} species ({len(SPECIES)*2} files)")

if __name__ == "__main__":
    main()
