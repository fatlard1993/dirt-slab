package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.ShortPlantBlock;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class SlabShortPlantBlock extends ShortPlantBlock implements OffsetableSlab {
	public static final MapCodec<SlabShortPlantBlock> CODEC = createCodec(SlabShortPlantBlock::new);

	// Normal shape (same as vanilla short grass/fern)
	private static final VoxelShape SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);
	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(2.0, -8.0, 2.0, 14.0, 5.0, 14.0);

	public SlabShortPlantBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(BOTTOM_OFFSET, false));
	}

	@Override
	@SuppressWarnings("unchecked")
	public MapCodec<ShortPlantBlock> getCodec() {
		return (MapCodec<ShortPlantBlock>)(MapCodec<?>)CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		return SlabRegistry.isPlantable(floor.getBlock()) || super.canPlantOnTop(floor, world, pos);
	}
}
