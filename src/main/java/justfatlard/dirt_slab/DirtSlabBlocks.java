package justfatlard.dirt_slab;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;

public class DirtSlabBlocks {
	public static final String MOD_ID = "dirt-slab-justfatlard";

	public static final Block COARSE_DIRT_SLAB = new SlabBlock(AbstractBlock.Settings.copy(Blocks.COARSE_DIRT).registryKey(blockKey("coarse_dirt_slab")));
	public static final Block DIRT_SLAB = new SlabBlock(AbstractBlock.Settings.copy(Blocks.DIRT).registryKey(blockKey("dirt_slab")));
	public static final Block FARMLAND_SLAB = new FarmlandSlab(AbstractBlock.Settings.copy(Blocks.FARMLAND).registryKey(blockKey("farmland_slab")));
	public static final Block GRASS_PATH_SLAB = new SlicedTopSlab(AbstractBlock.Settings.copy(Blocks.DIRT_PATH).registryKey(blockKey("grass_path_slab")));
	public static final Block GRASS_SLAB = new GrassSlab(AbstractBlock.Settings.copy(Blocks.GRASS_BLOCK).ticksRandomly().registryKey(blockKey("grass_slab")));
	public static final Block MUD_SLAB = new MudSlab(AbstractBlock.Settings.copy(Blocks.MUD).registryKey(blockKey("mud_slab")));
	public static final Block MYCELIUM_SLAB = new SpreadableSlab(AbstractBlock.Settings.copy(Blocks.MYCELIUM).registryKey(blockKey("mycelium_slab")), Blocks.MYCELIUM);
	public static final Block PODZOL_SLAB = new SlabBlock(AbstractBlock.Settings.copy(Blocks.PODZOL).registryKey(blockKey("podzol_slab")));
	public static final Block ROOTED_DIRT_SLAB = new RootedDirtSlab(AbstractBlock.Settings.copy(Blocks.ROOTED_DIRT).registryKey(blockKey("rooted_dirt_slab")));

	// Slab crop blocks (for proper rendering on bottom slabs)
	public static final Block WHEAT_SLAB_CROP = new SlabCropBlock(AbstractBlock.Settings.copy(Blocks.WHEAT).registryKey(blockKey("wheat_slab_crop")), Items.WHEAT_SEEDS);
	public static final Block CARROT_SLAB_CROP = new SlabCropBlock(AbstractBlock.Settings.copy(Blocks.CARROTS).registryKey(blockKey("carrot_slab_crop")), Items.CARROT);
	public static final Block POTATO_SLAB_CROP = new SlabCropBlock(AbstractBlock.Settings.copy(Blocks.POTATOES).registryKey(blockKey("potato_slab_crop")), Items.POTATO);
	public static final Block BEETROOT_SLAB_CROP = new BeetrootSlabCropBlock(AbstractBlock.Settings.copy(Blocks.BEETROOTS).registryKey(blockKey("beetroot_slab_crop")));
	public static final Block TORCHFLOWER_CROP_SLAB = new SlabTorchflowerCropBlock(AbstractBlock.Settings.copy(Blocks.TORCHFLOWER_CROP).registryKey(blockKey("torchflower_crop_slab")));
	public static final Block PITCHER_CROP_SLAB = new SlabPitcherCropBlock(AbstractBlock.Settings.copy(Blocks.PITCHER_CROP).registryKey(blockKey("pitcher_crop_slab")));
	public static final Block PITCHER_PLANT_SLAB = new SlabPitcherPlantBlock(AbstractBlock.Settings.copy(Blocks.PITCHER_PLANT).registryKey(blockKey("pitcher_plant_slab")));

	// Slab stem blocks (for melon/pumpkin on bottom slabs)
	public static final Block MELON_STEM_SLAB = new SlabStemBlock(true, Items.MELON_SEEDS, AbstractBlock.Settings.copy(Blocks.MELON_STEM).registryKey(blockKey("melon_stem_slab")));
	public static final Block PUMPKIN_STEM_SLAB = new SlabStemBlock(false, Items.PUMPKIN_SEEDS, AbstractBlock.Settings.copy(Blocks.PUMPKIN_STEM).registryKey(blockKey("pumpkin_stem_slab")));

