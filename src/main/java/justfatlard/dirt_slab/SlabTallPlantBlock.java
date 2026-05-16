package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabTallPlantBlock extends DoublePlantBlock implements OffsetableSlab {
	@SuppressWarnings("unchecked")
	public static final MapCodec<DoublePlantBlock> CODEC = (MapCodec<DoublePlantBlock>)(MapCodec<?>)simpleCodec(SlabTallPlantBlock::new);

	// Normal shape for tall plants
	private static final VoxelShape NORMAL_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.box(2.0, -8.0, 2.0, 14.0, 8.0, 14.0);

	public SlabTallPlantBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(HALF, DoubleBlockHalf.LOWER)
			.setValue(BOTTOM_OFFSET, false));
	}

	@Override
	public MapCodec<DoublePlantBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return NORMAL_SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		return SlabRegistry.isPlantable(floor.getBlock()) || super.mayPlaceOn(floor, world, pos);
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
			BlockPos upperPos = pos.above();
			boolean isOffset = state.getValue(BOTTOM_OFFSET);
			BlockState upperState = this.defaultBlockState()
				.setValue(HALF, DoubleBlockHalf.UPPER)
				.setValue(BOTTOM_OFFSET, isOffset);
			world.setBlock(upperPos, upperState, Block.UPDATE_ALL);
		}
	}
}
