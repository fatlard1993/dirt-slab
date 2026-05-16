package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BambooSaplingBlock.class)
public class BambooSaplingMixin {
	@Inject(at = @At("HEAD"), method = "canSurvive", cancellable = true)
	public void canPlaceAt(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		BlockState groundState = world.getBlockState(pos.below());
		if (SlabRegistry.isGrassType(groundState.getBlock()) && SlabRegistry.isTerrainSlab(groundState.getBlock()) && world.getBlockState(pos).isAir()) {
			info.setReturnValue(true);
		}
	}
}
