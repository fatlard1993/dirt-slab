package justfatlard.dirt_slab;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class DirtSlabConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirtSlab.MOD_ID);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("dirt-slab.json");

	public boolean worldgenEnabled = true;
	public final Map<String, String> terrainSlabs = new LinkedHashMap<>();

	private DirtSlabConfig() {}

	public static DirtSlabConfig load() {
		DirtSlabConfig config = new DirtSlabConfig();
		config.populateDefaults();

		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				JsonObject json = GSON.fromJson(reader, JsonObject.class);

				if (json.has("worldgen_enabled")) {
					config.worldgenEnabled = json.get("worldgen_enabled").getAsBoolean();
				}

				if (json.has("terrain_slabs")) {
					config.terrainSlabs.clear();
					JsonObject slabs = json.getAsJsonObject("terrain_slabs");
					for (Map.Entry<String, JsonElement> entry : slabs.entrySet()) {
						config.terrainSlabs.put(entry.getKey(), entry.getValue().getAsString());
					}
				}

				LOGGER.info("Loaded config from {}", CONFIG_PATH);
			} catch (Exception e) {
				LOGGER.error("Failed to load config, using defaults", e);
				config = new DirtSlabConfig();
				config.populateDefaults();
			}
		} else {
			config.save();
			LOGGER.info("Created default config at {}", CONFIG_PATH);
		}

		return config;
	}

	private void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				JsonObject json = new JsonObject();
				json.add("worldgen_enabled", new JsonPrimitive(worldgenEnabled));

				JsonObject slabs = new JsonObject();
				for (Map.Entry<String, String> entry : terrainSlabs.entrySet()) {
					slabs.addProperty(entry.getKey(), entry.getValue());
				}
				json.add("terrain_slabs", slabs);

				GSON.toJson(json, writer);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to save config", e);
		}
	}

	public Map<Block, Block> resolveTerrainSlabs() {
		Map<Block, Block> resolved = new LinkedHashMap<>();

		for (Map.Entry<String, String> entry : terrainSlabs.entrySet()) {
			Identifier sourceId = Identifier.parse(entry.getKey());
			Identifier targetId = Identifier.parse(entry.getValue());

			if (!BuiltInRegistries.BLOCK.containsKey(sourceId) || !BuiltInRegistries.BLOCK.containsKey(targetId)) {
				LOGGER.warn("Skipping terrain slab mapping: {} -> {} (block not found)", entry.getKey(), entry.getValue());
				continue;
			}

			resolved.put(BuiltInRegistries.BLOCK.getValue(sourceId), BuiltInRegistries.BLOCK.getValue(targetId));
		}

		return resolved;
	}

	private void populateDefaults() {
		// Dirt types → mod slabs
		terrainSlabs.put("minecraft:grass_block", "dirt-slab-justfatlard:grass_slab");
		terrainSlabs.put("minecraft:dirt", "dirt-slab-justfatlard:dirt_slab");
		terrainSlabs.put("minecraft:coarse_dirt", "dirt-slab-justfatlard:coarse_dirt_slab");
		terrainSlabs.put("minecraft:podzol", "dirt-slab-justfatlard:podzol_slab");
		terrainSlabs.put("minecraft:mycelium", "dirt-slab-justfatlard:mycelium_slab");
		terrainSlabs.put("minecraft:mud", "dirt-slab-justfatlard:mud_slab");
		terrainSlabs.put("minecraft:rooted_dirt", "dirt-slab-justfatlard:rooted_dirt_slab");
		terrainSlabs.put("minecraft:dirt_path", "dirt-slab-justfatlard:grass_path_slab");

		// Stone types → vanilla slabs
		terrainSlabs.put("minecraft:stone", "minecraft:stone_slab");
		terrainSlabs.put("minecraft:deepslate", "minecraft:deepslate_tile_slab");
		terrainSlabs.put("minecraft:tuff", "minecraft:tuff_slab");
		terrainSlabs.put("minecraft:andesite", "minecraft:andesite_slab");
		terrainSlabs.put("minecraft:diorite", "minecraft:diorite_slab");
		terrainSlabs.put("minecraft:granite", "minecraft:granite_slab");

		// Sandstone types → vanilla slabs
		terrainSlabs.put("minecraft:sandstone", "minecraft:sandstone_slab");
		terrainSlabs.put("minecraft:smooth_sandstone", "minecraft:smooth_sandstone_slab");
		terrainSlabs.put("minecraft:red_sandstone", "minecraft:red_sandstone_slab");
		terrainSlabs.put("minecraft:smooth_red_sandstone", "minecraft:smooth_red_sandstone_slab");
	}
}
