package justfatlard.dirt_slab;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.GrassColors;

import justfatlard.dirt_slab.SlabStemBlock;
import justfatlard.dirt_slab.worldgen.DirtSlabWorldGen;
import justfatlard.dirt_slab.worldgen.structure.SlabStructureProcessor;

public class Main implements ModInitializer, ClientModInitializer {
	public static final String MOD_ID = "dirt-slab-justfatlard";

	@Override
	public void onInitialize(){
		DirtSlabBlocks.register();
		SlabStructureProcessor.register();
		DirtSlabWorldGen.register();

		System.out.println("Loaded dirt-slab");
	}

	@Override
	public void onInitializeClient() {
		// Block color provider for grass tint (only for tintIndex 0 which is used by grass faces)
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			if(tintIndex != 0) return -1; // No tint for other indices
			if(view != null && pos != null) return BiomeColors.getGrassColor(view, pos);
			return GrassColors.getColor(0.5D, 1.0D);
		}, DirtSlabBlocks.GRASS_SLAB);

		// Stem color provider (stems get greener/yellower based on age)
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			int age = state.get(SlabStemBlock.AGE);
			int red = age * 32;
			int green = 255 - age * 8;
			int blue = age * 4;
			return red << 16 | green << 8 | blue;
		}, DirtSlabBlocks.MELON_STEM_SLAB, DirtSlabBlocks.PUMPKIN_STEM_SLAB);

		// Attached stem color provider (fully mature yellow/orange color)
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			// Same color as age 7 stem (fully mature)
			int red = 7 * 32;   // 224
			int green = 255 - 7 * 8; // 199
			int blue = 7 * 4;   // 28
			return red << 16 | green << 8 | blue;
		}, DirtSlabBlocks.ATTACHED_MELON_STEM_SLAB, DirtSlabBlocks.ATTACHED_PUMPKIN_STEM_SLAB);

		// Register cutout render layer for crops (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.WHEAT_SLAB_CROP, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.CARROT_SLAB_CROP, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.POTATO_SLAB_CROP, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.BEETROOT_SLAB_CROP, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.TORCHFLOWER_CROP_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.PITCHER_CROP_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.PITCHER_PLANT_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for stems (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.MELON_STEM_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.PUMPKIN_STEM_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.ATTACHED_MELON_STEM_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.ATTACHED_PUMPKIN_STEM_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for flowers (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.DANDELION_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.POPPY_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.BLUE_ORCHID_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.ALLIUM_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.AZURE_BLUET_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.RED_TULIP_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.ORANGE_TULIP_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.WHITE_TULIP_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.PINK_TULIP_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.OXEYE_DAISY_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.CORNFLOWER_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.LILY_OF_THE_VALLEY_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.WITHER_ROSE_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.TORCHFLOWER_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.OPEN_EYEBLOSSOM_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.CLOSED_EYEBLOSSOM_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for tall flowers (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.SUNFLOWER_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.LILAC_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.ROSE_BUSH_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.PEONY_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for mushrooms (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.RED_MUSHROOM_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.BROWN_MUSHROOM_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for pink petals (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.PINK_PETALS_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for wildflowers (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.WILDFLOWERS_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for short plants (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.SHORT_GRASS_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.FERN_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.DEAD_BUSH_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.SHORT_DRY_GRASS_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.BUSH_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for tall plants (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.TALL_GRASS_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.LARGE_FERN_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.TALL_DRY_GRASS_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for grass slab (overlay transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.GRASS_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for leaf litter (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.LEAF_LITTER_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for hanging roots (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.HANGING_ROOTS_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for sugar cane (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.SUGAR_CANE_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for bamboo (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.BAMBOO_SHOOT_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.BAMBOO_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for spore blossom (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.SPORE_BLOSSOM_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for cactus flower (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.CACTUS_FLOWER_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for firefly bush (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.FIREFLY_BUSH_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for moss carpet (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.MOSS_CARPET_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.PALE_MOSS_CARPET_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for sweet berry bush (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.SWEET_BERRY_BUSH_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for azalea (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.AZALEA_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for dripleaf (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.SMALL_DRIPLEAF_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.BIG_DRIPLEAF_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for cave vines (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.CAVE_VINES_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.CAVE_VINES_PLANT_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for pale hanging moss (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.PALE_HANGING_MOSS_SLAB, BlockRenderLayer.CUTOUT);

		// Register cutout render layer for saplings (transparency)
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.OAK_SAPLING_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.SPRUCE_SAPLING_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.BIRCH_SAPLING_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.JUNGLE_SAPLING_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.ACACIA_SAPLING_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.DARK_OAK_SAPLING_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.CHERRY_SAPLING_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.MANGROVE_PROPAGULE_SLAB, BlockRenderLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(DirtSlabBlocks.PALE_OAK_SAPLING_SLAB, BlockRenderLayer.CUTOUT);

		// Grass/fern color provider (biome grass color)
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			if(view != null && pos != null) return BiomeColors.getGrassColor(view, pos);
			return GrassColors.getColor(0.5D, 1.0D);
		}, DirtSlabBlocks.SHORT_GRASS_SLAB, DirtSlabBlocks.FERN_SLAB,
		   DirtSlabBlocks.TALL_GRASS_SLAB, DirtSlabBlocks.LARGE_FERN_SLAB,
		   DirtSlabBlocks.BUSH_SLAB);

		// Leaf litter color provider (fixed brown color matching vanilla)
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			return 0x8B7355; // Brown color for leaf litter (lighter to match vanilla)
		}, DirtSlabBlocks.LEAF_LITTER_SLAB);

		// Sugar cane color provider (biome grass color, same as vanilla)
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			if(view != null && pos != null) return BiomeColors.getGrassColor(view, pos);
			return GrassColors.getColor(0.5D, 1.0D);
		}, DirtSlabBlocks.SUGAR_CANE_SLAB);
	}

	public static boolean isAnySlab(Block block){
		if (block == null) return false;

		return block == DirtSlabBlocks.COARSE_DIRT_SLAB ||
			   block == DirtSlabBlocks.DIRT_SLAB ||
			   block == DirtSlabBlocks.FARMLAND_SLAB ||
			   block == DirtSlabBlocks.GRASS_PATH_SLAB ||
			   block == DirtSlabBlocks.GRASS_SLAB ||
			   block == DirtSlabBlocks.MUD_SLAB ||
			   block == DirtSlabBlocks.MYCELIUM_SLAB ||
			   block == DirtSlabBlocks.PODZOL_SLAB ||
			   block == DirtSlabBlocks.ROOTED_DIRT_SLAB;
	}

	public static boolean isDirtType(Block block){
		return block == DirtSlabBlocks.COARSE_DIRT_SLAB || block == DirtSlabBlocks.DIRT_SLAB || block == DirtSlabBlocks.FARMLAND_SLAB || block == DirtSlabBlocks.PODZOL_SLAB;
	}

	public static boolean isGrassType(Block block){
		return block == DirtSlabBlocks.GRASS_SLAB || block == DirtSlabBlocks.MYCELIUM_SLAB || isDirtType(block);
	}

	public static void dirtParticles(World world, BlockPos pos, int count){
		if(!world.isClient()) ((ServerWorld) world).spawnParticles(ParticleTypes.MYCELIUM, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, MathHelper.nextInt(world.random, 1, count), 0.25, 0.02, 0.25, 0.1);
	}

	public static void waterParticles(World world, BlockPos pos, int count){
		if(!world.isClient()) ((ServerWorld) world).spawnParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, MathHelper.nextInt(world.random, 1, count), 0.25, 0.02, 0.25, 0.1);
	}

	public static void happyParticles(World world, BlockPos pos, int count){
		if(!world.isClient()) ((ServerWorld) world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, MathHelper.nextInt(world.random, 1, count), 0.25, 0.02, 0.25, 0.1);
	}

	public static void sadParticles(World world, BlockPos pos, int count){
		if(!world.isClient()) ((ServerWorld) world).spawnParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, MathHelper.nextInt(world.random, 1, count), 0.25, 0.02, 0.25, 0.1);
	}

	public static void setToDirt(World world, BlockPos pos){
		BlockState state = world.getBlockState(pos);

		if(state.getBlock() instanceof SlabBlock) world.setBlockState(pos, DirtSlabBlocks.DIRT_SLAB.getDefaultState().with(SlabBlock.TYPE, state.get(SlabBlock.TYPE)).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED)));

		else world.setBlockState(pos, Blocks.DIRT.getDefaultState());

		dirtParticles(world, pos, 3);
	}

	public static void spreadableTick(BlockState spreader, ServerWorld world, BlockPos pos, Random random){
		if(SpreadableSlab.canSurvive(spreader, world, pos) && world.getLightLevel(pos.up()) >= 9){
			for(int x = 0; x < 4; ++x){
				BlockPos randBlockPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
				BlockState spreadee = world.getBlockState(randBlockPos);

				if(SpreadableSlab.canSpread(spreader, world, randBlockPos)){
					if(spreader.getBlock() == DirtSlabBlocks.GRASS_SLAB && spreadee.getBlock() == Blocks.DIRT){
						world.setBlockState(randBlockPos, Blocks.GRASS_BLOCK.getDefaultState());
					}

					else if(spreader.getBlock() == DirtSlabBlocks.MYCELIUM_SLAB && spreadee.getBlock() == Blocks.DIRT){
						world.setBlockState(randBlockPos, Blocks.MYCELIUM.getDefaultState());
					}

					else if((spreader.getBlock() == Blocks.GRASS_BLOCK || spreader.getBlock() == DirtSlabBlocks.GRASS_SLAB) && spreadee.getBlock() == DirtSlabBlocks.DIRT_SLAB){
						world.setBlockState(randBlockPos, DirtSlabBlocks.GRASS_SLAB.getDefaultState().with(SlabBlock.TYPE, spreadee.get(SlabBlock.TYPE)).with(SlabBlock.WATERLOGGED, spreadee.get(SlabBlock.WATERLOGGED)));
					}

					else if((spreader.getBlock() == Blocks.MYCELIUM || spreader.getBlock() == DirtSlabBlocks.MYCELIUM_SLAB) && spreadee.getBlock() == DirtSlabBlocks.DIRT_SLAB){
						world.setBlockState(randBlockPos, DirtSlabBlocks.MYCELIUM_SLAB.getDefaultState().with(SlabBlock.TYPE, spreadee.get(SlabBlock.TYPE)).with(SlabBlock.WATERLOGGED, spreadee.get(SlabBlock.WATERLOGGED)));
					}
				}
			}
		}
	}
}
