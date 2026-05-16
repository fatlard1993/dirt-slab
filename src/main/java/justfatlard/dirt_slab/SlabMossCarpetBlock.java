package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabMossCarpetBlock extends CarpetBlock implements OffsetableSlab {
	public static final MapCodec<SlabMossCarpetBlock> CODEC = simpleCodec(SlabMossCarpetBlock::new);

	// Normal shape (carpet is 1 pixel tall)
	private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.box(0.0, -8.0, 0.0, 16.0, -7.0, 16.0);

	public SlabMossCarpetBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(BOTTOM_OFFSET, false));
	}

	@Override
	public MapCodec<? extends CarpetBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(BOTTOM_OFFSET)) {
			return OFFSET_SHAPE;
		}
		return SHAPE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos belowPos = ctx.getClickedPos().below();
		BlockState belowState = ctx.getLevel().getBlockState(belowPos);
		boolean bottomOffset = shouldOffset(belowState);
		return this.defaultBlockState().setValue(BOTTOM_OFFSET, bottomOffset);
	}

	public static boolean shouldOffset(BlockState below) {
		if (SlabRegistry.isTerrainSlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos belowPos = pos.below();
		BlockState belowState = world.getBlockState(belowPos);
		// Can place on grass-type slabs or vanilla placement
		return SlabRegistry.isTerrainSlab(belowState.getBlock()) || super.canSurvive(state, world, pos);
	}
}
