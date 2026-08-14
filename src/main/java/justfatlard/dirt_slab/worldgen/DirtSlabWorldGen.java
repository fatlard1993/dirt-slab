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
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class DirtSlabWorldGen {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirtSlab.MOD_ID);

	// ConfiguredFeature doesn't exist in this snapshot; its replacement, Registries.FEATURE, is a
	// dynamic/data-driven registry, not a BuiltInRegistries one, so individual Feature entries
	// can't be registered from code via Registry.register(). Instead: register the Feature *type*
	// (its MapCodec) into the static BuiltInRegistries.FEATURE_TYPE registry, and the data pack
	// JSON (data/dirt-slab-justfatlard/worldgen/feature/terrain_slab.json,
	// {"type": "dirt-slab-justfatlard:terrain_slab"}) instantiates it into the dynamic
	// Registries.FEATURE registry at load time, the same way vanilla features (e.g.
	// "minecraft:tree") are type-registered in code and instance-registered via worldgen/feature/*.json.
	public static final ResourceKey<Feature> TERRAIN_SLAB_FEATURE_KEY =
		ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(DirtSlab.MOD_ID, "terrain_slab"));

	public static final ResourceKey<PlacedFeature> TERRAIN_SLAB_PLACED_KEY =
		ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(DirtSlab.MOD_ID, "terrain_slab"));

	public static void register() {
		Registry.register(BuiltInRegistries.FEATURE_TYPE, Identifier.fromNamespaceAndPath(DirtSlab.MOD_ID, "terrain_slab"), TerrainSlabFeature.CODEC);

		BiomeModifications.addFeature(
			BiomeSelectors.foundInOverworld(),
			GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
			TERRAIN_SLAB_PLACED_KEY
		);

		LOGGER.info("World generation registered");
	}
}
