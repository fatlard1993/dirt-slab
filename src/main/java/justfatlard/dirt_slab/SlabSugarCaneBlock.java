package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabSugarCaneBlock extends Block implements OffsetableSlab {
	public static final MapCodec<SlabSugarCaneBlock> CODEC = simpleCodec(SlabSugarCaneBlock::new);
	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 15);

	// Same shape as vanilla sugar cane
	private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.box(2.0, -8.0, 2.0, 14.0, 8.0, 14.0);

	public SlabSugarCaneBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(BOTTOM_OFFSET, false)
			.setValue(AGE, 0));
	}

	@Override
	public MapCodec<SlabSugarCaneBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET, AGE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());

		// Can place on top of another slab sugar cane
		if (below.getBlock() == this) {
			return true;
		}

		// Can place on dirt slabs if there's water nearby
		if (isDirtSlab(below.getBlock())) {
			BlockPos groundPos = pos.below();
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				BlockState adjacentState = world.getBlockState(groundPos.relative(direction));
				FluidState fluidState = world.getFluidState(groundPos.relative(direction));
				if (fluidState.is(FluidTags.WATER) || adjacentState.getBlock() == Blocks.FROSTED_ICE) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isDirtSlab(Block block) {
		return SlabRegistry.isSugarCanePlantable(block);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		// Growth logic similar to vanilla sugar cane
		if (world.isEmptyBlock(pos.above())) {
			int height = 1;
			while (world.getBlockState(pos.below(height)).getBlock() == this) {
				height++;
			}

			if (height < 3) {
				int age = state.getValue(AGE);
				if (age == 15) {
					// Grow new sugar cane above, inheriting BOTTOM_OFFSET
					world.setBlockAndUpdate(pos.above(), this.defaultBlockState().setValue(BOTTOM_OFFSET, state.getValue(BOTTOM_OFFSET)));
					world.setBlock(pos, state.setValue(AGE, 0), Block.UPDATE_INVISIBLE);
				} else {
					world.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_INVISIBLE);
				}
			}
		}
	}

	@Override
	public boolean shouldOffset(LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		// If below is also slab sugar cane, inherit its offset
		if (below.getBlock() == this) {
			return below.getValue(BOTTOM_OFFSET);
		}
		// Otherwise check if on bottom slab
		if (isDirtSlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == Direction.DOWN && !this.canSurvive(state, world, pos)) {
			tickView.scheduleTick(pos, this, 1);
		}
		return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (!this.canSurvive(state, world, pos)) {
			world.destroyBlock(pos, true);
		}
	}

}
