package justfatlard.dirt_slab.worldgen;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.OffsetableSlab;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabSnowLayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import java.util.ArrayList;

/**
 * Worldgen feature that converts terrain blocks at cliff edges into slabs,
 * then converts any vanilla plants on top to their slab variants.
 *
 * Two passes: first converts terrain and immediate plants, then revisits
 * bottom slab positions to catch plants placed by later worldgen features.
 */
public class TerrainSlabFeature extends Feature<NoneFeatureConfiguration> {
	public TerrainSlabFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		BlockPos origin = context.origin();
		RandomSource random = context.random();

		int placed = 0;
		int radius = 12;

		ArrayList<BlockPos> bottomSlabPositions = new ArrayList<>();

		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				if (random.nextFloat() > 0.40f) continue;

				BlockPos columnPos = origin.offset(x, 0, z);

				// Surface slabs (bottom type at cliff edges)
				BlockPos surfacePos = findSurface(world, columnPos);
				if (surfacePos != null) {
					BlockState state = world.getBlockState(surfacePos);
					Block block = state.getBlock();

					if (SlabRegistry.isConvertibleTerrain(block) && isTerrainEdge(world, surfacePos)) {
						BlockState slabState = SlabRegistry.getTerrainSlabState(block);
						if (slabState != null) {
							BlockState finalState = slabState.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
							if (slabState.hasProperty(BlockStateProperties.SNOWY) && hasSnowOnTop(world, surfacePos)) {
								finalState = finalState.setValue(BlockStateProperties.SNOWY, true);
							}
							if (isWaterAdjacent(world, surfacePos)) {
								finalState = finalState.setValue(BlockStateProperties.WATERLOGGED, true);
							}
							world.setBlock(surfacePos, finalState, Block.UPDATE_CLIENTS);
							placed++;
							bottomSlabPositions.add(surfacePos);

							placed += convertPlantAbove(world, surfacePos, finalState);
						}
					}
				}

