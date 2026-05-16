package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabBigDripleafStemBlock extends Block implements SimpleWaterloggedBlock, OffsetableSlab {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final VoxelShape NORMAL_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
	private static final VoxelShape OFFSET_SHAPE = Block.box(5.0, -8.0, 5.0, 11.0, 8.0, 11.0);

	public SlabBigDripleafStemBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(BOTTOM_OFFSET, false)
			.setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, BOTTOM_OFFSET, WATERLOGGED);
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
		return new ItemStack(Items.BIG_DRIPLEAF);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos pos = ctx.getClickedPos();
		Direction facing = ctx.getHorizontalDirection().getOpposite();
		boolean waterlogged = ctx.getLevel().getFluidState(pos).getType() == Fluids.WATER;
		boolean bottomOffset = shouldOffset(ctx.getLevel(), pos);
		return this.defaultBlockState()
			.setValue(FACING, facing)
			.setValue(BOTTOM_OFFSET, bottomOffset)
			.setValue(WATERLOGGED, waterlogged);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos belowPos = pos.below();
		BlockState belowState = world.getBlockState(belowPos);
		BlockPos abovePos = pos.above();
		BlockState aboveState = world.getBlockState(abovePos);

		// Can be placed if there's a valid support below and dripleaf/stem above
		boolean validBelow = belowState.is(Blocks.BIG_DRIPLEAF_STEM) ||
			   belowState.is(DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB) ||
			   SlabRegistry.isGrassType(belowState.getBlock()) ||
			   belowState.is(Blocks.CLAY) ||
			   belowState.is(Blocks.MOSS_BLOCK) ||
			   belowState.isFaceSturdy(world, belowPos, Direction.UP);

		boolean validAbove = aboveState.is(Blocks.BIG_DRIPLEAF) ||
			   aboveState.is(Blocks.BIG_DRIPLEAF_STEM) ||
			   aboveState.is(DirtSlabBlocks.BIG_DRIPLEAF_SLAB) ||
			   aboveState.is(DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB);

		return validBelow && validAbove;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public boolean shouldOffset(LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		if (SlabRegistry.isTerrainSlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		// If on slab stem, inherit the offset
		if (below.getBlock() == DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB) {
			return below.getValue(BOTTOM_OFFSET);
		}
		return false;
	}
}
