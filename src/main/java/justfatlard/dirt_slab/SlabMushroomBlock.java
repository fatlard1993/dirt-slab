package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabMushroomBlock extends VegetationBlock implements OffsetableSlab {
	public static final MapCodec<SlabMushroomBlock> CODEC = simpleCodec(SlabMushroomBlock::new);

	// Normal shape (same as vanilla mushroom)
	private static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);
	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.box(5.0, -8.0, 5.0, 11.0, -2.0, 11.0);

	public SlabMushroomBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(BOTTOM_OFFSET, false));
	}

	@Override
	protected MapCodec<? extends VegetationBlock> codec() {
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
	protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
		return SlabRegistry.isTerrainSlab(floor.getBlock());
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos groundPos = pos.below();
		BlockState groundState = world.getBlockState(groundPos);
		Block groundBlock = groundState.getBlock();

		// Can always place on mycelium and podzol slabs
		if (groundBlock == DirtSlabBlocks.MYCELIUM_SLAB || groundBlock == DirtSlabBlocks.PODZOL_SLAB) {
			return true;
		}

		// Can place on other dirt slabs in low light
		if (SlabRegistry.isTerrainSlab(groundBlock) && world.getRawBrightness(pos, 0) < 13) {
			return true;
		}

		return false;
	}
}