				// Overhang slabs (top type under cliff lips)
				BlockPos overhangPos = findOverhang(world, columnPos);
				if (overhangPos != null) {
					BlockState state = world.getBlockState(overhangPos);
					Block block = state.getBlock();

					if (SlabRegistry.isConvertibleTerrain(block) && isOverhangEdge(world, overhangPos)) {
						BlockState slabState = SlabRegistry.getTerrainSlabState(block);
						if (slabState != null) {
							BlockState finalState = slabState.setValue(SlabBlock.TYPE, SlabType.TOP);
							if (slabState.hasProperty(BlockStateProperties.SNOWY) && hasSnowOnTop(world, overhangPos)) {
								finalState = finalState.setValue(BlockStateProperties.SNOWY, true);
							}
							if (isWaterAdjacent(world, overhangPos)) {
								finalState = finalState.setValue(BlockStateProperties.WATERLOGGED, true);
							}
							world.setBlock(overhangPos, finalState, Block.UPDATE_CLIENTS);
							placed++;
						}
					}
				}

				}
		}

		// Second pass: catch plants placed by other features after our terrain conversion
		for (BlockPos slabPos : bottomSlabPositions) {
			BlockState slabState = world.getBlockState(slabPos);
			placed += convertOrSnow(world, slabPos.above(), slabState);
		}

		return placed > 0;
	}

	private int convertPlantAbove(WorldGenLevel world, BlockPos surfacePos, BlockState slabState) {
		BlockPos abovePos = surfacePos.above();
		return convertOrSnow(world, abovePos, slabState);
	}

	private int convertOrSnow(WorldGenLevel world, BlockPos abovePos, BlockState slabState) {
		BlockState aboveState = world.getBlockState(abovePos);
		BlockState plantSlabState = SlabRegistry.getPlantSlabDefaultState(aboveState.getBlock());
		if (plantSlabState != null) {
			boolean isBottomSlab = slabState.hasProperty(SlabBlock.TYPE) && slabState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
			if (plantSlabState.hasProperty(OffsetableSlab.BOTTOM_OFFSET)) {
				plantSlabState = plantSlabState.setValue(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);
			}
			world.setBlock(abovePos, plantSlabState, Block.UPDATE_CLIENTS);
			return 1;
		} else if (aboveState.isAir() || aboveState.is(Blocks.SNOW)) {
			Biome biome = world.getBiome(abovePos).value();
			if (biome.getPrecipitationAt(abovePos, world.getSeaLevel()) == Biome.Precipitation.SNOW) {
				BlockState snowState = SlabSnowLayerBlock.createForSlab(slabState);
				world.setBlock(abovePos, snowState, Block.UPDATE_CLIENTS);
				return 1;
			}
		}
		return 0;
	}

	private BlockPos findSurface(WorldGenLevel world, BlockPos pos) {
		int surfaceY = world.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()) - 1;
		if (surfaceY < world.getMinY()) return null;

		BlockPos surfacePos = new BlockPos(pos.getX(), surfaceY, pos.getZ());
		BlockState state = world.getBlockState(surfacePos);
		if (!state.isAir() && !state.liquid()) {
			BlockState aboveState = world.getBlockState(surfacePos.above());
			Block aboveBlock = aboveState.getBlock();
			if (aboveState.isAir() || SlabRegistry.isVegetation(aboveBlock) ||
				aboveBlock == Blocks.SNOW || aboveBlock == Blocks.SNOW_BLOCK) {
				return surfacePos;
			}
		}
		return null;
	}

	private static final int MAX_OVERHANG_DEPTH = 32;

	private BlockPos findOverhang(WorldGenLevel world, BlockPos pos) {
		int startY = world.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
		int minY = Math.max(world.getMinY() + 1, startY - MAX_OVERHANG_DEPTH);
		for (int y = startY; y >= minY; y--) {
			BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
			BlockState state = world.getBlockState(checkPos);
			if (!state.isAir() && !state.liquid()) {
				if (world.getBlockState(checkPos.below()).isAir()) {
					return checkPos;
				}
			}
		}
		return null;
	}

	private boolean hasSnowOnTop(WorldGenLevel world, BlockPos pos) {
		BlockState topState = world.getBlockState(pos.above());
		Block topBlock = topState.getBlock();
		return topBlock == Blocks.SNOW || topBlock == Blocks.SNOW_BLOCK || topState.is(DirtSlabBlocks.SNOW_LAYER_SLAB);
	}

	private boolean isWaterAdjacent(WorldGenLevel world, BlockPos pos) {
		for (Direction dir : Direction.values()) {
			if (dir == Direction.DOWN) continue;
			if (world.getBlockState(pos.relative(dir)).getBlock() == Blocks.WATER) return true;
		}
		return false;
	}

	private boolean isOverhangEdge(WorldGenLevel world, BlockPos pos) {
		int edgeCount = 0;

		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos adjacent = pos.relative(dir);
			BlockState adjacentState = world.getBlockState(adjacent);

			if (adjacentState.isAir() || adjacentState.liquid()) {
				edgeCount++;
				continue;
			}

			if (world.getBlockState(adjacent.above()).isAir()) {
				edgeCount++;
			}
		}

		return edgeCount >= 1 && edgeCount <= 3;
	}

	private boolean isTerrainEdge(WorldGenLevel world, BlockPos pos) {
		int airOrLowerCount = 0;

		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos adjacent = pos.relative(dir);
			BlockState adjacentState = world.getBlockState(adjacent);

			if (adjacentState.isAir() || adjacentState.liquid()) {
				airOrLowerCount++;
				continue;
			}

			if (world.getBlockState(adjacent.below()).isAir()) {
				airOrLowerCount++;
			}
		}

		return airOrLowerCount >= 1 && airOrLowerCount <= 3;
	}
}
