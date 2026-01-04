package justfatlard.dirt_slab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.CropBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class BeetrootSlabCropBlock extends BeetrootsBlock {
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");

	// Offset shapes for bottom slab placement (8 pixels lower)
	private static final VoxelShape[] OFFSET_AGE_TO_SHAPE = new VoxelShape[]{
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, -6.0, 16.0),   // age 0
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, -4.0, 16.0),   // age 1
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, -2.0, 16.0),   // age 2
		Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, 0.0, 16.0)     // age 3
	};

	public BeetrootSlabCropBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(AGE, 0).with(BOTTOM_OFFSET, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AGE, BOTTOM_OFFSET);
	}

	public BlockState withAgeAndOffset(int age, boolean offset) {
		return this.getDefaultState().with(AGE, age).with(BOTTOM_OFFSET, offset);
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (world.getBaseLightLevel(pos, 0) >= 9) {
			int age = this.getAge(state);
			if (age < this.getMaxAge()) {
				float moisture = CropBlock.getAvailableMoisture(this, world, pos);
				if (random.nextInt((int)(25.0F / moisture) + 1) == 0) {
					// Preserve BOTTOM_OFFSET when growing
					boolean offset = state.get(BOTTOM_OFFSET);
					world.setBlockState(pos, this.withAgeAndOffset(age + 1, offset), Block.NOTIFY_LISTENERS);
				}
			}
		}
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		int newAge = Math.min(this.getAge(state) + this.getGrowthAmount(world), this.getMaxAge());
		// Preserve BOTTOM_OFFSET when growing via bonemeal
		boolean offset = state.get(BOTTOM_OFFSET);
		world.setBlockState(pos, this.withAgeAndOffset(newAge, offset), Block.NOTIFY_LISTENERS);
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
		return this.getAge(state) < this.getMaxAge();
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(BOTTOM_OFFSET)) {
			return OFFSET_AGE_TO_SHAPE[state.get(AGE)];
		}
		return super.getOutlineShape(state, world, pos, context);
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		return floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB;
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos below = pos.down();
		BlockState floorState = world.getBlockState(below);
		return canPlantOnTop(floorState, world, below);
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (below.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
