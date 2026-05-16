package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabAttachedStemBlock extends VegetationBlock implements OffsetableSlab {
	public static final MapCodec<SlabAttachedStemBlock> CODEC = simpleCodec(SlabAttachedStemBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	// Normal shape (same as vanilla attached stem)
	private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.box(0.0, -8.0, 0.0, 16.0, 8.0, 16.0);

	private final ItemLike pickBlockItem;
	private final boolean isMelon;

	public SlabAttachedStemBlock(boolean isMelon, ItemLike pickBlockItem, Properties settings) {
		super(settings);
		this.isMelon = isMelon;
		this.pickBlockItem = pickBlockItem;
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACING, Direction.NORTH)
			.setValue(BOTTOM_OFFSET, false));
	}

	public SlabAttachedStemBlock(Properties settings) {
		this(true, null, settings);
	}

	@Override
	protected MapCodec<? extends VegetationBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		return floor.getBlock() == DirtSlabBlocks.FARMLAND_SLAB;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos below = pos.below();
		BlockState floorState = world.getBlockState(below);
		return mayPlaceOn(floorState, world, below);
	}

	@Override
	public boolean shouldOffset(LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		if (below.getBlock() == DirtSlabBlocks.FARMLAND_SLAB) {
			return below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	public boolean isMelon() {
		return isMelon;
	}
}
