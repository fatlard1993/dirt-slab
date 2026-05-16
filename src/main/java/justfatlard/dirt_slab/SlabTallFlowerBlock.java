package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabTallFlowerBlock extends TallFlowerBlock implements OffsetableSlab {
	@SuppressWarnings("unchecked")
	public static final MapCodec<TallFlowerBlock> CODEC = (MapCodec<TallFlowerBlock>)(MapCodec<?>)simpleCodec(SlabTallFlowerBlock::new);

	// Normal shape for tall flowers
	private static final VoxelShape NORMAL_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

	// Offset shape for bottom slab placement (8 pixels lower) - only applies to LOWER half
	private static final VoxelShape OFFSET_SHAPE = Block.box(0.0, -8.0, 0.0, 16.0, 8.0, 16.0);

	public SlabTallFlowerBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(HALF, DoubleBlockHalf.LOWER)
			.setValue(BOTTOM_OFFSET, false));
	}

	@Override
	public MapCodec<TallFlowerBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		// Both halves get offset when on a bottom slab (to stay connected)
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return NORMAL_SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		return SlabRegistry.isPlantable(floor.getBlock());
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		// Only place the upper half if we're placing the lower half
		if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
			BlockPos upperPos = pos.above();
			// Upper half also gets offset if lower half is offset (to stay connected)
			boolean isOffset = state.getValue(BOTTOM_OFFSET);
			BlockState upperState = this.defaultBlockState()
				.setValue(HALF, DoubleBlockHalf.UPPER)
				.setValue(BOTTOM_OFFSET, isOffset);
			world.setBlock(upperPos, upperState, Block.UPDATE_ALL);
		}
	}
}
