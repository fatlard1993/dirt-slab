package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabPitcherCropBlock extends CropBlock implements OffsetableSlab {
	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

	private static final VoxelShape[] LOWER_AGE_TO_SHAPE = new VoxelShape[]{
		Block.box(3.0, 0.0, 3.0, 13.0, 11.0, 13.0),  // age 0
		Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0),  // age 1
		Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0),  // age 2
		Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0),  // age 3 (2 blocks tall)
		Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)   // age 4 (2 blocks tall)
	};

	private static final VoxelShape[] LOWER_OFFSET_AGE_TO_SHAPE = new VoxelShape[]{
		Block.box(3.0, -8.0, 3.0, 13.0, 3.0, 13.0),   // age 0
		Block.box(3.0, -8.0, 3.0, 13.0, 6.0, 13.0),   // age 1
		Block.box(2.0, -8.0, 2.0, 14.0, 8.0, 14.0),   // age 2
		Block.box(2.0, -8.0, 2.0, 14.0, 8.0, 14.0),   // age 3
		Block.box(2.0, -8.0, 2.0, 14.0, 8.0, 14.0)    // age 4
	};

	private static final VoxelShape UPPER_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
	private static final VoxelShape UPPER_OFFSET_SHAPE = Block.box(2.0, -8.0, 2.0, 14.0, 8.0, 14.0);

	public SlabPitcherCropBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(AGE, 0)
			.setValue(BOTTOM_OFFSET, false)
			.setValue(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected IntegerProperty getAgeProperty() {
		return AGE;
	}

	@Override
	public int getMaxAge() {
		return 4;
	}

	@Override
	protected ItemLike getBaseSeedId() {
		return Items.PITCHER_POD;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE, BOTTOM_OFFSET, HALF);
	}

	@Override
	public BlockState getStateForAge(int age) {
		return this.defaultBlockState().setValue(AGE, age);
	}

	public BlockState withAgeAndOffset(int age, boolean offset, DoubleBlockHalf half) {
		return this.defaultBlockState().setValue(AGE, age).setValue(BOTTOM_OFFSET, offset).setValue(HALF, half);
	}

	private boolean isTwoBlockTall(int age) {
		return age >= 3;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		// Only the lower half should tick
		if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {
			return;
		}

		if (world.getRawBrightness(pos, 0) >= 9) {
			int age = this.getAge(state);
			if (age < this.getMaxAge()) {
				float moisture = getGrowthSpeed(this, world, pos);
				if (random.nextInt((int)(25.0F / moisture) + 1) == 0) {
					growCrop(world, pos, state, age + 1);
				}
			}
		}
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
		// If this is the upper half, find the lower half
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			BlockPos lowerPos = pos.below();
			BlockState lowerState = world.getBlockState(lowerPos);
			if (lowerState.getBlock() == this) {
				performBonemeal(world, random, lowerPos, lowerState);
			}
			return;
		}

		int newAge = Math.min(this.getAge(state) + this.getBonemealAgeIncrease(world), this.getMaxAge());
		growCrop(world, pos, state, newAge);
	}

	private void growCrop(ServerLevel world, BlockPos pos, BlockState state, int newAge) {
		boolean offset = state.getValue(BOTTOM_OFFSET);
		int oldAge = this.getAge(state);

		if (newAge >= this.getMaxAge()) {
			// Transform into pitcher plant when fully grown
			BlockPos upperPos = pos.above();
			if (!isTwoBlockTall(oldAge) && !world.getBlockState(upperPos).canBeReplaced()) {
				return; // Can't grow - no space for upper half
			}
			world.setBlock(pos, DirtSlabBlocks.PITCHER_PLANT_SLAB.defaultBlockState()
				.setValue(SlabPitcherPlantBlock.HALF, DoubleBlockHalf.LOWER)
				.setValue(BOTTOM_OFFSET, offset), Block.UPDATE_CLIENTS);
			world.setBlock(upperPos, DirtSlabBlocks.PITCHER_PLANT_SLAB.defaultBlockState()
				.setValue(SlabPitcherPlantBlock.HALF, DoubleBlockHalf.UPPER)
				.setValue(BOTTOM_OFFSET, offset), Block.UPDATE_CLIENTS);
			return;
		}

		// Check if we need to add or update the upper half
		if (isTwoBlockTall(newAge)) {
			BlockPos upperPos = pos.above();
			if (!isTwoBlockTall(oldAge)) {
				// Becoming two blocks tall - check if space is available
				if (!world.getBlockState(upperPos).canBeReplaced()) {
					return; // Can't grow - no space
				}
			}

			// Set the lower half
			world.setBlock(pos, this.withAgeAndOffset(newAge, offset, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
			// Set the upper half
			world.setBlock(upperPos, this.withAgeAndOffset(newAge, offset, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
		} else {
			// Still single block
			world.setBlock(pos, this.withAgeAndOffset(newAge, offset, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
		// Only fertilizable if not at max age
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			BlockPos lowerPos = pos.below();
			BlockState lowerState = world.getBlockState(lowerPos);
			if (lowerState.getBlock() == this) {
				return lowerState.getValue(AGE) < this.getMaxAge();
			}
			return false;
		}
		return this.getAge(state) < this.getMaxAge();
	}

	@Override
	public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		int age = state.getValue(AGE);
		boolean offset = state.getValue(BOTTOM_OFFSET);
		DoubleBlockHalf half = state.getValue(HALF);

		if (half == DoubleBlockHalf.UPPER) {
			return offset ? UPPER_OFFSET_SHAPE : UPPER_SHAPE;
		} else {
			return offset ? LOWER_OFFSET_AGE_TO_SHAPE[age] : LOWER_AGE_TO_SHAPE[age];
		}
	}

	@Override
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		return floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			BlockState below = world.getBlockState(pos.below());
			return below.getBlock() == this && below.getValue(HALF) == DoubleBlockHalf.LOWER && isTwoBlockTall(below.getValue(AGE));
		}
		BlockPos below = pos.below();
		BlockState floorState = world.getBlockState(below);
		return mayPlaceOn(floorState, world, below);
	}

	@Override
	public boolean shouldOffset(LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		if (below.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			return below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
