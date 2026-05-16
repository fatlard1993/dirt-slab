package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabHangingRootsBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HangingRootsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

@Mixin(HangingRootsBlock.class)
public class HangingRootsMixin {
	@Inject(at = @At("HEAD"), method = "canSurvive", cancellable = true)
	public void canPlaceAt(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
		Block self = (Block)(Object)this;

		// Skip if this is already our slab hanging roots
		if (self instanceof SlabHangingRootsBlock) {
			return;
		}

		// Check ceiling position
		BlockPos ceilingPos = pos.above();
		BlockState ceilingState = world.getBlockState(ceilingPos);
		Block ceilingBlock = ceilingState.getBlock();

		// Only allow hanging from rooted dirt slab (matches vanilla behavior)
		if (ceilingBlock == DirtSlabBlocks.ROOTED_DIRT_SLAB) {
			SlabType type = ceilingState.getValue(SlabBlock.TYPE);
			// Can only hang from TOP or DOUBLE slabs
			if (type == SlabType.TOP || type == SlabType.DOUBLE) {
				info.setReturnValue(true);
				return;
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "getStateForPlacement", cancellable = true)
	private void getHangingRootsPlacementState(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Skip if this is already our slab hanging roots
		if (self instanceof SlabHangingRootsBlock) {
			return;
		}

		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos ceilingPos = clickedPos.above();
		BlockState ceilingState = ctx.getLevel().getBlockState(ceilingPos);
		Block ceilingBlock = ceilingState.getBlock();

		// Only place slab variant below rooted dirt slab
		if (ceilingBlock == DirtSlabBlocks.ROOTED_DIRT_SLAB) {
			SlabType type = ceilingState.getValue(SlabBlock.TYPE);
			// Can only hang from TOP or DOUBLE slabs
			if (type == SlabType.TOP || type == SlabType.DOUBLE) {
				boolean isTopSlab = type == SlabType.TOP;

				BlockState slabState = DirtSlabBlocks.HANGING_ROOTS_SLAB.defaultBlockState()
					.setValue(SlabHangingRootsBlock.TOP_OFFSET, isTopSlab);

				info.setReturnValue(slabState);
				return;
			}
		}
	}
}
