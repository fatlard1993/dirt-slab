package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabBambooShootBlock extends Block implements OffsetableSlab {
	public static final MapCodec<SlabBambooShootBlock> CODEC = simpleCodec(SlabBambooShootBlock::new);

	// Same shape as vanilla bamboo shoot
	private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0);
	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.box(4.0, -8.0, 4.0, 12.0, 4.0, 12.0);

	public SlabBambooShootBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(BOTTOM_OFFSET, false));
	}

	@Override
	public MapCodec<SlabBambooShootBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Vec3 offset = state.getOffset(pos);
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE.move(offset.x, offset.y, offset.z);
		}
		return SHAPE.move(offset.x, offset.y, offset.z);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		return SlabRegistry.isGrassType(below.getBlock()) && SlabRegistry.isTerrainSlab(below.getBlock());
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (random.nextInt(3) == 0 && world.isEmptyBlock(pos.above()) && world.getRawBrightness(pos.above(), 0) >= 9) {
			// Grow into slab bamboo, inheriting the offset
			growIntoBamboo(world, pos, state.getValue(BOTTOM_OFFSET));
		}
	}

	private void growIntoBamboo(ServerLevel world, BlockPos pos, boolean bottomOffset) {
		// Replace shoot with slab bamboo
		world.setBlock(pos, DirtSlabBlocks.BAMBOO_SLAB.defaultBlockState()
			.setValue(BOTTOM_OFFSET, bottomOffset)
			.setValue(SlabBambooBlock.AGE, 0)
			.setValue(SlabBambooBlock.LEAVES, SlabBambooBlock.BambooLeaves.NONE)
			.setValue(SlabBambooBlock.STAGE, 0), Block.UPDATE_ALL);
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

}
