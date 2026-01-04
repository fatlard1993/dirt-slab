package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class SlabSugarCaneBlock extends Block {
	public static final MapCodec<SlabSugarCaneBlock> CODEC = createCodec(SlabSugarCaneBlock::new);
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");
	public static final IntProperty AGE = IntProperty.of("age", 0, 15);

	// Same shape as vanilla sugar cane
	private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(2.0, -8.0, 2.0, 14.0, 8.0, 14.0);

	public SlabSugarCaneBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(BOTTOM_OFFSET, false)
			.with(AGE, 0));
	}

	@Override
	public MapCodec<SlabSugarCaneBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET, AGE);
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

		// Can place on top of another slab sugar cane
		if (below.getBlock() == this) {
			return true;
		}

		// Can place on dirt slabs if there's water nearby
		if (isDirtSlab(below.getBlock())) {
			BlockPos groundPos = pos.down();
			for (Direction direction : Direction.Type.HORIZONTAL) {
				BlockState adjacentState = world.getBlockState(groundPos.offset(direction));
				FluidState fluidState = world.getFluidState(groundPos.offset(direction));
				if (fluidState.isIn(FluidTags.WATER) || adjacentState.getBlock() == Blocks.FROSTED_ICE) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isDirtSlab(Block block) {
		return block == DirtSlabBlocks.GRASS_SLAB ||
			   block == DirtSlabBlocks.DIRT_SLAB ||
			   block == DirtSlabBlocks.COARSE_DIRT_SLAB ||
			   block == DirtSlabBlocks.PODZOL_SLAB;
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		// Growth logic similar to vanilla sugar cane
		if (world.isAir(pos.up())) {
			int height = 1;
			while (world.getBlockState(pos.down(height)).getBlock() == this) {
				height++;
			}

			if (height < 3) {
				int age = state.get(AGE);
				if (age == 15) {
					// Grow new sugar cane above, inheriting BOTTOM_OFFSET
					world.setBlockState(pos.up(), this.getDefaultState().with(BOTTOM_OFFSET, state.get(BOTTOM_OFFSET)));
					world.setBlockState(pos, state.with(AGE, 0), Block.NO_REDRAW);
				} else {
					world.setBlockState(pos, state.with(AGE, age + 1), Block.NO_REDRAW);
				}
			}
		}
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		// If below is also slab sugar cane, inherit its offset
		if (below.getBlock() == this) {
			return below.get(BOTTOM_OFFSET);
		}
		// Otherwise check if on bottom slab
		if (isDirtSlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
		if (direction == Direction.DOWN && !this.canPlaceAt(state, world, pos)) {
			tickView.scheduleBlockTick(pos, this, 1);
		}
		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (!this.canPlaceAt(state, world, pos)) {
			world.breakBlock(pos, true);
		}
	}

}
