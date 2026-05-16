package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabPitcherPlantBlock extends DoublePlantBlock implements OffsetableSlab {
	@SuppressWarnings("unchecked")
	public static final MapCodec<DoublePlantBlock> CODEC = (MapCodec<DoublePlantBlock>)(MapCodec<?>)simpleCodec(SlabPitcherPlantBlock::new);

	private static final VoxelShape NORMAL_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape OFFSET_SHAPE = Block.box(0.0, -8.0, 0.0, 16.0, 8.0, 16.0);

	public SlabPitcherPlantBlock(Properties settings) {
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
	public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(Items.PITCHER_PLANT);
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
		return SlabRegistry.isPlantable(floor.getBlock()) ||
			   floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB ||
			   floor.is(Blocks.FARMLAND);
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
