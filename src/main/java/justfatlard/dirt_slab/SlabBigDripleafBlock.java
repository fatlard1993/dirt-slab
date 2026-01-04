package justfatlard.dirt_slab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.Tilt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class SlabBigDripleafBlock extends Block implements Waterloggable {
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");
	public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
	public static final EnumProperty<Tilt> TILT = Properties.TILT;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	private static final VoxelShape NORMAL_FULL_SHAPE = Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 15.0, 16.0);
	private static final VoxelShape OFFSET_FULL_SHAPE = Block.createCuboidShape(0.0, 3.0, 0.0, 16.0, 7.0, 16.0);
	private static final VoxelShape TILTED_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

	private static final int TILT_RESET_TICKS = 10;
	private static final int UNSTABLE_TILT_TICKS = 10;
	private static final int PARTIAL_TILT_TICKS = 10;

	public SlabBigDripleafBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(FACING, Direction.NORTH)
			.with(TILT, Tilt.NONE)
			.with(BOTTOM_OFFSET, false)
			.with(WATERLOGGED, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, TILT, BOTTOM_OFFSET, WATERLOGGED);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Tilt tilt = state.get(TILT);
		if (tilt == Tilt.NONE || tilt == Tilt.UNSTABLE) {
			if (state.get(BOTTOM_OFFSET)) {
				return OFFSET_FULL_SHAPE;
			}
			return NORMAL_FULL_SHAPE;
		}
		return TILTED_SHAPE;
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
			.with(TILT, Tilt.NONE)
			.with(BOTTOM_OFFSET, bottomOffset)
			.with(WATERLOGGED, waterlogged);
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos belowPos = pos.down();
		BlockState belowState = world.getBlockState(belowPos);
		return belowState.isOf(Blocks.BIG_DRIPLEAF_STEM) ||
			   belowState.isOf(DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB) ||
			   Main.isGrassType(belowState.getBlock()) ||
			   belowState.isOf(Blocks.CLAY) ||
			   belowState.isOf(Blocks.MOSS_BLOCK) ||
			   belowState.isSideSolidFullSquare(world, belowPos, Direction.UP);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean moving) {
		if (!world.isClient()) {
			if (state.get(TILT) == Tilt.NONE && canTilt(entity)) {
				setTilt(state, world, pos, Tilt.UNSTABLE);
			}
		}
	}

	@Override
	protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
		if (!world.isClient() && state.get(TILT) == Tilt.NONE) {
			setTilt(state, world, hit.getBlockPos(), Tilt.FULL);
		}
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		Tilt tilt = state.get(TILT);
		if (tilt == Tilt.UNSTABLE) {
			setTilt(state, world, pos, Tilt.PARTIAL);
		} else if (tilt == Tilt.PARTIAL) {
			setTilt(state, world, pos, Tilt.FULL);
		} else if (tilt == Tilt.FULL) {
			resetTilt(state, world, pos);
		}
	}

	private void setTilt(BlockState state, World world, BlockPos pos, Tilt tilt) {
		world.setBlockState(pos, state.with(TILT, tilt), Block.NOTIFY_LISTENERS);
		if (tilt.isStable()) {
			world.scheduleBlockTick(pos, this, tilt == Tilt.UNSTABLE ? UNSTABLE_TILT_TICKS :
				(tilt == Tilt.PARTIAL ? PARTIAL_TILT_TICKS : TILT_RESET_TICKS));
		}
	}

	private void resetTilt(BlockState state, World world, BlockPos pos) {
		world.setBlockState(pos, state.with(TILT, Tilt.NONE), Block.NOTIFY_LISTENERS);
	}

	private static boolean canTilt(Entity entity) {
		return !entity.bypassesLandingEffects();
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (Main.isAnySlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		if (below.getBlock() == DirtSlabBlocks.BIG_DRIPLEAF_STEM_SLAB) {
			return below.get(SlabBigDripleafStemBlock.BOTTOM_OFFSET);
		}
		return false;
	}
}
