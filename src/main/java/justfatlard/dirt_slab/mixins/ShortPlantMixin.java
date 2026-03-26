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

import justfatlard.dirt_slab.OffsetableSlab;
import justfatlard.dirt_slab.SlabRegistry;

@Mixin(Block.class)
public class ShortPlantMixin {
	@Inject(at = @At("HEAD"), method = "getPlacementState", cancellable = true)
	private void redirectShortPlantPlacement(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Only apply to short plant blocks, dead bush, and dry grass variants
		if (!(self instanceof ShortPlantBlock) && self != Blocks.DEAD_BUSH && self != Blocks.SHORT_DRY_GRASS && self != Blocks.TALL_DRY_GRASS && self != Blocks.BUSH) {
			return;
		}

		BlockPos clickedPos = ctx.getBlockPos();
		BlockPos belowPos = clickedPos.down();
		BlockState belowState = ctx.getWorld().getBlockState(belowPos);

		// Check if placing on a terrain slab
		if (SlabRegistry.isTerrainSlab(belowState.getBlock())) {
			Block targetBlock = SlabRegistry.getPlantSlab(self);

			if (targetBlock != null) {
				boolean isBottomSlab = belowState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
				BlockState targetState = targetBlock.getDefaultState();
				if (targetState.contains(OffsetableSlab.BOTTOM_OFFSET)) {
					targetState = targetState.with(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);
				}
				info.setReturnValue(targetState);
			}
		}
	}
}
