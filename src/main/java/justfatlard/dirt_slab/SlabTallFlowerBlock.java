package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.TallFlowerBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SlabTallFlowerBlock extends TallFlowerBlock {
	@SuppressWarnings("unchecked")
	public static final MapCodec<TallFlowerBlock> CODEC = (MapCodec<TallFlowerBlock>)(MapCodec<?>)createCodec(SlabTallFlowerBlock::new);
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");

	// Normal shape for tall flowers
	private static final VoxelShape NORMAL_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

	// Offset shape for bottom slab placement (8 pixels lower) - only applies to LOWER half
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, 8.0, 16.0);

	public SlabTallFlowerBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(HALF, DoubleBlockHalf.LOWER)
			.with(BOTTOM_OFFSET, false));
	}

	@Override
	public MapCodec<TallFlowerBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(HALF, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		// Both halves get offset when on a bottom slab (to stay connected)
		if (state.get(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return NORMAL_SHAPE;
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		return Main.isGrassType(floor.getBlock()) ||
			   floor.getBlock() == DirtSlabBlocks.MUD_SLAB ||
			   floor.getBlock() == DirtSlabBlocks.ROOTED_DIRT_SLAB;
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		// Only place the upper half if we're placing the lower half
		if (state.get(HALF) == DoubleBlockHalf.LOWER) {
			BlockPos upperPos = pos.up();
			// Upper half also gets offset if lower half is offset (to stay connected)
			boolean isOffset = state.get(BOTTOM_OFFSET);
			BlockState upperState = this.getDefaultState()
				.with(HALF, DoubleBlockHalf.UPPER)
				.with(BOTTOM_OFFSET, isOffset);
			world.setBlockState(upperPos, upperState, Block.NOTIFY_ALL);
		}
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (Main.isAnySlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
