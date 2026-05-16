package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import justfatlard.dirt_slab.SlabBambooBlock;
import justfatlard.dirt_slab.SlabBambooShootBlock;

@Mixin(BambooStalkBlock.class)
public class BambooMixin {
	@Inject(at = @At("HEAD"), method = "canSurvive", cancellable = true)
	public void canPlaceAt(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> info){
		BlockState groundState = world.getBlockState(pos.below());
		Block groundBlock = groundState.getBlock();

		// Allow placement on slab bamboo or bamboo shoot
		if (groundBlock == DirtSlabBlocks.BAMBOO_SLAB || groundBlock == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			info.setReturnValue(true);
			return;
		}

		// Allow placement on dirt slabs
		if (SlabRegistry.isGrassType(groundBlock) && SlabRegistry.isTerrainSlab(groundBlock) && world.getBlockState(pos).isAir()) {
			info.setReturnValue(true);
		}
	}

	@Inject(at = @At("HEAD"), method = "getStateForPlacement", cancellable = true)
	public void getPlacementState(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos pos = ctx.getClickedPos();
		BlockState groundState = ctx.getLevel().getBlockState(pos.below());
		Block groundBlock = groundState.getBlock();

		// If placing on slab bamboo, use slab variant with inherited offset
		if (groundBlock == DirtSlabBlocks.BAMBOO_SLAB) {
			boolean bottomOffset = groundState.getValue(SlabBambooBlock.BOTTOM_OFFSET);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SLAB.defaultBlockState()
				.setValue(SlabBambooBlock.BOTTOM_OFFSET, bottomOffset)
				.setValue(SlabBambooBlock.AGE, 0)
				.setValue(SlabBambooBlock.LEAVES, SlabBambooBlock.BambooLeaves.NONE)
				.setValue(SlabBambooBlock.STAGE, 0));
			return;
		}

		// If placing on slab bamboo shoot, place bamboo above (conversion handled in SlabBambooBlock.onPlaced)
		if (groundBlock == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			boolean bottomOffset = groundState.getValue(SlabBambooShootBlock.BOTTOM_OFFSET);
			info.setReturnValue(DirtSlabBlocks.BAMBOO_SLAB.defaultBlockState()
				.setValue(SlabBambooBlock.BOTTOM_OFFSET, bottomOffset)
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
				.setValue(SlabBambooShootBlock.BOTTOM_OFFSET, bottomOffset));
		}
	}
}
