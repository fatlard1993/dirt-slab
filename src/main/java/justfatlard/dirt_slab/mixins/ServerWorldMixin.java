package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabSnowLayerBlock;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

	/**
	 * After vanilla chunk tick, check if snow should be placed on our slabs
	 * This handles weather-based snow accumulation
	 */
	@Inject(method = "tickChunk", at = @At("TAIL"))
	private void handleSnowOnSlabs(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
		ServerWorld world = (ServerWorld)(Object)this;

		// Only proceed if it's raining (which includes snow in cold biomes)
		if (!world.isRaining()) {
			return;
		}

		// Random chance check similar to vanilla
		if (world.random.nextInt(16) != 0) {
			return;
		}

		// Get a random position in the chunk
		BlockPos startPos = world.getRandomPosInChunk(
			chunk.getPos().getStartX(), 0, chunk.getPos().getStartZ(), 15
		);

		// Get the top position that blocks motion
		BlockPos topPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, startPos);
		BlockState stateAtTop = world.getBlockState(topPos);

		// Case 1: heightmap returned the position of a bottom slab directly
		if (SlabRegistry.isTerrainSlab(stateAtTop.getBlock()) && stateAtTop.contains(SlabBlock.TYPE)
			&& stateAtTop.get(SlabBlock.TYPE) == SlabType.BOTTOM) {
			tryPlaceSnowOnSlab(world, stateAtTop, topPos.up());
			return;
		}

		// Case 2: heightmap returned the air position above a bottom slab
		BlockPos belowPos = topPos.down();
		BlockState belowState = world.getBlockState(belowPos);

		if (SlabRegistry.isTerrainSlab(belowState.getBlock()) && belowState.contains(SlabBlock.TYPE)
			&& belowState.get(SlabBlock.TYPE) == SlabType.BOTTOM) {
			tryPlaceSnowOnSlab(world, belowState, topPos);
		}
	}

	private static void tryPlaceSnowOnSlab(ServerWorld world, BlockState slabState, BlockPos snowPos) {
		Biome biome = world.getBiome(snowPos).value();
		if (biome.getPrecipitation(snowPos, world.getSeaLevel()) != Biome.Precipitation.SNOW) return;

		BlockState stateAtSnowPos = world.getBlockState(snowPos);

		if (stateAtSnowPos.isAir()) {
			world.setBlockState(snowPos, SlabSnowLayerBlock.createForSlab(slabState), Block.NOTIFY_ALL);
		} else if (stateAtSnowPos.isOf(DirtSlabBlocks.SNOW_LAYER_SLAB)) {
			int currentLayers = stateAtSnowPos.get(SlabSnowLayerBlock.LAYERS);
			if (currentLayers < 8) {
				world.setBlockState(snowPos, stateAtSnowPos.with(SlabSnowLayerBlock.LAYERS, currentLayers + 1), Block.NOTIFY_ALL);
			}
		}
	}
}
