package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SlabBambooBlock extends Block implements OffsetableSlab {
	public static final MapCodec<SlabBambooBlock> CODEC = simpleCodec(SlabBambooBlock::new);
	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 1);
	public static final EnumProperty<BambooLeaves> LEAVES = EnumProperty.create("leaves", BambooLeaves.class);
	public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);

	// Shapes for different stages (thin vs thick)
	private static final VoxelShape SMALL_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
	private static final VoxelShape LARGE_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
	// Offset shapes (8 pixels lower)
	private static final VoxelShape SMALL_OFFSET_SHAPE = Block.box(5.0, -8.0, 5.0, 11.0, 8.0, 11.0);
	private static final VoxelShape LARGE_OFFSET_SHAPE = Block.box(3.0, -8.0, 3.0, 13.0, 8.0, 13.0);

	public SlabBambooBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(BOTTOM_OFFSET, false)
			.setValue(AGE, 0)
			.setValue(LEAVES, BambooLeaves.NONE)
			.setValue(STAGE, 0));
	}

	@Override
	public MapCodec<SlabBambooBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET, AGE, LEAVES, STAGE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		VoxelShape shape = state.getValue(AGE) == 1 ? LARGE_SHAPE : SMALL_SHAPE;
		VoxelShape offsetShape = state.getValue(AGE) == 1 ? LARGE_OFFSET_SHAPE : SMALL_OFFSET_SHAPE;
		Vec3 offset = state.getOffset(pos);

		if (state.getValue(BOTTOM_OFFSET)) {
			return offsetShape.move(offset.x, offset.y, offset.z);
		}
		return shape.move(offset.x, offset.y, offset.z);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());

		// Can place on top of another slab bamboo or bamboo shoot
		if (below.getBlock() == this || below.getBlock() == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			return true;
		}

		// Can place on grass-type dirt slabs
		return SlabRegistry.isGrassType(below.getBlock()) && SlabRegistry.isTerrainSlab(below.getBlock());
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		super.setPlacedBy(world, pos, state, placer, itemStack);

		// Convert bamboo shoot below to bamboo when bamboo is placed on top
		BlockPos belowPos = pos.below();
		BlockState belowState = world.getBlockState(belowPos);
		if (belowState.getBlock() == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			boolean bottomOffset = belowState.getValue(BOTTOM_OFFSET);
			world.setBlock(belowPos, this.defaultBlockState()
				.setValue(BOTTOM_OFFSET, bottomOffset)
				.setValue(AGE, 0)
				.setValue(LEAVES, BambooLeaves.NONE)
				.setValue(STAGE, 0), Block.UPDATE_ALL);
		}
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (state.getValue(STAGE) == 0) {
			if (random.nextInt(3) == 0 && world.isEmptyBlock(pos.above()) && world.getRawBrightness(pos.above(), 0) >= 9) {
				int height = getHeightBelow(world, pos) + 1;
				if (height < 16) {
					grow(world, pos, state, height);
				}
			}
		}
	}

	private int getHeightBelow(LevelReader world, BlockPos pos) {
		int height = 0;
		BlockPos checkPos = pos.below();
		while (world.getBlockState(checkPos).getBlock() == this ||
			   world.getBlockState(checkPos).getBlock() == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			height++;
			checkPos = checkPos.below();
		}
		return height;
	}

	private void grow(ServerLevel world, BlockPos pos, BlockState state, int height) {
		BlockState above = world.getBlockState(pos.above());
		boolean bottomOffset = state.getValue(BOTTOM_OFFSET);

		// Update current block's leaves based on height
		BambooLeaves newLeaves = getNewLeaves(height);
		int newAge = height >= 1 ? 1 : 0;

		world.setBlock(pos, state
			.setValue(LEAVES, newLeaves)
			.setValue(AGE, newAge), Block.UPDATE_ALL);

		// Place new bamboo above
		world.setBlock(pos.above(), this.defaultBlockState()
			.setValue(BOTTOM_OFFSET, bottomOffset)
			.setValue(AGE, newAge)
			.setValue(LEAVES, BambooLeaves.NONE)
			.setValue(STAGE, 0), Block.UPDATE_ALL);
	}

	private BambooLeaves getNewLeaves(int height) {
		if (height >= 3) {
			return BambooLeaves.LARGE;
		} else if (height >= 2) {
			return BambooLeaves.SMALL;
		}
		return BambooLeaves.NONE;
	}

	@Override
	public boolean shouldOffset(LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		// If below is slab bamboo or bamboo shoot, inherit offset
		if (below.getBlock() == this) {
			return below.getValue(BOTTOM_OFFSET);
		}
		if (below.getBlock() == DirtSlabBlocks.BAMBOO_SHOOT_SLAB) {
			return below.getValue(BOTTOM_OFFSET);
		}
		// Otherwise check if on bottom slab
		if (SlabRegistry.isTerrainSlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == Direction.DOWN && !this.canSurvive(state, world, pos)) {
			tickView.scheduleTick(pos, this, 1);
		}
		return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (!this.canSurvive(state, world, pos)) {
			world.destroyBlock(pos, true);
		}
	}

	public enum BambooLeaves implements StringRepresentable {
		NONE("none"),
		SMALL("small"),
		LARGE("large");

		private final String name;

		BambooLeaves(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
