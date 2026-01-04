package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import justfatlard.dirt_slab.Main;

@Mixin(SnowBlock.class)
public class SnowBlockMixin {

	/**
	 * Allow snow to be placed on top of our dirt slabs (TOP or DOUBLE type)
	 */
	@Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
	private void allowPlacementOnSlabs(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BlockState below = world.getBlockState(pos.down());

		// Check if the block below is one of our slabs
		if (Main.isAnySlab(below.getBlock())) {
			if (below.contains(SlabBlock.TYPE)) {
				SlabType type = below.get(SlabBlock.TYPE);
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
