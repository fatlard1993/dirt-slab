package justfatlard.dirt_slab.mixins;

import net.minecraft.util.math.random.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import justfatlard.dirt_slab.Main;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(SpreadableBlock.class)
public class SpreadableBlockMixin {
	@Inject(at = @At("TAIL"), method = "randomTick", cancellable = true)
	public void randomTick(BlockState spreader, ServerWorld world, BlockPos pos, Random random, CallbackInfo info){
		Main.spreadableTick(spreader, world, pos, random);
	}
}
