package justfatlard.dirt_slab;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Explosions have a chance of splitting destroyed blocks into their slab variants,
 * so blast damage reads as rubble and half-broken structures.
 * The full block to slab map is derived generically from the block registry:
 * every registered SlabBlock with an id ending in "_slab" is paired with the first
 * existing full block among a few name-derivation rules, covering vanilla slabs,
 * this mod's slabs, and other mods' slabs alike.
 */
public class ExplosionRubble {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirtSlab.MOD_ID);

	// Tuning knob: chance a destroyed block survives as its slab variant instead of dropping
	public static final float SPLIT_CHANCE = 0.35F;

	private static final Map<Block, SlabBlock> FULL_TO_SLAB = new HashMap<>();

	public static void register() {
		// SERVER_STARTING so every mod's blocks are registered before the scan
		ServerLifecycleEvents.SERVER_STARTING.register(server -> buildMap());
	}

	private static void buildMap() {
		FULL_TO_SLAB.clear();

		for (Block block : BuiltInRegistries.BLOCK) {
			if (!(block instanceof SlabBlock slab)) continue;

			Identifier id = BuiltInRegistries.BLOCK.getKey(block);
			String path = id.getPath();
			if (!path.endsWith("_slab")) continue;

			String base = path.substring(0, path.length() - "_slab".length());
			// "_planks" is tried before the bare base so bamboo_slab pairs with
			// bamboo_planks rather than the bamboo plant block
			Block full = findFullBlock(id.getNamespace(),
				base + "_planks", base, base + "s", base + "_block");
			if (full == null || full instanceof SlabBlock) continue;

			FULL_TO_SLAB.putIfAbsent(full, slab);
		}

		LOGGER.info("Explosion rubble map built: {} full block -> slab pairs", FULL_TO_SLAB.size());
	}

	private static Block findFullBlock(String namespace, String... candidates) {
		for (String candidate : candidates) {
			Block block = lookup(namespace, candidate);
			if (block == null && !namespace.equals(Identifier.DEFAULT_NAMESPACE)) {
				block = lookup(Identifier.DEFAULT_NAMESPACE, candidate);
			}
			if (block != null) return block;
		}

		return null;
	}

	private static Block lookup(String namespace, String path) {
		Identifier id = Identifier.tryBuild(namespace, path);
		if (id == null) return null;

		return BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
	}

	/**
	 * Called from ServerExplosionMixin for each position the explosion destroys.
	 * Returns true if the block was replaced with its slab remainder, in which case
	 * vanilla destruction (and its drops) must be skipped; the slab is the remainder.
	 */
	public static boolean trySplit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion) {
		// Mirror the guards of BlockBehaviour.onExplosionHit so we never touch a block vanilla would not destroy
		if (state.isAir() || explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK) return false;

		BlockState remainder = slabRemainder(state);
		if (remainder == null) return false;
		if (level.getRandom().nextFloat() >= SPLIT_CHANCE) return false;

		level.setBlockAndUpdate(pos, remainder);
		return true;
	}

	private static BlockState slabRemainder(BlockState state) {
		SlabBlock slab;
		if (state.getBlock() instanceof SlabBlock own) {
			// Double slabs split back to a single bottom slab; single slabs are destroyed as vanilla
			if (!state.hasProperty(SlabBlock.TYPE) || state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) return null;
			slab = own;
		} else {
			slab = FULL_TO_SLAB.get(state.getBlock());
			if (slab == null) return null;
		}

		BlockState remainder = slab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
		boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED)
			&& state.getValue(BlockStateProperties.WATERLOGGED);
		if (waterlogged && remainder.hasProperty(BlockStateProperties.WATERLOGGED)) {
			remainder = remainder.setValue(BlockStateProperties.WATERLOGGED, true);
		}

		return remainder;
	}
}
