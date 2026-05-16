package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.OffsetableSlab;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

@Mixin(Block.class)
public class ShortPlantMixin {
	@Inject(at = @At("HEAD"), method = "getStateForPlacement", cancellable = true)
	private void redirectShortPlantPlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Only apply to short plant blocks, dead bush, and dry grass variants
		if (!(self instanceof TallGrassBlock) && self != Blocks.DEAD_BUSH && self != Blocks.SHORT_DRY_GRASS && self != Blocks.TALL_DRY_GRASS && self != Blocks.BUSH) {
			return;
		}

		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos belowPos = clickedPos.below();
		BlockState belowState = ctx.getLevel().getBlockState(belowPos);

		// Check if placing on a terrain slab
		if (SlabRegistry.isTerrainSlab(belowState.getBlock())) {
			Block targetBlock = SlabRegistry.getPlantSlab(self);

			if (targetBlock != null) {
				boolean isBottomSlab = belowState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
				BlockState targetState = targetBlock.defaultBlockState();
				if (targetState.hasProperty(OffsetableSlab.BOTTOM_OFFSET)) {
					targetState = targetState.setValue(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);
				}
				info.setReturnValue(targetState);
			}
		}
	}
}
