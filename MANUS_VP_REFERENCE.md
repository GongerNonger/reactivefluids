# Viva Piñata: Trouble in Paradise — Complete Reference

This document covers the full game mechanics of VP:TIP for use as reference when creating textures, models, and additional mod content.

---

## Game Concept

Viva Piñata is a garden simulation where you create and manage a garden to attract living piñata creatures. Each piñata is a paper-mâché animal filled with candy. The core loop:

1. **Terraform** your garden (dig ponds, plant flowers, place grass)
2. **Attract** wild piñatas by meeting their habitat requirements
3. **Feed** them to make them visitors, then residents
4. **Romance** pairs to breed babies
5. **Evolve** piñatas by feeding special items
6. **Defend** against sour piñatas, ruffians, and Professor Pester
7. **Produce** items (honey, milk, wool) from happy piñatas

---

## Visual Style Guide

### Piñata Aesthetic
Every creature in VP is made of **crepe paper** with visible fold lines, paper frills, and a slightly crinkled texture. They look like real piñatas you'd see at a party — colorful, paper-wrapped, with visible seams.

Key visual elements:
- **Paper fold stripes** — horizontal crepe paper bands visible on the body
- **Crepe paper frills** — ruffled edges where paper sections meet
- **Bold candy colors** — each species has a signature color palette based on a candy/food pun
- **Googly-style eyes** — large, round, expressive eyes with white sclera and dark pupils
- **Visible seams** — where paper sections are "glued" together
- **Slight glossiness** — like varnished paper
- **No fur/feather detail** — everything is stylized paper, not realistic

### Color Naming Convention
Every piñata name is a pun combining an animal + a candy/food:
- **Whirlm** = worm + swirl
- **Sparrowmint** = sparrow + spearmint
- **Fudgehog** = hedgehog + fudge
- **Horstachio** = horse + pistachio
- **Pretztail** = fox (pretzel-shaped tail)
- **Buzzlegum** = bee + bubblegum
- **Cocoadile** = crocodile + cocoa

The color palette of each species reflects its candy name:
- Fudgehog = chocolate brown + caramel
- Horstachio = pistachio green + cream
- Buzzlegum = bubblegum pink/yellow + black stripes
- Dragonache = royal purple + gold

---

## Complete Species Reference (88 species)

### Size Categories
- **Tiny**: Raisant (ant), Mothdrop (moth), Taffly (fly), Reddhott (firefly)
- **Small**: Whirlm, Mousemallow, Shellybean, Sweetle, Geckie, Dragumfly
- **Medium**: Most species — Sparrowmint, Fudgehog, Bunnycomb, Lickatoad, Pretztail, etc.
- **Large**: Horstachio, Sweetooth, Sarsgorilla, Cocoadile, Limeoceros
- **Huge**: Elephanilla, Choclodocus, Dragonache

### Body Shape Categories
- **Worm/Snake**: Whirlm, Syrupent, Twingersnap, Fourheads
- **Bird (round body + wings)**: Sparrowmint, Candary, Parrybo, Eaglair, Cluckles, Chocstrich, Hootyfruity, Pengum
- **Ball/Round (hedgehog-like)**: Fudgehog, Parmadillo, Goobaa, Sweetooth, Fizzlybear, Polollybear, Walrusk, Sarsgorilla, Limeoceros, Badgesicle
- **Quadruped (fox/dog/cat)**: Pretztail, Pieena, Barkbark, Kittyfloss, Mallowolf, Roario, Tigermisu
- **Duck/Waterfowl**: Quackberry, Juicygoose, Swanana
- **Frog/Lizard (flat body)**: Lickatoad, Newtgat, Salamango, Jameleon, Geckie
- **Insect (wings + small body)**: Taffly, Reddhott, Buzzlegum, Dragumfly, Mothdrop, Raisant, Sweetle
- **Snail/Shell**: Shellybean, Cherrapin, Custacean, Jeli
- **Horse/Large quadruped**: Horstachio, Zumbug, Chewnicorn, Elephanilla, Camello, Choclodocus
- **Rodent/Small mammal**: Mousemallow, Bunnycomb, Squazzil, Cinnamonkey
- **Pig**: Rashberry, Pigxie
- **Primate**: Cinnamonkey, Sarsgorilla

### Species Color Palettes (for texture creation)