	// Slab attached stem blocks (for connected stems on bottom slabs)
	public static final Block ATTACHED_MELON_STEM_SLAB = new SlabAttachedStemBlock(true, Items.MELON_SEEDS, AbstractBlock.Settings.copy(Blocks.ATTACHED_MELON_STEM).registryKey(blockKey("attached_melon_stem_slab")));
	public static final Block ATTACHED_PUMPKIN_STEM_SLAB = new SlabAttachedStemBlock(false, Items.PUMPKIN_SEEDS, AbstractBlock.Settings.copy(Blocks.ATTACHED_PUMPKIN_STEM).registryKey(blockKey("attached_pumpkin_stem_slab")));

	// Slab flower blocks (for proper rendering on bottom slabs)
	public static final Block DANDELION_SLAB = new SlabFlowerBlock(StatusEffects.SATURATION, 0.35F, AbstractBlock.Settings.copy(Blocks.DANDELION).registryKey(blockKey("dandelion_slab")));
	public static final Block POPPY_SLAB = new SlabFlowerBlock(StatusEffects.NIGHT_VISION, 5.0F, AbstractBlock.Settings.copy(Blocks.POPPY).registryKey(blockKey("poppy_slab")));
	public static final Block BLUE_ORCHID_SLAB = new SlabFlowerBlock(StatusEffects.SATURATION, 0.35F, AbstractBlock.Settings.copy(Blocks.BLUE_ORCHID).registryKey(blockKey("blue_orchid_slab")));
	public static final Block ALLIUM_SLAB = new SlabFlowerBlock(StatusEffects.FIRE_RESISTANCE, 4.0F, AbstractBlock.Settings.copy(Blocks.ALLIUM).registryKey(blockKey("allium_slab")));
	public static final Block AZURE_BLUET_SLAB = new SlabFlowerBlock(StatusEffects.BLINDNESS, 8.0F, AbstractBlock.Settings.copy(Blocks.AZURE_BLUET).registryKey(blockKey("azure_bluet_slab")));
	public static final Block RED_TULIP_SLAB = new SlabFlowerBlock(StatusEffects.WEAKNESS, 9.0F, AbstractBlock.Settings.copy(Blocks.RED_TULIP).registryKey(blockKey("red_tulip_slab")));
	public static final Block ORANGE_TULIP_SLAB = new SlabFlowerBlock(StatusEffects.WEAKNESS, 9.0F, AbstractBlock.Settings.copy(Blocks.ORANGE_TULIP).registryKey(blockKey("orange_tulip_slab")));
	public static final Block WHITE_TULIP_SLAB = new SlabFlowerBlock(StatusEffects.WEAKNESS, 9.0F, AbstractBlock.Settings.copy(Blocks.WHITE_TULIP).registryKey(blockKey("white_tulip_slab")));
	public static final Block PINK_TULIP_SLAB = new SlabFlowerBlock(StatusEffects.WEAKNESS, 9.0F, AbstractBlock.Settings.copy(Blocks.PINK_TULIP).registryKey(blockKey("pink_tulip_slab")));
	public static final Block OXEYE_DAISY_SLAB = new SlabFlowerBlock(StatusEffects.REGENERATION, 8.0F, AbstractBlock.Settings.copy(Blocks.OXEYE_DAISY).registryKey(blockKey("oxeye_daisy_slab")));
	public static final Block CORNFLOWER_SLAB = new SlabFlowerBlock(StatusEffects.JUMP_BOOST, 6.0F, AbstractBlock.Settings.copy(Blocks.CORNFLOWER).registryKey(blockKey("cornflower_slab")));
	public static final Block LILY_OF_THE_VALLEY_SLAB = new SlabFlowerBlock(StatusEffects.POISON, 12.0F, AbstractBlock.Settings.copy(Blocks.LILY_OF_THE_VALLEY).registryKey(blockKey("lily_of_the_valley_slab")));
	public static final Block WITHER_ROSE_SLAB = new SlabFlowerBlock(StatusEffects.WITHER, 8.0F, AbstractBlock.Settings.copy(Blocks.WITHER_ROSE).registryKey(blockKey("wither_rose_slab")));
	public static final Block TORCHFLOWER_SLAB = new SlabFlowerBlock(StatusEffects.NIGHT_VISION, 5.0F, AbstractBlock.Settings.copy(Blocks.TORCHFLOWER).registryKey(blockKey("torchflower_slab")));
	public static final Block OPEN_EYEBLOSSOM_SLAB = new SlabFlowerBlock(StatusEffects.NAUSEA, 9.0F, AbstractBlock.Settings.copy(Blocks.OPEN_EYEBLOSSOM).registryKey(blockKey("open_eyeblossom_slab")));
	public static final Block CLOSED_EYEBLOSSOM_SLAB = new SlabFlowerBlock(StatusEffects.BLINDNESS, 11.0F, AbstractBlock.Settings.copy(Blocks.CLOSED_EYEBLOSSOM).registryKey(blockKey("closed_eyeblossom_slab")));

