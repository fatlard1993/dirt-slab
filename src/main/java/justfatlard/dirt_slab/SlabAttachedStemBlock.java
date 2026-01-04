package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemConvertible;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class SlabAttachedStemBlock extends PlantBlock {
	public static final MapCodec<SlabAttachedStemBlock> CODEC = createCodec(SlabAttachedStemBlock::new);
	public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");

	// Normal shape (same as vanilla attached stem)
	private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(0.0, -8.0, 0.0, 16.0, 8.0, 16.0);

	private final ItemConvertible pickBlockItem;
	private final boolean isMelon;

	public SlabAttachedStemBlock(boolean isMelon, ItemConvertible pickBlockItem, Settings settings) {
		super(settings);
		this.isMelon = isMelon;
		this.pickBlockItem = pickBlockItem;
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(FACING, Direction.NORTH)
			.with(BOTTOM_OFFSET, false));
	}

	public SlabAttachedStemBlock(Settings settings) {
		this(true, null, settings);
	}

	@Override
	protected MapCodec<? extends PlantBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, BOTTOM_OFFSET);
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
		return floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB;
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos below = pos.down();
		BlockState floorState = world.getBlockState(below);
		return canPlantOnTop(floorState, world, below);
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (below.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	public boolean isMelon() {
		return isMelon;
	}
}
