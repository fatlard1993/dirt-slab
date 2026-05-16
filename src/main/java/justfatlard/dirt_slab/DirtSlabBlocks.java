package justfatlard.dirt_slab;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class DirtSlabBlocks {
	private static final String MOD_ID = DirtSlab.MOD_ID;

	public static final Block COARSE_DIRT_SLAB = new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COARSE_DIRT).setId(blockKey("coarse_dirt_slab")));
	public static final Block DIRT_SLAB = new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT).setId(blockKey("dirt_slab")));
	public static final Block FARMLAND_SLAB = new FarmlandSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND).setId(blockKey("farmland_slab")));
	public static final Block GRASS_PATH_SLAB = new SlicedTopSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT_PATH).setId(blockKey("grass_path_slab")));
	public static final Block GRASS_SLAB = new GrassSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).randomTicks().setId(blockKey("grass_slab")));
	public static final Block MUD_SLAB = new MudSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.MUD).setId(blockKey("mud_slab")));
	public static final Block MYCELIUM_SLAB = new SpreadableSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.MYCELIUM).setId(blockKey("mycelium_slab")), Blocks.MYCELIUM);
	public static final Block PODZOL_SLAB = new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PODZOL).setId(blockKey("podzol_slab")));
	public static final Block ROOTED_DIRT_SLAB = new RootedDirtSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.ROOTED_DIRT).setId(blockKey("rooted_dirt_slab")));

	// Crops
	public static final Block WHEAT_SLAB_CROP = new SlabCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).setId(blockKey("wheat_slab_crop")), Items.WHEAT_SEEDS);
	public static final Block CARROT_SLAB_CROP = new SlabCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARROTS).setId(blockKey("carrot_slab_crop")), Items.CARROT);
	public static final Block POTATO_SLAB_CROP = new SlabCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POTATOES).setId(blockKey("potato_slab_crop")), Items.POTATO);
	public static final Block BEETROOT_SLAB_CROP = new BeetrootSlabCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEETROOTS).setId(blockKey("beetroot_slab_crop")));
	public static final Block TORCHFLOWER_CROP_SLAB = new SlabTorchflowerCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TORCHFLOWER_CROP).setId(blockKey("torchflower_crop_slab")));
	public static final Block PITCHER_CROP_SLAB = new SlabPitcherCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PITCHER_CROP).setId(blockKey("pitcher_crop_slab")));
	public static final Block PITCHER_PLANT_SLAB = new SlabPitcherPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PITCHER_PLANT).setId(blockKey("pitcher_plant_slab")));

	// Stems
	public static final Block MELON_STEM_SLAB = new SlabStemBlock(true, Items.MELON_SEEDS, BlockBehaviour.Properties.ofFullCopy(Blocks.MELON_STEM).setId(blockKey("melon_stem_slab")));
	public static final Block PUMPKIN_STEM_SLAB = new SlabStemBlock(false, Items.PUMPKIN_SEEDS, BlockBehaviour.Properties.ofFullCopy(Blocks.PUMPKIN_STEM).setId(blockKey("pumpkin_stem_slab")));

	// Attached stems
	public static final Block ATTACHED_MELON_STEM_SLAB = new SlabAttachedStemBlock(true, Items.MELON_SEEDS, BlockBehaviour.Properties.ofFullCopy(Blocks.ATTACHED_MELON_STEM).setId(blockKey("attached_melon_stem_slab")));
	public static final Block ATTACHED_PUMPKIN_STEM_SLAB = new SlabAttachedStemBlock(false, Items.PUMPKIN_SEEDS, BlockBehaviour.Properties.ofFullCopy(Blocks.ATTACHED_PUMPKIN_STEM).setId(blockKey("attached_pumpkin_stem_slab")));

	// Flowers
	public static final Block DANDELION_SLAB = new SlabFlowerBlock(MobEffects.SATURATION, 0.35F, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey("dandelion_slab")));
	public static final Block POPPY_SLAB = new SlabFlowerBlock(MobEffects.NIGHT_VISION, 5.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY).setId(blockKey("poppy_slab")));
	public static final Block BLUE_ORCHID_SLAB = new SlabFlowerBlock(MobEffects.SATURATION, 0.35F, BlockBehaviour.Properties.ofFullCopy(Blocks.BLUE_ORCHID).setId(blockKey("blue_orchid_slab")));
	public static final Block ALLIUM_SLAB = new SlabFlowerBlock(MobEffects.FIRE_RESISTANCE, 4.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.ALLIUM).setId(blockKey("allium_slab")));
	public static final Block AZURE_BLUET_SLAB = new SlabFlowerBlock(MobEffects.BLINDNESS, 8.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.AZURE_BLUET).setId(blockKey("azure_bluet_slab")));
	public static final Block RED_TULIP_SLAB = new SlabFlowerBlock(MobEffects.WEAKNESS, 9.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.RED_TULIP).setId(blockKey("red_tulip_slab")));
	public static final Block ORANGE_TULIP_SLAB = new SlabFlowerBlock(MobEffects.WEAKNESS, 9.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.ORANGE_TULIP).setId(blockKey("orange_tulip_slab")));
	public static final Block WHITE_TULIP_SLAB = new SlabFlowerBlock(MobEffects.WEAKNESS, 9.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_TULIP).setId(blockKey("white_tulip_slab")));
	public static final Block PINK_TULIP_SLAB = new SlabFlowerBlock(MobEffects.WEAKNESS, 9.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_TULIP).setId(blockKey("pink_tulip_slab")));
	public static final Block OXEYE_DAISY_SLAB = new SlabFlowerBlock(MobEffects.REGENERATION, 8.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.OXEYE_DAISY).setId(blockKey("oxeye_daisy_slab")));
	public static final Block CORNFLOWER_SLAB = new SlabFlowerBlock(MobEffects.JUMP_BOOST, 6.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.CORNFLOWER).setId(blockKey("cornflower_slab")));
	public static final Block LILY_OF_THE_VALLEY_SLAB = new SlabFlowerBlock(MobEffects.POISON, 12.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.LILY_OF_THE_VALLEY).setId(blockKey("lily_of_the_valley_slab")));
	public static final Block WITHER_ROSE_SLAB = new SlabFlowerBlock(MobEffects.WITHER, 8.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.WITHER_ROSE).setId(blockKey("wither_rose_slab")));
	public static final Block TORCHFLOWER_SLAB = new SlabFlowerBlock(MobEffects.NIGHT_VISION, 5.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.TORCHFLOWER).setId(blockKey("torchflower_slab")));
	public static final Block OPEN_EYEBLOSSOM_SLAB = new SlabFlowerBlock(MobEffects.NAUSEA, 9.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.OPEN_EYEBLOSSOM).setId(blockKey("open_eyeblossom_slab")));
	public static final Block CLOSED_EYEBLOSSOM_SLAB = new SlabFlowerBlock(MobEffects.BLINDNESS, 11.0F, BlockBehaviour.Properties.ofFullCopy(Blocks.CLOSED_EYEBLOSSOM).setId(blockKey("closed_eyeblossom_slab")));

	// Tall flowers
	public static final Block SUNFLOWER_SLAB = new SlabTallFlowerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SUNFLOWER).setId(blockKey("sunflower_slab")));
	public static final Block LILAC_SLAB = new SlabTallFlowerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LILAC).setId(blockKey("lilac_slab")));
	public static final Block ROSE_BUSH_SLAB = new SlabTallFlowerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ROSE_BUSH).setId(blockKey("rose_bush_slab")));
	public static final Block PEONY_SLAB = new SlabTallFlowerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PEONY).setId(blockKey("peony_slab")));

	// Mushrooms
	public static final Block RED_MUSHROOM_SLAB = new SlabMushroomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RED_MUSHROOM).setId(blockKey("red_mushroom_slab")));
	public static final Block BROWN_MUSHROOM_SLAB = new SlabMushroomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM).setId(blockKey("brown_mushroom_slab")));

	// Pink petals
	public static final Block PINK_PETALS_SLAB = new SlabPinkPetalsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_PETALS).setId(blockKey("pink_petals_slab")));

	// Wildflowers
	public static final Block WILDFLOWERS_SLAB = new SlabWildflowersBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WILDFLOWERS).setId(blockKey("wildflowers_slab")));

	// Short plants
	public static final Block SHORT_GRASS_SLAB = new SlabShortPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).setId(blockKey("short_grass_slab")));
	public static final Block FERN_SLAB = new SlabShortPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FERN).setId(blockKey("fern_slab")));
	public static final Block DEAD_BUSH_SLAB = new SlabShortPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEAD_BUSH).setId(blockKey("dead_bush_slab")));
	public static final Block SHORT_DRY_GRASS_SLAB = new SlabShortPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_DRY_GRASS).setId(blockKey("short_dry_grass_slab")));
	public static final Block BUSH_SLAB = new SlabShortPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BUSH).setId(blockKey("bush_slab")));

	// Tall plants
	public static final Block TALL_GRASS_SLAB = new SlabTallPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS).setId(blockKey("tall_grass_slab")));
	public static final Block LARGE_FERN_SLAB = new SlabTallPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LARGE_FERN).setId(blockKey("large_fern_slab")));
	// Tall dry grass is a single-block plant (not a double plant like tall grass)
	public static final Block TALL_DRY_GRASS_SLAB = new SlabShortPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_DRY_GRASS).setId(blockKey("tall_dry_grass_slab")));

	// Leaf litter
	public static final Block LEAF_LITTER_SLAB = new SlabLeafLitterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LEAF_LITTER).setId(blockKey("leaf_litter_slab")));

	// Hanging roots
	public static final Block HANGING_ROOTS_SLAB = new SlabHangingRootsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HANGING_ROOTS).setId(blockKey("hanging_roots_slab")));

	// Sugar cane
	public static final Block SUGAR_CANE_SLAB = new SlabSugarCaneBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SUGAR_CANE).randomTicks().setId(blockKey("sugar_cane_slab")));

	// Bamboo
	public static final Block BAMBOO_SHOOT_SLAB = new SlabBambooShootBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_SAPLING).randomTicks().setId(blockKey("bamboo_shoot_slab")));
	public static final Block BAMBOO_SLAB = new SlabBambooBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO).randomTicks().setId(blockKey("bamboo_slab")));

	// Spore blossom
	public static final Block SPORE_BLOSSOM_SLAB = new SlabSporeBlossomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPORE_BLOSSOM).setId(blockKey("spore_blossom_slab")));

	// Cactus flower
	public static final Block CACTUS_FLOWER_SLAB = new SlabCactusFlowerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CACTUS_FLOWER).setId(blockKey("cactus_flower_slab")));

	// Firefly bush
	public static final Block FIREFLY_BUSH_SLAB = new SlabFireflyBushBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FIREFLY_BUSH).setId(blockKey("firefly_bush_slab")));

	// Moss carpet
	public static final Block MOSS_CARPET_SLAB = new SlabMossCarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_CARPET).setId(blockKey("moss_carpet_slab")));
	public static final Block PALE_MOSS_CARPET_SLAB = new SlabMossCarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PALE_MOSS_CARPET).setId(blockKey("pale_moss_carpet_slab")));

	// Sweet berry bush
	public static final Block SWEET_BERRY_BUSH_SLAB = new SlabSweetBerryBushBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).randomTicks().setId(blockKey("sweet_berry_bush_slab")));

	// Azalea
	public static final Block AZALEA_SLAB = new SlabAzaleaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AZALEA).setId(blockKey("azalea_slab")));

	// Dripleaf
	public static final Block SMALL_DRIPLEAF_SLAB = new SlabSmallDripleafBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SMALL_DRIPLEAF).setId(blockKey("small_dripleaf_slab")));
	public static final Block BIG_DRIPLEAF_SLAB = new SlabBigDripleafBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BIG_DRIPLEAF).setId(blockKey("big_dripleaf_slab")));
	public static final Block BIG_DRIPLEAF_STEM_SLAB = new SlabBigDripleafStemBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BIG_DRIPLEAF_STEM).setId(blockKey("big_dripleaf_stem_slab")));

	// Cave vines
	public static final Block CAVE_VINES_SLAB = new SlabCaveVinesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAVE_VINES).randomTicks().setId(blockKey("cave_vines_slab")));
	public static final Block CAVE_VINES_PLANT_SLAB = new SlabCaveVinesPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAVE_VINES_PLANT).setId(blockKey("cave_vines_plant_slab")));

	// Pale hanging moss
	public static final Block PALE_HANGING_MOSS_SLAB = new SlabPaleHangingMossBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PALE_HANGING_MOSS).setId(blockKey("pale_hanging_moss_slab")));

	// Snow layer
	public static final Block SNOW_LAYER_SLAB = new SlabSnowLayerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SNOW).randomTicks().setId(blockKey("snow_layer_slab")));

	// Saplings
	public static final Block OAK_SAPLING_SLAB = new SlabSaplingBlock(
		BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).randomTicks().setId(blockKey("oak_sapling_slab")),
		TreeFeatures.OAK, TreeFeatures.OAK_BEES_005, Items.OAK_SAPLING);
	public static final Block SPRUCE_SAPLING_SLAB = new SlabSaplingBlock(
		BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING).randomTicks().setId(blockKey("spruce_sapling_slab")),
		TreeFeatures.SPRUCE, TreeFeatures.MEGA_SPRUCE, Items.SPRUCE_SAPLING);
	public static final Block BIRCH_SAPLING_SLAB = new SlabSaplingBlock(
		BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_SAPLING).randomTicks().setId(blockKey("birch_sapling_slab")),
		TreeFeatures.BIRCH, Items.BIRCH_SAPLING);
	public static final Block JUNGLE_SAPLING_SLAB = new SlabSaplingBlock(
		BlockBehaviour.Properties.ofFullCopy(Blocks.JUNGLE_SAPLING).randomTicks().setId(blockKey("jungle_sapling_slab")),
		TreeFeatures.JUNGLE_TREE_NO_VINE, TreeFeatures.MEGA_JUNGLE_TREE, Items.JUNGLE_SAPLING);
	public static final Block ACACIA_SAPLING_SLAB = new SlabSaplingBlock(
		BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_SAPLING).randomTicks().setId(blockKey("acacia_sapling_slab")),
		TreeFeatures.ACACIA, Items.ACACIA_SAPLING);
	public static final Block DARK_OAK_SAPLING_SLAB = new SlabSaplingBlock(
		BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_SAPLING).randomTicks().setId(blockKey("dark_oak_sapling_slab")),
		TreeFeatures.DARK_OAK, TreeFeatures.DARK_OAK, Items.DARK_OAK_SAPLING);
	public static final Block CHERRY_SAPLING_SLAB = new SlabSaplingBlock(
		BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_SAPLING).randomTicks().setId(blockKey("cherry_sapling_slab")),
		TreeFeatures.CHERRY, Items.CHERRY_SAPLING);
	public static final Block MANGROVE_PROPAGULE_SLAB = new SlabSaplingBlock(
		BlockBehaviour.Properties.ofFullCopy(Blocks.MANGROVE_PROPAGULE).randomTicks().setId(blockKey("mangrove_propagule_slab")),
		TreeFeatures.MANGROVE, Items.MANGROVE_PROPAGULE);
	public static final Block PALE_OAK_SAPLING_SLAB = new SlabSaplingBlock(
		BlockBehaviour.Properties.ofFullCopy(Blocks.PALE_OAK_SAPLING).randomTicks().setId(blockKey("pale_oak_sapling_slab")),
		TreeFeatures.PALE_OAK, TreeFeatures.PALE_OAK, Items.PALE_OAK_SAPLING);

	private static ResourceKey<Block> blockKey(String name) {
		return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
	}

	private static ResourceKey<Item> itemKey(String name) {
		return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
	}

	private static void registerSlab(String name, Block block){
		Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name), block);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name), new BlockItem(block, new Item.Properties().setId(itemKey(name)).useBlockDescriptionPrefix()));
	}

	private static void registerBlock(String name, Block block){
		Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name), block);
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

		// Plant/decoration blocks have no items — they drop their vanilla equivalents
		registerBlock("wheat_slab_crop", WHEAT_SLAB_CROP);
		registerBlock("carrot_slab_crop", CARROT_SLAB_CROP);
		registerBlock("potato_slab_crop", POTATO_SLAB_CROP);
		registerBlock("beetroot_slab_crop", BEETROOT_SLAB_CROP);
		registerBlock("torchflower_crop_slab", TORCHFLOWER_CROP_SLAB);
		registerBlock("pitcher_crop_slab", PITCHER_CROP_SLAB);
		registerBlock("pitcher_plant_slab", PITCHER_PLANT_SLAB);
		registerBlock("melon_stem_slab", MELON_STEM_SLAB);
		registerBlock("pumpkin_stem_slab", PUMPKIN_STEM_SLAB);
		registerBlock("attached_melon_stem_slab", ATTACHED_MELON_STEM_SLAB);
		registerBlock("attached_pumpkin_stem_slab", ATTACHED_PUMPKIN_STEM_SLAB);
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
		registerBlock("sunflower_slab", SUNFLOWER_SLAB);
		registerBlock("lilac_slab", LILAC_SLAB);
		registerBlock("rose_bush_slab", ROSE_BUSH_SLAB);
		registerBlock("peony_slab", PEONY_SLAB);
		registerBlock("red_mushroom_slab", RED_MUSHROOM_SLAB);
		registerBlock("brown_mushroom_slab", BROWN_MUSHROOM_SLAB);
		registerBlock("pink_petals_slab", PINK_PETALS_SLAB);
		registerBlock("wildflowers_slab", WILDFLOWERS_SLAB);
		registerBlock("short_grass_slab", SHORT_GRASS_SLAB);
		registerBlock("fern_slab", FERN_SLAB);
		registerBlock("dead_bush_slab", DEAD_BUSH_SLAB);
		registerBlock("short_dry_grass_slab", SHORT_DRY_GRASS_SLAB);
		registerBlock("bush_slab", BUSH_SLAB);
		registerBlock("tall_grass_slab", TALL_GRASS_SLAB);
		registerBlock("large_fern_slab", LARGE_FERN_SLAB);
		registerBlock("tall_dry_grass_slab", TALL_DRY_GRASS_SLAB);
		registerBlock("leaf_litter_slab", LEAF_LITTER_SLAB);
		registerBlock("hanging_roots_slab", HANGING_ROOTS_SLAB);
		registerBlock("sugar_cane_slab", SUGAR_CANE_SLAB);
		registerBlock("bamboo_shoot_slab", BAMBOO_SHOOT_SLAB);
		registerBlock("bamboo_slab", BAMBOO_SLAB);
		registerBlock("spore_blossom_slab", SPORE_BLOSSOM_SLAB);
		registerBlock("cactus_flower_slab", CACTUS_FLOWER_SLAB);
		registerBlock("firefly_bush_slab", FIREFLY_BUSH_SLAB);
		registerBlock("moss_carpet_slab", MOSS_CARPET_SLAB);
		registerBlock("pale_moss_carpet_slab", PALE_MOSS_CARPET_SLAB);
		registerBlock("sweet_berry_bush_slab", SWEET_BERRY_BUSH_SLAB);
		registerBlock("azalea_slab", AZALEA_SLAB);
		registerBlock("small_dripleaf_slab", SMALL_DRIPLEAF_SLAB);
		registerBlock("big_dripleaf_slab", BIG_DRIPLEAF_SLAB);
		registerBlock("big_dripleaf_stem_slab", BIG_DRIPLEAF_STEM_SLAB);
		registerBlock("cave_vines_slab", CAVE_VINES_SLAB);
		registerBlock("cave_vines_plant_slab", CAVE_VINES_PLANT_SLAB);
		registerBlock("pale_hanging_moss_slab", PALE_HANGING_MOSS_SLAB);
		registerBlock("snow_layer_slab", SNOW_LAYER_SLAB);
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
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(content -> {
			content.accept(COARSE_DIRT_SLAB);
			content.accept(DIRT_SLAB);
			content.accept(FARMLAND_SLAB);
			content.accept(GRASS_PATH_SLAB);
			content.accept(GRASS_SLAB);
			content.accept(MUD_SLAB);
			content.accept(MYCELIUM_SLAB);
			content.accept(PODZOL_SLAB);
			content.accept(ROOTED_DIRT_SLAB);
		});
	}
}
