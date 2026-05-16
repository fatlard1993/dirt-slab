package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@Mixin(SugarCaneBlock.class)
public class SugarCaneMixin {
	@Inject(at = @At("TAIL"), method = "canSurvive", cancellable = true)
	public void canPlaceAt(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		BlockPos groundPos = pos.below();
		BlockState groundState = world.getBlockState(groundPos);
		Block groundBlock = groundState.getBlock();

		// Allow placement on slab sugar cane (for stacking)
		if (groundBlock == DirtSlabBlocks.SUGAR_CANE_SLAB) {
			info.setReturnValue(true);
			return;
		}

		if (SlabRegistry.isSugarCanePlantable(groundBlock)) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				BlockState blockState = world.getBlockState(groundPos.relative(direction));
				FluidState fluidState = world.getFluidState(groundPos.relative(direction));

				if (fluidState.is(FluidTags.WATER) || blockState.getBlock() == Blocks.FROSTED_ICE) {
					info.setReturnValue(true);
					return;
				}
			}
		}
	}
}
