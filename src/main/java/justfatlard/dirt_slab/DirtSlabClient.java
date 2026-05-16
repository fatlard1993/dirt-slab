package justfatlard.dirt_slab;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSources;

import java.util.List;

public class DirtSlabClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BlockColorRegistry.register(List.of(BlockTintSources.grass()), DirtSlabBlocks.GRASS_SLAB);

		BlockColorRegistry.register(List.of(BlockTintSources.stem()),
			DirtSlabBlocks.MELON_STEM_SLAB, DirtSlabBlocks.PUMPKIN_STEM_SLAB);

		BlockColorRegistry.register(List.of(BlockTintSources.stem()),
			DirtSlabBlocks.ATTACHED_MELON_STEM_SLAB, DirtSlabBlocks.ATTACHED_PUMPKIN_STEM_SLAB);

		BlockColorRegistry.register(List.of(BlockTintSources.grass()),
			DirtSlabBlocks.SHORT_GRASS_SLAB, DirtSlabBlocks.FERN_SLAB,
			DirtSlabBlocks.TALL_GRASS_SLAB, DirtSlabBlocks.LARGE_FERN_SLAB,
			DirtSlabBlocks.BUSH_SLAB);

		BlockColorRegistry.register(List.of(BlockTintSources.constant(0x8B7355)),
			DirtSlabBlocks.LEAF_LITTER_SLAB);

		BlockColorRegistry.register(List.of(BlockTintSources.sugarCane()),
			DirtSlabBlocks.SUGAR_CANE_SLAB);
	}
}
