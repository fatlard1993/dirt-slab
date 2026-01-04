package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CaveVines;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class SlabCaveVinesBlock extends PlantBlock implements Fertilizable, CaveVines {
	public static final MapCodec<SlabCaveVinesBlock> CODEC = createCodec(SlabCaveVinesBlock::new);
	public static final BooleanProperty TOP_OFFSET = BooleanProperty.of("top_offset");
	public static final BooleanProperty BERRIES = CaveVines.BERRIES;

	private static final VoxelShape SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(1.0, 8.0, 1.0, 15.0, 24.0, 15.0);

	public SlabCaveVinesBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(TOP_OFFSET, false)
			.with(BERRIES, false));
	}

	@Override
	protected MapCodec<? extends PlantBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(TOP_OFFSET, BERRIES);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(TOP_OFFSET) ? OFFSET_SHAPE : SHAPE;
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		return false;
	}

	private boolean canHangFrom(BlockState ceiling) {
		Block block = ceiling.getBlock();
		// Can hang from moss slabs or full moss blocks
		if (block == DirtSlabBlocks.MOSS_CARPET_SLAB ||
			block == Blocks.MOSS_BLOCK ||
			block == Blocks.MOSS_CARPET ||
			ceiling.isSideSolidFullSquare((BlockView) null, BlockPos.ORIGIN, Direction.DOWN)) {
			return true;
		}
		// Check for slabs
		if (ceiling.contains(SlabBlock.TYPE)) {
			SlabType type = ceiling.get(SlabBlock.TYPE);
			return type == SlabType.TOP || type == SlabType.DOUBLE;
		}
		return ceiling.isSideSolidFullSquare((BlockView) null, BlockPos.ORIGIN, Direction.DOWN);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos ceilingPos = pos.up();
		BlockState ceilingState = world.getBlockState(ceilingPos);
		// Can hang from solid bottom face or another cave vine slab
		if (ceilingState.getBlock() == this || ceilingState.getBlock() == DirtSlabBlocks.CAVE_VINES_PLANT_SLAB) {
			return true;
		}
		return ceilingState.isSideSolidFullSquare(world, ceilingPos, Direction.DOWN);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
		if (direction == Direction.UP && !canPlaceAt(state, world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		return CaveVines.pickBerries(player, state, world, pos);
	}

	// Fertilizable implementation
	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
		return !state.get(BERRIES);
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		world.setBlockState(pos, state.with(BERRIES, true), Block.NOTIFY_LISTENERS);
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState above = world.getBlockState(pos.up());
		if (above.contains(SlabBlock.TYPE)) {
			return above.get(SlabBlock.TYPE) == SlabType.TOP;
		}
		return false;
	}
}
