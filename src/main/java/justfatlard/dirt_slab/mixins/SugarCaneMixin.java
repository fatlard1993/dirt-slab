package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

import justfatlard.dirt_slab.DirtSlabBlocks;

@Mixin(SugarCaneBlock.class)
public class SugarCaneMixin {
	@Inject(at = @At("TAIL"), method = "canPlaceAt", cancellable = true)
	public void canPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		BlockPos groundPos = pos.down();
		BlockState groundState = world.getBlockState(groundPos);
		Block groundBlock = groundState.getBlock();

		// Allow placement on slab sugar cane (for stacking)
		if (groundBlock == DirtSlabBlocks.SUGAR_CANE_SLAB) {
			info.setReturnValue(true);
			return;
		}

		if (isDirtSlab(groundBlock)) {
			for (Direction direction : Direction.Type.HORIZONTAL) {
				BlockState blockState = world.getBlockState(groundPos.offset(direction));
				FluidState fluidState = world.getFluidState(groundPos.offset(direction));

				if (fluidState.isIn(FluidTags.WATER) || blockState.getBlock() == Blocks.FROSTED_ICE) {
					info.setReturnValue(true);
					return;
				}
			}
		}
	}

	// Note: SugarCaneBlock doesn't override getPlacementState, so placement handling
	// is done in SeedItemMixin which injects into Block.getPlacementState

	private boolean isDirtSlab(Block block) {
		return block == DirtSlabBlocks.GRASS_SLAB ||
			   block == DirtSlabBlocks.DIRT_SLAB ||
			   block == DirtSlabBlocks.COARSE_DIRT_SLAB ||
			   block == DirtSlabBlocks.PODZOL_SLAB;
	}
}
