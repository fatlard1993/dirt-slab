package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.DryVegetationBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import justfatlard.dirt_slab.SlabRegistry;

@Mixin(DryVegetationBlock.class)
public class DeadBushMixin {
	@Inject(at = @At("TAIL"), method = "canPlantOnTop", cancellable = true)
	public void canPlantOnTop(BlockState state, BlockView view, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
		if(SlabRegistry.isPlantable(state.getBlock())) info.setReturnValue(true);
	}
}
