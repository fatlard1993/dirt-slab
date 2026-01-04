package justfatlard.dirt_slab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;

public class GrassSlab extends SpreadableSlab implements Fertilizable {
	public GrassSlab(Settings settings){
		super(settings, Blocks.GRASS_BLOCK);
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		// Call the parent's scheduledTick logic for spreading/survival
		if(!SpreadableSlab.canSurvive(state, world, pos)) Main.setToDirt(world, pos);
		else Main.spreadableTick(state, world, pos, random);

		// For bottom slabs, check for floating vanilla vegetation above and convert to slab variants
		if(state.get(TYPE) == SlabType.BOTTOM){
			BlockPos abovePos = pos.up();
			BlockState aboveState = world.getBlockState(abovePos);
			BlockState plantSlabState = getPlantSlabFor(aboveState);
			if(plantSlabState != null){
				world.setBlockState(abovePos, plantSlabState, Block.NOTIFY_LISTENERS);
			}
		}
	}

	// Check if block is vanilla vegetation that needs conversion
	private static BlockState getPlantSlabFor(BlockState state){
		Block block = state.getBlock();

		// Short plants
		if(block == Blocks.SHORT_GRASS) return DirtSlabBlocks.SHORT_GRASS_SLAB.getDefaultState();
		if(block == Blocks.FERN) return DirtSlabBlocks.FERN_SLAB.getDefaultState();
		if(block == Blocks.DEAD_BUSH) return DirtSlabBlocks.DEAD_BUSH_SLAB.getDefaultState();
		if(block == Blocks.SHORT_DRY_GRASS) return DirtSlabBlocks.SHORT_DRY_GRASS_SLAB.getDefaultState();
		if(block == Blocks.TALL_DRY_GRASS) return DirtSlabBlocks.TALL_DRY_GRASS_SLAB.getDefaultState();

		// Flowers
		if(block == Blocks.DANDELION) return DirtSlabBlocks.DANDELION_SLAB.getDefaultState();
		if(block == Blocks.POPPY) return DirtSlabBlocks.POPPY_SLAB.getDefaultState();
		if(block == Blocks.BLUE_ORCHID) return DirtSlabBlocks.BLUE_ORCHID_SLAB.getDefaultState();
		if(block == Blocks.ALLIUM) return DirtSlabBlocks.ALLIUM_SLAB.getDefaultState();
		if(block == Blocks.AZURE_BLUET) return DirtSlabBlocks.AZURE_BLUET_SLAB.getDefaultState();
		if(block == Blocks.RED_TULIP) return DirtSlabBlocks.RED_TULIP_SLAB.getDefaultState();
		if(block == Blocks.ORANGE_TULIP) return DirtSlabBlocks.ORANGE_TULIP_SLAB.getDefaultState();
		if(block == Blocks.WHITE_TULIP) return DirtSlabBlocks.WHITE_TULIP_SLAB.getDefaultState();
		if(block == Blocks.PINK_TULIP) return DirtSlabBlocks.PINK_TULIP_SLAB.getDefaultState();
		if(block == Blocks.OXEYE_DAISY) return DirtSlabBlocks.OXEYE_DAISY_SLAB.getDefaultState();
		if(block == Blocks.CORNFLOWER) return DirtSlabBlocks.CORNFLOWER_SLAB.getDefaultState();
		if(block == Blocks.LILY_OF_THE_VALLEY) return DirtSlabBlocks.LILY_OF_THE_VALLEY_SLAB.getDefaultState();
		if(block == Blocks.WITHER_ROSE) return DirtSlabBlocks.WITHER_ROSE_SLAB.getDefaultState();
		if(block == Blocks.TORCHFLOWER) return DirtSlabBlocks.TORCHFLOWER_SLAB.getDefaultState();

		// Mushrooms
		if(block == Blocks.RED_MUSHROOM) return DirtSlabBlocks.RED_MUSHROOM_SLAB.getDefaultState();
		if(block == Blocks.BROWN_MUSHROOM) return DirtSlabBlocks.BROWN_MUSHROOM_SLAB.getDefaultState();

		// Leaf litter
		if(block == Blocks.LEAF_LITTER) return DirtSlabBlocks.LEAF_LITTER_SLAB.getDefaultState();

		// Pink petals and wildflowers
		if(block == Blocks.PINK_PETALS) return DirtSlabBlocks.PINK_PETALS_SLAB.getDefaultState();
		if(block == Blocks.WILDFLOWERS) return DirtSlabBlocks.WILDFLOWERS_SLAB.getDefaultState();

		// Firefly bush
		if(block == Blocks.FIREFLY_BUSH) return DirtSlabBlocks.FIREFLY_BUSH_SLAB.getDefaultState();

		// Sugar cane
		if(block == Blocks.SUGAR_CANE) return DirtSlabBlocks.SUGAR_CANE_SLAB.getDefaultState();

		// Bamboo
		if(block == Blocks.BAMBOO_SAPLING) return DirtSlabBlocks.BAMBOO_SHOOT_SLAB.getDefaultState();
		if(block == Blocks.BAMBOO) return DirtSlabBlocks.BAMBOO_SLAB.getDefaultState();

		// Tall plants (bottom half only)
		if(block == Blocks.TALL_GRASS) return DirtSlabBlocks.TALL_GRASS_SLAB.getDefaultState();
		if(block == Blocks.LARGE_FERN) return DirtSlabBlocks.LARGE_FERN_SLAB.getDefaultState();

		// Tall flowers (bottom half only)
		if(block == Blocks.SUNFLOWER) return DirtSlabBlocks.SUNFLOWER_SLAB.getDefaultState();
		if(block == Blocks.LILAC) return DirtSlabBlocks.LILAC_SLAB.getDefaultState();
		if(block == Blocks.ROSE_BUSH) return DirtSlabBlocks.ROSE_BUSH_SLAB.getDefaultState();
		if(block == Blocks.PEONY) return DirtSlabBlocks.PEONY_SLAB.getDefaultState();

		return null;
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state){
		return world.getBlockState(pos.up()).isAir();
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state){
		return true;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state){
		growAll(world, random, pos, state);
	}

