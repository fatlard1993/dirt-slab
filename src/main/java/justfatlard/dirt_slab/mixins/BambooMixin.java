package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlabBambooBlock;
import justfatlard.dirt_slab.SlabBambooShootBlock;

@Mixin(BambooBlock.class)
public class BambooMixin {
	@Inject(at = @At("HEAD"), method = "canPlaceAt", cancellable = true)
	public void canPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		BlockState groundState = world.getBlockState(pos.down());
		Block groundBlock = groundState.getBlock();

		// Allow placement on slab bamboo or bamboo shoot
		if (groundBlock == DirtSlabBlocks.BAMBOO_SLAB || groundBlock == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			info.setReturnValue(true);
			return;
		}

		// Allow placement on dirt slabs
		if (Main.isGrassType(groundBlock) && Main.isAnySlab(groundBlock) && world.getBlockState(pos).isAir()) {
			info.setReturnValue(true);
		}
	}

	@Inject(at = @At("HEAD"), method = "getPlacementState", cancellable = true)
	public void getPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos pos = ctx.getBlockPos();
		BlockState groundState = ctx.getWorld().getBlockState(pos.down());
		Block groundBlock = groundState.getBlock();

		// If placing on slab bamboo, use slab variant with inherited offset
		if (groundBlock == DirtSlabBlocks.BAMBOO_SLAB) {
			boolean bottomOffset = groundState.get(SlabBambooBlock.BOTTOM_OFFSET);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SLAB.getDefaultState()
				.with(SlabBambooBlock.BOTTOM_OFFSET, bottomOffset)
				.with(SlabBambooBlock.AGE, 0)
				.with(SlabBambooBlock.LEAVES, SlabBambooBlock.BambooLeaves.NONE)
				.with(SlabBambooBlock.STAGE, 0));
			return;
		}

		// If placing on slab bamboo shoot, place bamboo above (conversion handled in SlabBambooBlock.onPlaced)
		if (groundBlock == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			boolean bottomOffset = groundState.get(SlabBambooShootBlock.BOTTOM_OFFSET);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SLAB.getDefaultState()
				.with(SlabBambooBlock.BOTTOM_OFFSET, bottomOffset)
				.with(SlabBambooBlock.AGE, 0)
				.with(SlabBambooBlock.LEAVES, SlabBambooBlock.BambooLeaves.NONE)
				.with(SlabBambooBlock.STAGE, 0));
			return;
		}

		// If placing on dirt slab, use bamboo shoot slab
		if (Main.isGrassType(groundBlock) && Main.isAnySlab(groundBlock) && ctx.getWorld().getBlockState(pos).isAir()) {
			boolean bottomOffset = false;
			if (groundBlock instanceof SlabBlock) {
				bottomOffset = groundState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
			}
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SHOOT_SLAB.getDefaultState()
				.with(SlabBambooShootBlock.BOTTOM_OFFSET, bottomOffset));
		}
	}
}
