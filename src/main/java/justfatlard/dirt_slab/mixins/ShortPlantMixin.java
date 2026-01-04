package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShortPlantBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlabShortPlantBlock;

@Mixin(Block.class)
public class ShortPlantMixin {
	@Inject(at = @At("HEAD"), method = "getPlacementState", cancellable = true)
	private void redirectShortPlantPlacement(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Only apply to short plant blocks, dead bush, and dry grass variants
		if (!(self instanceof ShortPlantBlock) && self != Blocks.DEAD_BUSH && self != Blocks.SHORT_DRY_GRASS && self != Blocks.TALL_DRY_GRASS) {
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
			if (self == Blocks.SHORT_GRASS) {
				targetBlock = DirtSlabBlocks.SHORT_GRASS_SLAB;
			} else if (self == Blocks.FERN) {
				targetBlock = DirtSlabBlocks.FERN_SLAB;
			} else if (self == Blocks.DEAD_BUSH) {
				targetBlock = DirtSlabBlocks.DEAD_BUSH_SLAB;
			} else if (self == Blocks.SHORT_DRY_GRASS) {
				targetBlock = DirtSlabBlocks.SHORT_DRY_GRASS_SLAB;
			} else if (self == Blocks.TALL_DRY_GRASS) {
				targetBlock = DirtSlabBlocks.TALL_DRY_GRASS_SLAB;
			}

			if (targetBlock != null) {
				BlockState targetState = targetBlock.getDefaultState()
					.with(SlabShortPlantBlock.BOTTOM_OFFSET, isBottomSlab);
				info.setReturnValue(targetState);
			}
		}
	}
}
