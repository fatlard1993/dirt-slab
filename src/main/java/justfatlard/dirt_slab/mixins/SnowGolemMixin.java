package justfatlard.dirt_slab.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;

import justfatlard.dirt_slab.Main;
import justfatlard.dirt_slab.SlabSnowLayerBlock;

@Mixin(SnowGolemEntity.class)
public class SnowGolemMixin {

	/**
	 * After the snow golem tick, check if we need to place snow on our slabs
	 * This handles snow golem snow trail on bottom slabs
	 */
	@Inject(method = "tickMovement", at = @At("TAIL"))
	private void handleSnowTrailOnSlabs(CallbackInfo ci) {
		SnowGolemEntity self = (SnowGolemEntity)(Object)this;
		World world = self.getEntityWorld();

		if (!world.isClient() && world instanceof ServerWorld serverWorld) {
			// Check game rule
			if (!serverWorld.getGameRules().getValue(GameRules.DO_MOB_GRIEFING)) {
				return;
			}

			// Check if the biome is cold enough for snow golem to leave trail
			// Snow golems can leave snow in biomes with temperature < 0.8
			BlockPos golemPos = self.getBlockPos();
			if (world.getBiome(golemPos).value().isCold(golemPos, serverWorld.getSeaLevel())) {
				// Check 4 positions around the golem (same logic as vanilla)
				for (int i = 0; i < 4; i++) {
					int x = MathHelper.floor(self.getX() + (double)((float)(i % 2 * 2 - 1) * 0.25F));
					int y = MathHelper.floor(self.getY());
					int z = MathHelper.floor(self.getZ() + (double)((float)(i / 2 % 2 * 2 - 1) * 0.25F));
					BlockPos pos = new BlockPos(x, y, z);

					BlockState stateAtPos = world.getBlockState(pos);

					// Case 1: Entity is standing on a bottom slab (floor(Y) = slab's Y position)
					// The entity's Y is ~64.5 when standing on a bottom slab at Y=64
					// So we check if the block at pos is a bottom slab and place snow above it
					if (Main.isAnySlab(stateAtPos.getBlock()) && stateAtPos.contains(SlabBlock.TYPE)) {
						SlabType type = stateAtPos.get(SlabBlock.TYPE);
						if (type == SlabType.BOTTOM) {
							BlockPos abovePos = pos.up();
							BlockState stateAbove = world.getBlockState(abovePos);
							if (stateAbove.isAir()) {
								BlockState snowState = SlabSnowLayerBlock.createForSlab(stateAtPos);
								world.setBlockState(abovePos, snowState, Block.NOTIFY_ALL);
							}
						}
						// TOP and DOUBLE slabs are handled by vanilla snow placement
						continue;
					}

					// Case 2: Standard case - air above a block
					BlockPos belowPos = pos.down();
					BlockState belowState = world.getBlockState(belowPos);

					if (stateAtPos.isAir() && Main.isAnySlab(belowState.getBlock())) {
						if (belowState.contains(SlabBlock.TYPE)) {
							SlabType type = belowState.get(SlabBlock.TYPE);
							if (type == SlabType.BOTTOM) {
								BlockState snowState = SlabSnowLayerBlock.createForSlab(belowState);
								world.setBlockState(pos, snowState, Block.NOTIFY_ALL);
							}
						}
					}
				}
			}
		}
	}
}
