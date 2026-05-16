package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.SlabSporeBlossomBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

@Mixin(SporeBlossomBlock.class)
public class SporeBlossomMixin {
	// Allow placement check to pass for slabs
	// Actual placement state is handled by FlowerPlacementMixin on Block.class
	@Inject(at = @At("HEAD"), method = "canSurvive", cancellable = true)
	public void canPlaceAt(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
		Block self = (Block)(Object)this;

		// Skip if this is already our slab spore blossom
		if (self instanceof SlabSporeBlossomBlock) {
			return;
		}

		// Check ceiling position
		BlockPos ceilingPos = pos.above();
		BlockState ceilingState = world.getBlockState(ceilingPos);
		Block ceilingBlock = ceilingState.getBlock();

		// Allow hanging from any top or double slab
		if (ceilingBlock instanceof SlabBlock) {
			SlabType type = ceilingState.getValue(SlabBlock.TYPE);
			if (type == SlabType.TOP || type == SlabType.DOUBLE) {
				info.setReturnValue(true);
				return;
			}
		}
	}
}
