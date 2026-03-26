package justfatlard.dirt_slab;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.world.biome.GrassColors;

public class DirtSlabClient implements ClientModInitializer {
	private static final Block[] CUTOUT_BLOCKS = {
		DirtSlabBlocks.WHEAT_SLAB_CROP, DirtSlabBlocks.CARROT_SLAB_CROP,
		DirtSlabBlocks.POTATO_SLAB_CROP, DirtSlabBlocks.BEETROOT_SLAB_CROP,
		DirtSlabBlocks.TORCHFLOWER_CROP_SLAB, DirtSlabBlocks.PITCHER_CROP_SLAB,
		DirtSlabBlocks.PITCHER_PLANT_SLAB,
		DirtSlabBlocks.MELON_STEM_SLAB, DirtSlabBlocks.PUMPKIN_STEM_SLAB,
		DirtSlabBlocks.ATTACHED_MELON_STEM_SLAB, DirtSlabBlocks.ATTACHED_PUMPKIN_STEM_SLAB,
		DirtSlabBlocks.DANDELION_SLAB, DirtSlabBlocks.POPPY_SLAB,
		DirtSlabBlocks.BLUE_ORCHID_SLAB, DirtSlabBlocks.ALLIUM_SLAB,
		DirtSlabBlocks.AZURE_BLUET_SLAB, DirtSlabBlocks.RED_TULIP_SLAB,
		DirtSlabBlocks.ORANGE_TULIP_SLAB, DirtSlabBlocks.WHITE_TULIP_SLAB,
		DirtSlabBlocks.PINK_TULIP_SLAB, DirtSlabBlocks.OXEYE_DAISY_SLAB,
		DirtSlabBlocks.CORNFLOWER_SLAB, DirtSlabBlocks.LILY_OF_THE_VALLEY_SLAB,
		DirtSlabBlocks.WITHER_ROSE_SLAB, DirtSlabBlocks.TORCHFLOWER_SLAB,
		DirtSlabBlocks.OPEN_EYEBLOSSOM_SLAB, DirtSlabBlocks.CLOSED_EYEBLOSSOM_SLAB,
		DirtSlabBlocks.SUNFLOWER_SLAB, DirtSlabBlocks.LILAC_SLAB,
		DirtSlabBlocks.ROSE_BUSH_SLAB, DirtSlabBlocks.PEONY_SLAB,
		DirtSlabBlocks.RED_MUSHROOM_SLAB, DirtSlabBlocks.BROWN_MUSHROOM_SLAB,
		DirtSlabBlocks.PINK_PETALS_SLAB, DirtSlabBlocks.WILDFLOWERS_SLAB,
		DirtSlabBlocks.SHORT_GRASS_SLAB, DirtSlabBlocks.FERN_SLAB,
		DirtSlabBlocks.DEAD_BUSH_SLAB, DirtSlabBlocks.SHORT_DRY_GRASS_SLAB,
		DirtSlabBlocks.BUSH_SLAB,
		DirtSlabBlocks.TALL_GRASS_SLAB, DirtSlabBlocks.LARGE_FERN_SLAB,
		DirtSlabBlocks.TALL_DRY_GRASS_SLAB,
		DirtSlabBlocks.GRASS_SLAB,
		DirtSlabBlocks.LEAF_LITTER_SLAB, DirtSlabBlocks.HANGING_ROOTS_SLAB,
		DirtSlabBlocks.SUGAR_CANE_SLAB,
		DirtSlabBlocks.BAMBOO_SHOOT_SLAB, DirtSlabBlocks.BAMBOO_SLAB,
		DirtSlabBlocks.SPORE_BLOSSOM_SLAB, DirtSlabBlocks.CACTUS_FLOWER_SLAB,
		DirtSlabBlocks.FIREFLY_BUSH_SLAB,
		DirtSlabBlocks.MOSS_CARPET_SLAB, DirtSlabBlocks.PALE_MOSS_CARPET_SLAB,
		DirtSlabBlocks.SWEET_BERRY_BUSH_SLAB, DirtSlabBlocks.AZALEA_SLAB,
		DirtSlabBlocks.SMALL_DRIPLEAF_SLAB, DirtSlabBlocks.BIG_DRIPLEAF_SLAB,
		DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB,
		DirtSlabBlocks.CAVE_VINES_SLAB, DirtSlabBlocks.CAVE_VINES_PLANT_SLAB,
		DirtSlabBlocks.PALE_HANGING_MOSS_SLAB,
		DirtSlabBlocks.OAK_SAPLING_SLAB, DirtSlabBlocks.SPRUCE_SAPLING_SLAB,
		DirtSlabBlocks.BIRCH_SAPLING_SLAB, DirtSlabBlocks.JUNGLE_SAPLING_SLAB,
		DirtSlabBlocks.ACACIA_SAPLING_SLAB, DirtSlabBlocks.DARK_OAK_SAPLING_SLAB,
		DirtSlabBlocks.CHERRY_SAPLING_SLAB, DirtSlabBlocks.MANGROVE_PROPAGULE_SLAB,
		DirtSlabBlocks.PALE_OAK_SAPLING_SLAB,
	};

	@Override
	public void onInitializeClient() {
		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			if(tintIndex != 0) return -1;
			if(view != null && pos != null) return BiomeColors.getGrassColor(view, pos);
			return GrassColors.getColor(0.5D, 1.0D);
		}, DirtSlabBlocks.GRASS_SLAB);

		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			int age = state.get(SlabStemBlock.AGE);
			int red = age * 32;
			int green = 255 - age * 8;
			int blue = age * 4;
			return red << 16 | green << 8 | blue;
		}, DirtSlabBlocks.MELON_STEM_SLAB, DirtSlabBlocks.PUMPKIN_STEM_SLAB);

		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			int red = 7 * 32;
			int green = 255 - 7 * 8;
			int blue = 7 * 4;
			return red << 16 | green << 8 | blue;
		}, DirtSlabBlocks.ATTACHED_MELON_STEM_SLAB, DirtSlabBlocks.ATTACHED_PUMPKIN_STEM_SLAB);

		for(Block block : CUTOUT_BLOCKS){
			BlockRenderLayerMap.putBlock(block, BlockRenderLayer.CUTOUT);
		}

		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			if(view != null && pos != null) return BiomeColors.getGrassColor(view, pos);
			return GrassColors.getColor(0.5D, 1.0D);
		}, DirtSlabBlocks.SHORT_GRASS_SLAB, DirtSlabBlocks.FERN_SLAB,
		   DirtSlabBlocks.TALL_GRASS_SLAB, DirtSlabBlocks.LARGE_FERN_SLAB,
		   DirtSlabBlocks.BUSH_SLAB);

		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			return 0x8B7355;
		}, DirtSlabBlocks.LEAF_LITTER_SLAB);

		ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
			if(view != null && pos != null) return BiomeColors.getGrassColor(view, pos);
			return GrassColors.getColor(0.5D, 1.0D);
		}, DirtSlabBlocks.SUGAR_CANE_SLAB);
	}
}
