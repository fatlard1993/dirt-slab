package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import justfatlard.dirt_slab.OffsetableSlab;
import justfatlard.dirt_slab.SlabAzaleaBlock;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabSweetBerryBushBlock;
import justfatlard.dirt_slab.SlabTallFlowerBlock;
import justfatlard.dirt_slab.SlabTallPlantBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Intercepts setBlockState calls during worldgen (ChunkRegion) and swaps
 * vanilla plants for slab variants when placed on our terrain slabs.
 */
@Mixin(WorldGenRegion.class)
public class ChunkRegionMixin {

	@ModifyVariable(method = "setBlock", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private BlockState modifyPlantState(BlockState state, BlockPos pos) {
		Block block = state.getBlock();

		// Handle orphaned upper halves of tall plants/flowers placed above our slab variants
		if ((block instanceof DoublePlantBlock || block instanceof TallFlowerBlock) &&
			state.hasProperty(DoublePlantBlock.HALF) &&
			state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
			WorldGenRegion self = (WorldGenRegion)(Object)this;
			BlockState belowState = self.getBlockState(pos.below());
			Block belowBlock = belowState.getBlock();
			if (belowBlock instanceof SlabTallPlantBlock || belowBlock instanceof SlabTallFlowerBlock) {
				return Blocks.AIR.defaultBlockState();
			}
		}

		if (!SlabRegistry.isVegetation(block)) {
			return state;
		}

		WorldGenRegion self = (WorldGenRegion)(Object)this;
		BlockPos belowPos = pos.below();
		BlockState belowState = self.getBlockState(belowPos);

		if (!SlabRegistry.isTerrainSlab(belowState.getBlock())) {
			return state;
		}

		if (!(belowState.getBlock() instanceof SlabBlock)) {
			return state;
		}

		SlabType slabType = belowState.getValue(SlabBlock.TYPE);
		boolean isBottomSlab = slabType == SlabType.BOTTOM;

		BlockState slabVariant = getSlabVariantFor(block, state, isBottomSlab);

		return slabVariant != null ? slabVariant : state;
	}

	private BlockState getSlabVariantFor(Block block, BlockState state, boolean isBottomSlab) {
		// Tall plants/flowers: only convert lower half
		if ((block instanceof DoublePlantBlock || block instanceof TallFlowerBlock) &&
			state.hasProperty(DoublePlantBlock.HALF) &&
			state.getValue(DoublePlantBlock.HALF) != DoubleBlockHalf.LOWER) {
			return null;
		}

		Block slabBlock = SlabRegistry.getPlantSlab(block);
		if (slabBlock == null) return null;

		BlockState result = slabBlock.defaultBlockState();

		// Set BOTTOM_OFFSET via the shared interface — one check instead of 12
		if (slabBlock instanceof OffsetableSlab) {
			result = result.setValue(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);
		}

		// Transfer special state from vanilla blocks
		if (block == Blocks.SWEET_BERRY_BUSH) {
			int age = state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_3) ? state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_3) : 0;
			result = result.setValue(SlabSweetBerryBushBlock.AGE, age);
		}
		if (block == Blocks.FLOWERING_AZALEA) {
			result = result.setValue(SlabAzaleaBlock.FLOWERING, true);
		}

		return result;
	}
}
