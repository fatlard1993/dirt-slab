package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabCaveVinesPlantBlock extends VegetationBlock implements BonemealableBlock, CaveVines {
	public static final MapCodec<SlabCaveVinesPlantBlock> CODEC = simpleCodec(SlabCaveVinesPlantBlock::new);
	public static final BooleanProperty TOP_OFFSET = BooleanProperty.create("top_offset");
	public static final BooleanProperty BERRIES = CaveVines.BERRIES;

	private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
	private static final VoxelShape OFFSET_SHAPE = Block.box(1.0, 8.0, 1.0, 15.0, 24.0, 15.0);

	public SlabCaveVinesPlantBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(TOP_OFFSET, false)
			.setValue(BERRIES, false));
	}

	@Override
	protected MapCodec<? extends VegetationBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TOP_OFFSET, BERRIES);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return state.getValue(TOP_OFFSET) ? OFFSET_SHAPE : SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos pos = ctx.getClickedPos();
		boolean topOffset = shouldOffset(ctx.getLevel(), pos);
		return this.defaultBlockState().setValue(TOP_OFFSET, topOffset);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos ceilingPos = pos.above();
		BlockState ceilingState = world.getBlockState(ceilingPos);
		// Can hang from solid bottom face or another cave vine slab
		if (ceilingState.getBlock() == this ||
			ceilingState.getBlock() == DirtSlabBlocks.CAVE_VINES_SLAB) {
			return true;
		}
		return ceilingState.isFaceSturdy(world, ceilingPos, Direction.DOWN);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (direction == Direction.UP && !canSurvive(state, world, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return CaveVines.use(player, state, world, pos);
	}

	// Fertilizable implementation
	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
		return !state.getValue(BERRIES);
	}

	@Override
	public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
		world.setBlock(pos, state.setValue(BERRIES, true), Block.UPDATE_CLIENTS);
	}

	public boolean shouldOffset(LevelReader world, BlockPos pos) {
		BlockState above = world.getBlockState(pos.above());
		if (above.hasProperty(SlabBlock.TYPE)) {
			return above.getValue(SlabBlock.TYPE) == SlabType.TOP;
		}
		return false;
	}
}
