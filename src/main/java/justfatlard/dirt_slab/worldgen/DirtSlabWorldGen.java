package justfatlard.dirt_slab.worldgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import justfatlard.dirt_slab.DirtSlab;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class DirtSlabWorldGen {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirtSlab.MOD_ID);

	public static final Feature<?> TERRAIN_SLAB_FEATURE = new TerrainSlabFeature();

	public static final ResourceKey<Feature<?>> TERRAIN_SLAB_FEATURE_KEY =
		ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(DirtSlab.MOD_ID, "terrain_slab"));

	public static final ResourceKey<ConfiguredFeature<?, ?>> TERRAIN_SLAB_CONFIGURED_KEY =
		ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(DirtSlab.MOD_ID, "terrain_slab"));

	public static final ResourceKey<PlacedFeature> TERRAIN_SLAB_PLACED_KEY =
		ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(DirtSlab.MOD_ID, "terrain_slab"));

	public static void register() {
		Registry.register(BuiltInRegistries.FEATURE, Identifier.fromNamespaceAndPath(DirtSlab.MOD_ID, "terrain_slab"), TERRAIN_SLAB_FEATURE);

		BiomeModifications.addFeature(
			BiomeSelectors.foundInOverworld(),
			GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
			TERRAIN_SLAB_PLACED_KEY
		);

		LOGGER.info("World generation registered");
	}
}
