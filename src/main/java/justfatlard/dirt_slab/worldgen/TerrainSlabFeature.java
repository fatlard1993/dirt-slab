package justfatlard.dirt_slab.worldgen;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.OffsetableSlab;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabSnowLayerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

import java.util.ArrayList;

/**
 * Worldgen feature that converts terrain blocks at cliff edges into slabs,
 * then converts any vanilla plants on top to their slab variants.
 *
 * Two passes: first converts terrain and immediate plants, then revisits
 * bottom slab positions to catch plants placed by later worldgen features.
 */
public class TerrainSlabFeature extends Feature<DefaultFeatureConfig> {
	public TerrainSlabFeature() {
		super(DefaultFeatureConfig.CODEC);
	}

	@Override
	public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		StructureWorldAccess world = context.getWorld();
		BlockPos origin = context.getOrigin();
		Random random = context.getRandom();

		int placed = 0;
		int radius = 12;

		ArrayList<BlockPos> bottomSlabPositions = new ArrayList<>();

		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				if (random.nextFloat() > 0.40f) continue;

				BlockPos columnPos = origin.add(x, 0, z);

				// Surface slabs (bottom type at cliff edges)
				BlockPos surfacePos = findSurface(world, columnPos);
				if (surfacePos != null) {
					BlockState state = world.getBlockState(surfacePos);
					Block block = state.getBlock();

					if (SlabRegistry.isConvertibleTerrain(block) && isTerrainEdge(world, surfacePos)) {
						BlockState slabState = SlabRegistry.getTerrainSlabState(block);
						if (slabState != null) {
							BlockState finalState = slabState.with(SlabBlock.TYPE, SlabType.BOTTOM);
							if (slabState.contains(Properties.SNOWY) && hasSnowOnTop(world, surfacePos)) {
								finalState = finalState.with(Properties.SNOWY, true);
							}
							if (isWaterAdjacent(world, surfacePos)) {
								finalState = finalState.with(Properties.WATERLOGGED, true);
							}
							world.setBlockState(surfacePos, finalState, Block.NOTIFY_LISTENERS);
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
							BlockState finalState = slabState.with(SlabBlock.TYPE, SlabType.TOP);
							if (slabState.contains(Properties.SNOWY) && hasSnowOnTop(world, overhangPos)) {
								finalState = finalState.with(Properties.SNOWY, true);
							}
							if (isWaterAdjacent(world, overhangPos)) {
								finalState = finalState.with(Properties.WATERLOGGED, true);
							}
							world.setBlockState(overhangPos, finalState, Block.NOTIFY_LISTENERS);
							placed++;
						}
					}
				}

				}
		}

		// Second pass: catch plants placed by other features after our terrain conversion
		for (BlockPos slabPos : bottomSlabPositions) {
			BlockState slabState = world.getBlockState(slabPos);
			placed += convertOrSnow(world, slabPos.up(), slabState);
		}

		return placed > 0;
	}

	private int convertPlantAbove(StructureWorldAccess world, BlockPos surfacePos, BlockState slabState) {
		BlockPos abovePos = surfacePos.up();
		return convertOrSnow(world, abovePos, slabState);
	}

	private int convertOrSnow(StructureWorldAccess world, BlockPos abovePos, BlockState slabState) {
		BlockState aboveState = world.getBlockState(abovePos);
		BlockState plantSlabState = SlabRegistry.getPlantSlabDefaultState(aboveState.getBlock());
		if (plantSlabState != null) {
			boolean isBottomSlab = slabState.contains(SlabBlock.TYPE) && slabState.get(SlabBlock.TYPE) == SlabType.BOTTOM;
			if (plantSlabState.contains(OffsetableSlab.BOTTOM_OFFSET)) {
				plantSlabState = plantSlabState.with(OffsetableSlab.BOTTOM_OFFSET, isBottomSlab);
			}
			world.setBlockState(abovePos, plantSlabState, Block.NOTIFY_LISTENERS);
			return 1;
		} else if (aboveState.isAir() || aboveState.isOf(Blocks.SNOW)) {
			Biome biome = world.getBiome(abovePos).value();
			if (biome.getPrecipitation(abovePos, world.getSeaLevel()) == Biome.Precipitation.SNOW) {
				BlockState snowState = SlabSnowLayerBlock.createForSlab(slabState);
				world.setBlockState(abovePos, snowState, Block.NOTIFY_LISTENERS);
				return 1;
			}
		}
		return 0;
	}

	private BlockPos findSurface(StructureWorldAccess world, BlockPos pos) {
		int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ()) - 1;
		if (surfaceY < world.getBottomY()) return null;

		BlockPos surfacePos = new BlockPos(pos.getX(), surfaceY, pos.getZ());
		BlockState state = world.getBlockState(surfacePos);
		if (!state.isAir() && !state.isLiquid()) {
			BlockState aboveState = world.getBlockState(surfacePos.up());
			Block aboveBlock = aboveState.getBlock();
			if (aboveState.isAir() || SlabRegistry.isVegetation(aboveBlock) ||
				aboveBlock == Blocks.SNOW || aboveBlock == Blocks.SNOW_BLOCK) {
				return surfacePos;
			}
		}
		return null;
	}

	private static final int MAX_OVERHANG_DEPTH = 32;

	private BlockPos findOverhang(StructureWorldAccess world, BlockPos pos) {
		int startY = world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ());
		int minY = Math.max(world.getBottomY() + 1, startY - MAX_OVERHANG_DEPTH);
		for (int y = startY; y >= minY; y--) {
			BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
			BlockState state = world.getBlockState(checkPos);
			if (!state.isAir() && !state.isLiquid()) {
				if (world.getBlockState(checkPos.down()).isAir()) {
					return checkPos;
				}
			}
		}
		return null;
	}

	private boolean hasSnowOnTop(StructureWorldAccess world, BlockPos pos) {
		BlockState topState = world.getBlockState(pos.up());
		Block topBlock = topState.getBlock();
		return topBlock == Blocks.SNOW || topBlock == Blocks.SNOW_BLOCK || topState.isOf(DirtSlabBlocks.SNOW_LAYER_SLAB);
	}

	private boolean isWaterAdjacent(StructureWorldAccess world, BlockPos pos) {
		for (Direction dir : Direction.values()) {
			if (dir == Direction.DOWN) continue;
			if (world.getBlockState(pos.offset(dir)).getBlock() == Blocks.WATER) return true;
		}
		return false;
	}

	private boolean isOverhangEdge(StructureWorldAccess world, BlockPos pos) {
		int edgeCount = 0;

		for (Direction dir : Direction.Type.HORIZONTAL) {
			BlockPos adjacent = pos.offset(dir);
			BlockState adjacentState = world.getBlockState(adjacent);

			if (adjacentState.isAir() || adjacentState.isLiquid()) {
				edgeCount++;
				continue;
			}

			if (world.getBlockState(adjacent.up()).isAir()) {
				edgeCount++;
			}
		}

		return edgeCount >= 1 && edgeCount <= 3;
	}

	private boolean isTerrainEdge(StructureWorldAccess world, BlockPos pos) {
		int airOrLowerCount = 0;

		for (Direction dir : Direction.Type.HORIZONTAL) {
			BlockPos adjacent = pos.offset(dir);
			BlockState adjacentState = world.getBlockState(adjacent);

			if (adjacentState.isAir() || adjacentState.isLiquid()) {
				airOrLowerCount++;
				continue;
			}

			if (world.getBlockState(adjacent.down()).isAir()) {
				airOrLowerCount++;
			}
		}

		return airOrLowerCount >= 1 && airOrLowerCount <= 3;
	}
}
