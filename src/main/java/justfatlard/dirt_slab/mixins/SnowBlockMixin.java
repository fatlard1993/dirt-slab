package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

@Mixin(SnowLayerBlock.class)
public class SnowBlockMixin {

	/**
	 * Allow snow to be placed on top of our dirt slabs (TOP or DOUBLE type)
	 */
	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	private void allowPlacementOnSlabs(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BlockState below = world.getBlockState(pos.below());

		// Check if the block below is one of our slabs
		if (SlabRegistry.isTerrainSlab(below.getBlock())) {
			if (below.hasProperty(SlabBlock.TYPE)) {
				SlabType type = below.getValue(SlabBlock.TYPE);
				// Allow snow on TOP or DOUBLE slabs (they have a full top surface)
				if (type == SlabType.TOP || type == SlabType.DOUBLE) {
					cir.setReturnValue(true);
				}
				// For BOTTOM slabs, we'll use our custom snow layer block instead
				// Return false here so vanilla snow doesn't place, our mixin will handle it
			}
		}
	}
}
