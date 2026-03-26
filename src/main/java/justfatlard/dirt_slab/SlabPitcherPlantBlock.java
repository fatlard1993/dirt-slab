package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SlabPitcherPlantBlock extends TallPlantBlock implements OffsetableSlab {
	@SuppressWarnings("unchecked")
	public static final MapCodec<TallPlantBlock> CODEC = (MapCodec<TallPlantBlock>)(MapCodec<?>)createCodec(SlabPitcherPlantBlock::new);

	private static final VoxelShape NORMAL_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, 8.0, 16.0);

	public SlabPitcherPlantBlock(Settings settings) {
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
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(Items.PITCHER_PLANT);
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
		return SlabRegistry.isPlantable(floor.getBlock()) ||
			   floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB ||
			   floor.isOf(Blocks.FARMLAND);
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
