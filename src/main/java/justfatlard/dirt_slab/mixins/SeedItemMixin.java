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
import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlabBambooBlock;
import justfatlard.dirt_slab.SlabBambooShootBlock;
import justfatlard.dirt_slab.SlabCropBlock;
import justfatlard.dirt_slab.SlabPitcherCropBlock;
import justfatlard.dirt_slab.SlabStemBlock;
import justfatlard.dirt_slab.SlabSugarCaneBlock;
import justfatlard.dirt_slab.SlabTorchflowerCropBlock;
import justfatlard.dirt_slab.BeetrootSlabCropBlock;

@Mixin(Block.class)
public class SeedItemMixin {
	@Inject(at = @At("HEAD"), method = "getPlacementState", cancellable = true)
	private void getPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		System.out.println("[SeedMixin] getPlacementState called for: " + self.getClass().getSimpleName());

		// Handle sugar cane placement
		if (self instanceof SugarCaneBlock) {
			System.out.println("[SeedMixin]   -> Handling SugarCane");
			handleSugarCanePlacement(ctx, info);
			return;
		}

		// Handle bamboo shoot placement
		if (self instanceof BambooShootBlock) {
			System.out.println("[SeedMixin]   -> Handling BambooShoot");
			handleBambooShootPlacement(ctx, info);
			return;
		}

		// Handle bamboo placement
		if (self instanceof BambooBlock) {
			System.out.println("[SeedMixin]   -> Handling Bamboo");
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

		// Debug logging
		System.out.println("[SeedMixin] Placing " + self + " at " + clickedPos);
		System.out.println("[SeedMixin]   Block below: " + belowState.getBlock());
		System.out.println("[SeedMixin]   Block at pos: " + atState.getBlock());

		// Check if placing on farmland slab - check both below and at the clicked position
		BlockState farmlandState = null;
		if (belowState.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			farmlandState = belowState;
			System.out.println("[SeedMixin]   Found farmland slab BELOW");
		} else if (atState.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			// For bottom slabs, the farmland might be at the same position
			farmlandState = atState;
			System.out.println("[SeedMixin]   Found farmland slab AT position");
		}

		if (farmlandState != null) {
			boolean isBottomSlab = farmlandState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
			System.out.println("[SeedMixin]   Is bottom slab: " + isBottomSlab);

			Block targetBlock = null;

			// Determine which slab block to place based on the block type
			if (self == Blocks.WHEAT) {
				targetBlock = DirtSlabBlocks.WHEAT_SLAB_CROP;
			} else if (self == Blocks.CARROTS) {
				targetBlock = DirtSlabBlocks.CARROT_SLAB_CROP;
			} else if (self == Blocks.POTATOES) {
				targetBlock = DirtSlabBlocks.POTATO_SLAB_CROP;
			} else if (self == Blocks.BEETROOTS) {
				targetBlock = DirtSlabBlocks.BEETROOT_SLAB_CROP;
			} else if (self == Blocks.MELON_STEM) {
				targetBlock = DirtSlabBlocks.MELON_STEM_SLAB;
			} else if (self == Blocks.PUMPKIN_STEM) {
				targetBlock = DirtSlabBlocks.PUMPKIN_STEM_SLAB;
			} else if (self == Blocks.TORCHFLOWER_CROP) {
				targetBlock = DirtSlabBlocks.TORCHFLOWER_CROP_SLAB;
			} else if (self == Blocks.PITCHER_CROP) {
				targetBlock = DirtSlabBlocks.PITCHER_CROP_SLAB;
			}

			if (targetBlock != null) {
				BlockState targetState = targetBlock.getDefaultState();
				if (targetBlock instanceof SlabCropBlock) {
					targetState = targetState.with(SlabCropBlock.BOTTOM_OFFSET, isBottomSlab);
				} else if (targetBlock instanceof BeetrootSlabCropBlock) {
					targetState = targetState.with(BeetrootSlabCropBlock.BOTTOM_OFFSET, isBottomSlab);
				} else if (targetBlock instanceof SlabStemBlock) {
					targetState = targetState.with(SlabStemBlock.BOTTOM_OFFSET, isBottomSlab);
				} else if (targetBlock instanceof SlabTorchflowerCropBlock) {
					targetState = targetState.with(SlabTorchflowerCropBlock.BOTTOM_OFFSET, isBottomSlab);
				} else if (targetBlock instanceof SlabPitcherCropBlock) {
					targetState = targetState.with(SlabPitcherCropBlock.BOTTOM_OFFSET, isBottomSlab);
				}
				System.out.println("[SeedMixin]   Returning slab crop/stem: " + targetBlock);
				info.setReturnValue(targetState);
			}
		} else {
			System.out.println("[SeedMixin]   No farmland slab found, using vanilla behavior");
		}
	}

