package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabSnowLayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {

	/**
	 * After vanilla chunk tick, check if snow should be placed on our slabs
	 * This handles weather-based snow accumulation
	 */
	@Inject(method = "tickChunk", at = @At("TAIL"))
	private void handleSnowOnSlabs(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
		ServerLevel world = (ServerLevel)(Object)this;

		// Only proceed if it's raining (which includes snow in cold biomes)
		if (!world.isRaining()) {
			return;
		}

		// Random chance check similar to vanilla
		if (world.getRandom().nextInt(16) != 0) {
			return;
		}

		// Get a random position in the chunk
		BlockPos startPos = world.getBlockRandomPos(
			chunk.getPos().getMinBlockX(), 0, chunk.getPos().getMinBlockZ(), 15
		);

		// Get the top position that blocks motion
		BlockPos topPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, startPos);
		BlockState stateAtTop = world.getBlockState(topPos);

		// Case 1: heightmap returned the position of a bottom slab directly
		if (SlabRegistry.isTerrainSlab(stateAtTop.getBlock()) && stateAtTop.hasProperty(SlabBlock.TYPE)
			&& stateAtTop.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
			tryPlaceSnowOnSlab(world, stateAtTop, topPos.above());
			return;
		}

		// Case 2: heightmap returned the air position above a bottom slab
		BlockPos belowPos = topPos.below();
		BlockState belowState = world.getBlockState(belowPos);

		if (SlabRegistry.isTerrainSlab(belowState.getBlock()) && belowState.hasProperty(SlabBlock.TYPE)
			&& belowState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
			tryPlaceSnowOnSlab(world, belowState, topPos);
		}
	}

	private static void tryPlaceSnowOnSlab(ServerLevel world, BlockState slabState, BlockPos snowPos) {
		Biome biome = world.getBiome(snowPos).value();
		if (biome.getPrecipitationAt(snowPos, world.getSeaLevel()) != Biome.Precipitation.SNOW) return;

		BlockState stateAtSnowPos = world.getBlockState(snowPos);

		if (stateAtSnowPos.isAir()) {
			world.setBlock(snowPos, SlabSnowLayerBlock.createForSlab(slabState), Block.UPDATE_ALL);
		} else if (stateAtSnowPos.is(DirtSlabBlocks.SNOW_LAYER_SLAB)) {
			int currentLayers = stateAtSnowPos.getValue(SlabSnowLayerBlock.LAYERS);
			if (currentLayers < 8) {
				world.setBlock(snowPos, stateAtSnowPos.setValue(SlabSnowLayerBlock.LAYERS, currentLayers + 1), Block.UPDATE_ALL);
			}
		}
	}
}
