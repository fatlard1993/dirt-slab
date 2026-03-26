package justfatlard.dirt_slab;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;

/**
 * Single source of truth for vanilla block → slab block mappings.
 * Terrain slab mappings are loaded from config (see DirtSlabConfig).
 * All other maps and sets are populated during class loading.
 */
public class SlabRegistry {
	private static Map<Block, Block> TERRAIN_SLABS = Collections.emptyMap();
	private static final Map<Block, Block> PLANT_SLABS;
	private static final Map<Block, Block> CROP_SLABS;

	// Shovel: vanilla full block → slab for halving
	private static final Map<Block, Block> SHOVEL_HALVE;

	// Identity sets for our terrain slabs
	private static final Set<Block> ALL_TERRAIN_SLABS;
	private static final Set<Block> DIRT_TYPE_SLABS;
	private static final Set<Block> GRASS_TYPE_SLABS;
	private static final Set<Block> PLANTABLE_SLABS;
	private static final Set<Block> SUGAR_CANE_PLANTABLE_SLABS;

	/** Initialize terrain slab mappings from config. Must be called during mod init. */
	public static void initTerrainSlabs(Map<Block, Block> terrainSlabs) {
		TERRAIN_SLABS = Collections.unmodifiableMap(new HashMap<>(terrainSlabs));
	}

