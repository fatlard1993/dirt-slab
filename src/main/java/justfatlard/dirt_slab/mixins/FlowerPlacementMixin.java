package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.OffsetableSlab;
import justfatlard.dirt_slab.SlabCactusFlowerBlock;
import justfatlard.dirt_slab.SlabFireflyBushBlock;
import justfatlard.dirt_slab.SlabFlowerBlock;
import justfatlard.dirt_slab.SlabMushroomBlock;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabSaplingBlock;
import justfatlard.dirt_slab.SlabSporeBlossomBlock;
import justfatlard.dirt_slab.SlabTallFlowerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;

@Mixin(value = Block.class, priority = 1100)
public class FlowerPlacementMixin {
	@Inject(at = @At("HEAD"), method = "getStateForPlacement", cancellable = true)
	private void getFlowerPlacementState(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		Block self = (Block)(Object)this;

		// Handle regular flowers (but not our custom slab flowers)
		if (self instanceof FlowerBlock && !(self instanceof SlabFlowerBlock)) {
			handleRegularFlower(self, ctx, info);
			return;
		}

		// Handle tall flowers (but not our custom slab tall flowers)
		if (self instanceof TallFlowerBlock && !(self instanceof SlabTallFlowerBlock)) {
			handleTallFlower(self, ctx, info);
			return;
		}

		// Handle mushrooms (but not our custom slab mushrooms)
		if (self instanceof MushroomBlock && !(self instanceof SlabMushroomBlock)) {
			handleMushroom(self, ctx, info);
			return;
		}

		// Handle spore blossom (but not our custom slab spore blossom)
		if (self instanceof SporeBlossomBlock && !(self instanceof SlabSporeBlossomBlock)) {
			handleSporeBlossom(self, ctx, info);
			return;
		}

		// Handle cactus flower (but not our custom slab cactus flower)
		if (self.getClass() == Blocks.CACTUS_FLOWER.getClass() && !(self instanceof SlabCactusFlowerBlock)) {
			handleCactusFlower(self, ctx, info);
			return;
		}

		// Handle firefly bush (but not our custom slab firefly bush)
		if (self.getClass() == Blocks.FIREFLY_BUSH.getClass() && !(self instanceof SlabFireflyBushBlock)) {
			handleFireflyBush(self, ctx, info);
			return;
		}

		// Handle saplings (but not our custom slab saplings)
		if (self instanceof SaplingBlock && !(self instanceof SlabSaplingBlock)) {
			handleSapling(self, ctx, info);
			return;
		}
	}

	private void handleRegularFlower(Block self, BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos groundPos = clickedPos.below();
		BlockState groundState = ctx.getLevel().getBlockState(groundPos);

		// Check if placing on a dirt-type slab
		if (!SlabRegistry.isTerrainSlab(groundState.getBlock())) {
			return;
		}

		boolean isBottomSlab = false;
		if (groundState.getBlock() instanceof SlabBlock) {
			isBottomSlab = groundState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}

		Block flowerBlock = getSlabFlowerFor(self);

		if (flowerBlock != null) {
			BlockState flowerState = flowerBlock.defaultBlockState();
			if (flowerBlock instanceof OffsetableSlab) {
				flowerState = flowerState.setValue(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);
			}
			info.setReturnValue(flowerState);
		}
	}

	private void handleTallFlower(Block self, BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos groundPos = clickedPos.below();
		Level world = ctx.getLevel();
		BlockState groundState = world.getBlockState(groundPos);

		// Check if placing on a dirt-type slab
		if (!SlabRegistry.isTerrainSlab(groundState.getBlock())) {
			return;
		}

		// Check if there's room for the upper half
		BlockPos upperPos = clickedPos.above();
		if (!world.getBlockState(upperPos).canBeReplaced()) {
			return;
		}

		boolean isBottomSlab = false;
		if (groundState.getBlock() instanceof SlabBlock) {
			isBottomSlab = groundState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}

		Block tallFlowerBlock = getSlabTallFlowerFor(self);

		if (tallFlowerBlock != null) {
			// Only return the lower state - the upper half will be placed in onPlaced()
			BlockState lowerState = tallFlowerBlock.defaultBlockState()
				.setValue(TallFlowerBlock.HALF, DoubleBlockHalf.LOWER)
				.setValue(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);

			info.setReturnValue(lowerState);
		}
	}

