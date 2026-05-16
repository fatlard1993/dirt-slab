package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public class RootedDirtSlab extends SlabBlock implements BonemealableBlock {
	public RootedDirtSlab(Properties settings) {
		super(settings);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
		// Can only grow hanging roots from TOP or DOUBLE slabs (roots hang from underside)
		// BOTTOM slabs have nothing to hang from
		SlabType type = state.getValue(TYPE);
		if (type == SlabType.BOTTOM) {
			return false; // Bottom slab has no ceiling to hang roots from
		}
		return world.getBlockState(pos.below()).isAir();
	}

	@Override
	public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
		SlabType type = state.getValue(TYPE);
		// TOP slabs need offset rendering, DOUBLE slabs don't
		boolean needsOffset = type == SlabType.TOP;
		world.setBlockAndUpdate(pos.below(), DirtSlabBlocks.HANGING_ROOTS_SLAB.defaultBlockState()
			.setValue(SlabHangingRootsBlock.TOP_OFFSET, needsOffset));
	}

	@Override
	public BlockPos getParticlePos(BlockPos pos) {
		return pos.below();
	}
}
