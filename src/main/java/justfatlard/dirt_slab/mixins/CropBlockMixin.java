package justfatlard.dirt_slab.mixins;

import net.minecraft.util.math.random.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.FarmlandSlab;
import justfatlard.dirt_slab.Main;

@Mixin(CropBlock.class)
public class CropBlockMixin {
	@Inject(at = @At("TAIL"), method = "canPlantOnTop", cancellable = true)
	public void canPlantOnTop(BlockState state, BlockView view, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		Block block = state.getBlock();

		if(block == DirtSlabBlocks.FARMLAND_SLAB) info.setReturnValue(true);
	}

	@Inject(at = @At("RETURN"), method = "getAvailableMoisture", cancellable = true)
	private static void getAvailableMoisture(Block block, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> info){
		float moisture = info.getReturnValue();

		// Check for farmland slabs in the area and add their moisture contribution
		for(BlockPos checkPos : BlockPos.iterate(pos.add(-1, 0, -1), pos.add(1, -1, 1))){
			BlockState state = world.getBlockState(checkPos);
			if(state.getBlock() == DirtSlabBlocks.FARMLAND_SLAB){
				float slabMoisture = 1.0F;
				if(state.get(FarmlandSlab.MOISTURE) > 0){
					slabMoisture = 3.0F;
				}
				// Farmland directly below the crop gives full bonus
				if(checkPos.getX() == pos.getX() && checkPos.getZ() == pos.getZ()){
					moisture += slabMoisture;
				} else {
					// Adjacent farmland gives reduced bonus
					moisture += slabMoisture / 4.0F;
				}
			}
		}

		info.setReturnValue(moisture);
	}

	@Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
	private void onCropGrow(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo callbackInfo){
		CropBlock crop = (CropBlock) state.getBlock();
		Main.happyParticles(world, pos, crop.getAge(state));
	}
}
