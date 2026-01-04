package justfatlard.dirt_slab.mixins;

import net.minecraft.util.math.random.Random;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import justfatlard.dirt_slab.Main;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
	@Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 0))
	private void onHydrate(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo callbackInfo){
		Main.waterParticles(world, pos, 2);
	}

	@Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 1))
	private void onDehydrate(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo callbackInfo){
		Main.dirtParticles(world, pos, 1);
	}

	@Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FarmlandBlock;setToDirt(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
	private void onSetToDirt(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo callbackInfo){
		Main.dirtParticles(world, pos, 3);
	}
}
