package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class SlabSporeBlossomBlock extends Block {
	public static final MapCodec<SlabSporeBlossomBlock> CODEC = createCodec(SlabSporeBlossomBlock::new);
	public static final BooleanProperty TOP_OFFSET = BooleanProperty.of("top_offset");

	// Normal shape (hanging from full block ceiling)
	private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 13.0, 2.0, 14.0, 16.0, 14.0);
	// Offset shape for top slab placement (8 pixels higher)
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(2.0, 21.0, 2.0, 14.0, 24.0, 14.0);

	public SlabSporeBlossomBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(TOP_OFFSET, false));
	}

	@Override
	protected MapCodec<SlabSporeBlossomBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(TOP_OFFSET);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(TOP_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos ceilingPos = pos.up();
		BlockState ceilingState = world.getBlockState(ceilingPos);
		return canHangFrom(ceilingState);
	}

	private boolean canHangFrom(BlockState ceiling) {
		Block block = ceiling.getBlock();
		// Can hang from any top slab or double slab
		if (block instanceof SlabBlock) {
			SlabType type = ceiling.get(SlabBlock.TYPE);
			return type == SlabType.TOP || type == SlabType.DOUBLE;
		}
		// Can also hang from full blocks (vanilla behavior)
		return ceiling.isSideSolidFullSquare(null, null, Direction.DOWN);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
		// If the ceiling (block above) changes and we can no longer hang, break
		if (direction == Direction.UP && !canPlaceAt(state, world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState above = world.getBlockState(pos.up());
		if (above.getBlock() instanceof SlabBlock) {
			return above.get(SlabBlock.TYPE) == SlabType.TOP;
		}
		return false;
	}
}
