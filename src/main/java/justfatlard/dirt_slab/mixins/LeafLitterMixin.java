package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import justfatlard.dirt_slab.SlabLeafLitterBlock;

@Mixin(LeafLitterBlock.class)
public class LeafLitterMixin {
	@Inject(at = @At("HEAD"), method = "getStateForPlacement", cancellable = true)
	private void getLeafLitterPlacementState(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Skip if this is already our slab leaf litter
		if (self instanceof SlabLeafLitterBlock) {
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

		BlockState slabState = DirtSlabBlocks.LEAF_LITTER_SLAB.defaultBlockState()
			.setValue(SlabLeafLitterBlock.FACING, facing)
			.setValue(SlabLeafLitterBlock.SEGMENT_AMOUNT, 1)
			.setValue(SlabLeafLitterBlock.BOTTOM_OFFSET, isBottomSlab);

		info.setReturnValue(slabState);
	}
}
