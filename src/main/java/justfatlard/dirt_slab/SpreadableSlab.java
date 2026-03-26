package justfatlard.dirt_slab;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.tick.ScheduledTickView;

/**
 * Slab that spreads to adjacent dirt slabs (grass, mycelium).
 * Mirrors vanilla SpreadableBlock behavior for half-height slabs.
 */

public class SpreadableSlab extends SlabBlock {
	final Block baseBlock;
	public static final BooleanProperty SNOWY = Properties.SNOWY;

	public SpreadableSlab(Settings settings, Block baseBlock){
		super(settings);

		this.baseBlock = baseBlock;

		this.setDefaultState(this.stateManager.getDefaultState().with(TYPE, SlabType.BOTTOM).with(WATERLOGGED, false).with(SNOWY, false));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState blockState, World world, BlockPos blockPos, Random random){
		this.baseBlock.randomDisplayTick(blockState, world, blockPos, random);
	}

	@Override
	protected void randomTick(BlockState spreader, ServerWorld world, BlockPos pos, Random random){
		if(!canCoverSurvive(spreader, world, pos)) SlabEffects.setToDirt(world, pos);

		else spreadableTick(spreader, world, pos, random);
	}

	public static void spreadableTick(BlockState spreader, ServerWorld world, BlockPos pos, Random random){
		if(world.getLightLevel(pos.up()) < 9) return;

		for(int x = 0; x < 4; ++x){
			BlockPos randBlockPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
			BlockState spreadee = world.getBlockState(randBlockPos);

			if(!canSpread(spreader, world, randBlockPos)) continue;

			// Spread to vanilla dirt
			Block vanillaResult = SlabRegistry.getVanillaSpreadResult(spreader.getBlock());
			if(vanillaResult != null && spreadee.getBlock() == Blocks.DIRT){
				world.setBlockState(randBlockPos, vanillaResult.getDefaultState());
				continue;
			}

			// Spread to dirt slab
			Block slabResult = SlabRegistry.getSpreadResult(spreader.getBlock());
			if(slabResult != null && spreadee.getBlock() == DirtSlabBlocks.DIRT_SLAB){
				world.setBlockState(randBlockPos, SlabRegistry.copySlabProperties(spreadee, slabResult));
			}
		}
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		if(!canCoverSurvive(state, world, pos)) SlabEffects.setToDirt(world, pos);
	}

	/** Check if the spreadable covering (grass/mycelium) can survive — not the slab itself, but the surface layer. */
	public static boolean canCoverSurvive(BlockState state, WorldView world, BlockPos pos){
		BlockPos posUp = pos.up();
		BlockState topBlock = world.getBlockState(posUp);

		// Single snow layer always allows survival
		if(topBlock.getBlock() == Blocks.SNOW && topBlock.get(SnowBlock.LAYERS) == 1) return true;
		if(topBlock.isOf(DirtSlabBlocks.SNOW_LAYER_SLAB) && topBlock.get(SlabSnowLayerBlock.LAYERS) == 1) return true;

		// Top slabs survive under non-solid blocks (no full block above to smother them)
		if(state.getBlock() instanceof SpreadableSlab && !topBlock.isSolid() && state.get(TYPE) == SlabType.TOP) return true;

		// Otherwise check light opacity
		int i = ChunkLightProvider.getRealisticOpacity(state, topBlock, Direction.UP, topBlock.getOpacity());
		return i < 15;
	}

	public static boolean canSpread(BlockState state, WorldView world, BlockPos pos){
		if(!canCoverSurvive(state, world, pos)) return false;
		if(world.getFluidState(pos.up()).isIn(FluidTags.WATER)) return false;
		BlockState target = world.getBlockState(pos);
		if(target.getBlock() instanceof SpreadableSlab && target.get(WATERLOGGED) && target.get(TYPE) == SlabType.BOTTOM) return false;
		return true;
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random){
		if(direction == Direction.UP && !state.canPlaceAt(world, pos)) tickView.scheduleBlockTick(pos, this, 1);

		state = super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);

		if(direction == Direction.UP){
			return state.with(SNOWY, isSnow(neighborState));
		}
		return state;
	}

	private static boolean isSnow(BlockState state){
		Block block = state.getBlock();
		return block == Blocks.SNOW || block == Blocks.SNOW_BLOCK || state.isOf(DirtSlabBlocks.SNOW_LAYER_SLAB);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx){
		BlockState topState = ctx.getWorld().getBlockState(ctx.getBlockPos().up());

		return (!this.getDefaultState().canPlaceAt(ctx.getWorld(), ctx.getBlockPos()) ? pushEntitiesUpBeforeBlockChange(this.getDefaultState(), DirtSlabBlocks.DIRT_SLAB.getDefaultState(), ctx.getWorld(), ctx.getBlockPos()) : super.getPlacementState(ctx)).with(SNOWY, isSnow(topState));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){ builder.add(TYPE, WATERLOGGED, SNOWY); }
}
