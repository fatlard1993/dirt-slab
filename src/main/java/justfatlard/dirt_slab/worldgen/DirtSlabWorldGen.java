package justfatlard.dirt_slab.worldgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import justfatlard.dirt_slab.DirtSlab;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

public class DirtSlabWorldGen {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirtSlab.MOD_ID);

	public static final Feature<?> TERRAIN_SLAB_FEATURE = new TerrainSlabFeature();

	public static final RegistryKey<Feature<?>> TERRAIN_SLAB_FEATURE_KEY =
		RegistryKey.of(RegistryKeys.FEATURE, Identifier.of(DirtSlab.MOD_ID, "terrain_slab"));

	public static final RegistryKey<ConfiguredFeature<?, ?>> TERRAIN_SLAB_CONFIGURED_KEY =
		RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(DirtSlab.MOD_ID, "terrain_slab"));

	public static final RegistryKey<PlacedFeature> TERRAIN_SLAB_PLACED_KEY =
		RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(DirtSlab.MOD_ID, "terrain_slab"));

	public static void register() {
		Registry.register(Registries.FEATURE, Identifier.of(DirtSlab.MOD_ID, "terrain_slab"), TERRAIN_SLAB_FEATURE);

		BiomeModifications.addFeature(
			BiomeSelectors.foundInOverworld(),
			GenerationStep.Feature.TOP_LAYER_MODIFICATION,
			TERRAIN_SLAB_PLACED_KEY
		);

		LOGGER.info("World generation registered");
	}
}
