package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.TallFlowerBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;

import justfatlard.dirt_slab.OffsetableSlab;
import justfatlard.dirt_slab.SlabAzaleaBlock;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabSweetBerryBushBlock;
import justfatlard.dirt_slab.SlabTallFlowerBlock;
import justfatlard.dirt_slab.SlabTallPlantBlock;

/**
 * Intercepts setBlockState calls during worldgen (ChunkRegion) and swaps
 * vanilla plants for slab variants when placed on our terrain slabs.
 */
@Mixin(ChunkRegion.class)
public class ChunkRegionMixin {

	@ModifyVariable(method = "setBlockState", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private BlockState modifyPlantState(BlockState state, BlockPos pos) {
		Block block = state.getBlock();

		// Handle orphaned upper halves of tall plants/flowers placed above our slab variants
		if ((block instanceof TallPlantBlock || block instanceof TallFlowerBlock) &&
			state.contains(TallPlantBlock.HALF) &&
			state.get(TallPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
			ChunkRegion self = (ChunkRegion)(Object)this;
			BlockState belowState = self.getBlockState(pos.down());
			Block belowBlock = belowState.getBlock();
			if (belowBlock instanceof SlabTallPlantBlock || belowBlock instanceof SlabTallFlowerBlock) {
				return Blocks.AIR.getDefaultState();
			}
		}

		if (!SlabRegistry.isVegetation(block)) {
			return state;
		}

		ChunkRegion self = (ChunkRegion)(Object)this;
		BlockPos belowPos = pos.down();
		BlockState belowState = self.getBlockState(belowPos);

		if (!SlabRegistry.isTerrainSlab(belowState.getBlock())) {
			return state;
		}

		if (!(belowState.getBlock() instanceof SlabBlock)) {
			return state;
		}

		SlabType slabType = belowState.get(SlabBlock.TYPE);
		boolean isBottomSlab = slabType == SlabType.BOTTOM;

		BlockState slabVariant = getSlabVariantFor(block, state, isBottomSlab);

		return slabVariant != null ? slabVariant : state;
	}

	private BlockState getSlabVariantFor(Block block, BlockState state, boolean isBottomSlab) {
		// Tall plants/flowers: only convert lower half
		if ((block instanceof TallPlantBlock || block instanceof TallFlowerBlock) &&
			state.contains(TallPlantBlock.HALF) &&
			state.get(TallPlantBlock.HALF) != DoubleBlockHalf.LOWER) {
			return null;
		}

		Block slabBlock = SlabRegistry.getPlantSlab(block);
		if (slabBlock == null) return null;

		BlockState result = slabBlock.getDefaultState();

		// Set BOTTOM_OFFSET via the shared interface — one check instead of 12
		if (slabBlock instanceof OffsetableSlab) {
			result = result.with(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);
		}

		// Transfer special state from vanilla blocks
		if (block == Blocks.SWEET_BERRY_BUSH) {
			int age = state.contains(net.minecraft.state.property.Properties.AGE_3) ? state.get(net.minecraft.state.property.Properties.AGE_3) : 0;
			result = result.with(SlabSweetBerryBushBlock.AGE, age);
		}
		if (block == Blocks.FLOWERING_AZALEA) {
			result = result.with(SlabAzaleaBlock.FLOWERING, true);
		}

		return result;
	}
}
