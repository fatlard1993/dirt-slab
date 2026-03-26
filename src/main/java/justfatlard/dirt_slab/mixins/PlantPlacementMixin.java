package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.OffsetableSlab;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabBambooBlock;
import justfatlard.dirt_slab.SlabBambooShootBlock;

@Mixin(value = Block.class, priority = 900)
/**
 * Intercepts Block.getPlacementState to redirect plant placement onto slab terrain.
 * Targets Block.class because getPlacementState is defined there — subclass-targeted
 * mixins would modify the superclass method and affect all blocks anyway.
 * The instanceof dispatch handles: CropBlock, StemBlock, SugarCaneBlock, BambooBlock, BambooShootBlock.
 */
public class PlantPlacementMixin {
	@Inject(at = @At("HEAD"), method = "getPlacementState", cancellable = true)
	private void getPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Handle sugar cane placement
		if (self instanceof SugarCaneBlock) {
			handleSugarCanePlacement(ctx, info);
			return;
		}

		// Handle bamboo shoot placement
		if (self instanceof BambooShootBlock) {
			handleBambooShootPlacement(ctx, info);
			return;
		}

		// Handle bamboo placement
		if (self instanceof BambooBlock) {
			handleBambooPlacement(ctx, info);
			return;
		}

		// Only apply to crop and stem blocks
		if (!(self instanceof CropBlock) && !(self instanceof StemBlock)) {
			return;
		}

		BlockPos clickedPos = ctx.getBlockPos();
		BlockPos belowPos = clickedPos.down();
		BlockState belowState = ctx.getWorld().getBlockState(belowPos);
		BlockState atState = ctx.getWorld().getBlockState(clickedPos);

		// Check if placing on farmland slab - check both below and at the clicked position
		BlockState farmlandState = null;
		if (belowState.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			farmlandState = belowState;
		} else if (atState.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			// For bottom slabs, the farmland might be at the same position
			farmlandState = atState;
		}

		if (farmlandState != null) {
			boolean isBottomSlab = farmlandState.get(SlabBlock.TYPE) == SlabType.BOTTOM;

			Block targetBlock = null;

			// Look up the slab variant from the registry
			targetBlock = SlabRegistry.getCropSlab(self);

			if (targetBlock != null) {
				BlockState targetState = targetBlock.getDefaultState();
				if (targetBlock instanceof OffsetableSlab) {
					targetState = targetState.with(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);
				}
				info.setReturnValue(targetState);
			}
		}
	}

	private void handleSugarCanePlacement(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos pos = ctx.getBlockPos();
		BlockPos groundPos = pos.down();
		BlockState groundState = ctx.getWorld().getBlockState(groundPos);
		Block groundBlock = groundState.getBlock();

		// If placing on slab sugar cane, use slab variant with inherited offset
		if (groundBlock == DirtSlabBlocks.SUGAR_CANE_SLAB) {
			boolean bottomOffset = groundState.get(OffsetableSlab.BOTTOM_OFFSET);
			info.setReturnValue(DirtSlabBlocks.SUGAR_CANE_SLAB.getDefaultState()
				.with(OffsetableSlab.BOTTOM_OFFSET, bottomOffset));
			return;
		}

		// If placing on dirt slab, use slab variant
		if (SlabRegistry.isSugarCanePlantable(groundBlock)) {
			// Check for water nearby (required for sugar cane)
			boolean hasWater = false;
			for (Direction direction : Direction.Type.HORIZONTAL) {
				FluidState fluidState = ctx.getWorld().getFluidState(groundPos.offset(direction));
				BlockState blockState = ctx.getWorld().getBlockState(groundPos.offset(direction));
				if (fluidState.isIn(FluidTags.WATER) || blockState.getBlock() == Blocks.FROSTED_ICE) {
					hasWater = true;
					break;
				}
			}

			if (hasWater) {
				boolean bottomOffset = groundState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
				info.setReturnValue(DirtSlabBlocks.SUGAR_CANE_SLAB.getDefaultState()
					.with(OffsetableSlab.BOTTOM_OFFSET, bottomOffset));
			}
		}
	}

	private void handleBambooShootPlacement(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos pos = ctx.getBlockPos();
		BlockState groundState = ctx.getWorld().getBlockState(pos.down());
		Block groundBlock = groundState.getBlock();

		// If placing on a dirt slab, use slab bamboo shoot variant
		if (SlabRegistry.isGrassType(groundBlock) && SlabRegistry.isTerrainSlab(groundBlock) && ctx.getWorld().getBlockState(pos).isAir()) {
			boolean bottomOffset = false;
			if (groundBlock instanceof SlabBlock) {
				bottomOffset = groundState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
			}
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SHOOT_SLAB.getDefaultState()
				.with(OffsetableSlab.BOTTOM_OFFSET, bottomOffset));
		}
	}

	private void handleBambooPlacement(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos pos = ctx.getBlockPos();
		BlockState groundState = ctx.getWorld().getBlockState(pos.down());
		Block groundBlock = groundState.getBlock();

		// If placing on slab bamboo, use slab variant with inherited offset
		if (groundBlock == DirtSlabBlocks.BAMBOO_SLAB) {
			boolean bottomOffset = groundState.get(OffsetableSlab.BOTTOM_OFFSET);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SLAB.getDefaultState()
				.with(OffsetableSlab.BOTTOM_OFFSET, bottomOffset)
				.with(SlabBambooBlock.AGE, 0)
				.with(SlabBambooBlock.LEAVES, SlabBambooBlock.BambooLeaves.NONE)
				.with(SlabBambooBlock.STAGE, 0));
			return;
		}

		// If placing on slab bamboo shoot, use slab variant with inherited offset
		if (groundBlock == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			boolean bottomOffset = groundState.get(OffsetableSlab.BOTTOM_OFFSET);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SLAB.getDefaultState()
				.with(OffsetableSlab.BOTTOM_OFFSET, bottomOffset)
				.with(SlabBambooBlock.AGE, 0)
				.with(SlabBambooBlock.LEAVES, SlabBambooBlock.BambooLeaves.NONE)
				.with(SlabBambooBlock.STAGE, 0));
			return;
		}

		// If placing on dirt slab, use bamboo shoot slab
		if (SlabRegistry.isGrassType(groundBlock) && SlabRegistry.isTerrainSlab(groundBlock) && ctx.getWorld().getBlockState(pos).isAir()) {
			boolean bottomOffset = false;
			if (groundBlock instanceof SlabBlock) {
				bottomOffset = groundState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
			}
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SHOOT_SLAB.getDefaultState()
				.with(OffsetableSlab.BOTTOM_OFFSET, bottomOffset));
		}
	}

}
