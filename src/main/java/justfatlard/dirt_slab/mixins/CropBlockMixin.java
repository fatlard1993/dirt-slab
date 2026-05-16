package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.FarmlandSlab;
import justfatlard.dirt_slab.SlabEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CropBlock.class)
public class CropBlockMixin {
	@Inject(at = @At("TAIL"), method = "mayPlaceOn", cancellable = true)
	public void canPlantOnTop(BlockState state, BlockGetter view, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		Block block = state.getBlock();

		if(block == DirtSlabBlocks.FARMLAND_SLAB) info.setReturnValue(true);
	}

	@Inject(at = @At("RETURN"), method = "getGrowthSpeed", cancellable = true)
	private static void getAvailableMoisture(Block block, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Float> info){
		float moisture = info.getReturnValue();

		// Check for farmland slabs in the area and add their moisture contribution
		for(BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, -1, 1))){
			BlockState state = world.getBlockState(checkPos);
			if(state.getBlock() == DirtSlabBlocks.FARMLAND_SLAB){
				float slabMoisture = 1.0F;
				if(state.getValue(FarmlandSlab.MOISTURE) > 0){
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

	@Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
	private void onCropGrow(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo callbackInfo){
		int age = state.hasProperty(CropBlock.AGE) ? state.getValue(CropBlock.AGE) : 1;
		SlabEffects.happyParticles(world, pos, age);
	}
}