	// Slab tall flower blocks (for proper rendering of 2-block flowers on bottom slabs)
	public static final Block SUNFLOWER_SLAB = new SlabTallFlowerBlock(AbstractBlock.Settings.copy(Blocks.SUNFLOWER).registryKey(blockKey("sunflower_slab")));
	public static final Block LILAC_SLAB = new SlabTallFlowerBlock(AbstractBlock.Settings.copy(Blocks.LILAC).registryKey(blockKey("lilac_slab")));
	public static final Block ROSE_BUSH_SLAB = new SlabTallFlowerBlock(AbstractBlock.Settings.copy(Blocks.ROSE_BUSH).registryKey(blockKey("rose_bush_slab")));
	public static final Block PEONY_SLAB = new SlabTallFlowerBlock(AbstractBlock.Settings.copy(Blocks.PEONY).registryKey(blockKey("peony_slab")));

	// Slab mushroom blocks (for proper rendering on bottom slabs)
	public static final Block RED_MUSHROOM_SLAB = new SlabMushroomBlock(AbstractBlock.Settings.copy(Blocks.RED_MUSHROOM).registryKey(blockKey("red_mushroom_slab")));
	public static final Block BROWN_MUSHROOM_SLAB = new SlabMushroomBlock(AbstractBlock.Settings.copy(Blocks.BROWN_MUSHROOM).registryKey(blockKey("brown_mushroom_slab")));

	// Slab pink petals (for proper rendering on bottom slabs)
	public static final Block PINK_PETALS_SLAB = new SlabPinkPetalsBlock(AbstractBlock.Settings.copy(Blocks.PINK_PETALS).registryKey(blockKey("pink_petals_slab")));

	// Slab wildflowers (for proper rendering on bottom slabs)
	public static final Block WILDFLOWERS_SLAB = new SlabWildflowersBlock(AbstractBlock.Settings.copy(Blocks.WILDFLOWERS).registryKey(blockKey("wildflowers_slab")));

	// Slab short plants (for proper rendering on bottom slabs)
	public static final Block SHORT_GRASS_SLAB = new SlabShortPlantBlock(AbstractBlock.Settings.copy(Blocks.SHORT_GRASS).registryKey(blockKey("short_grass_slab")));
	public static final Block FERN_SLAB = new SlabShortPlantBlock(AbstractBlock.Settings.copy(Blocks.FERN).registryKey(blockKey("fern_slab")));
	public static final Block DEAD_BUSH_SLAB = new SlabShortPlantBlock(AbstractBlock.Settings.copy(Blocks.DEAD_BUSH).registryKey(blockKey("dead_bush_slab")));
	public static final Block SHORT_DRY_GRASS_SLAB = new SlabShortPlantBlock(AbstractBlock.Settings.copy(Blocks.SHORT_DRY_GRASS).registryKey(blockKey("short_dry_grass_slab")));
	public static final Block BUSH_SLAB = new SlabShortPlantBlock(AbstractBlock.Settings.copy(Blocks.BUSH).registryKey(blockKey("bush_slab")));

