package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.OffsetableSlab;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
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
	@Inject(at = @At("HEAD"), method = "getStateForPlacement", cancellable = true)
	private void getPlacementState(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Handle sugar cane placement
		if (self instanceof SugarCaneBlock) {
			handleSugarCanePlacement(ctx, info);
			return;
		}

		// Handle bamboo shoot placement
		if (self instanceof BambooSaplingBlock) {
			handleBambooShootPlacement(ctx, info);
			return;
		}

		// Handle bamboo placement
		if (self instanceof BambooStalkBlock) {
			handleBambooPlacement(ctx, info);
			return;
		}

		// Only apply to crop and stem blocks
		if (!(self instanceof CropBlock) && !(self instanceof StemBlock)) {
			return;
		}

		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos belowPos = clickedPos.below();
		BlockState belowState = ctx.getLevel().getBlockState(belowPos);
		BlockState atState = ctx.getLevel().getBlockState(clickedPos);

		// Check if placing on farmland slab - check both below and at the clicked position
		BlockState farmlandState = null;
		if (belowState.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			farmlandState = belowState;
		} else if (atState.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			// For bottom slabs, the farmland might be at the same position
			farmlandState = atState;
		}

		if (farmlandState != null) {
			boolean isBottomSlab = farmlandState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;

			Block targetBlock = null;

			// Look up the slab variant from the registry
			targetBlock = SlabRegistry.getCropSlab(self);

			if (targetBlock != null) {
				BlockState targetState = targetBlock.defaultBlockState();
				if (targetBlock instanceof OffsetableSlab) {
					targetState = targetState.setValue(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);
				}
				info.setReturnValue(targetState);
			}
		}
	}

	private void handleSugarCanePlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos pos = ctx.getClickedPos();
		BlockPos groundPos = pos.below();
		BlockState groundState = ctx.getLevel().getBlockState(groundPos);
		Block groundBlock = groundState.getBlock();

		// If placing on slab sugar cane, use slab variant with inherited offset
		if (groundBlock == DirtSlabBlocks.SUGAR_CANE_SLAB) {
			boolean bottomOffset = groundState.getValue(OffsetableSlab.BOTTOM_OFFSET);
			info.setReturnValue(DirtSlabBlocks.SUGAR_CANE_SLAB.defaultBlockState()
				.setValue(OffsetableSlab.BOTTOM_OFFSET, bottomOffset));
			return;
		}

		// If placing on dirt slab, use slab variant
		if (SlabRegistry.isSugarCanePlantable(groundBlock)) {
			// Check for water nearby (required for sugar cane)
			boolean hasWater = false;
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				FluidState fluidState = ctx.getLevel().getFluidState(groundPos.relative(direction));
				BlockState blockState = ctx.getLevel().getBlockState(groundPos.relative(direction));
				if (fluidState.is(FluidTags.WATER) || blockState.getBlock() == Blocks.FROSTED_ICE) {
					hasWater = true;
					break;
				}
			}

			if (hasWater) {
				boolean bottomOffset = groundState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
				info.setReturnValue(DirtSlabBlocks.SUGAR_CANE_SLAB.defaultBlockState()
					.setValue(OffsetableSlab.BOTTOM_OFFSET, bottomOffset));
			}
		}
	}

	private void handleBambooShootPlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos pos = ctx.getClickedPos();
		BlockState groundState = ctx.getLevel().getBlockState(pos.below());
		Block groundBlock = groundState.getBlock();

		// If placing on a dirt slab, use slab bamboo shoot variant
		if (SlabRegistry.isGrassType(groundBlock) && SlabRegistry.isTerrainSlab(groundBlock) && ctx.getLevel().getBlockState(pos).isAir()) {
			boolean bottomOffset = false;
			if (groundBlock instanceof SlabBlock) {
				bottomOffset = groundState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
			}
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SHOOT_SLAB.defaultBlockState()
				.setValue(OffsetableSlab.BOTTOM_OFFSET, bottomOffset));
		}
	}

	private void handleBambooPlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos pos = ctx.getClickedPos();
		BlockState groundState = ctx.getLevel().getBlockState(pos.below());
		Block groundBlock = groundState.getBlock();

		// If placing on slab bamboo, use slab variant with inherited offset
		if (groundBlock == DirtSlabBlocks.BAMBOO_SLAB) {
			boolean bottomOffset = groundState.getValue(OffsetableSlab.BOTTOM_OFFSET);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SLAB.defaultBlockState()
				.setValue(OffsetableSlab.BOTTOM_OFFSET, bottomOffset)
				.setValue(SlabBambooBlock.AGE, 0)
				.setValue(SlabBambooBlock.LEAVES, SlabBambooBlock.BambooLeaves.NONE)
				.setValue(SlabBambooBlock.STAGE, 0));
			return;
		}

		// If placing on slab bamboo shoot, use slab variant with inherited offset
		if (groundBlock == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			boolean bottomOffset = groundState.getValue(OffsetableSlab.BOTTOM_OFFSET);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SLAB.defaultBlockState()
				.setValue(OffsetableSlab.BOTTOM_OFFSET, bottomOffset)
				.setValue(SlabBambooBlock.AGE, 0)
				.setValue(SlabBambooBlock.LEAVES, SlabBambooBlock.BambooLeaves.NONE)
				.setValue(SlabBambooBlock.STAGE, 0));
			return;
		}

		// If placing on dirt slab, use bamboo shoot slab
		if (SlabRegistry.isGrassType(groundBlock) && SlabRegistry.isTerrainSlab(groundBlock) && ctx.getLevel().getBlockState(pos).isAir()) {
			boolean bottomOffset = false;
			if (groundBlock instanceof SlabBlock) {
				bottomOffset = groundState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
			}
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SHOOT_SLAB.defaultBlockState()
				.setValue(OffsetableSlab.BOTTOM_OFFSET, bottomOffset));
		}
	}

}
