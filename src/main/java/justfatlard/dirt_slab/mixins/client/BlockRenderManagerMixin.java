package justfatlard.dirt_slab.mixins.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

import justfatlard.dirt_slab.OffsetableSlab;
import justfatlard.dirt_slab.SlabRegistry;

@Mixin(BlockRenderManager.class)
public class BlockRenderManagerMixin {

	@Unique
	private static final ThreadLocal<Boolean> dirtSlab$pushed = ThreadLocal.withInitial(() -> false);

	@Inject(method = "renderBlock", at = @At("HEAD"))
	private void onRenderBlock(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<?> renderData, CallbackInfo ci) {
		boolean shouldOffset = false;

		if (state.contains(OffsetableSlab.BOTTOM_OFFSET)) {
			shouldOffset = state.get(OffsetableSlab.BOTTOM_OFFSET);
		} else if (state.getBlock() instanceof SugarCaneBlock) {
			shouldOffset = isOnBottomSlab(world, pos, state.getBlock());
		} else if (state.getBlock() instanceof BambooBlock) {
			shouldOffset = isOnBottomSlabBamboo(world, pos);
		} else if (state.getBlock() instanceof BambooShootBlock) {
			shouldOffset = isOnBottomSlabDirect(world, pos);
		}

		if (shouldOffset) {
			matrices.push();
			matrices.translate(0, -0.5, 0);
			dirtSlab$pushed.set(true);
		}
	}

	@Inject(method = "renderBlock", at = @At("RETURN"))
	private void onRenderBlockReturn(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<?> renderData, CallbackInfo ci) {
		if (dirtSlab$pushed.get()) {
			matrices.pop();
			dirtSlab$pushed.set(false);
		}
	}

	private static final int MAX_STACK_DEPTH = 32;

	private boolean isOnBottomSlab(BlockRenderView world, BlockPos pos, Block blockType) {
		BlockPos checkPos = pos.down();
		BlockState below = world.getBlockState(checkPos);

		int depth = 0;
		while (below.getBlock() == blockType && depth < MAX_STACK_DEPTH) {
			checkPos = checkPos.down();
			below = world.getBlockState(checkPos);
			depth++;
		}

		Block belowBlock = below.getBlock();
		if (isDirtSlab(belowBlock) && belowBlock instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	private boolean isOnBottomSlabBamboo(BlockRenderView world, BlockPos pos) {
		BlockPos checkPos = pos.down();
		BlockState below = world.getBlockState(checkPos);

		int depth = 0;
		while ((below.getBlock() instanceof BambooBlock || below.getBlock() instanceof BambooShootBlock) && depth < MAX_STACK_DEPTH) {
			checkPos = checkPos.down();
			below = world.getBlockState(checkPos);
			depth++;
		}

		Block belowBlock = below.getBlock();
		if (isDirtSlab(belowBlock) && belowBlock instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	private boolean isOnBottomSlabDirect(BlockRenderView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		Block belowBlock = below.getBlock();
		if (isDirtSlab(belowBlock) && belowBlock instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	private boolean isDirtSlab(Block block) {
		return SlabRegistry.isTerrainSlab(block);
	}
}
