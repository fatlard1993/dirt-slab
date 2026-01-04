package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HangingRootsBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabHangingRootsBlock;

@Mixin(HangingRootsBlock.class)
public class HangingRootsMixin {
	@Inject(at = @At("HEAD"), method = "canPlaceAt", cancellable = true)
	public void canPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
		Block self = (Block)(Object)this;

		// Skip if this is already our slab hanging roots
		if (self instanceof SlabHangingRootsBlock) {
			return;
		}

		// Check ceiling position
		BlockPos ceilingPos = pos.up();
		BlockState ceilingState = world.getBlockState(ceilingPos);
		Block ceilingBlock = ceilingState.getBlock();

		// Only allow hanging from rooted dirt slab (matches vanilla behavior)
		if (ceilingBlock == DirtSlabBlocks.ROOTED_DIRT_SLAB) {
			SlabType type = ceilingState.get(SlabBlock.TYPE);
			// Can only hang from TOP or DOUBLE slabs
			if (type == SlabType.TOP || type == SlabType.DOUBLE) {
				info.setReturnValue(true);
				return;
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "getPlacementState", cancellable = true)
	private void getHangingRootsPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Skip if this is already our slab hanging roots
		if (self instanceof SlabHangingRootsBlock) {
			return;
		}

		BlockPos clickedPos = ctx.getBlockPos();
		BlockPos ceilingPos = clickedPos.up();
		BlockState ceilingState = ctx.getWorld().getBlockState(ceilingPos);
		Block ceilingBlock = ceilingState.getBlock();

		// Only place slab variant below rooted dirt slab
		if (ceilingBlock == DirtSlabBlocks.ROOTED_DIRT_SLAB) {
			SlabType type = ceilingState.get(SlabBlock.TYPE);
			// Can only hang from TOP or DOUBLE slabs
			if (type == SlabType.TOP || type == SlabType.DOUBLE) {
				boolean isTopSlab = type == SlabType.TOP;

				BlockState slabState = DirtSlabBlocks.HANGING_ROOTS_SLAB.getDefaultState()
					.with(SlabHangingRootsBlock.TOP_OFFSET, isTopSlab);

				info.setReturnValue(slabState);
				return;
			}
		}
	}
}
