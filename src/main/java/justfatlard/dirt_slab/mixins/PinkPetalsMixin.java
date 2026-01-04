package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerbedBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlabPinkPetalsBlock;
import justfatlard.dirt_slab.SlabWildflowersBlock;

@Mixin(FlowerbedBlock.class)
public class PinkPetalsMixin {
	@Inject(at = @At("HEAD"), method = "getPlacementState", cancellable = true)
	private void getFlowerbedPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Skip if this is already our slab flowerbed
		if (self instanceof SlabPinkPetalsBlock || self instanceof SlabWildflowersBlock) {
			return;
		}

		BlockPos clickedPos = ctx.getBlockPos();
		BlockPos groundPos = clickedPos.down();
		BlockState groundState = ctx.getWorld().getBlockState(groundPos);

		// Check if placing on a dirt-type slab
		if (!Main.isAnySlab(groundState.getBlock())) {
			return;
		}

		boolean isBottomSlab = false;
		if (groundState.getBlock() instanceof SlabBlock) {
			isBottomSlab = groundState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}

		// Get the facing direction from placement context
		Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();

		// Determine which slab block to use based on the vanilla block being placed
		BlockState slabState;
		if (self == Blocks.WILDFLOWERS) {
			slabState = DirtSlabBlocks.WILDFLOWERS_SLAB.getDefaultState()
				.with(SlabWildflowersBlock.FACING, facing)
				.with(SlabWildflowersBlock.FLOWER_AMOUNT, 1)
				.with(SlabWildflowersBlock.BOTTOM_OFFSET, isBottomSlab);
		} else {
			// Default to pink petals for Blocks.PINK_PETALS or any other flowerbed
			slabState = DirtSlabBlocks.PINK_PETALS_SLAB.getDefaultState()
				.with(SlabPinkPetalsBlock.FACING, facing)
				.with(SlabPinkPetalsBlock.FLOWER_AMOUNT, 1)
				.with(SlabPinkPetalsBlock.BOTTOM_OFFSET, isBottomSlab);
		}

		info.setReturnValue(slabState);
	}
}
