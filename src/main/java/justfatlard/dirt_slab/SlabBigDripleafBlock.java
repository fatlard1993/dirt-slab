package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabBigDripleafBlock extends Block implements SimpleWaterloggedBlock, OffsetableSlab {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<Tilt> TILT = BlockStateProperties.TILT;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final VoxelShape NORMAL_FULL_SHAPE = Block.box(0.0, 11.0, 0.0, 16.0, 15.0, 16.0);
	private static final VoxelShape OFFSET_FULL_SHAPE = Block.box(0.0, 3.0, 0.0, 16.0, 7.0, 16.0);
	private static final VoxelShape TILTED_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

	private static final int TILT_RESET_TICKS = 10;
	private static final int UNSTABLE_TILT_TICKS = 10;
	private static final int PARTIAL_TILT_TICKS = 10;

	public SlabBigDripleafBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(TILT, Tilt.NONE)
			.setValue(BOTTOM_OFFSET, false)
			.setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, TILT, BOTTOM_OFFSET, WATERLOGGED);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Tilt tilt = state.getValue(TILT);
		if (tilt == Tilt.NONE || tilt == Tilt.UNSTABLE) {
			if (state.getValue(BOTTOM_OFFSET)) {
				return OFFSET_FULL_SHAPE;
			}
			return NORMAL_FULL_SHAPE;
		}
		return TILTED_SHAPE;
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
			.setValue(TILT, Tilt.NONE)
			.setValue(BOTTOM_OFFSET, bottomOffset)
			.setValue(WATERLOGGED, waterlogged);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos belowPos = pos.below();
		BlockState belowState = world.getBlockState(belowPos);
		return belowState.is(Blocks.BIG_DRIPLEAF_STEM) ||
			   belowState.is(DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB) ||
			   SlabRegistry.isGrassType(belowState.getBlock()) ||
			   belowState.is(Blocks.CLAY) ||
			   belowState.is(Blocks.MOSS_BLOCK) ||
			   belowState.isFaceSturdy(world, belowPos, Direction.UP);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean moving) {
		if (!world.isClientSide()) {
			if (state.getValue(TILT) == Tilt.NONE && canTilt(entity)) {
				setTilt(state, world, pos, Tilt.UNSTABLE);
			}
		}
	}

	@Override
	protected void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
		if (!world.isClientSide() && state.getValue(TILT) == Tilt.NONE) {
			setTilt(state, world, hit.getBlockPos(), Tilt.FULL);
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		Tilt tilt = state.getValue(TILT);
		if (tilt == Tilt.UNSTABLE) {
			setTilt(state, world, pos, Tilt.PARTIAL);
		} else if (tilt == Tilt.PARTIAL) {
			setTilt(state, world, pos, Tilt.FULL);
		} else if (tilt == Tilt.FULL) {
			resetTilt(state, world, pos);
		}
	}

	private void setTilt(BlockState state, Level world, BlockPos pos, Tilt tilt) {
		world.setBlock(pos, state.setValue(TILT, tilt), Block.UPDATE_CLIENTS);
		if (!tilt.causesVibration()) {
			world.scheduleTick(pos, this, tilt == Tilt.UNSTABLE ? UNSTABLE_TILT_TICKS :
				(tilt == Tilt.PARTIAL ? PARTIAL_TILT_TICKS : TILT_RESET_TICKS));
		}
	}

	private void resetTilt(BlockState state, Level world, BlockPos pos) {
		world.setBlock(pos, state.setValue(TILT, Tilt.NONE), Block.UPDATE_CLIENTS);
	}

	private static boolean canTilt(Entity entity) {
		return !entity.isSuppressingBounce();
	}

	@Override
	public boolean shouldOffset(LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		if (SlabRegistry.isTerrainSlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		if (below.getBlock() == DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB) {
			return below.getValue(BOTTOM_OFFSET);
		}
		return false;
	}
}