	static {
		Map<Block, Block> plantSlabs = new HashMap<>();
		Map<Block, Block> cropSlabs = new HashMap<>();
		Map<Block, Block> shovelHalve = new HashMap<>();

		// Plants: vanilla plant → slab plant
		// Short plants
		plantSlabs.put(Blocks.SHORT_GRASS, DirtSlabBlocks.SHORT_GRASS_SLAB);
		plantSlabs.put(Blocks.FERN, DirtSlabBlocks.FERN_SLAB);
		plantSlabs.put(Blocks.DEAD_BUSH, DirtSlabBlocks.DEAD_BUSH_SLAB);
		plantSlabs.put(Blocks.SHORT_DRY_GRASS, DirtSlabBlocks.SHORT_DRY_GRASS_SLAB);
		plantSlabs.put(Blocks.BUSH, DirtSlabBlocks.BUSH_SLAB);
		plantSlabs.put(Blocks.TALL_DRY_GRASS, DirtSlabBlocks.TALL_DRY_GRASS_SLAB);

		// Flowers
		plantSlabs.put(Blocks.DANDELION, DirtSlabBlocks.DANDELION_SLAB);
		plantSlabs.put(Blocks.POPPY, DirtSlabBlocks.POPPY_SLAB);
		plantSlabs.put(Blocks.BLUE_ORCHID, DirtSlabBlocks.BLUE_ORCHID_SLAB);
		plantSlabs.put(Blocks.ALLIUM, DirtSlabBlocks.ALLIUM_SLAB);
		plantSlabs.put(Blocks.AZURE_BLUET, DirtSlabBlocks.AZURE_BLUET_SLAB);
		plantSlabs.put(Blocks.RED_TULIP, DirtSlabBlocks.RED_TULIP_SLAB);
		plantSlabs.put(Blocks.ORANGE_TULIP, DirtSlabBlocks.ORANGE_TULIP_SLAB);
		plantSlabs.put(Blocks.WHITE_TULIP, DirtSlabBlocks.WHITE_TULIP_SLAB);
		plantSlabs.put(Blocks.PINK_TULIP, DirtSlabBlocks.PINK_TULIP_SLAB);
		plantSlabs.put(Blocks.OXEYE_DAISY, DirtSlabBlocks.OXEYE_DAISY_SLAB);
		plantSlabs.put(Blocks.CORNFLOWER, DirtSlabBlocks.CORNFLOWER_SLAB);
		plantSlabs.put(Blocks.LILY_OF_THE_VALLEY, DirtSlabBlocks.LILY_OF_THE_VALLEY_SLAB);
		plantSlabs.put(Blocks.WITHER_ROSE, DirtSlabBlocks.WITHER_ROSE_SLAB);
		plantSlabs.put(Blocks.TORCHFLOWER, DirtSlabBlocks.TORCHFLOWER_SLAB);
		plantSlabs.put(Blocks.OPEN_EYEBLOSSOM, DirtSlabBlocks.OPEN_EYEBLOSSOM_SLAB);
		plantSlabs.put(Blocks.CLOSED_EYEBLOSSOM, DirtSlabBlocks.CLOSED_EYEBLOSSOM_SLAB);

		// Mushrooms
		plantSlabs.put(Blocks.RED_MUSHROOM, DirtSlabBlocks.RED_MUSHROOM_SLAB);
		plantSlabs.put(Blocks.BROWN_MUSHROOM, DirtSlabBlocks.BROWN_MUSHROOM_SLAB);

		// Tall plants
		plantSlabs.put(Blocks.TALL_GRASS, DirtSlabBlocks.TALL_GRASS_SLAB);
		plantSlabs.put(Blocks.LARGE_FERN, DirtSlabBlocks.LARGE_FERN_SLAB);

		// Tall flowers
		plantSlabs.put(Blocks.SUNFLOWER, DirtSlabBlocks.SUNFLOWER_SLAB);
		plantSlabs.put(Blocks.LILAC, DirtSlabBlocks.LILAC_SLAB);
		plantSlabs.put(Blocks.ROSE_BUSH, DirtSlabBlocks.ROSE_BUSH_SLAB);
		plantSlabs.put(Blocks.PEONY, DirtSlabBlocks.PEONY_SLAB);

		// Special plants
		plantSlabs.put(Blocks.LEAF_LITTER, DirtSlabBlocks.LEAF_LITTER_SLAB);
		plantSlabs.put(Blocks.PINK_PETALS, DirtSlabBlocks.PINK_PETALS_SLAB);
		plantSlabs.put(Blocks.WILDFLOWERS, DirtSlabBlocks.WILDFLOWERS_SLAB);
		plantSlabs.put(Blocks.CACTUS_FLOWER, DirtSlabBlocks.CACTUS_FLOWER_SLAB);
		plantSlabs.put(Blocks.FIREFLY_BUSH, DirtSlabBlocks.FIREFLY_BUSH_SLAB);
		plantSlabs.put(Blocks.SWEET_BERRY_BUSH, DirtSlabBlocks.SWEET_BERRY_BUSH_SLAB);
		plantSlabs.put(Blocks.AZALEA, DirtSlabBlocks.AZALEA_SLAB);
		plantSlabs.put(Blocks.FLOWERING_AZALEA, DirtSlabBlocks.AZALEA_SLAB);
		plantSlabs.put(Blocks.SUGAR_CANE, DirtSlabBlocks.SUGAR_CANE_SLAB);
		plantSlabs.put(Blocks.BAMBOO_SAPLING, DirtSlabBlocks.BAMBOO_SHOOT_SLAB);
		plantSlabs.put(Blocks.BAMBOO, DirtSlabBlocks.BAMBOO_SLAB);

		// Saplings
		plantSlabs.put(Blocks.OAK_SAPLING, DirtSlabBlocks.OAK_SAPLING_SLAB);
		plantSlabs.put(Blocks.SPRUCE_SAPLING, DirtSlabBlocks.SPRUCE_SAPLING_SLAB);
		plantSlabs.put(Blocks.BIRCH_SAPLING, DirtSlabBlocks.BIRCH_SAPLING_SLAB);
		plantSlabs.put(Blocks.JUNGLE_SAPLING, DirtSlabBlocks.JUNGLE_SAPLING_SLAB);
		plantSlabs.put(Blocks.ACACIA_SAPLING, DirtSlabBlocks.ACACIA_SAPLING_SLAB);
		plantSlabs.put(Blocks.DARK_OAK_SAPLING, DirtSlabBlocks.DARK_OAK_SAPLING_SLAB);
		plantSlabs.put(Blocks.CHERRY_SAPLING, DirtSlabBlocks.CHERRY_SAPLING_SLAB);
		plantSlabs.put(Blocks.MANGROVE_PROPAGULE, DirtSlabBlocks.MANGROVE_PROPAGULE_SLAB);
		plantSlabs.put(Blocks.PALE_OAK_SAPLING, DirtSlabBlocks.PALE_OAK_SAPLING_SLAB);

		// Terrain slab identity sets
		Set<Block> allTerrainSlabs = new HashSet<>(List.of(
			DirtSlabBlocks.COARSE_DIRT_SLAB, DirtSlabBlocks.DIRT_SLAB,
			DirtSlabBlocks.FARMLAND_SLAB, DirtSlabBlocks.GRASS_PATH_SLAB,
			DirtSlabBlocks.GRASS_SLAB, DirtSlabBlocks.MUD_SLAB,
			DirtSlabBlocks.MYCELIUM_SLAB, DirtSlabBlocks.PODZOL_SLAB,
			DirtSlabBlocks.ROOTED_DIRT_SLAB
		));

		Set<Block> dirtTypeSlabs = Set.of(
			DirtSlabBlocks.COARSE_DIRT_SLAB, DirtSlabBlocks.DIRT_SLAB,
			DirtSlabBlocks.FARMLAND_SLAB, DirtSlabBlocks.PODZOL_SLAB
		);

		Set<Block> grassTypeSlabs = new HashSet<>(dirtTypeSlabs);
		grassTypeSlabs.add(DirtSlabBlocks.GRASS_SLAB);
		grassTypeSlabs.add(DirtSlabBlocks.MYCELIUM_SLAB);

		// Plantable: grass types + mud + rooted dirt (superset used by isPlantable)
		Set<Block> plantableSlabs = new HashSet<>(grassTypeSlabs);
		plantableSlabs.add(DirtSlabBlocks.MUD_SLAB);
		plantableSlabs.add(DirtSlabBlocks.ROOTED_DIRT_SLAB);

		Set<Block> sugarCanePlantableSlabs = Set.of(
			DirtSlabBlocks.GRASS_SLAB, DirtSlabBlocks.DIRT_SLAB,
			DirtSlabBlocks.COARSE_DIRT_SLAB, DirtSlabBlocks.PODZOL_SLAB,
			DirtSlabBlocks.MUD_SLAB, DirtSlabBlocks.ROOTED_DIRT_SLAB
		);

		// Crops: vanilla crop block → slab crop block
		cropSlabs.put(Blocks.WHEAT, DirtSlabBlocks.WHEAT_SLAB_CROP);
		cropSlabs.put(Blocks.CARROTS, DirtSlabBlocks.CARROT_SLAB_CROP);
		cropSlabs.put(Blocks.POTATOES, DirtSlabBlocks.POTATO_SLAB_CROP);
		cropSlabs.put(Blocks.BEETROOTS, DirtSlabBlocks.BEETROOT_SLAB_CROP);
		cropSlabs.put(Blocks.MELON_STEM, DirtSlabBlocks.MELON_STEM_SLAB);
		cropSlabs.put(Blocks.PUMPKIN_STEM, DirtSlabBlocks.PUMPKIN_STEM_SLAB);
		cropSlabs.put(Blocks.TORCHFLOWER_CROP, DirtSlabBlocks.TORCHFLOWER_CROP_SLAB);
		cropSlabs.put(Blocks.PITCHER_CROP, DirtSlabBlocks.PITCHER_CROP_SLAB);

		// Shovel halving: vanilla block or double slab → slab for sneak+shovel
		shovelHalve.put(Blocks.DIRT, DirtSlabBlocks.DIRT_SLAB);
		shovelHalve.put(Blocks.GRASS_BLOCK, DirtSlabBlocks.GRASS_SLAB);
		shovelHalve.put(Blocks.COARSE_DIRT, DirtSlabBlocks.COARSE_DIRT_SLAB);
		shovelHalve.put(Blocks.FARMLAND, DirtSlabBlocks.FARMLAND_SLAB);
		shovelHalve.put(Blocks.DIRT_PATH, DirtSlabBlocks.GRASS_PATH_SLAB);
		shovelHalve.put(Blocks.MUD, DirtSlabBlocks.MUD_SLAB);
		shovelHalve.put(Blocks.MYCELIUM, DirtSlabBlocks.MYCELIUM_SLAB);
		shovelHalve.put(Blocks.PODZOL, DirtSlabBlocks.PODZOL_SLAB);
		shovelHalve.put(Blocks.ROOTED_DIRT, DirtSlabBlocks.ROOTED_DIRT_SLAB);

		// Freeze all collections
		PLANT_SLABS = Collections.unmodifiableMap(plantSlabs);
		CROP_SLABS = Collections.unmodifiableMap(cropSlabs);
		SHOVEL_HALVE = Collections.unmodifiableMap(shovelHalve);
		ALL_TERRAIN_SLABS = Collections.unmodifiableSet(allTerrainSlabs);
		DIRT_TYPE_SLABS = dirtTypeSlabs;
		GRASS_TYPE_SLABS = Collections.unmodifiableSet(grassTypeSlabs);
		PLANTABLE_SLABS = Collections.unmodifiableSet(plantableSlabs);
		SUGAR_CANE_PLANTABLE_SLABS = sugarCanePlantableSlabs;
	}