	private Block getSlabFlowerFor(Block vanillaFlower) {
		return SlabRegistry.getPlantSlab(vanillaFlower);
	}

	private Block getSlabTallFlowerFor(Block vanillaTallFlower) {
		return SlabRegistry.getPlantSlab(vanillaTallFlower);
	}

	private void handleMushroom(Block self, BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos groundPos = clickedPos.below();
		BlockState groundState = ctx.getLevel().getBlockState(groundPos);

		// Check if placing on a dirt-type slab
		if (!SlabRegistry.isTerrainSlab(groundState.getBlock())) {
			return;
		}

		boolean isBottomSlab = false;
		if (groundState.getBlock() instanceof SlabBlock) {
			isBottomSlab = groundState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}

		Block mushroomBlock = getSlabMushroomFor(self);

		if (mushroomBlock != null) {
			BlockState mushroomState = mushroomBlock.defaultBlockState();
			if (mushroomBlock instanceof OffsetableSlab) {
				mushroomState = mushroomState.setValue(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);
			}
			info.setReturnValue(mushroomState);
		}
	}

	private Block getSlabMushroomFor(Block vanillaMushroom) {
		return SlabRegistry.getPlantSlab(vanillaMushroom);
	}

	private void handleSporeBlossom(Block self, BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos ceilingPos = clickedPos.above();
		BlockState ceilingState = ctx.getLevel().getBlockState(ceilingPos);

		// Check if placing below a slab (top or double)
		if (!(ceilingState.getBlock() instanceof SlabBlock)) {
			return;
		}

		SlabType type = ceilingState.getValue(SlabBlock.TYPE);
		if (type != SlabType.TOP && type != SlabType.DOUBLE) {
			return;
		}

		boolean isTopSlab = type == SlabType.TOP;

		BlockState sporeBlossomState = DirtSlabBlocks.SPORE_BLOSSOM_SLAB.defaultBlockState()
			.setValue(SlabSporeBlossomBlock.TOP_OFFSET, isTopSlab);

		info.setReturnValue(sporeBlossomState);
	}

	private void handleCactusFlower(Block self, BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos groundPos = clickedPos.below();
		BlockState groundState = ctx.getLevel().getBlockState(groundPos);

		// Check if placing on a slab
		if (!(groundState.getBlock() instanceof SlabBlock)) {
			return;
		}

		Block slabBlock = SlabRegistry.getPlantSlab(self);
		if (slabBlock == null) return;

		boolean isBottomSlab = groundState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;

		BlockState slabState = slabBlock.defaultBlockState()
			.setValue(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);

		info.setReturnValue(slabState);
	}

	private void handleFireflyBush(Block self, BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos groundPos = clickedPos.below();
		BlockState groundState = ctx.getLevel().getBlockState(groundPos);

		// Check if placing on a terrain slab
		if (!SlabRegistry.isTerrainSlab(groundState.getBlock())) {
			return;
		}

		Block slabBlock = SlabRegistry.getPlantSlab(self);
		if (slabBlock == null) return;

		boolean isBottomSlab = false;
		if (groundState.getBlock() instanceof SlabBlock) {
			isBottomSlab = groundState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}

		BlockState slabState = slabBlock.defaultBlockState()
			.setValue(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);

		info.setReturnValue(slabState);
	}

	private void handleSapling(Block self, BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos clickedPos = ctx.getClickedPos();
		BlockPos groundPos = clickedPos.below();
		BlockState groundState = ctx.getLevel().getBlockState(groundPos);

		// Check if placing on a dirt-type slab
		if (!SlabRegistry.isTerrainSlab(groundState.getBlock())) {
			return;
		}

		boolean isBottomSlab = false;
		if (groundState.getBlock() instanceof SlabBlock) {
			isBottomSlab = groundState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}

		Block saplingBlock = getSlabSaplingFor(self);

		if (saplingBlock != null) {
			BlockState saplingState = saplingBlock.defaultBlockState();
			if (saplingBlock instanceof OffsetableSlab) {
				saplingState = saplingState.setValue(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);
			}
			info.setReturnValue(saplingState);
		}
	}

	private Block getSlabSaplingFor(Block vanillaSapling) {
		return SlabRegistry.getPlantSlab(vanillaSapling);
	}
}
