package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class SlabCactusFlowerBlock extends Block implements OffsetableSlab {
	public static final MapCodec<SlabCactusFlowerBlock> CODEC = createCodec(SlabCactusFlowerBlock::new);

	// Cross shape for cactus flower
	private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);
	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(2.0, -8.0, 2.0, 14.0, 5.0, 14.0);

	public SlabCactusFlowerBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(BOTTOM_OFFSET, false));
	}

	@Override
	protected MapCodec<SlabCactusFlowerBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		return SlabRegistry.isTerrainSlab(below.getBlock()) || below.getBlock() == Blocks.CACTUS;
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
		if (direction == Direction.DOWN && !canPlaceAt(state, world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
