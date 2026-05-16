package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Slab with reduced height (7px per half instead of 8px), matching vanilla farmland and dirt path
 * which are 15/16ths of a block tall. Also handles degradation to dirt when smothered by a solid block above.
 */
public class SlicedTopSlab extends SlabBlock {
	protected static final VoxelShape TOP_SHAPE;
	protected static final VoxelShape BOTTOM_SHAPE;
	protected static final VoxelShape DOUBLE_SHAPE;

	protected SlicedTopSlab(BlockBehaviour.Properties settings){
		super(settings);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context){
		SlabType slabType = (SlabType)state.getValue(TYPE);

		switch(slabType){
			case DOUBLE:
				return DOUBLE_SHAPE;
			case TOP:
				return TOP_SHAPE;
			default:
				return BOTTOM_SHAPE;
		}
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos){
		return canExistAt(state, world, pos);
	}

	public static boolean canExistAt(BlockState state, LevelReader world, BlockPos pos){
		if(state.getBlock() instanceof SlabBlock && state.getValue(TYPE) == SlabType.BOTTOM) return true;

		BlockState upState = world.getBlockState(pos.above());

		return (upState.getBlock() instanceof SlabBlock && upState.getValue(TYPE) == SlabType.TOP)
			|| !upState.isSolid()
			|| upState.getBlock() instanceof FenceGateBlock
			|| upState.getBlock() instanceof MovingPistonBlock;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx){
		return !this.defaultBlockState().canSurvive(ctx.getLevel(), ctx.getClickedPos()) ? pushEntitiesUp(this.defaultBlockState(), DirtSlabBlocks.DIRT_SLAB.defaultBlockState(), ctx.getLevel(), ctx.getClickedPos()) : super.getStateForPlacement(ctx);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random){
		if(direction == Direction.UP && !state.canSurvive(world, pos)) tickView.scheduleTick(pos, this, 1);

		return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random){
		SlabEffects.setToDirt(world, pos);
	}

	static {
		TOP_SHAPE = Block.box(0.0D, 8.0D, 0.0D, 16.0D, 15.0D, 16.0D);
		BOTTOM_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
		DOUBLE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
	}
}
