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
import net.minecraft.world.rule.GameRules;

import justfatlard.dirt_slab.DirtSlabBlocks;
import justfatlard.dirt_slab.Main;
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
		// (this happens because bottom slabs have collision shape that blocks motion)
		if (Main.isAnySlab(stateAtTop.getBlock()) && stateAtTop.contains(SlabBlock.TYPE)) {
			SlabType type = stateAtTop.get(SlabBlock.TYPE);
			if (type == SlabType.BOTTOM) {
				BlockPos abovePos = topPos.up();
				Biome biome = world.getBiome(abovePos).value();
				if (biome.getPrecipitation(abovePos, world.getSeaLevel()) == Biome.Precipitation.SNOW) {
					BlockState stateAbove = world.getBlockState(abovePos);
					if (stateAbove.isAir()) {
						BlockState snowState = SlabSnowLayerBlock.createForSlab(stateAtTop);
						world.setBlockState(abovePos, snowState, Block.NOTIFY_ALL);
					} else if (stateAbove.isOf(DirtSlabBlocks.SNOW_LAYER_SLAB)) {
						int currentLayers = stateAbove.get(SlabSnowLayerBlock.LAYERS);
						if (currentLayers < 8) {
							world.setBlockState(abovePos, stateAbove.with(SlabSnowLayerBlock.LAYERS, currentLayers + 1), Block.NOTIFY_ALL);
						}
					}
				}
			}
			return;
		}

		// Case 2: heightmap returned the air position above a bottom slab
		BlockPos belowPos = topPos.down();
		BlockState belowState = world.getBlockState(belowPos);

		if (Main.isAnySlab(belowState.getBlock()) && belowState.contains(SlabBlock.TYPE)) {
			SlabType type = belowState.get(SlabBlock.TYPE);

			if (type == SlabType.BOTTOM) {
				Biome biome = world.getBiome(topPos).value();
				if (biome.getPrecipitation(topPos, world.getSeaLevel()) == Biome.Precipitation.SNOW) {
					// If the position is air, place our slab snow
					if (stateAtTop.isAir()) {
						BlockState snowState = SlabSnowLayerBlock.createForSlab(belowState);
						world.setBlockState(topPos, snowState, Block.NOTIFY_ALL);
					}
					// If there's already our slab snow, try to add layers (up to 8)
					else if (stateAtTop.isOf(DirtSlabBlocks.SNOW_LAYER_SLAB)) {
						int currentLayers = stateAtTop.get(SlabSnowLayerBlock.LAYERS);
						if (currentLayers < 8) {
							world.setBlockState(topPos, stateAtTop.with(SlabSnowLayerBlock.LAYERS, currentLayers + 1), Block.NOTIFY_ALL);
						}
					}
				}
			}
		}
	}
}
