package justfatlard.dirt_slab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

public class SlabFlowerBlock extends FlowerBlock {
	public static final BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");

	// Offset shape for bottom slab placement (8 pixels lower)
	private static final VoxelShape OFFSET_SHAPE = Block.createCuboidShape(5.0, -8.0, 5.0, 11.0, 2.0, 11.0);

	public SlabFlowerBlock(RegistryEntry<StatusEffect> suspiciousStewEffect, float effectLengthInSeconds, Settings settings) {
		super(suspiciousStewEffect, effectLengthInSeconds, settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(BOTTOM_OFFSET, false));
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
		return super.getOutlineShape(state, world, pos, context);
	}

	@Override
	protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
		return Main.isGrassType(floor.getBlock()) ||
			   floor.getBlock() == DirtSlabBlocks.MUD_SLAB ||
			   floor.getBlock() == DirtSlabBlocks.ROOTED_DIRT_SLAB;
	}

	public boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if (Main.isAnySlab(below.getBlock()) && below.getBlock() instanceof SlabBlock) {
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
