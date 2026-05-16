package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabSnowLayerBlock extends Block implements OffsetableSlab {
	public static final MapCodec<SlabSnowLayerBlock> CODEC = simpleCodec(SlabSnowLayerBlock::new);
	public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;

	// Normal shapes (on full blocks or top slabs)
	protected static final VoxelShape[] LAYERS_TO_SHAPE = new VoxelShape[]{
		Shapes.empty(),
		Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
		Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
	};

	// Offset shapes (on bottom slabs - shifted down by 8)
	protected static final VoxelShape[] LAYERS_TO_SHAPE_OFFSET = new VoxelShape[]{
		Shapes.empty(),
		Block.box(0.0, -8.0, 0.0, 16.0, -6.0, 16.0),
		Block.box(0.0, -8.0, 0.0, 16.0, -4.0, 16.0),
		Block.box(0.0, -8.0, 0.0, 16.0, -2.0, 16.0),
		Block.box(0.0, -8.0, 0.0, 16.0, 0.0, 16.0),
		Block.box(0.0, -8.0, 0.0, 16.0, 2.0, 16.0),
		Block.box(0.0, -8.0, 0.0, 16.0, 4.0, 16.0),
		Block.box(0.0, -8.0, 0.0, 16.0, 6.0, 16.0),
		Block.box(0.0, -8.0, 0.0, 16.0, 8.0, 16.0)
	};

	public SlabSnowLayerBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1).setValue(BOTTOM_OFFSET, false));
	}

	@Override
	protected MapCodec<SlabSnowLayerBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LAYERS, BOTTOM_OFFSET);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		switch (type) {
			case LAND:
				return state.getValue(LAYERS) < 5;
			case WATER:
				return false;
			case AIR:
				return false;
			default:
				return false;
		}
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		int layers = state.getValue(LAYERS);
		if (state.getValue(BOTTOM_OFFSET)) {
			return LAYERS_TO_SHAPE_OFFSET[layers];
		}
		return LAYERS_TO_SHAPE[layers];
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		int layers = state.getValue(LAYERS);
		if (state.getValue(BOTTOM_OFFSET)) {
			// For bottom slab offset, layers 1-4 have no collision (below slab surface)
			// layers 5-8 have collision starting from slab surface
			if (layers <= 4) {
				return Shapes.empty();
			}
			return LAYERS_TO_SHAPE_OFFSET[layers - 4];
		}
		return LAYERS_TO_SHAPE[layers - 1];
	}

	@Override
	protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos pos) {
		int layers = state.getValue(LAYERS);
		if (state.getValue(BOTTOM_OFFSET)) {
			return LAYERS_TO_SHAPE_OFFSET[layers];
		}
		return LAYERS_TO_SHAPE[layers];
	}

	@Override
	protected VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		int layers = state.getValue(LAYERS);
		if (state.getValue(BOTTOM_OFFSET)) {
			return LAYERS_TO_SHAPE_OFFSET[layers];
		}
		return LAYERS_TO_SHAPE[layers];
	}

	@Override
	protected boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());

		// Can place on our dirt slabs (any type - TOP, BOTTOM, or DOUBLE)
		// For BOTTOM slabs, the snow renders with offset to sit on top of the half-height slab
		if (SlabRegistry.isTerrainSlab(below.getBlock())) {
			return true;
		}

		// Can place on vanilla snow layer support blocks
		if (!below.is(net.minecraft.tags.BlockTags.CANNOT_SUPPORT_SNOW_LAYER)) {
			if (below.is(net.minecraft.tags.BlockTags.SUPPORT_OVERRIDE_SNOW_LAYER)) {
				return true;
			}
			return Block.isFaceFull(below.getCollisionShape(world, pos.below()), Direction.UP) ||
				   (below.is(this) && below.getValue(LAYERS) == 8);
		}
		return false;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == Direction.DOWN && !canSurvive(state, world, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		// Melt if light level is too high
		if (world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos) > 11) {
			dropResources(state, world, pos);
			world.removeBlock(pos, false);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState existingState = ctx.getLevel().getBlockState(ctx.getClickedPos());
		if (existingState.is(this)) {
			int currentLayers = existingState.getValue(LAYERS);
			return existingState.setValue(LAYERS, Math.min(8, currentLayers + 1));
		}

		BlockState below = ctx.getLevel().getBlockState(ctx.getClickedPos().below());
		boolean shouldOffset = shouldOffset(below);
		return super.getStateForPlacement(ctx).setValue(BOTTOM_OFFSET, shouldOffset);
	}

	@Override
	protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		int layers = state.getValue(LAYERS);
		if (context.getItemInHand().is(this.asItem()) && layers < 8) {
			if (context.replacingClickedOnBlock()) {
				return context.getClickedFace() == Direction.UP;
			}
			return true;
		}
		return layers == 1;
	}

	public static boolean shouldOffset(BlockState below) {
		if (SlabRegistry.isTerrainSlab(below.getBlock()) && below.hasProperty(SlabBlock.TYPE)) {
			return below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	/**
	 * Creates a snow layer state appropriate for placement above the given block
	 */
	public static BlockState createForSlab(BlockState below) {
		boolean offset = shouldOffset(below);
		return DirtSlabBlocks.SNOW_LAYER_SLAB.defaultBlockState().setValue(BOTTOM_OFFSET, offset);
	}
}
