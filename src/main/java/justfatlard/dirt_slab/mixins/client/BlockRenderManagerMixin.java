package justfatlard.dirt_slab.mixins.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabCropBlock;
import justfatlard.dirt_slab.SlabFlowerBlock;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {

	@Inject(method = "renderBlock", at = @At("HEAD"))
	private void onRenderBlock(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<?> renderData, CallbackInfo ci) {
		// Check if this block should be offset
		boolean shouldOffset = false;

		if (state.getBlock() instanceof SlabCropBlock) {
			if (state.contains(SlabCropBlock.BOTTOM_OFFSET) && state.get(SlabCropBlock.BOTTOM_OFFSET)) {
				shouldOffset = true;
			}
		} else if (state.getBlock() instanceof SlabFlowerBlock) {
			if (state.contains(SlabFlowerBlock.BOTTOM_OFFSET) && state.get(SlabFlowerBlock.BOTTOM_OFFSET)) {
				shouldOffset = true;
			}
		} else if (state.getBlock() instanceof SugarCaneBlock) {
			// For vanilla sugar cane, check if it's on a bottom dirt slab
			shouldOffset = isOnBottomSlab(world, pos, state.getBlock());
		} else if (state.getBlock() instanceof BambooBlock) {
			// For vanilla bamboo, check if it's on a bottom dirt slab
			shouldOffset = isOnBottomSlabBamboo(world, pos);
		} else if (state.getBlock() instanceof BambooShootBlock) {
			// For vanilla bamboo shoot, check if it's on a bottom dirt slab
			shouldOffset = isOnBottomSlabDirect(world, pos);
		}

		if (shouldOffset) {
			// Translate down by 0.5 blocks (8 pixels)
			matrices.translate(0, -0.5, 0);
		}
	}

	private boolean isOnBottomSlab(BlockRenderView world, BlockPos pos, Block blockType) {
		// Trace down through stacked blocks of the same type to find the supporting block
		BlockPos checkPos = pos.down();
		BlockState below = world.getBlockState(checkPos);

		// Skip past any stacked blocks of the same type (e.g., sugar cane on sugar cane)
		while (below.getBlock() == blockType) {
			checkPos = checkPos.down();
			below = world.getBlockState(checkPos);
		}

		Block belowBlock = below.getBlock();
		// Check if it's one of our dirt slabs (without debug output)
		if (isDirtSlab(belowBlock) && belowBlock instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	// For bamboo - trace through bamboo blocks and bamboo saplings
	private boolean isOnBottomSlabBamboo(BlockRenderView world, BlockPos pos) {
		BlockPos checkPos = pos.down();
		BlockState below = world.getBlockState(checkPos);

		// Skip past any stacked bamboo or bamboo shoots
		while (below.getBlock() instanceof BambooBlock || below.getBlock() instanceof BambooShootBlock) {
			checkPos = checkPos.down();
			below = world.getBlockState(checkPos);
		}

		Block belowBlock = below.getBlock();
		if (isDirtSlab(belowBlock) && belowBlock instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	// For blocks that don't stack (like bamboo sapling) - direct check
	private boolean isOnBottomSlabDirect(BlockRenderView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		Block belowBlock = below.getBlock();
		if (isDirtSlab(belowBlock) && belowBlock instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	// Lightweight check without debug output for render path
	private boolean isDirtSlab(Block block) {
		return block == DirtSlabBlocks.COARSE_DIRT_SLAB ||
			   block == DirtSlabBlocks.DIRT_SLAB ||
			   block == DirtSlabBlocks.FARMLAND_SLAB ||
			   block == DirtSlabBlocks.GRASS_PATH_SLAB ||
			   block == DirtSlabBlocks.GRASS_SLAB ||
			   block == DirtSlabBlocks.MUD_SLAB ||
			   block == DirtSlabBlocks.MYCELIUM_SLAB ||
			   block == DirtSlabBlocks.PODZOL_SLAB ||
			   block == DirtSlabBlocks.ROOTED_DIRT_SLAB;
	}
}
