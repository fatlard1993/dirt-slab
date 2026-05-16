package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabMossCarpetBlock;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableFeaturePlacerBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

@Mixin(BonemealableFeaturePlacerBlock.class)
public class MossBlockMixin {
	/**
	 * After moss block grows (bone meal), also spread moss carpet to nearby grass slabs.
	 */
	@Inject(at = @At("TAIL"), method = "performBonemeal")
	private void spreadToGrassSlabs(ServerLevel world, RandomSource random, BlockPos pos, BlockState state, CallbackInfo ci) {
		// Spread moss carpet to nearby grass-type slabs (similar radius to vanilla moss spreading)
		int radius = 3;
		int verticalRadius = 2;

		for (int attempt = 0; attempt < 24; attempt++) {
			BlockPos targetPos = pos.offset(
				random.nextInt(radius * 2 + 1) - radius,
				random.nextInt(verticalRadius * 2 + 1) - verticalRadius,
				random.nextInt(radius * 2 + 1) - radius
			);

			BlockState targetState = world.getBlockState(targetPos);
			Block targetBlock = targetState.getBlock();

			// Check if it's a grass-type slab that can have moss carpet on top
			if (isGrassTypeSlab(targetBlock)) {
				BlockPos abovePos = targetPos.above();
				BlockState aboveState = world.getBlockState(abovePos);

				// Place moss carpet if air above
				if (aboveState.isAir()) {
					// Check if bottom slab - use offset variant
					boolean isBottomSlab = targetState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
					if (isBottomSlab) {
						world.setBlock(abovePos, DirtSlabBlocks.MOSS_CARPET_SLAB.defaultBlockState()
							.setValue(SlabMossCarpetBlock.BOTTOM_OFFSET, true), Block.UPDATE_ALL);
					} else {
						// Top or double slab - use vanilla moss carpet
						world.setBlock(abovePos, Blocks.MOSS_CARPET.defaultBlockState(), Block.UPDATE_ALL);
					}
				}
			}
		}
	}

	private boolean isGrassTypeSlab(Block block) {
		return SlabRegistry.isGrassType(block);
	}
}
