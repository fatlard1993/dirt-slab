package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SlabSmallDripleafBlock extends TallPlantBlock implements Fertilizable, OffsetableSlab {
	@SuppressWarnings("unchecked")
	public static final MapCodec<TallPlantBlock> CODEC = (MapCodec<TallPlantBlock>)(MapCodec<?>)createCodec(SlabSmallDripleafBlock::new);

	private static final VoxelShape NORMAL_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(2.0, -8.0, 2.0, 14.0, 8.0, 14.0);

	public SlabSmallDripleafBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(HALF, DoubleBlockHalf.LOWER)
			.with(BOTTOM_OFFSET, false));
	}

	@Override
	public MapCodec<TallPlantBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(HALF, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return NORMAL_SHAPE;
	}

	@Override
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(Items.SMALL_DRIPLEAF);
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		// Small dripleaf can grow on clay, moss, mud, and dirt-type slabs
		return floor.isOf(Blocks.CLAY) ||
			   floor.isOf(Blocks.MOSS_BLOCK) ||
			   floor.isOf(Blocks.MUD) ||
			   SlabRegistry.isPlantable(floor.getBlock());
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		if (state.get(HALF) == DoubleBlockHalf.LOWER) {
			BlockPos upperPos = pos.up();
			boolean isOffset = state.get(BOTTOM_OFFSET);
			BlockState upperState = this.getDefaultState()
				.with(HALF, DoubleBlockHalf.UPPER)
				.with(BOTTOM_OFFSET, isOffset);
			world.setBlockState(upperPos, upperState, Block.NOTIFY_ALL);
		}
	}

	// Fertilizable - grows into big dripleaf
	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		if (state.get(HALF) == DoubleBlockHalf.UPPER) {
			pos = pos.down();
			state = world.getBlockState(pos);
		}
		boolean isOffset = state.get(BOTTOM_OFFSET);
		Direction facing = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}[random.nextInt(4)];
		// Clear both halves
		world.setBlockState(pos, Blocks.AIR.getDefaultState());
		world.setBlockState(pos.up(), Blocks.AIR.getDefaultState());
		// Place slab big dripleaf stem + head
		world.setBlockState(pos, DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB.getDefaultState()
			.with(SlabBigDripleafStemBlock.FACING, facing)
			.with(BOTTOM_OFFSET, isOffset));
		world.setBlockState(pos.up(), DirtSlabBlocks.BIG_DRIPLEAF_SLAB.getDefaultState()
			.with(SlabBigDripleafBlock.FACING, facing)
			.with(BOTTOM_OFFSET, isOffset));
	}
}
