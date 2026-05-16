package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabCropBlock extends CropBlock implements OffsetableSlab {

	// Offset shapes for bottom slab placement (8 pixels lower)
	private static final VoxelShape[] OFFSET_AGE_TO_SHAPE = new VoxelShape[]{
		Block.box(0.0, -8.0, 0.0, 16.0, -6.0, 16.0),   // age 0
		Block.box(0.0, -8.0, 0.0, 16.0, -4.0, 16.0),   // age 1
		Block.box(0.0, -8.0, 0.0, 16.0, -2.0, 16.0),   // age 2
		Block.box(0.0, -8.0, 0.0, 16.0, 0.0, 16.0),    // age 3
		Block.box(0.0, -8.0, 0.0, 16.0, 2.0, 16.0),    // age 4
		Block.box(0.0, -8.0, 0.0, 16.0, 4.0, 16.0),    // age 5
		Block.box(0.0, -8.0, 0.0, 16.0, 6.0, 16.0),    // age 6
		Block.box(0.0, -8.0, 0.0, 16.0, 8.0, 16.0)     // age 7
	};

	private final ItemLike seedsItem;

	public SlabCropBlock(Properties settings, ItemLike seedsItem) {
		super(settings);
		this.seedsItem = seedsItem;
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(BOTTOM_OFFSET, false));
	}

	@Override
	protected ItemLike getBaseSeedId() {
		return this.seedsItem;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE, BOTTOM_OFFSET);
	}

	@Override
	public BlockState getStateForAge(int age) {
		// Preserve BOTTOM_OFFSET when changing age - but we don't have access to current state here
		// So we need to override the methods that use withAge instead
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
					// Preserve BOTTOM_OFFSET when growing
					boolean offset = state.getValue(BOTTOM_OFFSET);
					world.setBlock(pos, this.withAgeAndOffset(age + 1, offset), Block.UPDATE_CLIENTS);
				}
			}
		}
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
		int newAge = this.getAge(state) + this.getBonemealAgeIncrease(world);
		int maxAge = this.getMaxAge();
		if (newAge > maxAge) {
			newAge = maxAge;
		}
		// Preserve BOTTOM_OFFSET when growing via bonemeal
		boolean offset = state.getValue(BOTTOM_OFFSET);
		world.setBlock(pos, this.withAgeAndOffset(newAge, offset), Block.UPDATE_CLIENTS);
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
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_AGE_TO_SHAPE[state.getValue(AGE)];
		}
		return super.getShape(state, world, pos, context);
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
