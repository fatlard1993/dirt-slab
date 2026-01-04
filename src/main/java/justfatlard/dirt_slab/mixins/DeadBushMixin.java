package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DryVegetationBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;

@Mixin(DryVegetationBlock.class)
public class DeadBushMixin {
	@Inject(at = @At("TAIL"), method = "canPlantOnTop", cancellable = true)
	public void canPlantOnTop(BlockState state, BlockView view, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
		Block block = state.getBlock();

		if(block == DirtSlabBlocks.DIRT_SLAB || block == DirtSlabBlocks.COARSE_DIRT_SLAB || block == DirtSlabBlocks.PODZOL_SLAB) info.setReturnValue(true);
	}
}