	// Slab tall plants (for proper rendering on bottom slabs)
	public static final Block TALL_GRASS_SLAB = new SlabTallPlantBlock(AbstractBlock.Settings.copy(Blocks.TALL_GRASS).registryKey(blockKey("tall_grass_slab")));
	public static final Block LARGE_FERN_SLAB = new SlabTallPlantBlock(AbstractBlock.Settings.copy(Blocks.LARGE_FERN).registryKey(blockKey("large_fern_slab")));
	// Tall dry grass is a single-block plant (not a double plant like tall grass)
	public static final Block TALL_DRY_GRASS_SLAB = new SlabShortPlantBlock(AbstractBlock.Settings.copy(Blocks.TALL_DRY_GRASS).registryKey(blockKey("tall_dry_grass_slab")));

	// Slab leaf litter (for proper rendering on bottom slabs)
	public static final Block LEAF_LITTER_SLAB = new SlabLeafLitterBlock(AbstractBlock.Settings.copy(Blocks.LEAF_LITTER).registryKey(blockKey("leaf_litter_slab")));

	// Slab hanging roots (for proper rendering from top slabs)
	public static final Block HANGING_ROOTS_SLAB = new SlabHangingRootsBlock(AbstractBlock.Settings.copy(Blocks.HANGING_ROOTS).registryKey(blockKey("hanging_roots_slab")));

	// Slab sugar cane (for proper rendering on bottom slabs)
	public static final Block SUGAR_CANE_SLAB = new SlabSugarCaneBlock(AbstractBlock.Settings.copy(Blocks.SUGAR_CANE).ticksRandomly().registryKey(blockKey("sugar_cane_slab")));

	// Slab bamboo (for proper rendering on bottom slabs)
	public static final Block BAMBOO_SHOOT_SLAB = new SlabBambooShootBlock(AbstractBlock.Settings.copy(Blocks.BAMBOO_SAPLING).ticksRandomly().registryKey(blockKey("bamboo_shoot_slab")));
	public static final Block BAMBOO_SLAB = new SlabBambooBlock(AbstractBlock.Settings.copy(Blocks.BAMBOO).ticksRandomly().registryKey(blockKey("bamboo_slab")));

	// Slab spore blossom (for proper rendering from top slabs)
	public static final Block SPORE_BLOSSOM_SLAB = new SlabSporeBlossomBlock(AbstractBlock.Settings.copy(Blocks.SPORE_BLOSSOM).registryKey(blockKey("spore_blossom_slab")));

	// Slab cactus flower (for proper rendering on bottom slabs)
	public static final Block CACTUS_FLOWER_SLAB = new SlabCactusFlowerBlock(AbstractBlock.Settings.copy(Blocks.CACTUS_FLOWER).registryKey(blockKey("cactus_flower_slab")));

	// Slab firefly bush (for proper rendering on bottom slabs)
	public static final Block FIREFLY_BUSH_SLAB = new SlabFireflyBushBlock(AbstractBlock.Settings.copy(Blocks.FIREFLY_BUSH).registryKey(blockKey("firefly_bush_slab")));

	// Slab moss carpet (for proper rendering on bottom slabs)
	public static final Block MOSS_CARPET_SLAB = new SlabMossCarpetBlock(AbstractBlock.Settings.copy(Blocks.MOSS_CARPET).registryKey(blockKey("moss_carpet_slab")));
	public static final Block PALE_MOSS_CARPET_SLAB = new SlabMossCarpetBlock(AbstractBlock.Settings.copy(Blocks.PALE_MOSS_CARPET).registryKey(blockKey("pale_moss_carpet_slab")));

