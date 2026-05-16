package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabTallPlantBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

@Mixin(Block.class)
public class TallPlantMixin {
	@Inject(at = @At("HEAD"), method = "getStateForPlacement", cancellable = true)
	private void redirectTallPlantPlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Only apply to tall plant blocks (tall grass, large fern)
		if (!(self instanceof DoublePlantBlock)) {
			return;
		}

		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos belowPos = clickedPos.below();
		BlockState belowState = ctx.getLevel().getBlockState(belowPos);

		// Check if placing on a grass-type slab
		if (SlabRegistry.isTerrainSlab(belowState.getBlock())) {
			boolean isBottomSlab = belowState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;

			Block targetBlock = null;

			// Determine which slab block to place based on the block type
			if (self == Blocks.TALL_GRASS) {
				targetBlock = DirtSlabBlocks.TALL_GRASS_SLAB;
			} else if (self == Blocks.LARGE_FERN) {
				targetBlock = DirtSlabBlocks.LARGE_FERN_SLAB;
			}

			if (targetBlock != null) {
				BlockState targetState = targetBlock.defaultBlockState()
					.setValue(SlabTallPlantBlock.BOTTOM_OFFSET, isBottomSlab);
				info.setReturnValue(targetState);
			}
		}
	}
}
