# Dirt Slab

[![CurseForge](http://cf.way2muchnoise.eu/full_dirt-slab_downloads.svg)](http://www.curseforge.com/minecraft/mc-mods/dirt-slab)
[![CurseForge](http://cf.way2muchnoise.eu/versions/dirt-slab.svg)](http://www.curseforge.com/minecraft/mc-mods/dirt-slab)

A [Minecraft](https://minecraft.net) mod built on [Fabric](https://fabricmc.net) that adds dirt-type slab blocks with full vanilla parity.

## Slab Blocks

- Dirt
- Coarse Dirt
- Farmland
- Grass Path
- Grass
- Mud
- Mycelium
- Podzol
- Rooted Dirt

All slabs have proper recipes and appear in the Building Blocks creative tab.

## World Generation

Terrain slabs generate naturally at terrain edges, creating smoother transitions between elevations. Works with:
- All dirt-type slabs
- Stone variants (stone, deepslate, tuff, andesite, diorite, granite)
- Sandstone variants

Plants on bottom slabs automatically convert to offset-rendered slab variants.

## Plant Support

Plants render correctly on bottom slabs with proper vertical offset. Supported plants:

**Crops:** Wheat, Carrots, Potatoes, Beetroots, Torchflower, Pitcher Plant

**Flowers:** Dandelion, Poppy, Blue Orchid, Allium, Azure Bluet, Tulips (all colors), Oxeye Daisy, Cornflower, Lily of the Valley, Wither Rose, Torchflower, Eyeblossoms

**Tall Flowers:** Sunflower, Lilac, Rose Bush, Peony

**Plants:** Short Grass, Fern, Dead Bush, Dry Grass, Bush, Tall Grass, Large Fern

**Other:** Mushrooms, Pink Petals, Wildflowers, Leaf Litter, Sugar Cane, Bamboo, Cactus Flower, Firefly Bush, Sweet Berry Bush, Azalea, Moss Carpet, Pale Moss Carpet

**Dripleaf & Vines:** Small Dripleaf, Big Dripleaf, Cave Vines, Pale Hanging Moss, Spore Blossom, Hanging Roots

**Saplings:** All vanilla saplings (Oak, Spruce, Birch, Jungle, Acacia, Dark Oak, Cherry, Mangrove, Pale Oak)

## Snow Support

Snow layers work properly on bottom slabs:
- Accumulates from weather in snowy biomes
- Snow golems leave trails on slabs
- Manual placement with snow items
- Grass and mycelium slabs show snowy texture when snow is on top

## Tool Interactions

**Shovel:**
- Right-click dirt slab → Path slab
- Sneak + right-click full block or double slab → Single slab
- Sneak + right-click single slab → Toggle top/bottom placement

**Hoe:**
- Right-click coarse dirt slab → Dirt slab
- Right-click dirt/path slab → Farmland slab
- Right-click farmland → Dirt slab

## Vanilla Parity

- Grass, podzol, and mycelium spread across blocks and slabs
- Sheep eat grass from grass slabs
- Bonemeal fertilizes grass slabs
- Giant pines convert adjacent dirt/grass slabs to podzol
- Farmland slabs support crop growth
- Melons/pumpkins grow onto appropriate slab types
- Villager farmers interact with farmland slabs
- All appropriate particles and sounds

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.18.1+
- Fabric API 0.140.0+