	// Slab sweet berry bush (for proper rendering on bottom slabs)
	public static final Block SWEET_BERRY_BUSH_SLAB = new SlabSweetBerryBushBlock(AbstractBlock.Settings.copy(Blocks.SWEET_BERRY_BUSH).ticksRandomly().registryKey(blockKey("sweet_berry_bush_slab")));

	// Slab azalea (for proper rendering on bottom slabs)
	public static final Block AZALEA_SLAB = new SlabAzaleaBlock(AbstractBlock.Settings.copy(Blocks.AZALEA).registryKey(blockKey("azalea_slab")));

	// Slab dripleaf (for proper rendering on bottom slabs)
	public static final Block SMALL_DRIPLEAF_SLAB = new SlabSmallDripleafBlock(AbstractBlock.Settings.copy(Blocks.SMALL_DRIPLEAF).registryKey(blockKey("small_dripleaf_slab")));
	public static final Block BIG_DRIPLEAF_SLAB = new SlabBigDripleafBlock(AbstractBlock.Settings.copy(Blocks.BIG_DRIPLEAF).registryKey(blockKey("big_dripleaf_slab")));
	public static final Block BIG_DRIPLEAF_STEM_SLAB = new SlabBigDripleafStemBlock(AbstractBlock.Settings.copy(Blocks.BIG_DRIPLEAF_STEM).registryKey(blockKey("big_dripleaf_stem_slab")));

	// Slab cave vines (for proper rendering from top slabs)
	public static final Block CAVE_VINES_SLAB = new SlabCaveVinesBlock(AbstractBlock.Settings.copy(Blocks.CAVE_VINES).ticksRandomly().registryKey(blockKey("cave_vines_slab")));
	public static final Block CAVE_VINES_PLANT_SLAB = new SlabCaveVinesPlantBlock(AbstractBlock.Settings.copy(Blocks.CAVE_VINES_PLANT).registryKey(blockKey("cave_vines_plant_slab")));

	// Slab pale hanging moss (for proper rendering from top slabs)
	public static final Block PALE_HANGING_MOSS_SLAB = new SlabPaleHangingMossBlock(AbstractBlock.Settings.copy(Blocks.PALE_HANGING_MOSS).registryKey(blockKey("pale_hanging_moss_slab")));

	// Slab snow layer (for proper rendering on bottom slabs)
	public static final Block SNOW_LAYER_SLAB = new SlabSnowLayerBlock(AbstractBlock.Settings.copy(Blocks.SNOW).ticksRandomly().registryKey(blockKey("snow_layer_slab")));

