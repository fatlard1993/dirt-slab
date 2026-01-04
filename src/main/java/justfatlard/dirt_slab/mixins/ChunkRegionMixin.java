package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.MushroomPlantBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.ShortPlantBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.TallFlowerBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlabFlowerBlock;
import justfatlard.dirt_slab.SlabLeafLitterBlock;
import justfatlard.dirt_slab.SlabMushroomBlock;
import justfatlard.dirt_slab.SlabPinkPetalsBlock;
import justfatlard.dirt_slab.SlabShortPlantBlock;
import justfatlard.dirt_slab.SlabTallFlowerBlock;
import justfatlard.dirt_slab.SlabTallPlantBlock;
import justfatlard.dirt_slab.SlabWildflowersBlock;
import justfatlard.dirt_slab.SlabFireflyBushBlock;
import justfatlard.dirt_slab.SlabSweetBerryBushBlock;
import justfatlard.dirt_slab.SlabAzaleaBlock;
import justfatlard.dirt_slab.SlabSaplingBlock;

/**
 * This mixin intercepts setBlockState calls during worldgen (ChunkRegion)
 * and swaps vanilla plants for our slab variants when placed on our slabs.
 */
@Mixin(ChunkRegion.class)
public class ChunkRegionMixin {

	@ModifyVariable(method = "setBlockState", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private BlockState modifyPlantState(BlockState state, BlockPos pos) {
		Block block = state.getBlock();

		// Only process vanilla plants
		if (!isConvertiblePlant(block)) {
			return state;
		}

		// Get the block below
		ChunkRegion self = (ChunkRegion)(Object)this;
		BlockPos belowPos = pos.down();
		BlockState belowState = self.getBlockState(belowPos);

		// Check if the block below is one of our slabs
		if (!Main.isAnySlab(belowState.getBlock())) {
			return state;
		}

		if (!(belowState.getBlock() instanceof SlabBlock)) {
			return state;
		}

		SlabType slabType = belowState.get(SlabBlock.TYPE);
		boolean isBottomSlab = slabType == SlabType.BOTTOM;

		// Get the slab variant for this plant
		BlockState slabVariant = getSlabVariantFor(block, state, isBottomSlab);

		if (slabVariant != null) {
			return slabVariant;
		}

		// No slab variant available - prevent floating plant by returning air
		return Blocks.AIR.getDefaultState();
	}

	private boolean isConvertiblePlant(Block block) {
		return block instanceof ShortPlantBlock ||
			   block instanceof FlowerBlock ||
			   block instanceof TallPlantBlock ||
			   block instanceof TallFlowerBlock ||
			   block instanceof MushroomPlantBlock ||
			   block instanceof SaplingBlock ||
			   block == Blocks.DEAD_BUSH ||
			   block == Blocks.SHORT_DRY_GRASS ||
			   block == Blocks.TALL_DRY_GRASS ||
			   block == Blocks.FIREFLY_BUSH ||
			   block == Blocks.LEAF_LITTER ||
			   block == Blocks.PINK_PETALS ||
			   block == Blocks.WILDFLOWERS ||
			   block == Blocks.SWEET_BERRY_BUSH ||
			   block == Blocks.AZALEA ||
			   block == Blocks.FLOWERING_AZALEA ||
			   block == Blocks.BUSH ||
			   block == Blocks.MANGROVE_PROPAGULE;
	}

	private BlockState getSlabVariantFor(Block block, BlockState state, boolean isBottomSlab) {
		// Short plants
		if (block == Blocks.SHORT_GRASS) {
			return DirtSlabBlocks.SHORT_GRASS_SLAB.getDefaultState()
				.with(SlabShortPlantBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.FERN) {
			return DirtSlabBlocks.FERN_SLAB.getDefaultState()
				.with(SlabShortPlantBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.DEAD_BUSH) {
			return DirtSlabBlocks.DEAD_BUSH_SLAB.getDefaultState()
				.with(SlabShortPlantBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.SHORT_DRY_GRASS) {
			return DirtSlabBlocks.SHORT_DRY_GRASS_SLAB.getDefaultState()
				.with(SlabShortPlantBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.BUSH) {
			return DirtSlabBlocks.BUSH_SLAB.getDefaultState()
				.with(SlabShortPlantBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.TALL_DRY_GRASS) {
			return DirtSlabBlocks.TALL_DRY_GRASS_SLAB.getDefaultState()
				.with(SlabShortPlantBlock.BOTTOM_OFFSET, isBottomSlab);
		}

		// Flowers
		if (block == Blocks.DANDELION) {
			return DirtSlabBlocks.DANDELION_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.POPPY) {
			return DirtSlabBlocks.POPPY_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.BLUE_ORCHID) {
			return DirtSlabBlocks.BLUE_ORCHID_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.ALLIUM) {
			return DirtSlabBlocks.ALLIUM_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.AZURE_BLUET) {
			return DirtSlabBlocks.AZURE_BLUET_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.RED_TULIP) {
			return DirtSlabBlocks.RED_TULIP_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.ORANGE_TULIP) {
			return DirtSlabBlocks.ORANGE_TULIP_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.WHITE_TULIP) {
			return DirtSlabBlocks.WHITE_TULIP_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.PINK_TULIP) {
			return DirtSlabBlocks.PINK_TULIP_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.OXEYE_DAISY) {
			return DirtSlabBlocks.OXEYE_DAISY_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.CORNFLOWER) {
			return DirtSlabBlocks.CORNFLOWER_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.LILY_OF_THE_VALLEY) {
			return DirtSlabBlocks.LILY_OF_THE_VALLEY_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.WITHER_ROSE) {
			return DirtSlabBlocks.WITHER_ROSE_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.TORCHFLOWER) {
			return DirtSlabBlocks.TORCHFLOWER_SLAB.getDefaultState()
				.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
		}

		// Mushrooms
		if (block == Blocks.RED_MUSHROOM) {
			return DirtSlabBlocks.RED_MUSHROOM_SLAB.getDefaultState()
				.with(SlabMushroomBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.BROWN_MUSHROOM) {
			return DirtSlabBlocks.BROWN_MUSHROOM_SLAB.getDefaultState()
				.with(SlabMushroomBlock.BOTTOM_OFFSET, isBottomSlab);
		}

		// Leaf litter
		if (block == Blocks.LEAF_LITTER) {
			return DirtSlabBlocks.LEAF_LITTER_SLAB.getDefaultState()
				.with(SlabLeafLitterBlock.BOTTOM_OFFSET, isBottomSlab);
		}

		// Pink petals
		if (block == Blocks.PINK_PETALS) {
			return DirtSlabBlocks.PINK_PETALS_SLAB.getDefaultState()
				.with(SlabPinkPetalsBlock.BOTTOM_OFFSET, isBottomSlab);
		}

		// Wildflowers
		if (block == Blocks.WILDFLOWERS) {
			return DirtSlabBlocks.WILDFLOWERS_SLAB.getDefaultState()
				.with(SlabWildflowersBlock.BOTTOM_OFFSET, isBottomSlab);
		}

		// Firefly bush
		if (block == Blocks.FIREFLY_BUSH) {
			return DirtSlabBlocks.FIREFLY_BUSH_SLAB.getDefaultState()
				.with(SlabFireflyBushBlock.BOTTOM_OFFSET, isBottomSlab);
		}

		// Sweet berry bush
		if (block == Blocks.SWEET_BERRY_BUSH) {
			int age = state.contains(net.minecraft.state.property.Properties.AGE_3) ? state.get(net.minecraft.state.property.Properties.AGE_3) : 0;
			return DirtSlabBlocks.SWEET_BERRY_BUSH_SLAB.getDefaultState()
				.with(SlabSweetBerryBushBlock.BOTTOM_OFFSET, isBottomSlab)
				.with(SlabSweetBerryBushBlock.AGE, age);
		}

		// Azalea
		if (block == Blocks.AZALEA) {
			return DirtSlabBlocks.AZALEA_SLAB.getDefaultState()
				.with(SlabAzaleaBlock.BOTTOM_OFFSET, isBottomSlab)
				.with(SlabAzaleaBlock.FLOWERING, false);
		}
		if (block == Blocks.FLOWERING_AZALEA) {
			return DirtSlabBlocks.AZALEA_SLAB.getDefaultState()
				.with(SlabAzaleaBlock.BOTTOM_OFFSET, isBottomSlab)
				.with(SlabAzaleaBlock.FLOWERING, true);
		}

		// Saplings
		if (block == Blocks.OAK_SAPLING) {
			return DirtSlabBlocks.OAK_SAPLING_SLAB.getDefaultState()
				.with(SlabSaplingBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.SPRUCE_SAPLING) {
			return DirtSlabBlocks.SPRUCE_SAPLING_SLAB.getDefaultState()
				.with(SlabSaplingBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.BIRCH_SAPLING) {
			return DirtSlabBlocks.BIRCH_SAPLING_SLAB.getDefaultState()
				.with(SlabSaplingBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.JUNGLE_SAPLING) {
			return DirtSlabBlocks.JUNGLE_SAPLING_SLAB.getDefaultState()
				.with(SlabSaplingBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.ACACIA_SAPLING) {
			return DirtSlabBlocks.ACACIA_SAPLING_SLAB.getDefaultState()
				.with(SlabSaplingBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.DARK_OAK_SAPLING) {
			return DirtSlabBlocks.DARK_OAK_SAPLING_SLAB.getDefaultState()
				.with(SlabSaplingBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.CHERRY_SAPLING) {
			return DirtSlabBlocks.CHERRY_SAPLING_SLAB.getDefaultState()
				.with(SlabSaplingBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.MANGROVE_PROPAGULE) {
			return DirtSlabBlocks.MANGROVE_PROPAGULE_SLAB.getDefaultState()
				.with(SlabSaplingBlock.BOTTOM_OFFSET, isBottomSlab);
		}
		if (block == Blocks.PALE_OAK_SAPLING) {
			return DirtSlabBlocks.PALE_OAK_SAPLING_SLAB.getDefaultState()
				.with(SlabSaplingBlock.BOTTOM_OFFSET, isBottomSlab);
		}

		// Tall plants (only convert lower half)
		if (block == Blocks.TALL_GRASS) {
			if (state.contains(TallPlantBlock.HALF) && state.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
				return DirtSlabBlocks.TALL_GRASS_SLAB.getDefaultState()
					.with(SlabTallPlantBlock.BOTTOM_OFFSET, isBottomSlab);
			}
		}
		if (block == Blocks.LARGE_FERN) {
			if (state.contains(TallPlantBlock.HALF) && state.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {
				return DirtSlabBlocks.LARGE_FERN_SLAB.getDefaultState()
					.with(SlabTallPlantBlock.BOTTOM_OFFSET, isBottomSlab);
			}
		}

		// Tall flowers (only convert lower half)
		if (block == Blocks.SUNFLOWER) {
			if (state.contains(TallFlowerBlock.HALF) && state.get(TallFlowerBlock.HALF) == DoubleBlockHalf.LOWER) {
				return DirtSlabBlocks.SUNFLOWER_SLAB.getDefaultState()
					.with(SlabTallFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
			}
		}
		if (block == Blocks.LILAC) {
			if (state.contains(TallFlowerBlock.HALF) && state.get(TallFlowerBlock.HALF) == DoubleBlockHalf.LOWER) {
				return DirtSlabBlocks.LILAC_SLAB.getDefaultState()
					.with(SlabTallFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
			}
		}
		if (block == Blocks.ROSE_BUSH) {
			if (state.contains(TallFlowerBlock.HALF) && state.get(TallFlowerBlock.HALF) == DoubleBlockHalf.LOWER) {
				return DirtSlabBlocks.ROSE_BUSH_SLAB.getDefaultState()
					.with(SlabTallFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
			}
		}
		if (block == Blocks.PEONY) {
			if (state.contains(TallFlowerBlock.HALF) && state.get(TallFlowerBlock.HALF) == DoubleBlockHalf.LOWER) {
				return DirtSlabBlocks.PEONY_SLAB.getDefaultState()
					.with(SlabTallFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
			}
		}

		return null;
	}
}
