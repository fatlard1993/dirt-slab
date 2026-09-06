package justfatlard.dirt_slab;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Cane and bamboo standing on a slab, made the slab kind all the way up.
 *
 * <p>A stalk grows a block at a time and the game's own growth only knows its own blocks, so a
 * column can end up mixed: vanilla cane grown on a slab before anything lowered it, a shoot that
 * came up as vanilla bamboo, a stalk set by a dispenser. Lowering only the bottom piece to the
 * slab's height, which is what happened, left it half a block clear of the rest. The whole column
 * is settled together, bottom up, each piece the slab kind with the same offset, so it stands as
 * one stalk at the slab's height.
 */
public final class PlantColumns {
	private PlantColumns() {}

	/** Whether this is a piece of one of the stalks that grow in columns, either kind. */
	public static boolean isStalk(BlockState state) {
		Block block = state.getBlock();
		return block == Blocks.SUGAR_CANE || block == Blocks.BAMBOO || block == Blocks.BAMBOO_SAPLING
			|| block == DirtSlabBlocks.SUGAR_CANE_SLAB || block == DirtSlabBlocks.BAMBOO_SLAB
			|| block == DirtSlabBlocks.BAMBOO_SHOOT_SLAB;
	}

	/**
	 * The column this piece belongs to settled onto its slab, if it stands on one. True when it
	 * did, in which case whatever the caller was about to do with the vanilla piece is moot.
	 */
	public static boolean settle(ServerLevel world, BlockPos pos) {
		if (!isStalk(world.getBlockState(pos))) return false;
		BlockPos base = pos;
		while (isStalk(world.getBlockState(base.below()))) base = base.below();

		BlockState bottom = world.getBlockState(base);
		Boolean offset = null;
		if (bottom.hasProperty(OffsetableSlab.BOTTOM_OFFSET)) {
			offset = bottom.getValue(OffsetableSlab.BOTTOM_OFFSET);
		} else {
			BlockState ground = world.getBlockState(base.below());
			if (SlabRegistry.isTerrainSlab(ground.getBlock()) && ground.getBlock() instanceof SlabBlock) {
				offset = ground.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
			}
		}
		if (offset == null) return false;

		boolean changed = false;
		for (BlockPos at = base; isStalk(world.getBlockState(at)); at = at.above()) {
			BlockState was = world.getBlockState(at);
			BlockState slab = was;
			if (!was.hasProperty(OffsetableSlab.BOTTOM_OFFSET)) {
				BlockState kind = SlabRegistry.getPlantSlabDefaultState(was.getBlock());
				if (kind == null) break;
				slab = alike(was, kind);
			}
			slab = slab.setValue(OffsetableSlab.BOTTOM_OFFSET, offset);
			if (slab != was) {
				world.setBlock(at, slab, Block.UPDATE_CLIENTS);
				changed = true;
			}
		}
		return changed;
	}

	/** The slab kind carrying over whatever the vanilla piece knew - age, leaves, stage - by name. */
	private static BlockState alike(BlockState from, BlockState to) {
		for (Property<?> theirs : from.getProperties()) {
			Property<?> ours = to.getBlock().getStateDefinition().getProperty(theirs.getName());
			if (ours == null) continue;
			Optional<?> value = ours.getValue(name(from, theirs));
			if (value.isPresent()) to = with(to, ours, value.get());
		}
		return to;
	}

	private static <T extends Comparable<T>> String name(BlockState state, Property<T> property) {
		return property.getName(state.getValue(property));
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, Object value) {
		return state.setValue(property, (T) value);
	}
}