	// Slab saplings (for proper rendering on bottom slabs)
	public static final Block OAK_SAPLING_SLAB = new SlabSaplingBlock(
		AbstractBlock.Settings.copy(Blocks.OAK_SAPLING).ticksRandomly().registryKey(blockKey("oak_sapling_slab")),
		TreeConfiguredFeatures.OAK, TreeConfiguredFeatures.OAK_BEES_005, Items.OAK_SAPLING);
	public static final Block SPRUCE_SAPLING_SLAB = new SlabSaplingBlock(
		AbstractBlock.Settings.copy(Blocks.SPRUCE_SAPLING).ticksRandomly().registryKey(blockKey("spruce_sapling_slab")),
		TreeConfiguredFeatures.SPRUCE, TreeConfiguredFeatures.MEGA_SPRUCE, Items.SPRUCE_SAPLING);
	public static final Block BIRCH_SAPLING_SLAB = new SlabSaplingBlock(
		AbstractBlock.Settings.copy(Blocks.BIRCH_SAPLING).ticksRandomly().registryKey(blockKey("birch_sapling_slab")),
		TreeConfiguredFeatures.BIRCH, Items.BIRCH_SAPLING);
	public static final Block JUNGLE_SAPLING_SLAB = new SlabSaplingBlock(
		AbstractBlock.Settings.copy(Blocks.JUNGLE_SAPLING).ticksRandomly().registryKey(blockKey("jungle_sapling_slab")),
		TreeConfiguredFeatures.JUNGLE_TREE_NO_VINE, TreeConfiguredFeatures.MEGA_JUNGLE_TREE, Items.JUNGLE_SAPLING);
	public static final Block ACACIA_SAPLING_SLAB = new SlabSaplingBlock(
		AbstractBlock.Settings.copy(Blocks.ACACIA_SAPLING).ticksRandomly().registryKey(blockKey("acacia_sapling_slab")),
		TreeConfiguredFeatures.ACACIA, Items.ACACIA_SAPLING);
	public static final Block DARK_OAK_SAPLING_SLAB = new SlabSaplingBlock(
		AbstractBlock.Settings.copy(Blocks.DARK_OAK_SAPLING).ticksRandomly().registryKey(blockKey("dark_oak_sapling_slab")),
		TreeConfiguredFeatures.DARK_OAK, TreeConfiguredFeatures.DARK_OAK, Items.DARK_OAK_SAPLING);
	public static final Block CHERRY_SAPLING_SLAB = new SlabSaplingBlock(
		AbstractBlock.Settings.copy(Blocks.CHERRY_SAPLING).ticksRandomly().registryKey(blockKey("cherry_sapling_slab")),
		TreeConfiguredFeatures.CHERRY, Items.CHERRY_SAPLING);
	public static final Block MANGROVE_PROPAGULE_SLAB = new SlabSaplingBlock(
		AbstractBlock.Settings.copy(Blocks.MANGROVE_PROPAGULE).ticksRandomly().registryKey(blockKey("mangrove_propagule_slab")),
		TreeConfiguredFeatures.MANGROVE, Items.MANGROVE_PROPAGULE);
	public static final Block PALE_OAK_SAPLING_SLAB = new SlabSaplingBlock(
		AbstractBlock.Settings.copy(Blocks.PALE_OAK_SAPLING).ticksRandomly().registryKey(blockKey("pale_oak_sapling_slab")),
		TreeConfiguredFeatures.PALE_OAK, TreeConfiguredFeatures.PALE_OAK, Items.PALE_OAK_SAPLING);

