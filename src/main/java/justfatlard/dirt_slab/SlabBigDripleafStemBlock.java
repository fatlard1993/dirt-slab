package justfatlard.dirt_slab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class SlabBigDripleafStemBlock extends Block implements Waterloggable {
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");
	public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	private static final VoxelShape NORMAL_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(5.0, -8.0, 5.0, 11.0, 8.0, 11.0);

	public SlabBigDripleafStemBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(FACING, Direction.NORTH)
			.with(BOTTOM_OFFSET, false)
			.with(WATERLOGGED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, BOTTOM_OFFSET, WATERLOGGED);
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
		return new ItemStack(Items.BIG_DRIPLEAF);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockPos pos = ctx.getBlockPos();
		Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
		boolean waterlogged = ctx.getWorld().getFluidState(pos).getFluid() == Fluids.WATER;
		boolean bottomOffset = shouldOffset(ctx.getWorld(), pos);
		return this.getDefaultState()
			.with(FACING, facing)
			.with(BOTTOM_OFFSET, bottomOffset)
			.with(WATERLOGGED, waterlogged);
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos belowPos = pos.down();
		BlockState belowState = world.getBlockState(belowPos);
		BlockPos abovePos = pos.up();
		BlockState aboveState = world.getBlockState(abovePos);

		// Can be placed if there's a valid support below and dripleaf/stem above
		boolean validBelow = belowState.isOf(Blocks.BIG_DRIPLEAF_STEM) ||
			   belowState.isOf(DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB) ||
			   Main.isGrassType(belowState.getBlock()) ||
			   belowState.isOf(Blocks.CLAY) ||
			   belowState.isOf(Blocks.MOSS_BLOCK) ||
			   belowState.isSideSolidFullSquare(world, belowPos, Direction.UP);

		boolean validAbove = aboveState.isOf(Blocks.BIG_DRIPLEAF) ||
			   aboveState.isOf(Blocks.BIG_DRIPLEAF_STEM) ||
			   aboveState.isOf(DirtSlabBlocks.BIG_DRIPLEAF_SLAB) ||
			   aboveState.isOf(DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB);

		return validBelow && validAbove;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (Main.isAnySlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		// If on slab stem, inherit the offset
		if (below.getBlock() == DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB) {
			return below.get(BOTTOM_OFFSET);
		}
		return false;
	}
}
