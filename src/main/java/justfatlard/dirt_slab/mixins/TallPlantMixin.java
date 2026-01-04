package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlabTallPlantBlock;

@Mixin(Block.class)
public class TallPlantMixin {
	@Inject(at = @At("HEAD"), method = "getPlacementState", cancellable = true)
	private void redirectTallPlantPlacement(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Only apply to tall plant blocks (tall grass, large fern)
		if (!(self instanceof TallPlantBlock)) {
			return;
		}

		BlockPos clickedPos = ctx.getBlockPos();
		BlockPos belowPos = clickedPos.down();
		BlockState belowState = ctx.getWorld().getBlockState(belowPos);

		// Check if placing on a grass-type slab
		if (Main.isAnySlab(belowState.getBlock())) {
			boolean isBottomSlab = belowState.get(SlabBlock.TYPE) == SlabType.BOTTOM;

			Block targetBlock = null;

			// Determine which slab block to place based on the block type
			if (self == Blocks.TALL_GRASS) {
				targetBlock = DirtSlabBlocks.TALL_GRASS_SLAB;
			} else if (self == Blocks.LARGE_FERN) {
				targetBlock = DirtSlabBlocks.LARGE_FERN_SLAB;
			}

			if (targetBlock != null) {
				BlockState targetState = targetBlock.getDefaultState()
					.with(SlabTallPlantBlock.BOTTOM_OFFSET, isBottomSlab);
				info.setReturnValue(targetState);
			}
		}
	}
}
