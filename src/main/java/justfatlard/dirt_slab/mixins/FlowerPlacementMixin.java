package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.MushroomPlantBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SporeBlossomBlock;
import net.minecraft.block.TallFlowerBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlabCactusFlowerBlock;
import justfatlard.dirt_slab.SlabFireflyBushBlock;
import justfatlard.dirt_slab.SlabFlowerBlock;
import justfatlard.dirt_slab.SlabMushroomBlock;
import justfatlard.dirt_slab.SlabSaplingBlock;
import justfatlard.dirt_slab.SlabSporeBlossomBlock;
import justfatlard.dirt_slab.SlabTallFlowerBlock;

@Mixin(Block.class)
public class FlowerPlacementMixin {
	@Inject(at = @At("HEAD"), method = "getPlacementState", cancellable = true)
	private void getFlowerPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
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
		if (self instanceof MushroomPlantBlock && !(self instanceof SlabMushroomBlock)) {
			handleMushroom(self, ctx, info);
			return;
		}

		// Handle spore blossom (but not our custom slab spore blossom)
		if (self instanceof SporeBlossomBlock && !(self instanceof SlabSporeBlossomBlock)) {
			handleSporeBlossom(self, ctx, info);
			return;
		}

		// Handle cactus flower (but not our custom slab cactus flower)
		if (self == Blocks.CACTUS_FLOWER && !(self instanceof SlabCactusFlowerBlock)) {
			handleCactusFlower(self, ctx, info);
			return;
		}

		// Handle firefly bush (but not our custom slab firefly bush)
		if (self == Blocks.FIREFLY_BUSH && !(self instanceof SlabFireflyBushBlock)) {
			handleFireflyBush(self, ctx, info);
			return;
		}

