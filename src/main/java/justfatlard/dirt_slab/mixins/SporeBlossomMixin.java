package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SporeBlossomBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import justfatlard.dirt_slab.SlabSporeBlossomBlock;

@Mixin(SporeBlossomBlock.class)
public class SporeBlossomMixin {
	// Allow placement check to pass for slabs
	// Actual placement state is handled by FlowerPlacementMixin on Block.class
	@Inject(at = @At("HEAD"), method = "canPlaceAt", cancellable = true)
	public void canPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
		Block self = (Block)(Object)this;

		// Skip if this is already our slab spore blossom
		if (self instanceof SlabSporeBlossomBlock) {
			return;
		}

		// Check ceiling position
		BlockPos ceilingPos = pos.up();
		BlockState ceilingState = world.getBlockState(ceilingPos);
		Block ceilingBlock = ceilingState.getBlock();

		// Allow hanging from any top or double slab
		if (ceilingBlock instanceof SlabBlock) {
			SlabType type = ceilingState.get(SlabBlock.TYPE);
			if (type == SlabType.TOP || type == SlabType.DOUBLE) {
				info.setReturnValue(true);
				return;
			}
		}
	}
}
