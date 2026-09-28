package justfatlard.dirt_slab.gametest;

import java.util.ArrayList;
import java.util.List;
import justfatlard.pandorical.gametest.Smoke;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;

/**
 * A terrain slab drops what the block it is half of drops.
 *
 * <p>Ground is the one family where the block you break and the block you get are often different:
 * a path, a farmland, a mycelium all hand back plain dirt, and the special one is what silk touch
 * is for. A slab that hands back itself is a slab that turned a shovel into a way of making path
 * blocks for free.
 *
 * <p>It is a loot table each, so the gap is one file being written the easy way rather than a
 * rule failing. Nothing errors, nothing logs, and the mod is wrong only for whoever digs that one
 * block - which is how grass path shipped for a year beside a grass slab that had it right.
 */
public final class TerrainDrops implements FabricClientGameTest {

	/** Slabs whose plain-tool drop is dirt, not themselves. */
	private static final List<String> BECOME_DIRT =
		List.of("grass_slab", "grass_path_slab", "farmland_slab", "podzol_slab", "mycelium_slab");

	/** Slabs that are their own drop, the way coarse dirt and mud are in vanilla. */
	private static final List<String> KEEP_THEMSELVES =
		List.of("dirt_slab", "coarse_dirt_slab", "rooted_dirt_slab", "mud_slab");

	@Override
	public void runTest(ClientGameTestContext context) {
		Smoke.run(context, "dirt-slab-justfatlard", session -> {
			List<String> wrong = new ArrayList<>();
			session.onServer(server -> {
				for (String name : BECOME_DIRT) {
					List<String> plain = drops(server, name, false);
					List<String> silked = drops(server, name, true);
					if (!plain.equals(List.of("dirt_slab"))) {
						wrong.add(name + " dug with a shovel gives " + plain + ", not a dirt slab");
					}
					if (!silked.equals(List.of(name))) {
						wrong.add(name + " dug with silk touch gives " + silked + ", not itself");
					}
				}
				for (String name : KEEP_THEMSELVES) {
					List<String> plain = drops(server, name, false);
					if (!plain.equals(List.of(name))) {
						wrong.add(name + " gives " + plain + " rather than itself");
					}
				}
			});
			if (!wrong.isEmpty()) throw new AssertionError(String.join("; ", wrong));
		});
	}

	/**
	 * What one slab drops, asked of the block itself rather than of the json.
	 *
	 * <p>Through the same call the game makes when a block breaks, so a table that parses and
	 * still answers wrongly is caught too.
	 */
	private static List<String> drops(net.minecraft.server.MinecraftServer server, String name, boolean silk) {
		Identifier id = Identifier.fromNamespaceAndPath("dirt-slab-justfatlard", name);
		Block block = BuiltInRegistries.BLOCK.getValue(id);
		ItemStack tool = new ItemStack(net.minecraft.world.item.Items.IRON_SHOVEL);
		if (silk) {
			server.registryAccess().lookup(Registries.ENCHANTMENT)
				.flatMap(all -> all.get(Enchantments.SILK_TOUCH))
				.ifPresent(silkTouch -> tool.enchant(silkTouch, 1));
		}
		net.minecraft.server.level.ServerLevel level = server.overworld();
		net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.ZERO;
		List<String> out = new ArrayList<>();
		for (ItemStack stack : Block.getDrops(block.defaultBlockState(), level, pos, null, null, tool)) {
			out.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
		}
		return out;
	}
}
