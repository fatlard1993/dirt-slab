package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import justfatlard.dirt_slab.Main;

@Mixin(BambooShootBlock.class)
public class BambooSaplingMixin {
	@Inject(at = @At("HEAD"), method = "canPlaceAt", cancellable = true)
	public void canPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		BlockState groundState = world.getBlockState(pos.down());
		if (Main.isGrassType(groundState.getBlock()) && Main.isAnySlab(groundState.getBlock()) && world.getBlockState(pos).isAir()) {
			info.setReturnValue(true);
		}
	}
}
