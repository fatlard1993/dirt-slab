package justfatlard.dirt_slab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SlabPitcherCropBlock extends CropBlock {
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");
	public static final IntProperty AGE = IntProperty.of("age", 0, 4);
	public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

	private static final VoxelShape[] LOWER_AGE_TO_SHAPE = new VoxelShape[]{
		Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 11.0, 13.0),  // age 0
		Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 14.0, 13.0),  // age 1
		Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0),  // age 2
		Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0),  // age 3 (2 blocks tall)
		Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)   // age 4 (2 blocks tall)
	};

	private static final VoxelShape[] LOWER_OFFSET_AGE_TO_SHAPE = new VoxelShape[]{
		Block.createCuboidShape(3.0, -8.0, 3.0, 13.0, 3.0, 13.0),   // age 0
		Block.createCuboidShape(3.0, -8.0, 3.0, 13.0, 6.0, 13.0),   // age 1
		Block.createCuboidShape(2.0, -8.0, 2.0, 14.0, 8.0, 14.0),   // age 2
		Block.createCuboidShape(2.0, -8.0, 2.0, 14.0, 8.0, 14.0),   // age 3
		Block.createCuboidShape(2.0, -8.0, 2.0, 14.0, 8.0, 14.0)    // age 4
	};

	private static final VoxelShape UPPER_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
	private static final VoxelShape UPPER_OFFSET_SHAPE = Block.createCuboidShape(2.0, -8.0, 2.0, 14.0, 8.0, 14.0);

	public SlabPitcherCropBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(AGE, 0)
			.with(BOTTOM_OFFSET, false)
			.with(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected IntProperty getAgeProperty() {
		return AGE;
	}

	@Override
	public int getMaxAge() {
		return 4;
	}

	@Override
	protected ItemConvertible getSeedsItem() {
		return Items.PITCHER_POD;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AGE, BOTTOM_OFFSET, HALF);
	}

	@Override
	public BlockState withAge(int age) {
		return this.getDefaultState().with(AGE, age);
	}

	public BlockState withAgeAndOffset(int age, boolean offset, DoubleBlockHalf half) {
		return this.getDefaultState().with(AGE, age).with(BOTTOM_OFFSET, offset).with(HALF, half);
	}

	private boolean isTwoBlockTall(int age) {
		return age >= 3;
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		// Only the lower half should tick
		if (state.get(HALF) != DoubleBlockHalf.LOWER) {
			return;
		}

		if (world.getBaseLightLevel(pos, 0) >= 9) {
			int age = this.getAge(state);
			if (age < this.getMaxAge()) {
				float moisture = getAvailableMoisture(this, world, pos);
				if (random.nextInt((int)(25.0F / moisture) + 1) == 0) {
					growCrop(world, pos, state, age + 1);
				}
			}
		}
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		// If this is the upper half, find the lower half
		if (state.get(HALF) == DoubleBlockHalf.UPPER) {
			BlockPos lowerPos = pos.down();
			BlockState lowerState = world.getBlockState(lowerPos);
			if (lowerState.getBlock() == this) {
				grow(world, random, lowerPos, lowerState);
			}
			return;
		}

		int newAge = Math.min(this.getAge(state) + this.getGrowthAmount(world), this.getMaxAge());
		growCrop(world, pos, state, newAge);
	}

	private void growCrop(ServerWorld world, BlockPos pos, BlockState state, int newAge) {
		boolean offset = state.get(BOTTOM_OFFSET);
		int oldAge = this.getAge(state);

		// Check if we need to add or update the upper half
		if (isTwoBlockTall(newAge)) {
			BlockPos upperPos = pos.up();
			if (!isTwoBlockTall(oldAge)) {
				// Becoming two blocks tall - check if space is available
				if (!world.getBlockState(upperPos).isReplaceable()) {
					return; // Can't grow - no space
				}
			}

			// Set the lower half
			world.setBlockState(pos, this.withAgeAndOffset(newAge, offset, DoubleBlockHalf.LOWER), Block.NOTIFY_LISTENERS);
			// Set the upper half
			world.setBlockState(upperPos, this.withAgeAndOffset(newAge, offset, DoubleBlockHalf.UPPER), Block.NOTIFY_LISTENERS);
		} else {
			// Still single block
			world.setBlockState(pos, this.withAgeAndOffset(newAge, offset, DoubleBlockHalf.LOWER), Block.NOTIFY_LISTENERS);
		}
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
		// Only fertilizable if not at max age
		if (state.get(HALF) == DoubleBlockHalf.UPPER) {
			BlockPos lowerPos = pos.down();
			BlockState lowerState = world.getBlockState(lowerPos);
			if (lowerState.getBlock() == this) {
				return lowerState.get(AGE) < this.getMaxAge();
			}
			return false;
		}
		return this.getAge(state) < this.getMaxAge();
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		int age = state.get(AGE);
		boolean offset = state.get(BOTTOM_OFFSET);
		DoubleBlockHalf half = state.get(HALF);

		if (half == DoubleBlockHalf.UPPER) {
			return offset ? UPPER_OFFSET_SHAPE : UPPER_SHAPE;
		} else {
			return offset ? LOWER_OFFSET_AGE_TO_SHAPE[age] : LOWER_AGE_TO_SHAPE[age];
		}
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		return floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB;
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (state.get(HALF) == DoubleBlockHalf.UPPER) {
			BlockState below = world.getBlockState(pos.down());
			return below.getBlock() == this && below.get(HALF) == DoubleBlockHalf.LOWER && isTwoBlockTall(below.get(AGE));
		}
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
