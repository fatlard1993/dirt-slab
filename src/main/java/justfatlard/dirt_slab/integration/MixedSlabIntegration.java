package justfatlard.dirt_slab.integration;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Terrain slabs that have been built into a mixed slab.
 *
 * <p>A mixed slab is its own block, so a dirt slab inside one stops answering to any of the checks
 * in this mod - it is no longer a dirt slab, it is half of something else. Everything here exists to
 * ask the other mod what is really in there.
 *
 * <p>The rule that governs every use of it: <b>a mixed slab is a full-height block whose surface
 * material is its top half.</b> So it is asked about material - is that grass, can grass creep onto
 * it - and never treated as a slab. Nothing here reads a slab type or offsets a plant onto one: a
 * mixed slab is full height, so a plant standing on it stands where it would on any full block.
 *
 * <p>Reached by reflection behind a mod-loaded check, the way the rest of the suite handles a
 * neighbour it does not require.
 */
public final class MixedSlabIntegration {
	private MixedSlabIntegration() {}

	private static final org.slf4j.Logger LOGGER =
		org.slf4j.LoggerFactory.getLogger("dirt-slab/mixed-slabs");

	private static final boolean AVAILABLE = FabricLoader.getInstance().isModLoaded("mixed-slabs-justfatlard");

	private static MethodHandle topHalf;
	private static MethodHandle withTopHalf;
	private static boolean bound = false;

	/** The slab making this block's surface, or null if it is not a mixed slab. */
	public static Block surfaceOf(BlockState state) {
		if (!bind()) return null;

		try {
			return (Block) topHalf.invokeExact(state);
		} catch (Throwable t) {
			return null;
		}
	}

	/**
	 * The same mixed slab with a different surface, or null if this is not a mixed slab whose
	 * surface is currently {@code expected}.
	 *
	 * <p>The {@code expected} check is the caller's whole safety net: it is how grass creeping onto
	 * a mixed slab knows it is creeping onto dirt rather than quietly repainting somebody's stone.
	 */
	public static BlockState resurface(BlockState state, Block expected, Block replacement) {
		if (!bind()) return null;
		if (surfaceOf(state) != expected) return null;

		try {
			return (BlockState) withTopHalf.invokeExact(state, replacement);
		} catch (Throwable t) {
			return null;
		}
	}

	private static synchronized boolean bind() {
		if (bound) return topHalf != null && withTopHalf != null;
		bound = true;

		if (!AVAILABLE) return false;

		try {
			Class<?> api = Class.forName("justfatlard.mixed_slabs.MixedSlabsApi");
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			topHalf = lookup.findStatic(api, "topHalf",
				MethodType.methodType(Block.class, BlockState.class));
			withTopHalf = lookup.findStatic(api, "withTopHalf",
				MethodType.methodType(BlockState.class, BlockState.class, Block.class));
		} catch (ReflectiveOperationException e) {
			LOGGER.warn("mixed-slabs is present but its API did not match; "
				+ "terrain slabs inside a mixed slab will be decorative only", e);
			topHalf = null;
			withTopHalf = null;
		}
		return topHalf != null && withTopHalf != null;
	}
}
