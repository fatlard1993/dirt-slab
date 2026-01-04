package justfatlard.dirt_slab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.block.SlabBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class SlicedTopSlab extends SlabBlock {
	protected static final VoxelShape TOP_SHAPE;
	protected static final VoxelShape BOTTOM_SHAPE;
	protected static final VoxelShape DOUBLE_SHAPE;

	protected SlicedTopSlab(Block.Settings settings){
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context){
		SlabType slabType = (SlabType)state.get(TYPE);

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
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos){
		return canExistAt(state, world, pos);
	}

	public static boolean canExistAt(BlockState state, WorldView world, BlockPos pos){
		BlockState upState = world.getBlockState(pos.up());

		return ((state.getBlock() instanceof SlabBlock && state.get(TYPE) == SlabType.BOTTOM) || (upState.getBlock() instanceof SlabBlock && upState.get(TYPE) == SlabType.TOP) || !upState.isSolid() || upState.getBlock() instanceof FenceGateBlock || upState.getBlock() instanceof PistonExtensionBlock);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx){
		return !this.getDefaultState().canPlaceAt(ctx.getWorld(), ctx.getBlockPos()) ? pushEntitiesUpBeforeBlockChange(this.getDefaultState(), DirtSlabBlocks.DIRT_SLAB.getDefaultState(), ctx.getWorld(), ctx.getBlockPos()) : super.getPlacementState(ctx);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random){
		if(direction == Direction.UP && !state.canPlaceAt(world, pos)) tickView.scheduleBlockTick(pos, this, 1);

		return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
		Main.setToDirt(world, pos);
	}

	static {
		TOP_SHAPE = Block.createCuboidShape(0.0D, 8.0D, 0.0D, 16.0D, 15.0D, 16.0D);
		BOTTOM_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
		DOUBLE_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
	}
}