		// Handle saplings (but not our custom slab saplings)
		if (self instanceof SaplingBlock && !(self instanceof SlabSaplingBlock)) {
			handleSapling(self, ctx, info);
			return;
		}
	}

	private void handleRegularFlower(Block self, ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
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

		Block flowerBlock = getSlabFlowerFor(self);

		if (flowerBlock != null) {
			BlockState flowerState = flowerBlock.getDefaultState();
			if (flowerBlock instanceof SlabFlowerBlock) {
				flowerState = flowerState.with(SlabFlowerBlock.BOTTOM_OFFSET, isBottomSlab);
			}
			info.setReturnValue(flowerState);
		}
	}

	private void handleTallFlower(Block self, ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos clickedPos = ctx.getBlockPos();
		BlockPos groundPos = clickedPos.down();
		World world = ctx.getWorld();
		BlockState groundState = world.getBlockState(groundPos);

		// Check if placing on a dirt-type slab
		if (!Main.isAnySlab(groundState.getBlock())) {
			return;
		}

		// Check if there's room for the upper half
		BlockPos upperPos = clickedPos.up();
		if (!world.getBlockState(upperPos).isReplaceable()) {
			return;
		}

		boolean isBottomSlab = false;
		if (groundState.getBlock() instanceof SlabBlock) {
			isBottomSlab = groundState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}

		Block tallFlowerBlock = getSlabTallFlowerFor(self);

		if (tallFlowerBlock != null) {
			// Only return the lower state - the upper half will be placed in onPlaced()
			BlockState lowerState = tallFlowerBlock.getDefaultState()
				.with(TallFlowerBlock.HALF, DoubleBlockHalf.LOWER)
				.with(SlabTallFlowerBlock.BOTTOM_OFFSET, isBottomSlab);

			info.setReturnValue(lowerState);
		}
	}

	private Block getSlabFlowerFor(Block vanillaFlower) {
		if (vanillaFlower == Blocks.DANDELION) return DirtSlabBlocks.DANDELION_SLAB;
		if (vanillaFlower == Blocks.POPPY) return DirtSlabBlocks.POPPY_SLAB;
		if (vanillaFlower == Blocks.BLUE_ORCHID) return DirtSlabBlocks.BLUE_ORCHID_SLAB;
		if (vanillaFlower == Blocks.ALLIUM) return DirtSlabBlocks.ALLIUM_SLAB;
		if (vanillaFlower == Blocks.AZURE_BLUET) return DirtSlabBlocks.AZURE_BLUET_SLAB;
		if (vanillaFlower == Blocks.RED_TULIP) return DirtSlabBlocks.RED_TULIP_SLAB;
		if (vanillaFlower == Blocks.ORANGE_TULIP) return DirtSlabBlocks.ORANGE_TULIP_SLAB;
		if (vanillaFlower == Blocks.WHITE_TULIP) return DirtSlabBlocks.WHITE_TULIP_SLAB;
		if (vanillaFlower == Blocks.PINK_TULIP) return DirtSlabBlocks.PINK_TULIP_SLAB;
		if (vanillaFlower == Blocks.OXEYE_DAISY) return DirtSlabBlocks.OXEYE_DAISY_SLAB;
		if (vanillaFlower == Blocks.CORNFLOWER) return DirtSlabBlocks.CORNFLOWER_SLAB;
		if (vanillaFlower == Blocks.LILY_OF_THE_VALLEY) return DirtSlabBlocks.LILY_OF_THE_VALLEY_SLAB;
		if (vanillaFlower == Blocks.WITHER_ROSE) return DirtSlabBlocks.WITHER_ROSE_SLAB;
		if (vanillaFlower == Blocks.TORCHFLOWER) return DirtSlabBlocks.TORCHFLOWER_SLAB;
		return null;
	}

	private Block getSlabTallFlowerFor(Block vanillaTallFlower) {
		if (vanillaTallFlower == Blocks.SUNFLOWER) return DirtSlabBlocks.SUNFLOWER_SLAB;
		if (vanillaTallFlower == Blocks.LILAC) return DirtSlabBlocks.LILAC_SLAB;
		if (vanillaTallFlower == Blocks.ROSE_BUSH) return DirtSlabBlocks.ROSE_BUSH_SLAB;
		if (vanillaTallFlower == Blocks.PEONY) return DirtSlabBlocks.PEONY_SLAB;
		return null;
	}

	private void handleMushroom(Block self, ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
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

		Block mushroomBlock = getSlabMushroomFor(self);

		if (mushroomBlock != null) {
			BlockState mushroomState = mushroomBlock.getDefaultState();
			if (mushroomBlock instanceof SlabMushroomBlock) {
				mushroomState = mushroomState.with(SlabMushroomBlock.BOTTOM_OFFSET, isBottomSlab);
			}
			info.setReturnValue(mushroomState);
		}
	}

	private Block getSlabMushroomFor(Block vanillaMushroom) {
		if (vanillaMushroom == Blocks.RED_MUSHROOM) return DirtSlabBlocks.RED_MUSHROOM_SLAB;
		if (vanillaMushroom == Blocks.BROWN_MUSHROOM) return DirtSlabBlocks.BROWN_MUSHROOM_SLAB;
		return null;
	}

	private void handleSporeBlossom(Block self, ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos clickedPos = ctx.getBlockPos();
		BlockPos ceilingPos = clickedPos.up();
		BlockState ceilingState = ctx.getWorld().getBlockState(ceilingPos);

		// Check if placing below a slab (top or double)
		if (!(ceilingState.getBlock() instanceof SlabBlock)) {
			return;
		}

		SlabType type = ceilingState.get(SlabBlock.TYPE);
		if (type != SlabType.TOP && type != SlabType.DOUBLE) {
			return;
		}

		boolean isTopSlab = type == SlabType.TOP;

		BlockState sporeBlossomState = DirtSlabBlocks.SPORE_BLOSSOM_SLAB.getDefaultState()
			.with(SlabSporeBlossomBlock.TOP_OFFSET, isTopSlab);

		info.setReturnValue(sporeBlossomState);
	}

	private void handleCactusFlower(Block self, ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos clickedPos = ctx.getBlockPos();
		BlockPos groundPos = clickedPos.down();
		BlockState groundState = ctx.getWorld().getBlockState(groundPos);

		// Check if placing on a slab
		if (!(groundState.getBlock() instanceof SlabBlock)) {
			return;
		}

		boolean isBottomSlab = groundState.get(SlabBlock.TYPE) == SlabType.BOTTOM;

		BlockState cactusFlowerState = DirtSlabBlocks.CACTUS_FLOWER_SLAB.getDefaultState()
			.with(SlabCactusFlowerBlock.BOTTOM_OFFSET, isBottomSlab);

		info.setReturnValue(cactusFlowerState);
	}

	private void handleFireflyBush(Block self, ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
		BlockPos clickedPos = ctx.getBlockPos();
		BlockPos groundPos = clickedPos.down();
		BlockState groundState = ctx.getWorld().getBlockState(groundPos);

		// Check if placing on a grass-type slab
		if (!Main.isAnySlab(groundState.getBlock())) {
			return;
		}

		boolean isBottomSlab = false;
		if (groundState.getBlock() instanceof SlabBlock) {
			isBottomSlab = groundState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}

		BlockState fireflyBushState = DirtSlabBlocks.FIREFLY_BUSH_SLAB.getDefaultState()
			.with(SlabFireflyBushBlock.BOTTOM_OFFSET, isBottomSlab);

		info.setReturnValue(fireflyBushState);
	}

	private void handleSapling(Block self, ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> info) {
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

		Block saplingBlock = getSlabSaplingFor(self);

		if (saplingBlock != null) {
			BlockState saplingState = saplingBlock.getDefaultState();
			if (saplingBlock instanceof SlabSaplingBlock) {
				saplingState = saplingState.with(SlabSaplingBlock.BOTTOM_OFFSET, isBottomSlab);
			}
			info.setReturnValue(saplingState);
		}
	}

	private Block getSlabSaplingFor(Block vanillaSapling) {
		if (vanillaSapling == Blocks.OAK_SAPLING) return DirtSlabBlocks.OAK_SAPLING_SLAB;
		if (vanillaSapling == Blocks.SPRUCE_SAPLING) return DirtSlabBlocks.SPRUCE_SAPLING_SLAB;
		if (vanillaSapling == Blocks.BIRCH_SAPLING) return DirtSlabBlocks.BIRCH_SAPLING_SLAB;
		if (vanillaSapling == Blocks.JUNGLE_SAPLING) return DirtSlabBlocks.JUNGLE_SAPLING_SLAB;
		if (vanillaSapling == Blocks.ACACIA_SAPLING) return DirtSlabBlocks.ACACIA_SAPLING_SLAB;
		if (vanillaSapling == Blocks.DARK_OAK_SAPLING) return DirtSlabBlocks.DARK_OAK_SAPLING_SLAB;
		if (vanillaSapling == Blocks.CHERRY_SAPLING) return DirtSlabBlocks.CHERRY_SAPLING_SLAB;
		if (vanillaSapling == Blocks.MANGROVE_PROPAGULE) return DirtSlabBlocks.MANGROVE_PROPAGULE_SLAB;
		if (vanillaSapling == Blocks.PALE_OAK_SAPLING) return DirtSlabBlocks.PALE_OAK_SAPLING_SLAB;
		return null;
	}
}
