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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabTorchflowerCropBlock extends CropBlock implements OffsetableSlab {
	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 2);

	private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[]{
		Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0),   // age 0
		Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0),  // age 1
		Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0)   // age 2 (same as 1 - will turn into flower)
	};

	private static final VoxelShape[] OFFSET_AGE_TO_SHAPE = new VoxelShape[]{
		Block.box(5.0, -8.0, 5.0, 11.0, -2.0, 11.0),  // age 0
		Block.box(5.0, -8.0, 5.0, 11.0, 2.0, 11.0),   // age 1
		Block.box(5.0, -8.0, 5.0, 11.0, 2.0, 11.0)    // age 2
	};

	public SlabTorchflowerCropBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(BOTTOM_OFFSET, false));
	}

	@Override
	protected IntegerProperty getAgeProperty() {
		return AGE;
	}

	@Override
	public int getMaxAge() {
		return 2;
	}

	@Override
	protected ItemLike getBaseSeedId() {
		return Items.TORCHFLOWER_SEEDS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE, BOTTOM_OFFSET);
	}

	@Override
	public BlockState getStateForAge(int age) {
		return this.defaultBlockState().setValue(AGE, age);
	}

	public BlockState withAgeAndOffset(int age, boolean offset) {
		return this.defaultBlockState().setValue(AGE, age).setValue(BOTTOM_OFFSET, offset);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (world.getRawBrightness(pos, 0) >= 9) {
			int age = this.getAge(state);
			if (age < this.getMaxAge()) {
				float moisture = getGrowthSpeed(this, world, pos);
				if (random.nextInt((int)(25.0F / moisture) + 1) == 0) {
					boolean offset = state.getValue(BOTTOM_OFFSET);
					if (age + 1 >= this.getMaxAge()) {
						// Transform into torchflower when fully grown
						world.setBlock(pos, DirtSlabBlocks.TORCHFLOWER_SLAB.defaultBlockState()
							.setValue(BOTTOM_OFFSET, offset), Block.UPDATE_CLIENTS);
					} else {
						world.setBlock(pos, this.withAgeAndOffset(age + 1, offset), Block.UPDATE_CLIENTS);
					}
				}
			}
		}
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
		int newAge = Math.min(this.getAge(state) + this.getBonemealAgeIncrease(world), this.getMaxAge());
		boolean offset = state.getValue(BOTTOM_OFFSET);

		if (newAge >= this.getMaxAge()) {
			// Transform into torchflower when fully grown
			world.setBlock(pos, DirtSlabBlocks.TORCHFLOWER_SLAB.defaultBlockState()
				.setValue(BOTTOM_OFFSET, offset), Block.UPDATE_CLIENTS);
		} else {
			world.setBlock(pos, this.withAgeAndOffset(newAge, offset), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
		return this.getAge(state) < this.getMaxAge();
	}

	@Override
	public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		int age = state.getValue(AGE);
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_AGE_TO_SHAPE[age];
		}
		return AGE_TO_SHAPE[age];
	}

	@Override
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		return floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
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
