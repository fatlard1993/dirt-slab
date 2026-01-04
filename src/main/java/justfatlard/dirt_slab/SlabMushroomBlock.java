package justfatlard.dirt_slab;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class SlabMushroomBlock extends PlantBlock {
	public static final MapCodec<SlabMushroomBlock> CODEC = createCodec(SlabMushroomBlock::new);
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");

	// Normal shape (same as vanilla mushroom)
	private static final VoxelShape SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 6.0, 11.0);
	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(5.0, -8.0, 5.0, 11.0, -2.0, 11.0);

	public SlabMushroomBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(BOTTOM_OFFSET, false));
	}

	@Override
	protected MapCodec<? extends PlantBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(BOTTOM_OFFSET);
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
		// Allow on mycelium, podzol slabs, and other dirt slabs
		return floor.getBlock() == DirtSlabBlocks.MYCELIUM_SLAB ||
			   floor.getBlock() == DirtSlabBlocks.PODZOL_SLAB ||
			   Main.isAnySlab(floor.getBlock());
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos groundPos = pos.down();
		BlockState groundState = world.getBlockState(groundPos);
		Block groundBlock = groundState.getBlock();

		// Can always place on mycelium and podzol slabs
		if (groundBlock == DirtSlabBlocks.MYCELIUM_SLAB || groundBlock == DirtSlabBlocks.PODZOL_SLAB) {
			return true;
		}

		// Can place on other dirt slabs in low light
		if (Main.isAnySlab(groundBlock) && world.getBaseLightLevel(pos, 0) < 13) {
			return true;
		}

		return false;
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (Main.isAnySlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