	/** Get the slab block for a vanilla terrain block, or null. */
	public static Block getTerrainSlab(Block vanillaBlock) {
		return TERRAIN_SLABS.get(vanillaBlock);
	}

	/** Get the default state of the slab for a vanilla terrain block, or null. */
	public static BlockState getTerrainSlabState(Block vanillaBlock) {
		Block slab = TERRAIN_SLABS.get(vanillaBlock);
		return slab != null ? slab.getDefaultState() : null;
	}

	/** Get the slab block for a vanilla plant block, or null. */
	public static Block getPlantSlab(Block vanillaBlock) {
		return PLANT_SLABS.get(vanillaBlock);
	}

	/** Get the default state of the slab for a vanilla plant block, or null. */
	public static BlockState getPlantSlabDefaultState(Block vanillaBlock) {
		Block slab = PLANT_SLABS.get(vanillaBlock);
		return slab != null ? slab.getDefaultState() : null;
	}

	/** Check if a block is a vanilla plant we have a slab variant for. */
	public static boolean isVegetation(Block block) {
		return PLANT_SLABS.containsKey(block);
	}

	/** Check if a block is a vanilla terrain block we can convert to a slab. */
	public static boolean isConvertibleTerrain(Block block) {
		return TERRAIN_SLABS.containsKey(block);
	}

