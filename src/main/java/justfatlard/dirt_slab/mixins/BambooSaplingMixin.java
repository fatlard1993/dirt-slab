package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
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

	/**
	 * A vanilla stalk growing on a slab is settled into the slab kind before it grows, whole
	 * column at once, so it never grows a vanilla piece at full height over a lowered one.
	 */
	@Inject(at = @At("HEAD"), method = "randomTick", cancellable = true)
	private void dirtSlab$settleColumn(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo info) {
		if (justfatlard.dirt_slab.PlantColumns.settle(world, pos)) info.cancel();
	}
}
