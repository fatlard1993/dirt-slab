package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public class GrassSlab extends SpreadableSlab implements BonemealableBlock {
	public GrassSlab(Properties settings){
		super(settings, Blocks.GRASS_BLOCK);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random){
		if(!SpreadableSlab.canCoverSurvive(state, world, pos)) SlabEffects.setToDirt(world, pos);
		else SpreadableSlab.spreadableTick(state, world, pos, random);

		// For bottom slabs, check for floating vanilla vegetation above and convert to slab variants
		if(state.getValue(TYPE) == SlabType.BOTTOM){
			BlockPos abovePos = pos.above();
			BlockState aboveState = world.getBlockState(abovePos);
			BlockState plantSlabState = SlabRegistry.getPlantSlabDefaultState(aboveState.getBlock());
			if(plantSlabState != null){
				if(plantSlabState.hasProperty(OffsetableSlab.BOTTOM_OFFSET)){
					plantSlabState = plantSlabState.setValue(OffsetableSlab.BOTTOM_OFFSET, true);
				}
				world.setBlock(abovePos, plantSlabState, Block.UPDATE_CLIENTS);
			}
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state){
		return world.getBlockState(pos.above()).isAir();
	}

	@Override
	public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state){
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state){
		growAll(world, random, pos, state);
	}

	public static void growAll(ServerLevel world, RandomSource random, BlockPos pos, BlockState state){
		BlockPos startPos = pos.above();

		// 128 attempts to place vegetation, with a random walk that spreads further each iteration
		growthAttempt:
		for(int attempt = 0; attempt < 128; ++attempt){
			BlockPos candidatePos = startPos;

			// Random walk: steps increase with attempt number (0 steps for attempts 0-15, 1 for 16-31, etc.)
			for(int step = 0; step < attempt / 16; ++step){
				candidatePos = candidatePos.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
				Block ground = world.getBlockState(candidatePos.below()).getBlock();

				if((ground != DirtSlabBlocks.GRASS_SLAB && ground != Blocks.GRASS_BLOCK) || world.getBlockState(candidatePos).isSolidRender()) continue growthAttempt;
			}

			BlockState candidateState = world.getBlockState(candidatePos);
			Block ground = world.getBlockState(candidatePos.below()).getBlock();
			boolean onSlab = ground == DirtSlabBlocks.GRASS_SLAB;

			// 10% chance to promote short grass to tall grass
			if(candidateState.getBlock() == Blocks.SHORT_GRASS && random.nextInt(10) == 0){
				((BonemealableBlock)Blocks.SHORT_GRASS).performBonemeal(world, random, candidatePos, candidateState);
				continue;
			}
			if(candidateState.getBlock() == DirtSlabBlocks.SHORT_GRASS_SLAB && random.nextInt(10) == 0){
				world.setBlock(candidatePos, DirtSlabBlocks.TALL_GRASS_SLAB.defaultBlockState(), 3);
				continue;
			}

			if(candidateState.isAir()){
				BlockState toPlace;
				if(random.nextInt(8) == 0 && random.nextInt(3) == 0){
					toPlace = onSlab ? DirtSlabBlocks.DANDELION_SLAB.defaultBlockState() : Blocks.DANDELION.defaultBlockState();
				} else {
					toPlace = onSlab ? DirtSlabBlocks.SHORT_GRASS_SLAB.defaultBlockState() : Blocks.SHORT_GRASS.defaultBlockState();
				}

				if(toPlace.canSurvive(world, candidatePos)) world.setBlock(candidatePos, toPlace, 3);
			}
		}
	}
}
