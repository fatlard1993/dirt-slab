package justfatlard.dirt_slab.mixins;

import net.minecraft.util.math.random.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StemBlock;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;

@Mixin(StemBlock.class)
public class StemBlockMixin {
	@Inject(at = @At("TAIL"), method = "canPlantOnTop", cancellable = true)
	public void canPlantOnTop(BlockState state, BlockView view, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		Block block = state.getBlock();

		if(block == DirtSlabBlocks.FARMLAND_SLAB) info.setReturnValue(true);
	}

	// Redirect the isIn(BlockTags.DIRT) check in randomTick to also allow our grass-type slabs
	@Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
	private boolean allowSlabsForGourdPlacement(BlockState groundState, TagKey<Block> tag){
		// Original check
		if(groundState.isIn(tag)) return true;
		// Also allow our grass-type slabs (dirt, grass, coarse dirt, podzol, mycelium)
		return Main.isGrassType(groundState.getBlock());
	}

	@Inject(method = "grow", at = @At("HEAD"))
	private void grow(ServerWorld world, Random random, BlockPos pos, BlockState state, CallbackInfo callbackInfo){
		if(!world.isClient()) Main.happyParticles(world, pos, 5);
	}
}
