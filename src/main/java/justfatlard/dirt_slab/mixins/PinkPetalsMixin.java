package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabPinkPetalsBlock;
import justfatlard.dirt_slab.SlabWildflowersBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

@Mixin(FlowerBedBlock.class)
public class PinkPetalsMixin {
	@Inject(at = @At("HEAD"), method = "getStateForPlacement", cancellable = true)
	private void getFlowerbedPlacementState(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Skip if this is already our slab flowerbed
		if (self instanceof SlabPinkPetalsBlock || self instanceof SlabWildflowersBlock) {
			return;
		}

		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos groundPos = clickedPos.below();
		BlockState groundState = ctx.getLevel().getBlockState(groundPos);

		// Check if placing on a dirt-type slab
		if (!SlabRegistry.isTerrainSlab(groundState.getBlock())) {
			return;
		}

		boolean isBottomSlab = false;
		if (groundState.getBlock() instanceof SlabBlock) {
			isBottomSlab = groundState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}

		// Get the facing direction from placement context
		Direction facing = ctx.getHorizontalDirection().getOpposite();

		// Determine which slab block to use based on the vanilla block being placed
		BlockState slabState;
		if (self == Blocks.WILDFLOWERS) {
			slabState = DirtSlabBlocks.WILDFLOWERS_SLAB.defaultBlockState()
				.setValue(SlabWildflowersBlock.FACING, facing)
				.setValue(SlabWildflowersBlock.FLOWER_AMOUNT, 1)
				.setValue(SlabWildflowersBlock.BOTTOM_OFFSET, isBottomSlab);
		} else {
			// Default to pink petals for Blocks.PINK_PETALS or any other flowerbed
			slabState = DirtSlabBlocks.PINK_PETALS_SLAB.defaultBlockState()
				.setValue(SlabPinkPetalsBlock.FACING, facing)
				.setValue(SlabPinkPetalsBlock.FLOWER_AMOUNT, 1)
				.setValue(SlabPinkPetalsBlock.BOTTOM_OFFSET, isBottomSlab);
		}

		info.setReturnValue(slabState);
	}
}
