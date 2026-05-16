package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DryVegetationBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(DryVegetationBlock.class)
public class DeadBushMixin {
	@Inject(at = @At("TAIL"), method = "mayPlaceOn", cancellable = true)
	public void canPlantOnTop(BlockState state, BlockGetter view, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
		if(SlabRegistry.isPlantable(state.getBlock())) info.setReturnValue(true);
	}
}
