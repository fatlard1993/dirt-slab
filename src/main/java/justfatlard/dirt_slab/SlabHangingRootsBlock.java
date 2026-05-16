package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabHangingRootsBlock extends VegetationBlock {
	public static final MapCodec<SlabHangingRootsBlock> CODEC = simpleCodec(SlabHangingRootsBlock::new);
	public static final BooleanProperty TOP_OFFSET = BooleanProperty.create("top_offset");

	// Normal shape (hanging from full block ceiling)
	private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
	// Offset shape for top slab placement (8 pixels higher, hanging from bottom of top slab)
	private static final VoxelShape OFFSET_SHAPE = Block.box(2.0, 8.0, 2.0, 14.0, 24.0, 14.0);

	public SlabHangingRootsBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(TOP_OFFSET, false));
	}

	@Override
	protected MapCodec<? extends VegetationBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TOP_OFFSET);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(TOP_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		// Hanging roots attach to ceilings, not floors
		return false;
	}

	private boolean canHangFrom(BlockState ceiling) {
		Block block = ceiling.getBlock();
		// Can hang from rooted dirt slab or full rooted dirt
		if (block == DirtSlabBlocks.ROOTED_DIRT_SLAB) {
			// Only hang from TOP or DOUBLE slabs
			SlabType type = ceiling.getValue(SlabBlock.TYPE);
			return type == SlabType.TOP || type == SlabType.DOUBLE;
		}
		return block == Blocks.ROOTED_DIRT;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos pos = ctx.getClickedPos();
		boolean topOffset = shouldOffset(ctx.getLevel(), pos);
		return this.defaultBlockState().setValue(TOP_OFFSET, topOffset);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos ceilingPos = pos.above();
		BlockState ceilingState = world.getBlockState(ceilingPos);
		return canHangFrom(ceilingState);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		// If the ceiling (block above) changes and we can no longer hang, break
		if (direction == Direction.UP && !canSurvive(state, world, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	public boolean shouldOffset(LevelReader world, BlockPos pos) {
		BlockState above = world.getBlockState(pos.above());
		if (above.getBlock() == DirtSlabBlocks.ROOTED_DIRT_SLAB) {
			return above.getValue(SlabBlock.TYPE) == SlabType.TOP;
		}
		return false;
	}
}
