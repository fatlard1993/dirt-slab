package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MossBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabMossCarpetBlock;

@Mixin(MossBlock.class)
public class MossBlockMixin {
	/**
	 * After moss block grows (bone meal), also spread moss carpet to nearby grass slabs.
	 */
	@Inject(at = @At("TAIL"), method = "grow")
	private void spreadToGrassSlabs(ServerWorld world, Random random, BlockPos pos, BlockState state, CallbackInfo ci) {
		// Spread moss carpet to nearby grass-type slabs (similar radius to vanilla moss spreading)
		int radius = 3;
		int verticalRadius = 2;

		for (int attempt = 0; attempt < 24; attempt++) {
			BlockPos targetPos = pos.add(
				random.nextInt(radius * 2 + 1) - radius,
				random.nextInt(verticalRadius * 2 + 1) - verticalRadius,
				random.nextInt(radius * 2 + 1) - radius
			);

			BlockState targetState = world.getBlockState(targetPos);
			Block targetBlock = targetState.getBlock();

			// Check if it's a grass-type slab that can have moss carpet on top
			if (isGrassTypeSlab(targetBlock)) {
				BlockPos abovePos = targetPos.up();
				BlockState aboveState = world.getBlockState(abovePos);

				// Place moss carpet if air above
				if (aboveState.isAir()) {
					// Check if bottom slab - use offset variant
					boolean isBottomSlab = targetState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
					if (isBottomSlab) {
						world.setBlockState(abovePos, DirtSlabBlocks.MOSS_CARPET_SLAB.getDefaultState()
							.with(SlabMossCarpetBlock.BOTTOM_OFFSET, true), Block.NOTIFY_ALL);
					} else {
						// Top or double slab - use vanilla moss carpet
						world.setBlockState(abovePos, Blocks.MOSS_CARPET.getDefaultState(), Block.NOTIFY_ALL);
					}
				}
			}
		}
	}

	private boolean isGrassTypeSlab(Block block) {
		return block == DirtSlabBlocks.GRASS_SLAB ||
			   block == DirtSlabBlocks.DIRT_SLAB ||
			   block == DirtSlabBlocks.PODZOL_SLAB ||
			   block == DirtSlabBlocks.MYCELIUM_SLAB ||
			   block == DirtSlabBlocks.COARSE_DIRT_SLAB;
	}
}
