package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import justfatlard.dirt_slab.SlabEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
	@Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 0))
	private void onHydrate(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo callbackInfo){
		SlabEffects.waterParticles(world, pos, 2);
	}

	@Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", ordinal = 1))
	private void onDehydrate(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo callbackInfo){
		SlabEffects.dirtParticles(world, pos, 1);
	}

	@Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FarmlandBlock;turnToDirt(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
	private void onSetToDirt(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo callbackInfo){
		SlabEffects.dirtParticles(world, pos, 3);
	}
}
