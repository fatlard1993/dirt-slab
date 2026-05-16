package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MudSlab extends SlabBlock {
	// Sinking collision shapes - entities sink 2 pixels into mud
	private static final VoxelShape BOTTOM_COLLISION_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);
	private static final VoxelShape TOP_COLLISION_SHAPE = Block.box(0.0, 8.0, 0.0, 16.0, 14.0, 16.0);
	private static final VoxelShape DOUBLE_COLLISION_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

	public MudSlab(Properties settings) {
		super(settings);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		SlabType type = state.getValue(TYPE);
		return switch (type) {
			case BOTTOM -> BOTTOM_COLLISION_SHAPE;
			case TOP -> TOP_COLLISION_SHAPE;
			case DOUBLE -> DOUBLE_COLLISION_SHAPE;
		};
	}

	@Override
	protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos pos) {
		return Shapes.block();
	}

	@Override
	protected VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
		return 0.2F;
	}
}
