package justfatlard.dirt_slab.worldgen;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlabSnowLayerBlock;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.property.Properties;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.ArrayList;

public class DirtSlabWorldGen {
	public static final String MOD_ID = Main.MOD_ID;

	// Feature for placing terrain slabs at edges
	public static final Feature<DefaultFeatureConfig> TERRAIN_SLAB_FEATURE = new Feature<DefaultFeatureConfig>(DefaultFeatureConfig.CODEC) {
		@Override
		public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
			StructureWorldAccess world = context.getWorld();
			BlockPos origin = context.getOrigin();
			Random random = context.getRandom();

			int placed = 0;
			int radius = 12;

			// Track positions where we place bottom slabs for the second pass
			ArrayList<BlockPos> bottomSlabPositions = new ArrayList<>();

			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					// Only process some positions for performance (about 40% chance)
					if (random.nextFloat() > 0.40f) continue;

					BlockPos checkPos = origin.add(x, 0, z);

					// Find the surface (top of terrain) for bottom slabs
					BlockPos surfacePos = findSurface(world, checkPos);
					if (surfacePos != null) {
						BlockState state = world.getBlockState(surfacePos);
						Block block = state.getBlock();

						// Check if this is a convertible block at a terrain edge
						if (isConvertibleBlock(block) && isTerrainEdge(world, surfacePos)) {
							BlockState slabState = getSlabFor(block);
							if (slabState != null) {
								// Set snowy state only if snow is directly on top (for grass-type slabs)
								BlockState finalState = slabState.with(SlabBlock.TYPE, SlabType.BOTTOM);
								if (slabState.contains(Properties.SNOWY) && hasSnowOnTop(world, surfacePos)) {
									finalState = finalState.with(Properties.SNOWY, true);
								}
								// Set waterlogged if water is adjacent
								if (isWaterAdjacent(world, surfacePos)) {
									finalState = finalState.with(Properties.WATERLOGGED, true);
								}
								world.setBlockState(surfacePos, finalState, Block.NOTIFY_LISTENERS);
								placed++;

								// Track this position for second pass
								bottomSlabPositions.add(surfacePos);

								// Convert any plant on top to slab variant, or place snow in snowy biomes
								BlockPos abovePos = surfacePos.up();
								BlockState aboveState = world.getBlockState(abovePos);
								BlockState plantSlabState = getPlantSlabFor(aboveState);
								if (plantSlabState != null) {
									world.setBlockState(abovePos, plantSlabState, Block.NOTIFY_LISTENERS);
								} else if (aboveState.isAir() || aboveState.isOf(Blocks.SNOW)) {
									// Check if biome should have snow
									Biome biome = world.getBiome(abovePos).value();
									if (biome.getPrecipitation(abovePos, world.getSeaLevel()) == Biome.Precipitation.SNOW) {
										BlockState snowState = SlabSnowLayerBlock.createForSlab(finalState);
										world.setBlockState(abovePos, snowState, Block.NOTIFY_LISTENERS);
									}
								}
							}
						}
					}

					// Find overhangs (bottom of terrain) for top slabs
					BlockPos overhangPos = findOverhang(world, checkPos);
					if (overhangPos != null) {
						BlockState state = world.getBlockState(overhangPos);
						Block block = state.getBlock();

						// Check if this is a convertible block at an overhang edge
						if (isConvertibleBlock(block) && isOverhangEdge(world, overhangPos)) {
							BlockState slabState = getSlabFor(block);
							if (slabState != null) {
								// Set snowy state only if snow is directly on top (for grass-type slabs)
								BlockState finalState = slabState.with(SlabBlock.TYPE, SlabType.TOP);
								if (slabState.contains(Properties.SNOWY) && hasSnowOnTop(world, overhangPos)) {
									finalState = finalState.with(Properties.SNOWY, true);
								}
								// Set waterlogged if water is adjacent
								if (isWaterAdjacent(world, overhangPos)) {
									finalState = finalState.with(Properties.WATERLOGGED, true);
								}
								world.setBlockState(overhangPos, finalState, Block.NOTIFY_LISTENERS);
								placed++;
							}
						}
					}

					// Also check for dirt paths (village paths) - convert at edges
					BlockPos pathPos = findDirtPath(world, checkPos);
					if (pathPos != null && isTerrainEdge(world, pathPos)) {
						BlockState pathSlabState = DirtSlabBlocks.GRASS_PATH_SLAB.getDefaultState()
							.with(SlabBlock.TYPE, SlabType.BOTTOM);
						// Set waterlogged if water is adjacent
						if (isWaterAdjacent(world, pathPos)) {
							pathSlabState = pathSlabState.with(Properties.WATERLOGGED, true);
						}
						world.setBlockState(pathPos, pathSlabState, Block.NOTIFY_LISTENERS);
						placed++;
						// Track path slabs too
						bottomSlabPositions.add(pathPos);
					}
				}
			}

			// Second pass: only check positions where we placed bottom slabs
			// This catches plants placed by other worldgen features after our terrain conversion
			// and also places snow on slabs in snowy biomes
			for (BlockPos slabPos : bottomSlabPositions) {
				BlockPos abovePos = slabPos.up();
				BlockState aboveState = world.getBlockState(abovePos);
				BlockState slabState = world.getBlockState(slabPos);

				if (isVegetation(aboveState.getBlock())) {
					BlockState plantSlabState = getPlantSlabFor(aboveState);
					if (plantSlabState != null) {
						world.setBlockState(abovePos, plantSlabState, Block.NOTIFY_LISTENERS);
						placed++;
					}
				} else if (aboveState.isAir() || aboveState.isOf(Blocks.SNOW)) {
					// Check if biome should have snow and we have a bottom slab
					if (slabState.contains(SlabBlock.TYPE) && slabState.get(SlabBlock.TYPE) == SlabType.BOTTOM) {
						Biome biome = world.getBiome(abovePos).value();
						if (biome.getPrecipitation(abovePos, world.getSeaLevel()) == Biome.Precipitation.SNOW) {
							BlockState snowState = SlabSnowLayerBlock.createForSlab(slabState);
							world.setBlockState(abovePos, snowState, Block.NOTIFY_LISTENERS);
							placed++;
						}
					}
				}
			}

			return placed > 0;
		}

		private BlockPos findSurface(StructureWorldAccess world, BlockPos pos) {
			// Start from world height and go down
			for (int y = world.getTopYInclusive(); y >= world.getBottomY(); y--) {
				BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
				BlockState state = world.getBlockState(checkPos);
				if (!state.isAir() && !state.isLiquid()) {
					// Found a solid block, check if air, vegetation, or snow is above
					BlockState aboveState = world.getBlockState(checkPos.up());
					Block aboveBlock = aboveState.getBlock();
					if (aboveState.isAir() || isVegetation(aboveBlock) ||
						aboveBlock == Blocks.SNOW || aboveBlock == Blocks.SNOW_BLOCK) {
						return checkPos;
					}
				}
			}
			return null;
		}

		private boolean isVegetation(Block block) {
			return block == Blocks.SHORT_GRASS ||
				   block == Blocks.TALL_GRASS ||
				   block == Blocks.FERN ||
				   block == Blocks.LARGE_FERN ||
				   block == Blocks.DEAD_BUSH ||
				   block == Blocks.SHORT_DRY_GRASS ||
				   block == Blocks.TALL_DRY_GRASS ||
				   block == Blocks.LEAF_LITTER ||
				   block == Blocks.PINK_PETALS ||
				   block == Blocks.WILDFLOWERS ||
				   block == Blocks.FIREFLY_BUSH ||
				   block == Blocks.SUGAR_CANE ||
				   block == Blocks.BAMBOO_SAPLING ||
				   block == Blocks.BAMBOO ||
				   block == Blocks.DANDELION ||
				   block == Blocks.POPPY ||
				   block == Blocks.BLUE_ORCHID ||
				   block == Blocks.ALLIUM ||
				   block == Blocks.AZURE_BLUET ||
				   block == Blocks.RED_TULIP ||
				   block == Blocks.ORANGE_TULIP ||
				   block == Blocks.WHITE_TULIP ||
				   block == Blocks.PINK_TULIP ||
				   block == Blocks.OXEYE_DAISY ||
				   block == Blocks.CORNFLOWER ||
				   block == Blocks.LILY_OF_THE_VALLEY ||
				   block == Blocks.WITHER_ROSE ||
				   block == Blocks.TORCHFLOWER ||
				   block == Blocks.SUNFLOWER ||
				   block == Blocks.LILAC ||
				   block == Blocks.ROSE_BUSH ||
				   block == Blocks.PEONY ||
				   block == Blocks.RED_MUSHROOM ||
				   block == Blocks.BROWN_MUSHROOM;
		}

		private BlockPos findOverhang(StructureWorldAccess world, BlockPos pos) {
			// Start from world height and go down, looking for blocks with air below (overhangs)
			for (int y = world.getTopYInclusive(); y >= world.getBottomY() + 1; y--) {
				BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
				BlockState state = world.getBlockState(checkPos);
				if (!state.isAir() && !state.isLiquid()) {
					// Found a solid block, check if air is below (overhang)
					if (world.getBlockState(checkPos.down()).isAir()) {
						return checkPos;
					}
				}
			}
			return null;
		}

		private BlockPos findDirtPath(StructureWorldAccess world, BlockPos pos) {
			// Start from world height and go down, looking for dirt paths (village paths)
			for (int y = world.getTopYInclusive(); y >= world.getBottomY(); y--) {
				BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
				if (world.getBlockState(checkPos).getBlock() == Blocks.DIRT_PATH) {
					return checkPos;
				}
			}
			return null;
		}

		private boolean hasSnowOnTop(StructureWorldAccess world, BlockPos pos) {
			// Only check block directly above - no nearby checks
			BlockState topState = world.getBlockState(pos.up());
			Block topBlock = topState.getBlock();
			if (topBlock == Blocks.SNOW || topBlock == Blocks.SNOW_BLOCK) return true;
			if (topState.isOf(DirtSlabBlocks.SNOW_LAYER_SLAB)) return true;
			return false;
		}

		private boolean isWaterAdjacent(StructureWorldAccess world, BlockPos pos) {
			// Check adjacent blocks for water (exclude below - only sides and above)
			for (Direction dir : Direction.values()) {
				if (dir == Direction.DOWN) continue; // Water below shouldn't waterlog
				BlockState adjacentState = world.getBlockState(pos.offset(dir));
				if (adjacentState.getBlock() == Blocks.WATER) return true;
			}
			return false;
		}

		private boolean isOverhangEdge(StructureWorldAccess world, BlockPos pos) {
			// Check if this block is at an overhang edge (has air adjacent at same level or below)
			int edgeCount = 0;

			for (Direction dir : Direction.Type.HORIZONTAL) {
				BlockPos adjacent = pos.offset(dir);
				BlockState adjacentState = world.getBlockState(adjacent);

				// If adjacent is air at same level, this is an overhang edge
				if (adjacentState.isAir() || adjacentState.isLiquid()) {
					edgeCount++;
					continue;
				}

				// If the block above adjacent is air (step up to open), consider it an edge
				BlockPos aboveAdjacent = adjacent.up();
				if (world.getBlockState(aboveAdjacent).isAir()) {
					edgeCount++;
				}
			}

			// Consider it an overhang edge if at least 1 side qualifies but not all 4
			return edgeCount >= 1 && edgeCount <= 3;
		}

		private boolean isConvertibleBlock(Block block) {
			return block == Blocks.GRASS_BLOCK ||
				   block == Blocks.DIRT ||
				   block == Blocks.COARSE_DIRT ||
				   block == Blocks.PODZOL ||
				   block == Blocks.MYCELIUM ||
				   block == Blocks.MUD ||
				   block == Blocks.ROOTED_DIRT ||
				   // Natural stone variants
				   block == Blocks.STONE ||
				   block == Blocks.DEEPSLATE ||
				   block == Blocks.TUFF ||
				   block == Blocks.ANDESITE ||
				   block == Blocks.DIORITE ||
				   block == Blocks.GRANITE ||
				   // Sandstone variants
				   block == Blocks.SANDSTONE ||
				   block == Blocks.SMOOTH_SANDSTONE ||
				   block == Blocks.RED_SANDSTONE ||
				   block == Blocks.SMOOTH_RED_SANDSTONE;
		}

		private boolean isTerrainEdge(StructureWorldAccess world, BlockPos pos) {
			// Check if this block is at a terrain edge (has air or lower terrain adjacent)
			int airOrLowerCount = 0;

			for (Direction dir : Direction.Type.HORIZONTAL) {
				BlockPos adjacent = pos.offset(dir);
				BlockState adjacentState = world.getBlockState(adjacent);

				// If adjacent is air or liquid, this is an edge
				if (adjacentState.isAir() || adjacentState.isLiquid()) {
					airOrLowerCount++;
					continue;
				}

				// If the block below adjacent is air (cliff edge)
				BlockPos belowAdjacent = adjacent.down();
				if (world.getBlockState(belowAdjacent).isAir()) {
					airOrLowerCount++;
				}
			}

			// Consider it an edge if at least 1 side qualifies but not all 4 (avoid isolated blocks)
			return airOrLowerCount >= 1 && airOrLowerCount <= 3;
		}

		private BlockState getSlabFor(Block block) {
			// Mod dirt slabs
			if (block == Blocks.GRASS_BLOCK) return DirtSlabBlocks.GRASS_SLAB.getDefaultState();
			if (block == Blocks.DIRT) return DirtSlabBlocks.DIRT_SLAB.getDefaultState();
			if (block == Blocks.COARSE_DIRT) return DirtSlabBlocks.COARSE_DIRT_SLAB.getDefaultState();
			if (block == Blocks.PODZOL) return DirtSlabBlocks.PODZOL_SLAB.getDefaultState();
			if (block == Blocks.MYCELIUM) return DirtSlabBlocks.MYCELIUM_SLAB.getDefaultState();
			if (block == Blocks.MUD) return DirtSlabBlocks.MUD_SLAB.getDefaultState();
			if (block == Blocks.ROOTED_DIRT) return DirtSlabBlocks.ROOTED_DIRT_SLAB.getDefaultState();

			// Vanilla stone slabs
			if (block == Blocks.STONE) return Blocks.STONE_SLAB.getDefaultState();
			if (block == Blocks.DEEPSLATE) return Blocks.DEEPSLATE_TILE_SLAB.getDefaultState();
			if (block == Blocks.TUFF) return Blocks.TUFF_SLAB.getDefaultState();
			if (block == Blocks.ANDESITE) return Blocks.ANDESITE_SLAB.getDefaultState();
			if (block == Blocks.DIORITE) return Blocks.DIORITE_SLAB.getDefaultState();
			if (block == Blocks.GRANITE) return Blocks.GRANITE_SLAB.getDefaultState();

			// Vanilla sandstone slabs
			if (block == Blocks.SANDSTONE) return Blocks.SANDSTONE_SLAB.getDefaultState();
			if (block == Blocks.SMOOTH_SANDSTONE) return Blocks.SMOOTH_SANDSTONE_SLAB.getDefaultState();
			if (block == Blocks.RED_SANDSTONE) return Blocks.RED_SANDSTONE_SLAB.getDefaultState();
			if (block == Blocks.SMOOTH_RED_SANDSTONE) return Blocks.SMOOTH_RED_SANDSTONE_SLAB.getDefaultState();

			return null;
		}

		private BlockState getPlantSlabFor(BlockState state) {
			Block block = state.getBlock();

			// Short plants
			if (block == Blocks.SHORT_GRASS) return DirtSlabBlocks.SHORT_GRASS_SLAB.getDefaultState();
			if (block == Blocks.FERN) return DirtSlabBlocks.FERN_SLAB.getDefaultState();
			if (block == Blocks.DEAD_BUSH) return DirtSlabBlocks.DEAD_BUSH_SLAB.getDefaultState();
			if (block == Blocks.SHORT_DRY_GRASS) return DirtSlabBlocks.SHORT_DRY_GRASS_SLAB.getDefaultState();
			if (block == Blocks.TALL_DRY_GRASS) return DirtSlabBlocks.TALL_DRY_GRASS_SLAB.getDefaultState();

			// Flowers
			if (block == Blocks.DANDELION) return DirtSlabBlocks.DANDELION_SLAB.getDefaultState();
			if (block == Blocks.POPPY) return DirtSlabBlocks.POPPY_SLAB.getDefaultState();
			if (block == Blocks.BLUE_ORCHID) return DirtSlabBlocks.BLUE_ORCHID_SLAB.getDefaultState();
			if (block == Blocks.ALLIUM) return DirtSlabBlocks.ALLIUM_SLAB.getDefaultState();
			if (block == Blocks.AZURE_BLUET) return DirtSlabBlocks.AZURE_BLUET_SLAB.getDefaultState();
			if (block == Blocks.RED_TULIP) return DirtSlabBlocks.RED_TULIP_SLAB.getDefaultState();
			if (block == Blocks.ORANGE_TULIP) return DirtSlabBlocks.ORANGE_TULIP_SLAB.getDefaultState();
			if (block == Blocks.WHITE_TULIP) return DirtSlabBlocks.WHITE_TULIP_SLAB.getDefaultState();
			if (block == Blocks.PINK_TULIP) return DirtSlabBlocks.PINK_TULIP_SLAB.getDefaultState();
			if (block == Blocks.OXEYE_DAISY) return DirtSlabBlocks.OXEYE_DAISY_SLAB.getDefaultState();
			if (block == Blocks.CORNFLOWER) return DirtSlabBlocks.CORNFLOWER_SLAB.getDefaultState();
			if (block == Blocks.LILY_OF_THE_VALLEY) return DirtSlabBlocks.LILY_OF_THE_VALLEY_SLAB.getDefaultState();
			if (block == Blocks.WITHER_ROSE) return DirtSlabBlocks.WITHER_ROSE_SLAB.getDefaultState();
			if (block == Blocks.TORCHFLOWER) return DirtSlabBlocks.TORCHFLOWER_SLAB.getDefaultState();

			// Mushrooms
			if (block == Blocks.RED_MUSHROOM) return DirtSlabBlocks.RED_MUSHROOM_SLAB.getDefaultState();
			if (block == Blocks.BROWN_MUSHROOM) return DirtSlabBlocks.BROWN_MUSHROOM_SLAB.getDefaultState();

			// Leaf litter
			if (block == Blocks.LEAF_LITTER) return DirtSlabBlocks.LEAF_LITTER_SLAB.getDefaultState();

			// Pink petals and wildflowers
			if (block == Blocks.PINK_PETALS) return DirtSlabBlocks.PINK_PETALS_SLAB.getDefaultState();
			if (block == Blocks.WILDFLOWERS) return DirtSlabBlocks.WILDFLOWERS_SLAB.getDefaultState();

			// Firefly bush
			if (block == Blocks.FIREFLY_BUSH) return DirtSlabBlocks.FIREFLY_BUSH_SLAB.getDefaultState();

			// Sugar cane
			if (block == Blocks.SUGAR_CANE) return DirtSlabBlocks.SUGAR_CANE_SLAB.getDefaultState();

			// Bamboo
			if (block == Blocks.BAMBOO_SAPLING) return DirtSlabBlocks.BAMBOO_SHOOT_SLAB.getDefaultState();
			if (block == Blocks.BAMBOO) return DirtSlabBlocks.BAMBOO_SLAB.getDefaultState();

			// Tall plants (bottom half only - these are 2-block plants)
			if (block == Blocks.TALL_GRASS) return DirtSlabBlocks.TALL_GRASS_SLAB.getDefaultState();
			if (block == Blocks.LARGE_FERN) return DirtSlabBlocks.LARGE_FERN_SLAB.getDefaultState();

			// Tall flowers (bottom half only)
			if (block == Blocks.SUNFLOWER) return DirtSlabBlocks.SUNFLOWER_SLAB.getDefaultState();
			if (block == Blocks.LILAC) return DirtSlabBlocks.LILAC_SLAB.getDefaultState();
			if (block == Blocks.ROSE_BUSH) return DirtSlabBlocks.ROSE_BUSH_SLAB.getDefaultState();
			if (block == Blocks.PEONY) return DirtSlabBlocks.PEONY_SLAB.getDefaultState();

			return null;
		}
	};

	// Registry keys
	public static final RegistryKey<Feature<?>> TERRAIN_SLAB_FEATURE_KEY =
		RegistryKey.of(RegistryKeys.FEATURE, Identifier.of(MOD_ID, "terrain_slab"));

	public static final RegistryKey<ConfiguredFeature<?, ?>> TERRAIN_SLAB_CONFIGURED_KEY =
		RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(MOD_ID, "terrain_slab"));

	public static final RegistryKey<PlacedFeature> TERRAIN_SLAB_PLACED_KEY =
		RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "terrain_slab"));

	public static void register() {
		// Register the feature
		Registry.register(Registries.FEATURE, Identifier.of(MOD_ID, "terrain_slab"), TERRAIN_SLAB_FEATURE);

		// Add to overworld biomes that have grass/dirt terrain
		BiomeModifications.addFeature(
			BiomeSelectors.foundInOverworld(),
			GenerationStep.Feature.TOP_LAYER_MODIFICATION,
			TERRAIN_SLAB_PLACED_KEY
		);

		System.out.println("[dirt-slab] World generation registered");
	}
}
