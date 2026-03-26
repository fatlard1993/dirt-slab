package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SlabTallPlantBlock extends TallPlantBlock implements OffsetableSlab {
	@SuppressWarnings("unchecked")
	public static final MapCodec<TallPlantBlock> CODEC = (MapCodec<TallPlantBlock>)(MapCodec<?>)createCodec(SlabTallPlantBlock::new);

	// Normal shape for tall plants
	private static final VoxelShape NORMAL_SHAPE = Block.createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(2.0, -8.0, 2.0, 14.0, 8.0, 14.0);

	public SlabTallPlantBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(HALF, DoubleBlockHalf.LOWER)
			.with(BOTTOM_OFFSET, false));
	}

	@Override
	public MapCodec<TallPlantBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(HALF, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return NORMAL_SHAPE;
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		return SlabRegistry.isPlantable(floor.getBlock()) || super.canPlantOnTop(floor, world, pos);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		if (state.get(HALF) == DoubleBlockHalf.LOWER) {
			BlockPos upperPos = pos.up();
			boolean isOffset = state.get(BOTTOM_OFFSET);
			BlockState upperState = this.getDefaultState()
				.with(HALF, DoubleBlockHalf.UPPER)
				.with(BOTTOM_OFFSET, isOffset);
			world.setBlockState(upperPos, upperState, Block.NOTIFY_ALL);
		}
	}
}
