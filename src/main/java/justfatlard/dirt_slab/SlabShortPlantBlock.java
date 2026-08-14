package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabShortPlantBlock extends TallGrassBlock implements OffsetableSlab {

	// Normal shape (same as vanilla short grass/fern)
	private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);
	private static final VoxelShape OFFSET_SHAPE = Block.box(2.0, -8.0, 2.0, 14.0, 5.0, 14.0);

	public SlabShortPlantBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(BOTTOM_OFFSET, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		return SlabRegistry.isPlantable(floor.getBlock()) || super.mayPlaceOn(floor, world, pos);
	}
}
