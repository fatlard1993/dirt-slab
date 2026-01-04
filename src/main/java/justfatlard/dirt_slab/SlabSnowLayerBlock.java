package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class SlabSnowLayerBlock extends Block {
	public static final MapCodec<SlabSnowLayerBlock> CODEC = createCodec(SlabSnowLayerBlock::new);
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");
	public static final IntProperty LAYERS = Properties.LAYERS;

	// Normal shapes (on full blocks or top slabs)
	protected static final VoxelShape[] LAYERS_TO_SHAPE = new VoxelShape[]{
		VoxelShapes.empty(),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
		Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
	};

	// Offset shapes (on bottom slabs - shifted down by 8)
	protected static final VoxelShape[] LAYERS_TO_SHAPE_OFFSET = new VoxelShape[]{
		VoxelShapes.empty(),
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, -6.0, 16.0),
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, -4.0, 16.0),
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, -2.0, 16.0),
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, 0.0, 16.0),
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, 2.0, 16.0),
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, 4.0, 16.0),
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, 6.0, 16.0),
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, 8.0, 16.0)
	};

	public SlabSnowLayerBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(LAYERS, 1).with(BOTTOM_OFFSET, false));
	}

	@Override
	protected MapCodec<SlabSnowLayerBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(LAYERS, BOTTOM_OFFSET);
	}

	@Override
	protected boolean canPathfindThrough(BlockState state, NavigationType type) {
		switch (type) {
			case LAND:
				return state.get(LAYERS) < 5;
			case WATER:
				return false;
			case AIR:
				return false;
			default:
				return false;
		}
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		int layers = state.get(LAYERS);
		if (state.get(BOTTOM_OFFSET)) {
			return LAYERS_TO_SHAPE_OFFSET[layers];
		}
		return LAYERS_TO_SHAPE[layers];
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		int layers = state.get(LAYERS);
		if (state.get(BOTTOM_OFFSET)) {
			// For bottom slab offset, layers 1-4 have no collision (below slab surface)
			// layers 5-8 have collision starting from slab surface
			if (layers <= 4) {
				return VoxelShapes.empty();
			}
			return LAYERS_TO_SHAPE_OFFSET[layers - 4];
		}
		return LAYERS_TO_SHAPE[layers - 1];
	}

	@Override
	protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
		int layers = state.get(LAYERS);
		if (state.get(BOTTOM_OFFSET)) {
			return LAYERS_TO_SHAPE_OFFSET[layers];
		}
		return LAYERS_TO_SHAPE[layers];
	}

	@Override
	protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		int layers = state.get(LAYERS);
		if (state.get(BOTTOM_OFFSET)) {
			return LAYERS_TO_SHAPE_OFFSET[layers];
		}
		return LAYERS_TO_SHAPE[layers];
	}

	@Override
	protected boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());

		// Can place on our dirt slabs (any type - TOP, BOTTOM, or DOUBLE)
		// For BOTTOM slabs, the snow renders with offset to sit on top of the half-height slab
		if (Main.isAnySlab(below.getBlock())) {
			return true;
		}

		// Can place on vanilla snow layer support blocks
		if (!below.isIn(net.minecraft.registry.tag.BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
			if (below.isIn(net.minecraft.registry.tag.BlockTags.SNOW_LAYER_CAN_SURVIVE_ON)) {
				return true;
			}
			return Block.isFaceFullSquare(below.getCollisionShape(world, pos.down()), Direction.UP) ||
				   (below.isOf(this) && below.get(LAYERS) == 8);
		}
		return false;
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
		if (direction == Direction.DOWN && !canPlaceAt(state, world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		// Melt if light level is too high
		if (world.getLightLevel(net.minecraft.world.LightType.BLOCK, pos) > 11) {
			dropStacks(state, world, pos);
			world.removeBlock(pos, false);
		}
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState existingState = ctx.getWorld().getBlockState(ctx.getBlockPos());
		if (existingState.isOf(this)) {
			int currentLayers = existingState.get(LAYERS);
			return existingState.with(LAYERS, Math.min(8, currentLayers + 1));
		}

		BlockState below = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
		boolean shouldOffset = shouldOffset(below);
		return super.getPlacementState(ctx).with(BOTTOM_OFFSET, shouldOffset);
	}

	@Override
	protected boolean canReplace(BlockState state, ItemPlacementContext context) {
		int layers = state.get(LAYERS);
		if (context.getStack().isOf(this.asItem()) && layers < 8) {
			if (context.canReplaceExisting()) {
				return context.getSide() == Direction.UP;
			}
			return true;
		}
		return layers == 1;
	}

	public static boolean shouldOffset(BlockState below) {
		if (Main.isAnySlab(below.getBlock()) && below.contains(SlabBlock.TYPE)) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	/**
	 * Creates a snow layer state appropriate for placement above the given block
	 */
	public static BlockState createForSlab(BlockState below) {
		boolean offset = shouldOffset(below);
		return DirtSlabBlocks.SNOW_LAYER_SLAB.getDefaultState().with(BOTTOM_OFFSET, offset);
	}
}
