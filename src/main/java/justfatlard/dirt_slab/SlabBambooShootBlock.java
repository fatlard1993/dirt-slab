package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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

public class SlabBambooShootBlock extends Block {
	public static final MapCodec<SlabBambooShootBlock> CODEC = createCodec(SlabBambooShootBlock::new);
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");

	// Same shape as vanilla bamboo shoot
	private static final VoxelShape SHAPE = Block.createCuboidShape(4.0, 0.0, 4.0, 12.0, 12.0, 12.0);
	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(4.0, -8.0, 4.0, 12.0, 4.0, 12.0);

	public SlabBambooShootBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(BOTTOM_OFFSET, false));
	}

	@Override
	public MapCodec<SlabBambooShootBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Vec3d offset = state.getModelOffset(pos);
		if (state.get(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE.offset(offset.x, offset.y, offset.z);
		}
		return SHAPE.offset(offset.x, offset.y, offset.z);
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		return Main.isGrassType(below.getBlock()) && Main.isAnySlab(below.getBlock());
	}

	@Override
	protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (random.nextInt(3) == 0 && world.isAir(pos.up()) && world.getBaseLightLevel(pos.up(), 0) >= 9) {
			// Grow into slab bamboo, inheriting the offset
			growIntoBamboo(world, pos, state.get(BOTTOM_OFFSET));
		}
	}

	private void growIntoBamboo(ServerWorld world, BlockPos pos, boolean bottomOffset) {
		// Replace shoot with slab bamboo
		world.setBlockState(pos, DirtSlabBlocks.BAMBOO_SLAB.getDefaultState()
			.with(SlabBambooBlock.BOTTOM_OFFSET, bottomOffset)
			.with(SlabBambooBlock.AGE, 0)
			.with(SlabBambooBlock.LEAVES, SlabBambooBlock.BambooLeaves.NONE)
			.with(SlabBambooBlock.STAGE, 0), Block.NOTIFY_ALL);
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
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

}