	private void handleSugarCanePlacement(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos pos = ctx.getBlockPos();
		BlockPos groundPos = pos.down();
		BlockState groundState = ctx.getWorld().getBlockState(groundPos);
		Block groundBlock = groundState.getBlock();

		// If placing on slab sugar cane, use slab variant with inherited offset
		if (groundBlock == DirtSlabBlocks.SUGAR_CANE_SLAB) {
			boolean bottomOffset = groundState.get(SlabSugarCaneBlock.BOTTOM_OFFSET);
			info.setReturnValue(DirtSlabBlocks.SUGAR_CANE_SLAB.getDefaultState()
				.with(SlabSugarCaneBlock.BOTTOM_OFFSET, bottomOffset));
			return;
		}

		// If placing on dirt slab, use slab variant
		if (isSugarCaneDirtSlab(groundBlock)) {
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
					.with(SlabSugarCaneBlock.BOTTOM_OFFSET, bottomOffset));
			}
		}
	}

	private void handleBambooShootPlacement(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos pos = ctx.getBlockPos();
		BlockState groundState = ctx.getWorld().getBlockState(pos.down());
		Block groundBlock = groundState.getBlock();

		System.out.println("[SeedMixin] BambooShoot placement at " + pos);
		System.out.println("[SeedMixin]   Ground block: " + groundBlock);
		System.out.println("[SeedMixin]   isGrassType: " + Main.isGrassType(groundBlock));
		System.out.println("[SeedMixin]   isAnySlab: " + Main.isAnySlab(groundBlock));
		System.out.println("[SeedMixin]   pos isAir: " + ctx.getWorld().getBlockState(pos).isAir());

		// If placing on a dirt slab, use slab bamboo shoot variant
		if (Main.isGrassType(groundBlock) && Main.isAnySlab(groundBlock) && ctx.getWorld().getBlockState(pos).isAir()) {
			boolean bottomOffset = false;
			if (groundBlock instanceof SlabBlock) {
				bottomOffset = groundState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
			}
			System.out.println("[SeedMixin]   Returning BAMBOO_SHOOT_SLAB with bottomOffset=" + bottomOffset);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SHOOT_SLAB.getDefaultState()
				.with(SlabBambooShootBlock.BOTTOM_OFFSET, bottomOffset));
		}
	}

	private void handleBambooPlacement(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos pos = ctx.getBlockPos();
		BlockState groundState = ctx.getWorld().getBlockState(pos.down());
		Block groundBlock = groundState.getBlock();

		System.out.println("[SeedMixin] Bamboo placement at " + pos);
		System.out.println("[SeedMixin]   Ground block: " + groundBlock);

		// If placing on slab bamboo, use slab variant with inherited offset
		if (groundBlock == DirtSlabBlocks.BAMBOO_SLAB) {
			boolean bottomOffset = groundState.get(SlabBambooBlock.BOTTOM_OFFSET);
			System.out.println("[SeedMixin]   Placing on BAMBOO_SLAB, bottomOffset=" + bottomOffset);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SLAB.getDefaultState()
				.with(SlabBambooBlock.BOTTOM_OFFSET, bottomOffset)
				.with(SlabBambooBlock.AGE, 0)
				.with(SlabBambooBlock.LEAVES, SlabBambooBlock.BambooLeaves.NONE)
				.with(SlabBambooBlock.STAGE, 0));
			return;
		}

		// If placing on slab bamboo shoot, use slab variant with inherited offset
		if (groundBlock == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			boolean bottomOffset = groundState.get(SlabBambooShootBlock.BOTTOM_OFFSET);
			System.out.println("[SeedMixin]   Placing on BAMBOO_SHOOT_SLAB, bottomOffset=" + bottomOffset);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SLAB.getDefaultState()
				.with(SlabBambooBlock.BOTTOM_OFFSET, bottomOffset)
				.with(SlabBambooBlock.AGE, 0)
				.with(SlabBambooBlock.LEAVES, SlabBambooBlock.BambooLeaves.NONE)
				.with(SlabBambooBlock.STAGE, 0));
			return;
		}

		// If placing on dirt slab, use bamboo shoot slab
		System.out.println("[SeedMixin]   isGrassType: " + Main.isGrassType(groundBlock));
		System.out.println("[SeedMixin]   isAnySlab: " + Main.isAnySlab(groundBlock));
		System.out.println("[SeedMixin]   pos isAir: " + ctx.getWorld().getBlockState(pos).isAir());
		if (Main.isGrassType(groundBlock) && Main.isAnySlab(groundBlock) && ctx.getWorld().getBlockState(pos).isAir()) {
			boolean bottomOffset = false;
			if (groundBlock instanceof SlabBlock) {
				bottomOffset = groundState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
			}
			System.out.println("[SeedMixin]   Returning BAMBOO_SHOOT_SLAB with bottomOffset=" + bottomOffset);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SHOOT_SLAB.getDefaultState()
				.with(SlabBambooShootBlock.BOTTOM_OFFSET, bottomOffset));
		}
	}

	private boolean isSugarCaneDirtSlab(Block block) {
		return block == DirtSlabBlocks.GRASS_SLAB ||
			   block == DirtSlabBlocks.DIRT_SLAB ||
			   block == DirtSlabBlocks.COARSE_DIRT_SLAB ||
			   block == DirtSlabBlocks.PODZOL_SLAB;
	}
}
