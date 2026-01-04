package justfatlard.dirt_slab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class MudSlab extends SlabBlock {
	// Sinking collision shapes - entities sink 2 pixels into mud
	private static final VoxelShape BOTTOM_COLLISION_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);
	private static final VoxelShape TOP_COLLISION_SHAPE = Block.createCuboidShape(0.0, 8.0, 0.0, 16.0, 14.0, 16.0);
	private static final VoxelShape DOUBLE_COLLISION_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

	public MudSlab(Settings settings) {
		super(settings);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		SlabType type = state.get(TYPE);
		return switch (type) {
			case BOTTOM -> BOTTOM_COLLISION_SHAPE;
			case TOP -> TOP_COLLISION_SHAPE;
			case DOUBLE -> DOUBLE_COLLISION_SHAPE;
		};
	}

	@Override
	protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
		return VoxelShapes.fullCube();
	}

	@Override
	protected VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.fullCube();
	}

	@Override
	protected boolean canPathfindThrough(BlockState state, NavigationType type) {
		return false;
	}

	@Override
	protected float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
		return 0.2F;
	}
}
