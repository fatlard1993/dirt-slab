package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeafLitterBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlabLeafLitterBlock;

@Mixin(LeafLitterBlock.class)
public class LeafLitterMixin {
	@Inject(at = @At("HEAD"), method = "getPlacementState", cancellable = true)
	private void getLeafLitterPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Skip if this is already our slab leaf litter
		if (self instanceof SlabLeafLitterBlock) {
			return;
		}

		BlockPos clickedPos = ctx.getBlockPos();
		BlockPos groundPos = clickedPos.down();
		BlockState groundState = ctx.getWorld().getBlockState(groundPos);

		// Check if placing on a dirt-type slab
		if (!Main.isAnySlab(groundState.getBlock())) {
			return;
		}

		boolean isBottomSlab = false;
		if (groundState.getBlock() instanceof SlabBlock) {
			isBottomSlab = groundState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}

		// Get the facing direction from placement context
		Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();

		BlockState slabState = DirtSlabBlocks.LEAF_LITTER_SLAB.getDefaultState()
			.with(SlabLeafLitterBlock.FACING, facing)
			.with(SlabLeafLitterBlock.SEGMENT_AMOUNT, 1)
			.with(SlabLeafLitterBlock.BOTTOM_OFFSET, isBottomSlab);

		info.setReturnValue(slabState);
	}
}
