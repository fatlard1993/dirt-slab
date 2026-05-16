package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabSmallDripleafBlock extends DoublePlantBlock implements BonemealableBlock, OffsetableSlab {
	@SuppressWarnings("unchecked")
	public static final MapCodec<DoublePlantBlock> CODEC = (MapCodec<DoublePlantBlock>)(MapCodec<?>)simpleCodec(SlabSmallDripleafBlock::new);

	private static final VoxelShape NORMAL_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
	private static final VoxelShape OFFSET_SHAPE = Block.box(2.0, -8.0, 2.0, 14.0, 8.0, 14.0);

	public SlabSmallDripleafBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(HALF, DoubleBlockHalf.LOWER)
			.setValue(BOTTOM_OFFSET, false));
	}

	@Override
	public MapCodec<DoublePlantBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return NORMAL_SHAPE;
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(Items.SMALL_DRIPLEAF);
	}

	@Override
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		// Small dripleaf can grow on clay, moss, mud, and dirt-type slabs
		return floor.is(Blocks.CLAY) ||
			   floor.is(Blocks.MOSS_BLOCK) ||
			   floor.is(Blocks.MUD) ||
			   SlabRegistry.isPlantable(floor.getBlock());
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
			BlockPos upperPos = pos.above();
			boolean isOffset = state.getValue(BOTTOM_OFFSET);
			BlockState upperState = this.defaultBlockState()
				.setValue(HALF, DoubleBlockHalf.UPPER)
				.setValue(BOTTOM_OFFSET, isOffset);
			world.setBlock(upperPos, upperState, Block.UPDATE_ALL);
		}
	}

	// Fertilizable - grows into big dripleaf
	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			pos = pos.below();
			state = world.getBlockState(pos);
		}
		boolean isOffset = state.getValue(BOTTOM_OFFSET);
		Direction facing = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}[random.nextInt(4)];
		// Clear both halves
		world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		world.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
		// Place slab big dripleaf stem + head
		world.setBlockAndUpdate(pos, DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB.defaultBlockState()
			.setValue(SlabBigDripleafStemBlock.FACING, facing)
			.setValue(BOTTOM_OFFSET, isOffset));
		world.setBlockAndUpdate(pos.above(), DirtSlabBlocks.BIG_DRIPLEAF_SLAB.defaultBlockState()
			.setValue(SlabBigDripleafBlock.FACING, facing)
			.setValue(BOTTOM_OFFSET, isOffset));
	}
}
