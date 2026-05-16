package justfatlard.dirt_slab;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.lighting.LightEngine;

/**
 * Slab that spreads to adjacent dirt slabs (grass, mycelium).
 * Mirrors vanilla SpreadableBlock behavior for half-height slabs.
 */

public class SpreadableSlab extends SlabBlock {
	final Block baseBlock;
	public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

	public SpreadableSlab(Properties settings, Block baseBlock){
		super(settings);

		this.baseBlock = baseBlock;

		this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, false).setValue(SNOWY, false));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void animateTick(BlockState blockState, Level world, BlockPos blockPos, RandomSource random){
		this.baseBlock.animateTick(blockState, world, blockPos, random);
	}

	@Override
	protected void randomTick(BlockState spreader, ServerLevel world, BlockPos pos, RandomSource random){
		if(!canCoverSurvive(spreader, world, pos)) SlabEffects.setToDirt(world, pos);

		else spreadableTick(spreader, world, pos, random);
	}

	public static void spreadableTick(BlockState spreader, ServerLevel world, BlockPos pos, RandomSource random){
		if(world.getMaxLocalRawBrightness(pos.above()) < 9) return;

		for(int x = 0; x < 4; ++x){
			BlockPos randBlockPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
			BlockState spreadee = world.getBlockState(randBlockPos);

			if(!canSpread(spreader, world, randBlockPos)) continue;

			// Spread to vanilla dirt
			Block vanillaResult = SlabRegistry.getVanillaSpreadResult(spreader.getBlock());
			if(vanillaResult != null && spreadee.getBlock() == Blocks.DIRT){
				world.setBlockAndUpdate(randBlockPos, vanillaResult.defaultBlockState());
				continue;
			}

			// Spread to dirt slab
			Block slabResult = SlabRegistry.getSpreadResult(spreader.getBlock());
			if(slabResult != null && spreadee.getBlock() == DirtSlabBlocks.DIRT_SLAB){
				world.setBlockAndUpdate(randBlockPos, SlabRegistry.copySlabProperties(spreadee, slabResult));
			}
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random){
		if(!canCoverSurvive(state, world, pos)) SlabEffects.setToDirt(world, pos);
	}

	/** Check if the spreadable covering (grass/mycelium) can survive — not the slab itself, but the surface layer. */
	public static boolean canCoverSurvive(BlockState state, LevelReader world, BlockPos pos){
		BlockPos posUp = pos.above();
		BlockState topBlock = world.getBlockState(posUp);

		// Single snow layer always allows survival
		if(topBlock.getBlock() == Blocks.SNOW && topBlock.getValue(SnowLayerBlock.LAYERS) == 1) return true;
		if(topBlock.is(DirtSlabBlocks.SNOW_LAYER_SLAB) && topBlock.getValue(SlabSnowLayerBlock.LAYERS) == 1) return true;

		// Top slabs survive under non-solid blocks (no full block above to smother them)
		if(state.getBlock() instanceof SpreadableSlab && !topBlock.isSolid() && state.getValue(TYPE) == SlabType.TOP) return true;

		// Otherwise check light opacity
		int i = LightEngine.getLightBlockInto(state, topBlock, Direction.UP, topBlock.getLightDampening());
		return i < 15;
	}

	public static boolean canSpread(BlockState state, LevelReader world, BlockPos pos){
		if(!canCoverSurvive(state, world, pos)) return false;
		if(world.getFluidState(pos.above()).is(FluidTags.WATER)) return false;
		BlockState target = world.getBlockState(pos);
		if(target.getBlock() instanceof SpreadableSlab && target.getValue(WATERLOGGED) && target.getValue(TYPE) == SlabType.BOTTOM) return false;
		return true;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random){
		if(direction == Direction.UP && !state.canSurvive(world, pos)) tickView.scheduleTick(pos, this, 1);

		state = super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);

		if(direction == Direction.UP){
			return state.setValue(SNOWY, isSnow(neighborState));
		}
		return state;
	}

	private static boolean isSnow(BlockState state){
		Block block = state.getBlock();
		return block == Blocks.SNOW || block == Blocks.SNOW_BLOCK || state.is(DirtSlabBlocks.SNOW_LAYER_SLAB);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx){
		BlockState topState = ctx.getLevel().getBlockState(ctx.getClickedPos().above());

		return (!this.defaultBlockState().canSurvive(ctx.getLevel(), ctx.getClickedPos()) ? pushEntitiesUp(this.defaultBlockState(), DirtSlabBlocks.DIRT_SLAB.defaultBlockState(), ctx.getLevel(), ctx.getClickedPos()) : super.getStateForPlacement(ctx)).setValue(SNOWY, isSnow(topState));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){ builder.add(TYPE, WATERLOGGED, SNOWY); }
}