	/** Check if a block is one of our terrain slabs. */
	public static boolean isTerrainSlab(Block block) {
		return block != null && ALL_TERRAIN_SLABS.contains(block);
	}

	/** Check if a block is a dirt-type slab (coarse dirt, dirt, farmland, podzol). */
	public static boolean isDirtType(Block block) {
		return DIRT_TYPE_SLABS.contains(block);
	}

	/** Check if a block is a grass-type slab (dirt types + grass, mycelium). */
	public static boolean isGrassType(Block block) {
		return GRASS_TYPE_SLABS.contains(block);
	}

	/** Check if a block is a slab that sugar cane can be planted on. */
	public static boolean isSugarCanePlantable(Block block) {
		return SUGAR_CANE_PLANTABLE_SLABS.contains(block);
	}

	/** Check if a block is a slab that general plants (flowers, short plants) can grow on. Includes grass types, mud, and rooted dirt. */
	public static boolean isPlantable(Block block) {
		return PLANTABLE_SLABS.contains(block);
	}

	/** Check if a block is a slab that flowers specifically can grow on (excludes mud and rooted dirt, matching vanilla). */
	public static boolean isFlowerPlantable(Block block) {
		return GRASS_TYPE_SLABS.contains(block);
	}

	/** Get the slab crop block for a vanilla crop/stem block, or null. */
	public static Block getCropSlab(Block vanillaCrop) {
		return CROP_SLABS.get(vanillaCrop);
	}

	/** Get the slab for shovel-halving a vanilla block or double slab. */
	public static Block getShovelHalveResult(Block block) {
		// Check vanilla blocks first
		Block result = SHOVEL_HALVE.get(block);
		if (result != null) return result;
		// Check if it's already one of our terrain slabs (double → single)
		if (ALL_TERRAIN_SLABS.contains(block)) return block;
		return null;
	}

	/** Copy TYPE and WATERLOGGED from one slab state to another slab's default state. Other properties (SNOWY, MOISTURE) are intentionally dropped. */
	public static BlockState copySlabProperties(BlockState from, Block toBlock) {
		return toBlock.getDefaultState()
			.with(SlabBlock.TYPE, from.get(SlabBlock.TYPE))
			.with(SlabBlock.WATERLOGGED, from.get(SlabBlock.WATERLOGGED));
	}

	/** Check if a block is valid ground for gourd (melon/pumpkin) placement. Vanilla dirt-family blocks only. */
	public static boolean isGourdGround(Block block) {
		return block == Blocks.FARMLAND || block == Blocks.DIRT || block == Blocks.COARSE_DIRT ||
			   block == Blocks.PODZOL || block == Blocks.GRASS_BLOCK || block == Blocks.MOSS_BLOCK ||
			   block == Blocks.MUD || block == Blocks.MUDDY_MANGROVE_ROOTS ||
			   block == Blocks.MYCELIUM || block == Blocks.ROOTED_DIRT;
	}

	/** Get the spread result: what does this spreader turn a dirt slab into? Returns null if not a spreader. */
	public static Block getSpreadResult(Block spreader) {
		if (spreader == DirtSlabBlocks.GRASS_SLAB || spreader == Blocks.GRASS_BLOCK) return DirtSlabBlocks.GRASS_SLAB;
		if (spreader == DirtSlabBlocks.MYCELIUM_SLAB || spreader == Blocks.MYCELIUM) return DirtSlabBlocks.MYCELIUM_SLAB;
		return null;
	}

	/** Get the vanilla spread result: what does this slab spreader turn vanilla dirt into? Returns null if not a slab spreader. */
	public static Block getVanillaSpreadResult(Block spreader) {
		if (spreader == DirtSlabBlocks.GRASS_SLAB) return Blocks.GRASS_BLOCK;
		if (spreader == DirtSlabBlocks.MYCELIUM_SLAB) return Blocks.MYCELIUM;
		return null;
	}
}
