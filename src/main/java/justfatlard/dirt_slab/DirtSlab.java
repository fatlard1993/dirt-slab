package justfatlard.dirt_slab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

import justfatlard.dirt_slab.worldgen.DirtSlabWorldGen;
import justfatlard.dirt_slab.worldgen.structure.SlabStructureProcessor;

public class DirtSlab implements ModInitializer {
	public static final String MOD_ID = "dirt-slab-justfatlard";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize(){
		DirtSlabBlocks.register();

		DirtSlabConfig config = DirtSlabConfig.load();
		SlabRegistry.initTerrainSlabs(config.resolveTerrainSlabs());

		SlabStructureProcessor.register();

		if (config.worldgenEnabled) {
			DirtSlabWorldGen.register();
		}

		LOGGER.info("Loaded dirt-slab");
	}
}
