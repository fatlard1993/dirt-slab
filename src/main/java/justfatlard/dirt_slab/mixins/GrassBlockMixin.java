package justfatlard.dirt_slab.mixins;

import net.minecraft.util.math.random.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.GrassBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import justfatlard.dirt_slab.GrassSlab;

@Mixin(GrassBlock.class)
public class GrassBlockMixin {
	@Inject(at = @At("TAIL"), method = "grow", cancellable = true)
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state, CallbackInfo info){
		GrassSlab.growAll(world, random, pos, state);
	}
}
