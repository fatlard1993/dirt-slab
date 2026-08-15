package justfatlard.dirt_slab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

import justfatlard.dirt_slab.worldgen.DirtSlabWorldGen;
import justfatlard.dirt_slab.worldgen.structure.SlabStructureProcessor;
import justfatlard.pandorical.api.PandoricalApi;

public class DirtSlab implements ModInitializer {
	public static final String MOD_ID = "dirt-slab-justfatlard";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize(){
		DirtSlabBlocks.register();
		PandoricalApi.content().registerModAssets(MOD_ID);
		registerBlockTints();
		HoeShovelInteraction.register();
		ExplosionRubble.register();

		DirtSlabConfig config = DirtSlabConfig.load();
		SlabRegistry.initTerrainSlabs(config.resolveTerrainSlabs());

		SlabStructureProcessor.register();

		if (config.worldgenEnabled) {
			DirtSlabWorldGen.register();
		}

		LOGGER.info("Loaded dirt-slab");
	}

	private static void registerBlockTints() {
		var tints = PandoricalApi.blockTints();

		tints.grass(
			MOD_ID + ":grass_slab",
			MOD_ID + ":short_grass_slab",
			MOD_ID + ":fern_slab",
			MOD_ID + ":tall_grass_slab",
			MOD_ID + ":large_fern_slab",
			MOD_ID + ":bush_slab"
		);
		tints.stem(
			MOD_ID + ":melon_stem_slab",
			MOD_ID + ":pumpkin_stem_slab",
			MOD_ID + ":attached_melon_stem_slab",
			MOD_ID + ":attached_pumpkin_stem_slab"
		);
		tints.constant(0x8B7355, MOD_ID + ":leaf_litter_slab");
		tints.sugarCane(MOD_ID + ":sugar_cane_slab");
	}
}
