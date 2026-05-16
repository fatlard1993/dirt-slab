package justfatlard.dirt_slab;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Shared interface for slab plant blocks that render offset on bottom slabs.
 *
 * Blocks placed on the surface of a bottom slab need to render 8 pixels lower
 * than normal. This is tracked by a BOTTOM_OFFSET block state property.
 * Custom slab blocks use this property directly; vanilla blocks (sugar cane,
 * bamboo) use BlockRenderManagerMixin for render-time translation instead.
 */
public interface OffsetableSlab {
	BooleanProperty BOTTOM_OFFSET = BooleanProperty.create("bottom_offset");

	default boolean shouldOffset(LevelReader world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.below());
		if(SlabRegistry.isTerrainSlab(below.getBlock()) && below.getBlock() instanceof SlabBlock){
			return below.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
