package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import justfatlard.dirt_slab.SlabRegistry;
import justfatlard.dirt_slab.SlabSnowLayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.gamerules.GameRules;

@Mixin(SnowGolem.class)
public class SnowGolemMixin {

	/**
	 * After the snow golem tick, check if we need to place snow on our slabs
	 * This handles snow golem snow trail on bottom slabs
	 */
	@Inject(method = "aiStep", at = @At("TAIL"))
	private void handleSnowTrailOnSlabs(CallbackInfo ci) {
		SnowGolem self = (SnowGolem)(Object)this;
		Level world = self.level();

		if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
			// Check game rule
			if (!serverWorld.getGameRules().get(GameRules.MOB_GRIEFING)) {
				return;
			}

			// Check if the biome is cold enough for snow golem to leave trail
			// Snow golems can leave snow in biomes with temperature < 0.8
			BlockPos golemPos = self.blockPosition();
			if (world.getBiome(golemPos).value().coldEnoughToSnow(golemPos, serverWorld.getSeaLevel())) {
				// Check 4 positions around the golem (same logic as vanilla)
				for (int i = 0; i < 4; i++) {
					int x = Mth.floor(self.getX() + (double)((float)(i % 2 * 2 - 1) * 0.25F));
					int y = Mth.floor(self.getY());
					int z = Mth.floor(self.getZ() + (double)((float)(i / 2 % 2 * 2 - 1) * 0.25F));
					BlockPos pos = new BlockPos(x, y, z);

					BlockState stateAtPos = world.getBlockState(pos);

					// Case 1: Entity is standing on a bottom slab (floor(Y) = slab's Y position)
					// The entity's Y is ~64.5 when standing on a bottom slab at Y=64
					// So we check if the block at pos is a bottom slab and place snow above it
					if (SlabRegistry.isTerrainSlab(stateAtPos.getBlock()) && stateAtPos.hasProperty(SlabBlock.TYPE)) {
						SlabType type = stateAtPos.getValue(SlabBlock.TYPE);
						if (type == SlabType.BOTTOM) {
							BlockPos abovePos = pos.above();
							BlockState stateAbove = world.getBlockState(abovePos);
							if (stateAbove.isAir()) {
								BlockState snowState = SlabSnowLayerBlock.createForSlab(stateAtPos);
								world.setBlock(abovePos, snowState, Block.UPDATE_ALL);
							}
						}
						// TOP and DOUBLE slabs are handled by vanilla snow placement
						continue;
					}

					// Case 2: Standard case - air above a block
					BlockPos belowPos = pos.below();
					BlockState belowState = world.getBlockState(belowPos);

					if (stateAtPos.isAir() && SlabRegistry.isTerrainSlab(belowState.getBlock())) {
						if (belowState.hasProperty(SlabBlock.TYPE)) {
							SlabType type = belowState.getValue(SlabBlock.TYPE);
							if (type == SlabType.BOTTOM) {
								BlockState snowState = SlabSnowLayerBlock.createForSlab(belowState);
								world.setBlock(pos, snowState, Block.UPDATE_ALL);
							}
						}
					}
				}
			}
		}
	}
}
