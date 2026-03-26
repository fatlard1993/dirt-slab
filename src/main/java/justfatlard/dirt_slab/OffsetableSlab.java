package justfatlard.dirt_slab;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

/**
 * Shared interface for slab plant blocks that render offset on bottom slabs.
 *
 * Blocks placed on the surface of a bottom slab need to render 8 pixels lower
 * than normal. This is tracked by a BOTTOM_OFFSET block state property.
 * Custom slab blocks use this property directly; vanilla blocks (sugar cane,
 * bamboo) use BlockRenderManagerMixin for render-time translation instead.
 */
public interface OffsetableSlab {
	BooleanProperty BOTTOM_OFFSET = BooleanProperty.of("bottom_offset");

	default boolean shouldOffset(WorldView world, BlockPos pos) {
		BlockState below = world.getBlockState(pos.down());
		if(SlabRegistry.isTerrainSlab(below.getBlock()) && below.getBlock() instanceof SlabBlock){
			return below.get(SlabBlock.TYPE) == SlabType.BOTTOM;
		}
		return false;
	}
}
