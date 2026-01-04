package justfatlard.dirt_slab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class RootedDirtSlab extends SlabBlock implements Fertilizable {
	public RootedDirtSlab(Settings settings) {
		super(settings);
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
		// Can only grow hanging roots from TOP or DOUBLE slabs (roots hang from underside)
		// BOTTOM slabs have nothing to hang from
		SlabType type = state.get(TYPE);
		if (type == SlabType.BOTTOM) {
			return false; // Bottom slab has no ceiling to hang roots from
		}
		return world.getBlockState(pos.down()).isAir();
	}

	@Override
	public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
		SlabType type = state.get(TYPE);
		// TOP slabs need offset rendering, DOUBLE slabs don't
		boolean needsOffset = type == SlabType.TOP;
		world.setBlockState(pos.down(), DirtSlabBlocks.HANGING_ROOTS_SLAB.getDefaultState()
			.with(SlabHangingRootsBlock.TOP_OFFSET, needsOffset));
	}

	@Override
	public BlockPos getFertilizeParticlePos(BlockPos pos) {
		return pos.down();
	}
}