| Species | Primary Color | Secondary Color | Accent |
|---------|--------------|-----------------|--------|
| Whirlm | Hot pink (#DC508C) | Light pink (#F08CB4) | Magenta frills |
| Sparrowmint | Mint green (#7BC86C) | Light green (#B4E6A0) | Yellow-green wing tips |
| Fudgehog | Chocolate (#8B5E3C) | Caramel (#D4A056) | Fudge-brown spines |
| Mousemallow | Marshmallow white (#F5E0E8) | Pink (#FFB6D9) | Pink ears/nose |
| Syrupent | Golden amber (#D4960A) | Dark amber (#8B6508) | Diamond back pattern |
| Taffly | Toffee brown (#C87828) | Light toffee (#E8D0A0) | Amber wings |
| Bunnycomb | Honeycomb yellow (#E8C850) | Orange (#F0A030) | Honey-gold ears |
| Quackberry | Blueberry blue (#4060D0) | Purple (#8060C0) | White chest |
| Shellybean | Jelly-bean green (#90D8A0) | Pink (#F0C0D0) | Multicolor shell swirl |
| Newtgat | Nougat brown (#C08040) | Orange (#E0A060) | Darker spots |
| Lickatoad | Lollipop green (#40C040) | Yellow-green (#80E060) | Red tongue |
| Pretztail | Pretzel orange (#D06020) | Brown (#8B4513) | Twisted tail pattern |
| Buzzlegum | Bubblegum yellow (#F0D040) | Black stripes (#202020) | Pink wings |
| Cluckles | Cookie brown (#D0A070) | Red comb (#C03020) | Dark choc-chip spots |
| Horstachio | Pistachio green (#80B060) | Cream (#C8E0A0) | Darker mane |
| Barkbark | Bark brown (#8B5A2B) | Golden (#D2A06D) | Darker patches |
| Kittyfloss | Candy-floss pink (#FFB6C1) | Hot pink (#FF69B4) | White chest |
| Goobaa | Wool white (#F0F0F0) | Cream (#E0D0C0) | Pastel tint varies |
| Rashberry | Raspberry pink (#E87090) | Dark raspberry (#C04060) | Lighter belly |
| Doenut | Doughnut brown (#C09060) | Sugar-dust white (#F0E0D0) | White spots |
| Sweetooth | Caramel (#C88040) | Light caramel (#E0C090) | Darker paws |
| Mallowolf | Marshmallow white (#E8E8F0) | Blue-gray (#8090B0) | Darker back |
| Cocoadile | Cocoa brown (#5C3A1E) | Light cocoa (#8B5E3C) | Darker scales |
| Dragonache | Royal purple (#C040FF) | Gold (#FFD700) | Fire-orange belly |
| Elephanilla | Gray (#909090) | Vanilla cream (#F0E0C0) | Pink ears |
| Chewnicorn | Bubblegum pink (#FFB6C1) | Lemon (#FFFFE0) | Rainbow mane |
| Roario | Golden mane (#D4A030) | Brown body (#8B4513) | Darker face |
| Tigermisu | Tiramisu orange (#E88020) | Black stripes (#202020) | White belly |
| Parrybo | Multi: red/green/blue/yellow | Varies per variant | High saturation |
| Swanana | Banana yellow (#FFFF80) | White (#FFFFFF) | Orange beak |
| Eaglair | Éclair brown (#8B5E3C) | White head (#FFFFFF) | Yellow beak |
| Choclodocus | Chocolate (#5C3A1E) | Milk choc (#D4A06D) | Darker plates |
| Zumbug | Humbug black (#202020) | White (#F0F0F0) | Alternating stripes |
| Pengum | Tuxedo black (#202040) | White belly (#F0F0FF) | Orange feet/beak |
| Camello | Caramel (#D4A060) | Sandy cream (#F0E0C0) | Darker humps |
| Polollybear | Lollipop white (#F0F0FF) | Ice blue (#A0D0FF) | Pink nose |

---

## Garden Mechanics

### Surface Types
| Surface | VP Use | MC Equivalent |
|---------|--------|---------------|
| Grass | Default, attracts basic species | Grass Block |
| Long Grass | Attracts Fudgehog, Syrupent, Horstachio | Tall Grass on Grass Block |
| Soil/Dirt | Base terrain for planting | Dirt |
| Water/Pond | Attracts aquatic species | Water source blocks |
| Sand | Attracts desert species (Camello, Geckie) | Sand |
| Snow | Attracts arctic species (Pengum, Polollybear) | Snow Block |
| Farmland | For growing crops | Farmland |

### Garden Levels
The garden levels up as the player achieves milestones (breeding species, taming sours, growing plants). Higher levels unlock:
- Larger garden radius
- Rarer species attraction
- Better tool upgrades
- Access to NPC shops

### Attraction Requirements (VP rules)
Each species has a condition to appear at the garden edge:
- **Block counts**: "have 10 square pinometers of long grass" (1 pinometer = 1 block)
- **Other species present**: "have 1 Whirlm resident"
- **Flowers/plants**: "have 2 daisies planted"
- **Garden level**: "garden level 3+"
- **Combination**: "have 4 water blocks AND grass blocks"

---

## Romance System

### Requirements (per species)
1. Two residents of the same species
2. Both at happiness > 60
3. Species-specific food fed (romance food)
4. Species house placed nearby (some species)
5. No romance cooldown active

### Process
1. Feed romance food → piñata shows hearts
2. Two heart-bearing piñatas path toward each other
3. They dance for 5 seconds (circling + particles)
4. Storkos flies in and delivers a cosmetic egg effect
5. Baby piñata spawns (small, grows to adult)
6. Both parents get 5-minute cooldown

### Special Cases
- **Pigxie**: Cross-breed of Rashberry + Swanana (in a Mystery House)
- **Wildcards**: Rare color variants when romance requirements are exceeded
- **Twins**: Small chance of two babies

---

## Evolution Chart

| Base → Evolved | Trigger Item | MC Stand-in |
|---------------|-------------|-------------|
| Sparrowmint → Candary | Daisy | Dandelion |
| Fudgehog → Parmadillo | Coconut | Cocoa Beans |
| Horstachio → Zumbug | Daisy + Blackberry | Sweet Berries |
| Pretztail → Pieena | White bone | Bone |
| Quackberry → Juicygoose | Gooseberry | Sweet Berries |
| Taffly → Reddhott | Catch fire + extinguish | Complex (fire + watering can) |
| Newtgat → Salamango | Chili | Blaze Powder |
| Cluckles → Chocstrich | Cactus fruit | Cactus |
| Doenut → Moojoo | Fir tree seed | Spruce Sapling |
| Fizzlybear → Polollybear | Blue gem | Lapis Lazuli |
| Lickatoad → Lackatoad | Nightshade + shovel hit | Complex (not yet implemented) |

---

## Sour Piñatas

Sour versions are hostile variants with dark/cracked textures. They enter gardens and cause trouble. Each can be tamed by meeting specific conditions.

| Sour Species | Hostile Behavior | Taming Method |
|-------------|-----------------|---------------|
| Sour Shellybean | Eats flower seeds | Feed apple seed |
| Sour Mallowolf | Prevents visitors | Feed Pigxie |
| Sour Cocoadile | Attacks helpers | Feed Sweetooth + Swanana + water |
| Sour Crowla | Eats sick piñatas | Build birdbath + feed medicine |
| Sour Macaraccoon | Steals eggs | 5 Master Romancer awards + feed Cluckle |
| Sour Bonboon | Starts fights | Loses fight to Syrupent/Twingersnap |
| Sour Profitamole | Attacks flowers | 2 mushrooms + feed Red Flutterscotch |
| Sour Sherbat | Drains piñatas | Feed Jack-o'-Lantern |

When tamed, a sour piñata enters a cocoon and emerges as a friendly resident.

---

## Conflict Pairs

These species auto-fight when near each other:

| Species A | Species B |
|-----------|-----------|
| Barkbark | Kittyfloss |
| Barkbark | Roario |
| Barkbark | Tigermisu |
| Bonboon | Cinnamonkey |
| Buzzlegum | Raisant |
| Chewnicorn | Ponocky |
| Chewnicorn | Zumbug |
| Dragumfly | Reddhott |
| Horstachio | Ponocky |
| Juicygoose | Quackberry |
| Juicygoose | Swanana |
| Lackatoad | Salamango |
| Lickatoad | Newtgat |
| Pigxie | Rashberry |
| Quackberry | Swanana |
| Roario | Tigermisu |

---

## Threats

### Ruffians
- Humanoid thugs sent by Professor Pester
- Break fences, destroy decorations, capture helpers
- Bribe with chocolate coins, scare with shovel
- Tamed Mallowolf howl scares them

### Professor Pester
- Main villain, appears rarely in established gardens
- Targets most valuable piñata and destroys it
- Can be bribed (expensive — 500 chocolate coins)
- Scared by Dragonache or Choclodocus
- Limeoceros attacks him directly

### Dastardos
- Appears when a piñata is sick for too long
- Destroys sick piñatas
- Cannot be killed (only distracted)
- Tamed Crowla or Sherbat distracts him
- Counter: cure the piñata quickly via Doc Patchingo or Chewnicorn

---

## NPCs

| NPC | Role | Interaction |
|-----|------|------------|
| Leafos | Tutorial guide | Gives advice and tips |
| Seedos | Seed provider | Free random seeds on interaction |
| Storkos | Egg delivery | Flies in after romance |
| Doc Patchingo | Piñata doctor | Heals sick piñatas for payment |
| Gretchen Fetchem | Piñata finder | Sells species you've bred before |
| Willy Builder | Builder | Constructs houses and produce buildings |
| Costolot | General store | Sells seeds, tools, items |
| Miss Petula | Accessory shop | Sells piñata accessories |
| Bart | Item transformer | Converts items (honey→medicine, milk→cheese) |
| Langston | Trap seller | Sells traps for desert/arctic expeditions |

---

## Produce System

| Producer | Building | Produce | Trigger |
|----------|----------|---------|---------|
| Buzzlegum | Honey Hive | Honeycomb | Feed daisy, direct to hive (or Keeper Hat auto-produce) |
| Moozipan/Flapyak | Milking Shed | Milk | Feed sunflower, direct to shed |
| Goobaa | Shearing Shed | Wool | Direct to shed (or Bonnet auto-produce) |

---

## Accessories (9 body slots)

**Slots**: Head, Eyes, Ears, Nose, Mouth, Neck, Arms, Body, Feet

Key accessories with gameplay effects:
- **Halo of Hardness** (Head): Auto-heals sickness
- **Keeper Hat** (Head): Auto-produces at produce buildings
- **Running Shoes** (Feet): Speed boost
- **Crown** (Head): Value boost
- **Captain's Cutlass** (Body): Protection from Ruffians (destroyed by Professor Pester)

All accessories increase happiness (candiosity) by a fixed amount.
