package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(MushroomBlock.class)
public class MushroomPlantMixin {
	@Inject(at = @At("HEAD"), method = "canSurvive", cancellable = true)
	public void canPlaceAt(BlockState state, LevelReader view, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		BlockState groundState = view.getBlockState(pos.below());
		Block groundBlock = groundState.getBlock();

		// Mushrooms can always be placed on mycelium and podzol slabs
		if(groundBlock == DirtSlabBlocks.MYCELIUM_SLAB || groundBlock == DirtSlabBlocks.PODZOL_SLAB) {
			info.setReturnValue(true);
			return;
		}

		// Mushrooms can be placed on other dirt-type slabs in low light (< 13)
		if(SlabRegistry.isTerrainSlab(groundBlock) && view.getRawBrightness(pos, 0) < 13) {
			info.setReturnValue(true);
		}
	}
}
