# Dirt Slab — Vision

## What It Is

A Fabric mod that adds half-height dirt-type blocks to Minecraft with full vanilla parity. Every behavior vanilla dirt has — spreading, farming, tool interactions, mob AI, snow accumulation, plant support, worldgen — works identically at half height.

## Who It's For

Three players, one mod:
- **The explorer** who doesn't want to mash spacebar over every one-block ledge
- **The builder** who wants finer control over terrain and landscaping
- **The player who never looks down** — the world just has more resolution

## Destination

The mod is feature-complete.

A config file (`config/dirt-slab.json`) maps full blocks to their slab variants. Add an entry, and terrain generation replaces that block at cliff edges. Default config covers vanilla dirt types and stone variants. Modpack authors extend it by adding entries, not writing code. Worldgen can be toggled off entirely via `worldgen_enabled`.

## Principles

**Vanilla parity is the bar, not a goal.** If vanilla dirt does it, dirt slabs do it. Crops grow, grass spreads, sheep eat, snow accumulates, villagers farm, melons attach. The half-block difference is geometric, not behavioral.

**The mod disappears into the game.** Terrain slabs generate naturally. Plants render correctly. Tool interactions are intuitive.

**Worldgen is opt-in and configurable.** The terrain feature converts cliff edges to slabs for smoother terrain. Which blocks participate is controlled by a block mapping in config. This is the integration surface for modded slabs — and the boundary of what this mod provides.

**Rendering serves generation.** Plants on bottom slabs render at the right height whether worldgen placed them or a player did. The rendering layer follows the world state, not the other way around.

**Sand slabs, gravel slabs, soul soil — those are different mods.**

## Version Strategy

Stable Minecraft releases only. Accept API churn at version boundaries; don't design around it.

## What Success Looks Like

Nobody files a bug report that says "this vanilla behavior doesn't work on slabs."
