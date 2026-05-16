package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import justfatlard.dirt_slab.SpreadableSlab;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SpreadingSnowyBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(SpreadingSnowyBlock.class)
public class SpreadableBlockMixin {
	@Inject(at = @At("TAIL"), method = "randomTick", cancellable = true)
	public void randomTick(BlockState spreader, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo info){
		SpreadableSlab.spreadableTick(spreader, world, pos, random);
	}
}