	public static void growAll(ServerWorld world, Random random, BlockPos pos, BlockState state){
		BlockPos blockPos = pos.up();

		label48:
		for(int i = 0; i < 128; ++i){
			BlockPos blockPos2 = blockPos;

			for(int j = 0; j < i / 16; ++j){
				blockPos2 = blockPos2.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
				Block groundBlock = world.getBlockState(blockPos2.down()).getBlock();

				if((groundBlock != DirtSlabBlocks.GRASS_SLAB && groundBlock != Blocks.GRASS_BLOCK) || world.getBlockState(blockPos2).isOpaqueFullCube()) continue label48;
			}

			BlockState blockState2 = world.getBlockState(blockPos2);
			Block groundBlock = world.getBlockState(blockPos2.down()).getBlock();
			boolean onSlab = groundBlock == DirtSlabBlocks.GRASS_SLAB;

			// Handle tall grass growing taller
			if(blockState2.getBlock() == Blocks.SHORT_GRASS && random.nextInt(10) == 0){
				((Fertilizable)Blocks.SHORT_GRASS).grow(world, random, blockPos2, blockState2);
				continue;
			}
			if(blockState2.getBlock() == DirtSlabBlocks.SHORT_GRASS_SLAB && random.nextInt(10) == 0){
				// Convert to tall grass slab
				world.setBlockState(blockPos2, DirtSlabBlocks.TALL_GRASS_SLAB.getDefaultState(), 3);
				continue;
			}

			if(blockState2.isAir()){
				BlockState toPlace;
				if(random.nextInt(8) == 0){
					// Occasionally place a flower
					if(random.nextInt(3) == 0){
						toPlace = onSlab ? DirtSlabBlocks.DANDELION_SLAB.getDefaultState() : Blocks.DANDELION.getDefaultState();
					} else {
						toPlace = onSlab ? DirtSlabBlocks.SHORT_GRASS_SLAB.getDefaultState() : Blocks.SHORT_GRASS.getDefaultState();
					}
				} else {
					toPlace = onSlab ? DirtSlabBlocks.SHORT_GRASS_SLAB.getDefaultState() : Blocks.SHORT_GRASS.getDefaultState();
				}

				if(toPlace.canPlaceAt(world, blockPos2)) world.setBlockState(blockPos2, toPlace, 3);
			}
		}
	}
}
