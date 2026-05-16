package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabEffects;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(StemBlock.class)
public class StemBlockMixin {
	@Inject(at = @At("TAIL"), method = "mayPlaceOn", cancellable = true)
	public void canPlantOnTop(BlockState state, BlockGetter view, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		Block block = state.getBlock();

		if(block == DirtSlabBlocks.FARMLAND_SLAB) info.setReturnValue(true);
	}

	@WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"))
	private boolean allowSlabsForGourdPlacement(BlockState groundState, TagKey<Block> tag, Operation<Boolean> original){
		if(original.call(groundState, tag)) return true;
		return SlabRegistry.isGrassType(groundState.getBlock());
	}

	@Inject(method = "performBonemeal", at = @At("HEAD"))
	private void grow(ServerLevel world, RandomSource random, BlockPos pos, BlockState state, CallbackInfo callbackInfo){
		if(!world.isClientSide()) SlabEffects.happyParticles(world, pos, 5);
	}
}
