package justfatlard.dirt_slab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.enums.SlabType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class GrassSlab extends SpreadableSlab implements Fertilizable {
	public GrassSlab(Settings settings){
		super(settings, Blocks.GRASS_BLOCK);
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		if(!SpreadableSlab.canCoverSurvive(state, world, pos)) SlabEffects.setToDirt(world, pos);
		else SpreadableSlab.spreadableTick(state, world, pos, random);

		// For bottom slabs, check for floating vanilla vegetation above and convert to slab variants
		if(state.get(TYPE) == SlabType.BOTTOM){
			BlockPos abovePos = pos.up();
			BlockState aboveState = world.getBlockState(abovePos);
			BlockState plantSlabState = SlabRegistry.getPlantSlabDefaultState(aboveState.getBlock());
			if(plantSlabState != null){
				if(plantSlabState.contains(OffsetableSlab.BOTTOM_OFFSET)){
					plantSlabState = plantSlabState.with(OffsetableSlab.BOTTOM_OFFSET, true);
				}
				world.setBlockState(abovePos, plantSlabState, Block.NOTIFY_LISTENERS);
			}
		}
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
		BlockPos startPos = pos.up();

		// 128 attempts to place vegetation, with a random walk that spreads further each iteration
		growthAttempt:
		for(int attempt = 0; attempt < 128; ++attempt){
			BlockPos candidatePos = startPos;

			// Random walk: steps increase with attempt number (0 steps for attempts 0-15, 1 for 16-31, etc.)
			for(int step = 0; step < attempt / 16; ++step){
				candidatePos = candidatePos.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
				Block ground = world.getBlockState(candidatePos.down()).getBlock();

				if((ground != DirtSlabBlocks.GRASS_SLAB && ground != Blocks.GRASS_BLOCK) || world.getBlockState(candidatePos).isOpaqueFullCube()) continue growthAttempt;
			}

			BlockState candidateState = world.getBlockState(candidatePos);
			Block ground = world.getBlockState(candidatePos.down()).getBlock();
			boolean onSlab = ground == DirtSlabBlocks.GRASS_SLAB;

			// 10% chance to promote short grass to tall grass
			if(candidateState.getBlock() == Blocks.SHORT_GRASS && random.nextInt(10) == 0){
				((Fertilizable)Blocks.SHORT_GRASS).grow(world, random, candidatePos, candidateState);
				continue;
			}
			if(candidateState.getBlock() == DirtSlabBlocks.SHORT_GRASS_SLAB && random.nextInt(10) == 0){
				world.setBlockState(candidatePos, DirtSlabBlocks.TALL_GRASS_SLAB.getDefaultState(), 3);
				continue;
			}

			if(candidateState.isAir()){
				BlockState toPlace;
				if(random.nextInt(8) == 0 && random.nextInt(3) == 0){
					toPlace = onSlab ? DirtSlabBlocks.DANDELION_SLAB.getDefaultState() : Blocks.DANDELION.getDefaultState();
				} else {
					toPlace = onSlab ? DirtSlabBlocks.SHORT_GRASS_SLAB.getDefaultState() : Blocks.SHORT_GRASS.getDefaultState();
				}

				if(toPlace.canPlaceAt(world, candidatePos)) world.setBlockState(candidatePos, toPlace, 3);
			}
		}
	}
}