	private static RegistryKey<Block> blockKey(String name) {
		return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, name));
	}

	private static RegistryKey<Item> itemKey(String name) {
		return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
	}

	private static void registerSlab(String name, Block block){
		Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), block);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), new BlockItem(block, new Item.Settings().registryKey(itemKey(name)).useBlockPrefixedTranslationKey()));
	}

	private static void registerBlock(String name, Block block){
		Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), block);
	}

	public static void register(){
		registerSlab("coarse_dirt_slab", COARSE_DIRT_SLAB);
		registerSlab("dirt_slab", DIRT_SLAB);
		registerSlab("farmland_slab", FARMLAND_SLAB);
		registerSlab("grass_path_slab", GRASS_PATH_SLAB);
		registerSlab("grass_slab", GRASS_SLAB);
		registerSlab("mud_slab", MUD_SLAB);
		registerSlab("mycelium_slab", MYCELIUM_SLAB);
		registerSlab("podzol_slab", PODZOL_SLAB);
		registerSlab("rooted_dirt_slab", ROOTED_DIRT_SLAB);

		// Register crop blocks (no items - seeds are the items)
		registerBlock("wheat_slab_crop", WHEAT_SLAB_CROP);
		registerBlock("carrot_slab_crop", CARROT_SLAB_CROP);
		registerBlock("potato_slab_crop", POTATO_SLAB_CROP);
		registerBlock("beetroot_slab_crop", BEETROOT_SLAB_CROP);
		registerBlock("torchflower_crop_slab", TORCHFLOWER_CROP_SLAB);
		registerBlock("pitcher_crop_slab", PITCHER_CROP_SLAB);
		registerBlock("pitcher_plant_slab", PITCHER_PLANT_SLAB);

		// Register stem blocks (no items - seeds are the items)
		registerBlock("melon_stem_slab", MELON_STEM_SLAB);
		registerBlock("pumpkin_stem_slab", PUMPKIN_STEM_SLAB);

		// Register attached stem blocks (no items - these are converted from stems)
		registerBlock("attached_melon_stem_slab", ATTACHED_MELON_STEM_SLAB);
		registerBlock("attached_pumpkin_stem_slab", ATTACHED_PUMPKIN_STEM_SLAB);

		// Register flower blocks (no items - flowers are picked up as vanilla items)
		registerBlock("dandelion_slab", DANDELION_SLAB);
		registerBlock("poppy_slab", POPPY_SLAB);
		registerBlock("blue_orchid_slab", BLUE_ORCHID_SLAB);
		registerBlock("allium_slab", ALLIUM_SLAB);
		registerBlock("azure_bluet_slab", AZURE_BLUET_SLAB);
		registerBlock("red_tulip_slab", RED_TULIP_SLAB);
		registerBlock("orange_tulip_slab", ORANGE_TULIP_SLAB);
		registerBlock("white_tulip_slab", WHITE_TULIP_SLAB);
		registerBlock("pink_tulip_slab", PINK_TULIP_SLAB);
		registerBlock("oxeye_daisy_slab", OXEYE_DAISY_SLAB);
		registerBlock("cornflower_slab", CORNFLOWER_SLAB);
		registerBlock("lily_of_the_valley_slab", LILY_OF_THE_VALLEY_SLAB);
		registerBlock("wither_rose_slab", WITHER_ROSE_SLAB);
		registerBlock("torchflower_slab", TORCHFLOWER_SLAB);
		registerBlock("open_eyeblossom_slab", OPEN_EYEBLOSSOM_SLAB);
		registerBlock("closed_eyeblossom_slab", CLOSED_EYEBLOSSOM_SLAB);

		// Register tall flower blocks (no items - flowers are picked up as vanilla items)
		registerBlock("sunflower_slab", SUNFLOWER_SLAB);
		registerBlock("lilac_slab", LILAC_SLAB);
		registerBlock("rose_bush_slab", ROSE_BUSH_SLAB);
		registerBlock("peony_slab", PEONY_SLAB);

		// Register mushroom blocks (no items - mushrooms are picked up as vanilla items)
		registerBlock("red_mushroom_slab", RED_MUSHROOM_SLAB);
		registerBlock("brown_mushroom_slab", BROWN_MUSHROOM_SLAB);

		// Register pink petals (no items - petals are picked up as vanilla items)
		registerBlock("pink_petals_slab", PINK_PETALS_SLAB);

		// Register wildflowers (no items - flowers are picked up as vanilla items)
		registerBlock("wildflowers_slab", WILDFLOWERS_SLAB);

		// Register short plants (no items - plants are picked up as vanilla items)
		registerBlock("short_grass_slab", SHORT_GRASS_SLAB);
		registerBlock("fern_slab", FERN_SLAB);
		registerBlock("dead_bush_slab", DEAD_BUSH_SLAB);
		registerBlock("short_dry_grass_slab", SHORT_DRY_GRASS_SLAB);
		registerBlock("bush_slab", BUSH_SLAB);

		// Register tall plants (no items - plants are picked up as vanilla items)
		registerBlock("tall_grass_slab", TALL_GRASS_SLAB);
		registerBlock("large_fern_slab", LARGE_FERN_SLAB);
		registerBlock("tall_dry_grass_slab", TALL_DRY_GRASS_SLAB);

		// Register leaf litter (no items - litter is picked up as vanilla items)
		registerBlock("leaf_litter_slab", LEAF_LITTER_SLAB);

		// Register hanging roots (no items - roots are picked up as vanilla items)
		registerBlock("hanging_roots_slab", HANGING_ROOTS_SLAB);

		// Register sugar cane slab (no items - sugar cane is picked up as vanilla item)
		registerBlock("sugar_cane_slab", SUGAR_CANE_SLAB);

		// Register bamboo slab blocks (no items - bamboo is picked up as vanilla item)
		registerBlock("bamboo_shoot_slab", BAMBOO_SHOOT_SLAB);
		registerBlock("bamboo_slab", BAMBOO_SLAB);

		// Register spore blossom slab (no items - spore blossom is picked up as vanilla item)
		registerBlock("spore_blossom_slab", SPORE_BLOSSOM_SLAB);

		// Register cactus flower slab (no items - cactus flower is picked up as vanilla item)
		registerBlock("cactus_flower_slab", CACTUS_FLOWER_SLAB);

		// Register firefly bush slab (no items - firefly bush is picked up as vanilla item)
		registerBlock("firefly_bush_slab", FIREFLY_BUSH_SLAB);

		// Register moss carpet slab (no items - moss carpet is picked up as vanilla item)
		registerBlock("moss_carpet_slab", MOSS_CARPET_SLAB);
		registerBlock("pale_moss_carpet_slab", PALE_MOSS_CARPET_SLAB);

		// Register sweet berry bush slab (no items - berries are picked up as vanilla item)
		registerBlock("sweet_berry_bush_slab", SWEET_BERRY_BUSH_SLAB);

		// Register azalea slab (no items - azalea is picked up as vanilla item)
		registerBlock("azalea_slab", AZALEA_SLAB);

		// Register dripleaf slabs (no items - dripleaf is picked up as vanilla items)
		registerBlock("small_dripleaf_slab", SMALL_DRIPLEAF_SLAB);
		registerBlock("big_dripleaf_slab", BIG_DRIPLEAF_SLAB);
		registerBlock("big_dripleaf_stem_slab", BIG_DRIPLEAF_STEM_SLAB);

		// Register cave vines slabs (no items - glow berries are picked up as vanilla item)
		registerBlock("cave_vines_slab", CAVE_VINES_SLAB);
		registerBlock("cave_vines_plant_slab", CAVE_VINES_PLANT_SLAB);

		// Register pale hanging moss slab (no items - pale hanging moss is picked up as vanilla item)
		registerBlock("pale_hanging_moss_slab", PALE_HANGING_MOSS_SLAB);

		// Register snow layer slab (no items - snow is picked up as vanilla item)
		registerBlock("snow_layer_slab", SNOW_LAYER_SLAB);

		// Register sapling slabs (no items - saplings are picked up as vanilla items)
		registerBlock("oak_sapling_slab", OAK_SAPLING_SLAB);
		registerBlock("spruce_sapling_slab", SPRUCE_SAPLING_SLAB);
		registerBlock("birch_sapling_slab", BIRCH_SAPLING_SLAB);
		registerBlock("jungle_sapling_slab", JUNGLE_SAPLING_SLAB);
		registerBlock("acacia_sapling_slab", ACACIA_SAPLING_SLAB);
		registerBlock("dark_oak_sapling_slab", DARK_OAK_SAPLING_SLAB);
		registerBlock("cherry_sapling_slab", CHERRY_SAPLING_SLAB);
		registerBlock("mangrove_propagule_slab", MANGROVE_PROPAGULE_SLAB);
		registerBlock("pale_oak_sapling_slab", PALE_OAK_SAPLING_SLAB);

		// Add items to the building blocks creative tab
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
			content.add(COARSE_DIRT_SLAB);
			content.add(DIRT_SLAB);
			content.add(FARMLAND_SLAB);
			content.add(GRASS_PATH_SLAB);
			content.add(GRASS_SLAB);
			content.add(MUD_SLAB);
			content.add(MYCELIUM_SLAB);
			content.add(PODZOL_SLAB);
			content.add(ROOTED_DIRT_SLAB);
		});
	}
}
