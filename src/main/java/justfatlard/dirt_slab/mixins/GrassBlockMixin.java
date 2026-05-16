package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import justfatlard.dirt_slab.GrassSlab;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(GrassBlock.class)
public class GrassBlockMixin {
	@Inject(at = @At("TAIL"), method = "performBonemeal", cancellable = true)
	public void grow(ServerLevel world, RandomSource random, BlockPos pos, BlockState state, CallbackInfo info){
		GrassSlab.growAll(world, random, pos, state);
	}
}
