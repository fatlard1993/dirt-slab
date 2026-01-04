package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import org.jetbrains.annotations.Nullable;

public class SlabBambooBlock extends Block {
	public static final MapCodec<SlabBambooBlock> CODEC = createCodec(SlabBambooBlock::new);
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");
	public static final IntProperty AGE = IntProperty.of("age", 0, 1);
	public static final EnumProperty<BambooLeaves> LEAVES = EnumProperty.of("leaves", BambooLeaves.class);
	public static final IntProperty STAGE = IntProperty.of("stage", 0, 1);

	// Shapes for different stages (thin vs thick)
	private static final VoxelShape SMALL_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
	private static final VoxelShape LARGE_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
	// Offset shapes (8 pixels lower)
	private static final VoxelShape SMALL_OFFSET_SHAPE = Block.createCuboidShape(5.0, -8.0, 5.0, 11.0, 8.0, 11.0);
	private static final VoxelShape LARGE_OFFSET_SHAPE = Block.createCuboidShape(3.0, -8.0, 3.0, 13.0, 8.0, 13.0);

	public SlabBambooBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(BOTTOM_OFFSET, false)
			.with(AGE, 0)
			.with(LEAVES, BambooLeaves.NONE)
			.with(STAGE, 0));
	}

	@Override
	public MapCodec<SlabBambooBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET, AGE, LEAVES, STAGE);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		VoxelShape shape = state.get(AGE) == 1 ? LARGE_SHAPE : SMALL_SHAPE;
		VoxelShape offsetShape = state.get(AGE) == 1 ? LARGE_OFFSET_SHAPE : SMALL_OFFSET_SHAPE;
		Vec3d offset = state.getModelOffset(pos);

		if (state.get(BOTTOM_OFFSET)) {
			return offsetShape.offset(offset.x, offset.y, offset.z);
		}
		return shape.offset(offset.x, offset.y, offset.z);
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());

		// Can place on top of another slab bamboo or bamboo shoot
		if (below.getBlock() == this || below.getBlock() == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			return true;
		}

		// Can place on grass-type dirt slabs
		return Main.isGrassType(below.getBlock()) && Main.isAnySlab(below.getBlock());
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);

		// Convert bamboo shoot below to bamboo when bamboo is placed on top
		BlockPos belowPos = pos.down();
		BlockState belowState = world.getBlockState(belowPos);
		if (belowState.getBlock() == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			boolean bottomOffset = belowState.get(SlabBambooShootBlock.BOTTOM_OFFSET);
			world.setBlockState(belowPos, this.getDefaultState()
				.with(BOTTOM_OFFSET, bottomOffset)
				.with(AGE, 0)
				.with(LEAVES, BambooLeaves.NONE)
				.with(STAGE, 0), Block.NOTIFY_ALL);
		}
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (state.get(STAGE) == 0) {
			if (random.nextInt(3) == 0 && world.isAir(pos.up()) && world.getBaseLightLevel(pos.up(), 0) >= 9) {
				int height = getHeightBelow(world, pos) + 1;
				if (height < 16) {
					grow(world, pos, state, height);
				}
			}
		}
	}

	private int getHeightBelow(WorldView world, BlockPos pos) {
		int height = 0;
		BlockPos checkPos = pos.down();
		while (world.getBlockState(checkPos).getBlock() == this ||
			   world.getBlockState(checkPos).getBlock() == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			height++;
			checkPos = checkPos.down();
		}
		return height;
	}

	private void grow(ServerWorld world, BlockPos pos, BlockState state, int height) {
		BlockState above = world.getBlockState(pos.up());
		boolean bottomOffset = state.get(BOTTOM_OFFSET);

		// Update current block's leaves based on height
		BambooLeaves newLeaves = getNewLeaves(height);
		int newAge = height >= 1 ? 1 : 0;

		world.setBlockState(pos, state
			.with(LEAVES, newLeaves)
			.with(AGE, newAge), Block.NOTIFY_ALL);

		// Place new bamboo above
		world.setBlockState(pos.up(), this.getDefaultState()
			.with(BOTTOM_OFFSET, bottomOffset)
			.with(AGE, newAge)
			.with(LEAVES, BambooLeaves.NONE)
			.with(STAGE, 0), Block.NOTIFY_ALL);
	}

	private BambooLeaves getNewLeaves(int height) {
		if (height >= 3) {
			return BambooLeaves.LARGE;
		} else if (height >= 2) {
			return BambooLeaves.SMALL;
		}
		return BambooLeaves.NONE;
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		// If below is slab bamboo or bamboo shoot, inherit offset
		if (below.getBlock() == this) {
			return below.get(BOTTOM_OFFSET);
		}
		if (below.getBlock() == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			return below.get(SlabBambooShootBlock.BOTTOM_OFFSET);
		}
		// Otherwise check if on bottom slab
		if (Main.isAnySlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
		if (direction == Direction.DOWN && !this.canPlaceAt(state, world, pos)) {
			tickView.scheduleBlockTick(pos, this, 1);
		}
		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (!this.canPlaceAt(state, world, pos)) {
			world.breakBlock(pos, true);
		}
	}

	public enum BambooLeaves implements StringIdentifiable {
		NONE("none"),
		SMALL("small"),
		LARGE("large");

		private final String name;

		BambooLeaves(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}
	}
}
